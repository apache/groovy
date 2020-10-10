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
import groovy.transform.CompileStatic

def testArrayInitializer() {
    def x = new double[] {}
    assert x.length == 0

    def y = new double[][] {}
    assert y.length == 0

    def a = new int[] {1, 2}
    assert a[0] == 1
    assert a[1] == 2
    assert a as List == [1, 2]

    def b = new int[][] {
        new int[] {1, 1.plus(1)},
        new int[] {2.plus(1), 4}
    }
    assert b[0][0] == 1
    assert b[0][1] == 2
    assert b[1][0] == 3
    assert b[1][1] == 4

    def c = new String[] {
        'a'
        ,
        'b'
        ,
        'c'
        ,
    }
    assert c[0] == 'a'
    assert c[1] == 'b'
    assert c[2] == 'c'

    assert new String[]
            {
                'a', 'b'
            }
    ==
            ['a', 'b'] as String[]
}
testArrayInitializer();

@CompileStatic
def testArrayInitializerCS() {
    def x = new double[] {}
    assert x.length == 0

    def y = new double[][] {}
    assert y.length == 0

    def a = new int[] {1, 2}
    assert a[0] == 1
    assert a[1] == 2
    assert a as List == [1, 2]

    def b = new int[][] {
        new int[] {1, 1.plus(1)},
        new int[] {2.plus(1), 4}
    }
    assert b[0][0] == 1
    assert b[0][1] == 2
    assert b[1][0] == 3
    assert b[1][1] == 4

    def c = new String[] {
        'a'
        ,
        'b'
        ,
        'c'
        ,
    }
    assert c[0] == 'a'
    assert c[1] == 'b'
    assert c[2] == 'c'

    assert new String[]
            {
                'a', 'b'
            }
    ==
    ['a', 'b'] as String[]
}
testArrayInitializerCS();