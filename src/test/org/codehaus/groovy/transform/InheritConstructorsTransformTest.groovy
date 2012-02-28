/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform

/**
 * @author Paul King
 */
class InheritConstructorsTransformTest extends GroovyShellTestCase {

    void testStandardCase() {
        assertScript """
            import groovy.transform.InheritConstructors
            @InheritConstructors class CustomException extends RuntimeException { }
            def ce = new CustomException('foo')
            assert ce.message == 'foo'
        """
    }

    void testOverrideCase() {
        assertScript """
            import groovy.transform.InheritConstructors
            @InheritConstructors
            class CustomException2 extends RuntimeException {
                CustomException2() { super('bar') }
            }
            def ce = new CustomException2()
            assert ce.message == 'bar'
            ce = new CustomException2('foo')
            assert ce.message == 'foo'
        """
    }

    void testChainedCase() {
        assertScript """
            import groovy.transform.InheritConstructors
            @InheritConstructors
            class CustomException5 extends CustomException4 {}
            @InheritConstructors
            class CustomException3 extends RuntimeException {}
            @InheritConstructors
            class CustomException4 extends CustomException3 {}
            def ce = new CustomException5('baz')
            assert ce.message == 'baz'
        """
    }

    void testInnerClassUsage() {
        assertScript """
            import groovy.transform.InheritConstructors
            @InheritConstructors
            class Outer extends RuntimeException {
                @InheritConstructors
                class Inner extends RuntimeException {}
                @InheritConstructors
                static class StaticInner extends RuntimeException {}
                void test() {
                    assert new StaticInner('bar').message == 'bar'
                    assert new Inner('foo').message == 'foo'
                }
            }
            class Outer2 extends Outer {
                @InheritConstructors
                class Inner2 extends Outer.Inner {}
                void test() {
                    assert new Inner2('foobar').message == 'foobar'
                }
            }

            def o = new Outer('baz')
            assert o.message == 'baz'
            o.test()
            new Outer2().test()
        """
    }

}