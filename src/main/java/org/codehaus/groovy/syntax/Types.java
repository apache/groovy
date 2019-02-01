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
package org.codehaus.groovy.syntax;

import org.codehaus.groovy.GroovyBugError;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Typing information for the CST system.  The types here are those
 *  used by CSTNode, Token, and Reduction.
 */
public class Types  {

  //---------------------------------------------------------------------------
  // TYPES: NOTE THAT ORDERING AND VALUES ARE IMPORTANT TO LOCAL ROUTINES!


    //
    // SPECIAL TOKENS

    public static final int EOF                         = -1;    // end of file
    public static final int UNKNOWN                     = 0;     // the unknown token


    //
    // RELEVANT WHITESPACE

    public static final int NEWLINE                     = 5;     // \n


    //
    // OPERATORS AND OTHER MARKERS

    public static final int LEFT_CURLY_BRACE            = 10;    // {
    public static final int RIGHT_CURLY_BRACE           = 20;    // }
    public static final int LEFT_SQUARE_BRACKET         = 30;    // [
    public static final int RIGHT_SQUARE_BRACKET        = 40;    // ]
    public static final int LEFT_PARENTHESIS            = 50;    // (
    public static final int RIGHT_PARENTHESIS           = 60;    // )

    public static final int DOT                         = 70;    // .
    public static final int DOT_DOT                     = 75;    // ..
    public static final int DOT_DOT_DOT                 = 77;    // ...

    public static final int NAVIGATE                    = 80;    // ->

    public static final int FIND_REGEX                  = 90;    // =~
    public static final int MATCH_REGEX                 = 94;    // ==~
    public static final int REGEX_PATTERN               = 97;    // ~

    public static final int EQUAL                       = 100;   // =
    public static final int EQUALS                      = EQUAL;
    public static final int ASSIGN                      = EQUAL;

    public static final int COMPARE_NOT_EQUAL           = 120;   // !=
    public static final int COMPARE_IDENTICAL           = 121;   // ===
    public static final int COMPARE_NOT_IDENTICAL       = 122;   // !==
    public static final int COMPARE_EQUAL               = 123;   // ==
    public static final int COMPARE_LESS_THAN           = 124;   // <
    public static final int COMPARE_LESS_THAN_EQUAL     = 125;   // <=
    public static final int COMPARE_GREATER_THAN        = 126;   // >
    public static final int COMPARE_GREATER_THAN_EQUAL  = 127;   // >=
    public static final int COMPARE_TO                  = 128;   // <=>

    public static final int NOT                         = 160;   // !
    public static final int LOGICAL_OR                  = 162;   // ||
    public static final int LOGICAL_AND                 = 164;   // &&

    public static final int LOGICAL_OR_EQUAL            = 166;   // ||=
    public static final int LOGICAL_AND_EQUAL           = 168;   // &&=

    public static final int PLUS                        = 200;   // +
    public static final int MINUS                       = 201;   // -
    public static final int MULTIPLY                    = 202;   // *
    public static final int DIVIDE                      = 203;   // /
    public static final int INTDIV                      = 204;   // \
    public static final int MOD                         = 205;   // %
    public static final int STAR_STAR                   = 206;   // **
    public static final int POWER                       = STAR_STAR;   // **

    public static final int PLUS_EQUAL                  = 210;   // +=
    public static final int MINUS_EQUAL                 = 211;   // -=
    public static final int MULTIPLY_EQUAL              = 212;   // *=
    public static final int DIVIDE_EQUAL                = 213;   // /=
    public static final int INTDIV_EQUAL                = 214;   // \=
    public static final int MOD_EQUAL                   = 215;   // %=
    public static final int POWER_EQUAL                 = 216;   // **=

    public static final int PLUS_PLUS                   = 250;   // ++
    public static final int PREFIX_PLUS_PLUS            = 251;   // ++
    public static final int POSTFIX_PLUS_PLUS           = 252;   // ++
    public static final int PREFIX_PLUS                 = 253;   // +

    public static final int MINUS_MINUS                 = 260;   // --
    public static final int PREFIX_MINUS_MINUS          = 261;   // --
    public static final int POSTFIX_MINUS_MINUS         = 262;   // --
    public static final int PREFIX_MINUS                = 263;   // - (negation)

    public static final int LEFT_SHIFT                  = 280;   // <<
    public static final int RIGHT_SHIFT                 = 281;   // >>
    public static final int RIGHT_SHIFT_UNSIGNED        = 282;   // >>>

    public static final int LEFT_SHIFT_EQUAL            = 285;   // <<=
    public static final int RIGHT_SHIFT_EQUAL           = 286;   // >>=
    public static final int RIGHT_SHIFT_UNSIGNED_EQUAL  = 287;   // >>>=

    public static final int STAR                        = MULTIPLY;

    public static final int COMMA                       = 300;   // -
    public static final int COLON                       = 310;   // :
    public static final int SEMICOLON                   = 320;   // ;
    public static final int QUESTION                    = 330;   // ?

    // TODO refactor PIPE to be BITWISE_OR
    public static final int PIPE                        = 340;   // |
    public static final int DOUBLE_PIPE                 = LOGICAL_OR;   // ||
    public static final int BITWISE_OR                  = PIPE;  // |
    public static final int BITWISE_AND                 = 341;   // &
    public static final int BITWISE_XOR                 = 342;   // ^

    public static final int BITWISE_OR_EQUAL            = 350;   // |=
    public static final int BITWISE_AND_EQUAL           = 351;   // &=
    public static final int BITWISE_XOR_EQUAL           = 352;   // ^=
    public static final int BITWISE_NEGATION            = REGEX_PATTERN;    // ~


    //
    // LITERALS

    public static final int STRING                      = 400;   // any bare string data

    public static final int IDENTIFIER                  = 440;   // anything text and not a keyword

    public static final int INTEGER_NUMBER              = 450;   // integer
    public static final int DECIMAL_NUMBER              = 451;   // decimal


    //
    // KEYWORDS: (PRIMARILY) CLASS/METHOD DECLARATION MODIFIERS

    public static final int KEYWORD_PRIVATE             = 500;   // declaration visibility
    public static final int KEYWORD_PROTECTED           = 501;   // declaration visibility
    public static final int KEYWORD_PUBLIC              = 502;   // declaration visibility

    public static final int KEYWORD_ABSTRACT            = 510;   // method body missing
    public static final int KEYWORD_FINAL               = 511;   // declaration cannot be overridden
    public static final int KEYWORD_NATIVE              = 512;   // a native code entry point
    public static final int KEYWORD_TRANSIENT           = 513;   // property should not be persisted
    public static final int KEYWORD_VOLATILE            = 514;   // compiler should never cache property

