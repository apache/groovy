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
package org.codehaus.groovy.antlr.java;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;

public class Java2GroovyConverter extends VisitorAdapter{
    private final int[] typeMapping;

    public Java2GroovyConverter(String[] tokenNames) {
        typeMapping = new int[400]; // magic number, much greater than current number of java tokens
        typeMapping[JavaTokenTypes.ABSTRACT] = GroovyTokenTypes.ABSTRACT;
        typeMapping[JavaTokenTypes.EOF] = GroovyTokenTypes.EOF;
        typeMapping[JavaTokenTypes.NULL_TREE_LOOKAHEAD] = GroovyTokenTypes.NULL_TREE_LOOKAHEAD;
        typeMapping[JavaTokenTypes.BLOCK] = GroovyTokenTypes.BLOCK;
        typeMapping[JavaTokenTypes.MODIFIERS] = GroovyTokenTypes.MODIFIERS;
        typeMapping[JavaTokenTypes.OBJBLOCK] = GroovyTokenTypes.OBJBLOCK;
        typeMapping[JavaTokenTypes.SLIST] = GroovyTokenTypes.SLIST;
        typeMapping[JavaTokenTypes.METHOD_DEF] = GroovyTokenTypes.METHOD_DEF;
        typeMapping[JavaTokenTypes.VARIABLE_DEF] = GroovyTokenTypes.VARIABLE_DEF;
        typeMapping[JavaTokenTypes.INSTANCE_INIT] = GroovyTokenTypes.INSTANCE_INIT;
        typeMapping[JavaTokenTypes.STATIC_INIT] = GroovyTokenTypes.STATIC_INIT;
        typeMapping[JavaTokenTypes.TYPE] = GroovyTokenTypes.TYPE;
        typeMapping[JavaTokenTypes.CLASS_DEF] = GroovyTokenTypes.CLASS_DEF;
        typeMapping[JavaTokenTypes.INTERFACE_DEF] = GroovyTokenTypes.INTERFACE_DEF;
        typeMapping[JavaTokenTypes.PACKAGE_DEF] = GroovyTokenTypes.PACKAGE_DEF;
        typeMapping[JavaTokenTypes.ARRAY_DECLARATOR] = GroovyTokenTypes.ARRAY_DECLARATOR;
        typeMapping[JavaTokenTypes.EXTENDS_CLAUSE] = GroovyTokenTypes.EXTENDS_CLAUSE;
        typeMapping[JavaTokenTypes.IMPLEMENTS_CLAUSE] = GroovyTokenTypes.IMPLEMENTS_CLAUSE;
        typeMapping[JavaTokenTypes.PARAMETERS] = GroovyTokenTypes.PARAMETERS;
        typeMapping[JavaTokenTypes.PARAMETER_DEF] = GroovyTokenTypes.PARAMETER_DEF;
        typeMapping[JavaTokenTypes.LABELED_STAT] = GroovyTokenTypes.LABELED_STAT;
        typeMapping[JavaTokenTypes.TYPECAST] = GroovyTokenTypes.TYPECAST;
        typeMapping[JavaTokenTypes.INDEX_OP] = GroovyTokenTypes.INDEX_OP;
        typeMapping[JavaTokenTypes.POST_INC] = GroovyTokenTypes.POST_INC;
        typeMapping[JavaTokenTypes.POST_DEC] = GroovyTokenTypes.POST_DEC;
        typeMapping[JavaTokenTypes.METHOD_CALL] = GroovyTokenTypes.METHOD_CALL;
        typeMapping[JavaTokenTypes.EXPR] = GroovyTokenTypes.EXPR;
        typeMapping[JavaTokenTypes.ARRAY_INIT] = GroovyTokenTypes.LIST_CONSTRUCTOR; // this assumes LIST_CONSTRUCTOR set by PreJava2GroovyConvertor
        typeMapping[JavaTokenTypes.IMPORT] = GroovyTokenTypes.IMPORT;
        typeMapping[JavaTokenTypes.UNARY_MINUS] = GroovyTokenTypes.UNARY_MINUS;
        typeMapping[JavaTokenTypes.UNARY_PLUS] = GroovyTokenTypes.UNARY_PLUS;
        typeMapping[JavaTokenTypes.CASE_GROUP] = GroovyTokenTypes.CASE_GROUP;
        typeMapping[JavaTokenTypes.ELIST] = GroovyTokenTypes.ELIST;
        typeMapping[JavaTokenTypes.FOR_INIT] = GroovyTokenTypes.FOR_INIT;
        typeMapping[JavaTokenTypes.FOR_CONDITION] = GroovyTokenTypes.FOR_CONDITION;
        typeMapping[JavaTokenTypes.FOR_ITERATOR] = GroovyTokenTypes.FOR_ITERATOR;
        typeMapping[JavaTokenTypes.EMPTY_STAT] = GroovyTokenTypes.EMPTY_STAT;
        typeMapping[JavaTokenTypes.FINAL] = GroovyTokenTypes.FINAL;
        typeMapping[JavaTokenTypes.STRICTFP] = GroovyTokenTypes.STRICTFP;
        typeMapping[JavaTokenTypes.SUPER_CTOR_CALL] = GroovyTokenTypes.SUPER_CTOR_CALL;
        typeMapping[JavaTokenTypes.CTOR_CALL] = GroovyTokenTypes.CTOR_CALL;
        typeMapping[JavaTokenTypes.VARIABLE_PARAMETER_DEF] = GroovyTokenTypes.VARIABLE_PARAMETER_DEF;
        typeMapping[JavaTokenTypes.STATIC_IMPORT] = GroovyTokenTypes.STATIC_IMPORT;
        typeMapping[JavaTokenTypes.ENUM_DEF] = GroovyTokenTypes.ENUM_DEF;
        typeMapping[JavaTokenTypes.ENUM_CONSTANT_DEF] = GroovyTokenTypes.ENUM_CONSTANT_DEF;
        typeMapping[JavaTokenTypes.FOR_EACH_CLAUSE] = GroovyTokenTypes.FOR_EACH_CLAUSE;
        typeMapping[JavaTokenTypes.ANNOTATION_DEF] = GroovyTokenTypes.ANNOTATION_DEF;
        typeMapping[JavaTokenTypes.ANNOTATIONS] = GroovyTokenTypes.ANNOTATIONS;
        typeMapping[JavaTokenTypes.ANNOTATION] = GroovyTokenTypes.ANNOTATION;
        typeMapping[JavaTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR] = GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR;
        typeMapping[JavaTokenTypes.ANNOTATION_FIELD_DEF] = GroovyTokenTypes.ANNOTATION_FIELD_DEF;
        typeMapping[JavaTokenTypes.ANNOTATION_ARRAY_INIT] = GroovyTokenTypes.ANNOTATION_ARRAY_INIT;
        typeMapping[JavaTokenTypes.TYPE_ARGUMENTS] = GroovyTokenTypes.TYPE_ARGUMENTS;
        typeMapping[JavaTokenTypes.TYPE_ARGUMENT] = GroovyTokenTypes.TYPE_ARGUMENT;
        typeMapping[JavaTokenTypes.TYPE_PARAMETERS] = GroovyTokenTypes.TYPE_PARAMETERS;
        typeMapping[JavaTokenTypes.TYPE_PARAMETER] = GroovyTokenTypes.TYPE_PARAMETER;
        typeMapping[JavaTokenTypes.WILDCARD_TYPE] = GroovyTokenTypes.WILDCARD_TYPE;
        typeMapping[JavaTokenTypes.TYPE_UPPER_BOUNDS] = GroovyTokenTypes.TYPE_UPPER_BOUNDS;
        typeMapping[JavaTokenTypes.TYPE_LOWER_BOUNDS] = GroovyTokenTypes.TYPE_LOWER_BOUNDS;
        typeMapping[JavaTokenTypes.LITERAL_package] = GroovyTokenTypes.LITERAL_package;
        typeMapping[JavaTokenTypes.SEMI] = GroovyTokenTypes.SEMI;
        typeMapping[JavaTokenTypes.LITERAL_import] = GroovyTokenTypes.LITERAL_import;
        typeMapping[JavaTokenTypes.LITERAL_static] = GroovyTokenTypes.LITERAL_static;
        typeMapping[JavaTokenTypes.LBRACK] = GroovyTokenTypes.LBRACK;
        typeMapping[JavaTokenTypes.RBRACK] = GroovyTokenTypes.RBRACK;
        typeMapping[JavaTokenTypes.IDENT] = GroovyTokenTypes.IDENT;
        typeMapping[JavaTokenTypes.DOT] = GroovyTokenTypes.DOT;
        typeMapping[JavaTokenTypes.QUESTION] = GroovyTokenTypes.QUESTION;
        typeMapping[JavaTokenTypes.LITERAL_extends] = GroovyTokenTypes.LITERAL_extends;
        typeMapping[JavaTokenTypes.LITERAL_super] = GroovyTokenTypes.LITERAL_super;
        typeMapping[JavaTokenTypes.LT] = GroovyTokenTypes.LT;
        typeMapping[JavaTokenTypes.COMMA] = GroovyTokenTypes.COMMA;
        typeMapping[JavaTokenTypes.GT] = GroovyTokenTypes.GT;
        typeMapping[JavaTokenTypes.SR] = GroovyTokenTypes.SR;
        typeMapping[JavaTokenTypes.BSR] = GroovyTokenTypes.BSR;
        typeMapping[JavaTokenTypes.LITERAL_void] = GroovyTokenTypes.LITERAL_void;
        typeMapping[JavaTokenTypes.LITERAL_boolean] = GroovyTokenTypes.LITERAL_boolean;
        typeMapping[JavaTokenTypes.LITERAL_byte] = GroovyTokenTypes.LITERAL_byte;
        typeMapping[JavaTokenTypes.LITERAL_char] = GroovyTokenTypes.LITERAL_char;
        typeMapping[JavaTokenTypes.LITERAL_short] = GroovyTokenTypes.LITERAL_short;
        typeMapping[JavaTokenTypes.LITERAL_int] = GroovyTokenTypes.LITERAL_int;
        typeMapping[JavaTokenTypes.LITERAL_float] = GroovyTokenTypes.LITERAL_float;
        typeMapping[JavaTokenTypes.LITERAL_long] = GroovyTokenTypes.LITERAL_long;
        typeMapping[JavaTokenTypes.LITERAL_double] = GroovyTokenTypes.LITERAL_double;
        typeMapping[JavaTokenTypes.STAR] = GroovyTokenTypes.STAR;
        typeMapping[JavaTokenTypes.LITERAL_private] = GroovyTokenTypes.LITERAL_private;
        typeMapping[JavaTokenTypes.LITERAL_public] = GroovyTokenTypes.LITERAL_public;
        typeMapping[JavaTokenTypes.LITERAL_protected] = GroovyTokenTypes.LITERAL_protected;
        typeMapping[JavaTokenTypes.LITERAL_transient] = GroovyTokenTypes.LITERAL_transient;
        typeMapping[JavaTokenTypes.LITERAL_native] = GroovyTokenTypes.LITERAL_native;
        typeMapping[JavaTokenTypes.LITERAL_threadsafe] = GroovyTokenTypes.LITERAL_threadsafe;
        typeMapping[JavaTokenTypes.LITERAL_synchronized] = GroovyTokenTypes.LITERAL_synchronized;
        typeMapping[JavaTokenTypes.LITERAL_volatile] = GroovyTokenTypes.LITERAL_volatile;
        typeMapping[JavaTokenTypes.AT] = GroovyTokenTypes.AT;
        typeMapping[JavaTokenTypes.LPAREN] = GroovyTokenTypes.LPAREN;
        typeMapping[JavaTokenTypes.RPAREN] = GroovyTokenTypes.RPAREN;
        typeMapping[JavaTokenTypes.ASSIGN] = GroovyTokenTypes.ASSIGN;
        typeMapping[JavaTokenTypes.LCURLY] = GroovyTokenTypes.LCURLY;
        typeMapping[JavaTokenTypes.RCURLY] = GroovyTokenTypes.RCURLY;
        typeMapping[JavaTokenTypes.LITERAL_class] = GroovyTokenTypes.LITERAL_class;
        typeMapping[JavaTokenTypes.LITERAL_interface] = GroovyTokenTypes.LITERAL_interface;
        typeMapping[JavaTokenTypes.LITERAL_enum] = GroovyTokenTypes.LITERAL_enum;
        typeMapping[JavaTokenTypes.BAND] = GroovyTokenTypes.BAND;
        typeMapping[JavaTokenTypes.LITERAL_default] = GroovyTokenTypes.LITERAL_default;
        typeMapping[JavaTokenTypes.LITERAL_implements] = GroovyTokenTypes.LITERAL_implements;
        typeMapping[JavaTokenTypes.LITERAL_this] = GroovyTokenTypes.LITERAL_this;
        typeMapping[JavaTokenTypes.LITERAL_throws] = GroovyTokenTypes.LITERAL_throws;
        typeMapping[JavaTokenTypes.TRIPLE_DOT] = GroovyTokenTypes.TRIPLE_DOT;
        typeMapping[JavaTokenTypes.COLON] = GroovyTokenTypes.COLON;
        typeMapping[JavaTokenTypes.LITERAL_if] = GroovyTokenTypes.LITERAL_if;
        typeMapping[JavaTokenTypes.LITERAL_else] = GroovyTokenTypes.LITERAL_else;
        typeMapping[JavaTokenTypes.LITERAL_while] = GroovyTokenTypes.LITERAL_while;
        typeMapping[JavaTokenTypes.LITERAL_do] = GroovyTokenTypes.LITERAL_while; // warning - do...while... ignored
        typeMapping[JavaTokenTypes.LITERAL_break] = GroovyTokenTypes.LITERAL_break;
        typeMapping[JavaTokenTypes.LITERAL_continue] = GroovyTokenTypes.LITERAL_continue;
        typeMapping[JavaTokenTypes.LITERAL_return] = GroovyTokenTypes.LITERAL_return;
        typeMapping[JavaTokenTypes.LITERAL_switch] = GroovyTokenTypes.LITERAL_switch;
        typeMapping[JavaTokenTypes.LITERAL_throw] = GroovyTokenTypes.LITERAL_throw;
        typeMapping[JavaTokenTypes.LITERAL_assert] = GroovyTokenTypes.LITERAL_assert;
        typeMapping[JavaTokenTypes.LITERAL_for] = GroovyTokenTypes.LITERAL_for;
        typeMapping[JavaTokenTypes.LITERAL_case] = GroovyTokenTypes.LITERAL_case;
        typeMapping[JavaTokenTypes.LITERAL_try] = GroovyTokenTypes.LITERAL_try;
        typeMapping[JavaTokenTypes.LITERAL_finally] = GroovyTokenTypes.LITERAL_finally;
        typeMapping[JavaTokenTypes.LITERAL_catch] = GroovyTokenTypes.LITERAL_catch;
        typeMapping[JavaTokenTypes.PLUS_ASSIGN] = GroovyTokenTypes.PLUS_ASSIGN;
        typeMapping[JavaTokenTypes.MINUS_ASSIGN] = GroovyTokenTypes.MINUS_ASSIGN;
        typeMapping[JavaTokenTypes.STAR_ASSIGN] = GroovyTokenTypes.STAR_ASSIGN;
        typeMapping[JavaTokenTypes.DIV_ASSIGN] = GroovyTokenTypes.DIV_ASSIGN;
        typeMapping[JavaTokenTypes.MOD_ASSIGN] = GroovyTokenTypes.MOD_ASSIGN;
        typeMapping[JavaTokenTypes.SR_ASSIGN] = GroovyTokenTypes.SR_ASSIGN;
        typeMapping[JavaTokenTypes.BSR_ASSIGN] = GroovyTokenTypes.BSR_ASSIGN;
        typeMapping[JavaTokenTypes.SL_ASSIGN] = GroovyTokenTypes.SL_ASSIGN;
        typeMapping[JavaTokenTypes.BAND_ASSIGN] = GroovyTokenTypes.BAND_ASSIGN;
        typeMapping[JavaTokenTypes.BXOR_ASSIGN] = GroovyTokenTypes.BXOR_ASSIGN;
        typeMapping[JavaTokenTypes.BOR_ASSIGN] = GroovyTokenTypes.BOR_ASSIGN;
        typeMapping[JavaTokenTypes.LOR] = GroovyTokenTypes.LOR;
        typeMapping[JavaTokenTypes.LAND] = GroovyTokenTypes.LAND;
        typeMapping[JavaTokenTypes.BOR] = GroovyTokenTypes.BOR;
        typeMapping[JavaTokenTypes.BXOR] = GroovyTokenTypes.BXOR;
        typeMapping[JavaTokenTypes.NOT_EQUAL] = GroovyTokenTypes.NOT_EQUAL;
        typeMapping[JavaTokenTypes.EQUAL] = GroovyTokenTypes.EQUAL;
        typeMapping[JavaTokenTypes.LE] = GroovyTokenTypes.LE;
        typeMapping[JavaTokenTypes.GE] = GroovyTokenTypes.GE;
        typeMapping[JavaTokenTypes.LITERAL_instanceof] = GroovyTokenTypes.LITERAL_instanceof;
        typeMapping[JavaTokenTypes.SL] = GroovyTokenTypes.SL;
        typeMapping[JavaTokenTypes.PLUS] = GroovyTokenTypes.PLUS;
        typeMapping[JavaTokenTypes.MINUS] = GroovyTokenTypes.MINUS;
        typeMapping[JavaTokenTypes.DIV] = GroovyTokenTypes.DIV;
        typeMapping[JavaTokenTypes.MOD] = GroovyTokenTypes.MOD;
        typeMapping[JavaTokenTypes.INC] = GroovyTokenTypes.INC;
        typeMapping[JavaTokenTypes.DEC] = GroovyTokenTypes.DEC;
        typeMapping[JavaTokenTypes.BNOT] = GroovyTokenTypes.BNOT;
        typeMapping[JavaTokenTypes.LNOT] = GroovyTokenTypes.LNOT;
        typeMapping[JavaTokenTypes.LITERAL_true] = GroovyTokenTypes.LITERAL_true;
        typeMapping[JavaTokenTypes.LITERAL_false] = GroovyTokenTypes.LITERAL_false;
        typeMapping[JavaTokenTypes.LITERAL_null] = GroovyTokenTypes.LITERAL_null;
        typeMapping[JavaTokenTypes.LITERAL_new] = GroovyTokenTypes.LITERAL_new;
        typeMapping[JavaTokenTypes.NUM_INT] = GroovyTokenTypes.NUM_INT;
        typeMapping[JavaTokenTypes.CHAR_LITERAL] = GroovyTokenTypes.STRING_LITERAL; // warning: treating Java chars as "String" in Groovy
        typeMapping[JavaTokenTypes.STRING_LITERAL] = GroovyTokenTypes.STRING_LITERAL;
        typeMapping[JavaTokenTypes.NUM_FLOAT] = GroovyTokenTypes.NUM_FLOAT;
        typeMapping[JavaTokenTypes.NUM_LONG] = GroovyTokenTypes.NUM_LONG;
        typeMapping[JavaTokenTypes.NUM_DOUBLE] = GroovyTokenTypes.NUM_DOUBLE;
        typeMapping[JavaTokenTypes.WS] = GroovyTokenTypes.WS;
        typeMapping[JavaTokenTypes.SL_COMMENT] = GroovyTokenTypes.SL_COMMENT;
        typeMapping[JavaTokenTypes.ML_COMMENT] = GroovyTokenTypes.ML_COMMENT;
        typeMapping[JavaTokenTypes.ESC] = GroovyTokenTypes.ESC;
        typeMapping[JavaTokenTypes.HEX_DIGIT] = GroovyTokenTypes.HEX_DIGIT;
        typeMapping[JavaTokenTypes.VOCAB] = GroovyTokenTypes.VOCAB;
        typeMapping[JavaTokenTypes.EXPONENT] = GroovyTokenTypes.EXPONENT;
        typeMapping[JavaTokenTypes.FLOAT_SUFFIX] = GroovyTokenTypes.FLOAT_SUFFIX;
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            // only want to do this once per node...
            t.setType(typeMapping[t.getType()]);
            // ----

            // need to remove double quotes in string literals
            // as groovy AST doesn't expect to have them
            if (t.getType() == GroovyTokenTypes.STRING_LITERAL) {
                String text = t.getText();
                if (isSingleQuoted(text) || isDoubleQuoted(text)) {
                    t.setText(text.substring(1, text.length() - 1)); // chop off the single quotes at start and end
                }
            }
        }
    }

    private static boolean isSingleQuoted(String text) {
        return text != null && text.length() > 2
                && text.charAt(0) == '\''
                && text.charAt(text.length() - 1) == '\'';
    }
    private static boolean isDoubleQuoted(String text) {
        return text != null && text.length() > 2
                && text.charAt(0) == '"'
                && text.charAt(text.length() - 1) == '"';
    }
}
