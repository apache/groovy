package org.codehaus.groovy.runtime;

/**
 * BigInteger NumberMath operations
 * 
 * @author Steve Goetze
 */
public class BigIntegerMath extends NumberMath {

	protected static BigIntegerMath instance = new BigIntegerMath();
	
	private BigIntegerMath() {}

	protected Number absImpl(Number number) {
		return toBigInteger(number).abs();
	}
	
	protected Number addImpl(Number left, Number right) {
		return toBigInteger(left).add(toBigInteger(right));
	}

	protected Number subtractImpl(Number left, Number right) {
		return toBigInteger(left).subtract(toBigInteger(right));
	}

	protected Number multiplyImpl(Number left, Number right) {
		return toBigInteger(left).multiply(toBigInteger(right));
	}

	protected Number divideImpl(Number left, Number right) {
		return BigDecimalMath.instance.divideImpl(left, right);
	}
	
	protected int compareToImpl(Number left, Number right) {
		return toBigInteger(left).compareTo(toBigInteger(right));
	}

    protected Number andImpl(Number left, Number right) {
        return toBigInteger(left).and(toBigInteger(right));
    }
    
    protected Number intdivImpl(Number left, Number right) {
        return toBigInteger(left).divide(toBigInteger(right));
    }
    
    protected Number modImpl(Number left, Number right) {
        return toBigInteger(left).mod(toBigInteger(right));
    }
    
    protected Number negateImpl(Number left) {
        return toBigInteger(left).negate();
    }

    protected Number orImpl(Number left, Number right) {
        return toBigInteger(left).or(toBigInteger(right));
    }
}
