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
package org.codehaus.groovy.syntax

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for Types class - token type classification and matching.
 */
class TypesTest {

    // ofType basic tests
    @Test
    void testOfTypeSameType() {
        assertTrue(Types.ofType(Types.PLUS, Types.PLUS))
        assertTrue(Types.ofType(Types.KEYWORD_IF, Types.KEYWORD_IF))
    }

    @Test
    void testOfTypeAny() {
        assertTrue(Types.ofType(Types.PLUS, Types.ANY))
        assertTrue(Types.ofType(Types.KEYWORD_IF, Types.ANY))
        assertTrue(Types.ofType(Types.STRING, Types.ANY))
    }

    @Test
    void testOfTypeNotEof() {
        assertTrue(Types.ofType(Types.UNKNOWN, Types.NOT_EOF))
        assertTrue(Types.ofType(Types.PLUS, Types.NOT_EOF))
        assertTrue(Types.ofType(Types.SYNTH_VARIABLE_DECLARATION, Types.NOT_EOF))
        assertFalse(Types.ofType(Types.EOF, Types.NOT_EOF))
    }

    // End of statement tests
    @Test
    void testOfTypeGeneralEndOfStatement() {
        assertTrue(Types.ofType(Types.EOF, Types.GENERAL_END_OF_STATEMENT))
        assertTrue(Types.ofType(Types.NEWLINE, Types.GENERAL_END_OF_STATEMENT))
        assertTrue(Types.ofType(Types.SEMICOLON, Types.GENERAL_END_OF_STATEMENT))
        assertFalse(Types.ofType(Types.PLUS, Types.GENERAL_END_OF_STATEMENT))
    }

    @Test
    void testOfTypeAnyEndOfStatement() {
        assertTrue(Types.ofType(Types.EOF, Types.ANY_END_OF_STATEMENT))
        assertTrue(Types.ofType(Types.NEWLINE, Types.ANY_END_OF_STATEMENT))
        assertTrue(Types.ofType(Types.SEMICOLON, Types.ANY_END_OF_STATEMENT))
        assertTrue(Types.ofType(Types.RIGHT_CURLY_BRACE, Types.ANY_END_OF_STATEMENT))
        assertFalse(Types.ofType(Types.PLUS, Types.ANY_END_OF_STATEMENT))
    }

    // Assignment operator tests
    @Test
    void testIsAssignment() {
        assertTrue(Types.isAssignment(Types.EQUAL))
        assertTrue(Types.isAssignment(Types.PLUS_EQUAL))
        assertTrue(Types.isAssignment(Types.MINUS_EQUAL))
        assertTrue(Types.isAssignment(Types.MULTIPLY_EQUAL))
        assertTrue(Types.isAssignment(Types.DIVIDE_EQUAL))
        assertTrue(Types.isAssignment(Types.MOD_EQUAL))
        assertTrue(Types.isAssignment(Types.POWER_EQUAL))
        assertTrue(Types.isAssignment(Types.ELVIS_EQUAL))
        assertTrue(Types.isAssignment(Types.LOGICAL_OR_EQUAL))
        assertTrue(Types.isAssignment(Types.LOGICAL_AND_EQUAL))
        assertTrue(Types.isAssignment(Types.LEFT_SHIFT_EQUAL))
        assertTrue(Types.isAssignment(Types.RIGHT_SHIFT_EQUAL))
        assertTrue(Types.isAssignment(Types.BITWISE_OR_EQUAL))
        assertTrue(Types.isAssignment(Types.BITWISE_AND_EQUAL))
        assertTrue(Types.isAssignment(Types.BITWISE_XOR_EQUAL))
        assertFalse(Types.isAssignment(Types.PLUS))
        assertFalse(Types.isAssignment(Types.COMPARE_EQUAL))
    }

    @Test
    void testOfTypeAssignmentOperator() {
        assertTrue(Types.ofType(Types.EQUAL, Types.ASSIGNMENT_OPERATOR))
        assertTrue(Types.ofType(Types.PLUS_EQUAL, Types.ASSIGNMENT_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.ASSIGNMENT_OPERATOR))
    }

    // Comparison operator tests
    @Test
    void testOfTypeComparisonOperator() {
        assertTrue(Types.ofType(Types.COMPARE_EQUAL, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_NOT_EQUAL, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_LESS_THAN, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_LESS_THAN_EQUAL, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_GREATER_THAN, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_GREATER_THAN_EQUAL, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_TO, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_IDENTICAL, Types.COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_NOT_IDENTICAL, Types.COMPARISON_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.COMPARISON_OPERATOR))
    }

