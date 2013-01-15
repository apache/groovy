/*
 * Copyright 2003-2013 the original author or authors.
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

package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * Test case for {@link groovy.transform.CompileDynamic}.
 */
@Mixin(StaticCompilationTestSupport)

class CompileDynamicTest extends StaticTypeCheckingTestCase {
    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testCompileDynamic() {
        assertScript '''import groovy.transform.CompileDynamic
            class Foo {
                @CompileDynamic
                void skipped() {
                    int i = 'should not pass'
                }
            }
            new Foo()
        '''
    }
}
