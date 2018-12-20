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

import org.codehaus.groovy.runtime.MethodRankHelper;

/**
 * An exception occurred if a dynamic property dispatch fails with an unknown property.
 * 
 * Note that the Missing*Exception classes were named for consistency and
 * to avoid conflicts with JDK exceptions of the same name.
 */
public class MissingPropertyException extends GroovyRuntimeException {

    public static final Object MPE = new Object();
    private static final long serialVersionUID = -1780027060966200019L;

    private final String property;
    private final Class type;

    public MissingPropertyException(String property, Class type) {
        this.property = property;
        this.type = type;
    }

    public MissingPropertyException(String property, Class type, Throwable t) {
        super(t);
        this.property = property;
        this.type = type;
    }

    public MissingPropertyException(String message) {
        super(message);
        this.property = null;
        this.type = null;
    }

    public MissingPropertyException(String message, String property, Class type) {
        super(message);
        this.property = property;
        this.type = type;
    }

    public String getMessageWithoutLocationText() {
        if (property == null || type == null) {
            return super.getMessageWithoutLocationText();
        }
        final Throwable cause = getCause();
        if (cause == null) {
            if (super.getMessageWithoutLocationText() != null) {
                return super.getMessageWithoutLocationText();
            }
            return "No such property: " + property + " for class: " + type.getName() +
                   MethodRankHelper.getPropertySuggestionString(property, type);
        }
        return "No such property: " + property + " for class: " + type.getName() + ". Reason: " + cause;
    }

    /**
     * @return the name of the property that could not be found
     */
    public String getProperty() {
        return property;
    }

    /**
     * 
     * @return The type on which the property was attempted to be called
     */
    public Class getType() {
        return type;
    }
}
