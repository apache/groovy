package groovy.swt.convertor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Point;

/**
 *  A Converter that turns a List in the form [x, y] into a Point object
 * 
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster</a>
 * $Id$
 */
public class PointConverter {
    private Logger log = Logger.getLogger(getClass().getName());
    
    private static final PointConverter instance = new PointConverter();

    public static PointConverter getInstance() {
        return instance;
    }

    public Point parse(List list) {
        if (list.size() != 2) {
            log.log(Level.WARNING, "size attribute must [x,y]");
            return null;	
        }
        int x = parseNumber("" + list.get(0));
        int y = parseNumber("" + list.get(1));
        return new Point(x, y);
    }

    protected int parseNumber(String text) {
        return Integer.parseInt(text.trim());
    }
}