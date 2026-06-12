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

class SqlStoredProcDetectionTest {

    @Test
    void detectsPlainCall() {
        assert Sql.appearsLikeStoredProc('call foo()')
        assert Sql.appearsLikeStoredProc('CALL foo()')
        assert Sql.appearsLikeStoredProc('Call foo()')
    }

    @Test
    void detectsBracedCall() {
        assert Sql.appearsLikeStoredProc('{call foo()}')
        assert Sql.appearsLikeStoredProc('{ call foo() }')
    }

    @Test
    void detectsOutParamCall() {
        assert Sql.appearsLikeStoredProc('{? = call foo()}')
        assert Sql.appearsLikeStoredProc('? = call foo()')
        assert Sql.appearsLikeStoredProc('?= call foo()')
        assert Sql.appearsLikeStoredProc('?=call foo()')
    }

    @Test
    void ignoresLeadingWhitespace() {
        assert Sql.appearsLikeStoredProc('   call foo()')
        assert Sql.appearsLikeStoredProc('\t\ncall foo()')
    }

    @Test
    void rejectsNonCall() {
        assert !Sql.appearsLikeStoredProc('select * from foo')
        assert !Sql.appearsLikeStoredProc('update t set x=1')
        assert !Sql.appearsLikeStoredProc('')
        assert !Sql.appearsLikeStoredProc('   ')
        assert !Sql.appearsLikeStoredProc('xcall foo')
    }

    // GROOVY-9992: catastrophic backtracking on whitespace-heavy input.
    // With the buggy regex, 1000 leading spaces followed by a non-"call" token
    // took seconds; we give the fixed implementation a very generous budget.
    @Test
    void noCatastrophicBacktrackingOnWhitespacePadding() {
        String input = ' ' * 10000 + 'select 1'
        long start = System.nanoTime()
        assert !Sql.appearsLikeStoredProc(input)
        long elapsedMs = (System.nanoTime() - start) / 1_000_000
        assert elapsedMs < 1000, "appearsLikeStoredProc took ${elapsedMs}ms on padded input"
    }
}
