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
 * This exception is thrown if an attempt is made to set a read only property
 */
public class ReadOnlyPropertyException extends MissingPropertyException {

    private static final long serialVersionUID = -1800912081930896077L;

    public ReadOnlyPropertyException(final String property, final Class type) {
        super("Cannot set readonly property: " + property + " for class: " + type.getName(), property, type);
    }

    public ReadOnlyPropertyException(final String property, final String classname) {
        super("Cannot set readonly property: " + property + " for class: " + classname);
    }
}
