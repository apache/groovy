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
package bugs

import org.junit.jupiter.api.Test

/**
 * GROOVY-9969: a negative range with Long bounds used as a list subscript
 * must yield the same forward sequence as the equivalent int range
 * (historically {@code list[(2L .. -2L)]} produced a reversed sequence).
 */
final class Groovy9969 {

    @Test
    void testNegativeLongRangeListSubscriptIsNotReversed() {
        def list = [1L, 2L, 3L, 4L, 5L, 6L]
        assert list[(2L .. -2L)] == [3L, 4L, 5L]
        assert list[(2 .. -2)] == [3L, 4L, 5L]
        assert (2L .. -2L) == (2 .. -2)
    }
}
