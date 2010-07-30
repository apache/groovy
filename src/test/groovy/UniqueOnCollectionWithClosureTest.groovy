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
package groovy

/**
 * @author Michael Baehr
 * @author Paul King
 */
class UniqueOnCollectionWithClosureTest extends GroovyTestCase {

    void testUniqueOnIterator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a, b -> Math.abs(a) <=> Math.abs(b)}
        def it = list.iterator().unique(closure)
        assert it instanceof Iterator
        def result = it.toList()
        assert result == [-1, 0]
    }

    /** GROOVY-3213  */
    void testUniqueWithTwoParameterClosureOnSet() {
        def set = [-1, 0, 1] as Set
        def closure = {a, b -> Math.abs(a) <=> Math.abs(b)}
        assert set.unique(closure).size() == 2
    }

    /** GROOVY-1236  */
    void testUniqueWithTwoParameterClosure() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a, b -> Math.abs(a) <=> Math.abs(b)}
        assert list.unique(closure) == [-1, 0]
    }

    /** GROOVY-1236  */
    void testUniqueWithOneParameterClosure() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a -> Math.abs(a)}
        assert list.unique(closure) == [-1, 0]
    }

}