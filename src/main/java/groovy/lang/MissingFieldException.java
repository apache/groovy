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


/**
 * An exception occurred if a dynamic field dispatch fails with an unknown field.
 * 
 * Note that the Missing*Exception classes were named for consistency and
 * to avoid conflicts with JDK exceptions of the same name.
 */
public class MissingFieldException extends GroovyRuntimeException {

    private static final long serialVersionUID = -9209464582858098430L;
    private final String field;
    private final Class type;

    public MissingFieldException(String field, Class type) {
        super("No such field: " + field + " for class: " + type.getName());
        this.field = field;
        this.type = type;
    }

    public MissingFieldException(String field, Class type, Throwable e) {
        super("No such field: " + field + " for class: " + type.getName() + ". Reason: " + e, e);
        this.field = field;
        this.type = type;
    }

    public MissingFieldException(String message, String field, Class type) {
        super(message);
        this.field = field;
        this.type = type;
    }

    /**
     * @return the name of the field that could not be found
     */
    public String getField() {
        return field;
    }

    /**
     * 
     * @return The type on which the field was attempted to be called
     */
    public Class getType() {
        return type;
    }
}
