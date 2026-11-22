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

import java.sql.Timestamp
import java.util.Date

/**
 * GROOVY-5717: {@code java.sql.Date == java.sql.Timestamp} used to be
 * false even when {@code equals} was true and {@code compareTo} was 0,
 * because the two types are siblings and the equality check did not
 * fall back to their common {@link Date} superclass.
 */
final class Groovy5717 {

    @Test
    void testSqlDateEqualsAndCompareEqualToTimestamp() {
        def s = new java.sql.Date(112, 8, 20)
        def t = new Timestamp(s.time)
        assert s.equals(t)
        assert s == t
    }
}
