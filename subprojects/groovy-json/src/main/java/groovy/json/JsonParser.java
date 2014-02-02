package groovy.json;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * Created by Richard on 2/1/14.
 */
public interface JsonParser {

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
