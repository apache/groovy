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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for {@link AwaitResult} value-object behaviour.
 */
final class AwaitResultTest {

    @Test
    void testSuccessHoldsValueIncludingNull() {
        def ok = AwaitResult.success('hello')
        assert ok.success
        assert !ok.failure
        assert ok.value == 'hello'
        assert ok.toString() == 'AwaitResult.Success[hello]'

        def nil = AwaitResult.success(null)
        assert nil.success
        assert nil.value == null
    }

    @Test
    void testFailureHoldsError() {
        def err = new RuntimeException('boom')
        def fail = AwaitResult.failure(err)
        assert fail.failure
        assert !fail.success
        assert fail.error.is(err)
        assert fail.toString().contains('boom')
    }

    @Test
    void testFailureRejectsNullError() {
        shouldFail(NullPointerException) {
            AwaitResult.failure(null)
        }
    }

    @Test
    void testGetValueOnFailureThrows() {
        def fail = AwaitResult.failure(new Exception('x'))
        def e = shouldFail(IllegalStateException) {
            fail.value
        }
        assert e.message.contains('failed')
    }

    @Test
    void testGetErrorOnSuccessThrows() {
        def ok = AwaitResult.success(1)
        def e = shouldFail(IllegalStateException) {
            ok.error
        }
        assert e.message.contains('successful')
    }

    @Test
    void testGetOrElse() {
        assert AwaitResult.success(7).getOrElse { -1 } == 7
        assert AwaitResult.failure(new Exception('e')).getOrElse { it.message.length() } == 1
    }

    @Test
    void testMapOnSuccessAndFailure() {
        assert AwaitResult.success('ab').map { it.length() }.value == 2
        def fail = AwaitResult.<String>failure(new Exception('keep'))
        def mapped = fail.map { it.toUpperCase() }
        assert mapped.failure
        assert mapped.error.message == 'keep'
    }

    @Test
    void testMapRejectsNullFunction() {
        shouldFail(NullPointerException) {
            AwaitResult.success(1).map(null)
        }
    }

    @Test
    void testEqualsAndHashCode() {
        def a = AwaitResult.success('x')
        def b = AwaitResult.success('x')
        def c = AwaitResult.success('y')
        def err = new RuntimeException('e')
        def f1 = AwaitResult.failure(err)
        def f2 = AwaitResult.failure(err)

        assert a == b
        assert a.hashCode() == b.hashCode()
        assert a != c
        assert a != f1
        assert f1 == f2
        assert f1.hashCode() == f2.hashCode()
        assert a != null
        assert a != 'x'
    }
}
