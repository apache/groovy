/*
 * Created on Mar 5, 2004
 *
 */
package org.codehaus.groovy.runtime;

/**
 * Integer NumberMath operations
 * 
 * @author Steve Goetze
 */
public class IntegerMath extends NumberMath {

	protected static IntegerMath instance = new IntegerMath();

	private IntegerMath() {}
					
	protected Number absImpl(Number number) {
		return new Integer(Math.abs(number.intValue()));
	}
	
	protected Number addImpl(Number left, Number right) {
		return new Integer(left.intValue() + right.intValue());
	}

	protected Number subtractImpl(Number left, Number right) {
		return new Integer(left.intValue() - right.intValue());
	}

	protected Number multiplyImpl(Number left, Number right) {
		return new Integer(left.intValue() * right.intValue());
	}

	protected Number divideImpl(Number left, Number right) {
		return BigDecimalMath.instance.divideImpl(left, right);
	}
	
	protected int compareToImpl(Number left, Number right) {
		int leftVal = left.intValue();
		int rightVal = right.intValue();
		return (leftVal<rightVal ? -1 : (leftVal==rightVal ? 0 : 1));
	}

    protected Number orImpl(Number left, Number right) {
        return new Integer(left.intValue() | right.intValue());
    }

    protected Number andImpl(Number left, Number right) {
        return new Integer(left.intValue() & right.intValue());
    }

    protected Number xorImpl(Number left, Number right) {
        return new Integer(left.intValue() ^ right.intValue());
    }

    protected Number intdivImpl(Number left, Number right) {
        return new Integer(left.intValue() / right.intValue());
    }
	
    protected Number modImpl(Number left, Number right) {
        return new Integer(left.intValue() % right.intValue());
    }

    protected Number negateImpl(Number left) {
        return new Integer(-left.intValue());
    }

    protected Number bitNegateImpl(Number left) {
        return new Integer(~left.intValue());
    }

    protected Number leftShiftImpl(Number left, Number right) {
        return new Integer(left.intValue() << right.intValue());
    }

    protected Number rightShiftImpl(Number left, Number right) {
        return new Integer(left.intValue() >> right.intValue());
    }

    protected Number rightShiftUnsignedImpl(Number left, Number right) {
        return new Integer(left.intValue() >>> right.intValue());
    }
}
