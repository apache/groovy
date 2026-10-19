/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.groovyfx.extensions;

import groovy.lang.Closure;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import org.apache.groovy.lang.annotation.Incubating;

/**
 * Extension methods that let JavaFX observable values take part in Groovy
 * operators and closures.
 * <p>
 * JavaFX names its fluent binding methods {@code add}, {@code subtract},
 * {@code divide}, {@code negate} and {@code concat}; Groovy's operators look
 * for {@code plus}, {@code minus}, {@code div}, {@code negative} and
 * {@code plus}. The arithmetic methods here supply the missing names so that
 * {@code width * 0.5 + 10} on a {@code DoubleProperty} yields a live
 * {@code NumberBinding} rather than a {@code MissingMethodException}
 * ({@code *} and {@code &} already work because JavaFX uses the same names
 * as Groovy for {@code multiply} and {@code and}).
 * <p>
 * A constant operand selects the JavaFX overload for its type: {@code int}
 * for an {@code Integer}, {@code Short} or {@code Byte}, {@code long} for a
 * {@code Long}, {@code float} for a {@code Float}, and {@code double} for any
 * other {@code Number}. Groovy's decimal literals are {@code BigDecimal}, so
 * {@code width * 0.5} is computed in {@code double} precision, as is
 * arithmetic with a {@code BigInteger}; JavaFX has no overloads for either.
 * <p>
 * Note that comparison operators cannot produce bindings: Groovy routes
 * {@code <} and {@code >} through {@code compareTo}, so use
 * {@code greaterThan(...)} and friends for a {@code BooleanBinding}; and
 * {@code !property} evaluates Groovy truth on the property object rather than
 * calling {@code not()}, so use {@code property.not()} for a live negation.
 */
@Incubating
public class FXExtensions {

    private FXExtensions() {
    }

    // ---- arithmetic with the observable on the left -------------------------------------------------

    /**
     * Adds two observable numbers, as a live binding.
     *
     * @param self  the left operand
     * @param other the right operand
     * @return a binding of the sum
     */
    public static NumberBinding plus(NumberExpression self, ObservableNumberValue other) {
        return self.add(other);
    }

    /**
     * Adds a constant to an observable number, as a live binding.
     *
     * @param self  the observable operand
     * @param other the constant
     * @return a binding of the sum
     */
    public static NumberBinding plus(NumberExpression self, Number other) {
        if (isIntegral(other)) return self.add(other.intValue());
        if (other instanceof Long) return self.add(other.longValue());
        if (other instanceof Float) return self.add(other.floatValue());
        return self.add(other.doubleValue());
    }

    /**
     * Subtracts one observable number from another, as a live binding.
     *
     * @param self  the left operand
     * @param other the right operand
     * @return a binding of the difference
     */
    public static NumberBinding minus(NumberExpression self, ObservableNumberValue other) {
        return self.subtract(other);
    }

    /**
     * Subtracts a constant from an observable number, as a live binding.
     *
     * @param self  the observable operand
     * @param other the constant
     * @return a binding of the difference
     */
    public static NumberBinding minus(NumberExpression self, Number other) {
        if (isIntegral(other)) return self.subtract(other.intValue());
        if (other instanceof Long) return self.subtract(other.longValue());
        if (other instanceof Float) return self.subtract(other.floatValue());
        return self.subtract(other.doubleValue());
    }

    /**
     * Divides one observable number by another, as a live binding.
     *
     * @param self  the dividend
     * @param other the divisor
     * @return a binding of the quotient
     */
    public static NumberBinding div(NumberExpression self, ObservableNumberValue other) {
        return self.divide(other);
    }

    /**
     * Divides an observable number by a constant, as a live binding.
     *
     * @param self  the dividend
     * @param other the constant divisor
     * @return a binding of the quotient
     */
    public static NumberBinding div(NumberExpression self, Number other) {
        if (isIntegral(other)) return self.divide(other.intValue());
        if (other instanceof Long) return self.divide(other.longValue());
        if (other instanceof Float) return self.divide(other.floatValue());
        return self.divide(other.doubleValue());
    }

    /**
     * Negates an observable number, as a live binding; backs the unary minus operator.
     *
     * @param self the operand
     * @return a binding of the negated value
     */
    public static NumberBinding negative(NumberExpression self) {
        return self.negate();
    }

    // ---- arithmetic with the constant on the left ---------------------------------------------------