    public static final int KEYWORD_SYNCHRONIZED        = 520;   // modifier and block type
    public static final int KEYWORD_STATIC              = 521;   // modifier and block type


    //
    // KEYWORDS: TYPE SYSTEM

    public static final int KEYWORD_DEF                 = 530;   // identifies a function declaration
    public static final int KEYWORD_DEFMACRO            = 539;   // XXX br identifies a macro declaration
    public static final int KEYWORD_CLASS               = 531;   // identifies a class declaration
    public static final int KEYWORD_INTERFACE           = 532;   // identifies an interface declaration
    public static final int KEYWORD_MIXIN               = 533;   // identifies a mixin declaration

    public static final int KEYWORD_IMPLEMENTS          = 540;   // specifies the interfaces implemented by a class
    public static final int KEYWORD_EXTENDS             = 541;   // specifies the base class/interface for a new one
    public static final int KEYWORD_THIS                = 542;   // method variable points to the current instance
    public static final int KEYWORD_SUPER               = 543;   // method variable points to the base instance
    public static final int KEYWORD_INSTANCEOF          = 544;   // type comparator
    public static final int KEYWORD_PROPERTY            = 545;   // deprecated; identifies a property
    public static final int KEYWORD_NEW                 = 546;   // used to create a new instance of a class

    public static final int KEYWORD_PACKAGE             = 550;   // declares the package scope
    public static final int KEYWORD_IMPORT              = 551;   // declares an external class
    public static final int KEYWORD_AS                  = 552;   // used in import statements to create an alias


    //
    // KEYWORDS: CONTROL STRUCTURES

    public static final int KEYWORD_RETURN              = 560;   // returns from a closure or method
    public static final int KEYWORD_IF                  = 561;   // if
    public static final int KEYWORD_ELSE                = 562;   // else
    public static final int KEYWORD_DO                  = 570;   // do loop
    public static final int KEYWORD_WHILE               = 571;   // while loop
    public static final int KEYWORD_FOR                 = 572;   // for loop
    public static final int KEYWORD_IN                  = 573;   // for (each) loop separator
    public static final int KEYWORD_BREAK               = 574;   // exits a loop or block
    public static final int KEYWORD_CONTINUE            = 575;   // restarts a loop on the next iteration
    public static final int KEYWORD_SWITCH              = 576;   // switch block
    public static final int KEYWORD_CASE                = 577;   // item in a switch block
    public static final int KEYWORD_DEFAULT             = 578;   // catch-all item in a switch block

    public static final int KEYWORD_TRY                 = 580;   // block to monitor for exceptions
    public static final int KEYWORD_CATCH               = 581;   // catch block for a particular exception
    public static final int KEYWORD_FINALLY             = 582;   // block to always execute on exit of the try
    public static final int KEYWORD_THROW               = 583;   // statement to throw an exception
    public static final int KEYWORD_THROWS              = 584;   // method modifier to declare thrown transactions
    public static final int KEYWORD_ASSERT              = 585;   // alternate throw for code invariants


    //
    // KEYWORDS: PRIMITIVE TYPES

    public static final int KEYWORD_VOID                = 600;   // void
    public static final int KEYWORD_BOOLEAN             = 601;   // boolean
    public static final int KEYWORD_BYTE                = 602;   // 1 byte integer
    public static final int KEYWORD_SHORT               = 603;   // 2 byte integer
    public static final int KEYWORD_INT                 = 604;   // 4 byte integer
    public static final int KEYWORD_LONG                = 605;   // 8 byte integer
    public static final int KEYWORD_FLOAT               = 606;   // 32 bit floating point number
    public static final int KEYWORD_DOUBLE              = 607;   // 64 bit floating point number
    public static final int KEYWORD_CHAR                = 608;   // unicode character code


    //
    // KEYWORDS: SPECIAL VALUES

    public static final int KEYWORD_TRUE                = 610;   // boolean truth
    public static final int KEYWORD_FALSE               = 611;   // boolean false
    public static final int KEYWORD_NULL                = 612;   // missing instance


    //
    // KEYWORDS: RESERVED

    public static final int KEYWORD_CONST               = 700;   // reserved in java and groovy
    public static final int KEYWORD_GOTO                = 701;   // reserved in java and groovy


    //
    // SPECIAL (CALCULATED) MEANINGS

    public static final int SYNTH_COMPILATION_UNIT      = 800;   // reserved: a synthetic root for a CST

    public static final int SYNTH_CLASS                 = 801;   // applied to class names
    public static final int SYNTH_INTERFACE             = 802;   // applied to interface names
    public static final int SYNTH_MIXIN                 = 803;   // applied to mixin names
    public static final int SYNTH_METHOD                = 804;   // applied to method names
    public static final int SYNTH_PROPERTY              = 805;   // applied to property names
    public static final int SYNTH_PARAMETER_DECLARATION = 806;   // applied to method/closure parameter names

    public static final int SYNTH_LIST                  = 810;   // applied to "[" that marks a list
    public static final int SYNTH_MAP                   = 811;   // applied to "[" that marks a map
    public static final int SYNTH_GSTRING               = 812;   // a complete GString

    public static final int SYNTH_METHOD_CALL           = 814;   // applied to the optional "(" that marks a call to a method
    public static final int SYNTH_CAST                  = 815;   // applied to "(" that marks a type cast
    public static final int SYNTH_BLOCK                 = 816;   // applied to "{" that marks a block
    public static final int SYNTH_CLOSURE               = 817;   // applied to "{" that marks a closure
    public static final int SYNTH_LABEL                 = 818;   // applied to a statement label
    public static final int SYNTH_TERNARY               = 819;   // applied to "?" that marks a ternary expression
    public static final int SYNTH_TUPLE                 = 820;   // applied to "{" that marks an array initializer

    public static final int SYNTH_VARIABLE_DECLARATION  = 830;   // applied to an identifier that specifies
                                                                 // the type of a variable declaration

    //
    // GSTRING TOKENS

    public static final int GSTRING_START               = 901;   // any marker tha begins a GString
    public static final int GSTRING_END                 = 902;   // any matching marker that ends a GString
    public static final int GSTRING_EXPRESSION_START    = 903;   // the ${ marker that starts a GString expression
    public static final int GSTRING_EXPRESSION_END      = 904;   // the } marker that ends a GString expression


    //
    // TYPE CLASSES

    public static final int ANY                         = 1000;  // anything
    public static final int NOT_EOF                     = 1001;  // anything but EOF
    public static final int GENERAL_END_OF_STATEMENT    = 1002;  // ";", "\n", EOF
    public static final int ANY_END_OF_STATEMENT        = 1003;  // ";", "\n", EOF, "}"

