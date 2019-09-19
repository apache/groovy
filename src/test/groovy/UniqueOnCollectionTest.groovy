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
package groovy

import groovy.test.GroovyTestCase

class UniqueOnCollectionTest extends GroovyTestCase {

    void testUnique() {
        def list = [-1, 0, 1, 1, 0, -1]
        assert list.unique() == [-1, 0, 1]
    }

    void testUniqueOnListNoDupls() {
        assert [].unique() == []
        assert [1].unique() == [1]
        assert [1, 2].unique() == [1, 2]
        def a = [1, 2]
        assert a.is(a.unique())
    }

    void testUniqueOnListOneDupl() {
        assert [1, 1].unique() == [1]
        def a = [1, 1]
        assert a.is(a.unique())
        assert [1, 2, 1].unique() == [1, 2]
        assert [1, 2, 1, 1].unique() == [1, 2]
        assert [1, 1, 2].unique() == [1, 2]
        assert [1, 1, 2, 1].unique() == [1, 2]
        assert [1, 1, 2, 1, 1].unique() == [1, 2]
    }

    void testUniqueOnListTwoDupls() {
        assert [1, 1, 2, 2].unique() == [1, 2]
        def a = [1, 1, 2, 2]
        assert a.is(a.unique())
        assert [1, 2, 1, 2].unique() == [1, 2]
        assert [1, 2, 1, 1, 2].unique() == [1, 2]
        assert [1, 1, 2, 2].unique() == [1, 2]
        assert [1, 1, 2, 1, 2].unique() == [1, 2]
        assert [1, 1, 2, 2, 1, 1, 2, 2].unique() == [1, 2]
    }

    void testUniqueOnIterator() {
        def it = [1, 1, 2, 2].iterator().unique()
        assert it instanceof Iterator
        def result = it.toList()
        assert result == [1, 2]
    }

    void testUniqueOnOtherCollections() {
        def a = new HashSet([1, 1])
        assert a.is(a.unique())
        assert 1 == a.size()
        a = new TreeSet([1, 1])
        assert a.is(a.unique())
        assert 1 == a.size()
        a = new Vector([1, 1])
        assert a.is(a.unique())
        assert 1 == a.size()
        a = new LinkedList([1, 1])
        assert a.is(a.unique())
        assert 1 == a.size()
    }

    /** GROOVY-1006 */
    void testUniqueOnDifferentTypes() {
        def a = [1, 2, (short) 1, 2L, 2.0]
        def b = a.unique()
        assert (b == a && a == [1, 2])
        a = [Math.PI, "foo", 1.0, 2L, (short) 2, 2.0F]
        b = a.unique()
        assert (b == a && a == [Math.PI, "foo", 1.0, 2L])
    }

    /** GROOVY-1956 */
    void testUniqueOnListWithNulls() {
        def x = [1, 2, 3, 1, 2, 3, null, 'a', null]
        x.unique()
        assert x == [1, 2, 3, null, 'a']
    }

    /** GROOVY-4742 */
    void testNonMutatingList() {
        def it = [-1, 0, 1, 1, 0, -1]
        def uniq = it.unique( false )
        assert uniq != it
        assert uniq == [-1, 0, 1]
        assert it   == [-1, 0, 1, 1, 0, -1]
    }

    /** GROOVY-4742 */
    void testImmutableUniqueListWithDefaultComparator() {
        def orig = [1, 3, 2, 3]
        def uniq = orig.unique(false)
        assert orig == [1, 3, 2, 3]
        assert uniq == [1, 3, 2]
    }
}
