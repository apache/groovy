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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A composite of many visitors. Any call to a method from Visitor
 * will invoke each visitor in turn, and reverse the invocation
 * order on a closing visit.
 * i.e.
 * with the list of visitors = [a,b,c]
 * composite.visitDefault() would...
 * call on the opening visit - a.visitDefault() then b.visitDefault() then c.visitDefault()
 * call on the closing visit - c.visitDefault() then b.visitDefault() then a.visitDefault()
 */

public class CompositeVisitor implements Visitor{
    final List visitors;
    final List backToFrontVisitors;

    /**
     * A composite of the supplied list of antlr AST visitors.
     * @param visitors a List of implementations of the Visitor interface
     */
    public CompositeVisitor(List visitors) {
        this.visitors = visitors;
        backToFrontVisitors = new ArrayList();
        backToFrontVisitors.addAll(visitors);
        Collections.reverse(backToFrontVisitors);
    }

    private Iterator itr(int visit) {
        Iterator itr=visitors.iterator();
        if (visit == CLOSING_VISIT) {
            itr = backToFrontVisitors.iterator();
        }
        return itr;
    }

    public void setUp() {
        for (Object visitor : visitors) {
            ((Visitor) visitor).setUp();
        }
    }

    public void visitAbstract(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAbstract(t,visit);}
    }

    public void visitAnnotation(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotation(t,visit);}
    }

    public void visitAnnotations(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotations(t,visit);}
    }

    public void visitAnnotationArrayInit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotationArrayInit(t,visit);}
    }

    public void visitAnnotationDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotationDef(t,visit);}
    }

    public void visitAnnotationFieldDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotationFieldDef(t,visit);}
    }

    public void visitAnnotationMemberValuePair(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAnnotationMemberValuePair(t,visit);}
    }

    public void visitArrayDeclarator(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitArrayDeclarator(t,visit);}
    }

    public void visitAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAssign(t,visit);}
    }

    public void visitAt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitAt(t,visit);}
    }

    public void visitBand(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBand(t,visit);}
    }

    public void visitBandAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBandAssign(t,visit);}
    }

    public void visitBigSuffix(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBigSuffix(t,visit);}
    }

    public void visitBlock(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBlock(t,visit);}
    }

    public void visitBnot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBnot(t,visit);}
    }

    public void visitBor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBor(t,visit);}
    }

    public void visitBorAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBorAssign(t,visit);}
    }

    public void visitBsr(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBsr(t,visit);}
    }

    public void visitBsrAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBsrAssign(t,visit);}
    }

    public void visitBxor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBxor(t,visit);}
    }

    public void visitBxorAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitBxorAssign(t,visit);}
    }

    public void visitCaseGroup(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitCaseGroup(t,visit);}
    }

    public void visitClassDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitClassDef(t,visit);}
    }

    public void visitClosedBlock(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitClosedBlock(t,visit);}
    }

    public void visitClosureList(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitClosureList(t,visit);}
    }

    public void visitClosureOp(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitClosureOp(t,visit);}
    }

    public void visitColon(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitColon(t,visit);}
    }

    public void visitComma(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitComma(t,visit);}
    }

    public void visitCompareTo(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitCompareTo(t,visit);}
    }

    public void visitCtorCall(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitCtorCall(t,visit);}
    }

    public void visitCtorIdent(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitCtorIdent(t,visit);}
    }

    public void visitDec(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDec(t,visit);}
    }

    public void visitDigit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDigit(t,visit);}
    }

    public void visitDiv(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDiv(t,visit);}
    }

    public void visitDivAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDivAssign(t,visit);}
    }

    public void visitDollar(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDollar(t,visit);}
    }

    public void visitDot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDot(t,visit);}
    }

    public void visitDynamicMember(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDynamicMember(t,visit);}
    }

    public void visitElist(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitElist(t,visit);}
    }

    public void visitEmptyStat(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEmptyStat(t,visit);}
    }

    public void visitEnumConstantDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEnumConstantDef(t,visit);}
    }

    public void visitEnumDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEnumDef(t,visit);}
    }

    public void visitEof(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEof(t,visit);}
    }

    public void visitEqual(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEqual(t,visit);}
    }

    public void visitEsc(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitEsc(t,visit);}
    }

    public void visitExponent(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitExponent(t,visit);}
    }

    public void visitExpr(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitExpr(t,visit);}
    }

    public void visitExtendsClause(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitExtendsClause(t,visit);}
    }

    public void visitFinal(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitFinal(t,visit);}
    }

    public void visitFloatSuffix(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitFloatSuffix(t,visit);}
    }

    public void visitForCondition(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitForCondition(t,visit);}
    }

    public void visitForEachClause(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitForEachClause(t,visit);}
    }

    public void visitForInit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitForInit(t,visit);}
    }

    public void visitForInIterable(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitForInIterable(t,visit);}
    }

    public void visitForIterator(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitForIterator(t,visit);}
    }

    public void visitGe(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitGe(t,visit);}
    }

    public void visitGt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitGt(t,visit);}
    }

    public void visitHexDigit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitHexDigit(t,visit);}
    }

    public void visitIdent(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitIdent(t,visit);}
    }

    public void visitImplementsClause(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitImplementsClause(t,visit);}
    }

    public void visitImplicitParameters(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitImplicitParameters(t,visit);}
    }

    public void visitImport(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitImport(t,visit);}
    }

    public void visitInc(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitInc(t,visit);}
    }

    public void visitIndexOp(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitIndexOp(t,visit);}
    }

    public void visitInstanceInit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitInstanceInit(t,visit);}
    }

    public void visitInterfaceDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitInterfaceDef(t,visit);}
    }

    public void visitLabeledArg(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLabeledArg(t,visit);}
    }

    public void visitLabeledStat(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLabeledStat(t,visit);}
    }

    public void visitLand(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLand(t,visit);}
    }

    public void visitLbrack(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLbrack(t,visit);}
    }

    public void visitLcurly(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLcurly(t,visit);}
    }

    public void visitLe(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLe(t,visit);}
    }

    public void visitLetter(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLetter(t,visit);}
    }

    public void visitListConstructor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitListConstructor(t,visit);}
    }

    public void visitLiteralAs(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralAs(t,visit);}
    }

    public void visitLiteralAssert(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralAssert(t,visit);}
    }

    public void visitLiteralBoolean(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralBoolean(t,visit);}
    }

    public void visitLiteralBreak(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralBreak(t,visit);}
    }

    public void visitLiteralByte(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralByte(t,visit);}
    }

    public void visitLiteralCase(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralCase(t,visit);}
    }

    public void visitLiteralCatch(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralCatch(t,visit);}
    }

    public void visitLiteralChar(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralChar(t,visit);}
    }

    public void visitLiteralClass(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralClass(t,visit);}
    }

    public void visitLiteralContinue(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralContinue(t,visit);}
    }

    public void visitLiteralDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralDef(t,visit);}
    }

    public void visitLiteralDefault(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralDefault(t,visit);}
    }

    public void visitLiteralDouble(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralDouble(t,visit);}
    }

    public void visitLiteralElse(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralElse(t,visit);}
    }

    public void visitLiteralEnum(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralEnum(t,visit);}
    }

    public void visitLiteralExtends(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralExtends(t,visit);}
    }

    public void visitLiteralFalse(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralFalse(t,visit);}
    }

    public void visitLiteralFinally(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralFinally(t,visit);}
    }

    public void visitLiteralFloat(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralFloat(t,visit);}
    }

    public void visitLiteralFor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralFor(t,visit);}
    }

    public void visitLiteralIf(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralIf(t,visit);}
    }

    public void visitLiteralImplements(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralImplements(t,visit);}
    }

    public void visitLiteralImport(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralImport(t,visit);}
    }

    public void visitLiteralIn(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralIn(t,visit);}
    }

    public void visitLiteralInstanceof(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralInstanceof(t,visit);}
    }

    public void visitLiteralInt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralInt(t,visit);}
    }

    public void visitLiteralInterface(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralInterface(t,visit);}
    }

    public void visitLiteralLong(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralLong(t,visit);}
    }

    public void visitLiteralNative(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralNative(t,visit);}
    }

    public void visitLiteralNew(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralNew(t,visit);}
    }

    public void visitLiteralNull(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralNull(t,visit);}
    }

    public void visitLiteralPackage(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralPackage(t,visit);}
    }

    public void visitLiteralPrivate(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralPrivate(t,visit);}
    }

    public void visitLiteralProtected(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralProtected(t,visit);}
    }

    public void visitLiteralPublic(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralPublic(t,visit);}
    }

    public void visitLiteralReturn(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralReturn(t,visit);}
    }

    public void visitLiteralShort(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralShort(t,visit);}
    }

    public void visitLiteralStatic(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralStatic(t,visit);}
    }

    public void visitLiteralSuper(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralSuper(t,visit);}
    }

    public void visitLiteralSwitch(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralSwitch(t,visit);}
    }

    public void visitLiteralSynchronized(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralSynchronized(t,visit);}
    }

    public void visitLiteralThis(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralThis(t,visit);}
    }

    public void visitLiteralThreadsafe(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralThreadsafe(t,visit);}
    }

    public void visitLiteralThrow(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralThrow(t,visit);}
    }

    public void visitLiteralThrows(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralThrows(t,visit);}
    }

    public void visitLiteralTransient(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralTransient(t,visit);}
    }

    public void visitLiteralTrue(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralTrue(t,visit);}
    }

    public void visitLiteralTry(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralTry(t,visit);}
    }

    public void visitLiteralVoid(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralVoid(t,visit);}
    }

    public void visitLiteralVolatile(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralVolatile(t,visit);}
    }

    public void visitLiteralWhile(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLiteralWhile(t,visit);}
    }

    public void visitLnot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLnot(t,visit);}
    }

    public void visitLor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLor(t,visit);}
    }

    public void visitLparen(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLparen(t,visit);}
    }

    public void visitLt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitLt(t,visit);}
    }

    public void visitMapConstructor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMapConstructor(t,visit);}
    }

    public void visitMemberPointer(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMemberPointer(t,visit);}
    }

    public void visitMethodCall(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMethodCall(t,visit);}
    }

    public void visitMethodDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMethodDef(t,visit);}
    }

    public void visitMinus(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMinus(t,visit);}
    }

    public void visitMinusAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMinusAssign(t,visit);}
    }

    public void visitMlComment(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMlComment(t,visit);}
    }

    public void visitMod(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMod(t,visit);}
    }

    public void visitModifiers(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitModifiers(t,visit);}
    }

    public void visitModAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitModAssign(t,visit);}
    }

    public void visitMultiCatch(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMultiCatch(t, visit);}
    }

    public void visitMultiCatchTypes(final GroovySourceAST t, final int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitMultiCatchTypes(t, visit);}
    }

    public void visitNls(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNls(t,visit);}
    }

    public void visitNotEqual(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNotEqual(t,visit);}
    }

    public void visitNullTreeLookahead(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNullTreeLookahead(t,visit);}
    }

    public void visitNumBigDecimal(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumBigDecimal(t,visit);}
    }

    public void visitNumBigInt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumBigInt(t,visit);}
    }

    public void visitNumDouble(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumDouble(t,visit);}
    }

    public void visitNumFloat(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumFloat(t,visit);}
    }

    public void visitNumInt(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumInt(t,visit);}
    }

    public void visitNumLong(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitNumLong(t,visit);}
    }

    public void visitObjblock(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitObjblock(t,visit);}
    }

    public void visitOneNl(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitOneNl(t,visit);}
    }

    public void visitOptionalDot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitOptionalDot(t,visit);}
    }

    public void visitPackageDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitPackageDef(t,visit);}
    }

    public void visitParameters(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitParameters(t,visit);}
    }

    public void visitParameterDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitParameterDef(t,visit);}
    }

    public void visitPlus(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitPlus(t,visit);}
    }

    public void visitPlusAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitPlusAssign(t,visit);}
    }

    public void visitPostDec(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitPostDec(t,visit);}
    }

    public void visitPostInc(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitPostInc(t,visit);}
    }

    public void visitQuestion(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitQuestion(t,visit);}
    }

    public void visitRangeExclusive(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRangeExclusive(t,visit);}
    }

    public void visitRangeInclusive(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRangeInclusive(t,visit);}
    }

    public void visitRbrack(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRbrack(t,visit);}
    }

    public void visitRcurly(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRcurly(t,visit);}
    }

    public void visitRegexpCtorEnd(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRegexpCtorEnd(t,visit);}
    }

    public void visitRegexpLiteral(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRegexpLiteral(t,visit);}
    }

    public void visitRegexpSymbol(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRegexpSymbol(t,visit);}
    }

    public void visitRegexFind(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRegexFind(t,visit);}
    }

    public void visitRegexMatch(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRegexMatch(t,visit);}
    }

    public void visitRparen(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitRparen(t,visit);}
    }

    public void visitSelectSlot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSelectSlot(t,visit);}
    }

    public void visitSemi(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSemi(t,visit);}
    }

    public void visitShComment(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitShComment(t,visit);}
    }

    public void visitSl(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSl(t,visit);}
    }

    public void visitSlist(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSlist(t,visit);}
    }

    public void visitSlAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSlAssign(t,visit);}
    }

    public void visitSlComment(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSlComment(t,visit);}
    }

    public void visitSpreadArg(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSpreadArg(t,visit);}
    }

    public void visitSpreadDot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSpreadDot(t,visit);}
    }

    public void visitSpreadMapArg(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSpreadMapArg(t,visit);}
    }

    public void visitSr(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSr(t,visit);}
    }

    public void visitSrAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSrAssign(t,visit);}
    }

    public void visitStar(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStar(t,visit);}
    }

    public void visitStarAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStarAssign(t,visit);}
    }

    public void visitStarStar(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStarStar(t,visit);}
    }

    public void visitStarStarAssign(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStarStarAssign(t,visit);}
    }

    public void visitStaticImport(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStaticImport(t,visit);}
    }

    public void visitStaticInit(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStaticInit(t,visit);}
    }

    public void visitStrictfp(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStrictfp(t,visit);}
    }

    public void visitStringCh(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringCh(t,visit);}
    }

    public void visitStringConstructor(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringConstructor(t,visit);}
    }

    public void visitStringCtorEnd(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringCtorEnd(t,visit);}
    }

    public void visitStringCtorMiddle(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringCtorMiddle(t,visit);}
    }

    public void visitStringCtorStart(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringCtorStart(t,visit);}
    }

    public void visitStringLiteral(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringLiteral(t,visit);}
    }

    public void visitStringNl(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitStringNl(t,visit);}
    }

    public void visitSuperCtorCall(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitSuperCtorCall(t,visit);}
    }

    public void visitTraitDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTraitDef(t,visit);}
    }

    public void visitTripleDot(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTripleDot(t,visit);}
    }

    public void visitType(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitType(t,visit);}
    }

    public void visitTypecast(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypecast(t,visit);}
    }

    public void visitTypeArgument(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeArgument(t,visit);}
    }

    public void visitTypeArguments(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeArguments(t,visit);}
    }

    public void visitTypeLowerBounds(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeLowerBounds(t,visit);}
    }

    public void visitTypeParameter(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeParameter(t,visit);}
    }

    public void visitTypeParameters(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeParameters(t,visit);}
    }

    public void visitTypeUpperBounds(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitTypeUpperBounds(t,visit);}
    }

    public void visitUnaryMinus(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitUnaryMinus(t,visit);}
    }

    public void visitUnaryPlus(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitUnaryPlus(t,visit);}
    }

    public void visitUnusedConst(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitUnusedConst(t,visit);}
    }

    public void visitUnusedDo(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitUnusedDo(t,visit);}
    }

    public void visitUnusedGoto(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitUnusedGoto(t,visit);}
    }

    public void visitVariableDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitVariableDef(t,visit);}
    }

    public void visitVariableParameterDef(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitVariableParameterDef(t,visit);}
    }

    public void visitVocab(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitVocab(t,visit);}
    }

    public void visitWildcardType(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitWildcardType(t,visit);}
    }

    public void visitWs(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitWs(t,visit);}
    }

    public void visitDefault(GroovySourceAST t, int visit) {
        Iterator itr = itr(visit);
        while (itr.hasNext()) {((Visitor)itr.next()).visitDefault(t,visit);}
    }

    public void tearDown() {
        for (Object backToFrontVisitor : backToFrontVisitors) {
            ((Visitor) backToFrontVisitor).tearDown();
        }
    }

    public void push(GroovySourceAST t) {
        for (Object visitor : visitors) {
            ((Visitor) visitor).push(t);
        }
    }
    public GroovySourceAST pop() {
        GroovySourceAST lastNodePopped = null;
        for (Object backToFrontVisitor : backToFrontVisitors) {
            lastNodePopped = ((Visitor) backToFrontVisitor).pop();
        }
        return lastNodePopped;
    }
}