    public static final int ASSIGNMENT_OPERATOR         = 1100;  // =, +=, etc.
    public static final int COMPARISON_OPERATOR         = 1101;  // ==, ===, >, <, etc.
    public static final int MATH_OPERATOR               = 1102;  // +, -, / *, %, plus the LOGICAL_OPERATORS
    public static final int LOGICAL_OPERATOR            = 1103;  // ||, &&, !
    public static final int RANGE_OPERATOR              = 1104;  // .., ...
    public static final int REGEX_COMPARISON_OPERATOR   = 1105;  // =~, etc.
    public static final int DEREFERENCE_OPERATOR        = 1106;  // ., ->
    public static final int BITWISE_OPERATOR            = 1107;  // |, &, <<, >>, >>>, ^, ~

    public static final int PREFIX_OPERATOR             = 1200;  // ++, !, etc.
    public static final int POSTFIX_OPERATOR            = 1210;  // ++, etc.
    public static final int INFIX_OPERATOR              = 1220;  // +, -, =, etc.
    public static final int PREFIX_OR_INFIX_OPERATOR    = 1230;  // +, -
    public static final int PURE_PREFIX_OPERATOR        = 1235;  // prefix +, prefix -

    public static final int KEYWORD                     = 1300;  // any keyword
    public static final int SYMBOL                      = 1301;  // any symbol
    public static final int LITERAL                     = 1310;  // strings, numbers, identifiers
    public static final int NUMBER                      = 1320;  // integers and decimals
    public static final int SIGN                        = 1325;  // "+", "-"
    public static final int NAMED_VALUE                 = 1330;  // true, false, null
    public static final int TRUTH_VALUE                 = 1331;  // true, false
    public static final int PRIMITIVE_TYPE              = 1340;  // void, byte, short, int, etc.
    public static final int CREATABLE_PRIMITIVE_TYPE    = 1341;  // any PRIMITIVE_TYPE except void
    public static final int LOOP                        = 1350;  // do, while, etc.
    public static final int RESERVED_KEYWORD            = 1360;  // const, goto, etc.
    public static final int KEYWORD_IDENTIFIER          = 1361;  // keywords that can appear as identifiers
    public static final int SYNTHETIC                   = 1370;  // any of the SYNTH types

    public static final int TYPE_DECLARATION            = 1400;  // class, interface, mixin
    public static final int DECLARATION_MODIFIER        = 1410;  // public, private, abstract, etc.

    public static final int TYPE_NAME                   = 1420;  // identifiers, primitive types
    public static final int CREATABLE_TYPE_NAME         = 1430;  // identifiers, primitive types except void

    public static final int MATCHED_CONTAINER           = 1500;  // (, ), [, ], {, }
    public static final int LEFT_OF_MATCHED_CONTAINER   = 1501;  // (, [, {
    public static final int RIGHT_OF_MATCHED_CONTAINER  = 1502;  // ), ], }

    public static final int EXPRESSION                  = 1900;  // all of the below 1900 series

    public static final int OPERATOR_EXPRESSION         = 1901;  // "."-"<<"
    public static final int SYNTH_EXPRESSION            = 1902;  // cast, ternary, and closure expression
    public static final int KEYWORD_EXPRESSION          = 1903;  // new, this, super, instanceof, true, false, null
    public static final int LITERAL_EXPRESSION          = 1904;  // LITERAL
    public static final int ARRAY_EXPRESSION            = 1905;  // "["

    public static final int SIMPLE_EXPRESSION           = 1910;  // LITERAL, this, true, false, null
    public static final int COMPLEX_EXPRESSION          = 1911;  // SIMPLE_EXPRESSION, and various molecules



    //
    // TYPE GROUPS (OPERATIONS SUPPORT)

    public static final int PARAMETER_TERMINATORS       = 2000;  // ")", ","
    public static final int ARRAY_ITEM_TERMINATORS      = 2001;  // "]", ","
    public static final int TYPE_LIST_TERMINATORS       = 2002;  // "implements", "throws", "{", ","
    public static final int OPTIONAL_DATATYPE_FOLLOWERS = 2003;  // identifier, "[", "."

    public static final int SWITCH_BLOCK_TERMINATORS    = 2004;  // "case", "default", "}"
    public static final int SWITCH_ENTRIES              = 2005;  // "case", "default"

    public static final int METHOD_CALL_STARTERS        = 2006;  // LITERAL, "(", "{"
    public static final int UNSAFE_OVER_NEWLINES        = 2007;  // things the expression parser should cross lines for in it doesn't have to

    public static final int PRECLUDES_CAST_OPERATOR     = 2008;  // anything that prevents (X) from being a cast





  //---------------------------------------------------------------------------
  // TYPE HIERARCHIES


   /**
    *  Given two types, returns true if the second describes the first.
    */

