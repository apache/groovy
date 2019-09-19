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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy3801Bug extends GroovyTestCase {
    void testMainMethodSignature() {
        def gcl = new GroovyClassLoader()
        def clazz

        clazz = gcl.parseClass( """
            class Groovy3801A {
                static main(args) {}
            }
        """, 'Groovy3801A.groovy'
        )
        def stdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert stdMainMethod.returnType.toString().contains('void')

        clazz = gcl.parseClass( """
            class Groovy3801B {
                static def main(args) {}
            }
        """, 'Groovy3801B.groovy'
        )
        stdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert stdMainMethod.returnType.toString().contains('void')

        clazz = gcl.parseClass( """
            class Groovy3801C {
                static main() {}
            }
        """, 'Groovy3801C.groovy'
        )
        def nonStdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert nonStdMainMethod.returnType.toString().contains('java.lang.Object')
    }
}
