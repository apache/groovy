package groovy.json.internal;

import static groovy.json.internal.Exceptions.die;

/**
 * Created by Richard on 2/6/14.
 */
public class ByteScanner {

    /**
     * Turns a single nibble into an ascii HEX digit.
     *
     * @param nibble the nibble to serializeObject.
     * @return the encoded nibble (1/2 byte).
     */
    protected static int encodeNibbleToHexAsciiCharByte( final int nibble ) {

        switch ( nibble ) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
                return nibble + 0x30; // 0x30('0') - 0x39('9')
            case 0x0A:
            case 0x0B:
            case 0x0C:
            case 0x0D:
            case 0x0E:
            case 0x0F:
                return nibble + 0x57; // 0x41('a') - 0x46('f')
            default:
                die( "illegal nibble: " + nibble );
                return -1;
        }
    }

    /**
     * Turn a single bytes into two hex character representation.
     *
     * @param decoded the byte to serializeObject.
     * @param encoded the array to which each encoded nibbles are now ascii hex representations.
     */
    public static void encodeByteIntoTwoAsciiCharBytes( final int decoded, final byte[] encoded ) {


        encoded[ 0 ] = ( byte ) encodeNibbleToHexAsciiCharByte( ( decoded >> 4 ) & 0x0F );
        encoded[ 1 ] = ( byte ) encodeNibbleToHexAsciiCharByte(  decoded & 0x0F  );
    }

}
