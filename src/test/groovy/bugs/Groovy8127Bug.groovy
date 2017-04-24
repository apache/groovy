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

import gls.CompilableTestSupport

class Groovy8127Bug extends CompilableTestSupport {
    void testTraitWithClosureReferencingField() {
        assertScript """
        trait BarTrait {
            String result = ''
            public final Runnable bar = { result = 'changeme' } as Runnable
            void doRun() { bar.run() }
        }

        class Bar implements BarTrait {}

        def b = new Bar()
        b.doRun()
        assert b.result == 'changeme'
        """
    }

    void testTraitWithCompileStaticAndCoercedClosure() {
        shouldCompile """
        @groovy.transform.CompileStatic
        trait FooTrait {
            public final Runnable foo = { println new Date() } as Runnable
            void doRun() { foo.run() }
        }

        class Foo implements FooTrait { }

        new Foo().doRun()
        """
    }
}
