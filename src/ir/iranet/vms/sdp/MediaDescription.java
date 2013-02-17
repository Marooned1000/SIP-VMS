package ir.iranet.vms.sdp;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 11:57:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class MediaDescription
{    
    String m; //media name and transport address
    String i; //media title     (Optional)
    ArrayList c=new ArrayList(); //connection information - optional if included at session-level (Optional)
    String b; //bandwidth information	(Optional)
    String k; //encryption key	(Optional)
    ArrayList a=new ArrayList(); // media attribute lines	(Optional)
        
    public MediaDescription() {
		// TODO Auto-generated constructor stub
	}

	public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public String getI() {
        return i;
    }

    public void setI(String i) {
        this.i = i;
    }

    public ArrayList getC() {
        return c;
    }
    public String getC(int i) {
        return (String) c.get(i);
    }

    public void setC(ArrayList c) {
        this.c=c;
    }
    public void setC(String c,int i) {
        this.c.add(i,c);
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public ArrayList getA() {
        return a;
    }
    public String getA(int i){
    	return (String) a.get(i);
    }

    public void setA(ArrayList a) {
        this.a = a;
    }
    
    public void setA(String a,int i){
    	this.a.add(i,a);
    }
}
