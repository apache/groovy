package groovy.json;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * Created by Richard on 2/1/14.
 */
public interface JsonParser {



    Map<String, Object> parseMap( String value );
    Map<String, Object> parseMap( char [] value );
    Map<String, Object> parseMap( byte[] value );
    Map<String, Object> parseMap( byte[] value, String charset );
    Map<String, Object> parseMap( InputStream value, String charset );
    Map<String, Object> parseMap( CharSequence value );
    Map<String, Object> parseMap( InputStream value );
    Map<String, Object> parseMap( Reader value );
    Map<String, Object> parseMap(  File file, String charset );

    Object parse(  String jsonString );
    Object parse(  byte[] bytes );
    Object parse(  byte[] bytes, String charset );
    Object parse(  CharSequence charSequence );
    Object parse(  char[] chars );
    Object parse(  Reader reader );
    Object parse(  InputStream input );
    Object parse(  InputStream input, String charset );
    Object parse(  File file, String charset);
}
