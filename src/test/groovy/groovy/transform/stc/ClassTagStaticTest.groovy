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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@code @ClassTag} (GROOVY-12115): under static type checking, a call may omit the
 * trailing compiler-supplied {@code Class<X>} token(s) and the type checker synthesises them from
 * the receiver's type argument(s). The first consumers are the {@code asChecked} extension methods.
 */
final class ClassTagStaticTest extends StaticTypeCheckingTestCase {

    @Test
    void testListTokenInjectedFromElementType() {
        assertScript '''
            List<String> base = []
            List<String> checked = base.asChecked()       // compiler injects String.class
            checked.add('ok')
            assert base == ['ok']                          // checked view writes through to base
            boolean threw = false
            try {
                ((List) checked).add(42)                   // wrong element type via raw view
            } catch (ClassCastException e) {
                threw = true
            }
            assert threw
        '''
    }

    @Test
    void testMapTokensInjectedForKeyAndValue() {
        assertScript '''
            Map<Number,String> base = [:]
            Map<Number,String> checked = base.asChecked()  // compiler injects Number.class, String.class
            checked.put(1, 'one')
            assert base == [1: 'one']

            boolean badKey = false
            try { ((Map) checked).put('x', 'y') } catch (ClassCastException e) { badKey = true }
            assert badKey

            boolean badValue = false
            try { ((Map) checked).put(2, 99) } catch (ClassCastException e) { badValue = true }
            assert badValue
        '''
    }