    public static boolean ofType( int specific, int general )
    {

        if( general == specific )
        {
            return true;
        }

        switch( general )
        {
            case ANY:
                return true;

            case NOT_EOF:
                return specific >= UNKNOWN && specific <= SYNTH_VARIABLE_DECLARATION;

            case GENERAL_END_OF_STATEMENT:
                switch( specific )
                {
                    case EOF:
                    case NEWLINE:
                    case SEMICOLON:
                        return true;
                }
                break;

            case ANY_END_OF_STATEMENT:
                switch( specific )
                {
                    case EOF:
                    case NEWLINE:
                    case SEMICOLON:
                    case RIGHT_CURLY_BRACE:
                        return true;
                }
                break;

            case ASSIGNMENT_OPERATOR:
                return specific == EQUAL || (specific >= PLUS_EQUAL && specific <= POWER_EQUAL) || (specific >= LOGICAL_OR_EQUAL && specific <= LOGICAL_AND_EQUAL)
                                         || (specific >= LEFT_SHIFT_EQUAL && specific <= RIGHT_SHIFT_UNSIGNED_EQUAL)
                                         || (specific >= BITWISE_OR_EQUAL && specific <= BITWISE_XOR_EQUAL);

            case COMPARISON_OPERATOR:
                return specific >= COMPARE_NOT_EQUAL && specific <= COMPARE_TO;

            case MATH_OPERATOR:
                return (specific >= PLUS && specific <= RIGHT_SHIFT_UNSIGNED) || (specific >= NOT && specific <= LOGICAL_AND)
                                 || (specific >= BITWISE_OR && specific <= BITWISE_XOR);

            case LOGICAL_OPERATOR:
                return specific >= NOT && specific <= LOGICAL_AND;

            case BITWISE_OPERATOR:
                return (specific >= BITWISE_OR && specific <= BITWISE_XOR) || specific == BITWISE_NEGATION;

            case RANGE_OPERATOR:
                return specific == DOT_DOT || specific == DOT_DOT_DOT;

            case REGEX_COMPARISON_OPERATOR:
                return specific == FIND_REGEX || specific == MATCH_REGEX;

            case DEREFERENCE_OPERATOR:
                return specific == DOT || specific == NAVIGATE;

            case PREFIX_OPERATOR:
                switch( specific )
                {
                    case MINUS:
                    case PLUS_PLUS:
                    case MINUS_MINUS:
                        return true;
                }

                /* FALL THROUGH */

            case PURE_PREFIX_OPERATOR:
                switch( specific )
                {
                    case REGEX_PATTERN:
                    case NOT:
                    case PREFIX_PLUS:
                    case PREFIX_PLUS_PLUS:
                    case PREFIX_MINUS:
                    case PREFIX_MINUS_MINUS:
                    case SYNTH_CAST:
                        return true;
                }
                break;

            case POSTFIX_OPERATOR:
                switch( specific )
                {
                    case PLUS_PLUS:
                    case POSTFIX_PLUS_PLUS:
                    case MINUS_MINUS:
                    case POSTFIX_MINUS_MINUS:
                        return true;
                }
                break;

            case INFIX_OPERATOR:
                switch( specific )
                {
                    case DOT:
                    case NAVIGATE:
                    case LOGICAL_OR:
                    case LOGICAL_AND:
                    case BITWISE_OR:
                    case BITWISE_AND:
                    case BITWISE_XOR:
                    case LEFT_SHIFT:
                    case RIGHT_SHIFT:
                    case RIGHT_SHIFT_UNSIGNED:
                    case FIND_REGEX:
                    case MATCH_REGEX:
                    case DOT_DOT:
                    case DOT_DOT_DOT:
                    case KEYWORD_INSTANCEOF:
                        return true;
                }

                return (specific >= COMPARE_NOT_EQUAL && specific <= COMPARE_TO) || (specific >= PLUS && specific <= MOD_EQUAL) || specific == EQUAL || (specific >= PLUS_EQUAL && specific <= POWER_EQUAL) || (specific >= LOGICAL_OR_EQUAL && specific <= LOGICAL_AND_EQUAL)
                                 || (specific >= LEFT_SHIFT_EQUAL && specific <= RIGHT_SHIFT_UNSIGNED_EQUAL) || (specific >= BITWISE_OR_EQUAL && specific <= BITWISE_XOR_EQUAL);

            case PREFIX_OR_INFIX_OPERATOR:
                switch( specific )
                {
                    case POWER:
                    case PLUS:
                    case MINUS:
                    case PREFIX_PLUS:
                    case PREFIX_MINUS:
                        return true;
                }
                break;


            case KEYWORD:
                return specific >= KEYWORD_PRIVATE && specific <= KEYWORD_GOTO;

            case SYMBOL:
                return specific >= NEWLINE && specific <= PIPE;

            case LITERAL:
                return specific >= STRING && specific <= DECIMAL_NUMBER;

            case NUMBER:
                return specific == INTEGER_NUMBER || specific == DECIMAL_NUMBER;

            case SIGN:
                switch( specific )
                {
                    case PLUS:
                    case MINUS:
                        return true;
                }
                break;

            case NAMED_VALUE:
                return specific >= KEYWORD_TRUE && specific <= KEYWORD_NULL;

            case TRUTH_VALUE:
                return specific == KEYWORD_TRUE || specific == KEYWORD_FALSE;

            case TYPE_NAME:
                if( specific == IDENTIFIER )
                {
                    return true;
                }

                /* FALL THROUGH */

            case PRIMITIVE_TYPE:
                return specific >= KEYWORD_VOID && specific <= KEYWORD_CHAR;

            case CREATABLE_TYPE_NAME:
                if( specific == IDENTIFIER )
                {
                    return true;
                }

                /* FALL THROUGH */

            case CREATABLE_PRIMITIVE_TYPE:
                return specific >= KEYWORD_BOOLEAN && specific <= KEYWORD_CHAR;

            case LOOP:
                switch( specific )
                {
                    case KEYWORD_DO:
                    case KEYWORD_WHILE:
                    case KEYWORD_FOR:
                        return true;
                }
                break;

            case RESERVED_KEYWORD:
                return specific >= KEYWORD_CONST && specific <= KEYWORD_GOTO;

            case KEYWORD_IDENTIFIER:
                switch( specific )
                {
                    case KEYWORD_CLASS:
                    case KEYWORD_INTERFACE:
                    case KEYWORD_MIXIN:
                    case KEYWORD_DEF:
                    case KEYWORD_DEFMACRO:
                    case KEYWORD_IN:
                    case KEYWORD_PROPERTY:
                        return true;
                }
                break;

            case SYNTHETIC:
                return specific >= SYNTH_COMPILATION_UNIT && specific <= SYNTH_VARIABLE_DECLARATION;

            case TYPE_DECLARATION:
                return specific >= KEYWORD_CLASS && specific <= KEYWORD_MIXIN;

            case DECLARATION_MODIFIER:
                return specific >= KEYWORD_PRIVATE && specific <= KEYWORD_STATIC;

            case MATCHED_CONTAINER:
                switch( specific )
                {
                    case LEFT_CURLY_BRACE:
                    case RIGHT_CURLY_BRACE:
                    case LEFT_SQUARE_BRACKET:
                    case RIGHT_SQUARE_BRACKET:
                    case LEFT_PARENTHESIS:
                    case RIGHT_PARENTHESIS:
                        return true;
                }
                break;

            case LEFT_OF_MATCHED_CONTAINER:
                switch( specific )
                {
                    case LEFT_CURLY_BRACE:
                    case LEFT_SQUARE_BRACKET:
                    case LEFT_PARENTHESIS:
                        return true;
                }
                break;

            case RIGHT_OF_MATCHED_CONTAINER:
                switch( specific )
                {
                    case RIGHT_CURLY_BRACE:
                    case RIGHT_SQUARE_BRACKET:
                    case RIGHT_PARENTHESIS:
                        return true;
                }
                break;


            case PARAMETER_TERMINATORS:
                return specific == RIGHT_PARENTHESIS || specific == COMMA;

            case ARRAY_ITEM_TERMINATORS:
                return specific == RIGHT_SQUARE_BRACKET || specific == COMMA;

            case TYPE_LIST_TERMINATORS:
                switch( specific )
                {
                    case KEYWORD_IMPLEMENTS:
                    case KEYWORD_THROWS:
                    case LEFT_CURLY_BRACE:
                    case COMMA:
                        return true;
                }
                break;

            case OPTIONAL_DATATYPE_FOLLOWERS:
                switch( specific )
                {
                    case IDENTIFIER:
                    case LEFT_SQUARE_BRACKET:
                    case DOT:
                        return true;
                }
                break;

            case SWITCH_BLOCK_TERMINATORS:
                if( specific == RIGHT_CURLY_BRACE )
                {
                    return true;
                }

                /* FALL THROUGH */

            case SWITCH_ENTRIES:
                return specific == KEYWORD_CASE || specific == KEYWORD_DEFAULT;

            case METHOD_CALL_STARTERS:
                if( specific >= STRING && specific <= DECIMAL_NUMBER )
                {
                    return true;
                }
                switch( specific )
                {
                    case LEFT_PARENTHESIS:
                    case GSTRING_START:
                    case SYNTH_GSTRING:
                    case KEYWORD_NEW:
                        return true;
                }
                break;

            case UNSAFE_OVER_NEWLINES:
                if( ofType(specific, SYMBOL) )
                {
                    switch( specific )
                    {
                        case LEFT_CURLY_BRACE:
                        case LEFT_PARENTHESIS:
                        case LEFT_SQUARE_BRACKET:
                        case PLUS:
                        case PLUS_PLUS:
                        case MINUS:
                        case MINUS_MINUS:
                        case REGEX_PATTERN:
                        case NOT:
                            return true;
                    }

                    return false;
                }

                switch( specific )
                {
                    case KEYWORD_INSTANCEOF:
                    case GSTRING_EXPRESSION_START:
                    case GSTRING_EXPRESSION_END:
                    case GSTRING_END:
                        return false;
                }

                return true;

            case PRECLUDES_CAST_OPERATOR:
                switch( specific )
                {
                    case PLUS:
                    case MINUS:
                    case PREFIX_MINUS:
                    case PREFIX_MINUS_MINUS:
                    case PREFIX_PLUS:
                    case PREFIX_PLUS_PLUS:
                    case LEFT_PARENTHESIS:
                        return false;
                }

                return !ofType( specific, COMPLEX_EXPRESSION );




            case OPERATOR_EXPRESSION:
                return specific >= DOT && specific <= RIGHT_SHIFT_UNSIGNED;

            case SYNTH_EXPRESSION:
                switch( specific )
                {
                    case SYNTH_CAST:
                    case SYNTH_CLOSURE:
                    case SYNTH_TERNARY:
                        return true;
                }
                break;

            case KEYWORD_EXPRESSION:
                switch( specific )
                {
                    case KEYWORD_NEW:
                    case KEYWORD_THIS:
                    case KEYWORD_SUPER:
                    case KEYWORD_INSTANCEOF:
                    case KEYWORD_TRUE:
                    case KEYWORD_FALSE:
                    case KEYWORD_NULL:
                        return true;
                }
                break;

            case LITERAL_EXPRESSION:
                return specific >= STRING && specific <= DECIMAL_NUMBER;

            case ARRAY_EXPRESSION:
                return specific == LEFT_SQUARE_BRACKET;

            case EXPRESSION:
                if( specific >= DOT && specific <= RIGHT_SHIFT_UNSIGNED )
                {
                    return true;
                }

                if( specific >= STRING && specific <= DECIMAL_NUMBER )
                {
                    return true;
                }

                switch( specific )
                {
                    case SYNTH_CAST:
                    case SYNTH_CLOSURE:
                    case SYNTH_TERNARY:
                    case SYNTH_GSTRING:
                    case KEYWORD_NEW:
                    case KEYWORD_THIS:
                    case KEYWORD_SUPER:
                    case KEYWORD_INSTANCEOF:
                    case KEYWORD_TRUE:
                    case KEYWORD_FALSE:
                    case KEYWORD_NULL:
                    case LEFT_SQUARE_BRACKET:
                        return true;
                }
                break;

            case COMPLEX_EXPRESSION:
                switch( specific )
                {
                    case KEYWORD_NEW:
                    case SYNTH_METHOD_CALL:
                    case SYNTH_GSTRING:
                    case SYNTH_LIST:
                    case SYNTH_MAP:
                    case SYNTH_CLOSURE:
                    case SYNTH_TERNARY:
                    case SYNTH_VARIABLE_DECLARATION:
                        return true;
                }

                /* FALL THROUGH */

            case SIMPLE_EXPRESSION:
                if( specific >= STRING && specific <= DECIMAL_NUMBER ) {
                    return true;
                }

                switch( specific ) {
                    case KEYWORD_SUPER:
                    case KEYWORD_THIS:
                    case KEYWORD_TRUE:
                    case KEYWORD_FALSE:
                    case KEYWORD_NULL:
                        return true;
                }

                break;
        }

        return false;
    }




