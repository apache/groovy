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
package org.codehaus.groovy.antlr.treewalker;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import java.io.PrintStream;

/**
 * An antlr AST visitor that prints a format suitable for viewing in http://freemind.sourceforge.net
 */

public class MindMapPrinter extends VisitorAdapter {
    private final String[] tokenNames;
    private final PrintStream out;
    private int depth;
    private SourceBuffer sourceBuffer;

    /**
     * A visitor that prints a format suitable for viewing in http://freemind.sourceforge.net
     * @param out where to print the mindmap file contents to
     * @param tokenNames an array of token names from antlr
     */

    public MindMapPrinter(PrintStream out,String[] tokenNames) {
        this.tokenNames = tokenNames;
        this.out = out;
    }

    public MindMapPrinter(PrintStream out,String[] tokenNames, SourceBuffer sourceBuffer) {
        this.tokenNames = tokenNames;
        this.out = out;
        this.sourceBuffer = sourceBuffer;
    }
    public void setUp() {
        depth = 0;
        out.println("<map version='0.7.1'><node TEXT='AST'>");
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            depth++;
            String name = getName(t);
            String colour = getColour(t);
            String folded = getFolded(t);
            out.print("<node TEXT='" + name + "' POSITION='right'" + colour + folded + ">");
        } else if (visit == CLOSING_VISIT) {
            out.println("</node>");
            depth--;
        }
    }

    public void tearDown() {
        out.println("</node></map>");
    }

    private String getFolded(GroovySourceAST t) {
        if (depth > 2 && t.getNumberOfChildren() > 0) {
            switch (t.getType()) {
                case GroovyTokenTypes.EXPR :
                case GroovyTokenTypes.METHOD_DEF :
                case GroovyTokenTypes.VARIABLE_DEF :
                    return " FOLDED='true'";
            }
        }
        if (t.getType() == GroovyTokenTypes.IMPORT) {
            return " FOLDED='true'";
        }
        return "";
    }

    private static String getColour(GroovySourceAST t) {
        String colour = "";
        String black = " COLOR=\"#000000\"";
        String cyan = " COLOR=\"#006699\"";
        String blue = " COLOR=\"#17178B\"";
        String green = " COLOR=\"#008000\"";
        switch (t.getType()) {
            case GroovyTokenTypes.ABSTRACT                      :
            case GroovyTokenTypes.ANNOTATION                    :
            case GroovyTokenTypes.ANNOTATIONS                   :
            case GroovyTokenTypes.ANNOTATION_ARRAY_INIT         :
            case GroovyTokenTypes.ANNOTATION_DEF                :
            case GroovyTokenTypes.ANNOTATION_FIELD_DEF          :
            case GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR  :
            case GroovyTokenTypes.ARRAY_DECLARATOR              :
            case GroovyTokenTypes.ASSIGN                        :
            case GroovyTokenTypes.AT                            :
            case GroovyTokenTypes.BAND                          :
            case GroovyTokenTypes.BAND_ASSIGN                   :
            case GroovyTokenTypes.BIG_SUFFIX                    :
            case GroovyTokenTypes.BLOCK                         :
            case GroovyTokenTypes.BNOT                          :
            case GroovyTokenTypes.BOR                           :
            case GroovyTokenTypes.BOR_ASSIGN                    :
            case GroovyTokenTypes.BSR                           :
            case GroovyTokenTypes.BSR_ASSIGN                    :
            case GroovyTokenTypes.BXOR                          :
            case GroovyTokenTypes.BXOR_ASSIGN                   :
            case GroovyTokenTypes.CASE_GROUP                    :
            case GroovyTokenTypes.CLOSABLE_BLOCK                :
            case GroovyTokenTypes.CLOSABLE_BLOCK_OP             :
            case GroovyTokenTypes.COLON                         :
            case GroovyTokenTypes.COMMA                         :
            case GroovyTokenTypes.COMPARE_TO                    :
            case GroovyTokenTypes.CTOR_CALL                     :
            case GroovyTokenTypes.CTOR_IDENT                    :
            case GroovyTokenTypes.DEC                           :
            case GroovyTokenTypes.DIGIT                         :
            case GroovyTokenTypes.DIV                           :
            case GroovyTokenTypes.DIV_ASSIGN                    :
            case GroovyTokenTypes.DOLLAR                        :
            case GroovyTokenTypes.DOT                           :
            case GroovyTokenTypes.DYNAMIC_MEMBER                :
            case GroovyTokenTypes.ELIST                         :
            case GroovyTokenTypes.EMPTY_STAT                    :
            case GroovyTokenTypes.ENUM_CONSTANT_DEF             :
            case GroovyTokenTypes.ENUM_DEF                      :
            case GroovyTokenTypes.EOF                           :
            case GroovyTokenTypes.EQUAL                         :
            case GroovyTokenTypes.ESC                           :
            case GroovyTokenTypes.EXPONENT                      :
            case GroovyTokenTypes.EXPR                          :
            case GroovyTokenTypes.FINAL                         :
            case GroovyTokenTypes.FLOAT_SUFFIX                  :
            case GroovyTokenTypes.FOR_CONDITION                 :
            case GroovyTokenTypes.FOR_EACH_CLAUSE               :
            case GroovyTokenTypes.FOR_INIT                      :
            case GroovyTokenTypes.FOR_IN_ITERABLE               :
            case GroovyTokenTypes.FOR_ITERATOR                  :
            case GroovyTokenTypes.GE                            :
            case GroovyTokenTypes.GT                            :
            case GroovyTokenTypes.HEX_DIGIT                     :
            case GroovyTokenTypes.IMPLICIT_PARAMETERS           :
            case GroovyTokenTypes.INC                           :
            case GroovyTokenTypes.INDEX_OP                      :
            case GroovyTokenTypes.INSTANCE_INIT                 :
            case GroovyTokenTypes.INTERFACE_DEF                 :
            case GroovyTokenTypes.LABELED_ARG                   :
            case GroovyTokenTypes.LABELED_STAT                  :
            case GroovyTokenTypes.LAND                          :
            case GroovyTokenTypes.LBRACK                        :
            case GroovyTokenTypes.LCURLY                        :
            case GroovyTokenTypes.LE                            :
            case GroovyTokenTypes.LETTER                        :
            case GroovyTokenTypes.LIST_CONSTRUCTOR              :
            case GroovyTokenTypes.LNOT                          :
            case GroovyTokenTypes.LOR                           :
            case GroovyTokenTypes.LPAREN                        :
            case GroovyTokenTypes.LT                            :
            case GroovyTokenTypes.MAP_CONSTRUCTOR               :
            case GroovyTokenTypes.MEMBER_POINTER                :
            case GroovyTokenTypes.METHOD_CALL                   :
            case GroovyTokenTypes.METHOD_DEF                    :
            case GroovyTokenTypes.MINUS                         :
            case GroovyTokenTypes.MINUS_ASSIGN                  :
            case GroovyTokenTypes.ML_COMMENT                    :
            case GroovyTokenTypes.MOD                           :
            case GroovyTokenTypes.MODIFIERS                     :
            case GroovyTokenTypes.MOD_ASSIGN                    :
            case GroovyTokenTypes.NLS                           :
            case GroovyTokenTypes.NOT_EQUAL                     :
            case GroovyTokenTypes.NULL_TREE_LOOKAHEAD           :
            case GroovyTokenTypes.NUM_BIG_DECIMAL               :
            case GroovyTokenTypes.NUM_BIG_INT                   :
            case GroovyTokenTypes.NUM_DOUBLE                    :
            case GroovyTokenTypes.NUM_FLOAT                     :
            case GroovyTokenTypes.NUM_INT                       :
            case GroovyTokenTypes.NUM_LONG                      :
            case GroovyTokenTypes.OBJBLOCK                      :
            case GroovyTokenTypes.ONE_NL                        :
            case GroovyTokenTypes.OPTIONAL_DOT                  :
            case GroovyTokenTypes.PARAMETERS                    :
            case GroovyTokenTypes.PARAMETER_DEF                 :
            case GroovyTokenTypes.PLUS                          :
            case GroovyTokenTypes.PLUS_ASSIGN                   :
            case GroovyTokenTypes.POST_DEC                      :
            case GroovyTokenTypes.POST_INC                      :
            case GroovyTokenTypes.QUESTION                      :
            case GroovyTokenTypes.RANGE_EXCLUSIVE               :
            case GroovyTokenTypes.RANGE_INCLUSIVE               :
            case GroovyTokenTypes.RBRACK                        :
            case GroovyTokenTypes.RCURLY                        :
            case GroovyTokenTypes.REGEXP_CTOR_END               :
            case GroovyTokenTypes.REGEXP_SYMBOL                 :
            case GroovyTokenTypes.REGEX_FIND                    :
            case GroovyTokenTypes.REGEX_MATCH                   :
            case GroovyTokenTypes.RPAREN                        :
            case GroovyTokenTypes.SELECT_SLOT                   :
            case GroovyTokenTypes.SEMI                          :
            case GroovyTokenTypes.SH_COMMENT                    :
            case GroovyTokenTypes.SL                            :
            case GroovyTokenTypes.SLIST                         :
            case GroovyTokenTypes.SL_ASSIGN                     :
            case GroovyTokenTypes.SL_COMMENT                    :
            case GroovyTokenTypes.SPREAD_ARG                    :
            case GroovyTokenTypes.SPREAD_DOT                    :
            case GroovyTokenTypes.SPREAD_MAP_ARG                :
            case GroovyTokenTypes.SR                            :
            case GroovyTokenTypes.SR_ASSIGN                     :
            case GroovyTokenTypes.STAR                          :
            case GroovyTokenTypes.STAR_ASSIGN                   :
            case GroovyTokenTypes.STAR_STAR                     :
            case GroovyTokenTypes.STAR_STAR_ASSIGN              :
            case GroovyTokenTypes.STATIC_IMPORT                 :
            case GroovyTokenTypes.STATIC_INIT                   :
            case GroovyTokenTypes.STRICTFP                      :
            case GroovyTokenTypes.STRING_CH                     :
            case GroovyTokenTypes.STRING_CONSTRUCTOR            :
            case GroovyTokenTypes.STRING_CTOR_END               :
            case GroovyTokenTypes.STRING_CTOR_MIDDLE            :
            case GroovyTokenTypes.STRING_CTOR_START             :
            case GroovyTokenTypes.STRING_NL                     :
            case GroovyTokenTypes.SUPER_CTOR_CALL               :
            case GroovyTokenTypes.TRIPLE_DOT                    :
            case GroovyTokenTypes.TYPECAST                      :
            case GroovyTokenTypes.TYPE_ARGUMENT                 :
            case GroovyTokenTypes.TYPE_ARGUMENTS                :
            case GroovyTokenTypes.TYPE_LOWER_BOUNDS             :
            case GroovyTokenTypes.TYPE_PARAMETER                :
            case GroovyTokenTypes.TYPE_PARAMETERS               :
            case GroovyTokenTypes.TYPE_UPPER_BOUNDS             :
            case GroovyTokenTypes.UNARY_MINUS                   :
            case GroovyTokenTypes.UNARY_PLUS                    :
            case GroovyTokenTypes.UNUSED_CONST                  :
            case GroovyTokenTypes.UNUSED_DO                     :
            case GroovyTokenTypes.UNUSED_GOTO                   :
            case GroovyTokenTypes.VARIABLE_DEF                  :
            case GroovyTokenTypes.VARIABLE_PARAMETER_DEF        :
            case GroovyTokenTypes.VOCAB                         :
            case GroovyTokenTypes.WILDCARD_TYPE                 :
            case GroovyTokenTypes.WS                            :
                colour = black;
                break;

            case GroovyTokenTypes.STRING_LITERAL                :
            case GroovyTokenTypes.REGEXP_LITERAL                :
            case GroovyTokenTypes.DOLLAR_REGEXP_LITERAL         :
                colour = green;
                break;

            case GroovyTokenTypes.CLASS_DEF                     :
            case GroovyTokenTypes.EXTENDS_CLAUSE                :
            case GroovyTokenTypes.IMPLEMENTS_CLAUSE             :
            case GroovyTokenTypes.IMPORT                        :
            case GroovyTokenTypes.LITERAL_as                    :
            case GroovyTokenTypes.LITERAL_assert                :
            case GroovyTokenTypes.LITERAL_boolean               :
            case GroovyTokenTypes.LITERAL_break                 :
            case GroovyTokenTypes.LITERAL_byte                  :
            case GroovyTokenTypes.LITERAL_case                  :
            case GroovyTokenTypes.LITERAL_catch                 :
            case GroovyTokenTypes.LITERAL_char                  :
            case GroovyTokenTypes.LITERAL_class                 :
            case GroovyTokenTypes.LITERAL_continue              :
            case GroovyTokenTypes.LITERAL_def                   :
            case GroovyTokenTypes.LITERAL_default               :
            case GroovyTokenTypes.LITERAL_double                :
            case GroovyTokenTypes.LITERAL_else                  :
            case GroovyTokenTypes.LITERAL_enum                  :
            case GroovyTokenTypes.LITERAL_extends               :
            case GroovyTokenTypes.LITERAL_false                 :
            case GroovyTokenTypes.LITERAL_finally               :
            case GroovyTokenTypes.LITERAL_float                 :
            case GroovyTokenTypes.LITERAL_for                   :
            case GroovyTokenTypes.LITERAL_if                    :
            case GroovyTokenTypes.LITERAL_implements            :
            case GroovyTokenTypes.LITERAL_import                :
            case GroovyTokenTypes.LITERAL_in                    :
            case GroovyTokenTypes.LITERAL_instanceof            :
            case GroovyTokenTypes.LITERAL_int                   :
            case GroovyTokenTypes.LITERAL_interface             :
            case GroovyTokenTypes.LITERAL_long                  :
            case GroovyTokenTypes.LITERAL_native                :
            case GroovyTokenTypes.LITERAL_new                   :
            case GroovyTokenTypes.LITERAL_null                  :
            case GroovyTokenTypes.LITERAL_package               :
            case GroovyTokenTypes.LITERAL_private               :
            case GroovyTokenTypes.LITERAL_protected             :
            case GroovyTokenTypes.LITERAL_public                :
            case GroovyTokenTypes.LITERAL_return                :
            case GroovyTokenTypes.LITERAL_short                 :
            case GroovyTokenTypes.LITERAL_static                :
            case GroovyTokenTypes.LITERAL_super                 :
            case GroovyTokenTypes.LITERAL_switch                :
            case GroovyTokenTypes.LITERAL_synchronized          :
            case GroovyTokenTypes.LITERAL_this                  :
            case GroovyTokenTypes.LITERAL_threadsafe            :
            case GroovyTokenTypes.LITERAL_throw                 :
            case GroovyTokenTypes.LITERAL_throws                :
            case GroovyTokenTypes.LITERAL_transient             :
            case GroovyTokenTypes.LITERAL_true                  :
            case GroovyTokenTypes.LITERAL_try                   :
            case GroovyTokenTypes.LITERAL_void                  :
            case GroovyTokenTypes.LITERAL_volatile              :
            case GroovyTokenTypes.LITERAL_while                 :
            case GroovyTokenTypes.PACKAGE_DEF                   :
            case GroovyTokenTypes.TYPE                          :
                colour = blue;
                break;

            case GroovyTokenTypes.IDENT                         :
                colour = cyan;
                break;

            default:
                colour = black;
                break;
        }

        // leaf nodes that haven't been coloured yet
        if (black.equals(colour) && t.getNumberOfChildren() == 0) {
            colour = cyan;
        }



        return colour;
    }

    private String getName(GroovySourceAST t) {
        String name = tokenNames[t.getType()] + " <" + t.getType() + ">";
        if (!(escape(tokenNames[t.getType()]).equals(escape(t.getText())))) {
            name = name + " : " + t.getText();
        }
        switch (t.getType()) {
            case GroovyTokenTypes.METHOD_DEF :
            case GroovyTokenTypes.VARIABLE_DEF :
                GroovySourceAST identNode = t.childOfType(GroovyTokenTypes.IDENT);
                if (identNode != null) {
                    name = name + " : " + identNode.getText() + "";
                }
        }
        name = escape(name);
        if (sourceBuffer != null) {
            name += "&#xa;";
            name += t.getLine() + "," + t.getColumn() + " - " + t.getLineLast() + "," + t.getColumnLast();
            name += "&#xa;";
            name += escape(sourceBuffer.getSnippet(new LineColumn(t.getLine(), t.getColumn()), new LineColumn(t.getLineLast(), t.getColumnLast())));
        }
        return name;
    }

    private static String escape(String name) {
        if (name == null) return null;
        // remove middle of large bits of text
        if (name.length() > 200) {
            name = name.substring(0,100) + " ..... " + name.substring(name.length() - 100);
        }
        name = name.replace('"',' ');
        name = name.replace('\'',' ');
        name = name.replaceAll("&","&amp;");
        name = name.replaceAll("<","&lt;");
        name = name.replaceAll(">","&gt;");
        name = name.trim();
        return name;
    }
}
