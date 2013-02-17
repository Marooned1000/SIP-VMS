package ir.iranet.vms.rtp;

import ir.iranet.vms.util.Utility;
import ir.iranet.vms.Session;
import ir.iranet.vms.dtmf.DTMFMessage;
import ir.iranet.vms.util.VMSLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 10:13:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTPMessage
{

     byte VPXCC[] ;

    /** version (V): 2 bits
      This field identifies the version of RTP.  The version defined by
      this specification is two (2).  (The value 1 is used by the first
      draft version of RTP and the value 0 is used by the protocol
      initially implemented in the "vat" audio tool.
     */


    /** padding (P): 1 bit
      If the padding bit is set, the packet contains one or more
      additional padding octets at the end which are not part of the
      payload.  The last octet of the padding contains a count of how
      many padding octets should be ignored, including itself.  Padding
      may be needed by some encryption algorithms with fixed block sizes
      or for carrying several RTP packets in a lower-layer protocol data
      unit.
     */


    /** extension (X): 1 bit
      If the extension bit is set, the fixed header MUST be followed by
      exactly one header extension.
     */


    /** CSRC count (CC): 4 bits
      The CSRC count contains the number of CSRC identifiers that follow
      the fixed header.
     */

     byte     M_PT[];

    /** marker (M): 1 bit
      The interpretation of the marker is defined by a profile.  It is
      intended to allow significant events such as frame boundaries to
      be marked in the packet stream.  A profile MAY define additional
      marker bits or specify that there is no marker bit by changing the
      number of bits in the payload type field.
     */


    /** payload type (PT): 7 bits
      This field identifies the format of the RTP payload and determines
      its interpretation by the application.  A profile MAY specify a
      default static mapping of payload type codes to payload formats.
     */

    byte SequenceNumber[];

    /** sequence number: 16 bits
      The sequence number increments by one for each RTP data packet
      sent, and may be used by the receiver to detect packet loss and to
      restore packet sequence.  The initial value of the sequence number
      SHOULD be random (unpredictable).
     */

    byte TimeStamp[];

    /** timestamp: 32 bits
      The timestamp reflects the sampling instant of the first octet in
      the RTP data packet.
     */

    byte SSRC[] ;


    /** SSRC: 32 bits
      The SSRC field identifies the synchronization source.  This
      identifier SHOULD be chosen randomly, with the intent that no two
      synchronization sources within the same RTP session will have the
      same SSRC identifier.
     */

    byte CSRC[][];

    /** CSRC list: 0 to 15 items, 32 bits each
      The CSRC list identifies the contributing sources for the payload
      contained in this packet.  The number of identifiers is given by
      the CC field.
     */
    byte Data[];
    
    RTPHeaderExtention HeaderExtention;


    // Constructors
    public RTPMessage()
    {
    	VPXCC=new byte[1];
    	M_PT=new byte[1];
    	SequenceNumber=new byte[2];
    	TimeStamp=new byte[4];
    	SSRC=new byte[4];
    	CSRC= new byte[15][4];
    	Data=new byte[0];
    	HeaderExtention=new RTPHeaderExtention();
    }

    
    //Setters & Getters
    public byte[] getVPXCC() {
        return VPXCC;
    }

    public void setVPXCC(byte b) {
        VPXCC[0] =b;
    }

    public byte[] getM_PT() {
        return M_PT;
    }

    public void setM_PT(byte m_PT) {
        M_PT[0] = m_PT;
    }

    public byte[] getSequenceNumber() {
        return SequenceNumber;
    }

    public void setSequenceNumber(byte[] sequenceNumber) {
        SequenceNumber = sequenceNumber;
    }

    public byte[] getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(byte[] timeStamp) {
        TimeStamp = timeStamp;
    }

    public byte[] getSSRC() {
        return SSRC;
    }

    public void setSSRC(byte[] SSRC) {
        this.SSRC = SSRC;
    }

    public byte[][] getCSRC() {
        return CSRC;
    }

    public void setCSRC(byte[][] CSRC) {
        this.CSRC = CSRC;
    }
    public byte[] getData(){
    	return Data;
    }
    public void setData(byte[] data){
    	Data=data;
    }
    public void setData(byte data,int i){
    	Data[i]=data;
    }
   
    public RTPHeaderExtention getHeaderExtention() {
        return HeaderExtention;
    }

    public void setHeaderExtention(RTPHeaderExtention headerExtention) {
        HeaderExtention = headerExtention;
    }
    
    
    ///////////////////////////////////////////////////////////////
    // Make RTP Packet                                           //
    ///////////////////////////////////////////////////////////////
    public byte[] MakePacket(byte[] data,Session session)
    {
    	byte[] RTPPacket;
    	VMSLogger.log(Level.INFO, "Start to make RTP Packet.");
        RTPMessage RTPMessage=new RTPMessage();


        /**
        *   First Byte of header
        */

        byte vpxcc;
        vpxcc=(byte) (0x80);
        RTPMessage.setVPXCC(vpxcc);            // +-+-+-+-+-+-+-+-+
                                               // |V=2|P|X|   CC  |
                                               // +-+-+-+-+-+-+-+-+
                                               //  1 0 0 0 0 0 0 0 = 0x80


        /**
        *   Second Byte of header
        */
       
        byte M_PT;
        M_PT = (byte) (0x03); //| (Session.PayloadType));
        RTPMessage.setM_PT(M_PT);
                                               // +-+-+-+-+-+-+-+-+
                                               // |M|     PT      |
                                               // +-+-+-+-+-+-+-+-+
                                               //  0 0 0 0 0 0 1 1
        

        // sequence is 2 bytes      
        RTPMessage.setSequenceNumber(Utility.LongToBytes ( session.sequence_number, 2 ));
        session.sequence_number++;
        
        session.timestamp=session.timestamp + (long)15;
        long timestamp=session.timestamp;
        byte ts[] = new byte[4];         // timestamp is 4 bytes
        ts = Utility.LongToBytes (  timestamp, 4 );
        RTPMessage.setTimeStamp(ts);        
        
        byte cs[][] = new byte[15][4];
        
        RTPMessage.setCSRC(cs);
        byte ss[] = new byte[4];
        ss = Utility.LongToBytes ( session.SSRC, 4);
        RTPMessage.setSSRC(ss);
        
       
        ////////////////////////////////////////////////////////
        // Construct the header by appending all the above byte
        // arrays into RTPPacket
        ////////////////////////////////////////////////////////

        RTPPacket= new byte [0];

        // Append the compound version, Padding, Extension and CSRC Count bits
        RTPPacket  = Utility.Append ( RTPPacket, RTPMessage.getVPXCC() );

        // Append the compound Marker and payload type byte
        RTPPacket  = Utility.Append ( RTPPacket,  RTPMessage.getM_PT());

        // Append the 2 sequence number bytes
        RTPPacket  = Utility.Append ( RTPPacket, RTPMessage.getSequenceNumber() );

        // Append the 4 timestamp bytes
        RTPPacket  = Utility.Append ( RTPPacket, RTPMessage.getTimeStamp() );

        // Append the 4 SSRC bytes
        RTPPacket  = Utility.Append ( RTPPacket, RTPMessage.getSSRC());

        // Append the data packet after 12 byte header
        
        RTPPacket  = Utility.Append ( RTPPacket, data );  
        
      //  System.out.println(RTPPacket.toString());

        
        VMSLogger.log(Level.INFO, "A RTP Packet is made.");
        return RTPPacket;
    }   
    
    /////////////////////////////////////////////////////////////
    //RTP Packet Parser
    /////////////////////////////////////////////////////////////
    public RTPMessage Assembler(DatagramPacket packet)
    {
    	VMSLogger.log(Level.INFO,"Start to parse RTP packet");
    	
    	int byteNumber=0;
    	byte[] rtpPacket=new byte[45];    	
    	rtpPacket=packet.getData();
    	RTPMessage rtpMessage=new RTPMessage();
    	
    	// VPXCC
    	rtpMessage.setVPXCC(rtpPacket[byteNumber++]);
    	//M_PT
    	rtpMessage.setM_PT(rtpPacket[byteNumber++]);
    	
    	//SequenceNumber
    	byte temp[]=new byte[2];
    	temp[0]=rtpPacket[byteNumber++];
    	temp[1]=rtpPacket[byteNumber++];
    	rtpMessage.setSequenceNumber(temp);
    	
    	//TimeStamp
    	temp=new byte[4];
    	temp[0]=rtpPacket[byteNumber++];
    	temp[1]=rtpPacket[byteNumber++];
    	temp[2]=rtpPacket[byteNumber++];
    	temp[3]=rtpPacket[byteNumber++];
    	rtpMessage.setTimeStamp(temp);
    	
    	//SSRC
    	temp=new byte[4];
    	temp[0]=rtpPacket[byteNumber++];
    	temp[1]=rtpPacket[byteNumber++];
    	temp[2]=rtpPacket[byteNumber++];
    	temp[3]=rtpPacket[byteNumber++];
    	rtpMessage.setSSRC(temp);
    	
    	//CSRC    	     	
    	int cc=((int)rtpMessage.getVPXCC()[0])%16; //cc:CSRC Count
    	if(cc==-8)
    		cc=0;
    	/*static int ConvertBinaryToDecimal(int[] bin){
    		   int dec=0;
    		   for (int i=0; i<bin.length; i++) {
    		      dec ><<= 1; // shift content of dec 1 position to the left (actually a multiplication with 2)
    		      dec += bin[i]; // add the 0 or 1
    		   }
    		   return dec;
    		}
    		
    		*/


    	
    	int csrcNumber=0;
    	byte bb[][]=new byte[15][4];
    	while(cc!=0)
    	{    		
    		bb[csrcNumber][0]=rtpPacket[byteNumber++];
    		bb[csrcNumber][1]=rtpPacket[byteNumber++];
    		bb[csrcNumber][2]=rtpPacket[byteNumber++];
    		bb[csrcNumber][3]=rtpPacket[byteNumber++];
    		rtpMessage.setCSRC(bb);
    		cc--;
    	
    		csrcNumber++;
    	}
    	//temp=new byte[rtpPacket.length-byteNumber];
    	temp=new byte[33];
    	int dataByteNum=0;
    	while(dataByteNum< 33)
    	{
    		temp[dataByteNum++]=rtpPacket[byteNumber++];    		
    	}
    	rtpMessage.setData(temp);
  
    	
    	VMSLogger.log(Level.INFO,"RTP packet is parsed");
    	return rtpMessage;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    public void send(byte[] packet,InetAddress address,int port) throws IOException
	{
		
		DatagramSocket socket = new DatagramSocket();			
		DatagramPacket rtpPacket = new DatagramPacket(packet, packet.length, address, port);            		
		socket.send(rtpPacket);   
		VMSLogger.log(Level.INFO,"Sent to    address:"+address+"     port:"+port);
		
	}
    /////////////////////////////////////////////////////////////////////////////////////
    public void record(){
    	
    	
    }
    
}

