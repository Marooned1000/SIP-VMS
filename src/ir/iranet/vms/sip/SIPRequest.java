package ir.iranet.vms.sip;

import ir.iranet.vms.util.VMSLogger;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 12:18:47 PM
 * To change this template use File | Settings | File Templates.
 */

public class SIPRequest extends SIPMessage
{
	
	String Method;     
    String RequestURI;
    String SIPVersion;
    String Authorization;
    String Hide;
    String In_Reply_To;
    String Max_Forward;
    String Priority;
    String Proxy_Authentication;
    String Proxy_Require;
    ArrayList Route=new ArrayList();
    String Response_Key;
    String Subject;
 
    
    
    public SIPRequest() {
				
	}	
        
    public String getMethod() {
        return Method;
    }
    
    public void setMethod(String method) {
        Method = method;
    }

    public String getRequestURI() {
        return RequestURI;
    }

    public void setRequestURI(String requestURI) {
        RequestURI = requestURI;
    }

    public String getSIPVersion() {
        return SIPVersion;
    }

    public void setSIPVersion(String SIPVersion) {
        this.SIPVersion = SIPVersion;
    }
    
    public String getAuthorization() {
		return Authorization;
	}

	public void setAuthorization(String authorization) {
		Authorization = authorization;
	}

	public String getHide() {
		return Hide;
	}

	public void setHide(String hide) {
		Hide = hide;
	}

	public String getIn_Reply_To() {
		return In_Reply_To;
	}

	public void setIn_Reply_To(String in_Reply_To) {
		In_Reply_To = in_Reply_To;
	}

	public String getMax_Forward() {
		return Max_Forward;
	}

	public void setMax_Forward(String max_Forward) {
		Max_Forward = max_Forward;
	}

	public String getPriority() {
		return Priority;
	}

	public void setPriority(String priority) {
		Priority = priority;
	}

	public String getProxy_Authentication() {
		return Proxy_Authentication;
	}

	public void setProxy_Authentication(String proxy_Authentication) {
		Proxy_Authentication = proxy_Authentication;
	}

	public String getProxy_Require() {
		return Proxy_Require;
	}

	public void setProxy_Require(String proxy_Require) {
		Proxy_Require = proxy_Require;
	}

	public String getResponse_Key() {
		return Response_Key;
	}

	public void setResponse_Key(String response_Key) {
		Response_Key = response_Key;
	}

	public String getRoute(int i) {
		return (String) Route.get(i);
	}
	
	public ArrayList getRoute(){
		return Route;
	}
	
	public void setRoute(String route,int i) {		
		Route.add(i,route);
	}	
	public void setRoute(ArrayList route){
		Route=route;
	}

	public String getSubject() {
		return Subject;
	}

	public void setSubject(String subject) {
		Subject = subject;
	}
	
	
	
