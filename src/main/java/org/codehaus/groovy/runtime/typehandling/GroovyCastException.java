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

import java.io.Serial;
import java.util.Optional;

/**
 * Exception thrown when a type cast or coercion fails.
 * <p>
 * This exception provides detailed information about the object that failed to cast,
 * the target type, and any underlying cause. The error messages suggest alternative
 * wrapper types when casting to primitive wrapper classes.
 */
public class GroovyCastException extends ClassCastException {

    @Serial private static final long serialVersionUID = 6859089155641797356L;

    /**
     * Constructs a GroovyCastException with details of the failed cast and its cause.
     *
     * @param objectToCast the object that failed to cast
     * @param classToCastTo the target class
     * @param cause the underlying exception (message is included in the error)
     */
    public GroovyCastException(final Object objectToCast, final Class classToCastTo, final Exception cause) {
        super(makeMessage(objectToCast, classToCastTo) +
            " due to: " + cause.getClass().getName() + Optional.ofNullable(cause.getMessage()).map(msg -> ": " + msg).orElse(""));
    }

    /**
     * Constructs a GroovyCastException with details of the failed cast.
     *
     * @param objectToCast the object that failed to cast
     * @param classToCastTo the target class
     */
    public GroovyCastException(final Object objectToCast, final Class classToCastTo) {
        super(makeMessage(objectToCast, classToCastTo));
    }

    /**
     * Constructs a GroovyCastException with a custom error message.
     *
     * @param message the error message
     */
    public GroovyCastException(final String message) {
        super(message);
    }

    //--------------------------------------------------------------------------

    private static String makeMessage(final Object object, final Class<?> target) {
        if (object == null) {
            return "Cannot cast 'null' to class '" + target.getName() + "'" + getWrapper(target);
        }
        return "Cannot cast object '" + object + "' with class '" + object.getClass().getName() + "' to class '" + target.getName() + "'";
    }

    private static String getWrapper(final Class<?> cls) {
        Class<?> ncls = cls;
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
