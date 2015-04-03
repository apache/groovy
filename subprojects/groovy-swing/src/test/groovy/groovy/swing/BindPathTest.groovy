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
package groovy.swing

import groovy.beans.Bindable

class BindPathTest extends GroovySwingTestCase {

    void testClosureBindingProperties() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions {
            beanA = new BeanPathTestA(foo:'x', bar:'y', bif:'z', qux:'w')
            beanC = new BeanPathTestA(foo:beanA, bar:'a')
            beanB = bean(new BeanPathTestB(), foo:bind {beanA.foo}, baz:bind {beanA.bar * 2}, bif: bind {beanC.foo.bar})
        }
        def beanA = swing.beanA
        def beanB = swing.beanB
        def beanC = swing.beanC
        assert beanB.foo == 'x'
        assert beanB.baz == 'yy'

        // bif is chained two levels down
        assert beanB.bif == 'y'

        beanA.bar = 3
        assert beanB.baz == 6

        // assert change at deepest level
        assert beanB.bif == 3
        //assert change at first level
        beanC.foo = beanC
        assert beanB.bif == 'a'

        // assert change at deepest level again
        beanC.bar = 'c'
        assert beanB.bif == 'c'
      }
    }

    void testClosureBindingLocalVariables() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        def beanA = null
        def beanB = null
        def beanC = null
        swing.actions {
            beanA = new BeanPathTestA(foo:'x', bar:'y', bif:'z', qux:'w')
            beanC = new BeanPathTestA(foo:beanA, bar:'a')
            beanB = bean(new BeanPathTestB(), foo:bind {beanA.foo}, baz:bind {beanA.bar * 2}, bif: bind {beanC.foo.bar})
        }
        assert beanB.foo == 'x'
        assert beanB.baz == 'yy'

        // bif is chained two levels down
        assert beanB.bif == 'y'

        beanA.bar = 3
        assert beanB.baz == 6

        // assert change at deepest level
        assert beanB.bif == 3
        //assert change at first level
        beanC.foo = beanC
        assert beanB.bif == 'a'

        // assert change at deepest level again
        beanC.bar = 'c'
        assert beanB.bif == 'c'
      }
    }

    void testSyntheticBindings() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.panel {
            tweetBox = textField()
            tweetButton = button(enabled:bind {tweetBox.text.length() in  1..140})
            tweetLimit = progressBar(value:bind {Math.min(140, tweetBox.text.length())},
                    string: bind { int count = tweetBox.text.length();
                        ((count <= 140)
                            ? "+${140 - count}"
                            : "-${count - 140}")
                    })
        }
        assert !swing.tweetButton.enabled
        assert swing.tweetLimit.string == "+140"

        swing.tweetBox.text = 'xxx'
        assert swing.tweetButton.enabled
        assert swing.tweetLimit.string == "+137"

        swing.tweetBox.text = 'x'*141
        assert !swing.tweetButton.enabled
        assert swing.tweetLimit.string == "-1"
      }
    }
}

class BeanPathTestA {
    @Bindable Object foo
    @Bindable Object bar
    Object bif
    @Bindable Object qux
}

class BeanPathTestB {
    @Bindable Object foo
    @Bindable Object baz
    @Bindable Object  bif
    Object qux
}
