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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy9293 {

    private final GroovyShell shell = GroovyShell.withConfig {
        ast(groovy.transform.CompileStatic)
        imports {
            normal 'groovy.transform.PackageScope'
        }
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    "".with { superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-11356
    void 'test accessing a package-private super class field inside a closure - diff package'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    "".with { superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package, it qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    with { it.superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-9293
    void 'test accessing a package-private super class field inside a closure - diff package, it qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    with { it.superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package, this qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    "".with { this.superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-9293
    void 'test accessing a package-private super class field inside a closure - diff package, this qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    "".with { this.superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package, owner qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    "".with { owner.superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-9293
    void 'test accessing a package-private super class field inside a closure - diff package, owner qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    "".with { owner.superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package, delegate qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    with { delegate.superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-9293
    void 'test accessing a package-private super class field inside a closure - diff package, delegate qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    with { delegate.superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }

    @Test
    void 'test accessing a package-private super class field inside a closure - same package, thisObject qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField = 'works'
            }

            class B extends A {
                def test() {
                    "".with { thisObject.superField }
                }
            }

            assert new B().test() == "works"
        '''
    }

    @Test // GROOVY-9293
    void 'test accessing a package-private super class field inside a closure - diff package, thisObject qualifier'() {
        assertScript shell, '''
            package a

            class A {
                @PackageScope String superField
            }

            assert true
        '''
        def err = shouldFail shell, '''
            package b

            class B extends a.A {
                def test() {
                    "".with { thisObject.superField }
                }
            }

            new B().test()
        '''
        assert err =~ /No such property: superField for class: b.B/
    }
}
