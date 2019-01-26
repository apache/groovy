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

import antlr.collections.AST;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class for Antlr AST traversal and visitation.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 */
public abstract class TraversalHelper implements AntlrASTProcessor {
    protected List<GroovySourceAST> unvisitedNodes;
    private final Visitor v;

    public TraversalHelper(Visitor visitor) {
        this.unvisitedNodes = new ArrayList<>();
        this.v = visitor;
    }

    protected void setUp(GroovySourceAST ast) {
        v.setUp();
    }

    protected void tearDown(GroovySourceAST ast) {
        v.tearDown();
    }

    protected void push(GroovySourceAST ast) {
        v.push(ast);
    }

    protected GroovySourceAST pop() {
        return v.pop();
    }

    protected void visitNode(GroovySourceAST ast, int n) {
        if (ast != null) {
            switch (ast.getType()) {
                case GroovyTokenTypes.ABSTRACT                      :   v.visitAbstract(ast,n);                     break;
                case GroovyTokenTypes.ANNOTATION                    :   v.visitAnnotation(ast,n);                   break;
                case GroovyTokenTypes.ANNOTATIONS                   :   v.visitAnnotations(ast,n);                  break;
                case GroovyTokenTypes.ANNOTATION_ARRAY_INIT         :   v.visitAnnotationArrayInit(ast,n);          break; // obsolete?
                case GroovyTokenTypes.ANNOTATION_DEF                :   v.visitAnnotationDef(ast,n);                break;
                case GroovyTokenTypes.ANNOTATION_FIELD_DEF          :   v.visitAnnotationFieldDef(ast,n);           break;
                case GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR  :   v.visitAnnotationMemberValuePair(ast,n);    break;
                case GroovyTokenTypes.ARRAY_DECLARATOR              :   v.visitArrayDeclarator(ast,n);              break;
                case GroovyTokenTypes.ASSIGN                        :   v.visitAssign(ast,n);                       break;
                case GroovyTokenTypes.AT                            :   v.visitAt(ast,n);                           break;
                case GroovyTokenTypes.BAND                          :   v.visitBand(ast,n);                         break;
                case GroovyTokenTypes.BAND_ASSIGN                   :   v.visitBandAssign(ast,n);                   break;
                case GroovyTokenTypes.BIG_SUFFIX                    :   v.visitBigSuffix(ast,n);                    break;
                case GroovyTokenTypes.BLOCK                         :   v.visitBlock(ast,n);                        break;
                case GroovyTokenTypes.BNOT                          :   v.visitBnot(ast,n);                         break;
                case GroovyTokenTypes.BOR                           :   v.visitBor(ast,n);                          break;
                case GroovyTokenTypes.BOR_ASSIGN                    :   v.visitBorAssign(ast,n);                    break;
                case GroovyTokenTypes.BSR                           :   v.visitBsr(ast,n);                          break;
                case GroovyTokenTypes.BSR_ASSIGN                    :   v.visitBsrAssign(ast,n);                    break;
                case GroovyTokenTypes.BXOR                          :   v.visitBxor(ast,n);                         break;
                case GroovyTokenTypes.BXOR_ASSIGN                   :   v.visitBxorAssign(ast,n);                   break;
                case GroovyTokenTypes.CASE_GROUP                    :   v.visitCaseGroup(ast,n);                    break;
                case GroovyTokenTypes.CLASS_DEF                     :   v.visitClassDef(ast,n);                     break;
                case GroovyTokenTypes.CLOSABLE_BLOCK                :   v.visitClosedBlock(ast,n);                  break;
                case GroovyTokenTypes.CLOSABLE_BLOCK_OP             :   v.visitClosureOp(ast,n);                    break;
                case GroovyTokenTypes.CLOSURE_LIST                  :   v.visitClosureList(ast,n);                  break;
                case GroovyTokenTypes.COLON                         :   v.visitColon(ast,n);                        break;
                case GroovyTokenTypes.COMMA                         :   v.visitComma(ast,n);                        break;
                case GroovyTokenTypes.COMPARE_TO                    :   v.visitCompareTo(ast,n);                    break;
                case GroovyTokenTypes.CTOR_CALL                     :   v.visitCtorCall(ast,n);                     break;
                case GroovyTokenTypes.CTOR_IDENT                    :   v.visitCtorIdent(ast,n);                    break;
                case GroovyTokenTypes.DEC                           :   v.visitDec(ast,n);                          break;
                case GroovyTokenTypes.DIGIT                         :   v.visitDigit(ast,n);                        break;
                case GroovyTokenTypes.DIV                           :   v.visitDiv(ast,n);                          break;
                case GroovyTokenTypes.DIV_ASSIGN                    :   v.visitDivAssign(ast,n);                    break;
                case GroovyTokenTypes.DOLLAR                        :   v.visitDollar(ast,n);                       break;
                case GroovyTokenTypes.DOLLAR_REGEXP_CTOR_END        :   v.visitRegexpCtorEnd(ast,n);                break;
                case GroovyTokenTypes.DOLLAR_REGEXP_LITERAL         :   v.visitRegexpLiteral(ast,n);                break;
                case GroovyTokenTypes.DOLLAR_REGEXP_SYMBOL          :   v.visitRegexpSymbol(ast,n);                 break;
                case GroovyTokenTypes.DOT                           :   v.visitDot(ast,n);                          break;
                case GroovyTokenTypes.DYNAMIC_MEMBER                :   v.visitDynamicMember(ast,n);                break;
                case GroovyTokenTypes.ELIST                         :   v.visitElist(ast,n);                        break;
                case GroovyTokenTypes.EMPTY_STAT                    :   v.visitEmptyStat(ast,n);                    break;
                case GroovyTokenTypes.ENUM_CONSTANT_DEF             :   v.visitEnumConstantDef(ast,n);              break;
                case GroovyTokenTypes.ENUM_DEF                      :   v.visitEnumDef(ast,n);                      break;
                case GroovyTokenTypes.EOF                           :   v.visitEof(ast,n);                          break;
                case GroovyTokenTypes.EQUAL                         :   v.visitEqual(ast,n);                        break;
                case GroovyTokenTypes.ESC                           :   v.visitEsc(ast,n);                          break;
                case GroovyTokenTypes.EXPONENT                      :   v.visitExponent(ast,n);                     break;
                case GroovyTokenTypes.EXPR                          :   v.visitExpr(ast,n);                         break;
                case GroovyTokenTypes.EXTENDS_CLAUSE                :   v.visitExtendsClause(ast,n);                break;
                case GroovyTokenTypes.FINAL                         :   v.visitFinal(ast,n);                        break;
                case GroovyTokenTypes.FLOAT_SUFFIX                  :   v.visitFloatSuffix(ast,n);                  break;
                case GroovyTokenTypes.FOR_CONDITION                 :   v.visitForCondition(ast,n);                 break;
                case GroovyTokenTypes.FOR_EACH_CLAUSE               :   v.visitForEachClause(ast,n);                break;
                case GroovyTokenTypes.FOR_INIT                      :   v.visitForInit(ast,n);                      break;
                case GroovyTokenTypes.FOR_IN_ITERABLE               :   v.visitForInIterable(ast,n);                break;
                case GroovyTokenTypes.FOR_ITERATOR                  :   v.visitForIterator(ast,n);                  break;
                case GroovyTokenTypes.GE                            :   v.visitGe(ast,n);                           break;
                case GroovyTokenTypes.GT                            :   v.visitGt(ast,n);                           break;
                case GroovyTokenTypes.HEX_DIGIT                     :   v.visitHexDigit(ast,n);                     break;
                case GroovyTokenTypes.IDENT                         :   v.visitIdent(ast,n);                        break;
                case GroovyTokenTypes.IMPLEMENTS_CLAUSE             :   v.visitImplementsClause(ast,n);             break;
                case GroovyTokenTypes.IMPLICIT_PARAMETERS           :   v.visitImplicitParameters(ast,n);           break;
                case GroovyTokenTypes.IMPORT                        :   v.visitImport(ast,n);                       break;
                case GroovyTokenTypes.INC                           :   v.visitInc(ast,n);                          break;
                case GroovyTokenTypes.INDEX_OP                      :   v.visitIndexOp(ast,n);                      break;
                case GroovyTokenTypes.INSTANCE_INIT                 :   v.visitInstanceInit(ast,n);                 break;
                case GroovyTokenTypes.INTERFACE_DEF                 :   v.visitInterfaceDef(ast,n);                 break;
                case GroovyTokenTypes.LABELED_ARG                   :   v.visitLabeledArg(ast,n);                   break;
                case GroovyTokenTypes.LABELED_STAT                  :   v.visitLabeledStat(ast,n);                  break;
                case GroovyTokenTypes.LAND                          :   v.visitLand(ast,n);                         break;
                case GroovyTokenTypes.LBRACK                        :   v.visitLbrack(ast,n);                       break;
                case GroovyTokenTypes.LCURLY                        :   v.visitLcurly(ast,n);                       break;
                case GroovyTokenTypes.LE                            :   v.visitLe(ast,n);                           break;
                case GroovyTokenTypes.LETTER                        :   v.visitLetter(ast,n);                       break;
                case GroovyTokenTypes.LIST_CONSTRUCTOR              :   v.visitListConstructor(ast,n);              break;
                case GroovyTokenTypes.LITERAL_as                    :   v.visitLiteralAs(ast,n);                    break;
                case GroovyTokenTypes.LITERAL_assert                :   v.visitLiteralAssert(ast,n);                break;
                case GroovyTokenTypes.LITERAL_boolean               :   v.visitLiteralBoolean(ast,n);               break;
                case GroovyTokenTypes.LITERAL_break                 :   v.visitLiteralBreak(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_byte                  :   v.visitLiteralByte(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_case                  :   v.visitLiteralCase(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_catch                 :   v.visitLiteralCatch(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_char                  :   v.visitLiteralChar(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_class                 :   v.visitLiteralClass(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_continue              :   v.visitLiteralContinue(ast,n);              break;
                case GroovyTokenTypes.LITERAL_def                   :   v.visitLiteralDef(ast,n);                   break;
                case GroovyTokenTypes.LITERAL_default               :   v.visitLiteralDefault(ast,n);               break;
                case GroovyTokenTypes.LITERAL_double                :   v.visitLiteralDouble(ast,n);                break;
                case GroovyTokenTypes.LITERAL_else                  :   v.visitLiteralElse(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_enum                  :   v.visitLiteralEnum(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_extends               :   v.visitLiteralExtends(ast,n);               break;
                case GroovyTokenTypes.LITERAL_false                 :   v.visitLiteralFalse(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_finally               :   v.visitLiteralFinally(ast,n);               break;
                case GroovyTokenTypes.LITERAL_float                 :   v.visitLiteralFloat(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_for                   :   v.visitLiteralFor(ast,n);                   break;
                case GroovyTokenTypes.LITERAL_if                    :   v.visitLiteralIf(ast,n);                    break;
                case GroovyTokenTypes.LITERAL_implements            :   v.visitLiteralImplements(ast,n);            break;
                case GroovyTokenTypes.LITERAL_import                :   v.visitLiteralImport(ast,n);                break;
                case GroovyTokenTypes.LITERAL_in                    :   v.visitLiteralIn(ast,n);                    break;
                case GroovyTokenTypes.LITERAL_instanceof            :   v.visitLiteralInstanceof(ast,n);            break;
                case GroovyTokenTypes.LITERAL_int                   :   v.visitLiteralInt(ast,n);                   break;
                case GroovyTokenTypes.LITERAL_interface             :   v.visitLiteralInterface(ast,n);             break;
                case GroovyTokenTypes.LITERAL_long                  :   v.visitLiteralLong(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_native                :   v.visitLiteralNative(ast,n);                break;
                case GroovyTokenTypes.LITERAL_new                   :   v.visitLiteralNew(ast,n);                   break;
                case GroovyTokenTypes.LITERAL_null                  :   v.visitLiteralNull(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_package               :   v.visitLiteralPackage(ast,n);               break;
                case GroovyTokenTypes.LITERAL_private               :   v.visitLiteralPrivate(ast,n);               break;
                case GroovyTokenTypes.LITERAL_protected             :   v.visitLiteralProtected(ast,n);             break;
                case GroovyTokenTypes.LITERAL_public                :   v.visitLiteralPublic(ast,n);                break;
                case GroovyTokenTypes.LITERAL_return                :   v.visitLiteralReturn(ast,n);                break;
                case GroovyTokenTypes.LITERAL_short                 :   v.visitLiteralShort(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_static                :   v.visitLiteralStatic(ast,n);                break;
                case GroovyTokenTypes.LITERAL_super                 :   v.visitLiteralSuper(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_switch                :   v.visitLiteralSwitch(ast,n);                break;
                case GroovyTokenTypes.LITERAL_synchronized          :   v.visitLiteralSynchronized(ast,n);          break;
                case GroovyTokenTypes.LITERAL_this                  :   v.visitLiteralThis(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_threadsafe            :   v.visitLiteralThreadsafe(ast,n);            break;
                case GroovyTokenTypes.LITERAL_throw                 :   v.visitLiteralThrow(ast,n);                 break;
                case GroovyTokenTypes.LITERAL_throws                :   v.visitLiteralThrows(ast,n);                break;
                case GroovyTokenTypes.LITERAL_transient             :   v.visitLiteralTransient(ast,n);             break;
                case GroovyTokenTypes.LITERAL_true                  :   v.visitLiteralTrue(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_try                   :   v.visitLiteralTry(ast,n);                   break;
                case GroovyTokenTypes.LITERAL_void                  :   v.visitLiteralVoid(ast,n);                  break;
                case GroovyTokenTypes.LITERAL_volatile              :   v.visitLiteralVolatile(ast,n);              break;
                case GroovyTokenTypes.LITERAL_while                 :   v.visitLiteralWhile(ast,n);                 break;
                case GroovyTokenTypes.LNOT                          :   v.visitLnot(ast,n);                         break;
                case GroovyTokenTypes.LOR                           :   v.visitLor(ast,n);                          break;
                case GroovyTokenTypes.LPAREN                        :   v.visitLparen(ast,n);                       break;
                case GroovyTokenTypes.LT                            :   v.visitLt(ast,n);                           break;
                case GroovyTokenTypes.MAP_CONSTRUCTOR               :   v.visitMapConstructor(ast,n);               break;
                case GroovyTokenTypes.MEMBER_POINTER                :   v.visitMemberPointer(ast,n);                break;
                case GroovyTokenTypes.METHOD_CALL                   :   v.visitMethodCall(ast,n);                   break;
                case GroovyTokenTypes.METHOD_DEF                    :   v.visitMethodDef(ast,n);                    break;
                case GroovyTokenTypes.MINUS                         :   v.visitMinus(ast,n);                        break;
                case GroovyTokenTypes.MINUS_ASSIGN                  :   v.visitMinusAssign(ast,n);                  break;
                case GroovyTokenTypes.ML_COMMENT                    :   v.visitMlComment(ast,n);                    break;
                case GroovyTokenTypes.MOD                           :   v.visitMod(ast,n);                          break;
                case GroovyTokenTypes.MODIFIERS                     :   v.visitModifiers(ast,n);                    break;
                case GroovyTokenTypes.MOD_ASSIGN                    :   v.visitModAssign(ast,n);                    break;
                case GroovyTokenTypes.NLS                           :   v.visitNls(ast,n);                          break;
                case GroovyTokenTypes.NOT_EQUAL                     :   v.visitNotEqual(ast,n);                     break;
                case GroovyTokenTypes.NULL_TREE_LOOKAHEAD           :   v.visitNullTreeLookahead(ast,n);            break;
                case GroovyTokenTypes.MULTICATCH                    :   v.visitMultiCatch(ast,n);                   break;
                case GroovyTokenTypes.MULTICATCH_TYPES              :   v.visitMultiCatchTypes(ast,n);              break;
                case GroovyTokenTypes.NUM_BIG_DECIMAL               :   v.visitNumBigDecimal(ast,n);                break;
                case GroovyTokenTypes.NUM_BIG_INT                   :   v.visitNumBigInt(ast,n);                    break;
                case GroovyTokenTypes.NUM_DOUBLE                    :   v.visitNumDouble(ast,n);                    break;
                case GroovyTokenTypes.NUM_FLOAT                     :   v.visitNumFloat(ast,n);                     break;
                case GroovyTokenTypes.NUM_INT                       :   v.visitNumInt(ast,n);                       break;
                case GroovyTokenTypes.NUM_LONG                      :   v.visitNumLong(ast,n);                      break;
                case GroovyTokenTypes.OBJBLOCK                      :   v.visitObjblock(ast,n);                     break;
                case GroovyTokenTypes.ONE_NL                        :   v.visitOneNl(ast,n);                        break;
                case GroovyTokenTypes.OPTIONAL_DOT                  :   v.visitOptionalDot(ast,n);                  break;
                case GroovyTokenTypes.PACKAGE_DEF                   :   v.visitPackageDef(ast,n);                   break;
                case GroovyTokenTypes.PARAMETERS                    :   v.visitParameters(ast,n);                   break;
                case GroovyTokenTypes.PARAMETER_DEF                 :   v.visitParameterDef(ast,n);                 break;
                case GroovyTokenTypes.PLUS                          :   v.visitPlus(ast,n);                         break;
                case GroovyTokenTypes.PLUS_ASSIGN                   :   v.visitPlusAssign(ast,n);                   break;
                case GroovyTokenTypes.POST_DEC                      :   v.visitPostDec(ast,n);                      break;
                case GroovyTokenTypes.POST_INC                      :   v.visitPostInc(ast,n);                      break;
                case GroovyTokenTypes.QUESTION                      :   v.visitQuestion(ast,n);                     break;
                case GroovyTokenTypes.RANGE_EXCLUSIVE               :   v.visitRangeExclusive(ast,n);               break;
                case GroovyTokenTypes.RANGE_INCLUSIVE               :   v.visitRangeInclusive(ast,n);               break;
                case GroovyTokenTypes.RBRACK                        :   v.visitRbrack(ast,n);                       break;
                case GroovyTokenTypes.RCURLY                        :   v.visitRcurly(ast,n);                       break;
                case GroovyTokenTypes.REGEXP_CTOR_END               :   v.visitRegexpCtorEnd(ast,n);                break;
                case GroovyTokenTypes.REGEXP_LITERAL                :   v.visitRegexpLiteral(ast,n);                break;
                case GroovyTokenTypes.REGEXP_SYMBOL                 :   v.visitRegexpSymbol(ast,n);                 break;
                case GroovyTokenTypes.REGEX_FIND                    :   v.visitRegexFind(ast,n);                    break;
                case GroovyTokenTypes.REGEX_MATCH                   :   v.visitRegexMatch(ast,n);                   break;
                case GroovyTokenTypes.RPAREN                        :   v.visitRparen(ast,n);                       break;
                case GroovyTokenTypes.SELECT_SLOT                   :   v.visitSelectSlot(ast,n);                   break;
                case GroovyTokenTypes.SEMI                          :   v.visitSemi(ast,n);                         break;
                case GroovyTokenTypes.SH_COMMENT                    :   v.visitShComment(ast,n);                    break;
                case GroovyTokenTypes.SL                            :   v.visitSl(ast,n);                           break;
                case GroovyTokenTypes.SLIST                         :   v.visitSlist(ast,n);                        break;
                case GroovyTokenTypes.SL_ASSIGN                     :   v.visitSlAssign(ast,n);                     break;
                case GroovyTokenTypes.SL_COMMENT                    :   v.visitSlComment(ast,n);                    break;
                case GroovyTokenTypes.SPREAD_ARG                    :   v.visitSpreadArg(ast,n);                    break;
                case GroovyTokenTypes.SPREAD_DOT                    :   v.visitSpreadDot(ast,n);                    break;
                case GroovyTokenTypes.SPREAD_MAP_ARG                :   v.visitSpreadMapArg(ast,n);                 break;
                case GroovyTokenTypes.SR                            :   v.visitSr(ast,n);                           break;
                case GroovyTokenTypes.SR_ASSIGN                     :   v.visitSrAssign(ast,n);                     break;
                case GroovyTokenTypes.STAR                          :   v.visitStar(ast,n);                         break;
                case GroovyTokenTypes.STAR_ASSIGN                   :   v.visitStarAssign(ast,n);                   break;
                case GroovyTokenTypes.STAR_STAR                     :   v.visitStarStar(ast,n);                     break;
                case GroovyTokenTypes.STAR_STAR_ASSIGN              :   v.visitStarStarAssign(ast,n);               break;
                case GroovyTokenTypes.STATIC_IMPORT                 :   v.visitStaticImport(ast,n);                 break;
                case GroovyTokenTypes.STATIC_INIT                   :   v.visitStaticInit(ast,n);                   break;
                case GroovyTokenTypes.STRICTFP                      :   v.visitStrictfp(ast,n);                     break;
                case GroovyTokenTypes.STRING_CH                     :   v.visitStringCh(ast,n);                     break;
                case GroovyTokenTypes.STRING_CONSTRUCTOR            :   v.visitStringConstructor(ast,n);            break;
                case GroovyTokenTypes.STRING_CTOR_END               :   v.visitStringCtorEnd(ast,n);                break;
                case GroovyTokenTypes.STRING_CTOR_MIDDLE            :   v.visitStringCtorMiddle(ast,n);             break;
                case GroovyTokenTypes.STRING_CTOR_START             :   v.visitStringCtorStart(ast,n);              break;
                case GroovyTokenTypes.STRING_LITERAL                :   v.visitStringLiteral(ast,n);                break;
                case GroovyTokenTypes.STRING_NL                     :   v.visitStringNl(ast,n);                     break;
                case GroovyTokenTypes.SUPER_CTOR_CALL               :   v.visitSuperCtorCall(ast,n);                break;
                case GroovyTokenTypes.TRAIT_DEF                     :   v.visitTraitDef(ast,n);                     break;
                case GroovyTokenTypes.TRIPLE_DOT                    :   v.visitTripleDot(ast,n);                    break;
                case GroovyTokenTypes.TYPE                          :   v.visitType(ast,n);                         break;
                case GroovyTokenTypes.TYPECAST                      :   v.visitTypecast(ast,n);                     break;
                case GroovyTokenTypes.TYPE_ARGUMENT                 :   v.visitTypeArgument(ast,n);                 break;
                case GroovyTokenTypes.TYPE_ARGUMENTS                :   v.visitTypeArguments(ast,n);                break;
                case GroovyTokenTypes.TYPE_LOWER_BOUNDS             :   v.visitTypeLowerBounds(ast,n);              break;
                case GroovyTokenTypes.TYPE_PARAMETER                :   v.visitTypeParameter(ast,n);                break;
                case GroovyTokenTypes.TYPE_PARAMETERS               :   v.visitTypeParameters(ast,n);               break;
                case GroovyTokenTypes.TYPE_UPPER_BOUNDS             :   v.visitTypeUpperBounds(ast,n);              break;
                case GroovyTokenTypes.UNARY_MINUS                   :   v.visitUnaryMinus(ast,n);                   break;
                case GroovyTokenTypes.UNARY_PLUS                    :   v.visitUnaryPlus(ast,n);                    break;
                case GroovyTokenTypes.UNUSED_CONST                  :   v.visitUnusedConst(ast,n);                  break;
                case GroovyTokenTypes.UNUSED_DO                     :   v.visitUnusedDo(ast,n);                     break;
                case GroovyTokenTypes.UNUSED_GOTO                   :   v.visitUnusedGoto(ast,n);                   break;
                case GroovyTokenTypes.VARIABLE_DEF                  :   v.visitVariableDef(ast,n);                  break;
                case GroovyTokenTypes.VARIABLE_PARAMETER_DEF        :   v.visitVariableParameterDef(ast,n);         break;
                case GroovyTokenTypes.VOCAB                         :   v.visitVocab(ast,n);                        break;
                case GroovyTokenTypes.WILDCARD_TYPE                 :   v.visitWildcardType(ast,n);                 break;
                case GroovyTokenTypes.WS                            :   v.visitWs(ast,n);                           break;


                default                                             :   v.visitDefault(ast,n);                      break;
            }
        } else {
            // the supplied AST was null
            v.visitDefault(null,n);
        }
    }

    protected abstract void accept(GroovySourceAST currentNode);

    protected void accept_v_FirstChildsFirstChild_v_Child2_Child3_v_Child4_v___v_LastChild(GroovySourceAST t) {
        openingVisit(t);
        GroovySourceAST expr2 = t.childAt(0);
        skip(expr2);
        accept(expr2.childAt(0));
        closingVisit(t);

        GroovySourceAST sibling = (GroovySourceAST) expr2.getNextSibling();
        boolean firstSList = true;
        while (sibling != null) {
            if (!firstSList) {
                subsequentVisit(t);
            }
            firstSList = false;
            accept(sibling);
            sibling = (GroovySourceAST) sibling.getNextSibling();
        }
    }

    protected void accept_v_FirstChildsFirstChild_v_RestOfTheChildren(GroovySourceAST t) {
        openingVisit(t);
        GroovySourceAST expr = t.childAt(0);
        skip(expr);
        accept(expr.childAt(0));
        closingVisit(t);
        acceptSiblings(expr);
    }

    protected void accept_FirstChild_v_SecondChild(GroovySourceAST t) {
        accept(t.childAt(0));
        subsequentVisit(t);
        accept(t.childAt(1));
    }

    protected void accept_FirstChild_v_SecondChild_v(GroovySourceAST t) {
        accept(t.childAt(0));
        openingVisit(t);
        accept(t.childAt(1));
        closingVisit(t);
    }

    protected void accept_SecondChild_v_ThirdChild_v(GroovySourceAST t) {
        accept(t.childAt(1));
        openingVisit(t);
        accept(t.childAt(2));
        closingVisit(t);
    }

    protected void accept_FirstChild_v_SecondChildsChildren_v(GroovySourceAST t) {
        accept(t.childAt(0));

        openingVisit(t);
        GroovySourceAST secondChild = t.childAt(1);
        if (secondChild != null) {
            acceptChildren(secondChild);
        }
        closingVisit(t);
    }


    protected void accept_v_FirstChild_SecondChild_v_ThirdChild_v(GroovySourceAST t) {
        openingVisit(t);
        accept(t.childAt(0));
        accept(t.childAt(1));
        subsequentVisit(t);
        accept(t.childAt(2));
        closingVisit(t);
    }

    protected void accept_FirstChild_v_SecondChild_v_ThirdChild_v(GroovySourceAST t) {
        accept(t.childAt(0));
        openingVisit(t);
        accept(t.childAt(1));
        subsequentVisit(t);
        accept(t.childAt(2));
        closingVisit(t);
    }

    protected void accept_FirstSecondAndThirdChild_v_v_ForthChild(GroovySourceAST t) {
        GroovySourceAST child1 = (GroovySourceAST) t.getFirstChild();
        if (child1 != null) {
            accept(child1);
            GroovySourceAST child2 = (GroovySourceAST) child1.getNextSibling();
            if (child2 != null) {
                accept(child2);
                GroovySourceAST child3 = (GroovySourceAST) child2.getNextSibling();
                if (child3 != null) {
                    accept(child3);
                    openingVisit(t);
                    GroovySourceAST child4 = (GroovySourceAST) child3.getNextSibling();
                    if (child4 != null) {
                        subsequentVisit(t);
                        accept(child4);
                    }
                }
            }
        }
    }

    protected void accept_v_FirstChild_2ndv_SecondChild_v___LastChild_v(GroovySourceAST t) {
        openingVisit(t);
        GroovySourceAST child = (GroovySourceAST) t.getFirstChild();
        if (child != null) {
            accept(child);
            GroovySourceAST sibling = (GroovySourceAST) child.getNextSibling();
            if (sibling != null) {
                secondVisit(t);
                accept(sibling);
                sibling = (GroovySourceAST) sibling.getNextSibling();
                while (sibling != null) {
                    subsequentVisit(t);
                    accept(sibling);
                    sibling = (GroovySourceAST) sibling.getNextSibling();
                }
            }
        }
        closingVisit(t);
    }

    protected void accept_v_FirstChild_v_SecondChild_v___LastChild_v(GroovySourceAST t) {
        openingVisit(t);
        GroovySourceAST child = (GroovySourceAST) t.getFirstChild();
        if (child != null) {
            accept(child);
            GroovySourceAST sibling = (GroovySourceAST) child.getNextSibling();
            while (sibling != null) {
                subsequentVisit(t);
                accept(sibling);
                sibling = (GroovySourceAST) sibling.getNextSibling();
            }
        }
        closingVisit(t);
    }

    protected void accept_v_FirstChild_v(GroovySourceAST t) {
        openingVisit(t);
        accept(t.childAt(0));
        closingVisit(t);
    }

    protected void accept_v_Siblings_v(GroovySourceAST t) {
        openingVisit(t);
        acceptSiblings(t);
        closingVisit(t);
    }

    protected void accept_v_AllChildren_v_Siblings(GroovySourceAST t) {
        openingVisit(t);
        acceptChildren(t);
        closingVisit(t);
        acceptSiblings(t);
    }

    protected void accept_v_AllChildren_v(GroovySourceAST t) {
        openingVisit(t);
        acceptChildren(t);
        closingVisit(t);
    }

    protected void accept_FirstChild_v_RestOfTheChildren(GroovySourceAST t) {
        accept(t.childAt(0));
        openingVisit(t);
        closingVisit(t);
        acceptSiblings(t.childAt(0));
    }

    protected void accept_FirstChild_v_RestOfTheChildren_v_LastChild(GroovySourceAST t) {
        int count = 0;
        accept(t.childAt(0));
        count++;
        openingVisit(t);
        if (t.childAt(0) != null) {
            GroovySourceAST sibling = (GroovySourceAST) t.childAt(0).getNextSibling();
            while (sibling != null) {
                if (count == t.getNumberOfChildren() - 1) {
                    closingVisit(t);
                }
                accept(sibling);
                count++;
                sibling = (GroovySourceAST) sibling.getNextSibling();
            }
        }


    }

    protected void accept_FirstChild_v_RestOfTheChildren_v(GroovySourceAST t) {
        accept(t.childAt(0));
        openingVisit(t);
        acceptSiblings(t.childAt(0));
        closingVisit(t);
    }

    protected void accept_v_FirstChild_v_RestOfTheChildren(GroovySourceAST t) {
        accept_v_FirstChild_v(t);
        acceptSiblings(t.childAt(0));
    }

    protected void accept_v_FirstChild_v_RestOfTheChildren_v(GroovySourceAST t) {
        openingVisit(t);
        accept(t.childAt(0));
        subsequentVisit(t);
        acceptSiblings(t.childAt(0));
        closingVisit(t);
    }

    protected void acceptSiblings(GroovySourceAST t) {
        if (t != null) {
            GroovySourceAST sibling = (GroovySourceAST) t.getNextSibling();
            while (sibling != null) {
                accept(sibling);
                sibling = (GroovySourceAST) sibling.getNextSibling();
            }
        }
    }

    protected void acceptChildren(GroovySourceAST t) {
        if (t != null) {
            GroovySourceAST child = (GroovySourceAST) t.getFirstChild();
            if (child != null) {
                accept(child);
                acceptSiblings(child);
            }
        }
    }

    protected void skip(GroovySourceAST expr) {
        unvisitedNodes.remove(expr);
    }

    protected void openingVisit(GroovySourceAST t) {
        unvisitedNodes.remove(t);

        int n = Visitor.OPENING_VISIT;
        visitNode(t, n);
    }

    protected void secondVisit(GroovySourceAST t) {
        int n = Visitor.SECOND_VISIT;
        visitNode(t, n);
    }

    protected void subsequentVisit(GroovySourceAST t) {
        int n = Visitor.SUBSEQUENT_VISIT;
        visitNode(t, n);
    }

    protected void closingVisit(GroovySourceAST t) {
        int n = Visitor.CLOSING_VISIT;
        visitNode(t, n);
    }

    public AST process(AST t) {
        GroovySourceAST node = (GroovySourceAST) t;

        // process each node in turn
        setUp(node);
        accept(node);
        acceptSiblings(node);
        tearDown(node);
        return null;
    }
}
