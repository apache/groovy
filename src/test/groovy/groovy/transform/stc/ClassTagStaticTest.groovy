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
 * compiler-supplied {@code Class<X>} token(s) and the type checker synthesises them, at their
 * declared parameter positions, from the receiver's type argument(s). The first consumers are the
 * {@code asChecked} extension methods.
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
    void testTokensInterleavedWithSuppliedArguments() {
        // tokens need not be adjacent or leading: each is filled at its declared position, with the
        // supplied arguments mapping to the remaining slots in order
        assertScript '''
            import groovy.transform.stc.ClassTag

            class Box<K,V> {
                List<Class> captured = []
                Map<K,V> bracket(@ClassTag Class<V> valueType, Closure init, @ClassTag Class<K> keyType) {
                    captured = [valueType, keyType]
                    [:]
                }
            }

            Box<Number,String> b = new Box<>()
            b.bracket{ }
            assert b.captured == [String, Number]   // closure landed between the two tokens
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

    @Test
    void testMethodTypeParameterIsRejectedAtCompileTime() {
        // a type variable declared by the method itself cannot be reified from the receiver, so the
        // annotation could never inject - report the mistake rather than silently disabling
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class Maker {
                def <X> X make(@ClassTag Class<X> type) { null }
            }
        ''', "@ClassTag cannot reify type parameter 'X' declared by the method itself"
    }

    @Test
    void testShadowedTypeParameterIsRejectedAtCompileTime() {
        // per Java scoping the method-declared K shadows the class-level K, so reifying the
        // receiver's K would inject the wrong class - rejected rather than mis-targeted
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class Box<K,V> {
                def <K> K pick(@ClassTag Class<K> type, Closure c) { null }
            }
        ''', "@ClassTag cannot reify type parameter 'K' declared by the method itself"
    }

    @Test
    void testStaticSelfStyleMethodTypeParameterAccepted() {
        // extension-method authoring pattern: on a static method the type variable is necessarily
        // method-declared and connects to the receiver through the self parameter - no error
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Exts {
                static <T> List<T> checkedView(List<T> self, @ClassTag Class<T> type) { self.asChecked(type) }
            }
            assert Exts.checkedView(['a'], String) == ['a']
        '''
    }

    @Test
    void testNonClassParameterIsRejectedAtCompileTime() {
        // on anything but a Class parameter the annotation could never inject - same rationale as
        // the override typo check: report rather than silently disable
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class C {
                def load(@ClassTag String name) { }
            }
        ''', '@ClassTag only applies to a Class parameter'
    }

    private static Object evalCompileStatic(boolean preemptionDisabled, String script) {
        def cfg = new CompilerConfiguration()
        cfg.classTagPreemptionDisabled = preemptionDisabled
        cfg.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        new GroovyShell(cfg).evaluate(script)
    }

    @Test
    void testPreemptionDisabledKeepsWithDefaultLenient() {
        // the global opt-out disables every preemptive upgrade: withDefault binds the lenient
        // overload again, exactly as before any library declared preemption
        def lenient = evalCompileStatic(true, '''
            Map<Number,String> base = [:]
            Map<Number,String> m = base.withDefault{ 'n/a' }
            ((Map) m).put('x', 'y')           // no ClassCastException - not a checked view
            ((Map) m).containsKey('x')
        ''')
        assert lenient == true
    }

    @Test
    void testPreemptionEnabledIsTheDefault() {
        // an explicit false matches the default: the checked upgrade applies
        def badKey = evalCompileStatic(false, '''
            Map<Number,?> base = [:]
            Map<Number,?> m = base.withDefault{ null }
            boolean threw = false
            try { ((Map) m).put('x', 1) } catch (ClassCastException e) { threw = true }
            threw
        ''')
        assert badKey == true
    }

    @Test
    void testAdditiveAsCheckedUnaffectedByGlobalDisable() {
        // additive injection is not preemption, so asChecked() still works under the global opt-out
        def result = evalCompileStatic(true, '''
            List<String> base = []
            List<String> checked = base.asChecked()
            checked.add('ok')
            base
        ''')
        assert result == ['ok']
    }

    @Test
    void testUserApiDeclaresPreemptionIntent() {
        // a user API self-declares preemption with preempt=true - no compiler configuration needed
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String grow(Closure c) { 'lenient' }
                String grow(@ClassTag(preempt=true) Class<K> keyType, Closure c) { 'checked:' + keyType.simpleName }
            }
            @groovy.transform.CompileStatic
            String check() {
                Holder<Number,String> h = new Holder<>()
                h.grow{ }
            }
            assert check() == 'checked:Number'
        '''
    }

    @Test
    void testUserApiWithoutDeclaredIntentStaysLenient() {
        // without preempt=true a tagged overload is only ever selected additively: the token-less
        // incumbent keeps existing calls
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
    void testUserApiIntentVetoedByConsumer() {
        // declared intent is still subject to the consuming build's global opt-out
        def result = evalCompileStatic(true, '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String grow(Closure c) { 'lenient' }
                String grow(@ClassTag(preempt=true) Class<K> keyType, Closure c) { 'checked:' + keyType.simpleName }
            }
            Holder<Number,String> h = new Holder<>()
            h.grow{ }
        ''')
        assert result == 'lenient'
    }

    @Test
    void testPreemptionAllowedWithinClassHierarchy() {
        // containment permits a subclass to preempt its superclass's token-less overload - the
        // receiver's own hierarchy counts as one owner
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Base<K,V> {
                String grow(Closure c) { 'lenient' }
            }
            class Sub<K,V> extends Base<K,V> {
                String grow(@ClassTag(preempt=true) Class<K> keyType, Closure c) { 'checked:' + keyType.simpleName }
            }
            @groovy.transform.CompileStatic
            String check() {
                Sub<Number,String> s = new Sub<>()
                s.grow{ }
            }
            assert check() == 'checked:Number'
        '''
    }

    @Test
    void testPreemptionRolledBackWhenTagOverloadDoesNotApply() {
        // injection matches by arity only, so a preempt=true overload can arity-match while its
        // remaining parameters reject the supplied argument types; the retried selection fails and
        // the call must still bind the original lenient match, exactly as written
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String grow(Closure c) { 'lenient' }
                String grow(@ClassTag(preempt=true) Class<K> keyType, String s) { 'checked' }
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
    void testAdditiveInjectionRolledBackWhenTagOverloadDoesNotApply() {
        // same arity-match-but-inapplicable shape with no token-less overload to fall back on:
        // the rollback ensures the error cites the call as written, not the synthesised tokens
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class Holder<K,V> {
                String take(@ClassTag Class<K> keyType, String s) { 'checked' }
            }
            void use(Holder<Number,String> h) {
                h.take{ }
            }
        ''', 'Cannot find matching method', 'take(groovy.lang.Closure'
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
    void testAllObjectTokensDoNotInjectAdditively() {
        // the nothing-to-gain guard also covers the additive path: a checked Object view could
        // never reject anything, so no token is synthesised and the call fails to resolve as
        // written; asChecked(Object) remains available explicitly
        shouldFailWithMessages '''
            void useObjects(List<Object> base) {
                base.asChecked()
            }
        ''', 'Cannot find matching method', 'asChecked()'
    }

    @Test
    void testWildcardReceiverDoesNotResolve() {
        // wildcard type arguments carry no reifiable class, so injection degrades exactly like
        // the raw-receiver case
        shouldFailWithMessages '''
            void useWild(Map<?,?> base) {
                base.asChecked()
            }
        ''', 'Cannot find matching method', 'asChecked()'
    }

    @Test
    void testEqualTokenCandidatesDisagreeingOnClassesAreAmbiguous() {
        // two overloads arity-match with one token each but reify different classes (K vs V);
        // neither is more specific, so no injection occurs and the call fails as written
        shouldFailWithMessages '''
            import groovy.transform.stc.ClassTag
            class Box<K,V> {
                String m(@ClassTag Class<K> keyType, Closure c) { 'k' }
                String m(Closure c, @ClassTag Class<V> valueType) { 'v' }
            }
            void use(Box<Number,String> b) {
                b.m{ }
            }
        ''', 'Cannot find matching method', 'm(groovy.lang.Closure'
    }

    @Test
    void testAdditiveInjectionPrecedesClosurePropertyFallback() {
        // resolution order: when a call matches no method as written, @ClassTag injection is
        // attempted before the closure-property fallback (GROOVY-5705 et al.); a tagged overload
        // wins over a same-named closure-valued property
        assertScript '''
            import groovy.transform.stc.ClassTag
            class Box<K,V> {
                Closure fetch = { -> 'property' }
                String fetch(@ClassTag Class<K> keyType, Closure c) { 'method:' + keyType.simpleName }
            }
            @groovy.transform.CompileStatic
            String check() {
                Box<Number,String> b = new Box<>()
                b.fetch{ }
            }
            assert check() == 'method:Number'
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
