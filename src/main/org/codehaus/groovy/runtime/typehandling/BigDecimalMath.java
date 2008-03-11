/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.runtime.typehandling;

import java.math.BigDecimal;

/**
 * BigDecimal NumberMath operations
 * 
 * @author Steve Goetze
 */
public class BigDecimalMath extends NumberMath {

	//This is an arbitrary value, picked as a reasonable choice for a rounding point
	//for typical user math.
	public static final int MAX_DIVISION_SCALE = 10;
	
	public static final BigDecimalMath INSTANCE = new BigDecimalMath();
	
	private BigDecimalMath() {}

	protected Number absImpl(Number number) {
		return toBigDecimal(number).abs();
	}
	
	public Number addImpl(Number left, Number right) {
		return toBigDecimal(left).add(toBigDecimal(right));
	}

	public Number subtractImpl(Number left, Number right) {
		return toBigDecimal(left).subtract(toBigDecimal(right));
	}

	public Number multiplyImpl(Number left, Number right) {
		return toBigDecimal(left).multiply(toBigDecimal(right));
	}

	public Number divideImpl(Number left, Number right) {
		//Hack until Java 1.5 BigDecimal is available.  For now, pick
		//a result scale which is the maximum of the scale of the
		//two operands and an arbitrary maximum (similar to what a
		//handheld calculator would do).  Then, normalize the result
		//by removing any trailing zeros.
		BigDecimal bigLeft = toBigDecimal(left);
		BigDecimal bigRight = toBigDecimal(right);
		int scale = Math.max(bigLeft.scale(), bigRight.scale());
		return normalize(bigLeft.divide(bigRight, Math.max(scale, MAX_DIVISION_SCALE), BigDecimal.ROUND_HALF_UP));
	}
	
	public int compareToImpl(Number left, Number right) {
		return toBigDecimal(left).compareTo(toBigDecimal(right));
	}
	
	private BigDecimal normalize(BigDecimal number) {
        // we have to take care of the case number==0, because 0 can have every
        // scale and the test in the while loop would never end
        if (number.signum()==0) {
            // the smallest scale for 0 is 0
            return number.setScale(0);
        }
        // rescale until we found the smallest possible scale
		try {
			while (true) {
				number = number.setScale(number.scale()-1);
			} 
		} catch (ArithmeticException e) {
			return number;
		}
	}

    protected Number unaryMinusImpl(Number left) {
        return toBigDecimal(left).negate();
    }
}