    // Instanceof operator tests
    @Test
    void testOfTypeInstanceofOperator() {
        assertTrue(Types.ofType(Types.KEYWORD_INSTANCEOF, Types.INSTANCEOF_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_NOT_INSTANCEOF, Types.INSTANCEOF_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.INSTANCEOF_OPERATOR))
    }

    // Math operator tests
    @Test
    void testOfTypeMathOperator() {
        assertTrue(Types.ofType(Types.PLUS, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.MINUS, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.MULTIPLY, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.DIVIDE, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.MOD, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.POWER, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_OR, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_AND, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.NOT, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_OR, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_AND, Types.MATH_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_XOR, Types.MATH_OPERATOR))
        assertFalse(Types.ofType(Types.EQUAL, Types.MATH_OPERATOR))
    }

    // Logical operator tests
    @Test
    void testOfTypeLogicalOperator() {
        assertTrue(Types.ofType(Types.NOT, Types.LOGICAL_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_OR, Types.LOGICAL_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_AND, Types.LOGICAL_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.LOGICAL_OPERATOR))
    }

    // Bitwise operator tests
    @Test
    void testOfTypeBitwiseOperator() {
        assertTrue(Types.ofType(Types.BITWISE_OR, Types.BITWISE_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_AND, Types.BITWISE_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_XOR, Types.BITWISE_OPERATOR))
        assertTrue(Types.ofType(Types.BITWISE_NEGATION, Types.BITWISE_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.BITWISE_OPERATOR))
    }

    // Range operator tests
    @Test
    void testOfTypeRangeOperator() {
        assertTrue(Types.ofType(Types.DOT_DOT, Types.RANGE_OPERATOR))
        assertTrue(Types.ofType(Types.DOT_DOT_DOT, Types.RANGE_OPERATOR))
        assertFalse(Types.ofType(Types.DOT, Types.RANGE_OPERATOR))
    }

    // Regex comparison operator tests
    @Test
    void testOfTypeRegexComparisonOperator() {
        assertTrue(Types.ofType(Types.FIND_REGEX, Types.REGEX_COMPARISON_OPERATOR))
        assertTrue(Types.ofType(Types.MATCH_REGEX, Types.REGEX_COMPARISON_OPERATOR))
        assertFalse(Types.ofType(Types.EQUAL, Types.REGEX_COMPARISON_OPERATOR))
    }

    // Dereference operator tests
    @Test
    void testOfTypeDereferenceOperator() {
        assertTrue(Types.ofType(Types.DOT, Types.DEREFERENCE_OPERATOR))
        assertTrue(Types.ofType(Types.NAVIGATE, Types.DEREFERENCE_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.DEREFERENCE_OPERATOR))
    }

    // Prefix operator tests
    @Test
    void testOfTypePrefixOperator() {
        assertTrue(Types.ofType(Types.MINUS, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PLUS_PLUS, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.MINUS_MINUS, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.REGEX_PATTERN, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.NOT, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_PLUS, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_MINUS, Types.PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.SYNTH_CAST, Types.PREFIX_OPERATOR))
    }

    // Pure prefix operator tests
    @Test
    void testOfTypePurePrefixOperator() {
        assertTrue(Types.ofType(Types.REGEX_PATTERN, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.NOT, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_PLUS, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_PLUS_PLUS, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_MINUS, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_MINUS_MINUS, Types.PURE_PREFIX_OPERATOR))
        assertTrue(Types.ofType(Types.SYNTH_CAST, Types.PURE_PREFIX_OPERATOR))
        assertFalse(Types.ofType(Types.PLUS, Types.PURE_PREFIX_OPERATOR))
    }

    // Postfix operator tests
    @Test
    void testOfTypePostfixOperator() {
        assertTrue(Types.ofType(Types.PLUS_PLUS, Types.POSTFIX_OPERATOR))
        assertTrue(Types.ofType(Types.POSTFIX_PLUS_PLUS, Types.POSTFIX_OPERATOR))
        assertTrue(Types.ofType(Types.MINUS_MINUS, Types.POSTFIX_OPERATOR))
        assertTrue(Types.ofType(Types.POSTFIX_MINUS_MINUS, Types.POSTFIX_OPERATOR))
        assertFalse(Types.ofType(Types.NOT, Types.POSTFIX_OPERATOR))
    }

