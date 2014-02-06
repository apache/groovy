package groovy.json.internal;

/**
 * Created by Richard on 2/6/14.
 */
public class Byt {

    public static void charTo( byte[] b,  char val ) {
        b[  1 ] = ( byte ) ( val );
        b[ 0 ] = ( byte ) ( val >>> 8 );
    }


}
