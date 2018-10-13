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
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MethodRankHelper;

/**
 * An exception occurred if a dynamic method dispatch fails with an unknown method.
 * <p>
 * Note that the Missing*Exception classes were named for consistency and
 * to avoid conflicts with JDK exceptions of the same name.
 */
public class MissingMethodException extends GroovyRuntimeException {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final long serialVersionUID = -6676430495683939401L;
    private final String method;
    private final Class type;
    private final boolean isStatic;

    public Object[] getArguments() {
        return arguments;
    }

    private final Object arguments[];

    public MissingMethodException(String method, Class type, Object[] arguments) {
        this(method, type, arguments, false);
    }

    public MissingMethodException(String method, Class type, Object[] arguments, boolean isStatic) {
        super();
        this.method = method;
        this.type = type;
        this.isStatic = isStatic;
        this.arguments = arguments == null ? EMPTY_OBJECT_ARRAY : arguments;
    }

    public String getMessage() {
         String s = "No signature of method: ";
         if(isStatic) {
             s += "static ";
         }
         if( type != null  ) {
             s +=  type.getName() + ".";
         }
         s += method
                + "() is applicable for argument types: ("
                + InvokerHelper.toTypeString(arguments, 60)
                + ") values: "
                + InvokerHelper.toArrayString(arguments, 60, true);
         if( type != null  ) {
             s += MethodRankHelper.getMethodSuggestionString(method, type, arguments) ;
         }
         return s;
    }

    /**
     * @return the name of the method that could not be found
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return The type on which the method was attempted to be called
     */
    public Class getType() {
        return type;
    }

    /**
     * @return Whether the method was called in a static way,
     *         i.e. on a class rather than an object.
     */
    public boolean isStatic() {
        return isStatic;
    }
}
