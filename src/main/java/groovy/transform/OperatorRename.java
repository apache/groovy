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
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.OperatorRenameASTTransformation")
public @interface OperatorRename {
    String plus() default Undefined.STRING;
    String minus() default Undefined.STRING;
    String multiply() default Undefined.STRING;
    String div() default Undefined.STRING;
    String remainder() default Undefined.STRING;
    String power() default Undefined.STRING;
    String leftShift() default Undefined.STRING;
    String rightShift() default Undefined.STRING;
    String rightShiftUnsigned() default Undefined.STRING;
    String and() default Undefined.STRING;
    String or() default Undefined.STRING;
    String xor() default Undefined.STRING;
    String compareTo() default Undefined.STRING;
}