	/////////////////////////////////////////////////////////////
	public String toString ()
	{
		VMSLogger.log(Level.INFO, "Start to make SIP request." + getCallId());
		
		String reqMessage="";
		if(getMethod()!=null)
			reqMessage=reqMessage+getMethod()+" ";
		
		if(getRequestURI()!=null)
			reqMessage=reqMessage+getRequestURI()+" ";
		
		if(getSIPVersion()!=null)
			reqMessage=reqMessage+getSIPVersion()+"\n";
		
		
								
		if(getTo()!=null)
			reqMessage=reqMessage+"To:"+getTo()+"\n";
		
		if(getFrom()!=null)
			reqMessage=reqMessage+"From:"+getFrom()+"\n";
		
		if(getVia()!=null)
		{
			int count=getVia().size();
			for(int i=0;i<count;i++)
				reqMessage=reqMessage+"Via:"+getVia(i)+"\n";
		}
		
		if(getCallId()!=null)
			reqMessage=reqMessage+"Call-ID:"+getCallId()+"\n";
		
		if(getAuthorization()!=null)
			reqMessage=reqMessage+"Authorization:"+getAuthorization()+"\n";
		
		if(getCSeq()!=null)
			reqMessage=reqMessage+"CSeq:"+getCSeq()+"\n";
		
		if(getMax_Forward()!=null)
			reqMessage=reqMessage+"Max-Forwards:"+getMax_Forward()+"\n";
		

		if(getContact()!=null)
		{
			int count=getContact().size();
			for(int i=0;i<count;i++)
				reqMessage=reqMessage+"Contact:"+getContact(i)+"\n";
		}
		
		if(getExpires()!=null)
			reqMessage=reqMessage+"Expires:"+getExpires()+"\n";
		
		if(getAccept()!=null)
			reqMessage=reqMessage+"Accept:"+getAccept()+"\n";
		
		if(getAccept_Encoding()!=null)
			reqMessage=reqMessage+"Accept-Encoding:"+Accept_Encoding+"\n";
		
		if(getAccept_Language()!=null)
			reqMessage=reqMessage+"Accept-Language:"+getAccept_Language()+"\n";
		
		if(getDate()!=null)
			reqMessage=reqMessage+"Date:"+getDate()+"\n";
		
		if(getAllow()!=null)
			reqMessage=reqMessage+"Allow:"+getAllow()+"\n";
		
		if(getEncryption()!=null)
			reqMessage=reqMessage+"Encryption:"+getEncryption()+"\n";
		
		if(getHide()!=null)
			reqMessage=reqMessage+"Hide:"+getHide()+"\n";
		
		if(getIn_Reply_To() !=null)
			reqMessage=reqMessage+"In-Reply-To:"+getIn_Reply_To()+"\n";
		
		if(getOrganization()!=null)
			reqMessage=reqMessage+"Organization:"+getOrganization()+"\n";
		
		if(getPriority()!=null)
			reqMessage=reqMessage+"Priority:"+getPriority()+"\n";
		
		if(getProxy_Authentication()!=null)
			reqMessage=reqMessage+"Proxy-Authentication:"+getProxy_Authentication()+"\n";
		
		if(getProxy_Require()!=null)
			reqMessage=reqMessage+"Proxy-Require:"+getProxy_Require()+"\n";
		

		if(getRecord_Route()!=null)
		{
			int count=getRecord_Route().size();
			for(int i=0;i<count;i++)
				reqMessage=reqMessage+"Record-Route:"+getRecord_Route(i)+"\n";			
		}					
		
		if(getRoute()!=null)
		{
			int count=getRoute().size();
			for(int i=0;i<count;i++)
				reqMessage=reqMessage+"Route:"+getRoute(i)+"\n";			
		}
		if(getRequire()!=null)
			reqMessage=reqMessage+"Require:"+getRequire()+"\n";
		
		if(getResponse_Key()!=null)
			reqMessage=reqMessage+"Response-Key:"+getResponse_Key()+"\n";
		

		if(getRoute()!=null)
		{
			int count=getRoute().size();
			for(int i=0;i<count;i++)
				reqMessage=reqMessage+"Route:"+getRoute(i)+"\n";
		}				
		
		if(getSubject()!=null)
			reqMessage=reqMessage+"Subject:"+getSubject()+"\n";
		
		if(getSupported()!=null)
			reqMessage=reqMessage+"Supported:"+getSupported()+"\n";
		
		if(getTimestamp()!=null)
			reqMessage=reqMessage+"Timestamp:"+getTimestamp()+"\n";
		
		if(getUser_Agent()!=null)
			reqMessage=reqMessage+"User-Agent:"+getUser_Agent()+"\n";
		
		if(getContent_Desposition()!=null)
			reqMessage=reqMessage+"Content-Desposition:"+getContent_Desposition()+"\n";
		
		if(getContent_Encoding()!=null)
			reqMessage=reqMessage+"Content-Encoding:"+getContent_Encoding()+"\n";

		if(getContent_Language()!=null)
			reqMessage=reqMessage+"Content-Language:"+getContent_Language()+"\n";
		
		if(getContent_Length()!=null)
			reqMessage=reqMessage+"Content-Length:"+getContent_Length()+"\n";		
		
		if(getContent_Type()!=null)
			reqMessage=reqMessage+"Content-Type:"+getContent_Type()+"\n";
		
		if(sdp!=null)
		{
			reqMessage=reqMessage+"\r\n"+sdp.toString();
			
		}
		VMSLogger.log(Level.INFO, "Maked a SIP request ");
		return reqMessage;
	}	


}
