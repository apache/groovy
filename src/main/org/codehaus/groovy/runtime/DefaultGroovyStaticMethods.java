package org.codehaus.groovy.runtime;

/**
 * This class defines all the new static groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the
 * first parameter as the destination class.
 *
 * @author Guillaume Laforge
 * @version $Revision$
 */
public class DefaultGroovyStaticMethods {

    public static void hello(String stringClass, String msg)
    {
        System.out.println("Hello " + msg);
    }
}
