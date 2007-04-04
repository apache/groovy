package groovy.bugs;

import java.util.HashMap;

/**
 * Class to support the GROOVY-662 test.  There is a difference between improper uses of
 * properties between Groovy defined classes and Java defined classes.  There is no difference
 * between correct uses so this is not a problem just an anti-regression test.
 *
 * @author Russel Winder
 * @version $Revision$
 */
public class Groovy662_JavaClass extends HashMap {
    String myProperty = "Hello";

    public String getMyProperty() {
        return myProperty;
    }
}
