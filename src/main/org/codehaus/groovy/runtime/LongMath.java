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
		long diff = left.longValue() - right.longValue();
		if (diff == 0) {
			return 0;
		}
		else {
			return (diff > 0) ? 1 : -1;
		}
	}

}
