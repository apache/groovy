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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

import static groovy.test.GroovyAssert.isAtLeastJdk

class Groovy10565 extends AbstractBytecodeTestCase {

    void testPermittedSubclassName() {
        if (!isAtLeastJdk('17.0')) return
        def bytecode= compile('''
            package example
            sealed class Foo permits Bar { }
            class Bar extends Foo { }
        ''')
        assert bytecode.hasSequence(['PERMITTEDSUBCLASS example/Bar'])
    }
}
