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

/**
 * Integer NumberMath operations
 * 
 * @author Steve Goetze
 */
public class IntegerMath extends NumberMath {

	public static final IntegerMath INSTANCE = new IntegerMath();

	private IntegerMath() {}
					
	protected Number absImpl(Number number) {
		return Integer.valueOf(Math.abs(number.intValue()));
	}
	
	public Number addImpl(Number left, Number right) {
		return Integer.valueOf(left.intValue() + right.intValue());
	}

	public Number subtractImpl(Number left, Number right) {
		return Integer.valueOf(left.intValue() - right.intValue());
	}

	public Number multiplyImpl(Number left, Number right) {
		return Integer.valueOf(left.intValue() * right.intValue());
	}

	public Number divideImpl(Number left, Number right) {
		return BigDecimalMath.INSTANCE.divideImpl(left, right);
	}
	
	public int compareToImpl(Number left, Number right) {
		int leftVal = left.intValue();
		int rightVal = right.intValue();
		return (leftVal<rightVal ? -1 : (leftVal==rightVal ? 0 : 1));
	}

    protected Number orImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() | right.intValue());
    }

    protected Number andImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() & right.intValue());
    }

    protected Number xorImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() ^ right.intValue());
    }

    protected Number intdivImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() / right.intValue());
    }
	
    protected Number modImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() % right.intValue());
    }

    protected Number unaryMinusImpl(Number left) {
        return Integer.valueOf(-left.intValue());
    }

    protected Number bitwiseNegateImpl(Number left) {
        return Integer.valueOf(~left.intValue());
    }

    protected Number leftShiftImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() << right.intValue());
    }

    protected Number rightShiftImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() >> right.intValue());
    }

    protected Number rightShiftUnsignedImpl(Number left, Number right) {
        return Integer.valueOf(left.intValue() >>> right.intValue());
    }
}
