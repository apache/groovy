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

}
