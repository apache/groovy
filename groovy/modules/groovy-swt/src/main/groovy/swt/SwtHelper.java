package groovy.swt;

import java.lang.reflect.Field;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyException;

/** 
 * A helper class for working with SWT.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class SwtHelper  {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SwtHelper.class);
    

    /**
     * Parses the comma delimited String of style codes which are or'd
     * together. The given class describes the integer static constants
     *
     * @param constantClass is the type to look for static fields
     * @param text is a comma delimited text value such as "border, resize"
     * @return the int code
     */
    public static int parseStyle(Class constantClass, String text) throws GroovyException {
        return parseStyle(constantClass, text, true);
    }

    /**
     * Parses the comma delimited String of style codes which are or'd
     * together. The given class describes the integer static constants
     *
     * @param constantClass is the type to look for static fields
     * @param text is a comma delimited text value such as "border, resize"
     * @param toUpperCase is whether the text should be converted to upper case
     * before its compared against the reflection fields
     * 
     * @return the int code
     */
    public static int parseStyle(Class constantClass, String text, boolean toUpperCase) throws GroovyException{
        int answer = 0;
        if (text != null) {
            if (toUpperCase) {
                text = text.toUpperCase();
            }
            StringTokenizer enum = new StringTokenizer(text, ",");
            while (enum.hasMoreTokens()) {
                String token = enum.nextToken().trim();
                answer |= getStyleCode(constantClass, token);
            }
        }
        return answer;
    }
    
    /**
     * @return the code for the given word or zero if the word doesn't match a
     * valid style
     */
    public static int getStyleCode(Class constantClass,String text) throws GroovyException {
        try {
            Field field = constantClass.getField(text);
            if (field == null) {
                log.warn( "Unknown style code: " + text +" will be ignored");
                return 0;
            }
            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            throw new GroovyException("The value: " + text + " is not understood ");
        } catch (IllegalAccessException e) {
            throw new GroovyException("The value: " + text + " is not understood");
        }
    }
}
