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
package groovy.text.markup;

/**
 * An enumeration used internally to map include types to {@link groovy.text.markup.BaseTemplate} method calls.
 */
enum IncludeType {
    /**
     * Includes another Groovy template.
     */
    template("includeGroovy"),
    /**
     * Includes raw content after escaping it for markup output.
     */
    escaped("includeEscaped"),
    /**
     * Includes raw content without escaping it.
     */
    unescaped("includeUnescaped");

    private final String methodName;

    /**
     * Creates an include type that maps to the specified {@link BaseTemplate} helper method.
     *
     * @param methodName helper method used to realize the include
     */
    IncludeType(final String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the helper method name corresponding to this include type.
     *
     * @return the {@link BaseTemplate} method invoked for this include type
     */
    public String getMethodName() {
        return methodName;
    }
}
