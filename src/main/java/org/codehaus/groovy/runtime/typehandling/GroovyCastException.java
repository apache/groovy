/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.typehandling;

public class GroovyCastException extends ClassCastException {

    private static final long serialVersionUID = 6859089155641797356L;

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
       Object msgObject = objectToCast;
       if (objectToCast!=null) {
           classToCastFrom = objectToCast.getClass().getName();
       } else {
           msgObject = "null";
           classToCastFrom = "null";
       }
       String msg = 
               "Cannot cast object '" + msgObject + "' " +
               "with class '" + classToCastFrom + "' " +
               "to class '" + classToCastTo.getName() + "'";

       if (objectToCast==null){
           msg += getWrapper(classToCastTo);
       }

       return msg;
    }

    private static String getWrapper(Class cls) {
        Class ncls = cls;
        if (cls==byte.class)        {ncls=Byte.class;}
        else if (cls==short.class)  {ncls=Short.class;}
        else if (cls==char.class)   {ncls=Character.class;}
        else if (cls==int.class)    {ncls=Integer.class;}
        else if (cls==long.class)   {ncls=Long.class;}
        else if (cls==float.class)  {ncls=Float.class;}
        else if (cls==double.class) {ncls=Double.class;}
        if (cls!=null && ncls!=cls) {
            String msg = ". Try '" + ncls.getName() + "' instead";
            return msg;
        }
        return "";
    }

}
