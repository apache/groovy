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
package org.codehaus.groovy.transform.traitx

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * A sub-trait must be able to resolve a {@code static} method inherited from a
 * super-trait from its own body under {@code @CompileStatic}, including when an
 * argument's static type is a <em>proper subtype</em> of the declared parameter
 * type (GROOVY-12106).
 *
 * <p>The bug: {@code TraitTypeCheckingExtension} resolved the inherited super-trait
 * helper static by an <em>exact</em> parameter-type match, so only an argument whose
 * static type exactly matched the parameter resolved; a subtype argument failed with
 * {@code Cannot find matching method <Child>$Trait$Helper#m(java.lang.Class, ...)}.
 * Because the minimal report passed an exact-type argument, the issue was first closed
 * "Cannot Reproduce". Plain class inheritance never had this problem — the defect was
 * specific to trait static-helper dispatch.
 */
final class Groovy12106 {

    @Test
    void testUnqualifiedInheritedStaticWithSubtypeArg() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object delegate) { 'target=' + delegate.class.simpleName }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable<T> extends ExecutesClosures {
                String run(SimpleArgument arg) { withDelegate({ -> }, arg) }   // unqualified, subtype arg
            }
            class C implements Arguable<String> { }
            assert new C().run(new SimpleArgument()) == 'target=SimpleArgument'
        '''
    }

    @Test
    void testThisQualifiedInheritedStaticWithSubtypeArg() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object delegate) { 'target=' + delegate.class.simpleName }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(SimpleArgument arg) { this.withDelegate({ -> }, arg) }
            }
            class C implements Arguable { }
            assert new C().run(new SimpleArgument()) == 'target=SimpleArgument'
        '''
    }

    // On GROOVY_5_0_X: @NotYetImplemented — the qualified form resolves via the trait's
    // promoted interface static (GROOVY-12111), which is master-only; the unqualified and
    // this.-qualified forms above (the GROOVY-12106 fix) work here, but Parent.m(...) does not.
    @NotYetImplemented
    @Test
    void testQualifiedInheritedStaticWithSubtypeArg() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object delegate) { 'target=' + delegate.class.simpleName }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(SimpleArgument arg) { ExecutesClosures.withDelegate({ -> }, arg) }
            }
            class C implements Arguable { }
            assert new C().run(new SimpleArgument()) == 'target=SimpleArgument'
        '''
    }

    @Test
    void testExactTypeArgStillResolves() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object delegate) { 'target=' + delegate.class.simpleName }
            }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(Object arg) { withDelegate({ -> }, arg) }   // exact-type arg (the case that always worked)
            }
            class C implements Arguable { }
            assert new C().run('s') == 'target=String'
        '''
    }

    @Test
    void testOverloadResolutionAcrossTraitInheritance() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait P {
                static String pick(Object o) { 'object' }
                static String pick(CharSequence c) { 'charseq' }
            }
            @CompileStatic
            trait Q extends P {
                String viaCharSequence(String s) { pick(s) }   // String -> most specific CharSequence overload
                String viaObject(Integer i) { pick(i) }        // Integer -> Object overload
            }
            class C implements Q { }
            assert new C().viaCharSequence('x') == 'charseq'
            assert new C().viaObject(42) == 'object'
        '''
    }

    @Test
    void testThreeLevelTraitInheritance() {
        assertScript '''
            import groovy.transform.CompileStatic
            final class Sub { }
            @CompileStatic
            trait A { static String tag(Object o) { 'A:' + o.class.simpleName } }
            @CompileStatic
            trait B extends A { }
            @CompileStatic
            trait C extends B {
                String go(Sub s) { tag(s) }   // inherited from grandparent A, subtype arg
            }
            class Impl implements C { }
            assert new Impl().go(new Sub()) == 'A:Sub'
        '''
    }

    @Test
    void testGrailsHelperShapeWithDelegatesTo() {
        assertScript '''
            import groovy.transform.CompileStatic
            final class Field { String name = 'f' }
            @CompileStatic
            trait ExecutesClosures {
                static void withDelegate(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, genericTypeIndex=0) Closure callable, Object delegate) {
                    if (callable != null) { callable.delegate = delegate; callable.resolveStrategy = Closure.DELEGATE_ONLY; callable.call() }
                }
            }
            @CompileStatic
            trait Arguable<T> extends ExecutesClosures {
                String describe(Field f) {
                    def sb = new StringBuilder()
                    withDelegate({ -> sb.append('seen') }, f)   // f:Field is a subtype of Object
                    sb.toString()
                }
            }
            class C implements Arguable<String> { }
            assert new C().describe(new Field()) == 'seen'
        '''
    }
}
