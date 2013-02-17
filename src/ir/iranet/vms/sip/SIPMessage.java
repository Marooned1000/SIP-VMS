package ir.iranet.vms.sip;
import ir.iranet.vms.util.Utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import ir.iranet.vms.SessionHandler;
import ir.iranet.vms.sdp.SDP;
import ir.iranet.vms.util.VMSLogger;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class SIPMessage 

{
	//General headers
	String Accept;
	String Accept_Encoding;
	String Accept_Language;
	String CallId;
	ArrayList Contact = new ArrayList(); 
	String CSeq;
	String Date;
	String Encryption;
	String From;
	String Organization;
	ArrayList Record_Route = new ArrayList();	
	String Require;
	String Supported;
	String Timestamp;
	String To;
	String User_Agent;
	ArrayList Via=new ArrayList();
	
	// Entity headers		
	String Allow;
	String Content_Desposition;
	String Content_Encoding;
	String Content_Language;
	String Content_Length;
	String Content_Type;
	String Expires;	
	
	// Message Body
	public SDP sdp;
	
	
	//byte[] sipPacket;
	static int start,end;
	static boolean body;
	static String SMT;       //Session Description(S) or Media Description(M) or Time Description(T)
	static Utility siputils;
	
	
	
	//Constructors
	public SIPMessage ()
	{
		sdp = new SDP();
	}
	
	// Setter & Getter	
	public String getAccept() {
		return Accept;
	}

	public void setAccept(String accept) {
		Accept = accept;
	}

	public String getAllow() {
		return Allow;
	}

	public void setAllow(String allow) {
		Allow = allow;
	}
	
	public String getCallId() {
		return CallId;
	}

	public void setCallId(String callId) {
		CallId = callId;
	}

	public String getCSeq() {
		return CSeq;
	}

	public void setCSeq(String CSeq) {
		this.CSeq = CSeq;
	}

	public String getContact(int i) {
		return (String) Contact.get(i);
	}
	public ArrayList getContact(){
		return Contact;
	}

	public void setContact(String contact,int i) {
		Contact.add(i,contact);		
	}	
	public void setContact(ArrayList contact) {
		Contact= contact;
	}	
	
	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	public String getExpires() {
		return Expires;
	}

	public void setExpires(String expires) {
		Expires = expires;
	}

	public String getFrom() {
		return From;
	}

	public void setFrom(String from) {
		From = from;
	}	

	public String getOrganization() {
		return Organization;
	}

	public void setOrganization(String organization) {
		Organization = organization;
	}

	public String getRequire() {
		return Require;
	}

	public void setRequire(String require) {
		Require = require;
	}

	public String getSupported() {
		return Supported;
	}

	public void setSupported(String supported) {
		Supported = supported;
	}

	public String getTimestamp() {
		return Timestamp;
	}

	public void setTimestamp(String timestamp) {
		Timestamp = timestamp;
	}

	public String getTo() {
		return To;
	}

	public void setTo(String to) {
		To = to;
	}
	
	public String getVia(int i) {
		return (String) Via.get(i);
	}
	public ArrayList getVia(){
		return Via;
	}

	public void setVia(String via,int i) {		
		Via.add(i,via);
	}
	public void setVia(ArrayList via) {
		Via = via;
	}

	public String getAccept_Encoding() {
		return Accept_Encoding;
	}


	public void setAccept_Encoding(String accept_Encoding) {
		Accept_Encoding = accept_Encoding;
	}


	public String getAccept_Language() {
		return Accept_Language;
	}


	public void setAccept_Language(String accept_Language) {
		Accept_Language = accept_Language;
	}


	public String getContent_Desposition() {
		return Content_Desposition;
	}


	public void setContent_Desposition(String content_Desposition) {
		Content_Desposition = content_Desposition;
	}


	public String getContent_Encoding() {
		return Content_Encoding;
	}


	public void setContent_Encoding(String content_Encoding) {
		Content_Encoding = content_Encoding;
	}


	public String getContent_Language() {
		return Content_Language;
	}


	public void setContent_Language(String content_Language) {
		Content_Language = content_Language;
	}


	public String getContent_Length() {
		return Content_Length;
	}


	public void setContent_Length(String content_Length) {
		Content_Length = content_Length;
	}


	public String getContent_Type() {
		return Content_Type;
	}


	public void setContent_Type(String content_Type) {
		Content_Type = content_Type;
	}


	public String getEncryption() {
		return Encryption;
	}


	public void setEncryption(String encryption) {
		Encryption = encryption;
	}


	public String getRecord_Route(int i) {
		return (String) Record_Route.get(i);
	}
	
	public ArrayList getRecord_Route(){
		return Record_Route;
	}

	public void setRecord_Route(String record_Route,int i) {		
		Record_Route.add(i,record_Route);
	}
	public void setRecord_Route(ArrayList record_Route) {
		Record_Route = record_Route;
	}

	public String getUser_Agent() {
		return User_Agent;
	}


	public void setUser_Agent(String user_Agent) {
		User_Agent = user_Agent;
	}
	
	
	////////////////////////////////////////////////////////////////////
	// SIP Message Parser                                             //
	////////////////////////////////////////////////////////////////////

		public SIPMessage parser(DatagramPacket packet)
		{		
			
			byte[] sipPacket = new byte[1500];
			sipPacket=null;
			
			VMSLogger.log(Level.INFO, "Start to parse a SIP message.");
			siputils = new Utility();
			sipPacket=packet.getData();	
			String message=new String(sipPacket);			
			start=0;			
			if(!message.equals("")){
				end=message.indexOf(" ");
				
				if(end==-1){
			         
					//SIPUtils.response400()
					//Syntax Error
					//siputils.SetErrorNumber(400);
					return null;
				}
				
				String str;
				str=message.substring(start,end);
				if(str.equals("SIP/2.0"))   // SIP message is a response
				{
					SIPResponse response=new SIPResponse();
					parse(message,response);
					return response;
				}
				else						// SIP message is a request
				{			
					//System.out.println("++++++++++++ here is request ++++++++++++++");
					SIPRequest request=new SIPRequest();
					parse(message,request);
					return request;
				}
			}
			return null;
		}
		
		////////////////////////////////////////////////////////////////
	    //	Parse SIP Requests
		////////////////////////////////////////////////////////////////
		private static void parse(String message,SIPRequest request)
		{		
			start=0;
			body=false;
			
					
			VMSLogger.log(Level.INFO, "SIP message is a request");
			
			if(!message.equals(""))
			{
				
				end=message.indexOf(" ");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				request.setMethod(message.substring(start,end));			
				message=message.substring(end+1);			
			}
			else{
				//syntax error
				
			}
			
			if(!message.equals(""))
			{			
				end=message.indexOf(" ");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				
				request.setRequestURI(message.substring(start,end));			
				message=message.substring(end+1);			
			}
			else{
				//syntax error
				
			}
			if(!message.equals(""))
			{
				end=message.indexOf("\n");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				request.setSIPVersion(message.substring(start,end));		
				message=message.substring(end+1);		
				
			}
			else{
				//syntax error
				
			}
			
			
			int viaNum=0;
			int contactNum=0;
			int routeNum=0;
			int record_routeNum=0;
			while(!message.equals(""))
			{	
				if(body==false){
					end=message.indexOf(":");
					if(end==-1){
						//Syntax Error
						
						return;
					}
					String headerName=message.substring(start,end);
					message=message.substring(end+1);
					end=message.indexOf("\n");
					if(end==-1){
						//Syntax Error
						
						return;
					}
				
					if(headerName.equals("Via")){
						request.setVia(message.substring(start,end),viaNum++);									
					}
					else if(headerName.equals("To")){
						request.setTo(message.substring(start,end));
					}
					else if(headerName.equals("From")){
						request.setFrom(message.substring(start,end));
					}
					else if(headerName.equals("Call-ID")){
						request.setCallId(message.substring(start,end));
					}
					else if(headerName.equals("Authorization")){
						request.setAuthorization(message.substring(start,end));
					}							
					else if(headerName.equals("Accept")){
						request.setAccept(message.substring(start,end));
					}
					else if(headerName.equals("Accept-Encoding")){
						request.setAccept_Encoding(message.substring(start,end));
					}	
					else if(headerName.equals("Accept-Language")){
						request.setAccept_Encoding(message.substring(start,end));
					}
					else if(headerName.equals("Allow")){
						request.setAllow(message.substring(start,end));
					}				
					else if(headerName.equals("CSeq")){
						request.setCSeq(message.substring(start,end));
					}
					else if(headerName.equals("Contact")){
						request.setContact(message.substring(start,end),contactNum++);					
					}
					else if(headerName.equals("Content-Desposition")){
						request.setContent_Desposition(message.substring(start,end));
					}
					else if(headerName.equals("Content-Encoding")){
						request.setContent_Encoding(message.substring(start,end));
					}
					else if(headerName.equals("Content-Length")){
						request.setContent_Length(message.substring(start,end));					
					}
					else if(headerName.equals("Content-Type")){
						request.setContent_Type(message.substring(start,end));
					}
					else if(headerName.equals("Content-Language")){
						request.setContent_Language(message.substring(start,end));
					}				
					else if(headerName.equals("Date")){
						request.setDate(message.substring(start,end));
					}				
					else if(headerName.equals("Expires")){
						request.setExpires(message.substring(start,end));
					}
					else if(headerName.equals("In-Reply-To")){
						request.setIn_Reply_To(message.substring(start,end));
					}				
					else if(headerName.equals("Organization")){
						request.setOrganization(message.substring(start,end));
					}											
					else if(headerName.equals("Require")){
						request.setRequire(message.substring(start,end));
					}				
					else if(headerName.equals("Route")){
						request.setRoute(message.substring(start,end),routeNum++);					
					}				
					else if(headerName.equals("Subject")){
						request.setSubject(message.substring(start,end));
					}
					else if(headerName.equals("Supported")){
						request.setSupported(message.substring(start,end));
					}
					else if(headerName.equals("Timestamp")){
						request.setTimestamp(message.substring(start,end));
					}				
					else if(headerName.equals("User-Agent")){
						request.setUser_Agent(message.substring(start,end));
					}
					else if(headerName.equals("Encryption")){
						request.setEncryption(message.substring(start,end));
					}
					else if(headerName.equals("Record-Route")){
						request.setRecord_Route(message.substring(start,end),record_routeNum++);					
					}
					else if(headerName.equals("Hide")){
						request.setHide(message.substring(start,end));
					}
					else if(headerName.equals("Max-Forwards")){
						request.setMax_Forward(message.substring(start,end));
					}
					else if(headerName.equals("Priority")){
						request.setPriority(message.substring(start,end));
					}
					else if(headerName.equals("Proxy-Authentication")){
						request.setProxy_Authentication(message.substring(start,end));
					}
					else if(headerName.equals("Proxy-Require")){
						request.setProxy_Require(message.substring(start,end));
					}
					else if(headerName.equals("Response-Key")){
						request.setResponse_Key(message.substring(start,end));
					}
					else
					{
						//siputils.response415();
					}
					
				}
				else
				{
					SDP.parse(message,request);
					break;
				}
																										
				message=message.substring(end+1);			
				
				if(!message.equals("")){
					String s=message.substring(0,1);				
					if (s.equals("\r"))
					{	
						body=true;
						message=message.substring(2);												
					}
				}
				
			}	//While
			VMSLogger.log(Level.INFO, "SIP request is parsed.");
			
		}
		
		///////////////////////////////////////////////////////////////
		//Parse SIP Responses                                        //
		///////////////////////////////////////////////////////////////
		private static void parse(String message,SIPResponse response)
		{
			
			VMSLogger.log(Level.INFO, "SIP message is a response.");
			
			start=0;
			body=false;
				
			message="SIP/2.0 200 Ok"+"\n";
			
			if(!message.equals(""))
			{
				
				end=message.indexOf(" ");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				response.setSIPVersion(message.substring(start,end));						
				message=message.substring(end+1);		
			}
			else{
				//Syntax error
				
			}
			
			if(!message.equals(""))
			{			
				end=message.indexOf(" ");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				response.setStatusCode(message.substring(start,end));			
				message=message.substring(end+1);		
			}
			
			if(!message.equals(""))
			{
				end=message.indexOf("\n");
				if(end==-1){
					//Syntax Error
					
					return;
				}
				response.setReasonPhrase(message.substring(start,end));			
				message=message.substring(end+1);			
				
			}
			
			int contactNum=0;
			int record_routeNum=0;
			int viaNum=0;
			
			while(!message.equals(""))
			{	
				if(body==false){
					end=message.indexOf(":");
					if(end==-1){
						//Syntax Error
					
						return;
					}
					String headerName=message.substring(start,end);
					message=message.substring(end+1);
					end=message.indexOf("\n");
					if(end==-1){
						//Syntax Error
						
						return;
					}
					
					
					//	General Headers
					if(headerName.equals("Accept")){
						response.setAccept(message.substring(start,end));						
					}
					else if(headerName.equals("Accept-Encoding")){
						response.setAccept_Encoding(message.substring(start,end));
					}
					else if(headerName.equals("Accept-Language")){
						response.setAccept_Language(message.substring(start,end));
					}
					else if(headerName.equals("Call-ID")){
						response.setCallId(message.substring(start,end));
					}
					else if(headerName.equals("Contact")){
						response.setContact(message.substring(start,end),contactNum++);					
					}							
					else if(headerName.equals("CSeq")){
						response.setCSeq(message.substring(start,end));
					}
					else if(headerName.equals("Date")){
						response.setDate(message.substring(start,end));
					}	
					else if(headerName.equals("Encryption")){
						response.setEncryption(message.substring(start,end));
					}
					else if(headerName.equals("From")){
						response.setFrom(message.substring(start,end));
					}				
					else if(headerName.equals("Organization")){
						response.setOrganization(message.substring(start,end));
					}				
					else if(headerName.equals("Record-Route")){
						response.setRecord_Route(message.substring(start,end),record_routeNum++);					
					}
					else if(headerName.equals("Require")){
						response.setRequire(message.substring(start,end));
					}
					else if(headerName.equals("Supported")){
						response.setSupported(message.substring(start,end));
					}
					else if(headerName.equals("Timestamp")){
						response.setTimestamp(message.substring(start,end));
					}
					else if(headerName.equals("To")){
						response.setTo(message.substring(start,end));
					}
					else if(headerName.equals("User-Agent")){
						response.setUser_Agent(message.substring(start,end));
					}				
					else if(headerName.equals("Via")){
						response.setVia(message.substring(start,end),viaNum++);					
					}
					
					
					//Entity Headers
					else if(headerName.equals("Allow")){
						response.setAllow(message.substring(start,end));
					}				
					else if(headerName.equals("Content-Desposition")){
						response.setContent_Desposition(message.substring(start,end));
					}											
					else if(headerName.equals("Content-Encoding")){
						response.setContent_Encoding(message.substring(start,end));
					}				
					else if(headerName.equals("Content-Language")){
						response.setContent_Language(message.substring(start,end));
					}				
					else if(headerName.equals("Content-Length")){
						response.setContent_Length(message.substring(start,end));
					}
					else if(headerName.equals("Content-Type")){
						response.setContent_Type(message.substring(start,end));
					}
					else if(headerName.equals("Expires")){
						response.setExpires(message.substring(start,end));
					}
					
					
					// Response Headers
					else if(headerName.equals("Proxy-Authenticate")){
						response.setProxy_Authenticate(message.substring(start,end));
					}
					else if(headerName.equals("Retry-After")){
						response.setRetry_After(message.substring(start,end));
					}
					else if(headerName.equals("Server")){
						response.setServer(message.substring(start,end));
					}
					else if(headerName.equals("Unsupported")){
						response.setUnsupported(message.substring(start,end));
					}
					else if(headerName.equals("Warning")){
						response.setWarning(message.substring(start,end));
					}
					else if(headerName.equals("WWW-Authenticate")){
						response.setWWW_Authenticate(message.substring(start,end));
					}	
					else
					{
						//Syntax error
						
					}
				}
				else
				{
					SDP.parse(message,response);
					break;
				}
																										
				message=message.substring(end+1);	
				
				if(!message.equals("")){
					String s=message.substring(0,1);
					if (s.equals("\n"))
					{	
						body=true;
						message=message.substring(1);												
					}
				}
			}		
			VMSLogger.log(Level.INFO, "SIP Response is parsed.");			
		}
			
		
	
	////////////////////////////////////////////////////////////////////
	// Send SIP Message                                               //
	////////////////////////////////////////////////////////////////////
	public void send(byte[] packet,InetAddress address,int port) throws IOException
	{
		
		DatagramSocket socket = new DatagramSocket();			
		DatagramPacket sipPacket = new DatagramPacket(packet, packet.length, address, port);            		
		socket.send(sipPacket);   
		VMSLogger.log(Level.INFO,"Sent to    address:"+address+"     port:"+port);
		
	}
	//////////////////////////////////////////////////////////////////////////////
	
	
}
