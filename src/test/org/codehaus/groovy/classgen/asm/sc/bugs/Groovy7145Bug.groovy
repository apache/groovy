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

class Groovy7145Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testShouldHandlerNullSafeOperatorOnPublicField() {
        assertScript '''
class SafeNavigation {

    String name1
    public String name2

    public static void main(String[] args) {
        SafeNavigation test

        println test?.name1 // 'null'
        println test?.name2 // NullPointerException
    }
}
'''
    }

    void testShouldHandlerNullSafeOperatorOnPublicFieldOfPrimitiveType() {
        assertScript '''
class SafeNavigation {

    int name1
    public int name2

    public static void main(String[] args) {
        SafeNavigation test

        println test?.name1 // 'null'
        println test?.name2 // NullPointerException
    }
}
'''
    }
}
