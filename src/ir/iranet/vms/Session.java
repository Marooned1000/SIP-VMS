package ir.iranet.vms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 28, 2006
 * Time: 11:03:35 AM
 * To change this template use File | Settings | File Templates.
 */

public class Session {

    /**
     *   Payload type for this session.
     *
     */
    public byte PayloadType;
    public long sequence_number;
    public long timestamp;
    public long SSRC;
    Integer rtpPort=null; // The destination port to send RTP packet 
	InetAddress DestAddress=null; // The destination address to send RTP packet    
    public long sequence_number_Base;
    Random rnd = new Random();
    
    static Random RandomNumGenerator = new Random();

    /**
     *   Random Offset -32 bit
     */
    public static final short RandomOffset = (short) Math.abs ( RandomNumGenerator.nextInt() & 0x000000FF) ;


    long PacketCount;

    /**
     *   Total Number of payload octets (i.e not including header or padding)
     *   sent out by this source since starting transmission.
     *
     */
    long OctetCount;


    public static long CurrentTime()
    {
        return (long) (new Date()).getTime();
    }


    public Session() throws UnknownHostException
    {    	
       	sequence_number =(long)( Math.abs(rnd.nextInt()) & 0x000000FF); 
       	timestamp = (short) Math.abs ( RandomNumGenerator.nextInt() & 0x000000FF)+Session.CurrentTime(); ;
    	SSRC = (long) Math.abs( rnd.nextInt() ) ; 
    }

	public InetAddress getDestAddress() {
		return DestAddress;
	}


	public void setDestAddress(InetAddress destAddress) {
		DestAddress = destAddress;
	}


	public Integer getRtpPort() {
		return rtpPort;
	}


	public void setRtpPort(Integer rtpPort) {
		this.rtpPort = rtpPort;
	}
	
}
