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
package org.codehaus.groovy.groovydoc;

public interface GroovyType {

    boolean isPrimitive();

    /**
     * The qualified name of this type excluding any dimension information.
     * For example, a two dimensional array of String returns "<code>java.lang.String</code>".
     */
    String qualifiedTypeName();

    /**
     * The unqualified name of this type excluding any dimension or nesting information.
     * For example, the class <code>Outer.Inner</code> returns "<code>Inner</code>".
     */
    String simpleTypeName();

    /**
     * The unqualified name of this type excluding any dimension information.
     * For example, a two dimensional array of String returns "<code>String</code>".
     */
    String typeName();

    /**
     * The qualified name including any dimension information.
     * For example, a two dimensional array of String returns
     * "<code>java.lang.String[][]</code>", and the parameterized type
     * <code>List&lt;Integer&gt;</code> returns "<code>java.util.List&lt;java.lang.Integer&gt;</code>".
     */
    @Override
    String toString();
}
