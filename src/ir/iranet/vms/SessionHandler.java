package ir.iranet.vms;
import ir.iranet.vms.util.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import com.sun.net.ssl.internal.www.protocol.https.Handler;

import sun.security.jca.GetInstance.Instance;
import ir.iranet.vms.sip.SIPMessage;
import ir.iranet.vms.sip.SIPRequest;
import ir.iranet.vms.sip.SIPResponse;
import ir.iranet.vms.util.VMSLogger;
import ir.iranet.vms.util.Utility;
import ir.iranet.vms.util.AppendFileStream;
import ir.iranet.vms.util.TimeoutInt;
import ir.iranet.vms.util.VMSProperties; 

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:12:07 AM
 * To change this template use File | Settings | File Templates.
 */

enum SessionState {
	start, s0, s1, s2, s3, s4, s5, s6,s7  
}

public class SessionHandler extends Thread
{
	
	boolean sessionAlive;
	boolean processingRequest=false;
	SessionState state;	
	TimeoutInt timer;
	ArrayList ports=new ArrayList();
	ArrayList addresses=new ArrayList();	
	HashMap sessionHash;
	Integer destRtpPort=new Integer(0);
	Integer srcRtpPort=new Integer(0);
	InetAddress DestAddress; 
	RTPServer rtpServer;
	HashMap rtpHash;
	String mailBox;
	int rtpState; 
	int byeCounter;
	int requestCounter;	
	int key;
	int maxMsg;
	boolean endSession=false;
		
	TimeoutInt sessionTimer;
	
	SIPResponse sipResponse;
	SIPRequest sipRequest;   
	SIPMessage sipMessage;
	
	
	/////////////////////////////////////////////
	// Constructor                             //
	/////////////////////////////////////////////
	
	public SessionHandler ()
	{
		state = SessionState.start;
		rtpHash=new HashMap();	
		rtpState=-1;
		
		byeCounter=0;
		requestCounter=1;
		key=-1;
		maxMsg=30;
	}
	
	public SessionHandler (SIPMessage sipMessage,HashMap sh)
	{	
		state = SessionState.start;
		rtpState=-1;
		rtpHash=new HashMap();
		
		sessionHash =new HashMap();	
		this.sipMessage = sipMessage;
		sessionHash=sh;
		
		byeCounter=0;
		requestCounter=1;
		key=-1;
		maxMsg=30;
	}
	
	//////////////////////////////////////////////////////////
	
	/**********************************************************
	 * This method recieves a SIP message ,determines         *
	 * that the message is request or response and determines *
	 * the state of this session ,then calls related method   *
	 * ********************************************************/		
	public void AddNewSip (SIPMessage sipMessage,HashMap sh) throws UnknownHostException,IOException, InterruptedException
	{
		
		//Determining type of the SIP message (Request message or Response message)
		
		if(sipMessage instanceof SIPRequest)
		{
			// The SIP message is a request message
			
			sipRequest = (SIPRequest) sipMessage;	    	
			VMSLogger.log(Level.INFO ,"******Add new SIP message in state: "+state+"******");
		
			//Determining the state of this session
			
			switch (state)
			{
				case start:
				
					stateStart(sipRequest);						
					break;	
				
				case s0:
				
					stateS0(sipRequest);
					break;
				
				case s1:
							
					stateS1(sipRequest);			
					break;
				
				case s2:
				
					stateS2(sipRequest);
					break;
				
				case s3:
					
					stateS3(sipResponse);					
					break;
				
				case s4:
				
					stateS4(sipRequest);
					break;
				
				case s5:
				
					stateS5(sipRequest);
					break;
					
				case s6:
					
					stateS6(sipRequest);
					break;
			}
		}
		else			
		{
			//The SIP message is a response message
			
			sipResponse=new SIPResponse();			
			System.out.println("state: "+state);
			
			switch (state)
			{
				case s3:
					
					stateS3(sipResponse);
					break;
			}
			
		}
	}
				
