package ir.iranet.vms.util;

import ir.iranet.vms.sip.SIPMessage;
import ir.iranet.vms.sip.SIPRequest;
import ir.iranet.vms.sip.SIPResponse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;





import java.io.*;

import java.io.File;
public class Utility {
	
	public static Set methods=new HashSet();
	public static Set contentTypes=new HashSet();
	public static Set contentLanguages=new HashSet();
	public static Set contentEncodings=new HashSet();
	public static Set contentDispositions=new HashSet();
	public static Set attributes=new HashSet();		
	public static Set rtpPorts=new HashSet();
		
	public Utility()
	{
		set();
	}
	///////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static void set()
	{
		//METHODs Set
		
		
		methods.add("ACK");		
		methods.add("BYE");
		methods.add("CANCEL");
		methods.add("INFO");
		methods.add("INVITE");
		methods.add("OPTION");
        methods.add("OPTIONS");

        //Content Types Set
				
		contentTypes.add(" application/sdp\r");
		
		//Content Languages Set
		
		contentLanguages.add("en");
		
		
		//Content Encodings Set
		
		contentEncodings.add("gzip");
		
		//Content Dispositions
		
		
		//Attribtes Set
		
		
		// RTP Port set
		if(rtpPorts.isEmpty()){
			
			for(int i=7000;i<8000;i++)
			{
				rtpPorts.add(i);
			}
		}
						        			
	}
	
	///////////////////////////////////////////////////////////////////////
	public synchronized static byte[] Append (  byte[] packetA, byte[] packetB )
    {
        // Create a new array whose size is equal to sum of packets
        // being added
        byte packetAB [] = new byte [ packetA.length + packetB.length ];

        // First paste in packetA
        for ( int i=0; i < packetA.length; i++ )
            packetAB [i] = packetA [i];

        // Now start pasting packetB
        for ( int i=0; i < packetB.length; i++ )
            packetAB [i+packetA.length] = packetB [i];

        return packetAB;
    }
	/////////////////////////////////////////////////////////////////////////
	public synchronized static byte[] Append (  byte[] packetA, byte packetB )
    {
        // Create a new array whose size is equal to sum of packets
        // being added
        byte packetAB [] = new byte [ packetA.length + 1];

        // First paste in packetA
        for ( int i=0; i < packetA.length; i++ )
            packetAB [i] = packetA [i];

        // Now start pasting packetB
        
        packetAB [packetA.length+1] = packetB;

        return packetAB;
    }
	/////////////////////////////////////////////////////////////////////////
	
	 public synchronized static byte [] LongToBytes ( long ldata, int n )
	    {
	        byte buff[] = new byte [ n ];

	        for ( int i = n-1; i>=0; i--)
	        {
	            // Keep assigning the right most 8 bits to the
	            // byte arrays while shift 8 bits during each iteration
	            buff [ i ] = (byte) ldata;
	            ldata = ldata >> 8;
	        }
	        return buff;
		
	    }
	 //////////////////////////////////////////////////////////////////////

