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
package lang

import org.junit.Test

class TupleFactoryMethodTest {

    @Test
    void tuple0() {
        var list = []
        var t = Tuple.tuple(*list)
        assert list.size() == t.size()
        var t2 = t.clone()
        assert list.size() == t2.size()
    }

    @Test
    void tuple1() {
        var list = [1]
        var t = Tuple.tuple(*list)
        assert list == [t.v1]
        var t2 = t.clone()
        assert list == [t2.v1]
    }

    @Test
    void tuple2() {
        var list = [1, 2]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2]
    }

    @Test
    void tuple3() {
        var list = [1, 2, 3]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3]
    }

    @Test
    void tuple4() {
        var list = [1, 2, 3, 4]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4]
    }

    @Test
    void tuple5() {
        var list = [1, 2, 3, 4, 5]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5]
    }

    @Test
    void tuple6() {
        var list = [1, 2, 3, 4, 5, 6]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6]
    }

    @Test
    void tuple7() {
        var list = [1, 2, 3, 4, 5, 6, 7]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7]
    }

    @Test
    void tuple8() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8]
    }

    @Test
    void tuple9() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9]
    }

    @Test
    void tuple10() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10]
    }

    @Test
    void tuple11() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11]
    }

    @Test
    void tuple12() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11, t.v12]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11, t2.v12]
    }

    @Test
    void tuple13() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11, t.v12, t.v13]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11, t2.v12, t2.v13]
    }

    @Test
    void tuple14() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11, t.v12, t.v13, t.v14]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11, t2.v12, t2.v13, t2.v14]
    }

    @Test
    void tuple15() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11, t.v12, t.v13, t.v14, t.v15]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11, t2.v12, t2.v13, t2.v14, t2.v15]
    }

    @Test
    void tuple16() {
        var list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
        var t = Tuple.tuple(*list)
        assert list == [t.v1, t.v2, t.v3, t.v4, t.v5, t.v6, t.v7, t.v8, t.v9, t.v10, t.v11, t.v12, t.v13, t.v14, t.v15, t.v16]
        var t2 = t.clone()
        assert list == [t2.v1, t2.v2, t2.v3, t2.v4, t2.v5, t2.v6, t2.v7, t2.v8, t2.v9, t2.v10, t2.v11, t2.v12, t2.v13, t2.v14, t2.v15, t2.v16]
    }

}
