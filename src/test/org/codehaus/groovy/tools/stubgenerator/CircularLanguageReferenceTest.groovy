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

/**
 * Test circular reference between Java and Groovy, as well as inheritance:
 * a Shape Java interface is implemented by a Rectangle Groovy class,
 * which is then extended by a Square Java class.
 *
 * The test below looks at the characteristics of the stub generated in Java for the Rectangle class.
 */
class CircularLanguageReferenceTest extends StubTestCase {

    void verifyStubs() {
        classes['stubgenerator.circularLanguageReference.Rectangle'].with {
            assert methods['area'     ].signature == "public double area()"
            assert methods['Rectangle'].signature == "public Rectangle(double x, double y)"
            assert !annotations
            assert interfaces.toSet() == ['groovy.lang.GroovyObject', 'stubgenerator.circularLanguageReference.Shape'] as Set
            assert baseClass == 'java.lang.Object'
            assert imports == ['java.lang.*', 'java.io.*', 'java.net.*', 'java.util.*', 'groovy.lang.*', 'groovy.util.*']
        }
    }
}

