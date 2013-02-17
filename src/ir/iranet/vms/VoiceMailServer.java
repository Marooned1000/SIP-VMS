package ir.iranet.vms;
import ir.iranet.vms.sip.SIPMessage;
import ir.iranet.vms.util.TimeoutInt;
import ir.iranet.vms.util.VMSLogger;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.io.File;
import ir.iranet.vms.util.Utility;

public class VoiceMailServer
{
	
	DatagramSocket socket;
	HashMap sessionHash;
	public static final int LISTEN_PORT = 5090;
	
	public class Handler extends Thread
	{
		DatagramPacket packet;
		
		
		public VoiceMailServer getOuter ()
		{
			return VoiceMailServer.this;
		}
		
		Handler (DatagramPacket packet)
		{			
			this.packet = packet;
		}
		
	/***************************************************************
	 * If received packet is not related to the current sessions   *
	 * create a new session handler else use related sessionhandler* 
	 * then send sipMessage to the AddNewSip.                      *
	 * *************************************************************/
		public void run ()
		{
			System.out.println("Handler");
			SessionHandler sessionHandler;
			SIPMessage sipMessage =new SIPMessage(); 
			sipMessage=sipMessage.parser(packet);
			
			if ( sessionHash.get(sipMessage.getCallId()) == null )
			{	
				//Request doesn't related to the current sessions
				sessionHandler = new SessionHandler();
				sessionHash.put( (sipMessage.getCallId()), sessionHandler );
						
			} 
			else 
			{
				//Request related to the current sessions
				sessionHandler = (SessionHandler) sessionHash.get(sipMessage.getCallId());			
				System.out.println("Session Exist.");
			}
			
			try
			{
				sessionHandler.AddNewSip(sipMessage,sessionHash);
			}
			catch(Exception e)
			{
				
			}
		}
	}	
	
	VoiceMailServer() 
	{
		sessionHash = new HashMap();
	}
	
	/*****************************************
	 * Listen to the SIP port(5060)and send  *
	 * received packet to the handler        * 
	 *****************************************/
	public void run() 
	{		
		byte[] inbuf = new byte[1500];
		DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
		
	
		
		while(true){
			try {
				
				socket = new DatagramSocket(LISTEN_PORT);				
				VMSLogger.log(Level.INFO, "Listening to the port:"+LISTEN_PORT);
								
				socket.receive(packet);					
				VMSLogger.log(Level.INFO, "Received from the port:"+LISTEN_PORT);
				socket.close();
				String s=new String(packet.getData());
				System.out.println(s);
				
				DatagramPacket copyPacket = new DatagramPacket(packet.getData(), packet.getLength());
				
				new Handler (copyPacket).start();
						
			} catch (SocketException se) 
			{
				// TODO Auto-generated catch block
				se.printStackTrace();
			} catch (IOException ioe) 
			{
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}
	
	
	public static void main (String[] argv) throws IOException
	{	
		VoiceMailServer vms = new VoiceMailServer();
		vms.run();                      
     }

	}
	