  //---------------------------------------------------------------------------
  // TYPE COERSIONS


   /**
    *  Given two types, returns true if the first can be viewed as the second.
    *  NOTE that <code>canMean()</code> is orthogonal to <code>ofType()</code>.
    */

    public static boolean canMean( int actual, int preferred ) {

        if( actual == preferred ) {
            return true;
        }

        switch( preferred ) {

            case SYNTH_PARAMETER_DECLARATION:
            case IDENTIFIER:
                switch( actual ) {
                    case IDENTIFIER:
                    case KEYWORD_DEF:
                    case KEYWORD_DEFMACRO:
                    case KEYWORD_CLASS:
                    case KEYWORD_INTERFACE:
                    case KEYWORD_MIXIN:
                        return true;
                }
                break;

            case SYNTH_CLASS:
            case SYNTH_INTERFACE:
            case SYNTH_MIXIN:
            case SYNTH_METHOD:
            case SYNTH_PROPERTY:
                return actual == IDENTIFIER;

            case SYNTH_LIST:
            case SYNTH_MAP:
                return actual == LEFT_SQUARE_BRACKET;

            case SYNTH_CAST:
                return actual == LEFT_PARENTHESIS;

            case SYNTH_BLOCK:
            case SYNTH_CLOSURE:
                return actual == LEFT_CURLY_BRACE;

            case SYNTH_LABEL:
                return actual == COLON;

            case SYNTH_VARIABLE_DECLARATION:
                return actual == IDENTIFIER;
        }

        return false;
    }



   /**
    *  Converts a node from a generic type to a specific prefix type.
    *  Throws a <code>GroovyBugError</code> if the type can't be converted
    *  and requested.
    */