    // Infix operator tests
    @Test
    void testOfTypeInfixOperator() {
        assertTrue(Types.ofType(Types.DOT, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.NAVIGATE, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_OR, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.LOGICAL_AND, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PLUS, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.MINUS, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.MULTIPLY, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.DIVIDE, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.EQUAL, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.COMPARE_EQUAL, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.DOT_DOT, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.FIND_REGEX, Types.INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.KEYWORD_INSTANCEOF, Types.INFIX_OPERATOR))
    }

    // Prefix or infix operator tests
    @Test
    void testOfTypePrefixOrInfixOperator() {
        assertTrue(Types.ofType(Types.POWER, Types.PREFIX_OR_INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PLUS, Types.PREFIX_OR_INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.MINUS, Types.PREFIX_OR_INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_PLUS, Types.PREFIX_OR_INFIX_OPERATOR))
        assertTrue(Types.ofType(Types.PREFIX_MINUS, Types.PREFIX_OR_INFIX_OPERATOR))
        assertFalse(Types.ofType(Types.NOT, Types.PREFIX_OR_INFIX_OPERATOR))
    }

    // Keyword tests
    @Test
    void testOfTypeKeyword() {
        assertTrue(Types.ofType(Types.KEYWORD_PRIVATE, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_PUBLIC, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_CLASS, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_IF, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_WHILE, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_TRUE, Types.KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_GOTO, Types.KEYWORD))
        assertFalse(Types.ofType(Types.PLUS, Types.KEYWORD))
    }

