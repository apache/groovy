/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gls.enums.vm5

/**
 * Tests various properties of enums.
 *
 * @author Paul King
 */
class EnumTest extends GroovyTestCase {

    void testValues() {
        assert UsCoin.values().size() == 4
        assert UsCoin.values().toList().sum{ it.value } == 41
    }

    void testNext() {
        def coin = UsCoin.penny
        def coins = [coin++, coin++, coin++, coin++, coin]
        assert coins == [UsCoin.penny, UsCoin.nickel, UsCoin.dime, UsCoin.quarter, UsCoin.penny]
    }

    void testPrevious() {
        def coin = UsCoin.quarter
        def coins = [coin--, coin--, coin--, coin--, coin]
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel, UsCoin.penny, UsCoin.quarter]
    }

    void testRange() {
        def coinRange1 = UsCoin.penny..UsCoin.dime
        assert (UsCoin.nickel in coinRange1)
        assert !(UsCoin.quarter in coinRange1)
    }

    void testMinValue() {
        assert UsCoin.MIN_VALUE == UsCoin.penny
        shouldFail(MissingPropertyException) {
           EmptyEnum.MIN_VALUE
        }
    }

    void testMaxValue() {
        assert UsCoin.MAX_VALUE == UsCoin.quarter
        shouldFail(MissingPropertyException) {
           EmptyEnum.MAX_VALUE
        }
    }

    void testComparators() {
        assert UsCoin.nickel <=> UsCoin.penny  ==  1
        assert UsCoin.nickel <=> UsCoin.nickel ==  0
        assert UsCoin.nickel <=> UsCoin.dime   == -1
        assert UsCoin.nickel <=  UsCoin.nickel
        assert UsCoin.nickel <=  UsCoin.dime
        assert UsCoin.nickel >=  UsCoin.penny
        assert UsCoin.nickel >=  UsCoin.nickel
    }

    void testStepWithRange() {
        def coinRange2 = UsCoin.nickel..UsCoin.quarter
        def coins = coinRange2.toList()
        assert coins == [UsCoin.nickel, UsCoin.dime, UsCoin.quarter]
        coins = coinRange2.step(2)
        assert coins == [UsCoin.nickel, UsCoin.quarter]
        coins = coinRange2.step(3)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(4)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(-1)
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel]
        coins = coinRange2.step(-2)
        assert coins == [UsCoin.quarter, UsCoin.nickel]
        coins = coinRange2.step(-3)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(-4)
        assert coins == [UsCoin.quarter]
    }

    void testStepWithReverseRange() {
        def coinRange2 = UsCoin.quarter..UsCoin.nickel
        def coins = coinRange2.toList()
        assert coins == [UsCoin.quarter, UsCoin.dime, UsCoin.nickel]
        coins = coinRange2.step(2)
        assert coins == [UsCoin.quarter, UsCoin.nickel]
        coins = coinRange2.step(3)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(4)
        assert coins == [UsCoin.quarter]
        coins = coinRange2.step(-1)
        assert coins == [UsCoin.nickel, UsCoin.dime, UsCoin.quarter]
        coins = coinRange2.step(-2)
        assert coins == [UsCoin.nickel, UsCoin.quarter]
        coins = coinRange2.step(-3)
        assert coins == [UsCoin.nickel]
        coins = coinRange2.step(-4)
        assert coins == [UsCoin.nickel]
    }

}

enum UsCoin {
    penny(1), nickel(5), dime(10), quarter(25)
    UsCoin(int value) { this.value = value }
    private final int value
    int getValue() { value }
}

enum EmptyEnum{}