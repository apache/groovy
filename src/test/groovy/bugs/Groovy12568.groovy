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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * A dynamic {@code new} of a protected constructor of another package, in a
 * subclass, is selected through the meta-class. When the JVM refuses access to
 * the constructor, as for an encapsulated JDK class, the call fails with the
 * same Groovy exception with and without invokedynamic.
 */
final class Groovy12568 {

    private static GroovyShell shell(final boolean indy) {
        def config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.INVOKEDYNAMIC] = indy
        new GroovyShell(Groovy12568.classLoader, new Binding(), config)
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testProtectedConstructorOfEncapsulatedJdkClass(final boolean indy) {
        assumeFalse(ObjectInputStream.module.isOpen('java.io', Groovy12568.module),
            'the JVM opens java.base/java.io, so the constructor is reflectively reachable')

        def err = shouldFail(GroovyRuntimeException) {
            shell(indy).evaluate '''
                class C extends ObjectInputStream {
                    static ObjectInputStream make() {
                        new ObjectInputStream()
                    }
                }
                C.make()
            '''
        }
        // was, with invokedynamic: GroovyBugError "BUG! UNCAUGHT EXCEPTION: member is protected"
        assert err.class == GroovyRuntimeException
        assert err.message.startsWith('could not access constructor: protected java.io.ObjectInputStream()')
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testProtectedConstructorOfClassOfAnotherPackage(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class A {
                final String value
                protected A(String value) { this.value = value }
            }
            true
        '''
        assert shell.evaluate('''package q
            class C extends p.A {
                C() { super('c') }
                static p.A make() {
                    new p.A('x')
                }
            }
            C.make().value
        ''') == 'x'
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testSuperCallOfPackagePrivateMethodOfClassOfAnotherClassLoader(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class M {
                @groovy.transform.PackageScope String pm() { 'pm' }
            }
            true
        '''
        assert shell.evaluate('''package p
            class B extends M { // same package name as M, but another class loader
                def test() {
                    super.pm()
                }
            }
            @groovy.transform.CompileStatic
            class C extends M {
                def test() {
                    super.pm()
                }
            }
            [new B().test(), new C().test()]
        ''') == ['pm', 'pm']
    }

    // GROOVY-12570
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testSuperCallOfPackagePrivateMethodOfClassOfAnotherClassLoaderWithMethodOfSameName(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class M {
                @groovy.transform.PackageScope String pm() { 'M' }
            }
            true
        '''
        assert shell.evaluate('''package p
            class B extends M { // same package name as M, but another class loader
                String pm() { 'B>' + super.pm() }
                def test() {
                    [super.pm(), pm()]
                }
            }
            @groovy.transform.CompileStatic
            class C extends M {
                String pm() { 'C>' + super.pm() }
                def test() {
                    [super.pm(), pm()]
                }
            }
            new B().test() + new C().test()
        ''') == ['M', 'B>M', 'M', 'C>M']
    }

    // GROOVY-12570
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testSuperCallReachesNearestSuperclassMethodOfSameNameAndParameters(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class A {
                @groovy.transform.PackageScope String pm() { 'A' }
            }
            true
        '''
        shell.evaluate '''package p
            class MidOfOtherLoader extends A { // its pm() does not override A's
                @groovy.transform.PackageScope String pm() { 'MidOfOtherLoader' }
            }
            class PublicMid extends A {
                String pm() { 'PublicMid' }
            }
            class MidWithoutPm extends A {
            }
            true
        '''
        def mids = ['MidOfOtherLoader', 'PublicMid', 'MidWithoutPm', 'MidOfSameLoader']
        List<Class> classes = shell.evaluate('''package p
            class MidOfSameLoader extends A {
                @groovy.transform.PackageScope String pm() { 'MidOfSameLoader' }
            }
        ''' + mids.collect { mid -> """
            class D$mid extends $mid {
                String pm() { 'D>' + super.pm() }
                def test() { [super.pm(), pm()] }
            }
            @groovy.transform.CompileStatic
            class S$mid extends $mid {
                String pm() { 'S>' + super.pm() }
                def test() { [super.pm(), pm()] }
            }
        """ }.join('') + '''
            [DMidOfOtherLoader, SMidOfOtherLoader, DPublicMid, SPublicMid, DMidWithoutPm, SMidWithoutPm, DMidOfSameLoader, SMidOfSameLoader]
        ''')
        // the nearest method of the superclasses decides, so a deeper A.pm() is not reached
        def nearest = { Class c -> c.superclass.simpleName == 'MidWithoutPm' ? 'A' : c.superclass.simpleName }
        assert classes.collect { it.getDeclaredConstructor().newInstance().test() } == classes.collect {
            [nearest(it), it.simpleName[0] + '>' + nearest(it)]
        }
        // the super method of the meta-class: a super$ method of D, if any, or the method of the superclass;
        // S calls PublicMid.pm() and MidOfSameLoader.pm() directly, so it has no super$ method for them, and
        // the nearest method hides A.pm() all the same
        assert classes.collect { c ->
            def metaClass = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(c)
            metaClass.getMethodWithoutCaching(c, 'pm', [] as Class[], true)?.invoke(c.getDeclaredConstructor().newInstance(), [] as Object[])
        } == classes.collect { c ->
            c.simpleName in ['SPublicMid', 'SMidOfSameLoader'] ? null : nearest(c)
        }
    }

    // GROOVY-12570
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testSuperCallOfPackagePrivateMethodOfClassOfAnotherPackage(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package q
            class M {
                @groovy.transform.PackageScope String pm() { 'M' }
            }
            true
        '''
        def err = shouldFail(MissingMethodException) {
            shell.evaluate '''package p
                class B extends q.M { // another package: M.pm() is not accessible
                    String pm() { 'B' }
                    def test() {
                        super.pm()
                    }
                }
                new B().test()
            '''
        }
        assert err.method == 'pm'
    }

    // GROOVY-12570
    @Test
    void testSuperMethodsOfMetaClassForPackagePrivateMethodsOfClassOfAnotherClassLoader() {
        def shell = shell(true)
        shell.evaluate '''package p
            class M {
                @groovy.transform.PackageScope String pm() { 'M' }
                @groovy.transform.PackageScope String pm(String s) { 'M' + s }
                @groovy.transform.PackageScope String qm() { 'M' }
            }
            true
        '''
        Class b = shell.evaluate '''package p
            class B extends M {
                String pm() { 'B' }
                String pm(String s) { 'B' + s }
                String qm() { 'B' }
            }
            B
        '''
        def metaClass = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(b)
        def object = b.getDeclaredConstructor().newInstance()
        // the method of M, not B: for the overloads of pm and for qm, which has none
        [pm: [], pm1: [String], qm: []].each { name, types ->
            MetaMethod method = metaClass.getMethodWithoutCaching(b, name - '1', types as Class[], true)
            assert method.declaringClass.theClass == b.superclass
            assert method.invoke(object, types ? ['x'] as Object[] : [] as Object[]) == (types ? 'Mx' : 'M')
            assert metaClass.getMethodWithoutCaching(b, name - '1', types as Class[], false).declaringClass.theClass == b
        }
    }
}
