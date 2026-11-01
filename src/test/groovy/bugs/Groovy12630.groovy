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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript

/**
 * {@code ++} and {@code --} on a property whose getter or setter is an extension
 * method, and compound assignment to a property with a real setter and an
 * extension getter. Each case runs in dynamic, type-checked and statically
 * compiled code. The accessors log their calls: lower case for an extension
 * method, upper case for a real one.
 */
final class Groovy12630 {

    private static final String IMPORTS = '''
        import org.codehaus.groovy.runtime.m12n.Groovy12630Extension.Counter
        import org.codehaus.groovy.runtime.m12n.Groovy12630StaticExtension
    '''

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testIncrementAndDecrement(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                def c = new Counter(n: 3)
                def a = c.tally++
                def b = ++c.tally
                def d = c.tally--
                def e = --c.tally
                [a, b, d, e, c.n, c.log]
            }
            assert test() == [3, 5, 5, 3, 3, 'gsgsgsgs']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testNextAndPrevious(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                def c = new Counter()
                def a = c.money++
                ++c.money
                c.letter++
                def b = --c.letter
                [a, c.amount, c.text, b]
            }
            assert test() == [1, 3, 'a', 'a']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testReceiverIsEvaluatedOnce(String mode) {
        assertScript IMPORTS + """
            $mode
            class C {
                int calls = 0
                Counter counter = new Counter(n: 1)
                Counter obj() { calls += 1; counter }
                def test() {
                    obj().tally++
                    ++obj().tally
                    [calls, counter.n, counter.log]
                }
            }
            assert new C().test() == [2, 3, 'gsgs']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testRealSetterAndExtensionGetter(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                def c = new Counter(n: 1)
                c.total += 2
                c.total++
                [c.n, c.log]
            }
            assert test() == [4, 'gSgS']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testRealGetterAndExtensionSetter(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                def c = new Counter(n: 1)
                c.score += 2
                c.score++
                [c.n, c.log]
            }
            assert test() == [4, 'GsGs']
        """
    }

    // the GDK's getText(File) is not used when the class has its own getter
    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testRealAccessorsBeatExtensionGetter(String mode) {
        assertScript """
            class F extends File {
                String log = ''
                String stored = 'a'
                F() { super('unused') }
                String getText() { log += 'G'; stored }
                void setText(String text) { log += 'S'; stored = text }
            }
            $mode
            def test() {
                def f = new F()
                f.text += 'b'
                f.text++
                [f.stored, f.log]
            }
            assert test() == ['ac', 'GSGS']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testStaticProperty(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                Groovy12630StaticExtension.level = 10
                def a = Counter.level++
                --Counter.level
                ++Counter.level
                [a, Groovy12630StaticExtension.level]
            }
            assert test() == [10, 11]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testInClosureAndWith(String mode) {
        assertScript IMPORTS + """
            $mode
            def test() {
                def c = new Counter(n: 1)
                def closure = { -> c.tally++; ++c.tally }
                closure()
                c.with { tally++ }
                c?.tally++
                c.n
            }
            assert test() == 5
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testGdkExtensionProperty(String mode) {
        assertScript """
            $mode
            def test(File file) {
                file.text++
                file.text
            }
            File file = File.createTempFile('groovy12630', null)
            try {
                file.text = 'a'
                assert test(file) == 'b'
            } finally {
                file.delete()
            }
        """
    }
}