    // Symbol tests
    @Test
    void testOfTypeSymbol() {
        assertTrue(Types.ofType(Types.NEWLINE, Types.SYMBOL))
        assertTrue(Types.ofType(Types.DOT, Types.SYMBOL))
        assertTrue(Types.ofType(Types.COMMA, Types.SYMBOL))
        assertTrue(Types.ofType(Types.COLON, Types.SYMBOL))
        assertTrue(Types.ofType(Types.PIPE, Types.SYMBOL))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.SYMBOL))
    }

    // Literal tests
    @Test
    void testOfTypeLiteral() {
        assertTrue(Types.ofType(Types.STRING, Types.LITERAL))
        assertTrue(Types.ofType(Types.IDENTIFIER, Types.LITERAL))
        assertTrue(Types.ofType(Types.INTEGER_NUMBER, Types.LITERAL))
        assertTrue(Types.ofType(Types.DECIMAL_NUMBER, Types.LITERAL))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.LITERAL))
    }

    // Number tests
    @Test
    void testOfTypeNumber() {
        assertTrue(Types.ofType(Types.INTEGER_NUMBER, Types.NUMBER))
        assertTrue(Types.ofType(Types.DECIMAL_NUMBER, Types.NUMBER))
        assertFalse(Types.ofType(Types.STRING, Types.NUMBER))
    }

    // Sign tests
    @Test
    void testOfTypeSign() {
        assertTrue(Types.ofType(Types.PLUS, Types.SIGN))
        assertTrue(Types.ofType(Types.MINUS, Types.SIGN))
        assertFalse(Types.ofType(Types.MULTIPLY, Types.SIGN))
    }

    // Named value tests
    @Test
    void testOfTypeNamedValue() {
        assertTrue(Types.ofType(Types.KEYWORD_TRUE, Types.NAMED_VALUE))
        assertTrue(Types.ofType(Types.KEYWORD_FALSE, Types.NAMED_VALUE))
        assertTrue(Types.ofType(Types.KEYWORD_NULL, Types.NAMED_VALUE))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.NAMED_VALUE))
    }

    // Truth value tests
    @Test
    void testOfTypeTruthValue() {
        assertTrue(Types.ofType(Types.KEYWORD_TRUE, Types.TRUTH_VALUE))
        assertTrue(Types.ofType(Types.KEYWORD_FALSE, Types.TRUTH_VALUE))
        assertFalse(Types.ofType(Types.KEYWORD_NULL, Types.TRUTH_VALUE))
    }

    // Primitive type tests
    @Test
    void testOfTypePrimitiveType() {
        assertTrue(Types.ofType(Types.KEYWORD_VOID, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_BOOLEAN, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_BYTE, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_SHORT, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_INT, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_LONG, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_FLOAT, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_DOUBLE, Types.PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_CHAR, Types.PRIMITIVE_TYPE))
        assertFalse(Types.ofType(Types.KEYWORD_CLASS, Types.PRIMITIVE_TYPE))
    }

    // Creatable primitive type tests
    @Test
    void testOfTypeCreatablePrimitiveType() {
        assertTrue(Types.ofType(Types.KEYWORD_BOOLEAN, Types.CREATABLE_PRIMITIVE_TYPE))
        assertTrue(Types.ofType(Types.KEYWORD_INT, Types.CREATABLE_PRIMITIVE_TYPE))
        assertFalse(Types.ofType(Types.KEYWORD_VOID, Types.CREATABLE_PRIMITIVE_TYPE))
    }

    // Loop tests
    @Test
    void testOfTypeLoop() {
        assertTrue(Types.ofType(Types.KEYWORD_DO, Types.LOOP))
        assertTrue(Types.ofType(Types.KEYWORD_WHILE, Types.LOOP))
        assertTrue(Types.ofType(Types.KEYWORD_FOR, Types.LOOP))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.LOOP))
    }

    // Reserved keyword tests
    @Test
    void testOfTypeReservedKeyword() {
        assertTrue(Types.ofType(Types.KEYWORD_CONST, Types.RESERVED_KEYWORD))
        assertTrue(Types.ofType(Types.KEYWORD_GOTO, Types.RESERVED_KEYWORD))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.RESERVED_KEYWORD))
    }

    // Type declaration tests
    @Test
    void testOfTypeTypeDeclaration() {
        assertTrue(Types.ofType(Types.KEYWORD_CLASS, Types.TYPE_DECLARATION))
        assertTrue(Types.ofType(Types.KEYWORD_INTERFACE, Types.TYPE_DECLARATION))
        assertTrue(Types.ofType(Types.KEYWORD_MIXIN, Types.TYPE_DECLARATION))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.TYPE_DECLARATION))
    }

    // Declaration modifier tests
    @Test
    void testOfTypeDeclarationModifier() {
        assertTrue(Types.ofType(Types.KEYWORD_PUBLIC, Types.DECLARATION_MODIFIER))
        assertTrue(Types.ofType(Types.KEYWORD_PRIVATE, Types.DECLARATION_MODIFIER))
        assertTrue(Types.ofType(Types.KEYWORD_PROTECTED, Types.DECLARATION_MODIFIER))
        assertTrue(Types.ofType(Types.KEYWORD_STATIC, Types.DECLARATION_MODIFIER))
        assertTrue(Types.ofType(Types.KEYWORD_FINAL, Types.DECLARATION_MODIFIER))
        assertTrue(Types.ofType(Types.KEYWORD_ABSTRACT, Types.DECLARATION_MODIFIER))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.DECLARATION_MODIFIER))
    }

    // Type name tests
    @Test
    void testOfTypeTypeName() {
        assertTrue(Types.ofType(Types.IDENTIFIER, Types.TYPE_NAME))
        assertTrue(Types.ofType(Types.KEYWORD_INT, Types.TYPE_NAME))
        assertTrue(Types.ofType(Types.KEYWORD_VOID, Types.TYPE_NAME))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.TYPE_NAME))
    }

    // Creatable type name tests
    @Test
    void testOfTypeCreatableTypeName() {
        assertTrue(Types.ofType(Types.IDENTIFIER, Types.CREATABLE_TYPE_NAME))
        assertTrue(Types.ofType(Types.KEYWORD_INT, Types.CREATABLE_TYPE_NAME))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.CREATABLE_TYPE_NAME))
    }

    // Matched container tests
    @Test
    void testOfTypeMatchedContainer() {
        assertTrue(Types.ofType(Types.LEFT_PARENTHESIS, Types.MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.RIGHT_PARENTHESIS, Types.MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.LEFT_SQUARE_BRACKET, Types.MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.RIGHT_SQUARE_BRACKET, Types.MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.LEFT_CURLY_BRACE, Types.MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.RIGHT_CURLY_BRACE, Types.MATCHED_CONTAINER))
        assertFalse(Types.ofType(Types.DOT, Types.MATCHED_CONTAINER))
    }

    @Test
    void testOfTypeLeftOfMatchedContainer() {
        assertTrue(Types.ofType(Types.LEFT_PARENTHESIS, Types.LEFT_OF_MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.LEFT_SQUARE_BRACKET, Types.LEFT_OF_MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.LEFT_CURLY_BRACE, Types.LEFT_OF_MATCHED_CONTAINER))
        assertFalse(Types.ofType(Types.RIGHT_PARENTHESIS, Types.LEFT_OF_MATCHED_CONTAINER))
    }

    @Test
    void testOfTypeRightOfMatchedContainer() {
        assertTrue(Types.ofType(Types.RIGHT_PARENTHESIS, Types.RIGHT_OF_MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.RIGHT_SQUARE_BRACKET, Types.RIGHT_OF_MATCHED_CONTAINER))
        assertTrue(Types.ofType(Types.RIGHT_CURLY_BRACE, Types.RIGHT_OF_MATCHED_CONTAINER))
        assertFalse(Types.ofType(Types.LEFT_PARENTHESIS, Types.RIGHT_OF_MATCHED_CONTAINER))
    }

    // Expression tests
    @Test
    void testOfTypeExpression() {
        assertTrue(Types.ofType(Types.DOT, Types.EXPRESSION))
        assertTrue(Types.ofType(Types.PLUS, Types.EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_CAST, Types.EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_NEW, Types.EXPRESSION))
        assertTrue(Types.ofType(Types.STRING, Types.EXPRESSION))
        assertTrue(Types.ofType(Types.LEFT_SQUARE_BRACKET, Types.EXPRESSION))
    }

    @Test
    void testOfTypeOperatorExpression() {
        assertTrue(Types.ofType(Types.DOT, Types.OPERATOR_EXPRESSION))
        assertTrue(Types.ofType(Types.PLUS, Types.OPERATOR_EXPRESSION))
        assertTrue(Types.ofType(Types.MINUS, Types.OPERATOR_EXPRESSION))
        assertTrue(Types.ofType(Types.LEFT_SHIFT, Types.OPERATOR_EXPRESSION)) // within DOT to RIGHT_SHIFT_UNSIGNED range
        assertTrue(Types.ofType(Types.RIGHT_SHIFT_UNSIGNED, Types.OPERATOR_EXPRESSION))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.OPERATOR_EXPRESSION))
    }

    @Test
    void testOfTypeSynthExpression() {
        assertTrue(Types.ofType(Types.SYNTH_CAST, Types.SYNTH_EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_TERNARY, Types.SYNTH_EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_CLOSURE, Types.SYNTH_EXPRESSION))
        assertFalse(Types.ofType(Types.STRING, Types.SYNTH_EXPRESSION))
    }

    @Test
    void testOfTypeKeywordExpression() {
        assertTrue(Types.ofType(Types.KEYWORD_NEW, Types.KEYWORD_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_THIS, Types.KEYWORD_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_SUPER, Types.KEYWORD_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_TRUE, Types.KEYWORD_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_FALSE, Types.KEYWORD_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_NULL, Types.KEYWORD_EXPRESSION))
        assertFalse(Types.ofType(Types.KEYWORD_IF, Types.KEYWORD_EXPRESSION))
    }

    @Test
    void testOfTypeArrayExpression() {
        assertTrue(Types.ofType(Types.LEFT_SQUARE_BRACKET, Types.ARRAY_EXPRESSION))
        assertFalse(Types.ofType(Types.RIGHT_SQUARE_BRACKET, Types.ARRAY_EXPRESSION))
    }

    @Test
    void testOfTypeSimpleExpression() {
        assertTrue(Types.ofType(Types.STRING, Types.SIMPLE_EXPRESSION))
        assertTrue(Types.ofType(Types.INTEGER_NUMBER, Types.SIMPLE_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_THIS, Types.SIMPLE_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_TRUE, Types.SIMPLE_EXPRESSION))
        assertFalse(Types.ofType(Types.PLUS, Types.SIMPLE_EXPRESSION))
    }

    @Test
    void testOfTypeComplexExpression() {
        assertTrue(Types.ofType(Types.STRING, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_THIS, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_METHOD_CALL, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.KEYWORD_NEW, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_GSTRING, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.SYNTH_LIST, Types.COMPLEX_EXPRESSION))
        assertTrue(Types.ofType(Types.IDENTIFIER, Types.COMPLEX_EXPRESSION)) // also includes SIMPLE_EXPRESSION
    }

    // Terminators tests
    @Test
    void testOfTypeParameterTerminators() {
        assertTrue(Types.ofType(Types.RIGHT_PARENTHESIS, Types.PARAMETER_TERMINATORS))
        assertTrue(Types.ofType(Types.COMMA, Types.PARAMETER_TERMINATORS))
        assertFalse(Types.ofType(Types.SEMICOLON, Types.PARAMETER_TERMINATORS))
    }

    @Test
    void testOfTypeArrayItemTerminators() {
        assertTrue(Types.ofType(Types.RIGHT_SQUARE_BRACKET, Types.ARRAY_ITEM_TERMINATORS))
        assertTrue(Types.ofType(Types.COMMA, Types.ARRAY_ITEM_TERMINATORS))
        assertFalse(Types.ofType(Types.SEMICOLON, Types.ARRAY_ITEM_TERMINATORS))
    }

    @Test
    void testOfTypeSwitchBlockTerminators() {
        assertTrue(Types.ofType(Types.KEYWORD_CASE, Types.SWITCH_BLOCK_TERMINATORS))
        assertTrue(Types.ofType(Types.KEYWORD_DEFAULT, Types.SWITCH_BLOCK_TERMINATORS))
        assertTrue(Types.ofType(Types.RIGHT_CURLY_BRACE, Types.SWITCH_BLOCK_TERMINATORS))
        assertFalse(Types.ofType(Types.SEMICOLON, Types.SWITCH_BLOCK_TERMINATORS))
    }

    @Test
    void testOfTypeSwitchEntries() {
        assertTrue(Types.ofType(Types.KEYWORD_CASE, Types.SWITCH_ENTRIES))
        assertTrue(Types.ofType(Types.KEYWORD_DEFAULT, Types.SWITCH_ENTRIES))
        assertFalse(Types.ofType(Types.RIGHT_CURLY_BRACE, Types.SWITCH_ENTRIES))
    }

    // getText tests
    @Test
    void testGetTextForOperators() {
        assertEquals("{", Types.getText(Types.LEFT_CURLY_BRACE))
        assertEquals("}", Types.getText(Types.RIGHT_CURLY_BRACE))
        assertEquals("[", Types.getText(Types.LEFT_SQUARE_BRACKET))
        assertEquals("]", Types.getText(Types.RIGHT_SQUARE_BRACKET))
        assertEquals("(", Types.getText(Types.LEFT_PARENTHESIS))
        assertEquals(")", Types.getText(Types.RIGHT_PARENTHESIS))
        assertEquals(".", Types.getText(Types.DOT))
        assertEquals("..", Types.getText(Types.DOT_DOT))
        assertEquals("...", Types.getText(Types.DOT_DOT_DOT))
        assertEquals("->", Types.getText(Types.NAVIGATE))
        assertEquals("=~", Types.getText(Types.FIND_REGEX))
        assertEquals("==~", Types.getText(Types.MATCH_REGEX))
        assertEquals("~", Types.getText(Types.REGEX_PATTERN))
        assertEquals("=", Types.getText(Types.EQUAL))
        assertEquals("+", Types.getText(Types.PLUS))
        assertEquals("-", Types.getText(Types.MINUS))
        assertEquals("*", Types.getText(Types.MULTIPLY))
        assertEquals("/", Types.getText(Types.DIVIDE))
        assertEquals("%", Types.getText(Types.REMAINDER)) // REMAINDER is registered, not MOD
        assertEquals("**", Types.getText(Types.POWER))
    }

    @Test
    void testGetTextForComparisonOperators() {
        assertEquals("!=", Types.getText(Types.COMPARE_NOT_EQUAL))
        assertEquals("===", Types.getText(Types.COMPARE_IDENTICAL))
        assertEquals("!==", Types.getText(Types.COMPARE_NOT_IDENTICAL))
        assertEquals("==", Types.getText(Types.COMPARE_EQUAL))
        assertEquals("<", Types.getText(Types.COMPARE_LESS_THAN))
        assertEquals("<=", Types.getText(Types.COMPARE_LESS_THAN_EQUAL))
        assertEquals(">", Types.getText(Types.COMPARE_GREATER_THAN))
        assertEquals(">=", Types.getText(Types.COMPARE_GREATER_THAN_EQUAL))
        assertEquals("<=>", Types.getText(Types.COMPARE_TO))
    }

    @Test
    void testGetTextForKeywords() {
        assertEquals("private", Types.getText(Types.KEYWORD_PRIVATE))
        assertEquals("public", Types.getText(Types.KEYWORD_PUBLIC))
        assertEquals("class", Types.getText(Types.KEYWORD_CLASS))
        assertEquals("if", Types.getText(Types.KEYWORD_IF))
        assertEquals("else", Types.getText(Types.KEYWORD_ELSE))
        assertEquals("while", Types.getText(Types.KEYWORD_WHILE))
        assertEquals("for", Types.getText(Types.KEYWORD_FOR))
        assertEquals("true", Types.getText(Types.KEYWORD_TRUE))
        assertEquals("false", Types.getText(Types.KEYWORD_FALSE))
        assertEquals("null", Types.getText(Types.KEYWORD_NULL))
    }

    @Test
    void testGetTextReturnsEmptyForUnknown() {
        // getText returns "" for types not in TEXTS map
        assertEquals("", Types.getText(Types.UNKNOWN))
        assertEquals("", Types.getText(Types.STRING))
        assertEquals("", Types.getText(Types.IDENTIFIER))
    }

    // getDescription tests
    @Test
    void testGetDescription() {
        assertNotNull(Types.getDescription(Types.EOF))
        assertNotNull(Types.getDescription(Types.UNKNOWN))
        assertNotNull(Types.getDescription(Types.PLUS))
        assertNotNull(Types.getDescription(Types.KEYWORD_IF))
        assertNotNull(Types.getDescription(Types.STRING))
    }

    // lookup tests
    @Test
    void testLookupKeywords() {
        assertEquals(Types.KEYWORD_IF, Types.lookup("if", Types.UNKNOWN))
        assertEquals(Types.KEYWORD_ELSE, Types.lookup("else", Types.UNKNOWN))
        assertEquals(Types.KEYWORD_CLASS, Types.lookup("class", Types.UNKNOWN))
        assertEquals(Types.KEYWORD_TRUE, Types.lookup("true", Types.UNKNOWN))
        assertEquals(Types.KEYWORD_FALSE, Types.lookup("false", Types.UNKNOWN))
        assertEquals(Types.KEYWORD_NULL, Types.lookup("null", Types.UNKNOWN))
    }

    @Test
    void testLookupReturnsUnknownForNotFound() {
        // lookup returns UNKNOWN when text is not in LOOKUP map
        // second param is a filter, not a default value
        assertEquals(Types.UNKNOWN, Types.lookup("notAKeyword", Types.UNKNOWN))
        assertEquals(Types.UNKNOWN, Types.lookup("xyz", Types.KEYWORD)) // not in map
        // when filter doesn't match, also returns UNKNOWN
        assertEquals(Types.UNKNOWN, Types.lookup("if", Types.SYMBOL)) // "if" is keyword, not symbol
    }

    // lookupKeyword tests
    @Test
    void testLookupKeyword() {
        assertEquals(Types.KEYWORD_IF, Types.lookupKeyword("if"))
        assertEquals(Types.KEYWORD_CLASS, Types.lookupKeyword("class"))
        assertEquals(Types.UNKNOWN, Types.lookupKeyword("notAKeyword"))
    }

    // lookupSymbol tests
    @Test
    void testLookupSymbol() {
        assertEquals(Types.LEFT_CURLY_BRACE, Types.lookupSymbol("{"))
        assertEquals(Types.RIGHT_CURLY_BRACE, Types.lookupSymbol("}"))
        assertEquals(Types.PLUS, Types.lookupSymbol("+"))
        assertEquals(Types.MINUS, Types.lookupSymbol("-"))
        assertEquals(Types.COMPARE_EQUAL, Types.lookupSymbol("=="))
        assertEquals(Types.UNKNOWN, Types.lookupSymbol("xyz"))
    }

    // Synthetic type tests
    @Test
    void testOfTypeSynthetic() {
        assertTrue(Types.ofType(Types.SYNTH_COMPILATION_UNIT, Types.SYNTHETIC))
        assertTrue(Types.ofType(Types.SYNTH_CLASS, Types.SYNTHETIC))
        assertTrue(Types.ofType(Types.SYNTH_METHOD, Types.SYNTHETIC))
        assertTrue(Types.ofType(Types.SYNTH_CLOSURE, Types.SYNTHETIC))
        assertTrue(Types.ofType(Types.SYNTH_VARIABLE_DECLARATION, Types.SYNTHETIC))
        assertFalse(Types.ofType(Types.KEYWORD_CLASS, Types.SYNTHETIC))
    }
}
