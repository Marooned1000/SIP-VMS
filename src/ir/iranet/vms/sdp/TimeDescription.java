package ir.iranet.vms.sdp;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 11:58:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TimeDescription
{    
     ArrayList t=new ArrayList(); //time the session is active
     String r; //zero or more repeat times	(Optional)
       
    public TimeDescription() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList getT() {
        return t;
    }
	public String getT(int i){
		return (String) t.get(i);
	}

    public void setT(ArrayList t) {
        this.t = t;
    }
    public void setT(String t,int i){
    	this.t.add(i,t);
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

}
