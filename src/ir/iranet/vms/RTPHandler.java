package ir.iranet.vms;

import ir.iranet.vms.dtmf.DTMFMessage;
import ir.iranet.vms.rtp.*;
import ir.iranet.vms.util.AppendFileStream;
import ir.iranet.vms.util.Utility;
import ir.iranet.vms.util.TimeoutInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:12:49 AM
 * To change this template use File | Settings | File Templates.
 */

public class RTPHandler extends Thread
{
	
   DatagramPacket rtpPacket;   
   Set set=new HashSet();
   Session session;
   String str="";
   SessionHandler sessionHandler;
   
    //////////////////////////////////////////////////////////////

	public RTPHandler(DatagramPacket rtpPacket)
	{		
		this.rtpPacket = rtpPacket;		
	}
	
	public RTPHandler(Session s,String st)
	{
		session=s;
		str=st;
	}
	public RTPHandler()
	{
		
	}

	public RTPHandler(SessionHandler sH,int option) throws NumberFormatException, IOException, InterruptedException
	{
		sessionHandler = new SessionHandler();
		sessionHandler=sH;
		sessionHandler.Alarm(option);
		
	}
	////////////////////////////////////////////////////////////////
	/****************************************************
	 * Save rtp packets into the hash to sort them     *  
	 ****************************************************/
	public void AddNewRtp(DatagramPacket packet,SessionHandler sh,HashMap rtpHash,RTPServer rtpSer) throws UnknownHostException, InterruptedException
	{
		RTPMessage rtpMessage=new RTPMessage();				
		rtpMessage=rtpMessage.Assembler(packet);
		
		//GSM Messages
		if(rtpMessage.getM_PT()[0]==3)
		{	
			rtpHash.put(rtpMessage.getTimeStamp(),rtpMessage.getData());						
		}
		
		//Telephon Events//65
		else if(rtpMessage.getM_PT()[0]==101)
		{
			
			DTMFMessage dtmfMessage=new DTMFMessage();
			dtmfMessage=dtmfMessage.Assembler(rtpMessage.getData());			
			if(dtmfMessage.getEvent()==0 || dtmfMessage.getEvent()==1 ||
			   dtmfMessage.getEvent()==2 || dtmfMessage.getEvent()==3 ||
			   dtmfMessage.getEvent()==4 || dtmfMessage.getEvent()==11)
			{
				rtpHash.clear();
				try{
					
					/////////////////////////////
					
					PrintStream out = new PrintStream(new AppendFileStream("C:\\Documents and Settings\\VOIP\\workspace\\VMS2\\Info.text"));        
			        out.println("---------- Alarm is called -------------"+dtmfMessage.getEvent());	            	
			        out.close();
					
					/////////////////////////////
			       
					sh.Alarm(dtmfMessage.getEvent());
					rtpSer.wait();
					//this.yield();
				}
				catch(IOException e)
				{
					
				}
			}									
		}	
	}

	////////////////////////////////////////////////////////////////
	public void Alarm(SessionHandler sh,int option) throws NumberFormatException, IOException, InterruptedException
	{
		sh.Alarm(option);
		
	}
	///////////////////////////////////////////////////////////////
	/*************************************
	 * Plays audio files for the users.  *
	 *************************************/
    public static void play(Session session,String str,SessionHandler sh)    
    {
    	
    	try
		{	
    				
    		str=str+".gsm";
		    File file = new File(str);
			FileInputStream fis = new FileInputStream(file); 
			byte[]  bytes=new byte[(int)file.length()];					
			fis.read(bytes);					
			fis.close();
			
			// Payload type is gsm
			RTPMessage rtpMessage=new RTPMessage();																		
			byte[] packet=new byte[45];
			byte[] payLoad=new byte[33];
			
			for(int i=0;i<=(bytes.length)/33;i++){
				
				if(sh.endSession)
					return;
				if((i==(bytes.length)/33))
				{
					if(((bytes.length)%33!=0))
					for(int j=0;j<32;j++)
					{
						payLoad[j]=bytes[i*33+j];							
					}
					payLoad[32]=(byte)(33-((bytes.length) % 33));
					packet=rtpMessage.MakePacket(payLoad,session);
					//set p by 1
					packet[0]=(byte)(0xc0);	
					
					rtpMessage.send(packet,session.getDestAddress(),session.getRtpPort());
				}
				else
				{
					for(int j=0;j<33;j++)
					{
						payLoad[j]=bytes[i*33+j];							
					}
					packet=rtpMessage.MakePacket(payLoad,session);																			
					rtpMessage.send(packet,session.getDestAddress(),session.getRtpPort());						
				}
								
				Thread.sleep(18);
			}				
		
		} 
        catch (Exception e)
		{
			System.err.println("File input error"+e);
		}
    }
    
}
 			
 			
 	   
  

 