import groovy.transform.CompileStatic

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

def c() {
    def x = []
    def y = []
    assert y !== x
    assert 'a' === 'a'
    def otherA = new String('a')
    assert 'a' == otherA && 'a'.equals(otherA)
    assert 'a' !== otherA && !'a'.is(otherA)
    assert 0 == 0 && 'a' === 'a' && 1 != 2 && 'a' !== new String('a') && 1 == 1

    def a = 'abc'
    def b = a
    assert a === b
    assert !(a !== b)
    assert a === b && !(a !== b)
    assert null === null
    assert true === true
    assert false === false
    assert 0 === 0
    assert 0 !== null
    assert null !== 0
}
c()

@CompileStatic
def c_cs() {
    def x = []
    def y = []
    assert y !== x
    assert 'a' === 'a'
    def otherA = new String('a')
    assert 'a' == otherA && 'a'.equals(otherA)
    assert 'a' !== otherA && !'a'.is(otherA)
    assert 0 == 0 && 'a' === 'a' && 1 != 2 && 'a' !== new String('a') && 1 == 1

    def a = 'abc'
    def b = a
    assert a === b
    assert !(a !== b)
    assert a === b && !(a !== b)
    assert null === null
    assert true === true
    assert false === false
    assert 0 === 0
    assert 0 !== null
    assert null !== 0
}
c_cs()