    public static void makePrefix( CSTNode node, boolean throwIfInvalid ) {

        switch( node.getMeaning() ) {
            case PLUS:
                node.setMeaning( PREFIX_PLUS );
                break;

            case MINUS:
                node.setMeaning( PREFIX_MINUS );
                break;

            case PLUS_PLUS:
                node.setMeaning( PREFIX_PLUS_PLUS );
                break;

            case MINUS_MINUS:
                node.setMeaning( PREFIX_MINUS_MINUS );
                break;

            default:
                if( throwIfInvalid ) {
                    throw new GroovyBugError( "cannot convert to prefix for type [" + node.getMeaning() + "]" );
                }
        }

    }



   /**
    *  Converts a node from a generic type to a specific postfix type.
    *  Throws a <code>GroovyBugError</code> if the type can't be converted.
    */

    public static void makePostfix( CSTNode node, boolean throwIfInvalid ) {

        switch( node.getMeaning() ) {
            case PLUS_PLUS:
                node.setMeaning( POSTFIX_PLUS_PLUS );
                break;

            case MINUS_MINUS:
                node.setMeaning( POSTFIX_MINUS_MINUS );
                break;

            default:
                if( throwIfInvalid ) {
                    throw new GroovyBugError( "cannot convert to postfix for type [" + node.getMeaning() + "]" );
                }
        }

    }




  //---------------------------------------------------------------------------
  // OPERATOR PRECEDENCE


   /**
    *  Returns the precedence of the specified operator.  Non-operator's will
    *  receive -1 or a GroovyBugError, depending on your preference.
    */

    public static int getPrecedence( int type, boolean throwIfInvalid ) {

        switch( type ) {

            case LEFT_PARENTHESIS:
                return 0;

            case EQUAL:
            case PLUS_EQUAL:
            case MINUS_EQUAL:
            case MULTIPLY_EQUAL:
            case DIVIDE_EQUAL:
            case INTDIV_EQUAL:
            case MOD_EQUAL:
            case POWER_EQUAL:
            case LOGICAL_OR_EQUAL:
            case LOGICAL_AND_EQUAL:
            case LEFT_SHIFT_EQUAL:
            case RIGHT_SHIFT_EQUAL:
            case RIGHT_SHIFT_UNSIGNED_EQUAL:
            case BITWISE_OR_EQUAL:
            case BITWISE_AND_EQUAL:
            case BITWISE_XOR_EQUAL:
                return 5;

            case QUESTION:
                return 10;

            case LOGICAL_OR:
                return 15;

            case LOGICAL_AND:
                return 20;

            case BITWISE_OR:
            case BITWISE_AND:
            case BITWISE_XOR:
                return 22;

            case COMPARE_IDENTICAL:
            case COMPARE_NOT_IDENTICAL:
                return 24;

            case COMPARE_NOT_EQUAL:
            case COMPARE_EQUAL:
            case COMPARE_LESS_THAN:
            case COMPARE_LESS_THAN_EQUAL:
            case COMPARE_GREATER_THAN:
            case COMPARE_GREATER_THAN_EQUAL:
            case COMPARE_TO:
            case FIND_REGEX:
            case MATCH_REGEX:
            case KEYWORD_INSTANCEOF:
                return 25;

            case DOT_DOT:
            case DOT_DOT_DOT:
                return 30;

            case LEFT_SHIFT:
            case RIGHT_SHIFT:
            case RIGHT_SHIFT_UNSIGNED:
                return 35;

            case PLUS:
            case MINUS:
                return 40;

            case MULTIPLY:
            case DIVIDE:
            case INTDIV:
            case MOD:
                return 45;

            case NOT:
            case REGEX_PATTERN:
                return 50;

            case SYNTH_CAST:
                return 55;

            case PLUS_PLUS:
            case MINUS_MINUS:
            case PREFIX_PLUS_PLUS:
            case PREFIX_MINUS_MINUS:
            case POSTFIX_PLUS_PLUS:
            case POSTFIX_MINUS_MINUS:
                return 65;

            case PREFIX_PLUS:
            case PREFIX_MINUS:
                return 70;

            case POWER:
                return 72;

            case SYNTH_METHOD:
            case LEFT_SQUARE_BRACKET:
                return 75;

            case DOT:
            case NAVIGATE:
                return 80;

            case KEYWORD_NEW:
                return 85;
        }

        if( throwIfInvalid ) {
            throw new GroovyBugError( "precedence requested for non-operator" );
        }

        return -1;
    }




  //---------------------------------------------------------------------------
  // TEXTS

    private static final Map<Integer,String> TEXTS  = new HashMap<Integer,String>();  // symbol/keyword type -> text
    private static final Map<String,Integer> LOOKUP = new HashMap<String,Integer>();  // text -> symbol/keyword type
    private static final Set<String> KEYWORDS = new HashSet<String>();  // valid keywords

    public static Collection<String> getKeywords() {
        return Collections.unmodifiableSet(KEYWORDS);
    }

    public static boolean isKeyword(final String text) {
        return KEYWORDS.contains(text);
    }

   /**
    *  Returns the type for the specified symbol/keyword text.  Returns UNKNOWN
    *  if the text isn't found.  You can filter finds on a type.
    */

    public static int lookup( String text, int filter ) {
        int type = UNKNOWN;

        if( LOOKUP.containsKey(text) ) {
            type = LOOKUP.get(text);
            if( filter != UNKNOWN && !ofType(type, filter) ) {
                type = UNKNOWN;
            }
        }

        return type;
    }


   /**
    *  Returns the type for the specified keyword text.  Returns UNKNOWN
    *  if the text isn't found.
    */

    public static int lookupKeyword( String text ) {
        return lookup( text, KEYWORD );
    }


   /**
    *  Returns the type for the specified symbol text.  Returns UNKNOWN
    *  if the text isn't found.
    */

    public static int lookupSymbol( String text ) {
        return lookup( text, SYMBOL );
    }


   /**
    *  Returns the text for the specified type.  Returns "" if the
    *  text isn't found.
    */

    public static String getText( int type ) {
        String text = "";

        if( TEXTS.containsKey(type) ) {
            text = TEXTS.get( type );
        }

        return text;
    }


    /**
     *  Adds a element to the TEXTS and LOOKUP.
     */
    private static void addTranslation( String text, int type ) {
        TEXTS.put( type, text );
        LOOKUP.put( text, type );
    }

    /**
     *  Adds a element to the KEYWORDS, TEXTS and LOOKUP.
     */
    private static void addKeyword( String text, int type ) {
        KEYWORDS.add(text);
        addTranslation(text, type);
    }

