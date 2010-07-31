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
 * FloatingPoint (Double and Float) NumberMath operations
 * 
 * @author Steve Goetze
 */
public final class FloatingPointMath extends NumberMath {

    public static final FloatingPointMath INSTANCE = new FloatingPointMath();
    
    private FloatingPointMath() {}
                
    protected Number absImpl(Number number) {
        return new Double(Math.abs(number.doubleValue()));
    }
    
    public Number addImpl(Number left, Number right) {
        return new Double(left.doubleValue() + right.doubleValue());
    }

    public Number subtractImpl(Number left, Number right) {
        return new Double(left.doubleValue() - right.doubleValue());
    }

    public Number multiplyImpl(Number left, Number right) {
        return new Double(left.doubleValue() * right.doubleValue());
    }

    public Number divideImpl(Number left, Number right) {
        return new Double(left.doubleValue() / right.doubleValue());
    }

    public int compareToImpl(Number left, Number right) {
        return Double.compare(left.doubleValue(), right.doubleValue());
    }
    
    protected Number modImpl(Number left, Number right) {
        return new Double(left.doubleValue() % right.doubleValue());
    }
    
    protected Number unaryMinusImpl(Number left) {
        return new Double(-left.doubleValue());
    }
}
