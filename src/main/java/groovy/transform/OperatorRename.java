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
package groovy.transform;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows renaming of Groovy's operator methods. This can be useful for using Groovy's
 * operator overloading with libraries designed with different method names. As an example,
 * here is using the Commons Numbers Fraction library. This normally has an "add" method,
 * but we can use the "+" operator using this transform.
 * <pre>
 * &#64;OperatorRename(plus="add")
 * def testAddOfTwoFractions() {
 *     var half = Fraction.of(1, 2)
 *     var third = Fraction.of(1, 3)
 *     assert half + third == Fraction.of(5, 6)
 * }
 * </pre>
 *
 * @since 5.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.OperatorRenameASTTransformation")
public @interface OperatorRename {
    /**
     * Returns the replacement method name for the {@code +} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String plus() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code -} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String minus() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code *} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String multiply() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code /} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String div() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code %} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String remainder() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code **} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String power() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code <<} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String leftShift() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code >>} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String rightShift() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code >>>} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String rightShiftUnsigned() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code &} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String and() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code |} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String or() default Undefined.STRING;

    /**
     * Returns the replacement method name for the {@code ^} operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String xor() default Undefined.STRING;

    /**
     * Returns the replacement method name for the spaceship operator.
     * Defaults to {@link Undefined#STRING}, meaning no rename.
     *
     * @return the replacement method name
     */
    String compareTo() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code +=}. */
    String plusAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code -=}. */
    String minusAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code *=}. */
    String multiplyAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code /=}. */
    String divAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code %=}. */
    String remainderAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code **=}. */
    String powerAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code <<=}. */
    String leftShiftAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code >>=}. */
    String rightShiftAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code >>>=}. */
    String rightShiftUnsignedAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code &=}. */
    String andAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code |=}. */
    String orAssign() default Undefined.STRING;
    /** GEP-15: rename the dedicated compound-assignment method for {@code ^=}. */
    String xorAssign() default Undefined.STRING;
}
