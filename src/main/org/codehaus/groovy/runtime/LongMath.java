/*
 * Created on Mar 5, 2004
 *
 */
package org.codehaus.groovy.runtime;

/**
 * Long NumberMath operations
 * 
 * @author Steve Goetze
 */
public class LongMath extends NumberMath {

	protected static LongMath instance = new LongMath();

	private LongMath() {}
					
	protected Number absImpl(Number number) {
		return new Long(Math.abs(number.longValue()));
	}
	
	protected Number addImpl(Number left, Number right) {
		return new Long(left.longValue() + right.longValue());
	}

	protected Number subtractImpl(Number left, Number right) {
		return new Long(left.longValue() - right.longValue());
	}

	protected Number multiplyImpl(Number left, Number right) {
		return new Long(left.longValue() * right.longValue());
	}

	protected Number divideImpl(Number left, Number right) {
		return BigDecimalMath.instance.divideImpl(left, right);
	}
	
	protected int compareToImpl(Number left, Number right) {
		long leftVal = left.longValue();
		long rightVal = right.longValue();
		return (leftVal<rightVal ? -1 : (leftVal==rightVal ? 0 : 1));
	}

	protected Number intdivImpl(Number left, Number right) {
        return new Long(left.longValue() / right.longValue());
	}
	
    protected Number modImpl(Number left, Number right) {
        return new Long(left.longValue() % right.longValue());
    }
    
    protected Number negateImpl(Number left) {
        return new Long(-left.longValue());
    }
    
    protected Number bitNegateImpl(Number left) {
        return new Long(~left.longValue());
    }
    
    protected Number orImpl(Number left, Number right) {
        return new Long(left.longValue() | right.longValue());
    }

    protected Number andImpl(Number left, Number right) {
        return new Long(left.longValue() & right.longValue());
    }
    
    protected Number xorImpl(Number left, Number right) {
        return new Long(left.longValue() ^ right.longValue());
    }
    
    protected Number leftShiftImpl(Number left, Number right) {
        return new Long(left.longValue() << right.longValue());
    }

    protected Number rightShiftImpl(Number left, Number right) {
        return new Long(left.longValue() >> right.longValue());
    }

    protected Number rightShiftUnsignedImpl(Number left, Number right) {
        return new Long(left.longValue() >>> right.longValue());
    }

    protected Number bitAndImpl(Number left, Number right) {
        return new Long(left.longValue() & right.longValue());
    }
}
