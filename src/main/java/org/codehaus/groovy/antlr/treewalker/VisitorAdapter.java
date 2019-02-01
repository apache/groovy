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
 * A default implementation of all visitor methods.
 * If you extend this class, any un-overridden visit methods will
 * call visitDefault.
 */
public class VisitorAdapter implements Visitor {
    public void setUp() {}
    public void visitAbstract(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotation(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotations(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotationArrayInit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotationDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotationFieldDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAnnotationMemberValuePair(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitArrayDeclarator(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitAt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBand(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBandAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBigSuffix(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBlock(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBnot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBorAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBsr(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBsrAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBxor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitBxorAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitCaseGroup(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitClassDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitClosedBlock(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitClosureOp(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitClosureList(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitColon(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitComma(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitCompareTo(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitCtorCall(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitCtorIdent(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDec(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDigit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDiv(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDivAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDollar(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitDynamicMember(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitElist(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEmptyStat(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEnumConstantDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEnumDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEof(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEqual(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitEsc(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitExponent(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitExpr(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitExtendsClause(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitFinal(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitFloatSuffix(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitForCondition(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitForEachClause(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitForInit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitForInIterable(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitForIterator(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitGe(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitGt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitHexDigit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitIdent(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitImplementsClause(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitImplicitParameters(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitImport(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitInc(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitIndexOp(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitInstanceInit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitInterfaceDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLabeledArg(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLabeledStat(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLand(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLbrack(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLcurly(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLe(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLetter(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitListConstructor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralAs(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralAssert(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralBoolean(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralBreak(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralByte(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralCase(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralCatch(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralChar(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralClass(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralContinue(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralDefault(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralDouble(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralElse(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralEnum(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralExtends(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralFalse(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralFinally(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralFloat(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralFor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralIf(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralImplements(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralImport(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralIn(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralInstanceof(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralInt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralInterface(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralLong(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralNative(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralNew(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralNull(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralPackage(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralPrivate(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralProtected(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralPublic(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralReturn(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralShort(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralStatic(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralSuper(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralSwitch(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralSynchronized(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralThis(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralThreadsafe(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralThrow(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralThrows(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralTransient(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralTrue(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralTry(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralVoid(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralVolatile(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLiteralWhile(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLnot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLparen(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitLt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMapConstructor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMemberPointer(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMethodCall(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMethodDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMinus(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMinusAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMlComment(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMod(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitModifiers(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitModAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMultiCatch(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitMultiCatchTypes(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNls(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNotEqual(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNullTreeLookahead(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumBigDecimal(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumBigInt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumDouble(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumFloat(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumInt(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitNumLong(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitObjblock(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitOneNl(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitOptionalDot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitPackageDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitParameters(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitParameterDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitPlus(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitPlusAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitPostDec(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitPostInc(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitQuestion(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRangeExclusive(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRangeInclusive(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRbrack(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRcurly(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRegexpCtorEnd(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRegexpLiteral(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRegexpSymbol(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRegexFind(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRegexMatch(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitRparen(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSelectSlot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSemi(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitShComment(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSl(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSlist(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSlAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSlComment(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSpreadArg(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSpreadDot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSpreadMapArg(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSr(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSrAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStar(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStarAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStarStar(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStarStarAssign(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStaticImport(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStaticInit(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStrictfp(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringCh(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringConstructor(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringCtorEnd(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringCtorMiddle(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringCtorStart(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringLiteral(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitStringNl(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitSuperCtorCall(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTraitDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTripleDot(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitType(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypecast(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeArgument(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeArguments(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeLowerBounds(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeParameter(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeParameters(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitTypeUpperBounds(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitUnaryMinus(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitUnaryPlus(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitUnusedConst(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitUnusedDo(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitUnusedGoto(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitVariableDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitVariableParameterDef(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitVocab(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitWildcardType(GroovySourceAST t,int visit) {visitDefault(t,visit);}
    public void visitWs(GroovySourceAST t,int visit) {visitDefault(t,visit);}

    public void visitDefault(GroovySourceAST t,int visit) {}
    public void tearDown() {}

    public void push(GroovySourceAST t) {}
    public GroovySourceAST pop() {return null;}
}
