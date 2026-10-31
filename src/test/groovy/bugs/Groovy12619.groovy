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
 * A compound assignment or an increment of a property, e.g. {@code obj().v += 2} or
 * {@code obj().v++}, must evaluate the receiver of the property once and reuse it for
 * the read and the write. Previously the receiver was evaluated twice.
 */
final class GroovyNNNNN {

    private static final String BOX = '''
        class Box {
            int v = 1
            public int f = 1
            public String t = 'a'
            static int sv = 1
            String s = 'a'
            BigDecimal d = 1.5
            Box inner
        }
    '''

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testCompoundAssignment(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box()
                Box obj() { calls += 1; box }
                def test() {
                    def r = (obj().v += 2)
                    assert r == 3
                    obj().v *= 5
                    obj().v -= 1
                    obj().v <<= 1
                    obj().v %= 10
                    obj().s += 'b'
                    obj().d += 1
                    [calls, box.v, box.s, box.d]
                }
            }
            assert new C().test() == [7, 8, 'ab', 2.5]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testIncrementAndDecrement(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box()
                Box obj() { calls += 1; box }
                def test() {
                    def a = obj().v++
                    def b = ++obj().v
                    def c = obj().v--
                    def d = --obj().v
                    obj().d++
                    [calls, box.v, a, b, c, d, box.d]
                }
            }
            assert new C().test() == [5, 1, 1, 3, 3, 1, 2.5]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testReceiverIsEvaluatedBeforeRightHandSide(String mode) {
        assertScript BOX + """
            $mode
            class C {
                List<String> order = []
                Box box = new Box()
                Box obj() { order << 'receiver'; box }
                int rhs() { order << 'rhs'; 2 }
                def test() {
                    obj().v += rhs()
                    [order, box.v]
                }
            }
            assert new C().test() == [['receiver', 'rhs'], 3]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testPropertyChain(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box(inner: new Box())
                Box getB() { calls += 1; box }
                def test() {
                    b.inner.v += 1
                    b.inner.v++
                    ++b.inner.v
                    [calls, box.inner.v]
                }
            }
            assert new C().test() == [3, 4]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testSafeNavigation(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box()
                Box obj() { calls += 1; box }
                Box none() { calls += 1; null }
                def test() {
                    obj()?.v += 2
                    obj()?.v++
                    none()?.s += 'b'
                    [calls, box.v]
                }
            }
            assert new C().test() == [3, 4]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testAttribute(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box()
                Box obj() { calls += 1; box }
                def test() {
                    obj().@f += 2
                    obj().@f++
                    --obj().@f
                    obj().@t += 'b'
                    [calls, box.f, box.t]
                }
            }
            assert new C().test() == [4, 3, 'ab']
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testStaticPropertyThroughInstance(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                Box box = new Box()
                Box obj() { calls += 1; box }
                def test() {
                    obj().sv += 2
                    obj().sv++
                    [calls, Box.sv]
                }
            }
            assert new C().test() == [2, 4]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testInClosureAndGenericReceiver(String mode) {
        assertScript BOX + """
            $mode
            class C {
                int calls = 0
                List<Box> boxes = [new Box()]
                Box obj() { calls += 1; boxes[0] }
                List<Box> list() { calls += 1; boxes }
                def <T extends Box> T pick() { calls += 1; (T) boxes[0] }
                def test() {
                    Closure c = { -> obj().v += 2; obj().v++ }
                    c()
                    list().get(0).v += 1
                    list()[0].v++
                    pick().v += 1
                    [calls, boxes[0].v]
                }
            }
            assert new C().test() == [5, 7]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testExtensionProperty(String mode) {
        assertScript """
            $mode
            class C {
                int calls = 0
                File file
                File obj() { calls += 1; file }
                def test() {
                    obj().text += 'b'
                    [calls, file.text]
                }
            }
            File file = File.createTempFile('groovyNNNNN', null)
            try {
                file.text = 'a'
                assert new C(file: file).test() == [1, 'ab']
            } finally {
                file.delete()
            }
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testCompoundAssignmentOperatorMethod(String mode) {
        assertScript """
            class Acc {
                List items = []
                void plusAssign(Object x) { items << x }
            }
            class Holder {
                Acc acc = new Acc()
            }
            $mode
            class C {
                int calls = 0
                Holder holder = new Holder()
                Holder obj() { calls += 1; holder }
                def test() {
                    obj().acc += 1
                    obj().acc += 2
                    [calls, holder.acc.items]
                }
            }
            assert new C().test() == [2, [1, 2]]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testOtherReceiversUnchanged(String mode) {
        assertScript BOX + """
            class Base {
                int w = 1
            }
            $mode
            class C extends Base {
                int x = 1
                private int p = 1
                Box box = new Box()
                def test() {
                    Box local = box
                    local.v += 1
                    local.v++
                    this.x += 1
                    this.x++
                    this.@p += 1
                    super.w += 1
                    super.w++
                    Box.sv += 1
                    Box.sv++
                    new C().p += 1
                    [box.v, x, p, w, Box.sv]
                }
            }
            assert new C().test() == [3, 3, 2, 3, 3]
        """
    }

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testQualifiedOuterThis(String mode) {
        assertScript """
            $mode
            class Outer {
                int x = 1
                static int sx = 1
                class Inner {
                    def test() {
                        Outer.this.x += 1
                        Outer.this.x++
                        Outer.this.sx += 1
                        [Outer.this.x, Outer.sx]
                    }
                }
            }
            assert new Outer().new Inner().test() == [3, 2]
        """
    }
}
