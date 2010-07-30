/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.bugs

/**
 * @version $Revision: 1.4 $
 */
class BlockAsClosureBug extends GroovyTestCase {

    void testBug() {
        def c = 0

        block: {
            c = 9
        }

        println(c)

        assert c == 9
    }

    void testStaticBug() {
        staticMethod(null)
    }

    void testNonVoidMethod() {
        foo()
    }

    static void staticMethod(args) {
        def c = 0

        block: {
            c = 9
        }

        println(c)

        assert c == 9
    }

    def foo() {
        def c = 0

        block: {
            c = 9
        }
        println(c)

        assert c == 9
        return 5
    }
}