    /**********************************************************************
     * At any where that server is waiting to happen an event,a timer sets*
     * that if the time expire,act suitable function. 
     **********************************************************************/
	/////////////////////////////////////////////////////////////////
	public void TimeOut () throws NumberFormatException, IOException, InterruptedException
	{
		
		VMSLogger.log(Level.INFO,"Time out in state :"+state);
		//timeout in state start
		
		SIPRequest byeRequest;
		InetAddress address;
		TimeoutInt t;
		
		switch(state)
		{
		
			case start:
					
				sipResponse=Utility.response487(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
				sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				if(sessionHash!=null)
				if(sessionHash.containsValue(sipRequest.getCallId())== true)
					sessionHash.remove(sipRequest.getCallId());												
			    sessionTimer.setStop(true);
				break;
					
			case s0:
				
				///////////////////////////////
				byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(10000,this);    //Ehsan
				sessionTimer.start();
				//////////////////////////////
				break;
				
			case s1:
				/////////////////////////////////////////////////
				
				Set set = new HashSet();
				set=rtpHash.keySet();		
			
				Iterator iter = set.iterator();
				
				ArrayList rtpAList=new ArrayList();
				
				while (iter.hasNext())
		        {
		             rtpAList.add(iter.next());	        
		        }
							
				/////////////////////////// Sort RTP Array List
				for(int i=0;i<rtpAList.size()-1;i++)
				{				
					for(int j=i+1;j>0;j--)
					{
						if( Utility.CompareBytes((byte[])rtpAList.get(j),(byte[])rtpAList.get(j-1))<0)
						{						
							Object temp;
							temp=rtpAList.get(j);
							rtpAList.set(j,rtpAList.get(j-1));
							rtpAList.set(j-1,temp);
						}
						else
						{
							j=0;
						}
					}
				}
				
				////////////////////////////////////////////////
				
				byte[] media=new byte[0];
				
				for(int i=0;i<rtpAList.size();i++)
				{				
					media=Utility.Append(media, (byte[])rtpHash.get(rtpAList.get(i)));
				}
				
				// Create a mailBox for callee 			
				String spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox;
				System.out.println("------------------------"+mailBox+"---------------");
				File f = new File(spoonFeeding);
				f.mkdir();
				
				spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage";
				f= new File(spoonFeeding);
				f.mkdir();
				
				spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox+"\\OldMessage";
				f= new File(spoonFeeding);
				f.mkdir();
				
							
				try {	
					
					//Save message
					Long time=System.nanoTime();
			        File file = new File("D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\msg"+time.toString()+".gsm");
			        VMSLogger.log(Level.INFO,"create a file: msg"+time.toString()+".gsm");
			        // Create file if it does not exist
			        boolean success = file.createNewFile();
			        if (success) {
			        	System.out.println("File did not exist and was created");		        	
			            // File did not exist and was created
			        } else {
			            // File already exists
			        }
	                FileOutputStream fos = new  FileOutputStream(file);
			        
			        for(int i=0;i<media.length;i++)
			        	fos.write(media[i]);
			       
			        fos.close();
			        
			     
				/////////////////////////////////////////////////////////////////////
				
				RandomAccessFile rafNew=new RandomAccessFile(new File("VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\Info.txt"),"rw");
        		rafNew.seek(0);
        		int a;
        		String str;
        		if(rafNew.readLine()==null)
        		{
        			try
		    		{
		    		    // Open an output stream
		        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\Info.txt");

		    		    // Print a line of text
		    		    new PrintStream(fout).println (1+"-");

		    		    // Close our output stream
		    		    fout.close();		
		    		}
		    		// Catches any error conditions
		    		catch (IOException e)
		    		{
		    		}	
        			
        		}
        		else
        		{		            			
        		   	
		        	rafNew.seek(0);
		        	str = rafNew.readLine();
		        	str=str.substring(0,str.indexOf("-"));
		        	//For restrict message number 
		        	a=Integer.parseInt(new String(str));		    		        			    		        			    		        	    		        				       
		        		
        			if(a == maxMsg)
        			{
        						            						    		  		            		
        				rafNew.seek(0);
        				rafNew.readLine();
        				String line=rafNew.readLine();
        				rafNew.close();
        				new Utility().deleteLine("VoiceMailBox\\"+mailBox+"\\NewMessage\\Info.txt",line);
        				line=line.substring(line.indexOf(" ")+1);
        				File extFile= new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\"+line.substring(0,line.indexOf("-"))+".gsm");						
        				extFile.delete();
        				
        			}
        			else
        			{
        				try
			    		{
			    		    // Open an output stream
			        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");

			    		    // Print a line of text
			        		a++;
			    		    new PrintStream(fout).print (a+"-");

			    		    // Close our output stream
			    		    fout.close();		
			    		}
			    		// Catches any error conditions
			    		catch (IOException e)
			    		{
			    		}	
			    		File tempFile = new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");	
			    		RandomAccessFile tempRAF=new RandomAccessFile(tempFile,"rw");
			    		tempRAF.seek(0);
			    		String tempLine="";
			    		tempLine=tempRAF.readLine();
			    		tempRAF.close();
			    		tempFile.delete();
			    		
			    		rafNew.seek(0);
			    		rafNew.writeBytes(tempLine);
			    		
		        	}
        		}				            					       
	        	
        	//	PrintStream out = new PrintStream(new AppendFileStream("VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\Info.txt"));				        			        		
			//	out.println(Info);	            	
			//	out.close();	
			
		/////////////////////////////////////////////////////////////////////////////////////        		        
		        PrintStream out = new PrintStream(new AppendFileStream("D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\Info.txt"));
		        String todayStr="";
		        
		        try {	
		        	Calendar cal = Calendar.getInstance();		        	
		        	SimpleDateFormat fmt =new SimpleDateFormat("MM-dd-yyyy");
		        	todayStr = fmt.format(cal.getTime());
		        }
		        catch(ClassCastException e) 
		        {
			           System.out.println(e.toString());
			    }
		        			        	
	            out.print(todayStr);
	            out.println(" msg"+time.toString()+"-");
	            
	            out.close();
			           		       
		    } 
			catch (IOException e) {
		    }
			/////////////////////////////////////////////////////////////////////////////
        		byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));				
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				state=SessionState.s3;
				
				sessionTimer=new TimeoutInt(10000,this);            //Ehsan
				sessionTimer.start();
				
				/////////////////////////////////////////////////
				break;
				
			case s2:
				//////////////////////////////////
				byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(10000,this);    //Ehsan
				sessionTimer.start();
				//////////////////////////////////
				break;
				
			case s3:
				
				///////////////////////////////
				byeCounter++;
				if(byeCounter<3)
				{
					byeRequest=new SIPRequest();
					byeRequest=ByeRequest(sipRequest);
					address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
					byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
					sessionTimer=new TimeoutInt(10000,this);  //Ehsan
					sessionTimer.start();
				}
				else
				{		
					sessionTimer.setStop(true);
					if(sessionHash!=null)
					if(sessionHash.containsValue(sipRequest.getCallId())== true)
						sessionHash.remove((Object)sipRequest.getCallId());
					
					//this.yield();
				}
				////////////////////////////
				break;
				
							
			case s5:
				
				///////////////////////////////
				byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(10000,this);   //Ehsan
				sessionTimer.start();
				//////////////////////////////
				break;
				
			case s6:
				
				///////////////////////////////
				byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(10000,this);    //Ehsan
				sessionTimer.start();
				//////////////////////////////
				break;
		}		
		
	}
	
	/*********************************************************************************
	 * At this point the pressed key are handles.It means that in the middle of      *
	 * the leaving a message if user press # the message not saved and if in the     *
	 * the Retrieving message after reading first menue if user presses 1,it         *
	 * means that user wants to go to the new message and 2 means to the old message.*
	 * then if user press 1 means to play message,2 means to delete message,3 to go  *
	 * to the next message and 0 to go to the previous step.                         * 
	 * @param option
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws InterruptedException
	 ********************************************************************************/
	////////////////////////////////////////////////////////////////////
	public synchronized void Alarm(int option) throws NumberFormatException, IOException, InterruptedException
	{
						
		if(rtpState==0)
		{
			if(option==11)
			{
				sessionTimer.setStop(true);
				
				Session session=new Session();
				session.setRtpPort(destRtpPort);
				session.setDestAddress(DestAddress);
				RTPHandler.play(session,"sounds\\menu6",this);
				
				SIPRequest byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=new SIPRequest();
				byeRequest=ByeRequest(sipRequest);
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
				
				sessionTimer.setStop(true);
				
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(10000,this);      //Ehsan
				sessionTimer.start();
												
			}
		}
		
		else if(rtpState==1)
		{
			if(option==1)
			{
				sessionTimer.setStop(true);
				
				rtpHash.clear();
				Session session=new Session();
				session.setRtpPort(destRtpPort);
				session.setDestAddress(DestAddress);
				state=SessionState.s5;	
				RTPHandler.play(session,"sounds\\menu4",this);						
				rtpState=2;														
				new PlayMsgInfo("VoiceMailBox\\"+mailBox+"\\NewMessage\\",this).start();				
					
			}
			else if(option==2)
			{
				sessionTimer.setStop(true);
				rtpHash.clear();
				Session session=new Session();
				session.setRtpPort(destRtpPort);
				session.setDestAddress(DestAddress);				
				RTPHandler.play(session,"sounds\\menu4",this);				
				rtpState=3;
				state=SessionState.s6;
				
				new PlayMsgInfo("VoiceMailBox\\"+mailBox+"\\OldMessage\\",this).start();
				
			}
		}
		else if(rtpState==2 || rtpState==3)
		{
			
			if(option==0)
			{
				key=0;
			}
			else if(option==1)
			{
				key=1;
			}
			else if(option==2)
			{
				key=2;
			}
			else if(option==3)
			{
				key=3;
			}
			else if(option==4)
			{
				key=4;				
			}
		}		
	}
	
	////////////////////////////////////////////////////////////////////
	// Start state                                                    //
	////////////////////////////////////////////////////////////////////
	/**************************************************************************
	 * This state receives the first message of the session,                  *
	 * if the requested message is accepted then sends ok and goto the state 2*
	 * else sends suitable error response.                                                    *
	 **********************************************************************/
	private void stateStart(SIPRequest sipRequest) throws UnknownHostException,IOException, InterruptedException 
	{
		
		VMSLogger.log(Level.INFO,"START stateStart "+" CallId:"+sipRequest.getCallId() + ", Method: " + sipRequest.getMethod());

        if(Utility.rtpPorts.isEmpty())
		{
			//all of RTP port are busy
			SIPResponse response=Utility.response486(sipRequest);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			response.send(response.makeSipResponse().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			//this.yield();
		}
		else
		{		
			//allocate a new RTP port to this session
			Iterator iter = Utility.rtpPorts.iterator();
			srcRtpPort=(Integer)iter.next();
			System.out.println(Utility.rtpPorts);
			Utility.rtpPorts.remove(srcRtpPort);
			System.out.println("------------------------Source rtp port:"+srcRtpPort);
		}

		if(processingRequest==false)
		{
		 processingRequest=true;
		  // Check METHOD
		  if(! Utility.methods.contains(sipRequest.getMethod()))
		  {
			// Generate the Response405
			VMSLogger.log(Level.INFO,"Method Not Allowed");			
			String str="";
			String[] array = (String[])Utility.methods.toArray(new String[Utility.methods.size()]);
			for(int i=0;i<array.length;i++)
			{
				if(str.equals(""))
					str=" "+array[i];
				else
					str=str+","+array[i];
			}
			str=str+"\r";
			
			sipResponse = new SIPResponse();
			sipResponse=Utility.response405(sipRequest);
			sipResponse.setAllow(str);
			VMSLogger.log(Level.INFO,"405 Response is generated");
			String s = sipResponse.toString();
			VMSLogger.log(Level.INFO,"\n"+s);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			processingRequest=false;			
			return;
					
		  }
		  else if(sipRequest.getMethod().equals("INVITE") || sipRequest.getMethod().equals("OPTIONS")) // Ehsan
		  {
			
			  
			  if(sipRequest.getExpires()!=null) // if Expiration, send 487
			  {
				  int time=Integer.parseInt(new String(sipRequest.getExpires().getBytes()));;
				  sessionTimer=new TimeoutInt(time,this);
				  sessionTimer.start();
			  }
			  //Check Content-Encoding header
			  if(sipRequest.getContent_Encoding()!=null)
			  {
				 if (! Utility.contentEncodings.contains(sipRequest.getContent_Encoding()))
				 {				
					//Generate the Response415				
					String str="";
					String[] array = (String[])Utility.contentEncodings.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					
					sipResponse = new SIPResponse();
					sipResponse=Utility.response415(sipRequest);
					sipResponse.setAccept_Encoding(str);
					VMSLogger.log(Level.INFO,"415 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					if(sessionHash!=null)
					if(sessionHash.containsValue(sipRequest.getCallId())== true)
						sessionHash.remove(sipRequest.getCallId());
					sessionTimer.setStop(true);
					processingRequest=false;
					return;
								            
				}
								
			  }
			
			  //check Content-Language header
			  if(sipRequest.getContent_Language()!=null)
			  {
				if (! Utility.contentLanguages.contains(sipRequest.getContent_Language()))
				{				
					//Generate Response415								
					String str="";
					String[] array = (String[])Utility.contentLanguages.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse = new SIPResponse();
					sipResponse=Utility.response415(sipRequest);
					sipResponse.setAccept_Language(str);
					VMSLogger.log(Level.INFO,"415 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					if(sessionHash!=null)
					if(sessionHash.containsValue(sipRequest.getCallId())== true)
						sessionHash.remove(sipRequest.getCallId());
					sessionTimer.setStop(true);
					processingRequest=false;
					return;
					
				}
			  }
			
			  //Check Content-Type header
			  if(sipRequest.getContent_Type()!=null)
			  {
				if(! Utility.contentTypes.contains(sipRequest.getContent_Type()))
				{				
					//Generate Response415				
					String str="";
					String[] array = (String[])Utility.contentTypes.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse = new SIPResponse();
					sipResponse=Utility.response415(sipRequest);
					sipResponse.setAccept(str);
					VMSLogger.log(Level.INFO,"415 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					if(sessionHash!=null)
					if(sessionHash.containsValue(sipRequest.getCallId())== true)
						sessionHash.remove(sipRequest.getCallId());
					sessionTimer.setStop(true);
					processingRequest=false;
					return;
					
				}
			  }
			
			  // Check Content-Dispositions header
			  if(sipRequest.getContent_Desposition()!=null)
			  {
				if(! Utility.contentDispositions.contains(sipRequest.getContent_Desposition()))
				{				
					//Generate Response415			
					VMSLogger.log(Level.INFO,"415 Response is generated");
					sipResponse = new SIPResponse();
					sipResponse=Utility.response415(sipRequest);
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					if(sessionHash!=null)
					if(sessionHash.containsValue(sipRequest.getCallId())== true)
						sessionHash.remove(sipRequest.getCallId());
					sessionTimer.setStop(true);
					processingRequest=false;
					return;					
				}							
			  }

			  if(!sipRequest.getContent_Length().equals("0"))
			  {
				  if(sipRequest.sdp.sessionDescription.getA()!=null)
				  {			
					  int count=sipRequest.sdp.sessionDescription.getA().size();
					  for(int i=0;i<count;i++)
					  {
						  if(! Utility.attributes.contains(sipRequest.sdp.sessionDescription.getA(i)))
						  {
							  sipResponse = new SIPResponse();
							  sipResponse=Utility.response306(sipRequest);
							  VMSLogger.log(Level.INFO,"306 Response is generated");
							  String s = sipResponse.toString();
							  VMSLogger.log(Level.INFO,"\n"+s);
							  InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
							  sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
							  if(sessionHash!=null)
							  if(sessionHash.containsValue(sipRequest.getCallId())== true)
								  sessionHash.remove(sipRequest.getCallId());
							  sessionTimer.setStop(true);
							  processingRequest=false;
							  return;				
						  }
					  }		
				  }
				
			  }		
			  if (sipRequest.getMethod().equals("INVITE"))
			  {
				sipRequest.getTo();
				
				if(sipRequest.getContact()!=null)
				{
					int count=sipRequest.getContact().size();
					for(int i=0;i<count;i++)
					{
						String str=sipRequest.getContact(i);					
						str=str.substring(str.indexOf("@")+1);										
						//addresses.add(i,InetAddress.getByName(str.substring(0,str.indexOf(":"))));
						//str=str.substring(str.indexOf(":")+1);
						//ports.add(i,Integer.parseInt(new String(str.substring(0,str.indexOf(">")).getBytes())));
                        addresses.add(i,InetAddress.getByName(str.substring(0,str.indexOf(">"))));  // Ehsan

						ports.add(i,5060);
                    }
				}

				String str="";
				/*************************************************************
				 * IF a SIP Message consists of body then we extract RTP port*
				 * and destination address from M & O parameters.            *
				 * ***********************************************************/
				if(!sipRequest.getContent_Length().equals("0"))
				{
					str=sipRequest.sdp.mediaDescription.getM();				
					str=str.substring(str.indexOf(" ")+1);				
					str=str.substring(0,str.indexOf(" "));				
					destRtpPort=Integer.parseInt(new String(str.getBytes()));
					System.out.println("dest trp port: "+destRtpPort);
					
					str=sipRequest.sdp.sessionDescription.getO();				
					str=str.substring(str.indexOf("IP4")+4);				
					str=str.substring(0,str.indexOf("\r"));
					DestAddress=InetAddress.getByName(str);

				}
				
				sipResponse = new SIPResponse();
				sipResponse=Utility.response200(sipRequest,srcRtpPort);
				VMSLogger.log(Level.INFO,"200 Response is generated");
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));																		
				String s = sipResponse.toString();
				VMSLogger.log(Level.INFO,"\n"+s);
				sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				
				state=SessionState.s0;
				if(sessionTimer!=null)
					sessionTimer.setStop(true);
				
				
				sessionTimer=new TimeoutInt(50000,this);        //Ehsan
				sessionTimer.start();				
				
				processingRequest=false;
				
				return;
							
			}
			else if(sipRequest.getMethod().equals("OPTIONS"))   // Ehsan
			{				
				//require Ack?
				sipResponse=Utility.response200(sipRequest,srcRtpPort);
				String s = sipResponse.toString();
				VMSLogger.log(Level.INFO,"\n"+s);
				VMSLogger.log(Level.INFO,"200 Response is generated");
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
				sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				processingRequest=false;
				return;
			}
		  }
		  else if(sipRequest.getMethod().equals("BYE"))
		  {
			  	sipResponse=Utility.response200(sipRequest,srcRtpPort);			
				String s = sipResponse.toString();
				VMSLogger.log(Level.INFO,"\n"+s);
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
				sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));							
				state=SessionState.start;
				if(sessionHash!=null)
				if(sessionHash.containsValue(sipRequest.getCallId())== true)
					sessionHash.remove(sipRequest.getCallId());
				sessionTimer.setStop(true);
				processingRequest=false;
				//this.stop();
				return;		
		  }
		}
		else //processingRequest==true
		{			
			if(sipRequest.getMethod().equals("CANCEL"))
			{
				VMSLogger.log(Level.INFO,"CANCEL Recieved.");
				sipResponse=Utility.response487(sipRequest);
				VMSLogger.log(Level.INFO,"487 Response is generated");
				String s = sipResponse.toString();
				VMSLogger.log(Level.INFO,"\n"+s);				
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
				sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				if(sessionHash!=null)
				if(sessionHash.containsValue(sipRequest.getCallId())== true)
					sessionHash.remove(sipRequest.getCallId());	
				sessionTimer.setStop(true);
				processingRequest=false;
				//this.stop();
			}
			processingRequest=false;
		}		
		VMSLogger.log(Level.INFO, "END stateStart"+" CallId:"+sipRequest.getCallId());		
		
	}	
	
	/////////////////////////////////////////////////////////////////////
	//  S0 state                                                       //
	/////////////////////////////////////////////////////////////////////
	/************************************************************************
	 * At this state if Ack request is received,the session is complete and * 
	 * research the request is for retrieving message or leaveing message   *
	 * if it is for retrieving message then plays menu by rtphandler then   *
	 * rtpserver waits until any key pressed then the remained processes    *
	 * continue else is for leaving message rtpserver waits for limited time*
	 * to user leaves his message expect user presses # as doesn't save it. * 
	 ************************************************************************/
	private void stateS0(SIPRequest sipRequest) throws IOException, InterruptedException 	
	{
		VMSLogger.log(Level.INFO, "START stateS0 : Received Ack");
		SIPResponse sipResponse = new SIPResponse();
		Utility.set();
		if(sipRequest.getMethod().equals("CANCEL"))
		{
			sessionTimer.setStop(true);
			sipResponse=Utility.response200(sipRequest,srcRtpPort);			
			String s = sipResponse.toString();
			VMSLogger.log(Level.INFO,"\n"+s);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));					
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			//this.yield();			
			
			return;					
		}
		else if(sipRequest.getMethod().equals("ACK"))
		{	

			sessionTimer.setStop(true);
			if(!sipRequest.getContent_Length().equals(" 0\r"))
			{
				String str=sipRequest.sdp.mediaDescription.getM();				
				str=str.substring(str.indexOf(" ")+1);				
				str=str.substring(0,str.indexOf(" "));				
				destRtpPort=Integer.parseInt(new String(str.getBytes()));
				System.out.println("dest rtp port: "+destRtpPort);
				
				str=sipRequest.sdp.sessionDescription.getO();				
				str=str.substring(str.indexOf("IP4")+4);				
				str=str.substring(0,str.indexOf("\r"));
				DestAddress=InetAddress.getByName(str);						
			}
			else if(destRtpPort==null || DestAddress==null)
			{
				//send a bye request
				SIPRequest byeRequest=new SIPRequest();		
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				String str=sipRequest.getContact(0);				
				str=str.substring(str.indexOf("@")+1);							
				InetAddress address=InetAddress.getByName(str.substring(0,str.indexOf(":")));
				str=str.substring(str.indexOf(":")+1);								
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(str.substring(0,str.indexOf(">")))));
                
				Utility.rtpPorts.add(srcRtpPort);
				//change state
				state=SessionState.s3;
				
				TimeoutInt t=new TimeoutInt(10000,this);  //Ehsan
				t.start();

				return;
			}
			
			String s=sipRequest.getTo();
			s=s.substring(s.indexOf("<")+1,s.indexOf(">"));
			///////////////////////////////////////////////////////////
			//Session is for Retrieve a message
			//////////////////////////////////////////////////////////
			VMSLogger.log(Level.INFO,"To Header:"+s);
		
			
		 if(s.equals("sip:2000@voip.iranet.ir"))
		   {
			   	//extract mailbox 
				String str=sipRequest.getFrom();							
				str=str.substring(str.indexOf(":")+1);				
				String ID=str.substring(0,str.indexOf("@"));	
				
				if((mailBox=Utility.Mailbox(ID))==null)
				{
					SIPRequest byeRequest;
					InetAddress address;
					TimeoutInt t;
					
					byeRequest=new SIPRequest();
					requestCounter++;
					byeRequest=ByeRequest(sipRequest);
					address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
					byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
					Utility.rtpPorts.add(srcRtpPort);
					state=SessionState.s3;
					sessionTimer=new TimeoutInt(10000,this);  //Ehsan
					sessionTimer.start();
					
				}	
				else
				{					
					VMSLogger.log(Level.INFO,"*******Request for retrieve the messages.******");
					Session session=new Session();
					session.setRtpPort(destRtpPort);
					session.setDestAddress(DestAddress);
					
					//play related menu	
					RTPHandler.play(session,"sounds\\menu0",this);
					state = SessionState.s2;
					rtpState=1;
				
					//Call rtpserver to listen to the RTP port to recieved pressed key
					//else in the limited time any key was'nt pressed the session expired
				
					rtpServer = new RTPServer(this,srcRtpPort,rtpHash);							
					rtpServer.start();
				
					sessionTimer=new TimeoutInt(10000,this);
					sessionTimer.start();
				}
		   }
          /////////////////////////////////////////////////////////////////		 
		  //Session is for Leave a message
		 //////////////////////////////////////////////////////////////////
		else
			{
				VMSLogger.log(Level.INFO,"******Request for leave a message.******");
				
				//extract mailbox 
				String str=sipRequest.getTo();							
				str=str.substring(str.indexOf(":")+1);				
				String ID=str.substring(0,str.indexOf("@"));	
				
				if((mailBox=Utility.Mailbox(ID))==null)
				{
					SIPRequest byeRequest;
					InetAddress address;
					TimeoutInt t;
					
					byeRequest=new SIPRequest();
					requestCounter++;
					byeRequest=ByeRequest(sipRequest);
					address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
					byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
					Utility.rtpPorts.add(srcRtpPort);
					state=SessionState.s3;
					sessionTimer=new TimeoutInt(1000,this);
					sessionTimer.start();
					
				}
				else
				{
					rtpState=0;
					Session session=new Session();
					session.setRtpPort(destRtpPort);
					session.setDestAddress(DestAddress);						
					//play related menu		    
					RTPHandler.play(session,"sounds\\menu1",this);
					state=SessionState.s1;		      
					
					sessionTimer=new TimeoutInt(3000,this);
					sessionTimer.start();
					
					//Call rtpserver to listen to the RTP port to recieved user message 
					//else the user presses # as doesn't save it
					rtpServer = new RTPServer(this,srcRtpPort,rtpHash);
					rtpServer.start();
				
				}
								
			}
							
		}
		else if(sipRequest.getMethod().equals("BYE"))
		{	
			endSession=true;
			
			sessionTimer.setStop(true);
			sipResponse=Utility.response200(sipRequest,srcRtpPort);			
			String s = sipResponse.toString();
			VMSLogger.log(Level.INFO,"\n"+s);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));										
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			//this.yield();
			
			return;				
		}
		
		VMSLogger.log(Level.INFO, "START stateS0"+" CallId:"+sipRequest.getCallId());
	}
	
	//////////////////////////////////////////////////////////////////////////
	//S1 state                                                            //
	/////////////////////////////////////////////////////////////////////////
	/*************************************************************************
	 * At this state may recieve bye message as saving the message before the* 
	 * limited time was expired or recieve invite message as modifiying the  * 
	 * properties of the session.* 
	 *************************************************************************/
	private void stateS1(SIPRequest sipRequest) throws NumberFormatException, IOException
	{
		
		VMSLogger.log(Level.INFO, "START stateS1"+" CallId:"+sipRequest.getCallId());
				
		if(sipRequest.getMethod().equals("INVITE"))
		{
			 state=SessionState.s4;
			 //Check Content-Encoding header
			 if(sipRequest.getContent_Encoding()!=null)
			 {
				 if (! Utility.contentEncodings.contains(sipRequest.getContent_Encoding()))
				 {				
					//Generate the Response488
							
					String str="";
					String[] array = (String[])Utility.contentEncodings.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);		
					sipResponse.setAccept_Encoding(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
					state=SessionState.s1;
		
					return;
								            
				}
								
			  }
			
			  //check Content-Language header
			  if(sipRequest.getContent_Language()!=null)
			  {
				if (! Utility.contentLanguages.contains(sipRequest.getContent_Language()))
				{				
					//Generate Response488							
					String str="";
					String[] array = (String[])Utility.contentLanguages.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);
					sipResponse.setAccept_Language(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s1;
					return;
					
				}
			  }
			
			  //Check Content-Type header
			  if(sipRequest.getContent_Type()!=null)
			  {
				if(! Utility.contentTypes.contains(sipRequest.getContent_Type()))
				{				
					//Generate Response488		
					String str="";
					String[] array = (String[])Utility.contentTypes.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);
					sipResponse.setAccept(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s1;
					return;
					
				}
			  }
			
			  // Check Content-Dispositions header
			  if(sipRequest.getContent_Desposition()!=null)
			  {
				if(! Utility.contentDispositions.contains(sipRequest.getContent_Desposition()))
				{				
					//Generate Response488						
					sipResponse=Utility.response488(sipRequest);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s1;
					return;
					
				}
							
			  }
			  if(!sipRequest.getContent_Length().equals("0"))
			  {
				  if(sipRequest.sdp.sessionDescription.getA()!=null)
				  {			
					  int count=sipRequest.sdp.sessionDescription.getA().size();
					  for(int i=0;i<count;i++)
					  {
						  if(! Utility.attributes.contains(sipRequest.sdp.sessionDescription.getA(i)))
						  {
							  sipResponse=Utility.response488(sipRequest);
							  VMSLogger.log(Level.INFO,"488 Response is generated");
							  String s = sipResponse.toString();
							  VMSLogger.log(Level.INFO,"\n"+s);
							  InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
							  sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
						
							  state=SessionState.s1;
							  return;				
						  }
					  }		
				  }
			  }
			  sipResponse=new SIPResponse();
			  sipResponse=Utility.response200(sipRequest,srcRtpPort);
			  InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			  sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
			  state=SessionState.s1;

		}
		else if(sipRequest.getMethod().equals("BYE"))
		{
			sessionTimer.setStop(true);
			state=SessionState.start;	
			SIPResponse sipResponse=Utility.response200(sipRequest,srcRtpPort);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				
			Set set = new HashSet();
			set=rtpHash.keySet();		
		
			Iterator iter = set.iterator();
			
			ArrayList rtpAList=new ArrayList();
			
			while (iter.hasNext())
	        {
	             rtpAList.add(iter.next());	        
	        }
						
			/////////////////////////// Sort RTP Array List
			for(int i=0;i<rtpAList.size()-1;i++)
			{				
				for(int j=i+1;j>0;j--)
				{
					if( Utility.CompareBytes((byte[])rtpAList.get(j),(byte[])rtpAList.get(j-1))<0)
					{						
						Object temp;
						temp=rtpAList.get(j);
						rtpAList.set(j,rtpAList.get(j-1));
						rtpAList.set(j-1,temp);
					}
					else
					{
						j=0;
					}
				}
			}
			
			////////////////////////////////////////////////
			
			byte[] media=new byte[0];
			
			for(int i=0;i<rtpAList.size();i++)
			{				
				media=Utility.Append(media, (byte[])rtpHash.get(rtpAList.get(i)));
			}
			
			// Create a mailBox for callee 			
			String spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox;						 
			File f = new File(spoonFeeding);
			f.mkdir();
			
			spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage";
			f= new File(spoonFeeding);
			f.mkdir();
			
			spoonFeeding = "D:\\vms\\VoiceMailBox\\"+mailBox+"\\OldMessage";
			f= new File(spoonFeeding);
			f.mkdir();
			
						
			try {	
								
				Long time=System.nanoTime(); 								
		        File file = new File("D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\msg"+time.toString()+".gsm");
		        VMSLogger.log(Level.INFO,"create a file: msg"+time.toString()+".gsm");
		        // Create file if it does not exist
		        boolean success = file.createNewFile();
		        if (success) {
		        	System.out.println("File did not exist and was created");		        	
		            // File did not exist and was created
		        } else {
		            // File already exists
		        }
                FileOutputStream fos = new  FileOutputStream(file);
		        
                fos.write(media);
		        fos.close();
		         
		        file = new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\Info.txt");		       
		        // Create file if it does not exist
		        success = file.createNewFile();
		        if (success)
		        {
		        	System.out.println("Info File did not exist and was created");		        	
		            // File did not exist and was created
		        }
		        else
		        {
		        	System.out.println("Info File already exists");
		        }
		        RandomAccessFile raf = new RandomAccessFile(file,"rw");
		        
		        if(raf.readLine()==null)
		        {
		        			        			        
		        	try
		    		{
		    		    // Open an output stream
		        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\NewMessage\\Info.txt");

		    		    // Print a line of text
		    		    new PrintStream(fout).println (1+"-");

		    		    // Close our output stream
		    		    fout.close();		
		    		}
		    		// Catches any error conditions
		    		catch (IOException e)
		    		{
		    		}		 
		        }
		        else
		        {
		        	int a;
		        	String str;
		        	raf.seek(0);
		        	str = raf.readLine();
		        	str=str.substring(0,str.indexOf("-"));
		        	//For restrict message number 
		        	a=Integer.parseInt(new String(str));
		        	
		        	if(a == maxMsg)
		        	{
		        		raf.seek(0);
		        		//raf.readLine();
		        		raf.readLine();
		        		String line=raf.readLine();
		        		raf.close();		        		
		        		new Utility().deleteLine("VoiceMailBox\\"+mailBox+"\\NewMessage\\Info.txt",line);
		        	    line=line.substring(line.indexOf(" ")+1);
		        	    File extFile= new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\"+line.substring(0,line.indexOf("-"))+".gsm");						
						extFile.delete();					
		        	}
		        	else
		        	{
		        				        		
		        		try
			    		{
			    		    // Open an output stream
			        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");

			    		    // Print a line of text
			        		a++;
			    		    new PrintStream(fout).print (a+"-");

			    		    // Close our output stream
			    		    fout.close();		
			    		}
			    		// Catches any error conditions
			    		catch (IOException e)
			    		{
			    		}	
			    		File tempFile = new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");	
			    		RandomAccessFile tempRAF=new RandomAccessFile(tempFile,"rw");
			    		tempRAF.seek(0);
			    		String tempLine="";
			    		tempLine=tempRAF.readLine();
			    		tempRAF.close();
			    		tempFile.delete();
			    		
			    		raf.seek(0);
			    		raf.writeBytes(tempLine);
			    		
		        	}
		        }
		        //Append the message info into the Info.text file
		        PrintStream out = new PrintStream(new AppendFileStream("D:\\vms\\VoiceMailBox\\"+mailBox+"\\NewMessage"+"\\Info.txt"));
		        String todayStr="";				
		        
		        try {	
		        	
		        	Calendar cal = Calendar.getInstance();		        	
		        	SimpleDateFormat fmt =new SimpleDateFormat("MM-dd-yyyy");		        
		        	todayStr = fmt.format(cal.getTime());
		        }
		        catch(ClassCastException e) 
		        {
			           System.out.println(e.toString());
			    }
		        			
	            out.print(todayStr);
	            out.println(" msg"+time+"-");
		        out.close();
		       		           		   		        		              		 
		    } 
			catch (IOException e)
			{
		    }

			
		////////////////////////////
						
		}
		
		
	}
	/////////////////////////////////////////////////////////////
	/*******************************************************************
	 * At this point if recieve Bye it means that user in the middle of*
	 * the retrieving the message want to terminates the session or    * 
	 * recive invite message to modify in the properties of the session*    
	 *******************************************************************/
	private void stateS2(SIPRequest sipRequest) throws NumberFormatException, IOException
	{
		VMSLogger.log(Level.INFO, "START stateS2"+" CallId:"+sipRequest.getCallId());
		
		if(sipRequest.getMethod().equals("INVITE"))
		{
			 state=SessionState.s4;
			 //Check Content-Encoding header
			 if(sipRequest.getContent_Encoding()!=null)
			 {
				 if (! Utility.contentEncodings.contains(sipRequest.getContent_Encoding()))
				 {				
					//Generate the Response488
							
					String str="";
					String[] array = (String[])Utility.contentEncodings.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);		
					sipResponse.setAccept_Encoding(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
					state=SessionState.s2;
		
					return;
								            
				}
								
			  }
			
			  //check Content-Language header
			  if(sipRequest.getContent_Language()!=null)
			  {
				if (! Utility.contentLanguages.contains(sipRequest.getContent_Language()))
				{				
					//Generate Response488							
					String str="";
					String[] array = (String[])Utility.contentLanguages.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=" "+array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);
					sipResponse.setAccept_Language(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s2;
					return;
					
				}
			  }
			
			  //Check Content-Type header
			  if(sipRequest.getContent_Type()!=null)
			  {
				if(! Utility.contentTypes.contains(sipRequest.getContent_Type()))
				{				
					//Generate Response488		
					String str="";
					String[] array = (String[])Utility.contentTypes.toArray(new String[Utility.methods.size()]);
					for(int i=0;i<array.length;i++)
					{
						if(str.equals(""))
							str=array[i];
						else
							str=str+","+array[i];
					}
					str=str+"\r";
					sipResponse=Utility.response488(sipRequest);
					sipResponse.setAccept(str);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s2;
					return;
					
				}
			  }
			
			  // Check Content-Dispositions header
			  if(sipRequest.getContent_Desposition()!=null)
			  {
				if(! Utility.contentDispositions.contains(sipRequest.getContent_Desposition()))
				{				
					//Generate Response488						
					sipResponse=Utility.response488(sipRequest);
					VMSLogger.log(Level.INFO,"488 Response is generated");
					String s = sipResponse.toString();
					VMSLogger.log(Level.INFO,"\n"+s);
					InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
					sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
					
					state=SessionState.s2;
					return;
					
				}
							
			  }
			  if(!sipRequest.getContent_Length().equals("0"))
			  {
				  if(sipRequest.sdp.sessionDescription.getA()!=null)
				  {			
					  int count=sipRequest.sdp.sessionDescription.getA().size();
					  for(int i=0;i<count;i++)
					  {
						  if(! Utility.attributes.contains(sipRequest.sdp.sessionDescription.getA(i)))
						  {
							  sipResponse=Utility.response488(sipRequest);
							  VMSLogger.log(Level.INFO,"488 Response is generated");
							  String s = sipResponse.toString();
							  VMSLogger.log(Level.INFO,"\n"+s);
							  InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
							  sipResponse.send(s.getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
						
							  state=SessionState.s2;
							  return;				
						  }
					  }		
				  }
			  }
			  sipResponse=new SIPResponse();
			  sipResponse=Utility.response200(sipRequest,srcRtpPort);
			  InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			  sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
			  state=SessionState.s2;

		}
		else if(sipRequest.getMethod().equals("BYE"))
		{
			endSession=true;
			sessionTimer.setStop(true);
			
			sipResponse=new SIPResponse();
			sipResponse=Utility.response200(sipRequest,srcRtpPort);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			//this.yield();
			
		}
		
		VMSLogger.log(Level.INFO, "START stateS2"+" CallId:"+sipRequest.getCallId());
	}
	/////////////////////////////////////////////////////////////
    /*******************************************************************
     * At this point server waits for recieving ok message to the      *
     * sended bye message.It sends bye for two times again until       *
     * recieve ok message else does'nt recieve it terminate the session*
     *******************************************************************/
	private void stateS3(SIPResponse sipResponse) throws NumberFormatException, IOException, InterruptedException
	{
		VMSLogger.log(Level.INFO, "START stateS3"+" CallId:"+sipResponse.getCallId());
				
		if(sipResponse.getStatusCode().equals("200"))
		{
			sessionTimer.setStop(true);
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipResponse.getCallId());
			sessionTimer.setStop(true);
			//this.yield();
		}		
		
		
		VMSLogger.log(Level.INFO, "START stateS3"+" CallId:"+sipResponse.getCallId());
	}
	/////////////////////////////////////////////////////////////
	private void stateS4(SIPRequest sipRequest) throws NumberFormatException, IOException
	{
		VMSLogger.log(Level.INFO, "START stateS4"+" CallId:"+sipRequest.getCallId());
		
		if(sipRequest.getMethod().equals("INVITE"))
		{
			sipResponse=new SIPResponse();
			sipResponse=Utility.response491(sipRequest);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));	
			state=SessionState.s1;
		}
		
		VMSLogger.log(Level.INFO, "START stateS4"+" CallId:"+sipRequest.getCallId());
	}
		
	/////////////////////////////////////////////////////////////
	private void stateS5(SIPRequest sipRequest) throws NumberFormatException, IOException, InterruptedException
	{
		VMSLogger.log(Level.INFO, "START stateS5"+" CallId:"+sipRequest.getCallId());
		
		if(sipRequest.getMethod().equals("BYE"))
		{
			endSession=true;
			
			sipResponse=new SIPResponse();
			sipResponse=Utility.response200(sipRequest,srcRtpPort);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId());
			sessionTimer.setStop(true);
			//this.yield();
		}
		
		VMSLogger.log(Level.INFO, "START stateS5"+" CallId:"+sipRequest.getCallId());
	}
	
	/////////////////////////////////////////////////////////////
	private void stateS6(SIPRequest sipRequest) throws NumberFormatException, IOException, InterruptedException
	{
		VMSLogger.log(Level.INFO, "START stateS6"+" CallId:"+sipRequest.getCallId());
		
		if(sipRequest.getMethod().equals("BYE"))
		{
			endSession=true;
			sipResponse=new SIPResponse();
			sipResponse=Utility.response200(sipRequest,srcRtpPort);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));
			sipResponse.send(sipResponse.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
			if(sessionHash!=null)
			if(sessionHash.containsValue(sipRequest.getCallId())== true)
				sessionHash.remove(sipRequest.getCallId()); 
			sessionTimer.setStop(true);
			//this.yield();
		}
		
		VMSLogger.log(Level.INFO, "START stateS6"+" CallId:"+sipRequest.getCallId());
	}

	/////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////	
	/***********************************************************************
	 * Plays message information of the user then user can select an option* 
	 * and server acts related function                                    *
	 ***********************************************************************/
		
	public class PlayMsgInfo extends Thread{
		
		String path="";
		SessionHandler sessionHandler;
		
	PlayMsgInfo(String str,SessionHandler sh)
	{
			path=str;
			sessionHandler=new SessionHandler();
			sessionHandler=sh;
	}
		
	public void run()
	{
		
		try{
		File file = new File(path+"Info.txt");			
		RandomAccessFile raf=new RandomAccessFile(file, "rwd");
		raf.seek(0); 
		long curOffset=0;
		long preOffset=0;
		
		while(raf.readLine()!= null)
		{			
			preOffset=curOffset;
			curOffset=raf.getFilePointer();			
		}
	    
		if(curOffset==0)
		{
			Session session=new Session();
			session.setRtpPort(destRtpPort);
			session.setDestAddress(DestAddress);
		    //User has not any message
			RTPHandler.play(session,"sounds\\anymessage",sessionHandler);
			//Return to previous step				    
			RTPHandler.play(session,"sounds\\vm-goodbye",sessionHandler);
			
			SIPRequest byeRequest=new SIPRequest();
			requestCounter++;
			byeRequest=ByeRequest(sipRequest);
			InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
			byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
			Utility.rtpPorts.add(srcRtpPort);
							
			state=SessionState.s3;
			sessionTimer=new TimeoutInt(5000,sessionHandler);
			sessionTimer.start();
		    return;
		}
		long dis=curOffset - preOffset ;
		int msgCounter=1;
		while(true){
			//Terminate session
			if(curOffset<dis)
			{
				Session session=new Session();
				session.setRtpPort(destRtpPort);
				session.setDestAddress(DestAddress);			
				RTPHandler.play(session,"sounds\\vm-goodbye",sessionHandler);
								
				///////////////////////////////////////////
				
				SIPRequest byeRequest=new SIPRequest();
				requestCounter++;
				byeRequest=ByeRequest(sipRequest);
				InetAddress address=InetAddress.getByName(VMSProperties.getProperty("SerAddress"));					
				byeRequest.send(byeRequest.toString().getBytes(),address,Integer.parseInt(new String(VMSProperties.getProperty("sipPort"))));
				Utility.rtpPorts.add(srcRtpPort);
								
				state=SessionState.s3;
				sessionTimer=new TimeoutInt(5000,sessionHandler);
				sessionTimer.start();
				
				break;				
			}			
			raf.seek(curOffset - dis);	
			String Info=raf.readLine();
			Session session=new Session();
			session.setRtpPort(destRtpPort);
			session.setDestAddress(DestAddress);			
			RTPHandler.play(session,"sounds\\"+Integer.toString(msgCounter)+"m",sessionHandler);						
			//RTPHandler.play(session,"sounds\\message");						
			String fileName=Play(Info);
			
			rtpServer = new RTPServer(sessionHandler,srcRtpPort,rtpHash);							
			rtpServer.start();
			
			System.out.println("----------------before waiting --------------");
			Utility.wait(1000000);
			
			if(key>=0)
			{				
				switch(key)
				{
					case 0:
					
						rtpHash.clear();
						rtpState=2;
						session.setRtpPort(destRtpPort);
						session.setDestAddress(DestAddress);
						RTPHandler.play(session,"sounds\\menu0",sessionHandler);				
					 
						break;
						
					case 1:
						
						//Play Message and if it is in New Message folder, send it to Old Message folder
						//and modify New and Old Info Files 
						
						RTPHandler.play(session,path+fileName,sessionHandler);
						if(path.equals("VoiceMailBox\\"+mailBox+"\\NewMessage\\"))
						{							
							File preFile = new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\"+fileName+".gsm");
							File curFile = new File("VoiceMailBox\\"+mailBox+"\\OldMessage\\"+fileName+".gsm");
				        						
							// Create file if it does not exist
							try
							{
								boolean success = curFile.createNewFile();
							}
							catch(Exception e)
							{
								System.out.println(e);		        								
							}
							FileInputStream fis=new FileInputStream(preFile);				       				       
							FileOutputStream fos = new FileOutputStream("VoiceMailBox\\"+mailBox+"\\OldMessage\\"+fileName+".gsm");				   
				            ///////////////////////////												
							byte[]  bytes=new byte[(int)preFile.length()];					
							fis.read(bytes);					        
							fos.write(bytes);
				        
							fos.close();
							fis.close();				        
							preFile.delete();
		                
							//////////////////////////////////////////////////////////
							// Delete a line from the New Info File 
							//////////////////////////////////////////////////////////
							raf.close();
							new  Utility().deleteLine(path+"Info.txt",Info);
							raf=new RandomAccessFile(file, "rwd");
							
							String str;
	    		        	raf.seek(0);
	    		        	str = raf.readLine();
	    		        	str=str.substring(0,str.indexOf("-"));
	    		        	//For restrict message number 
	    		        	int a=Integer.parseInt(new String(str));
	    		        	
	    		        	try
    			    		{    			    		         	
    			    		    
    			        		if(a==1)
    			        		{
    			        			raf.close();
    			        			file.delete();
    			        			new File(path+"Info.txt");    			        			
    			        		}
    			        		else
    			        		 {
    			        			
    			        			FileOutputStream fout = new FileOutputStream (path+"temp.txt");
    			        			a--;
    			        		    new PrintStream(fout).print (a+"-");   
    			        		    fout.close();
    			        		    File tempFile = new File(path+"temp.txt");	
    	    			    		RandomAccessFile tempRAF=new RandomAccessFile(tempFile,"rw");
    	    			    		tempRAF.seek(0);
    	    			    		String tempLine="";
    	    			    		tempLine=tempRAF.readLine();
    	    			    		tempRAF.close();
    	    			    		tempFile.delete();
    	    			    		
    	    			    		raf.seek(0);
    	    			    		raf.writeBytes(tempLine);
    	    			    		
    			        		 }
    			    		}
    			    		// Catches any error conditions
    			    		catch (IOException e)
    			    		{
    			    		}	
    			    		
		            		
							////////////////////////////////////////
		            		///Insert into Old Info file
		            		////////////////////////////////////////
		            		
		            		RandomAccessFile rafOld=new RandomAccessFile(new File("VoiceMailBox\\"+mailBox+"\\OldMessage"+"\\Info.txt"),"rw");
		            		rafOld.seek(0);
		            		if(rafOld.readLine()==null)
		            		{
		            			try
		    		    		{
		    		    		    // Open an output stream
		    		        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\OldMessage"+"\\Info.txt");

		    		    		    // Print a line of text
		    		    		    new PrintStream(fout).println (1+"-");

		    		    		    // Close our output stream
		    		    		    fout.close();		
		    		    		}
		    		    		// Catches any error conditions
		    		    		catch (IOException e)
		    		    		{
		    		    		}	
		            			
		            		}
		            		else
		            		{		            			
		            		   	
		    		        	rafOld.seek(0);
		    		        	str = rafOld.readLine();
		    		        	str=str.substring(0,str.indexOf("-"));
		    		        	//For restrict message number 
		    		        	a=Integer.parseInt(new String(str));		    		        			    		        			    		        	    		        				       
		    		        		
		            			if(a == maxMsg)
		            			{
		            						            						    		  		            		
		            				rafOld.seek(0);
		            				rafOld.readLine();
		            				String line=rafOld.readLine();
		            				rafOld.close();
		            				new Utility().deleteLine("VoiceMailBox\\"+mailBox+"\\OldMessage\\Info.txt",line);
		            				line=line.substring(line.indexOf(" ")+1);
		            				File extFile= new File("VoiceMailBox\\"+mailBox+"\\OldMessage\\"+line.substring(0,line.indexOf("-"))+".gsm");						
		            				extFile.delete();
		            				
		            			}
		            			else
		            			{
		            				try
		    			    		{
		    			    		    // Open an output stream
		    			        		FileOutputStream fout = new FileOutputStream ("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");

		    			    		    // Print a line of text
		    			        		a++;
		    			    		    new PrintStream(fout).print (a+"-");
		    			    		    		    			    		    		    			    		    
		    			    		    // Close our output stream
		    			    		    fout.close();		
		    			    		}
		    			    		// Catches any error conditions
		    			    		catch (IOException e)
		    			    		{
		    			    		}	
		    			    		File tempFile = new File("VoiceMailBox\\"+mailBox+"\\NewMessage\\temp.txt");	
		    			    		RandomAccessFile tempRAF=new RandomAccessFile(tempFile,"rw");
		    			    		tempRAF.seek(0);
		    			    		String tempLine="";
		    			    		tempLine=tempRAF.readLine();
		    			    		tempRAF.close();
		    			    		tempFile.delete();
		    			    		
		    			    		rafOld.seek(0);
		    			    		rafOld.writeBytes(tempLine);
		    			    		
		    		        	}
		            					            					       
				        	}
		            		PrintStream out = new PrintStream(new AppendFileStream("VoiceMailBox\\"+mailBox+"\\OldMessage"+"\\Info.txt"));				        			        		
							out.println(Info);	            	
							out.close();	
						}
						
																					           
						break;
						
					case 2:
						//Delete related Message
						String spoonFeeding = path+fileName+".gsm";
					    File f= new File(spoonFeeding);
						FileInputStream fis = new FileInputStream(spoonFeeding);
			
						try
						{
							fis.close();
							f.delete();
						}
						catch(SecurityException s){
							System.out.println("Exception is : --------------"+s);
						}
						raf.close();
						new Utility().deleteLine(path+"Info.txt",Info);
						raf=new RandomAccessFile(file, "rwd");
						String str;
    		        	raf.seek(0);
    		        	str = raf.readLine();
    		        	str=str.substring(0,str.indexOf("-"));
    		        	//For restrict message number 
    		        	int a=Integer.parseInt(new String(str));
    		        	
    		        	try
			    		{    			    		         	
			    		    
			        		if(a==1)
			        		{
			        			raf.close();
			        			file.delete();
			        			new File(path+"Info.txt");    			        			
			        		}
			        		else
			        		 {
			        			
			        			FileOutputStream fout = new FileOutputStream (path+"temp.txt");
			        			a--;
			        		    new PrintStream(fout).print (a+"-");   
			        		    fout.close();
			        		    File tempFile = new File(path+"temp.txt");	
	    			    		RandomAccessFile tempRAF=new RandomAccessFile(tempFile,"rw");
	    			    		tempRAF.seek(0);
	    			    		String tempLine="";
	    			    		tempLine=tempRAF.readLine();
	    			    		tempRAF.close();
	    			    		tempFile.delete();
	    			    		
	    			    		raf.seek(0);
	    			    		raf.writeBytes(tempLine);
	    			    		
			        		 }
			    		}
			    		// Catches any error conditions
			    		catch (IOException e)
			    		{
			    		}	
			    		
						break;
											
					case 3:
						session.setRtpPort(destRtpPort);
						session.setDestAddress(DestAddress);
						RTPHandler.play(session,"sounds\\menu4",sessionHandler);			
						break;
						
				}
				key=-1;
			}
			
			msgCounter++;
			
			curOffset=curOffset-dis;
			preOffset=curOffset-dis;
		
		}
		raf.close();
		}
		catch(Exception e)
		{
			System.out.println("--------------------exception in Play info is-----------"+e);
		}
	}
	}
	
    /***************************************
     * Plays date of the recieved messages *
     * @param info
     * @return
     * @throws UnknownHostException
     ***************************************/
	//////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
	// Bye Request                                                //
	////////////////////////////////////////////////////////////////
	public SIPRequest ByeRequest(SIPMessage sipMessage)	
	{
				
		// The Bye request send to destination directly
		SIPRequest bye=new SIPRequest();
		bye.setMethod("BYE");
		String str=sipMessage.getContact(0);
		int start = str.indexOf("<");
		int end = str.indexOf(">");
		str = str.substring(start+1,end);		
		//str="sip:voicemail@194.225.73.41:5060";
		bye.setRequestURI(str);
		bye.setSIPVersion("SIP/2.0\r");
		bye.setTo(sipMessage.getFrom());
		bye.setFrom(sipMessage.getTo());	
		bye.setVia(" SIP/2.0/UDP 194.225.73.41:5060\r",0);		
		//bye.setRoute(" <sip:194.225.73.138;lr;ftag=6d575c14>\r",0);		
		bye.setCallId(sipMessage.getCallId());
		str=sipMessage.getCSeq();		
		str=str.substring(str.indexOf(" ")+1);
		str=str.substring(0,str.indexOf(" "));	
		int a=(Integer.parseInt(str)+1);
		bye.setCSeq(" "+requestCounter+" BYE\r");		
		bye.setMax_Forward(" 70\r");
		bye.setContent_Length(" 0\r");
		bye.setContact(" <sip:voicemail@194.225.73.41:5060>\r",0);
		
		if(sipMessage instanceof SIPRequest)
		{
		     bye.setRoute(((SIPRequest)sipMessage).getRoute());
		}	
			
		
		
		return bye;
	}
	
	////////////////////////////////////////////////////////////////////
	public String Play(String info) throws UnknownHostException
	{
		
		String yy="",mm="",dd="",fileName="";		
		try
		{
			mm=info.substring(0,info.indexOf("-"));
			info=info.substring(info.indexOf("-")+1);
			dd=info.substring(0,info.indexOf("-"));
			info=info.substring(info.indexOf("-")+1);
			yy=info.substring(0,info.indexOf(" "));
			info=info.substring(info.indexOf(" ")+1);
			fileName=info.substring(0,info.indexOf("-"));
		
			Session session=new Session();
			session.setRtpPort(destRtpPort);
			session.setDestAddress(DestAddress);
		
		//---------------------------check month
		if(mm.equals("01"))			
			RTPHandler.play(session,"sounds\\jan",this);
		
		else if(mm.equals("02"))			
			RTPHandler.play(session,"sounds\\feb",this);
		
		else if(mm.equals("03"))			
			 RTPHandler.play(session,"sounds\\march",this);
		
		else if(mm.equals("04"))					
			 RTPHandler.play(session,"sounds\\april",this) ;
		
		else if(mm.equals("05"))			
			 RTPHandler.play(session,"sounds\\april",this) ;
		
		else if(mm.equals("06"))		
			 RTPHandler.play(session,"sounds\\june",this) ;
		
		else if(mm.equals("07"))		
			 RTPHandler.play(session,"sounds\\july",this) ;
		
		else if(mm.equals("08"))		
			 RTPHandler.play(session,"sounds\\aug",this) ;
		
		else if(mm.equals("09"))		
			 RTPHandler.play(session,"sounds\\sep",this) ;
		
		else if(mm.equals("10"))		
			RTPHandler.play(session,"sounds\\oct",this) ;
		
		else if(mm.equals("11"))		
			 RTPHandler.play(session,"sounds\\nov",this) ;
		
		else if(mm.equals("12"))		
			 RTPHandler.play(session,"sounds\\des",this) ;
		////////////////////////////////////////
		//---------------------------check day
		if(dd.equals("01"))		
			 RTPHandler.play(session,"sounds\\1",this) ;
		
		else if(dd.equals("02"))
			 RTPHandler.play(session,"sounds\\2",this) ;
			
		else if(dd.equals("03"))
			 RTPHandler.play(session,"sounds\\3",this) ;
		
		else if(dd.equals("04"))
			 RTPHandler.play(session,"sounds\\4",this) ;
		
		else if(dd.equals("05"))
			 RTPHandler.play(session,"sounds\\5",this) ;
		
		else if(dd.equals("06"))
			 RTPHandler.play(session,"sounds\\6",this) ;
		
		else if(dd.equals("07"))
			 RTPHandler.play(session,"sounds\\7",this) ;
		
		else if(dd.equals("08"))
			 RTPHandler.play(session,"sounds\\8",this) ;
		
		else if(dd.equals("09"))
			 RTPHandler.play(session,"sounds\\9",this) ;
		
		else if(dd.equals("10"))
			 RTPHandler.play(session,"sounds\\10",this) ;
		
		else if(dd.equals("11"))
			 RTPHandler.play(session,"sounds\\11",this) ;
		
		else if(dd.equals("12"))
			 RTPHandler.play(session,"sounds\\12",this) ;
		
		else if(dd.equals("13"))
			 RTPHandler.play(session,"sounds\\13",this) ;
		
		else if(dd.equals("14"))
			 RTPHandler.play(session,"sounds\\14",this) ;
		
		else if(dd.equals("15"))
			 RTPHandler.play(session,"sounds\\15",this) ;
		
		else if(dd.equals("16"))
			 RTPHandler.play(session,"sounds\\16",this) ;
		
		else if(dd.equals("17"))
			 RTPHandler.play(session,"sounds\\17",this) ;
		
		else if(dd.equals("18"))
			 RTPHandler.play(session,"sounds\\18",this) ;
		
		else if(dd.equals("19"))
			 RTPHandler.play(session,"sounds\\19",this) ;
		
		else if(dd.equals("20"))
			 RTPHandler.play(session,"sounds\\20",this) ;
		
		else if(dd.equals("21"))
			 RTPHandler.play(session,"sounds\\21",this) ;
		
		else if(dd.equals("22"))
			 RTPHandler.play(session,"sounds\\22",this) ;
		
		else if(dd.equals("23"))
			 RTPHandler.play(session,"sounds\\23",this) ;
		
		else if(dd.equals("24"))
			 RTPHandler.play(session,"sounds\\24",this) ;
		
		else if(dd.equals("25"))
			 RTPHandler.play(session,"sounds\\25",this) ;
		
		else if(dd.equals("26"))
			 RTPHandler.play(session,"sounds\\26",this) ;
		
		else if(dd.equals("27"))
			 RTPHandler.play(session,"sounds\\27",this) ;
		
		else if(dd.equals("28"))
			 RTPHandler.play(session,"sounds\\28",this) ;
		
		else if(dd.equals("29"))
			 RTPHandler.play(session,"sounds\\29",this) ;
		
		else if(dd.equals("30"))
			 RTPHandler.play(session,"sounds\\30",this) ;
		
		else if(dd.equals("31"))
			 RTPHandler.play(session,"sounds\\31",this) ;
		
		////////////////////////////////////////
		//---------------------------check year
		if(yy.equals("2006"))
			 RTPHandler.play(session,"sounds\\2006",this) ;
		
		else if(yy.equals("2007"))
			 RTPHandler.play(session,"sounds\\2007",this) ;
		
		else if(yy.equals("2008"))
			 RTPHandler.play(session,"sounds\\2008",this) ;
		
		else if(yy.equals("2009"))
			 RTPHandler.play(session,"sounds\\2009",this) ;
		
		else if(yy.equals("2010"))
			 RTPHandler.play(session,"sounds\\2010",this) ;
		////////////////////////////////////////
	//	return fileName;
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("--------------exception is " + e);
		}
		return fileName;
	}
	////////////////////////////////////////////////////////////////////

}



