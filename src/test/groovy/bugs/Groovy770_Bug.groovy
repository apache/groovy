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
/**
 */

package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy770_Bug extends GroovyTestCase {
     
    void testBug() {
        def a = new Pair(sym:"x")
        def b = new Pair(sym:"y")
        def c = new Pair(sym:"y")

        def l1 = [a, b]
        def l2 = [c]
        assert l1 - l2 == l1


        a = new CPair(sym:"x")
        b = new CPair(sym:"y")
        c = new CPair(sym:"y")
        l1 = [a, b]
        l2 = [c]
        assert l1 - l2 == [a]
    }
}

import java.util.*

class Pair {
  String sym
}

class CPair implements Comparable {
  public String sym
  int compareTo(Object o) {
      return sym.compareTo(((CPair) o).sym);
  }
}


