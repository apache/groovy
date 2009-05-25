/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.powerassert;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.List;
import java.util.ListIterator;

/**
 * Rewrites all assertions in a source unit.
 *
 * @author Peter Niederwieser
 */
public class AssertionRewriter extends StatementReplacingVisitorSupport {
    static final VariableExpression recorderVariable = new VariableExpression("$valueRecorder");

    private static final ClassNode verifierClass = ClassHelper.makeWithoutCaching(AssertionVerifier.class);
    private static final ClassNode recorderClass = ClassHelper.makeWithoutCaching(ValueRecorder.class);

    private final SourceUnit sourceUnit;
    private final Janitor janitor = new Janitor();

    private boolean assertFound;

    private AssertionRewriter(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    /**
     * Rewrites all assertions in the given source unit.
     *
     * @param sourceUnit a source unit
     */
    public static void rewrite(SourceUnit sourceUnit) {
        new AssertionRewriter(sourceUnit).visitModule();
    }

    private void visitModule() {
        ModuleNode module = sourceUnit.getAST();

        try {
            @SuppressWarnings("unchecked")
            List<ClassNode> classes = module.getClasses();
            for (ClassNode clazz : classes)
                visitClass(clazz);
        } finally {
            janitor.cleanup();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitClass(ClassNode node) {
        visitAnnotations(node);
        node.visitContents(this);
        visitInstanceInitializer(node.getObjectInitializerStatements());
    }

    private void visitInstanceInitializer(List<Statement> stats) {
        boolean old = assertFound;
        assertFound = false;
        for (Statement stat : stats) stat.visit(this);
        if (assertFound) defineRecorderVariable(stats);
        assertFound = old;
    }

    @Override
    public void visitConstructor(ConstructorNode constructor) {
        boolean old = assertFound;
        assertFound = false;
        super.visitConstructor(constructor);
        if (assertFound) defineRecorderVariable((BlockStatement) constructor.getCode());
        assertFound = old;
    }

    @Override
    public void visitMethod(MethodNode method) {
        boolean old = assertFound;
        assertFound = false;
        super.visitMethod(method);
        if (assertFound) defineRecorderVariable((BlockStatement) method.getCode());
        assertFound = old;
    }

    @Override
    public void visitClosureExpression(ClosureExpression expr) {
        boolean old = assertFound;
        assertFound = false;
        super.visitClosureExpression(expr);
        if (assertFound) defineRecorderVariable((BlockStatement) expr.getCode());
        assertFound = old;
    }

    @Override
    public void visitAssertStatement(AssertStatement stat) {
        super.visitAssertStatement(stat);
        rewriteAssertion(stat);
    }

    private void rewriteAssertion(AssertStatement stat) {
        if (stat.getMessageExpression() != ConstantExpression.NULL)
            return; // don't rewrite assertions with message

        SourceText text;
        try {
            // because source position seems to be more reliable for statements
            // than for expressions, we get the source text for the whole statement
            text = new SourceText(stat, sourceUnit, janitor);
        } catch (SourceTextNotAvailableException e) {
            return; // don't rewrite assertions w/o source text
        }

        assertFound = true;

        ExpressionStatement verifyCall =
                new ExpressionStatement(
                        new MethodCallExpression(
                                new ClassExpression(verifierClass),
                                AssertionVerifier.VERIFY_METHOD_NAME,
                                new ArgumentListExpression(
                                        TruthExpressionRewriter.rewrite(stat.getBooleanExpression(), text, this),
                                        new ConstantExpression(text.getNormalizedText()),
                                        recorderVariable
                                )
                        )
                );

        // if we don't wrap call in a BlockStatement, line information in class file will be wrong
        BlockStatement tryBlock = new BlockStatement();
        tryBlock.addStatement(verifyCall);
        tryBlock.setSourcePosition(stat);

        TryCatchStatement tryCatchStat =
                new TryCatchStatement(
                        tryBlock,
                        // clear recorded values in finally block to avoid any risk of a memory leak
                        new ExpressionStatement(
                                new MethodCallExpression(
                                        recorderVariable,
                                        ValueRecorder.CLEAR_METHOD_NAME,
                                        ArgumentListExpression.EMPTY_ARGUMENTS
                                )
                        )
                );
        
        replaceVisitedStatementWith(tryCatchStat);
    }

    private static void defineRecorderVariable(BlockStatement block) {
        defineRecorderVariable(block.getStatements());
    }

    private static void defineRecorderVariable(List<Statement> stats) {
        // recorder variable needs to be defined in outermost scope,
        // hence we insert it at the beginning of the block
        int insertPos = startsWithConstructorCall(stats) ? 1 : 0;

        stats.add(insertPos,
                new ExpressionStatement(
                        new DeclarationExpression(
                                recorderVariable,
                                Token.newSymbol(Types.ASSIGN, -1, -1),
                                new ConstructorCallExpression(
                                        recorderClass,
                                        ArgumentListExpression.EMPTY_ARGUMENTS
                                )
                        )
                )
        );
    }

    private static boolean startsWithConstructorCall(List<Statement> stats) {
        if (stats.size() == 0) return false;
        Statement stat = stats.get(0);
        return stat instanceof ExpressionStatement
                && ((ExpressionStatement)stat).getExpression() instanceof ConstructorCallExpression;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        throw new UnsupportedOperationException("getSourceUnit");
    }
}

