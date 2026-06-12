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

final class Groovy3238 {

    @Test
    void testRelativeExactnessToMatchForBigIntegerParam() {
        def obj = new Groovy3238()
        def bi = new BigInteger('1')

        Groovy3238.metaClass.m = {Double val -> 'Double'}; obj.metaClass = null
        assert obj.m(bi) == 'Double'
        Groovy3238.metaClass.m = {double val -> 'double'}; obj.metaClass = null
        assert obj.m(bi) == 'double' //double should be chosen over Double
        Groovy3238.metaClass.m = {BigDecimal val -> 'BigDecimal'}; obj.metaClass = null
        assert obj.m(bi) == 'BigDecimal' //BigDecimal should be chosen over Double, double
        Groovy3238.metaClass.m = {Object val -> 'Object'}; obj.metaClass = null
        assert obj.m(bi) == 'Object' //Object should be chosen over Double, double, BigDecimal
        Groovy3238.metaClass.m = {Number val -> 'Number'}; obj.metaClass = null
        assert obj.m(bi) == 'Number' //Number should be chosen over Double, double, BigDecimal, Object
        Groovy3238.metaClass.m = {BigInteger val -> 'BigInteger'}; obj.metaClass = null
        assert obj.m(bi) == 'BigInteger' //BigInteger should be chosen over Double, double, BigDecimal, Object, Number
    }
}
