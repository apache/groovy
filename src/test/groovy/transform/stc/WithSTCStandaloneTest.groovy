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

package groovy.transform.stc

/**
 * Unit tests for static type checking : with method.
 */
class WithSTCStandaloneTest extends GroovyTestCase {
    void testMethodAndPropertyAccessWithinNestedWithStatements() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Foo {
                String foo = 'foo'
                String foom() { 'foom' }
            }

            class Bar {
                String bar = 'bar'
                String barm() { 'barm' }
            }

            class Baz {
                String baz = 'baz'
                String bazm() { 'bazm' }
            }

            def other() { 'other' }

            @CompileStatic
            def main() {
                new Foo().with {
                    assert other() == 'other'
                    assert foom() == 'foom'
                    assert foo == 'foo'
                    new Bar().with {
                        assert foo == 'foo'
                        assert bar == 'bar'
                        assert barm() == 'barm'
                        assert other() == 'other'
                        assert foom() == 'foom'
                        new Baz().with {
                            assert foo == 'foo'
                            assert bar == 'bar'
                            assert baz == 'baz'
                            assert barm() == 'barm'
                            assert other() == 'other'
                            assert foom() == 'foom'
                            assert bazm() == 'bazm'
                        }
                    }
                }
            }

            main()
        '''
    }
}
