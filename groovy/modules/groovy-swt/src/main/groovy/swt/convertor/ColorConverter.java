package groovy.swt.convertor;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.RGB;

/**
 * A Converter that converts Strings in the form "#uuuuuu" or "x,y,z" into a
 * RGB object
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ColorConverter {
    private Logger log = Logger.getLogger(getClass().getName());
    private static final ColorConverter instance = new ColorConverter();
    private static String usageText = "Color value should be in the form of '#xxxxxx' or 'x,y,z'";

    public static ColorConverter getInstance() {
        return instance;
    }

    /**
     * Parsers a String in the form "x, y, z" into an SWT RGB class
     * 
     * @param value
     * @return RGB
     */
    protected RGB parseRGB(String value) {
        StringTokenizer enum = new StringTokenizer(value, ",");
        int red = 0;
        int green = 0;
        int blue = 0;
        if (enum.hasMoreTokens()) {
            red = parseNumber(enum.nextToken());
        }
        if (enum.hasMoreTokens()) {
            green = parseNumber(enum.nextToken());
        }
        if (enum.hasMoreTokens()) {
            blue = parseNumber(enum.nextToken());
        }
        return new RGB(red, green, blue);
    }

    /**
     * Parsers a String in the form "#xxxxxx" into an SWT RGB class
     * 
     * @param value
     * @return RGB
     */
    protected RGB parseHtml(String value) {
        if (value.length() != 7) {
            throw new IllegalArgumentException(usageText);
        }
        int colorValue = 0;
        try {
            colorValue = Integer.parseInt(value.substring(1), 16);
            java.awt.Color swingColor = new java.awt.Color(colorValue);
            return new RGB(swingColor.getRed(), swingColor.getGreen(), swingColor.getBlue());
        }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException(value + "is not a valid Html color\n " + ex);
        }
    }

    /**
     * Parse a String
     */
    public RGB parse(String value) {
        if (value.length() <= 1) {
            throw new IllegalArgumentException(usageText);
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.charAt(0) == '#') {
            return parseHtml(value);
        }
        else if (value.indexOf(',') != -1) {
            return parseRGB(value);
        }
        else {
            throw new IllegalArgumentException(usageText);
        }
    }

    public RGB parse(List list) {
        if (list.size() != 3) {
            log.log(Level.WARNING, "color attribute must [x,y,z]");
            return null;
        }
        int red = parseNumber("" + list.get(0));
        int green = parseNumber("" + list.get(1));
        int blue = parseNumber("" + list.get(2));
        return new RGB(red, green, blue);
    }

    public Object convert(Class type, Object value) {
        Object answer = null;
        if (value != null) {
            String text = value.toString();
            answer = parse(text);
        }
        return answer;
    }

    protected int parseNumber(String text) {
        text = text.trim();
        return Integer.parseInt(text);
    }
}
