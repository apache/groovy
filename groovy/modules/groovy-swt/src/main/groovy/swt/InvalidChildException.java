/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt;

import org.codehaus.groovy.GroovyException;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class InvalidChildException extends GroovyException {

    public InvalidChildException(String parent, String child) {
        super("first child of " + parent + " should be " + child);
    }

}
