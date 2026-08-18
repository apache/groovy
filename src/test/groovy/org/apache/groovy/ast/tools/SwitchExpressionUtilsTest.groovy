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
package org.apache.groovy.ast.tools

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.junit.jupiter.api.Test

import java.time.Month

/**
 * Classification of switch-expression labels for intrinsic vs {@code isCase} dispatch.
 */
final class SwitchExpressionUtilsTest {

    @Test
    void emptyOrNullCasesAreNotOptimized() {
        assert !SwitchExpressionUtils.isOptimizedSwitch(ClassHelper.int_TYPE, [])
        assert !SwitchExpressionUtils.isOptimizedSwitch(ClassHelper.int_TYPE, null)
        assert !SwitchExpressionUtils.isOptimizedSwitch(null, [intCase(1)])
    }

    @Test
    void allIntConstantsOnIntegralSelectorAreOptimized() {
        assert SwitchExpressionUtils.isOptimizedIntSwitch(ClassHelper.int_TYPE, [intCase(1), intCase(2)])
        assert SwitchExpressionUtils.isOptimizedIntSwitch(ClassHelper.Integer_TYPE, [intCase((byte) 1)])
        assert SwitchExpressionUtils.isOptimizedSwitch(ClassHelper.char_TYPE, [charCase((char) 'A')])
    }

    @Test
    void mixedIntAndRangeIsNotOptimized() {
        def mixed = [
                intCase(1),
                new CaseStatement(new RangeExpression(GeneralUtils.constX(300), GeneralUtils.constX(400), true), EmptyStatement.INSTANCE)
        ]
        assert !SwitchExpressionUtils.isOptimizedSwitch(ClassHelper.int_TYPE, mixed)
    }

    @Test
    void stringConstantsOnStringSelectorAreOptimized() {
        assert SwitchExpressionUtils.isOptimizedStringSwitch(ClassHelper.STRING_TYPE, [stringCase('Foo')])
        assert !SwitchExpressionUtils.isOptimizedStringSwitch(ClassHelper.OBJECT_TYPE, [stringCase('Foo')])
        assert !SwitchExpressionUtils.isOptimizedStringSwitch(ClassHelper.STRING_TYPE, [intCase(1)])
    }

    @Test
    void enumConstantsOnEnumSelectorAreOptimized() {
        def enumType = ClassHelper.make(Month)
        def label = GeneralUtils.propX(GeneralUtils.classX(enumType), 'JANUARY')
        def cases = [new CaseStatement(label, EmptyStatement.INSTANCE)]
        assert SwitchExpressionUtils.isOptimizedEnumSwitch(enumType, cases)
        assert SwitchExpressionUtils.enumConstantName(label, enumType) == 'JANUARY'
        assert SwitchExpressionUtils.enumConstantName(new ConstantExpression(Month.JANUARY), enumType) == 'JANUARY'
        assert SwitchExpressionUtils.enumConstantName(intCase(1).expression, enumType) == null
    }

    @Test
    void extractorsRejectNonConstants() {
        assert SwitchExpressionUtils.intConstant(GeneralUtils.constX(1)) == 1
        assert SwitchExpressionUtils.intConstant(GeneralUtils.constX((short) 2)) == 2
        assert SwitchExpressionUtils.intConstant(GeneralUtils.constX((char) 'A')) == (int) 'A'
        assert SwitchExpressionUtils.intConstant(GeneralUtils.constX('x')) == null
        assert SwitchExpressionUtils.stringConstant(GeneralUtils.constX('x')) == 'x'
        assert SwitchExpressionUtils.stringConstant(GeneralUtils.constX(1)) == null
        assert SwitchExpressionUtils.unwrapEnumType(ClassHelper.int_TYPE) == null
        assert SwitchExpressionUtils.unwrapEnumType(ClassHelper.make(Month)).isEnum()
    }

    private static CaseStatement intCase(final Object value) {
        new CaseStatement(GeneralUtils.constX(value), EmptyStatement.INSTANCE)
    }

    private static CaseStatement charCase(final char value) {
        new CaseStatement(GeneralUtils.constX(value), EmptyStatement.INSTANCE)
    }

    private static CaseStatement stringCase(final String value) {
        new CaseStatement(GeneralUtils.constX(value), EmptyStatement.INSTANCE)
    }
}
