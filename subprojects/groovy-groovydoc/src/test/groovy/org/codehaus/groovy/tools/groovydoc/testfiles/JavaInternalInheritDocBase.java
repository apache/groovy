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

public class JavaInternalInheritDocBase {
    /**
     * Resolves a constructor by parameter types.
     *
     * @param arguments the constructor parameter types
     * @return the matching constructor, or {@code null} if none matches
     */
    public Object retrieveConstructor(Class[] arguments) {
        return null;
    }

    /**
     * This is a helper method which is used only by indy. It is for internal use.
     *
     * @param arguments the runtime arguments
     * @return the selected constructor helper
     */
    @Internal
    public Object retrieveConstructor(Object[] arguments) {
        return null;
    }
}
