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
package groovy.sql

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

class InListExpanderTest {

    @Test
    void expandsSingleInListToNPlaceholders() {
        def result = InListExpander.expand(
            'SELECT * FROM t WHERE id IN (?)',
            [Sql.inList([1, 2, 3])])
        assert result.sql == 'SELECT * FROM t WHERE id IN (?, ?, ?)'
        assert result.params == [1, 2, 3]
    }

    @Test
    void leavesSqlUnchangedWhenNoInListMarkers() {
        def result = InListExpander.expand(
            'SELECT * FROM t WHERE id = ?',
            [42])
        assert result.sql == 'SELECT * FROM t WHERE id = ?'
        assert result.params == [42]
    }

    @Test
    void preservesScalarParamsAroundInList() {
        def result = InListExpander.expand(
            'SELECT * FROM t WHERE country = ? AND first_name IN (?) AND active = ?',
            ['US', Sql.inList(['Jack', 'Jill']), true])
        assert result.sql == 'SELECT * FROM t WHERE country = ? AND first_name IN (?, ?) AND active = ?'
        assert result.params == ['US', 'Jack', 'Jill', true]
    }

    @Test
    void singletonInListProducesSingleQuestionMark() {
        def result = InListExpander.expand(
            'SELECT * FROM t WHERE id IN (?)',
            [Sql.inList([42])])
        assert result.sql == 'SELECT * FROM t WHERE id IN (?)'
        assert result.params == [42]
    }

    @Test
    void questionMarkInsideSingleQuotedStringIsNotExpanded() {
        def result = InListExpander.expand(
            "SELECT 'what?' FROM t WHERE id IN (?)",
            [Sql.inList([1, 2])])
        assert result.sql == "SELECT 'what?' FROM t WHERE id IN (?, ?)"
        assert result.params == [1, 2]
    }

    @Test
    void questionMarkInsideDoubleQuotedIdentifierIsNotExpanded() {
        def result = InListExpander.expand(
            'SELECT "col?name" FROM t WHERE id IN (?)',
            [Sql.inList([1, 2])])
        assert result.sql == 'SELECT "col?name" FROM t WHERE id IN (?, ?)'
        assert result.params == [1, 2]
    }

    @Test
    void questionMarkInsideLineCommentIsNotExpanded() {
        def result = InListExpander.expand(
            'SELECT * FROM t -- WHERE id = ?\nWHERE id IN (?)',
            [Sql.inList([1, 2])])
        assert result.sql == 'SELECT * FROM t -- WHERE id = ?\nWHERE id IN (?, ?)'
        assert result.params == [1, 2]
    }

    @Test
    void questionMarkInsideBlockCommentIsNotExpanded() {
        def result = InListExpander.expand(
            'SELECT * FROM t /* was: id = ? */ WHERE id IN (?)',
            [Sql.inList([1, 2])])
        assert result.sql == 'SELECT * FROM t /* was: id = ? */ WHERE id IN (?, ?)'
        assert result.params == [1, 2]
    }

    @Test
    void doubledQuoteEscapeInStringIsRespected() {
        def result = InListExpander.expand(
            "SELECT 'O''Brien?' FROM t WHERE id IN (?)",
            [Sql.inList([1, 2])])
        assert result.sql == "SELECT 'O''Brien?' FROM t WHERE id IN (?, ?)"
        assert result.params == [1, 2]
    }

    @Test
    void multipleInListsInSameQuery() {
        def result = InListExpander.expand(
            'SELECT * FROM t WHERE a IN (?) AND b IN (?)',
            [Sql.inList([1, 2]), Sql.inList(['x', 'y', 'z'])])
        assert result.sql == 'SELECT * FROM t WHERE a IN (?, ?) AND b IN (?, ?, ?)'
        assert result.params == [1, 2, 'x', 'y', 'z']
    }

    @Test
    void inListRejectsEmptyCollection() {
        def e = shouldFail(IllegalArgumentException) {
            Sql.inList([])
        }
        assert e.message.contains('non-empty collection')
    }

    @Test
    void inListRejectsNullCollection() {
        shouldFail(IllegalArgumentException) {
            Sql.inList(null)
        }
    }

    @Test
    void inListSnapshotsCollection() {
        def backing = [1, 2, 3]
        def marker = Sql.inList(backing)
        backing.add(99)
        assert marker.values == [1, 2, 3]
    }

    // Sql.inList blocks empty collections at construction, but the InListParameter
    // interface is public — a custom implementation could still return an empty
    // collection. expand() must surface that as IllegalArgumentException, not
    // crash with NoSuchElementException from iterator.next().
    @Test
    void customInListParameterWithEmptyCollectionRejected() {
        def badMarker = new InListParameter() {
            @Override Collection<?> getValues() { [] }
        }
        def e = shouldFail(IllegalArgumentException) {
            InListExpander.expand('SELECT * FROM t WHERE id IN (?)', [badMarker])
        }
        assert e.message.contains('non-null, non-empty')
    }

    @Test
    void customInListParameterWithNullCollectionRejected() {
        def badMarker = new InListParameter() {
            @Override Collection<?> getValues() { null }
        }
        shouldFail(IllegalArgumentException) {
            InListExpander.expand('SELECT * FROM t WHERE id IN (?)', [badMarker])
        }
    }
}
