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

import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod

/**
 * Category methods for the QDox library to simplify the writing assertions to check the generated stubs.
 */
class QDoxCategory {

    /**
     * Given <code>JavaClass[]</code> array,
     * like provided by the <code>getClasses()</code> method (or <code>classes</code> property) in <code>StubTestCase</code>,
     * you can access a specific class by its fully qualified name, using the subscript notation:
     *
     * <pre><code>
     * classes['com.bar.Rectangle']
     * </code></pre>
     *
     * @param self an array of <code>JavaClass[]</code>
     * @param className the name of the class to find
     * @return a <code>JavaClass</code>
     */
    static JavaClass getAt(JavaClass[] self, String className) {
        def clazz = self.find { JavaClass jc -> jc.fullyQualifiedName == className }
        assert clazz, "No stub class found for name $className, among ${self.collect { it.fullyQualifiedName }}"
        return clazz
    }

    /**
     * Find one or several methods from a <code>JavaMethod[]</code> array.
     *
     * Usage:
     *
     * <pre><code>
     * classes['com.bar.Rectangle'].methods['area']
     * </code></pre>
     *
     * @param self an array of <code>JavaMethod</code>
     * @param methodName the name of the method(s) to find
     * @return a single <code>JavaMethod</code> if only one of that name exists, or a <code>JavaMethod[]</code> array
     */
    static getAt(JavaMethod[] self, String methodName) {
        def methods = self.findAll { JavaMethod jc -> jc.name == methodName }
        if (methods.size() == 1)
            return methods[0]
        else
            return methods
    }

    /**
     * Get a normalized string of the signature of a method, including its modifiers.
     * <br><br>
     * Usage:
     *
     * <pre><code>
     * // an area() method
     * assert classes['Rectangle'].methods['area'].signature == "public double area()"
     * // a constructor
     * assert classes['Rectangle'].methods['Rectangle'].signature == "public Rectangle(double x, double y)"
     * </code></pre>
     *
     * @param self a method
     * @return a normalized strings representing the signature
     */
    static String getSignature(JavaMethod self) {
        self.getDeclarationSignature(true)
    }

    /**
     * Lists the interfaces implemented by a class.
     * <br><br>
     * Usage:
     *
     * <pre><code>
     *  assert classes['Rectangle'].interfaces == ['groovy.lang.GroovyObject', 'Shape']
     * </code></pre>
     *
     * @param self a <code>JavaClass</code>
     * @return a list of fully qualified interface names
     */
    static List<String> getInterfaces(JavaClass self) {
        self.implementedInterfaces.collect { JavaClass jc -> jc.fullyQualifiedName }
    }

    /**
     * Get the name of the base class of that class.
     * <br><br>
     * Usage:
     *
     * <pre><code>
     *  assert classes['Rectangle'].baseClass == 'java.lang.Object'
     * </code></pre>
     *
     * @param self the class for which we want to know the base class
     * @return the fully qualified name of the parent class
     */
    static String getBaseClass(JavaClass self) {
        self.superJavaClass.fullyQualifiedName
    }

    /**
     * Shortcut to get the imports from the source associated with the class.
     * <br><br>
     * Usage:
     *
     * <pre><code>
     *  assert classes['Rectangle'].imports == ['java.lang.*', 'java.io.*',
     *      'java.net.*', 'java.util.*',
     *      'groovy.lang.*', 'groovy.util.*']
     * </code></pre>
     *
     * @param self the class
     * @return the list of fully qualified imports
     */
    static List<String> getImports(JavaClass self) {
        self.source.imports.toList()
    }
}
