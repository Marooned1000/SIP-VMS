package ir.iranet.vms.dtmf;

import ir.iranet.vms.util.Utility;
import ir.iranet.vms.util.VMSLogger;

import java.util.logging.Level;

public class DTMFMessage{

	byte event;
	byte e_r_volume;
	byte duration[];
	
	//////////////////////////////////////////////////////
	public DTMFMessage() {			
		duration=new byte[2]; 
	}
	//////////////////////////////////////////////////////
	public byte[] getDuration() {
		return duration;
	}
	public void setDuration(byte[] duration) {
		this.duration = duration;
	}
	public void setDuration(byte duration,int i) {
		this.duration[i] = duration;
	}
	public byte getE_R_volume() {
		return e_r_volume;
	}
	
	public void setE_R_volume(byte  e_r_volume) {
		this.e_r_volume = e_r_volume;
	}
	public byte getEvent() {
		return event;
	}	
	public void setEvent(byte event) {
		this.event = event;
	}
	////////////////////////////////////////////////////////////////
	public byte[] tostring(DTMFMessage dtmfMessage){
		
		byte[] dtmfPacket=new byte[0];
		
		dtmfPacket=Utility.Append(dtmfPacket,dtmfMessage.getEvent());
		dtmfPacket=Utility.Append(dtmfPacket,dtmfMessage.getE_R_volume());
		dtmfPacket=Utility.Append(dtmfPacket,dtmfMessage.getDuration());
		
		return dtmfPacket;
	}
	///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	public DTMFMessage Assembler(byte[] packet){
		
		
		VMSLogger.log(Level.INFO,"Start to parse RTP packet");    	
    	int byteNumber=0;
    	DTMFMessage dtmfMessage=new DTMFMessage(); 
    	
    	dtmfMessage.setEvent(packet[byteNumber++]);
    	//M_PT
    	dtmfMessage.setE_R_volume(packet[byteNumber++]);
    	
    	//SequenceNumber++
    	byte temp[]=new byte[2];
    	temp[0]=packet[byteNumber++];
    	temp[1]=packet[byteNumber++];
    	dtmfMessage.setDuration(temp);
    	    	
		return dtmfMessage;
	}
	/////////////////////////////////////////////////////////
}