    static {

        //
        // SYMBOLS

        addTranslation( "\n"          , NEWLINE                     );

        addTranslation( "{"           , LEFT_CURLY_BRACE            );
        addTranslation( "}"           , RIGHT_CURLY_BRACE           );
        addTranslation( "["           , LEFT_SQUARE_BRACKET         );
        addTranslation( "]"           , RIGHT_SQUARE_BRACKET        );
        addTranslation( "("           , LEFT_PARENTHESIS            );
        addTranslation( ")"           , RIGHT_PARENTHESIS           );

        addTranslation( "."           , DOT                         );
        addTranslation( ".."          , DOT_DOT                     );
        addTranslation( "..."         , DOT_DOT_DOT                 );

        addTranslation( "->"          , NAVIGATE                    );

        addTranslation( "=~"          , FIND_REGEX                  );
        addTranslation( "==~"         , MATCH_REGEX                 );
        addTranslation( "~"           , REGEX_PATTERN               );

        addTranslation( "="           , EQUAL                       );

        addTranslation( "!="          , COMPARE_NOT_EQUAL           );
        addTranslation( "==="         , COMPARE_IDENTICAL           );
        addTranslation( "!=="         , COMPARE_NOT_IDENTICAL       );
        addTranslation( "=="          , COMPARE_EQUAL               );
        addTranslation( "<"           , COMPARE_LESS_THAN           );
        addTranslation( "<="          , COMPARE_LESS_THAN_EQUAL     );
        addTranslation( ">"           , COMPARE_GREATER_THAN        );
        addTranslation( ">="          , COMPARE_GREATER_THAN_EQUAL  );
        addTranslation( "<=>"         , COMPARE_TO                  );

        addTranslation( "!"           , NOT                         );
        addTranslation( "||"          , LOGICAL_OR                  );
        addTranslation( "&&"          , LOGICAL_AND                 );

        addTranslation( "||="         , LOGICAL_OR_EQUAL            );
        addTranslation( "&&="         , LOGICAL_AND_EQUAL           );

        addTranslation( "+"           , PLUS                        );
        addTranslation( "-"           , MINUS                       );
        addTranslation( "*"           , MULTIPLY                    );
        addTranslation( "/"           , DIVIDE                      );
        addTranslation( "\\"          , INTDIV                      );
        addTranslation( "%"           , MOD                         );

        addTranslation( "**"          , POWER                       );
        addTranslation( "+="          , PLUS_EQUAL                  );
        addTranslation( "-="          , MINUS_EQUAL                 );
        addTranslation( "*="          , MULTIPLY_EQUAL              );
        addTranslation( "/="          , DIVIDE_EQUAL                );
        addTranslation( "\\="         , INTDIV_EQUAL                );
        addTranslation( "%="          , MOD_EQUAL                   );
        addTranslation( "**="         , POWER_EQUAL                 );

        addTranslation( "++"          , PLUS_PLUS                   );
        addTranslation( "--"          , MINUS_MINUS                 );

        addTranslation( "<<"          , LEFT_SHIFT                  );
        addTranslation( ">>"          , RIGHT_SHIFT                 );
        addTranslation( ">>>"         , RIGHT_SHIFT_UNSIGNED        );

        addTranslation( "<<="         , LEFT_SHIFT_EQUAL            );
        addTranslation( ">>="         , RIGHT_SHIFT_EQUAL           );
        addTranslation( ">>>="        , RIGHT_SHIFT_UNSIGNED_EQUAL  );

        addTranslation( "&"           , BITWISE_AND                 );
        addTranslation( "^"           , BITWISE_XOR                 );

        addTranslation( "|="          , BITWISE_OR_EQUAL           );
        addTranslation( "&="          , BITWISE_AND_EQUAL           );
        addTranslation( "^="          , BITWISE_XOR_EQUAL           );

        addTranslation( ","           , COMMA                       );
        addTranslation( ":"           , COLON                       );
        addTranslation( ";"           , SEMICOLON                   );
        addTranslation( "?"           , QUESTION                    );
        addTranslation( "|"           , PIPE                        );

        addTranslation( "${}"         , GSTRING_EXPRESSION_START    );


        //
        // Keywords

        addKeyword( "abstract"    , KEYWORD_ABSTRACT            );
        addKeyword( "as"          , KEYWORD_AS                  );
        addKeyword( "assert"      , KEYWORD_ASSERT              );
        addKeyword( "break"       , KEYWORD_BREAK               );
        addKeyword( "case"        , KEYWORD_CASE                );
        addKeyword( "catch"       , KEYWORD_CATCH               );
        addKeyword( "class"       , KEYWORD_CLASS               );
        addKeyword( "const"       , KEYWORD_CONST               );
        addKeyword( "continue"    , KEYWORD_CONTINUE            );
        addKeyword( "def"         , KEYWORD_DEF                 );
        addKeyword( "defmacro"    , KEYWORD_DEF                 ); // xxx br defmacro
        addKeyword( "default"     , KEYWORD_DEFAULT             );
        addKeyword( "do"          , KEYWORD_DO                  );
        addKeyword( "else"        , KEYWORD_ELSE                );
        addKeyword( "extends"     , KEYWORD_EXTENDS             );
        addKeyword( "final"       , KEYWORD_FINAL               );
        addKeyword( "finally"     , KEYWORD_FINALLY             );
        addKeyword( "for"         , KEYWORD_FOR                 );
        addKeyword( "goto"        , KEYWORD_GOTO                );
        addKeyword( "if"          , KEYWORD_IF                  );
        addKeyword( "in"          , KEYWORD_IN                  );
        addKeyword( "implements"  , KEYWORD_IMPLEMENTS          );
        addKeyword( "import"      , KEYWORD_IMPORT              );
        addKeyword( "instanceof"  , KEYWORD_INSTANCEOF          );
        addKeyword( "interface"   , KEYWORD_INTERFACE           );
        addKeyword( "mixin"       , KEYWORD_MIXIN               );
        addKeyword( "native"      , KEYWORD_NATIVE              );
        addKeyword( "new"         , KEYWORD_NEW                 );
        addKeyword( "package"     , KEYWORD_PACKAGE             );
        addKeyword( "private"     , KEYWORD_PRIVATE             );
        addKeyword( "property"    , KEYWORD_PROPERTY            );
        addKeyword( "protected"   , KEYWORD_PROTECTED           );
        addKeyword( "public"      , KEYWORD_PUBLIC              );
        addKeyword( "return"      , KEYWORD_RETURN              );
        addKeyword( "static"      , KEYWORD_STATIC              );
        addKeyword( "super"       , KEYWORD_SUPER               );
        addKeyword( "switch"      , KEYWORD_SWITCH              );
        addKeyword( "synchronized", KEYWORD_SYNCHRONIZED        );
        addKeyword( "this"        , KEYWORD_THIS                );
        addKeyword( "throw"       , KEYWORD_THROW               );
        addKeyword( "throws"      , KEYWORD_THROWS              );
        addKeyword( "transient"   , KEYWORD_TRANSIENT           );
        addKeyword( "try"         , KEYWORD_TRY                 );
        addKeyword( "volatile"    , KEYWORD_VOLATILE            );
        addKeyword( "while"       , KEYWORD_WHILE               );
        addKeyword( "true"        , KEYWORD_TRUE                );
        addKeyword( "false"       , KEYWORD_FALSE               );
        addKeyword( "null"        , KEYWORD_NULL                );
        addKeyword( "void"        , KEYWORD_VOID                );
        addKeyword( "boolean"     , KEYWORD_BOOLEAN             );
        addKeyword( "byte"        , KEYWORD_BYTE                );
        addKeyword( "int"         , KEYWORD_INT                 );
        addKeyword( "short"       , KEYWORD_SHORT               );
        addKeyword( "long"        , KEYWORD_LONG                );
        addKeyword( "float"       , KEYWORD_FLOAT               );
        addKeyword( "double"      , KEYWORD_DOUBLE              );
        addKeyword( "char"        , KEYWORD_CHAR                );
    }




