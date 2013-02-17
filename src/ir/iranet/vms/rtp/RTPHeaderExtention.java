package ir.iranet.vms.rtp;

/**
 * Created by IntelliJ IDEA.
 * User: VoIP
 * Date: May 21, 2006
 * Time: 11:10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTPHeaderExtention
{
    /** An extension mechanism is provided to allow individual
     *  implementations to experiment with new payload-format-independent
     *  functions that require additional information to be carried in the
     *  RTP data packet header.  This mechanism is designed so that the
     *  header extension may be ignored by other interoperating
     *  implementations that have not been extended.
     */

    public long DefinedByProfile;
    public long lenght;
    /** lenght :16-bit
     *  counts the number of 32-bit words in the extension, excluding the
     *  four-octet extension header (therefore zero is a valid length).
     */
    public long Exeption;

    public RTPHeaderExtention(long definedByProfile, long exeption, long lenght) {
        DefinedByProfile = definedByProfile;
        Exeption = exeption;
        this.lenght = lenght;
    }

    public RTPHeaderExtention() {
		// TODO Auto-generated constructor stub
	}

	public long getDefinedByProfile() {
        return DefinedByProfile;
    }

    public void setDefinedByProfile(long definedByProfile) {
        DefinedByProfile = definedByProfile;
    }

    public long getLenght() {
        return lenght;
    }

    public void setLenght(long lenght) {
        this.lenght = lenght;
    }

    public long getExeption() {
        return Exeption;
    }

    public void setExeption(long exeption) {
        Exeption = exeption;
    }


}
