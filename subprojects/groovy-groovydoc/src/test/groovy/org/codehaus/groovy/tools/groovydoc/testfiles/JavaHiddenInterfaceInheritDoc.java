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
package org.codehaus.groovy.tools.groovydoc.testfiles;

import groovy.transform.Internal;

public interface JavaHiddenInterfaceInheritDoc {
    /**
     * Invokes the given method.
     *
     * @param name the name of the method to call
     * @param args the arguments to use for the method call
     * @return the result of invoking the method
     */
    @Internal
    default Object invokeMethod(String name, Object args) {
        return null;
    }

    /**
     * Retrieves a property value.
     *
     * @param propertyName the name of the property of interest
     * @return the property value
     */
    @Internal
    default Object getProperty(String propertyName) {
        return null;
    }

    /**
     * Sets the given property to the new value.
     *
     * @param propertyName the name of the property of interest
     * @param newValue the new value for the property
     */
    @Internal
    default void setProperty(String propertyName, Object newValue) {
    }
}
