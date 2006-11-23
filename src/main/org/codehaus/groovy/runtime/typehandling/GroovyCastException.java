/*
 * GroovyCastException.java created on 21.11.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.codehaus.groovy.runtime.typehandling;

public class GroovyCastException extends ClassCastException {

    public GroovyCastException(Object objectToCast, Class classToCastTo) {
        super(makeMessage(objectToCast,classToCastTo));
    }

    public GroovyCastException(String string) {
        super(string);
    }

    private static String makeMessage(Object objectToCast, Class classToCastTo) {
       String classToCastFrom;
       if (objectToCast!=null) {
           classToCastFrom = objectToCast.getClass().getName();
       } else {
           objectToCast = "null";
           classToCastFrom = "null";
       }
       return "Cannot cast object '" + objectToCast + "' " +
              "with class '" + classToCastFrom + "' " +
              "to class '" + classToCastTo.getName() + "'";
    }

}
