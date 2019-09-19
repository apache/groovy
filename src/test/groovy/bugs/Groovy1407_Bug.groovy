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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy1407_Bug extends GroovyTestCase {
   void testGPathOnMultiKeyMap(){
      // each key is a two-element String list
      // each value is a two-element integer list
      def map = [['a','b']:[2,34],['c','d']:[2,16],['e','f']:[3,97],['g','h']:[4,48]]
      def expected = [["a", "b"],["c", "d"],["e", "f"],["g", "h"]]
      // previous returned value was [a, b, g, h, e, f, c, d]
      // i.e, expanded
      def actual = map.entrySet().key
      assert expected == actual
   }

   void testGPathOnMultiValueMap(){
      // each key is a two-element String list
      // each value is a two-element integer list
      def map = [['a','b']:[2,34],['c','d']:[2,16],['e','f']:[3,97],['g','h']:[4,48]]
      def expected = [[2, 34],[2, 16],[3, 97],[4, 48]]
      // previous returned value was [2, 34, 4, 48, 3, 97, 2, 16]
      // i.e, expanded
      def actual = map.entrySet().value
      assert expected == actual
   }
}
