package ir.iranet.vms;
import ir.iranet.vms.util.Utility;

import ir.iranet.vms.dtmf.DTMFMessage;
import ir.iranet.vms.rtp.RTPMessage;
import ir.iranet.vms.util.VMSLogger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.logging.Level;



/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:13:01 AM
 * To change this template use File | Settings | File Templates.
 */

public  class RTPServer extends Thread
{

	HashMap MediaHash;
	//int LISTEN_PORT;
	byte[] buf=new byte[53];
	SessionHandler sessionHandler;
	byte[] timestamp=new byte[4];	
	HashMap rtpHash;
	int LISTEN_PORT;
	
	RTPServer(){
		
	}
	RTPServer(SessionHandler sh,int port,HashMap RtpHash)
	{
		MediaHash=new HashMap();
		LISTEN_PORT = new Integer(0);
		LISTEN_PORT=port;
		sessionHandler=new SessionHandler();
		sessionHandler=sh;
		rtpHash = new HashMap();
		rtpHash=RtpHash;
	}
	
	/////////////////////////////////////////////////////
    /******************************************************************
     * Server listens to the rtp port,if it waits for the rtp packets * 
     * call AddNewRTP else if it receive expected dtmf packet,alarms  *
     * rtp handler.                                                   *
     ******************************************************************/
	public  void  run()
	{
		try{
			
			VMSLogger.log(Level.INFO, "Listening to the RTPport :"+LISTEN_PORT);
							
			DatagramSocket socket = new DatagramSocket(LISTEN_PORT);
				
			while(true)
			{				
				DatagramPacket packet=new DatagramPacket(buf,buf.length);						
				socket.receive(packet);		
				RTPMessage rtpMessage=new RTPMessage();				
				rtpMessage=rtpMessage.Assembler(packet);
				
				if(rtpMessage.getM_PT()[0]==3)
				{	
					System.out.println("----------------RTP Packet --------------");
					rtpHash.put(rtpMessage.getTimeStamp(),rtpMessage.getData());
					new RTPHandler().AddNewRtp(packet,sessionHandler,rtpHash,this);						
				    
				}				
				else if(rtpMessage.getM_PT()[0]==101 && Utility.CompareBytes(timestamp,rtpMessage.getTimeStamp())!=0)
				{
					System.out.println("----------------DTMF Packet --------------");
					DTMFMessage dtmfMessage=new DTMFMessage();
					dtmfMessage=dtmfMessage.Assembler(rtpMessage.getData());	
					int option=dtmfMessage.getEvent();
					
					if(option==0 || option==1 ||  option==2 || option==3 ||
					   option==4 || option==11 )
					{
						rtpHash.clear();							
						timestamp=rtpMessage.getTimeStamp();
						new RTPHandler(sessionHandler,option);
						socket.close();
					}									
				}		
						
			}		
	}
		
		catch(Exception e)
		{			
			System.out.println("----------------RTP Server exception---------------"+e);
		}
		}
	
	
}
