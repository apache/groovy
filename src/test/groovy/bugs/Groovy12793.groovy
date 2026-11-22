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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

/**
 * A statically compiled call to a method that returns or takes an array of a bounded type variable
 * must use the bound's erasure in the method descriptor, whether the method's class is read from
 * its class file or through reflection (GROOVY-12793).
 */
class Groovy12793 {

    protected Map<String, Boolean> getOptions() {
        ['asmResolving': Boolean.TRUE]
    }

    @Test
    void testJdkMethodReturningArrayOfBoundedTypeVariable() {
        assertStaticScript '''
            Class<?> type = Annotated
            Deprecated[] annotations = type.getAnnotationsByType(Deprecated)
            assert annotations.length == 1
            assert type.getDeclaredAnnotationsByType(Deprecated).length == 1
            assert Annotated.getAnnotationsByType(Deprecated)[0] instanceof Deprecated
        '''
    }

    @Test
    void testMethodReturningArrayOfBoundedTypeVariable() {
        assertStaticScript '''
            Integer[] values = Groovy12793Support.array(Integer)
            assert values.length == 2
        '''
    }

    @Test
    void testMethodReturningTwoDimensionalArrayOfBoundedTypeVariable() {
        assertStaticScript '''
            Integer[][] grid = Groovy12793Support.grid(Integer)
            assert grid.length == 2
            assert grid[0].length == 3
        '''
    }

    @Test
    void testMethodTakingArrayOfBoundedTypeVariable() {
        assertStaticScript '''
            assert Groovy12793Support.count(1, 2, 3) == 3
            assert Groovy12793Support.count(new Integer[] {1, 2}) == 2
        '''
    }

    @Test
    void testMethodTakingTwoDimensionalArrayOfBoundedTypeVariable() {
        assertStaticScript '''
            Integer[][] grid = [[1, 2], [3]]
            assert Groovy12793Support.cells(grid) == 3
        '''
    }

    private void assertStaticScript(String body) {
        def config = new CompilerConfiguration()
        config.optimizationOptions.putAll(getOptions())
        new GroovyShell(config).evaluate("""
            import org.codehaus.groovy.dummy.Groovy12793Support

            @Deprecated
            class Annotated {
            }

            @groovy.transform.CompileStatic
            void test() {
                $body
            }

            test()
        """)
    }
}

final class Groovy12793_AsmResolveOff extends Groovy12793 {
    @Override
    protected Map<String, Boolean> getOptions() {
        ['asmResolving': Boolean.FALSE]
    }
}
