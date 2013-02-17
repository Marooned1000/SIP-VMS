package ir.iranet.vms.sdp;

import ir.iranet.vms.util.Utility;
import ir.iranet.vms.sip.SIPMessage;
import ir.iranet.vms.util.VMSLogger;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class SDP
{
    public SessionDescription sessionDescription;
    public TimeDescription timeDescription;    
    public MediaDescription mediaDescription;
    
    public SDP() {
    	sessionDescription=new SessionDescription();
    	timeDescription=new TimeDescription();
    	mediaDescription = new MediaDescription();
    	//for(int i=0; i<5; i++)
    	//	MediaDescription[i]= new MediaDescription();
    	
		// TODO Auto-generated constructor stub
	}

	public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public TimeDescription getTimeDescription() {
        return timeDescription;
    }

    public void setTimeDescription(TimeDescription timeDescription) {
        this.timeDescription = timeDescription;
    }

    public MediaDescription getMediaDescription() {
        return mediaDescription;
    }
        
    public void setMediaDescription(MediaDescription mediaDescription) {
    	this.mediaDescription=mediaDescription;
        
    }

    
    
    ///////////////////////////////////////////////////////////////////////////

    public String toString ()
    {
    	VMSLogger.log(Level.INFO, "Start to make SIP message body.");
    	String message = "";		
		
		//Session Description
		
		
		if(getSessionDescription().getV()!=null)
			message=message+"v="+getSessionDescription().getV()+"\n";
		
		if(getSessionDescription().getO()!=null)
			message=message+"o="+getSessionDescription().getO()+"\n";
		
		if(getSessionDescription().getS()!=null)
			message=message+"s="+getSessionDescription().getS()+"\n";
		
		if(getSessionDescription().getA()!=null)
		{
			int count=getSessionDescription().getA().size();
			for(int i=0;i<count;i++)
				if(getSessionDescription().getA(i)!=null)
					message=message+"a="+getSessionDescription().getA(i)+"\n";
		}
					
		if(getSessionDescription().getB()!=null)
		{
			int count=getSessionDescription().getB().size();			
			for(int i=0;i<count;i++)
				if(getSessionDescription().getB(i)!=null)
					message=message+"b="+getSessionDescription().getB(i)+"\n";
		}
					
		if(getSessionDescription().getC()!=null)
			message=message+"c="+getSessionDescription().getC()+"\n";

		if(getSessionDescription().getE()!=null)
		{
			int count=getSessionDescription().getE().size();
			for(int i=0;i<count;i++)
				if(getSessionDescription().getE(i)!=null)
					message=message+"e="+getSessionDescription().getE(i)+"\n";
		}
					
		if(getSessionDescription().getI()!=null)
			message=message+"i="+getSessionDescription().getI()+"\n";		

		if(getSessionDescription().getK()!=null)
			message=message+"k="+getSessionDescription().getK()+"\n";		

				

		if(getSessionDescription().getP()!=null)
		{
			int count=getSessionDescription().getP().size();
			for(int i=0;i<count;i++)
				if(getSessionDescription().getP(i)!=null)
					message=message+"p="+getSessionDescription().getP(i)+"\n";
		}
					
		if(getSessionDescription().getU()!=null)
			message=message+"u="+getSessionDescription().getU()+"\n";				
		
		if(getSessionDescription().getZ()!=null)
			message=message+"z="+getSessionDescription().getZ()+"\n";

		
		//Time Description

		if(getTimeDescription().getT()!=null)
		{
			int count=getTimeDescription().getT().size();
			for(int i=0;i<count;i++)
				if(getTimeDescription().getT(i)!=null)
					message=message+"t="+getTimeDescription().getT(i)+"\n";
		}
							
		if(getTimeDescription().getR()!=null)
			message=message+"r"+getTimeDescription().getR()+"\n";
		
		//Media Description

		//int count=getMediaDescription().size();
	//	for(int i=0;i<count;i++)
		//{
			if(getMediaDescription().getM()!=null)
				message=message+"m="+getMediaDescription().getM()+"\n";			

			if(getMediaDescription().getA()!=null)
			{
				int c=getMediaDescription().getA().size();
				for(int j=0;j<c;j++)
					if(getMediaDescription().getA(j)!=null)
						message=message+"a="+getMediaDescription().getA(j)+"\n";
			}
					
			if(getMediaDescription().getB()!=null)
				message=message+"b="+getMediaDescription().getB()+"\n";

			if(getMediaDescription().getC()!=null)
			{
				int c=getMediaDescription().getC().size();
				for(int j=0;j<c;j++)
					if(getMediaDescription().getC(j)!=null)
						message=message+"c="+getMediaDescription().getC(j)+"\n";
			}
				
			if(getMediaDescription().getI()!=null)
				message=message+"i="+getMediaDescription().getI()+"\n";

			if(getMediaDescription().getK()!=null)
				message=message+"k="+getMediaDescription().getK()+"\n";			

		
		VMSLogger.log(Level.INFO, "make SIP message body.");
		return message;
    }
    
	////////////////////////////////////////////////////////
	// parse SDP
	//////////////////////////////////////////////////////////
	public static void parse(String message,SIPMessage s)
	{
		int start=0;
		int end;
		String SMT="";       //Session Description(S) or Media Description(M) or Time Description(T)		
		VMSLogger.log(Level.INFO, "Start to parse SIP message body(SDP).");
		
		
		int aNum=0;
		int bNum=0;
		int eNum=0;
		int pNum=0;
		int tNum=0;
		int cNum=0;
		int mediaDescriptionNum=-1;
		
		while(!message.equals(""))
		{
			end=message.indexOf("=");
			if(end==-1){
				//Syntax Error
				//siputils.SetErrorNumber(400);
				return;
			}
			String SDPField=message.substring(start,end);
			message=message.substring(end+1);
			end=message.indexOf("\n");
			if(end==-1){
				//Syntax Error
				//siputils.SetErrorNumber(400);
				return;
			}
		
		
			if (SDPField.equals("v")){ //Session Description
				SMT="S";
			}else if (SDPField.equals("t")){ //Time Description
				SMT="T";
			}else if (SDPField.equals("m")){ //Media Description
				mediaDescriptionNum++;
				aNum=0;
				SMT="M";
			}
					
			if(SMT.equals("S")) // s:Session Description
			{
				if(SDPField.equals("v"))
					s.sdp.sessionDescription.setV(message.substring(start,end));
				else if(SDPField.equals("a"))
					s.sdp.sessionDescription.setA(message.substring(start,end),aNum++);					
				else if(SDPField.equals("b"))
					s.sdp.sessionDescription.setB(message.substring(start,end),bNum++);
				else if(SDPField.equals("c"))
					s.sdp.sessionDescription.setC(message.substring(start,end));
				else if(SDPField.equals("e"))
					s.sdp.sessionDescription.setE(message.substring(start,end),eNum++);
				else if(SDPField.equals("i"))
					s.sdp.sessionDescription.setI(message.substring(start,end));
				else if(SDPField.equals("k"))
					s.sdp.sessionDescription.setK(message.substring(start,end));
				else if(SDPField.equals("o"))
					s.sdp.sessionDescription.setO(message.substring(start,end));
				else if(SDPField.equals("p"))
					s.sdp.sessionDescription.setP(message.substring(start,end),pNum++);
				else if(SDPField.equals("s"))
					s.sdp.sessionDescription.setS(message.substring(start,end));
				else if(SDPField.equals("u"))
					s.sdp.sessionDescription.setU(message.substring(start,end));
				else if(SDPField.equals("z"))
					s.sdp.sessionDescription.setZ(message.substring(start,end));
				else{
					
					//SDP Parameters Not Understood /307
					//siputils.SetErrorNumber(307);
					return ;
				}
			
			}
			else if (SMT.equals("M"))   //M:Media Description
			{
				if (SDPField.equals("m"))			
					s.sdp.getMediaDescription().setM(message.substring(start,end));					
				else if (SDPField.equals("a"))
					s.sdp.getMediaDescription().setA(message.substring(start,end),aNum++);
				else if (SDPField.equals("b"))
					s.sdp.getMediaDescription().setB(message.substring(start,end));
				else if (SDPField.equals("c"))
					s.sdp.getMediaDescription().setC(message.substring(start,end),cNum++);
				else if (SDPField.equals("i"))
					s.sdp.getMediaDescription().setI(message.substring(start,end));
				else if (SDPField.equals("k"))
					s.sdp.getMediaDescription().setK(message.substring(start,end));
				else{
					
					// Generaate Response
					//SDP Parameters Not Understood /307
					//siputils.SetErrorNumber(307);
					return;
				}
			
			
			}
			else if(SMT.equals("T"))   //T:Time Description
			{
				if (SDPField.equals("t"))
					s.sdp.timeDescription.setT(message.substring(start,end),tNum++);
				else if (SDPField.equals("r"))
					s.sdp.timeDescription.setR(message.substring(start,end));
				else
				{
					//	Generaate Response
					//	SDP Parameters Not Understood /307
					//siputils.SetErrorNumber(307);
					return;
				}
			
			}
			else
			{
				// Generaate Response
				//	SDP Parameters Not Understood /307
				//siputils.SetErrorNumber(307);
				return;
			}
			message=message.substring(end+1);
		}
		VMSLogger.log(Level.INFO, "SIP message body is parsed.");		
}

}