  //---------------------------------------------------------------------------
  // DESCRIPTIONS


    private static final Map<Integer,String> DESCRIPTIONS = new HashMap<Integer,String>();


   /**
    *  Gets the description for the specified type.
    */

    public static String getDescription( int type ) {
        if (DESCRIPTIONS.containsKey(type)) {
            return DESCRIPTIONS.get(type);
        }

        return "<>";
    }


   /**
    *  Adds a description to the set.
    */

    private static void addDescription(int type, String description) {
        if (description.startsWith("<") && description.endsWith(">")) {
            DESCRIPTIONS.put(type, description);
        }
        else {
            DESCRIPTIONS.put(type, '"' + description + '"');
        }
    }


    static {
        for (Map.Entry<String,Integer> entry : LOOKUP.entrySet()) {
            addDescription(entry.getValue(), entry.getKey());
        }

        addDescription( NEWLINE                     , "<newline>"        );
        addDescription( PREFIX_PLUS_PLUS            , "<prefix ++>"      );
        addDescription( POSTFIX_PLUS_PLUS           , "<postfix ++>"     );
        addDescription( PREFIX_MINUS_MINUS          , "<prefix -->"      );
        addDescription( POSTFIX_MINUS_MINUS         , "<postfix -->"     );
        addDescription( PREFIX_PLUS                 , "<positive>"       );
        addDescription( PREFIX_MINUS                , "<negative>"       );

        addDescription( STRING                      , "<string literal>" );
        addDescription( IDENTIFIER                  , "<identifier>"     );
        addDescription( INTEGER_NUMBER              , "<integer>"        );
        addDescription( DECIMAL_NUMBER              , "<decimal>"        );

        addDescription( SYNTH_COMPILATION_UNIT      , "<compilation unit>" );
        addDescription( SYNTH_CLASS                 , "<class>"          );
        addDescription( SYNTH_INTERFACE             , "<interface>"      );
        addDescription( SYNTH_MIXIN                 , "<mixin>"          );
        addDescription( SYNTH_METHOD                , "<method>"         );
        addDescription( SYNTH_METHOD_CALL           , "<method call>"    );
        addDescription( SYNTH_PROPERTY              , "<property>"       );
        addDescription( SYNTH_PARAMETER_DECLARATION , "<parameter>"      );
        addDescription( SYNTH_LIST                  , "<list>"           );
        addDescription( SYNTH_MAP                   , "<map>"            );
        addDescription( SYNTH_TUPLE                 , "<tuple>"          );
        addDescription( SYNTH_GSTRING               , "<gstring>"        );
        addDescription( SYNTH_CAST                  , "<cast>"           );
        addDescription( SYNTH_BLOCK                 , "<block>"          );
        addDescription( SYNTH_CLOSURE               , "<closure>"        );
        addDescription( SYNTH_TERNARY               , "<ternary>"        );
        addDescription( SYNTH_LABEL                 , "<label>"          );
        addDescription( SYNTH_VARIABLE_DECLARATION  , "<variable declaration>"       );

        addDescription( GSTRING_START               , "<start of gstring tokens>"    );
        addDescription( GSTRING_END                 , "<end of gstring tokens>"      );
        addDescription( GSTRING_EXPRESSION_START    , "<start of gstring expression>");
        addDescription( GSTRING_EXPRESSION_END      , "<end of gstring expression>"  );

        addDescription( ASSIGNMENT_OPERATOR         , "<assignment operator>"        );
        addDescription( COMPARISON_OPERATOR         , "<comparison operator>"        );
        addDescription( MATH_OPERATOR               , "<math operator>"              );
        addDescription( LOGICAL_OPERATOR            , "<logical operator>"           );
        addDescription( BITWISE_OPERATOR            , "<bitwise operator>"           );
        addDescription( RANGE_OPERATOR              , "<range operator>"             );
        addDescription( REGEX_COMPARISON_OPERATOR   , "<regex comparison operator>"  );
        addDescription( DEREFERENCE_OPERATOR        , "<dereference operator>"       );
        addDescription( PREFIX_OPERATOR             , "<prefix operator>"            );
        addDescription( POSTFIX_OPERATOR            , "<postfix operator>"           );
        addDescription( INFIX_OPERATOR              , "<infix operator>"             );
        addDescription( KEYWORD                     , "<keyword>"                    );
        addDescription( LITERAL                     , "<literal>"                    );
        addDescription( NUMBER                      , "<number>"                     );
        addDescription( NAMED_VALUE                 , "<named value>"                );
        addDescription( TRUTH_VALUE                 , "<truth value>"                );
        addDescription( PRIMITIVE_TYPE              , "<primitive type>"             );
        addDescription( CREATABLE_PRIMITIVE_TYPE    , "<creatable primitive type>"   );
        addDescription( LOOP                        , "<loop>"                       );
        addDescription( RESERVED_KEYWORD            , "<reserved keyword>"           );
        addDescription( SYNTHETIC                   , "<synthetic>"                  );
        addDescription( TYPE_DECLARATION            , "<type declaration>"           );
        addDescription( DECLARATION_MODIFIER        , "<declaration modifier>"       );
        addDescription( TYPE_NAME                   , "<type name>"                  );
        addDescription( CREATABLE_TYPE_NAME         , "<creatable type name>"        );
        addDescription( MATCHED_CONTAINER           , "<matched container>"          );
        addDescription( LEFT_OF_MATCHED_CONTAINER   , "<left of matched container>"  );
        addDescription( RIGHT_OF_MATCHED_CONTAINER  , "<right of matched container>" );
        addDescription( SWITCH_ENTRIES              , "<valid in a switch body>"     );
    }

}
