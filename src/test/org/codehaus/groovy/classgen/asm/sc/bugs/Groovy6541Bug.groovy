/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

@Mixin(StaticCompilationTestSupport)
class Groovy6541Bug extends StaticTypeCheckingTestCase {
    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testShouldCompileGetAtWithBoxedInteger() {
        assertScript '''class Foo {
    String getAt(int i) {
        "$i"
    }
}

def foo = new Foo()

for (i in 1..2) {
    assert foo[i] == "$i"
}
    '''
    }
    void testShouldCompileGetAtWithPrimitiveInteger() {
        assertScript '''class Foo {
    String getAt(Integer i) {
        "$i"
    }
}

def foo = new Foo()

for (i in 1..2) {
    assert foo[(int)i] == "$i"
}
    '''
    }

    void testShouldCompilePutAtWithBoxedInteger() {
        assertScript '''class Foo {
    String getAt(int i) {
        "$i"
    }

    void putAt(int i, String value) {
        assert value == "${i}"
    }
}

def foo = new Foo()

for (i in 1..2) {
    foo[i] = "$i"
}
    '''
    }
    void testShouldCompilePutAtWithPrimitiveInteger() {
        assertScript '''class Foo {
    String getAt(Integer i) {
        "$i"
    }

    void putAt(int i, String value) {
        assert value == "${i}"
    }
}

def foo = new Foo()

for (i in 1..2) {
    foo[(int)i] = "$i"
}
    '''
    }
}
