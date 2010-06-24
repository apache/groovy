/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.typehandling;

public class GroovyCastException extends ClassCastException {

    /**
     * @param objectToCast object we tried to cast
     * @param classToCastTo class we tried to cast to
     * @param cause not kept but we pass on message from this Exception if any
     */
    public GroovyCastException(Object objectToCast, Class classToCastTo, Exception cause) {
        super(makeMessage(objectToCast,classToCastTo) + " due to: " +
                cause.getClass().getName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage()));
    }

    /**
     * @param objectToCast object we tried to cast
     * @param classToCastTo class we tried to cast to
     */
    public GroovyCastException(Object objectToCast, Class classToCastTo) {
        super(makeMessage(objectToCast,classToCastTo));
    }

    /**
     * @param message custom Exception message
     */
    public GroovyCastException(String message) {
        super(message);
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
