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
package org.codehaus.groovy.tools.stubgenerator

import groovy.transform.CompileStatic

/**
 * Category methods that keep stub-generator assertions concise while using JavaParser-backed views.
 */
class JavaParserCategory {

    /**
     * Access a parsed class by fully qualified name.
     */
    static JavaSourceClass getAt(JavaSourceClass[] self, String className) {
        def clazz = self.find { JavaSourceClass jc -> jc.fullyQualifiedName == className }
        assert clazz, "No stub class found for name $className, among ${self.collect { it.fullyQualifiedName }}"
        clazz
    }

    /**
     * Access one or several parsed methods by name.
     */
    static getAt(JavaSourceMethod[] self, String methodName) {
        getAt(self.toList(), methodName)
    }

    static getAt(List<JavaSourceMethod> self, String methodName) {
        def methods = self.findAll { JavaSourceMethod method -> method.name == methodName }
        methods.size() == 1 ? methods[0] : methods
    }

    @CompileStatic
    static List<JavaSourceMethod> getMethods(JavaSourceClass self) {
        self.methods
    }
}
