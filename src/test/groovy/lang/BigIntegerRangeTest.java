/*
 * Copyright 2003-2013 the original author or authors.
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
package groovy.lang;

import java.math.BigInteger;

/**
 * Tests {@link ObjectRange}s of {@link BigInteger}s.
 *
 * @author Edwin Tellman
 */
public class BigIntegerRangeTest extends NumberRangeTest {

    /**
     * {@inheritDoc}
     */
    protected Range createRange(int from, int to) {
        return new ObjectRange(BigInteger.valueOf(from), BigInteger.valueOf(to));
    }

    /**
     * {@inheritDoc}
     */
    protected Comparable createValue(int value) {
        return BigInteger.valueOf(value);
    }

}
