
package ir.iranet.vms.sdp;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 11:57:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class SessionDescription
{   
     String v;  // protocol version   
     String o;  //owner/creator and session identifier
     String s;  //session name
     String i;  //Session information    (Optional)
     String u;  //URI of description	(Optional)
     ArrayList e=new ArrayList();  //email address		(Optional)
     ArrayList p=new ArrayList();  //phone number		(Optional)
     String c;  //connection information - not required if included in all media	(Optional)
     ArrayList b=new ArrayList();  //bandwidth information	(Optional)
     String z;  //time zone adjustments	(Optional)
     String k;  //encryption key	(Optional)
     ArrayList a=new ArrayList();   //zero or more session attribute lines	(Optional)
    
 
    public SessionDescription() {
		// TODO Auto-generated constructor stub
	}

	public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getI() {
        return i;
    }

    public void setI(String i) {
        this.i = i;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public ArrayList getE() {
        return e;
    }
    public String getE(int i) {
        return (String) e.get(i);
    }

    public void setE(ArrayList e) {
        this.e = e;
    }
    public void setE(String e,int i) {
        this.e.add(i,e);
    }

    public ArrayList getP() {
        return p;
    }
    public String getP(int i) {
        return (String) p.get(i);
    }
    public void setP(ArrayList p) {
        this.p = p;
    }
    public void setP(String p,int i) {
        this.p.add(i,p);
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public ArrayList getB() {
        return b;
    }
    public String getB(int i) {
        return (String) b.get(i);
    }
    public void setB(ArrayList b) {
        this.b=b;
    }

    public void setB(String b,int i) {
        this.b.add(i,b);
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
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
    public String getA(int i) {
        return (String) a.get(i);
    }

    public void setA(ArrayList a) {
        this.a = a;
    }
    public void setA(String a,int i) {
        this.a.add(i,a);
    }

}
