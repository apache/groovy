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

class CollateTest extends GroovyTestCase {
  void testSimple() {
    def list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
    assert list.collate( 3 ) == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7, 8, 9 ], [ 10 ] ]

    int a = 1
    Iterable iterable = { [ hasNext:{ a <= 10 }, next:{ a++ } ] as Iterator } as Iterable
    assert iterable.collate( 3 ) == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7, 8, 9 ], [ 10 ] ]
  }

  void testRemain() {
    def list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
    assert list.collate( 3, false ) == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7, 8, 9 ] ]

    int a = 1
    Iterable iterable = { [ hasNext:{ a <= 10 }, next:{ a++ } ] as Iterator } as Iterable
    assert iterable.collate( 3, false ) == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7, 8, 9 ] ]
  }

  void testStepSimple() {
    def list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
    def expected = [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4, 5 ],
                     [ 4, 5, 6 ], [ 5, 6, 7 ], [ 6, 7, 8 ],
                     [ 7, 8, 9 ], [ 8, 9, 10 ], [ 9, 10 ], [ 10 ] ]
    assert list.collate( 3, 1 ) == expected

    int a = 1
    Iterable iterable = { [ hasNext:{ a <= 10 }, next:{ a++ } ] as Iterator } as Iterable
    assert iterable.collate( 3, 1 ) == expected
  }

  void testStepSimpleRemain() {
    def list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
    def expected = [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4, 5 ],
                     [ 4, 5, 6 ], [ 5, 6, 7 ], [ 6, 7, 8 ],
                     [ 7, 8, 9 ], [ 8, 9, 10 ] ]
    assert list.collate( 3, 1, false ) == expected

    int a = 1
    Iterable iterable = { [ hasNext:{ a <= 10 }, next:{ a++ } ] as Iterator } as Iterable
    assert iterable.collate( 3, 1, false ) == expected
  }

  void testTwoDimensions() {
    def list = 1..8
    def expected = [ [ [ 1,2 ], [ 3,4 ] ],
                     [ [ 5,6 ], [ 7,8 ] ] ]
    assert list.collate( 2 ).collate( 2 ) == expected
  }

  void testLargeStep() {
    def list = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
    assert list.collate( 2, 4, false ) == [ [ 1, 2 ], [ 5, 6 ], [ 9, 10 ] ]
    assert list.collate( 2, 4        ) == [ [ 1, 2 ], [ 5, 6 ], [ 9, 10 ] ]
  }

  void testEmpty() {
    assert [].collate( 3 ) == []
  }

  void testZero() {
    assert [ 1, 2, 3 ].collate( 0 ) == [[ 1, 2, 3 ]]
  }

  void testNegative() {
    assert [ 1, 2, 3 ].collate( -1 ) == [[ 1, 2, 3 ]]
  }

  void testNegativeStep() {
    // As soon as pos goes out of bounds, we get back what we are up to...
    assert [ 1, 2, 3 ].collate( 2, -1 ) == [[ 1, 2 ]]
  }

  void testZeroedStep() {
    String message = shouldFail (IllegalArgumentException) {
      [ 1, 2, 3 ].collate( 2, 0 )
    }
    assert message == 'step cannot be zero'
  }

  void testChaining() {
    def list = 1..15
    def expected = [ [ [ 1, 2, 3],  [ 4, 5, 6] ],
                     [ [ 7, 8, 9],  [10,11,12] ],
                     [ [13,14,15]              ] ]
    assert list.collate( 3 ).collate( 2 ) == expected
  }

  void testChainingRemain() {
    def list = 1..15
    def expected = [ [ [ 1, 2, 3],  [ 4, 5, 6] ],
                     [ [ 7, 8, 9],  [10,11,12] ] ]
    assert list.collate( 3 ).collate( 2, false ) == expected
  }

  void testSimpleUsecase() {
    def list = [ 'tim', 20, 'dave', 14, 'steve', 23 ]
    assert list.collate( 2 ).collectEntries() == [ 'tim':20, 'dave':14, 'steve':23 ]
  }

  void fancyManipulation() {
    def list = [ 'tim', 20, 'dave', 14, 'steve', 23 ]
    assert list.collate( 2 ).transpose() == [ [ 'tim', 'dave', 'steve' ], [ 20, 14, 23 ] ]
  }
}