    @Test
    void testInjectionWorksUnderCompileStaticDirectCall() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static List<String> make() {
                    List<String> base = []
                    List<String> checked = base.asChecked()
                    checked.add('ok')
                    base
                }
            }
            assert C.make() == ['ok']
        '''
    }

    @Test
    void testExplicitTokenStillResolves() {
        assertScript '''
            List<String> base = []
            List<String> checked = base.asChecked(String)  // nothing injected; existing overload
            checked.add('ok')
            assert base == ['ok']
        '''
    }

    @Test
    void testSortedMapSubtypeReceiver() {
        assertScript '''
            TreeMap<Number,String> base = new TreeMap<>()
            Map<Number,String> checked = base.asChecked()  // ConcreteMap subtype still resolves K,V
            checked.put(1, 'one')
            assert base == [1: 'one']
        '''
    }

    @Test
    void testRawReceiverDoesNotResolve() {
        // a raw receiver has no statically-known type argument, so no token is synthesised
        shouldFailWithMessages '''
            void useRaw(Map base) {
                base.asChecked()
            }
        ''', 'Cannot find matching method', 'asChecked()'
    }

    @Test
    void testWithDefaultPreemptedToKeyAndValueChecked() {
        assertScript '''
            Map<Number,String> base = [:]
            Map<Number,String> m = base.withDefault{ 'n/a' }   // preempted: key+value checked
            assert m[1] == 'n/a'                                // compatible key auto-grows with String default

            boolean badKey = false
            try { ((Map) m).put('x', 'y') } catch (ClassCastException e) { badKey = true }
            assert badKey

            boolean badValue = false
            try { ((Map) m).put(2, 99) } catch (ClassCastException e) { badValue = true }
            assert badValue
        '''
    }

    @Test
    void testWithDefaultPreemptedKeyCheckedWhenValueUnconstrained() {
        assertScript '''
            Map<Number,?> base = [:]
            Map<Number,?> m = base.withDefault{ null }          // preempted: at least key is checked
            assert m[1] == null

            boolean badKey = false
            try { ((Map) m).put('x', 1) } catch (ClassCastException e) { badKey = true }
            assert badKey
        '''
    }

    @Test
    void testWithDefaultStaysLenientWhenNothingToCheck() {
        // an untyped map erases both tokens to Object, so there is nothing to gain and the lenient
        // withDefault is kept rather than silently becoming a checked view
        assertScript '''
            def base = [:]
            def m = base.withDefault{ 'x' }
            ((Map) m).put('any', 1)                             // no ClassCastException
            assert m['any'] == 1
        '''
    }

    @Test
    void testExplicitWithDefaultTokensNotReinjected() {
        assertScript '''
            Map<Number,String> base = [:]
            Map<Number,String> m = base.withDefault(Number, String){ 'n/a' }
            assert m[1] == 'n/a'
        '''
    }

    @Test
    void testTokensReorderedByTypeVariableNotPosition() {
        // a method declaring the value token BEFORE the key token still receives each token in the
        // slot its Class<X> names - resolution is by type-variable name, not by position
        assertScript '''
            import groovy.transform.stc.ClassTag

            class Box<K,V> {
                List<Class> captured = []
                Map<K,V> record(@ClassTag Class<V> valueType, @ClassTag Class<K> keyType, Closure init) {
                    captured = [valueType, keyType]
                    [:]
                }
            }

            Box<Number,String> b = new Box<>()
            b.record{ }
            assert b.captured == [String, Number]   // valueType <- V=String, keyType <- K=Number
        '''
    }

    @Test
    void testOverrideReifiesRawClassParameter() {
        // a raw Class parameter cannot carry the type variable, so @ClassTag("K") names it explicitly
        assertScript '''
            import groovy.transform.stc.ClassTag

            class Box<K,V> {
                List<Class> captured = []
                Map<K,V> keyOf(@ClassTag('K') Class keyType, Closure init) {
                    captured = [keyType]
                    [:]
                }
            }

            Box<Number,String> b = new Box<>()
            b.keyOf{ }
            assert b.captured == [Number]
        '''
    }

    @Test
    void testOverrideReifiesWildcardClassParameter() {
        // a Class<?> parameter likewise carries no usable name; @ClassTag("V") supplies it
        assertScript '''
            import groovy.transform.stc.ClassTag

            class Box<K,V> {
                List<Class> captured = []
                Map<K,V> valueOf(@ClassTag('V') Class<?> valueType, Closure init) {
                    captured = [valueType]
                    [:]
                }
            }

            Box<Number,String> b = new Box<>()
            b.valueOf{ }
            assert b.captured == [String]
        '''
    }

    @Test
    void testOverrideTypoIsRejectedAtCompileTime() {
        // a name that does not match any type variable in scope is a typo that would silently disable
        // injection, so it is reported rather than ignored
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class Box<K,V> {
                Map<K,V> bad(@ClassTag('Z') Class keyType, Closure init) { [:] }
            }
        ''', '@ClassTag("Z") does not name a type parameter in scope'
    }

    private static Object evalCompileStatic(Set<String> preemptionTargets, String script) {
        def cfg = new CompilerConfiguration()
        cfg.classTagPreemptionTargets = preemptionTargets
        cfg.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        new GroovyShell(cfg).evaluate(script)
    }

    @Test
    void testPreemptionDisabledViaConfigKeepsWithDefaultLenient() {
        // clearing the allowlist disables preemption: withDefault binds the lenient overload again
        def lenient = evalCompileStatic([] as Set, '''
            Map<Number,String> base = [:]
            Map<Number,String> m = base.withDefault{ 'n/a' }
            ((Map) m).put('x', 'y')           // no ClassCastException - not a checked view
            ((Map) m).containsKey('x')
        ''')
        assert lenient == true
    }

    @Test
    void testAdditiveAsCheckedUnaffectedByEmptyPreemptionConfig() {
        // additive injection is not gated by the allowlist, so asChecked() still works when it is empty
        def result = evalCompileStatic([] as Set, '''
            List<String> base = []
            List<String> checked = base.asChecked()
            checked.add('ok')
            base
        ''')
        assert result == ['ok']
    }

    @Test
    void testUserApiOptsIntoPreemptionByName() {
        // a user API can be allowlisted to gain the same preemptive upgrade
        def result = evalCompileStatic(['grow'] as Set, '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String grow(Closure c) { 'lenient' }
                String grow(@ClassTag Class<K> keyType, Closure c) { 'checked:' + keyType.simpleName }
            }
            Holder<Number,String> h = new Holder<>()
            h.grow{ }
        ''')
        assert result == 'checked:Number'
    }

    @Test
    void testUserApiNotPreemptedWhenNotAllowlisted() {
        // the same API stays lenient under the default allowlist (which holds only withDefault)
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String grow(Closure c) { 'lenient' }
                String grow(@ClassTag Class<K> keyType, Closure c) { 'checked:' + keyType.simpleName }
            }
            @groovy.transform.CompileStatic
            String check() {
                Holder<Number,String> h = new Holder<>()
                h.grow{ }
            }
            assert check() == 'lenient'
        '''
    }

    @Test
    void testEmptyMapLiteralIdiomStaysLenient() {
        // mirrors a real @CompileStatic production idiom (groovy-docgenerator): the bare [:] receiver
        // is Object-typed, so the Object-guard keeps it lenient - no silent upgrade to a checked view
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static Map<String, Set<String>> idx() { [:].withDefault { new LinkedHashSet<String>() } }
            }
            def m = C.idx()
            boolean lenient = true
            try { ((Map) m).put(42, new LinkedHashSet()) } catch (ClassCastException e) { lenient = false }
            assert lenient                               // not preempted into a checked view
            m['foo'] << 'bar'
            assert m['foo'] == (['bar'] as Set)
        '''
    }

    @Test
    void testDynamicRequiresExplicitToken() {
        // the token-less spelling is static-only syntax: there is no injection outside static
        // checking, so dynamic Groovy must pass the Class explicitly
        assert new GroovyShell().evaluate("def l = [].asChecked(String); l.add('x'); l") == ['x']
        // and the token-less form does not silently yield a usable checked view
        shouldFail {
            new GroovyShell().evaluate('[].asChecked()')
        }
    }
}
