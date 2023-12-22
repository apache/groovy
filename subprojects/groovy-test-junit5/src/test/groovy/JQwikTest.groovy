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
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import net.jqwik.api.constraints.Size
import net.jqwik.api.constraints.UniqueElements

class JQwikTest {
    @Property
    def uniqueInList(@ForAll @Size(5) @UniqueElements List<@IntRange(min = 0, max = 10) Integer> aList) {
        assert aList.size() == aList.toSet().size()
        assert aList.every{ anInt -> anInt >= 0 && anInt <= 10 }
    }

    @Property(tries=10)
    boolean 'only zero is the same as the negative of itself'(@ForAll int i) {
        i == 0 ==> i == -i
    }

    @CompileStatic
    @Property(tries=10)
    boolean 'only zero is the same as the negative of itself - CS'(@ForAll int i) {
        i == 0 ==> i == -i
    }

    @Property(tries=100)
    boolean 'an odd number squared is still odd'(@ForAll int n) {
        n % 2 == 1 ==> (n ** 2) % 2 == 1
    }

    @CompileStatic
    @Property(tries=100)
    boolean 'an odd number squared is still odd - CS'(@ForAll int n) {
        n % 2 == 1 ==> (n ** 2) % 2 == 1
    }

    @Property(tries=100)
    boolean 'abs of a positive integer is itself'(@ForAll int i) {
        i >= 0 ==> i.abs() == i
    }

    @CompileStatic
    @Property(tries=100)
    boolean 'abs of a positive integer is itself - CS'(@ForAll int i) {
        i >= 0 ==> i.abs() == i
    }

    @Property(tries=100)
    boolean 'abs of a positive integer is itself alternative'(@ForAll @IntRange(min = 0, max = 10000) int i) {
        i.abs() == i
    }
}