    /**
     * Adds an observable number to a constant, as a live binding.
     *
     * @param self  the constant
     * @param other the observable operand
     * @return a binding of the sum
     */
    public static NumberBinding plus(Number self, ObservableNumberValue other) {
        if (isIntegral(self)) return Bindings.add(self.intValue(), other);
        if (self instanceof Long) return Bindings.add(self.longValue(), other);
        if (self instanceof Float) return Bindings.add(self.floatValue(), other);
        return Bindings.add(self.doubleValue(), other);
    }

    /**
     * Subtracts an observable number from a constant, as a live binding.
     *
     * @param self  the constant
     * @param other the observable operand
     * @return a binding of the difference
     */
    public static NumberBinding minus(Number self, ObservableNumberValue other) {
        if (isIntegral(self)) return Bindings.subtract(self.intValue(), other);
        if (self instanceof Long) return Bindings.subtract(self.longValue(), other);
        if (self instanceof Float) return Bindings.subtract(self.floatValue(), other);
        return Bindings.subtract(self.doubleValue(), other);
    }

    /**
     * Multiplies a constant by an observable number, as a live binding.
     *
     * @param self  the constant
     * @param other the observable operand
     * @return a binding of the product
     */
    public static NumberBinding multiply(Number self, ObservableNumberValue other) {
        if (isIntegral(self)) return Bindings.multiply(self.intValue(), other);
        if (self instanceof Long) return Bindings.multiply(self.longValue(), other);
        if (self instanceof Float) return Bindings.multiply(self.floatValue(), other);
        return Bindings.multiply(self.doubleValue(), other);
    }

    /**
     * Divides a constant by an observable number, as a live binding.
     *
     * @param self  the constant dividend
     * @param other the observable divisor
     * @return a binding of the quotient
     */
    public static NumberBinding div(Number self, ObservableNumberValue other) {
        if (isIntegral(self)) return Bindings.divide(self.intValue(), other);
        if (self instanceof Long) return Bindings.divide(self.longValue(), other);
        if (self instanceof Float) return Bindings.divide(self.floatValue(), other);
        return Bindings.divide(self.doubleValue(), other);
    }

    // ---- strings -----------------------------------------------------------------------------------

    /**
     * Concatenates a value onto an observable string, as a live binding.
     *
     * @param self  the observable string
     * @param other the value to append; observable values are tracked
     * @return a binding of the concatenation
     */
    public static StringExpression plus(StringExpression self, Object other) {
        return self.concat(other);
    }

    /**
     * Concatenates an observable value onto a string, as a live binding.
     *
     * @param self  the prefix
     * @param other the observable value to append
     * @return a binding of the concatenation
     */
    public static StringExpression plus(String self, ObservableValue<?> other) {
        return Bindings.concat(self, other);
    }

    // ---- listeners ---------------------------------------------------------------------------------

    /**
     * Registers a change listener backed by a closure.
     * <p>
     * The closure may take one parameter (the new value), two (the old and
     * new values) or three (the observable, the old value and the new value).
     *
     * @param self   the observable value
     * @param action the closure to call on each change
     * @param <T>    the value type
     * @return the registered listener, for later removal
     */
    public static <T> ChangeListener<T> onChange(ObservableValue<T> self, Closure<?> action) {
        int arity = action.getMaximumNumberOfParameters();
        ChangeListener<T> listener = (observable, oldValue, newValue) -> {
            if (arity <= 1) {
                action.call(newValue);
            } else if (arity == 2) {
                action.call(oldValue, newValue);
            } else {
                action.call(observable, oldValue, newValue);
            }
        };
        self.addListener(listener);
        return listener;
    }

    /**
     * Registers an invalidation listener backed by a closure.
     * <p>
     * The closure may take no parameters or one (the observable).
     *
     * @param self   the observable
     * @param action the closure to call on each invalidation
     * @return the registered listener, for later removal
     */
    public static InvalidationListener onInvalidate(Observable self, Closure<?> action) {
        int arity = action.getMaximumNumberOfParameters();
        InvalidationListener listener = observable -> {
            if (arity == 0) {
                action.call();
            } else {
                action.call(observable);
            }
        };
        self.addListener(listener);
        return listener;
    }

    private static boolean isIntegral(Number number) {
        return number instanceof Integer || number instanceof Short || number instanceof Byte;
    }
}
