/*
 * Created on Jun 15, 2004
 *
 */
package groovy.swt;

import org.codehaus.groovy.GroovyException;


/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class UnKnownStyleException extends GroovyException {

    /**
     * @param message
     */
    public UnKnownStyleException(String style) {
        super("Unknow SWT style: " + style);
    }
}
