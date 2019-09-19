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

class Groovy3509Bug extends GroovyTestCase {

    void testGPathInconcistency() {
        def data = [
                [a: null],
                [a: [b: 1]],
                [a: [b: 2]]
        ]
        assert data.a.b.sum() == 3

        data = [
                [a: [b: 1]],
                [a: [b: 2]],
                [a: null]
        ]
        assert data.a.b.sum() == 3

        data = [
                [a: [b: [c:1]]],
                [a: [b: null]],
                [a: [b: [c:2]]]
        ]
        assert data.a.b.c.sum() == 3
    }

    void testOriginalCase() {
        // initialize structure
        def root = new Root()
        for (i in 0..2) {
            def level1 = new Level1()
            level1.level2 = new Level2()
            level1.level2.level3 = i
            root.level1 << level1
        }

        // given
        assert root.level1[0].level2.level3 == 0
        assert root.level1[1].level2.level3 == 1
        assert root.level1[2].level2.level3 == 2

        // then
        assert root?.level1?.level2?.level3.sum() == 3

        // but now we have a null property in between
        root.level1[0].level2 = null

        // even with this intermediary null node, we should still get 3
        assert root?.level1?.level2?.level3.sum() == 3
    }
}

class Root {
  List level1 = []
}

class Level2 {
  Integer level3
}

class Level1 {
  Level2 level2
}
