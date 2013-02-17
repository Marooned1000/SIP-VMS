package ir.iranet.vms.sip;

import ir.iranet.vms.util.VMSLogger;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 12:19:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SIPResponse extends SIPMessage
{
    String SIPVersion;
    String StatusCode;
    String ReasonPhrase;
    String Proxy_Authenticate;
    String Retry_After;
    String Server;
    String Unsupported;
    String Warning;
    String WWW_Authenticate;

    public SIPResponse(String SIPVersion, String statusCode, String reasonPhrase) {
        this.SIPVersion = SIPVersion;
        StatusCode = statusCode;
        ReasonPhrase = reasonPhrase;
    }

    public SIPResponse() {
		// TODO Auto-generated constructor stub
	}	

	public String getSIPVersion() {
        return SIPVersion;
    }

    public void setSIPVersion(String SIPVersion) {
        this.SIPVersion = SIPVersion;
    }

    public String getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(String statusCode) {
        StatusCode = statusCode;
    }

    public String getReasonPhrase() {
        return ReasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        ReasonPhrase = reasonPhrase;
    }
    
    public String makeSipResponse(){
    	String message="";
    	return message;
    }
              
    public String getProxy_Authenticate() {
		return Proxy_Authenticate;
	}

	public void setProxy_Authenticate(String proxy_Authenticate) {
		Proxy_Authenticate = proxy_Authenticate;
	}

	public String getRetry_After() {
		return Retry_After;
	}

	public void setRetry_After(String retry_After) {
		Retry_After = retry_After;
	}

	public String getServer() {
		return Server;
	}

	public void setServer(String server) {
		Server = server;
	}

	public String getUnsupported() {
		return Unsupported;
	}

	public void setUnsupported(String unsupported) {
		Unsupported = unsupported;
	}

	public String getWarning() {
		return Warning;
	}

	public void setWarning(String warning) {
		Warning = warning;
	}

	public String getWWW_Authenticate() {
		return WWW_Authenticate;
	}

	public void setWWW_Authenticate(String authenticate) {
		WWW_Authenticate = authenticate;
	}

	
	
	///////////////////////////////////////////////////////////////////////
	//  Make SIP Response                                                //
	///////////////////////////////////////////////////////////////////////
	
	public String toString ()
    {
		VMSLogger.log(Level.INFO, "Start to make SIP response");
		
		String resMessage="";
		if(getSIPVersion()!=null)
			resMessage=resMessage+getSIPVersion()+" ";
		
		if(getStatusCode()!=null)
			resMessage=resMessage+getStatusCode()+" ";
		
		if(getReasonPhrase()!=null)
			resMessage=resMessage+getReasonPhrase()+"\n";
		
		if(getVia()!=null)
		{
			int count=getVia().size();
			for(int i=0;i<count;i++)
				if(getVia(i)!=null)
					resMessage=resMessage+"Via:"+getVia(i)+"\n";			
		}			
		
		if(getFrom()!=null)
			resMessage=resMessage+"From:"+getFrom()+"\n";
		
		if(getTo()!=null)
			resMessage=resMessage+"To:"+getTo()+"\n";
				
		if(getContact()!=null)
		{
			int count=getContact().size();
			for(int i=0;i<count;i++)
				if(getContact(i)!=null)
					resMessage=resMessage+"Contact:"+getContact(i)+"\n";
			
		}	
		if(getRecord_Route()!=null)
		{
			int count=getRecord_Route().size();
			for(int i=0;i<count;i++)
				if(getRecord_Route(i)!=null)
					resMessage=resMessage+"Record-Route:"+getRecord_Route(i)+"\n";			
		}
		
		if(getCallId()!=null)
			resMessage=resMessage+"Call-ID:"+getCallId()+"\n";
		
		if(getCSeq()!=null)
			resMessage=resMessage+"CSeq:"+getCSeq()+"\n";
		
		if(getAccept()!=null)
			resMessage=resMessage+"Accept:"+getAccept()+"\n";
		
		if(getAccept_Encoding()!=null)
			resMessage=resMessage+"Accept-Encoding:"+getAccept_Encoding()+"\n";
		
		if(getAccept_Language()!=null)
			resMessage=resMessage+"Accept-Language:"+getAccept_Language()+"\n";
		
		if(getAllow()!=null)
			resMessage=resMessage+"Allow:"+getAllow()+"\n";
		
						
		if(getContent_Desposition()!=null)
			resMessage=resMessage+"Content-Desposition:"+getContent_Desposition()+"\n";
		
		if(getContent_Encoding()!=null)
			resMessage=resMessage+"Content-Encoding:"+getContent_Encoding()+"\n";
		
		if(getContent_Language()!=null)
			resMessage=resMessage+"Content-Language:"+getContent_Language()+"\n";
		
		if(getContent_Type()!=null)
			resMessage=resMessage+"Content-Type:"+getContent_Type()+"\n";
		
		if(getContent_Length()!=null)
			resMessage=resMessage+"Content-Length:"+getContent_Length()+"\n";		
		
		if(getDate()!=null)
			resMessage=resMessage+"Date:"+getDate()+"\n";
		
		if(getEncryption()!=null)
			resMessage=resMessage+"Encryption:"+getEncryption()+"\n";
		
		if(getExpires()!=null)
			resMessage=resMessage+"Expires:"+getExpires()+"\n";
		
		if(getOrganization()!=null)
			resMessage=resMessage+"Organization:"+getOrganization()+"\n";
		
		if(getProxy_Authenticate()!=null)
			resMessage=resMessage+"Proxy-Authenticate:"+getProxy_Authenticate()+"\n";
		
		if(getRetry_After()!=null)
			resMessage=resMessage+"Retry-After:"+getRetry_After()+"\n";
		
		if(getRequire()!=null)
			resMessage=resMessage+"Require:"+getRequire()+"\n";
		
		if(getServer()!=null)
			resMessage=resMessage+"Server:"+getServer()+"\n";
		
		if(getSupported()!=null)
			resMessage=resMessage+"Supported:"+getSupported()+"\n";
		
		if(getTimestamp()!=null)
			resMessage=resMessage+"Timestamp:"+getTimestamp()+"\n";
		
		if(getUnsupported()!=null)
			resMessage=resMessage+"Unsupported:"+getUnsupported()+"\n";
		
		if(getUser_Agent()!=null)
			resMessage=resMessage+"User-Agent:"+getUser_Agent()+"\n";
		
		if(sdp!=null)
		{
			resMessage=resMessage+"\r"+"\n";			
			String s = sdp.toString();
			resMessage = resMessage + s;
			
		}
		VMSLogger.log(Level.INFO, "Make a SIP response");
		return resMessage;
    }


}
