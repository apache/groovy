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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.test.GroovyTestCase

class Groovy6670Bug extends GroovyTestCase {
    void testCircularClassNodeReference() {
        // NOTE: The bug only seems to show up if we copy the following code into a file
        // and compile it from within an IDE
        assertScript '''import groovy.transform.CompileStatic

interface Transform<T> {
    T transform(T input)
}

class Holder<T> {
    final T thing

    Holder(T thing) {
        this.thing = thing
    }

    T transform(Transform<T> transform) {
        transform.transform(thing)
    }
}

@CompileStatic
class Container {
    static void m() {
        def holder = new Holder<Integer>(2)
        def transformed = holder.transform {
            it * 2
        }
        assert transformed == 4
    }
}
Container.m()
'''
    }
}
