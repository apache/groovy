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

/**
 * An interface for visiting a GroovySourceAST node.
 */
public interface Visitor {
    int OPENING_VISIT = 1;
    int SECOND_VISIT = 2; // only used on rare occasions, e.g. the '(' in this snippet...   @Foo  (  a=1, b=2, c=3)
    int SUBSEQUENT_VISIT = 3;
    int CLOSING_VISIT = 4;

    void setUp();
    void visitAbstract(GroovySourceAST t, int visit);
    void visitAnnotation(GroovySourceAST t, int visit);
    void visitAnnotations(GroovySourceAST t, int visit);
    void visitAnnotationArrayInit(GroovySourceAST t, int visit);
    void visitAnnotationDef(GroovySourceAST t, int visit);
    void visitAnnotationFieldDef(GroovySourceAST t, int visit);
    void visitAnnotationMemberValuePair(GroovySourceAST t, int visit);
    void visitArrayDeclarator(GroovySourceAST t, int visit);
    void visitAssign(GroovySourceAST t, int visit);
    void visitAt(GroovySourceAST t, int visit);
    void visitBand(GroovySourceAST t, int visit);
    void visitBandAssign(GroovySourceAST t, int visit);
    void visitBigSuffix(GroovySourceAST t, int visit);
    void visitBlock(GroovySourceAST t, int visit);
    void visitBnot(GroovySourceAST t, int visit);
    void visitBor(GroovySourceAST t, int visit);
    void visitBorAssign(GroovySourceAST t, int visit);
    void visitBsr(GroovySourceAST t, int visit);
    void visitBsrAssign(GroovySourceAST t, int visit);
    void visitBxor(GroovySourceAST t, int visit);
    void visitBxorAssign(GroovySourceAST t, int visit);
    void visitCaseGroup(GroovySourceAST t, int visit);
    void visitClassDef(GroovySourceAST t, int visit);
    void visitClosedBlock(GroovySourceAST t, int visit);
    void visitClosureList(GroovySourceAST t, int visit);
    void visitClosureOp(GroovySourceAST t, int visit);
    void visitColon(GroovySourceAST t, int visit);
    void visitComma(GroovySourceAST t, int visit);
    void visitCompareTo(GroovySourceAST t, int visit);
    void visitCtorCall(GroovySourceAST t, int visit);
    void visitCtorIdent(GroovySourceAST t, int visit);
    void visitDec(GroovySourceAST t, int visit);
    void visitDigit(GroovySourceAST t, int visit);
    void visitDiv(GroovySourceAST t, int visit);
    void visitDivAssign(GroovySourceAST t, int visit);
    void visitDollar(GroovySourceAST t, int visit);
    void visitDot(GroovySourceAST t, int visit);
    void visitDynamicMember(GroovySourceAST t, int visit);
    void visitElist(GroovySourceAST t, int visit);
    void visitEmptyStat(GroovySourceAST t, int visit);
    void visitEnumConstantDef(GroovySourceAST t, int visit);
    void visitEnumDef(GroovySourceAST t, int visit);
    void visitEof(GroovySourceAST t, int visit);
    void visitEqual(GroovySourceAST t, int visit);
    void visitEsc(GroovySourceAST t, int visit);
    void visitExponent(GroovySourceAST t, int visit);
    void visitExpr(GroovySourceAST t, int visit);
    void visitExtendsClause(GroovySourceAST t, int visit);
    void visitFinal(GroovySourceAST t, int visit);
    void visitFloatSuffix(GroovySourceAST t, int visit);
    void visitForCondition(GroovySourceAST t, int visit);
    void visitForEachClause(GroovySourceAST t, int visit);
    void visitForInit(GroovySourceAST t, int visit);
    void visitForInIterable(GroovySourceAST t, int visit);
    void visitForIterator(GroovySourceAST t, int visit);
    void visitGe(GroovySourceAST t, int visit);
    void visitGt(GroovySourceAST t, int visit);
    void visitHexDigit(GroovySourceAST t, int visit);
    void visitIdent(GroovySourceAST t, int visit);
    void visitImplementsClause(GroovySourceAST t, int visit);
    void visitImplicitParameters(GroovySourceAST t, int visit);
    void visitImport(GroovySourceAST t, int visit);
    void visitInc(GroovySourceAST t, int visit);
    void visitIndexOp(GroovySourceAST t, int visit);
    void visitInstanceInit(GroovySourceAST t, int visit);
    void visitInterfaceDef(GroovySourceAST t, int visit);
    void visitLabeledArg(GroovySourceAST t, int visit);
    void visitLabeledStat(GroovySourceAST t, int visit);
    void visitLand(GroovySourceAST t, int visit);
    void visitLbrack(GroovySourceAST t, int visit);
    void visitLcurly(GroovySourceAST t, int visit);
    void visitLe(GroovySourceAST t, int visit);
    void visitLetter(GroovySourceAST t, int visit);
    void visitListConstructor(GroovySourceAST t, int visit);
    void visitLiteralAs(GroovySourceAST t, int visit);
    void visitLiteralAssert(GroovySourceAST t, int visit);
    void visitLiteralBoolean(GroovySourceAST t, int visit);
    void visitLiteralBreak(GroovySourceAST t, int visit);
    void visitLiteralByte(GroovySourceAST t, int visit);
    void visitLiteralCase(GroovySourceAST t, int visit);
    void visitLiteralCatch(GroovySourceAST t, int visit);
    void visitLiteralChar(GroovySourceAST t, int visit);
    void visitLiteralClass(GroovySourceAST t, int visit);
    void visitLiteralContinue(GroovySourceAST t, int visit);
    void visitLiteralDef(GroovySourceAST t, int visit);
    void visitLiteralDefault(GroovySourceAST t, int visit);
    void visitLiteralDouble(GroovySourceAST t, int visit);
    void visitLiteralElse(GroovySourceAST t, int visit);
    void visitLiteralEnum(GroovySourceAST t, int visit);
    void visitLiteralExtends(GroovySourceAST t, int visit);
    void visitLiteralFalse(GroovySourceAST t, int visit);
    void visitLiteralFinally(GroovySourceAST t, int visit);
    void visitLiteralFloat(GroovySourceAST t, int visit);
    void visitLiteralFor(GroovySourceAST t, int visit);
    void visitLiteralIf(GroovySourceAST t, int visit);
    void visitLiteralImplements(GroovySourceAST t, int visit);
    void visitLiteralImport(GroovySourceAST t, int visit);
    void visitLiteralIn(GroovySourceAST t, int visit);
    void visitLiteralInstanceof(GroovySourceAST t, int visit);
    void visitLiteralInt(GroovySourceAST t, int visit);
    void visitLiteralInterface(GroovySourceAST t, int visit);
    void visitLiteralLong(GroovySourceAST t, int visit);
    void visitLiteralNative(GroovySourceAST t, int visit);
    void visitLiteralNew(GroovySourceAST t, int visit);
    void visitLiteralNull(GroovySourceAST t, int visit);
    void visitLiteralPackage(GroovySourceAST t, int visit);
    void visitLiteralPrivate(GroovySourceAST t, int visit);
    void visitLiteralProtected(GroovySourceAST t, int visit);
    void visitLiteralPublic(GroovySourceAST t, int visit);
    void visitLiteralReturn(GroovySourceAST t, int visit);
    void visitLiteralShort(GroovySourceAST t, int visit);
    void visitLiteralStatic(GroovySourceAST t, int visit);
    void visitLiteralSuper(GroovySourceAST t, int visit);
    void visitLiteralSwitch(GroovySourceAST t, int visit);
    void visitLiteralSynchronized(GroovySourceAST t, int visit);
    void visitLiteralThis(GroovySourceAST t, int visit);
    void visitLiteralThreadsafe(GroovySourceAST t, int visit);
    void visitLiteralThrow(GroovySourceAST t, int visit);
    void visitLiteralThrows(GroovySourceAST t, int visit);
    void visitLiteralTransient(GroovySourceAST t, int visit);
    void visitLiteralTrue(GroovySourceAST t, int visit);
    void visitLiteralTry(GroovySourceAST t, int visit);
    void visitLiteralVoid(GroovySourceAST t, int visit);
    void visitLiteralVolatile(GroovySourceAST t, int visit);
    void visitLiteralWhile(GroovySourceAST t, int visit);
    void visitLnot(GroovySourceAST t, int visit);
    void visitLor(GroovySourceAST t, int visit);
    void visitLparen(GroovySourceAST t, int visit);
    void visitLt(GroovySourceAST t, int visit);
    void visitMapConstructor(GroovySourceAST t, int visit);
    void visitMemberPointer(GroovySourceAST t, int visit);
    void visitMethodCall(GroovySourceAST t, int visit);
    void visitMethodDef(GroovySourceAST t, int visit);
    void visitMinus(GroovySourceAST t, int visit);
    void visitMinusAssign(GroovySourceAST t, int visit);
    void visitMlComment(GroovySourceAST t, int visit);
    void visitMod(GroovySourceAST t, int visit);
    void visitModifiers(GroovySourceAST t, int visit);
    void visitModAssign(GroovySourceAST t, int visit);
    void visitMultiCatch(GroovySourceAST t, int visit);
    void visitMultiCatchTypes(GroovySourceAST t, int visit);
    void visitNls(GroovySourceAST t, int visit);
    void visitNotEqual(GroovySourceAST t, int visit);
    void visitNullTreeLookahead(GroovySourceAST t, int visit);
    void visitNumBigDecimal(GroovySourceAST t, int visit);
    void visitNumBigInt(GroovySourceAST t, int visit);
    void visitNumDouble(GroovySourceAST t, int visit);
    void visitNumFloat(GroovySourceAST t, int visit);
    void visitNumInt(GroovySourceAST t, int visit);
    void visitNumLong(GroovySourceAST t, int visit);
    void visitObjblock(GroovySourceAST t, int visit);
    void visitOneNl(GroovySourceAST t, int visit);
    void visitOptionalDot(GroovySourceAST t, int visit);
    void visitPackageDef(GroovySourceAST t, int visit);
    void visitParameters(GroovySourceAST t, int visit);
    void visitParameterDef(GroovySourceAST t, int visit);
    void visitPlus(GroovySourceAST t, int visit);
    void visitPlusAssign(GroovySourceAST t, int visit);
    void visitPostDec(GroovySourceAST t, int visit);
    void visitPostInc(GroovySourceAST t, int visit);
    void visitQuestion(GroovySourceAST t, int visit);
    void visitRangeExclusive(GroovySourceAST t, int visit);
    void visitRangeInclusive(GroovySourceAST t, int visit);
    void visitRbrack(GroovySourceAST t, int visit);
    void visitRcurly(GroovySourceAST t, int visit);
    void visitRegexpCtorEnd(GroovySourceAST t, int visit);
    void visitRegexpLiteral(GroovySourceAST t, int visit);
    void visitRegexpSymbol(GroovySourceAST t, int visit);
    void visitRegexFind(GroovySourceAST t, int visit);
    void visitRegexMatch(GroovySourceAST t, int visit);
    void visitRparen(GroovySourceAST t, int visit);
    void visitSelectSlot(GroovySourceAST t, int visit);
    void visitSemi(GroovySourceAST t, int visit);
    void visitShComment(GroovySourceAST t, int visit);
    void visitSl(GroovySourceAST t, int visit);
    void visitSlist(GroovySourceAST t, int visit);
    void visitSlAssign(GroovySourceAST t, int visit);
    void visitSlComment(GroovySourceAST t, int visit);
    void visitSpreadArg(GroovySourceAST t, int visit);
    void visitSpreadDot(GroovySourceAST t, int visit);
    void visitSpreadMapArg(GroovySourceAST t, int visit);
    void visitSr(GroovySourceAST t, int visit);
    void visitSrAssign(GroovySourceAST t, int visit);
    void visitStar(GroovySourceAST t, int visit);
    void visitStarAssign(GroovySourceAST t, int visit);
    void visitStarStar(GroovySourceAST t, int visit);
    void visitStarStarAssign(GroovySourceAST t, int visit);
    void visitStaticImport(GroovySourceAST t, int visit);
    void visitStaticInit(GroovySourceAST t, int visit);
    void visitStrictfp(GroovySourceAST t, int visit);
    void visitStringCh(GroovySourceAST t, int visit);
    void visitStringConstructor(GroovySourceAST t, int visit);
    void visitStringCtorEnd(GroovySourceAST t, int visit);
    void visitStringCtorMiddle(GroovySourceAST t, int visit);
    void visitStringCtorStart(GroovySourceAST t, int visit);
    void visitStringLiteral(GroovySourceAST t, int visit);
    void visitStringNl(GroovySourceAST t, int visit);
    void visitSuperCtorCall(GroovySourceAST t, int visit);
    void visitTraitDef(GroovySourceAST t, int visit);
    void visitTripleDot(GroovySourceAST t, int visit);
    void visitType(GroovySourceAST t, int visit);
    void visitTypecast(GroovySourceAST t, int visit);
    void visitTypeArgument(GroovySourceAST t, int visit);
    void visitTypeArguments(GroovySourceAST t, int visit);
    void visitTypeLowerBounds(GroovySourceAST t, int visit);
    void visitTypeParameter(GroovySourceAST t, int visit);
    void visitTypeParameters(GroovySourceAST t, int visit);
    void visitTypeUpperBounds(GroovySourceAST t, int visit);
    void visitUnaryMinus(GroovySourceAST t, int visit);
    void visitUnaryPlus(GroovySourceAST t, int visit);
    void visitUnusedConst(GroovySourceAST t, int visit);
    void visitUnusedDo(GroovySourceAST t, int visit);
    void visitUnusedGoto(GroovySourceAST t, int visit);
    void visitVariableDef(GroovySourceAST t, int visit);
    void visitVariableParameterDef(GroovySourceAST t, int visit);
    void visitVocab(GroovySourceAST t, int visit);
    void visitWildcardType(GroovySourceAST t, int visit);
    void visitWs(GroovySourceAST t, int visit);

    void visitDefault(GroovySourceAST t,int visit);
    void tearDown();

    void push(GroovySourceAST t);
    GroovySourceAST pop();
}