	public synchronized static int CompareBytes(byte[] A,byte[] B)
	{
			int ASize=A.length;
			String a="";
			String b="";
			for(int i=0;i<ASize-1;i++)
			{
				a=a+Integer.toBinaryString(A[i]);
				b=b+Integer.toBinaryString(B[i]);
				
			}
			return a.compareTo(b);				
	}
	 /////////////////////////////////////////////////
	public synchronized static void wait(int time)
	{
		for(int i=0;i++<1000;)
			for(int j=0;j++<time;);
	}
	///////////////////////////////////////////////////
	public static void deleteLine(String name,String text)
	{	
		try
		{
			File f = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line=null;
			//String text_to_be_deleted="and";
			PrintWriter out = new PrintWriter("temp.txt");
			while( (line=br.readLine())!=null )
			{
				if((line.trim()).equals(text)){
					continue; 
				}
				else{
					out.println(line); 
				}
			}
			System.gc();
			out.close();
			br.close();	
			boolean bool=f.delete();
			File f2=new File("temp.txt");
			f2.renameTo( new File(name));
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
		
	///////////////////////////////////////////////////
	/*****************************************************************
	 *  The request has succeeded.  The information returned with the*
     *  response depends on the method used in the request.          *
	 *****************************************************************/
	
	//////////////////////////////////////////////////////////////
	// 200 OK Response                                          //
	//////////////////////////////////////////////////////////////
	public static SIPResponse response200(SIPRequest request,int srcRtpPort)
	{		

		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("200");
		sipResponse.setReasonPhrase("Ok\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setVia(request.getVia());		
		String str=" <sip:voicemail@194.225.73.41:5060>"+"\r";
		sipResponse.setContact(str,0);
		
		//set Allow header
		str="";
		String[] array = (String[])Utility.methods.toArray(new String[Utility.methods.size()]);
		for(int i=0;i<array.length;i++)
		{
			if(str.equals(""))
				str=" "+array[i];
			else
				str=str+", "+array[i];
		}
		str=str+"\r";
		//sipResponse.setAllow(str);	
		
		if(request.getMethod().equals("INVITE")){
			sipResponse.setContent_Type(" application/sdp\r");
			sipResponse.setContent_Length(" 150\r");
			sipResponse.sdp.sessionDescription.setV("0"+"\r");	
			sipResponse.sdp.sessionDescription.setO("voicemail 5825114 7118244 IN IP4 194.225.73.41\r");
			sipResponse.sdp.sessionDescription.setS("voicemail\r");
			sipResponse.sdp.sessionDescription.setC("IN IP4 194.225.73.41\r");
			sipResponse.sdp.timeDescription.setT(request.sdp.timeDescription.getT(0),0);			
			sipResponse.sdp.getMediaDescription().setM("audio "+srcRtpPort+" RTP/AVP 3 101 \r");			
			sipResponse.sdp.getMediaDescription().setA("rtpmap:3 gsm/8000\r",0);			
			sipResponse.sdp.getMediaDescription().setA("rtpmap:101 telephone-event/8000\r",1);
			sipResponse.sdp.getMediaDescription().setA("fmtp:101 0-15\r",2);
			sipResponse.sdp.getMediaDescription().setA("sendrecv\r",3);
			////////////////////////////////
			sipResponse.setAllow(str);
			/////////////////////////////////////////
		}
		if(request.getMethod().equals("BYE")){
			sipResponse.setRecord_Route(" <sip:194.225.73.138;lr=on>"+"\r",0);
			sipResponse.setContent_Length(" 0");
		}
		else if(request.getMethod().equals("OPTION"))
		{
			//set Accept-Encoding header
			array = (String[])Utility.contentEncodings.toArray(new String[Utility.contentEncodings.size()]);
			for(int i=0;i<array.length;i++)
			{
				if(str.equals(""))
					str=" "+array[i];
				else
					str=str+", "+array[i];
			}
			str=str+"\r";
			sipResponse.setAccept_Encoding(str);
			
			//set Accept-Language header
			
			array = (String[])Utility.contentLanguages.toArray(new String[Utility.contentLanguages.size()]);
			for(int i=0;i<array.length;i++)
			{
				if(str.equals(""))
					str=" "+array[i];
				else
					str=str+", "+array[i];
			}
			str=str+"\r";
			sipResponse.setAccept_Language(str);
			
			//set Accept header
			array = (String[])Utility.contentTypes.toArray(new String[Utility.contentTypes.size()]);
			for(int i=0;i<array.length;i++)
			{
				if(str.equals(""))
					str=" "+array[i];
				else
					str=str+", "+array[i];
			}
			str=str+"\r";
			sipResponse.setAccept(str);
			
			///////////////////////////////////
			sipResponse.setAllow(str);
			//////////////////////////////////////
		}
		
		return sipResponse;
	}
	
	
	/**************************************************************
	 * One or more of the media attributes in the session         *
	 * description are not supported.                             *
	 * ************************************************************/ 
	////////////////////////////////////////////////////////////////
	// 306 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response306(SIPRequest request)
	{
		
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("306");
		sipResponse.setReasonPhrase("Attribute not understood\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	
    ////////////////////////////////////////////////////////////////
	// 307 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response307(SIPRequest request)
	{
		
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("307");
		sipResponse.setReasonPhrase("Session description parameter not understood\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	/*************************************************************************
	 * The request could not be understood due to malformed syntax.  The     *
     *  Reason-Phrase SHOULD identify the syntax problem in more detail, for *
     *  example, "Missing Call-ID header field".                             *
     * **********************************************************************/ 
    //////////////////////////////////////////////////////////////////////////
	// 400 Response                                                         //
	//////////////////////////////////////////////////////////////////////////
	public static SIPResponse response400(SIPRequest request)
	{
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("400");
		sipResponse.setReasonPhrase("Bad Request\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	/*******************************************************************
	 * The method specified in the Request-Line is understood, but not *
     * allowed for the address identified by the Request-URI.          *
     * *****************************************************************/ 
    ////////////////////////////////////////////////////////////////
	// 405 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response405(SIPRequest request)
	{
		
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("405");
		sipResponse.setReasonPhrase("Method Not Allowed\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	/**********************************************************************
	 *The server is refusing to service the request because the message   *
     *body of the request is in a format not supported by the server for  *
     *the requested method.  The server MUST return a list of acceptable  *
     *formats using the Accept, Accept-Encoding, or Accept-Language header*
     *field, depending on the specific problem with the content.          *
     **********************************************************************/ 
    ///////////////////////////////////////////////////////////////////////
	// 415 Response                                                      //
	///////////////////////////////////////////////////////////////////////
	public static SIPResponse response415(SIPRequest request)
	{
		
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.1");
		sipResponse.setStatusCode("415");
		sipResponse.setReasonPhrase("Media Not Supported\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());	
				
		return sipResponse;
	}

	/************************************************************************
	 * The callee's end system was contacted successfully, but the callee is*
     * currently not willing or able to take additional calls at this end   *
     * system.                                                              *
     ************************************************************************/ 
    ////////////////////////////////////////////////////////////////
	// 486 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response486(SIPRequest request)
	{
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.1");
		sipResponse.setStatusCode("486");
		sipResponse.setReasonPhrase("Busy Here\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());	
				
		return sipResponse;
	}
	/**********************************************************************
	 * The request was terminated by a BYE or CANCEL request.             *
     **********************************************************************/ 
    ////////////////////////////////////////////////////////////////
	// 487 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response487(SIPRequest request)
	{
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("487");
		sipResponse.setReasonPhrase("Request Terminated\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	/***********************************************************************
	 *The response has the same meaning as 606 (Not Acceptable), but only  *
     *applies to the specific resource addressed by the Request-URI and the* 
     *request may succeed elsewhere.                                       *
     ***********************************************************************/ 
    
	/////////////////////////////////////////////////////////////////////////
	// 488 Response                                                        //
	/////////////////////////////////////////////////////////////////////////
	public static SIPResponse response488(SIPRequest request)
	{
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("488");
		sipResponse.setReasonPhrase("Not Acceptable Here\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}
	
	/**********************************************************************
	 * The request was received by a UAS that had a pending request within*
     * the same dialog                                                    *
     **********************************************************************/ 
    
    ////////////////////////////////////////////////////////////////
	// 491 Response                                               //
	////////////////////////////////////////////////////////////////
	public static SIPResponse response491(SIPRequest request)
	{
		SIPResponse sipResponse = new SIPResponse();
		
		sipResponse.setSIPVersion("SIP/2.0");
		sipResponse.setStatusCode("491");
		sipResponse.setReasonPhrase("Request Pending\r");
		sipResponse.setTo(request.getTo());
		sipResponse.setFrom(request.getFrom());
		sipResponse.setCallId(request.getCallId());
		sipResponse.setCSeq(request.getCSeq());
		sipResponse.setRecord_Route(request.getRecord_Route());
		sipResponse.setContent_Length("0\r");
		sipResponse.setVia(request.getVia());
		
		return sipResponse;
	}

	////////////////////////////////////////////////////////////////
	public static String Mailbox(String ID) throws IOException
	{
		String spoonFeeding = "D:\\vms\\users";						 
		File f = new File(spoonFeeding);
		f.mkdir();
		
		File file = new File("users\\account.txt");
		
		boolean success;
		
		try
		{
			 success = file.createNewFile();
			
		}
		catch(Exception e)
		{
			System.out.println(e);		        								
		}
		
		RandomAccessFile raf = new RandomAccessFile(file,"rw");		
		String mailbox="",number,mailBox;
    	raf.seek(0);
    	    	
    	while((mailbox=raf.readLine())!=null)
    	{
    		mailBox=mailbox.substring(0,mailbox.indexOf("*"));
    		number=mailbox.substring(mailbox.indexOf("*")+1,mailbox.indexOf("-"));
    		
    		
    		if(ID.equals(mailBox) || ID.equals(number) )
    			return mailBox;
    	
    	}
		return null;	
   	
	}
}