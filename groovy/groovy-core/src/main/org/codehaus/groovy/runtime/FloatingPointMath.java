package org.codehaus.groovy.runtime;

/**
 * FloatingPoint (Double and Float) NumberMath operations
 * 
 * @author Steve Goetze
 */
public class FloatingPointMath extends NumberMath {

	protected static FloatingPointMath instance = new FloatingPointMath();
	
	private FloatingPointMath() {}
				
	protected Number absImpl(Number number) {
		return new Double(Math.abs(number.doubleValue()));
	}
	
	protected Number addImpl(Number left, Number right) {
		return new Double(left.doubleValue() + right.doubleValue());
	}

	protected Number subtractImpl(Number left, Number right) {
		return new Double(left.doubleValue() - right.doubleValue());
	}

	protected Number multiplyImpl(Number left, Number right) {
		return new Double(left.doubleValue() * right.doubleValue());
	}

	protected Number divideImpl(Number left, Number right) {
		return new Double(left.doubleValue() / right.doubleValue());
	}
	protected int compareToImpl(Number left, Number right) {
		double diff = left.doubleValue() - right.doubleValue();
		if (diff == 0) {
			return 0;
		}
		else {
			return (diff > 0) ? 1 : -1;
		}
	}
}
