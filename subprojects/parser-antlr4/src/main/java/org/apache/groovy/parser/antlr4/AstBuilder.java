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
package org.apache.groovy.parser.antlr4;

import groovy.lang.IntRange;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.groovy.parser.antlr4.internal.AtnManager;
import org.apache.groovy.parser.antlr4.internal.DescriptiveErrorStrategy;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.antlr.EnumHelper;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.EnumConstantClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.groovy.parser.antlr4.GroovyLangParser.*;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

/**
 * Building the AST from the parse tree generated by Antlr4
 *
 * @author <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 *         Created on 2016/08/14
 */
public class AstBuilder extends GroovyParserBaseVisitor<Object> implements GroovyParserVisitor<Object> {

    public AstBuilder(SourceUnit sourceUnit, ClassLoader classLoader) {
        this.sourceUnit = sourceUnit;
        this.moduleNode = new ModuleNode(sourceUnit);
        this.classLoader = classLoader; // unused for the time being

        this.lexer = new GroovyLangLexer(
                new ANTLRInputStream(
                        this.readSourceCode(sourceUnit)));
        this.parser = new GroovyLangParser(
                new CommonTokenStream(this.lexer));

        this.parser.setErrorHandler(new DescriptiveErrorStrategy());

        this.tryWithResourcesASTTransformation = new TryWithResourcesASTTransformation(this);
        this.groovydocManager = new GroovydocManager(this);
    }

    private GroovyParserRuleContext buildCST() {
        GroovyParserRuleContext result;

        // parsing have to wait util clearing is complete.
        AtnManager.RRWL.readLock().lock();
        try {
            result = buildCST(PredictionMode.SLL);
        } catch (Throwable t) {
            // if some syntax error occurred in the lexer, no need to retry the powerful LL mode
            if (t instanceof GroovySyntaxError && GroovySyntaxError.LEXER == ((GroovySyntaxError) t).getSource()) {
                throw t;
            }

            result = buildCST(PredictionMode.LL);
        } finally {
            AtnManager.RRWL.readLock().unlock();
        }

        return result;
    }

    private GroovyParserRuleContext buildCST(PredictionMode predictionMode) {
        parser.getInterpreter().setPredictionMode(predictionMode);

        if (PredictionMode.SLL.equals(predictionMode)) {
            this.removeErrorListeners();
        } else {
            ((CommonTokenStream) parser.getInputStream()).reset();
            this.addErrorListeners();
        }

        return parser.compilationUnit();
    }

    public ModuleNode buildAST() {
        try {
            return (ModuleNode) this.visit(this.buildCST());
        } catch (Throwable t) {
            CompilationFailedException cfe;

            if (t instanceof CompilationFailedException) {
                cfe = (CompilationFailedException) t;
            } else if (t instanceof ParseCancellationException) {
                cfe = createParsingFailedException(t.getCause());
            } else {
                cfe = createParsingFailedException(t);
            }

//            LOGGER.log(Level.SEVERE, "Failed to build AST", cfe);

            throw cfe;
        }
    }

    @Override
    public ModuleNode visitCompilationUnit(CompilationUnitContext ctx) {
        this.visit(ctx.packageDeclaration());

        ctx.statement().stream()
                .map(this::visit)
//                .filter(e -> e instanceof Statement)
                .forEach(e -> {
                    if (e instanceof DeclarationListStatement) { // local variable declaration
                        ((DeclarationListStatement) e).getDeclarationStatements().forEach(moduleNode::addStatement);
                    } else if (e instanceof Statement) {
                        moduleNode.addStatement((Statement) e);
                    } else if (e instanceof MethodNode) { // script method
                        moduleNode.addMethod((MethodNode) e);
                    }
                });

        this.classNodeList.forEach(moduleNode::addClass);

        if (this.isPackageInfoDeclaration()) {
            this.addPackageInfoClassNode();
        } else {
            // if groovy source file only contains blank(including EOF), add "return null" to the AST
            if (this.isBlankScript(ctx)) {
                this.addEmptyReturnStatement();
            }
        }

        this.configureScriptClassNode();

        return moduleNode;
    }

    @Override
    public PackageNode visitPackageDeclaration(PackageDeclarationContext ctx) {
        String packageName = this.visitQualifiedName(ctx.qualifiedName());
        moduleNode.setPackageName(packageName + DOT_STR);

        PackageNode packageNode = moduleNode.getPackage();

        this.visitAnnotationsOpt(ctx.annotationsOpt()).forEach(packageNode::addAnnotation);

        return this.configureAST(packageNode, ctx);
    }

    @Override
    public ImportNode visitImportDeclaration(ImportDeclarationContext ctx) {
        ImportNode importNode;

        boolean hasStatic = asBoolean(ctx.STATIC());
        boolean hasStar = asBoolean(ctx.MUL());
        boolean hasAlias = asBoolean(ctx.alias);

        List<AnnotationNode> annotationNodeList = this.visitAnnotationsOpt(ctx.annotationsOpt());

        if (hasStatic) {
            if (hasStar) { // e.g. import static java.lang.Math.*
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());
                ClassNode type = ClassHelper.make(qualifiedName);


                moduleNode.addStaticStarImport(type.getText(), type, annotationNodeList);

                importNode = last(moduleNode.getStaticStarImports().values());
            } else { // e.g. import static java.lang.Math.pow
                List<GroovyParserRuleContext> identifierList = new LinkedList<>(ctx.qualifiedName().qualifiedNameElement());
                int identifierListSize = identifierList.size();
                String name = identifierList.get(identifierListSize - 1).getText();
                ClassNode classNode =
                        ClassHelper.make(
                                identifierList.stream()
                                        .limit(identifierListSize - 1)
                                        .map(ParseTree::getText)
                                        .collect(Collectors.joining(DOT_STR)));
                String alias = hasAlias
                        ? ctx.alias.getText()
                        : name;

                moduleNode.addStaticImport(classNode, name, alias, annotationNodeList);

                importNode = last(moduleNode.getStaticImports().values());
            }
        } else {
            if (hasStar) { // e.g. import java.util.*
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());

                moduleNode.addStarImport(qualifiedName + DOT_STR, annotationNodeList);

                importNode = last(moduleNode.getStarImports());
            } else { // e.g. import java.util.Map
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());
                String name = last(ctx.qualifiedName().qualifiedNameElement()).getText();
                ClassNode classNode = ClassHelper.make(qualifiedName);
                String alias = hasAlias
                        ? ctx.alias.getText()
                        : name;

                moduleNode.addImport(alias, classNode, annotationNodeList);

                importNode = last(moduleNode.getImports());
            }
        }

        return this.configureAST(importNode, ctx);
    }

    // statement {    --------------------------------------------------------------------
    @Override
    public AssertStatement visitAssertStatement(AssertStatementContext ctx) {
        Expression conditionExpression = (Expression) this.visit(ctx.ce);
        BooleanExpression booleanExpression =
                this.configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        if (!asBoolean(ctx.me)) {
            return this.configureAST(
                    new AssertStatement(booleanExpression), ctx);
        }

        return this.configureAST(new AssertStatement(booleanExpression,
                        (Expression) this.visit(ctx.me)),
                ctx);
    }

    @Override
    public AssertStatement visitAssertStmtAlt(AssertStmtAltContext ctx) {
        return this.configureAST(this.visitAssertStatement(ctx.assertStatement()), ctx);
    }

    @Override
    public IfStatement visitIfElseStmtAlt(IfElseStmtAltContext ctx) {
        Expression conditionExpression = this.visitParExpression(ctx.parExpression());
        BooleanExpression booleanExpression =
                this.configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        Statement ifBlock =
                this.unpackStatement(
                        (Statement) this.visit(ctx.tb));
        Statement elseBlock =
                this.unpackStatement(
                        asBoolean(ctx.ELSE())
                                ? (Statement) this.visit(ctx.fb)
                                : EmptyStatement.INSTANCE);

        return this.configureAST(new IfStatement(booleanExpression, ifBlock, elseBlock), ctx);
    }

    @Override
    public Statement visitLoopStmtAlt(LoopStmtAltContext ctx) {
        return this.configureAST((Statement) this.visit(ctx.loopStatement()), ctx);
    }

    @Override
    public ForStatement visitForStmtAlt(ForStmtAltContext ctx) {
        Pair<Parameter, Expression> controlPair = this.visitForControl(ctx.forControl());

        Statement loopBlock = this.unpackStatement((Statement) this.visit(ctx.statement()));

        return this.configureAST(
                new ForStatement(controlPair.getKey(), controlPair.getValue(), asBoolean(loopBlock) ? loopBlock : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public Pair<Parameter, Expression> visitForControl(ForControlContext ctx) {
        if (asBoolean(ctx.enhancedForControl())) { // e.g. for(int i in 0..<10) {}
            return this.visitEnhancedForControl(ctx.enhancedForControl());
        }

        if (asBoolean(ctx.classicalForControl())) { // e.g. for(int i = 0; i < 10; i++) {}
            return this.visitClassicalForControl(ctx.classicalForControl());
        }

        throw createParsingFailedException("Unsupported for control: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitForInit(ForInitContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        if (asBoolean(ctx.localVariableDeclaration())) {
            DeclarationListStatement declarationListStatement = this.visitLocalVariableDeclaration(ctx.localVariableDeclaration());
            List<?> declarationExpressionList = declarationListStatement.getDeclarationExpressions();

            if (declarationExpressionList.size() == 1) {
                return this.configureAST((Expression) declarationExpressionList.get(0), ctx);
            } else {
                return this.configureAST(new ClosureListExpression((List<Expression>) declarationExpressionList), ctx);
            }
        }

        if (asBoolean(ctx.expressionList())) {
            return this.translateExpressionList(ctx.expressionList());
        }

        throw createParsingFailedException("Unsupported for init: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitForUpdate(ForUpdateContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        return this.translateExpressionList(ctx.expressionList());
    }

    private Expression translateExpressionList(ExpressionListContext ctx) {
        List<Expression> expressionList = this.visitExpressionList(ctx);

        if (expressionList.size() == 1) {
            return this.configureAST(expressionList.get(0), ctx);
        } else {
            return this.configureAST(new ClosureListExpression(expressionList), ctx);
        }
    }

    @Override
    public Pair<Parameter, Expression> visitEnhancedForControl(EnhancedForControlContext ctx) {
        Parameter parameter = this.configureAST(
                new Parameter(this.visitType(ctx.type()), this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName()),
                ctx.variableDeclaratorId());

        // FIXME Groovy will ignore variableModifier of parameter in the for control
        // In order to make the new parser behave same with the old one, we do not process variableModifier*

        return new Pair<>(parameter, (Expression) this.visit(ctx.expression()));
    }

    @Override
    public Pair<Parameter, Expression> visitClassicalForControl(ClassicalForControlContext ctx) {
        ClosureListExpression closureListExpression = new ClosureListExpression();

        closureListExpression.addExpression(this.visitForInit(ctx.forInit()));
        closureListExpression.addExpression(asBoolean(ctx.expression()) ? (Expression) this.visit(ctx.expression()) : EmptyExpression.INSTANCE);
        closureListExpression.addExpression(this.visitForUpdate(ctx.forUpdate()));

        return new Pair<>(ForStatement.FOR_LOOP_DUMMY, closureListExpression);
    }

    @Override
    public WhileStatement visitWhileStmtAlt(WhileStmtAltContext ctx) {
        Expression conditionExpression = this.visitParExpression(ctx.parExpression());
        BooleanExpression booleanExpression =
                this.configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        Statement loopBlock = this.unpackStatement((Statement) this.visit(ctx.statement()));

        return this.configureAST(
                new WhileStatement(booleanExpression, asBoolean(loopBlock) ? loopBlock : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public DoWhileStatement visitDoWhileStmtAlt(DoWhileStmtAltContext ctx) {
        Expression conditionExpression = this.visitParExpression(ctx.parExpression());

        BooleanExpression booleanExpression =
                this.configureAST(
                        new BooleanExpression(conditionExpression),
                        conditionExpression
                );

        Statement loopBlock = this.unpackStatement((Statement) this.visit(ctx.statement()));

        return this.configureAST(
                new DoWhileStatement(booleanExpression, asBoolean(loopBlock) ? loopBlock : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public Statement visitTryCatchStmtAlt(TryCatchStmtAltContext ctx) {
        return this.configureAST(this.visitTryCatchStatement(ctx.tryCatchStatement()), ctx);
    }

    @Override
    public Statement visitTryCatchStatement(TryCatchStatementContext ctx) {
        TryCatchStatement tryCatchStatement =
                new TryCatchStatement((Statement) this.visit(ctx.block()),
                        this.visitFinallyBlock(ctx.finallyBlock()));

        if (asBoolean(ctx.resources())) {
            this.visitResources(ctx.resources()).forEach(tryCatchStatement::addResource);
        }

        ctx.catchClause().stream().map(this::visitCatchClause)
                .reduce(new LinkedList<CatchStatement>(), (r, e) -> {
                    r.addAll(e); // merge several LinkedList<CatchStatement> instances into one LinkedList<CatchStatement> instance
                    return r;
                })
                .forEach(tryCatchStatement::addCatch);

        return this.configureAST(
                tryWithResourcesASTTransformation.transform(
                        this.configureAST(tryCatchStatement, ctx)),
                ctx);
    }


    @Override
    public List<ExpressionStatement> visitResources(ResourcesContext ctx) {
        return this.visitResourceList(ctx.resourceList());
    }

    @Override
    public List<ExpressionStatement> visitResourceList(ResourceListContext ctx) {
        return ctx.resource().stream().map(this::visitResource).collect(Collectors.toList());
    }

    @Override
    public ExpressionStatement visitResource(ResourceContext ctx) {
        if (asBoolean(ctx.localVariableDeclaration())) {
            List<ExpressionStatement> declarationStatements = this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()).getDeclarationStatements();

            if (declarationStatements.size() > 1) {
                throw createParsingFailedException("Multi resources can not be declared in one statement", ctx);
            }

            return declarationStatements.get(0);
        } else if (asBoolean(ctx.expression())) {
            Expression expression = (Expression) this.visit(ctx.expression());
            if (!(expression instanceof BinaryExpression
                    && Types.ASSIGN == ((BinaryExpression) expression).getOperation().getType()
                    && ((BinaryExpression) expression).getLeftExpression() instanceof VariableExpression)) {

                throw createParsingFailedException("Only variable declarations are allowed to declare resource", ctx);
            }

            BinaryExpression assignmentExpression = (BinaryExpression) expression;

            return this.configureAST(
                    new ExpressionStatement(
                            this.configureAST(
                                    new DeclarationExpression(
                                            this.configureAST(
                                                    new VariableExpression(assignmentExpression.getLeftExpression().getText()),
                                                    assignmentExpression.getLeftExpression()
                                            ),
                                            assignmentExpression.getOperation(),
                                            assignmentExpression.getRightExpression()
                                    ), ctx)
                    ), ctx);
        }

        throw createParsingFailedException("Unsupported resource declaration: " + ctx.getText(), ctx);
    }

    /**
     * Multi-catch(1..*) clause will be unpacked to several normal catch clauses, so the return type is List
     *
     * @param ctx the parse tree
     * @return
     */
    @Override
    public List<CatchStatement> visitCatchClause(CatchClauseContext ctx) {
        // FIXME Groovy will ignore variableModifier of parameter in the catch clause
        // In order to make the new parser behave same with the old one, we do not process variableModifier*

        return this.visitCatchType(ctx.catchType()).stream()
                .map(e -> this.configureAST(
                        new CatchStatement(
                                // FIXME The old parser does not set location info for the parameter of the catch clause.
                                // we could make it better
                                //this.configureAST(new Parameter(e, this.visitIdentifier(ctx.identifier())), ctx.Identifier()),

                                new Parameter(e, this.visitIdentifier(ctx.identifier())),
                                this.visitBlock(ctx.block())),
                        ctx))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassNode> visitCatchType(CatchTypeContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.singletonList(ClassHelper.OBJECT_TYPE);
        }

        return ctx.qualifiedClassName().stream()
                .map(this::visitQualifiedClassName)
                .collect(Collectors.toList());
    }


    @Override
    public Statement visitFinallyBlock(FinallyBlockContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyStatement.INSTANCE;
        }

        return this.configureAST(
                this.createBlockStatement((Statement) this.visit(ctx.block())),
                ctx);
    }

    @Override
    public SwitchStatement visitSwitchStmtAlt(SwitchStmtAltContext ctx) {
        return this.configureAST(this.visitSwitchStatement(ctx.switchStatement()), ctx);
    }

    public SwitchStatement visitSwitchStatement(SwitchStatementContext ctx) {
        List<Statement> statementList =
                ctx.switchBlockStatementGroup().stream()
                        .map(this::visitSwitchBlockStatementGroup)
                        .reduce(new LinkedList<>(), (r, e) -> {
                            r.addAll(e);
                            return r;
                        });

        List<CaseStatement> caseStatementList = new LinkedList<>();
        List<Statement> defaultStatementList = new LinkedList<>();

        statementList.forEach(e -> {
            if (e instanceof CaseStatement) {
                caseStatementList.add((CaseStatement) e);
            } else if (isTrue(e, IS_SWITCH_DEFAULT)) {
                defaultStatementList.add(e);
            }
        });

        int defaultStatementListSize = defaultStatementList.size();
        if (defaultStatementListSize > 1) {
            throw createParsingFailedException("switch statement should have only one default case, which should appear at last", defaultStatementList.get(0));
        }

        if (defaultStatementListSize > 0 && last(statementList) instanceof CaseStatement) {
            throw createParsingFailedException("default case should appear at last", defaultStatementList.get(0));
        }

        return this.configureAST(
                new SwitchStatement(
                        this.visitParExpression(ctx.parExpression()),
                        caseStatementList,
                        defaultStatementListSize == 0 ? EmptyStatement.INSTANCE : defaultStatementList.get(0)
                ),
                ctx);

    }


    @Override
    @SuppressWarnings({"unchecked"})
    public List<Statement> visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
        int labelCnt = ctx.switchLabel().size();
        List<Token> firstLabelHolder = new ArrayList<>(1);

        return (List<Statement>) ctx.switchLabel().stream()
                .map(e -> (Object) this.visitSwitchLabel(e))
                .reduce(new ArrayList<Statement>(4), (r, e) -> {
                    List<Statement> statementList = (List<Statement>) r;
                    Pair<Token, Expression> pair = (Pair<Token, Expression>) e;

                    boolean isLast = labelCnt - 1 == statementList.size();

                    switch (pair.getKey().getType()) {
                        case CASE: {
                            if (!asBoolean(statementList)) {
                                firstLabelHolder.add(pair.getKey());
                            }

                            statementList.add(
                                    this.configureAST(
                                            new CaseStatement(
                                                    pair.getValue(),

                                                    // check whether processing the last label. if yes, block statement should be attached.
                                                    isLast ? this.visitBlockStatements(ctx.blockStatements())
                                                            : EmptyStatement.INSTANCE
                                            ),
                                            firstLabelHolder.get(0)));

                            break;
                        }
                        case DEFAULT: {

                            BlockStatement blockStatement = this.visitBlockStatements(ctx.blockStatements());
                            blockStatement.putNodeMetaData(IS_SWITCH_DEFAULT, true);

                            statementList.add(
                                    // this.configureAST(blockStatement, pair.getKey())
                                    blockStatement
                            );

                            break;
                        }
                    }

                    return statementList;
                });

    }

    @Override
    public Pair<Token, Expression> visitSwitchLabel(SwitchLabelContext ctx) {
        if (asBoolean(ctx.CASE())) {
            return new Pair<>(ctx.CASE().getSymbol(), (Expression) this.visit(ctx.expression()));
        } else if (asBoolean(ctx.DEFAULT())) {
            return new Pair<>(ctx.DEFAULT().getSymbol(), EmptyExpression.INSTANCE);
        }

        throw createParsingFailedException("Unsupported switch label: " + ctx.getText(), ctx);
    }


    @Override
    public SynchronizedStatement visitSynchronizedStmtAlt(SynchronizedStmtAltContext ctx) {
        return this.configureAST(
                new SynchronizedStatement(this.visitParExpression(ctx.parExpression()), this.visitBlock(ctx.block())),
                ctx);
    }


    @Override
    public ExpressionStatement visitExpressionStmtAlt(ExpressionStmtAltContext ctx) {
        return (ExpressionStatement) this.visit(ctx.statementExpression());
    }

    @Override
    public ReturnStatement visitReturnStmtAlt(ReturnStmtAltContext ctx) {
        return this.configureAST(new ReturnStatement(asBoolean(ctx.expression())
                        ? (Expression) this.visit(ctx.expression())
                        : ConstantExpression.EMPTY_EXPRESSION),
                ctx);
    }

    @Override
    public ThrowStatement visitThrowStmtAlt(ThrowStmtAltContext ctx) {
        return this.configureAST(
                new ThrowStatement((Expression) this.visit(ctx.expression())),
                ctx);
    }

    @Override
    public Statement visitLabeledStmtAlt(LabeledStmtAltContext ctx) {
        Statement statement = (Statement) this.visit(ctx.statement());

        statement.addStatementLabel(this.visitIdentifier(ctx.identifier()));

        return statement; // this.configureAST(statement, ctx);
    }

    @Override
    public BreakStatement visitBreakStatement(BreakStatementContext ctx) {
        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return this.configureAST(new BreakStatement(label), ctx);
    }

    @Override
    public BreakStatement visitBreakStmtAlt(BreakStmtAltContext ctx) {
        return this.configureAST(this.visitBreakStatement(ctx.breakStatement()), ctx);
    }

    @Override
    public ContinueStatement visitContinueStatement(ContinueStatementContext ctx) {
        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return this.configureAST(new ContinueStatement(label), ctx);

    }

    @Override
    public ContinueStatement visitContinueStmtAlt(ContinueStmtAltContext ctx) {
        return this.configureAST(this.visitContinueStatement(ctx.continueStatement()), ctx);
    }

    @Override
    public ImportNode visitImportStmtAlt(ImportStmtAltContext ctx) {
        return this.configureAST(this.visitImportDeclaration(ctx.importDeclaration()), ctx);
    }

    @Override
    public ClassNode visitTypeDeclarationStmtAlt(TypeDeclarationStmtAltContext ctx) {
        return this.configureAST(this.visitTypeDeclaration(ctx.typeDeclaration()), ctx);
    }


    @Override
    public Statement visitLocalVariableDeclarationStmtAlt(LocalVariableDeclarationStmtAltContext ctx) {
        return this.configureAST(this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()), ctx);
    }

    @Override
    public MethodNode visitMethodDeclarationStmtAlt(MethodDeclarationStmtAltContext ctx) {
        return this.configureAST(this.visitMethodDeclaration(ctx.methodDeclaration()), ctx);
    }

    // } statement    --------------------------------------------------------------------

    @Override
    public ClassNode visitTypeDeclaration(TypeDeclarationContext ctx) {
        if (asBoolean(ctx.classDeclaration())) { // e.g. class A {}
            ctx.classDeclaration().putNodeMetaData(TYPE_DECLARATION_MODIFIERS, this.visitClassOrInterfaceModifiersOpt(ctx.classOrInterfaceModifiersOpt()));
            return this.configureAST(this.visitClassDeclaration(ctx.classDeclaration()), ctx);
        }

        throw createParsingFailedException("Unsupported type declaration: " + ctx.getText(), ctx);
    }

    private void initUsingGenerics(ClassNode classNode) {
        if (classNode.isUsingGenerics()) {
            return;
        }

        if (!classNode.isEnum()) {
            classNode.setUsingGenerics(classNode.getSuperClass().isUsingGenerics());
        }

        if (!classNode.isUsingGenerics() && asBoolean((Object) classNode.getInterfaces())) {
            for (ClassNode anInterface : classNode.getInterfaces()) {
                classNode.setUsingGenerics(classNode.isUsingGenerics() || anInterface.isUsingGenerics());

                if (classNode.isUsingGenerics())
                    break;
            }
        }
    }

    @Override
    public ClassNode visitClassDeclaration(ClassDeclarationContext ctx) {
        String packageName = moduleNode.getPackageName();
        packageName = asBoolean((Object) packageName) ? packageName : "";

        List<ModifierNode> modifierNodeList = ctx.getNodeMetaData(TYPE_DECLARATION_MODIFIERS);
        Objects.requireNonNull(modifierNodeList, "modifierNodeList should not be null");

        ModifierManager modifierManager = new ModifierManager(this, modifierNodeList);
        int modifiers = modifierManager.getClassModifiersOpValue();

        boolean syntheticPublic = ((modifiers & Opcodes.ACC_SYNTHETIC) != 0);
        modifiers &= ~Opcodes.ACC_SYNTHETIC;

        final ClassNode outerClass = classNodeStack.peek();
        ClassNode classNode;
        String className = this.visitIdentifier(ctx.identifier());
        if (asBoolean(ctx.ENUM())) {
            classNode =
                    EnumHelper.makeEnumNode(
                            asBoolean(outerClass) ? className : packageName + className,
                            modifiers, null, outerClass);
        } else {
            if (asBoolean(outerClass)) {
                classNode =
                        new InnerClassNode(
                                outerClass,
                                outerClass.getName() + "$" + className,
                                modifiers | (outerClass.isInterface() ? Opcodes.ACC_STATIC : 0),
                                ClassHelper.OBJECT_TYPE);
            } else {
                classNode =
                        new ClassNode(
                                packageName + className,
                                modifiers,
                                ClassHelper.OBJECT_TYPE);
            }

        }

        this.configureAST(classNode, ctx);
        classNode.putNodeMetaData(CLASS_NAME, className);
        classNode.setSyntheticPublic(syntheticPublic);

        if (asBoolean(ctx.TRAIT())) {
            classNode.addAnnotation(new AnnotationNode(ClassHelper.make(GROOVY_TRANSFORM_TRAIT)));
        }
        classNode.addAnnotations(modifierManager.getAnnotations());
        classNode.setGenericsTypes(this.visitTypeParameters(ctx.typeParameters()));

        boolean isInterface = asBoolean(ctx.INTERFACE()) && !asBoolean(ctx.AT());
        boolean isInterfaceWithDefaultMethods = false;

        // declaring interface with default method
        if (isInterface && this.containsDefaultMethods(ctx)) {
            isInterfaceWithDefaultMethods = true;
            classNode.addAnnotation(new AnnotationNode(ClassHelper.make(GROOVY_TRANSFORM_TRAIT)));
            classNode.putNodeMetaData(IS_INTERFACE_WITH_DEFAULT_METHODS, true);
        }

        if (asBoolean(ctx.CLASS()) || asBoolean(ctx.TRAIT()) || isInterfaceWithDefaultMethods) { // class OR trait OR interface with default methods
            classNode.setSuperClass(this.visitType(ctx.sc));
            classNode.setInterfaces(this.visitTypeList(ctx.is));

            this.initUsingGenerics(classNode);
        } else if (isInterface) { // interface(NOT annotation)
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT);

            classNode.setSuperClass(ClassHelper.OBJECT_TYPE);
            classNode.setInterfaces(this.visitTypeList(ctx.scs));

            this.initUsingGenerics(classNode);

            this.hackMixins(classNode);
        } else if (asBoolean(ctx.ENUM())) { // enum
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_ENUM | Opcodes.ACC_FINAL);

            classNode.setInterfaces(this.visitTypeList(ctx.is));

            this.initUsingGenerics(classNode);
        } else if (asBoolean(ctx.AT())) { // annotation
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_ANNOTATION);

            classNode.addInterface(ClassHelper.Annotation_TYPE);

            this.hackMixins(classNode);
        } else {
            throw createParsingFailedException("Unsupported class declaration: " + ctx.getText(), ctx);
        }

        // we put the class already in output to avoid the most inner classes
        // will be used as first class later in the loader. The first class
        // there determines what GCL#parseClass for example will return, so we
        // have here to ensure it won't be the inner class
        if (asBoolean(ctx.CLASS()) || asBoolean(ctx.TRAIT())) {
            classNodeList.add(classNode);
        }

        int oldAnonymousInnerClassCounter = this.anonymousInnerClassCounter;
        classNodeStack.push(classNode);
        ctx.classBody().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
        this.visitClassBody(ctx.classBody());
        classNodeStack.pop();
        this.anonymousInnerClassCounter = oldAnonymousInnerClassCounter;

        if (!(asBoolean(ctx.CLASS()) || asBoolean(ctx.TRAIT()))) {
            classNodeList.add(classNode);
        }

        groovydocManager.handle(classNode, ctx);

        return classNode;
    }

    @SuppressWarnings({"unchecked"})
    private boolean containsDefaultMethods(ClassDeclarationContext ctx) {
        List<MethodDeclarationContext> methodDeclarationContextList =
                (List<MethodDeclarationContext>) ctx.classBody().classBodyDeclaration().stream()
                .map(ClassBodyDeclarationContext::memberDeclaration)
                .filter(Objects::nonNull)
                .map(e -> (Object) e.methodDeclaration())
                .filter(Objects::nonNull).reduce(new LinkedList<MethodDeclarationContext>(), (r, e) -> {
                    MethodDeclarationContext methodDeclarationContext = (MethodDeclarationContext) e;

                    if (createModifierManager(methodDeclarationContext).contains(DEFAULT)) {
                        ((List) r).add(methodDeclarationContext);
                    }

                    return r;
        });

        return !methodDeclarationContextList.isEmpty();
    }

    @Override
    public Void visitClassBody(ClassBodyContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        if (asBoolean(ctx.enumConstants())) {
            ctx.enumConstants().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitEnumConstants(ctx.enumConstants());
        }

        ctx.classBodyDeclaration().forEach(e -> {
            e.putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitClassBodyDeclaration(e);
        });

        return null;
    }

    @Override
    public List<FieldNode> visitEnumConstants(EnumConstantsContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        return ctx.enumConstant().stream()
                .map(e -> {
                    e.putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
                    return this.visitEnumConstant(e);
                })
                .collect(Collectors.toList());
    }

    @Override
    public FieldNode visitEnumConstant(EnumConstantContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        InnerClassNode anonymousInnerClassNode = null;
        if (asBoolean(ctx.anonymousInnerClassDeclaration())) {
            ctx.anonymousInnerClassDeclaration().putNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS, classNode);
            anonymousInnerClassNode = this.visitAnonymousInnerClassDeclaration(ctx.anonymousInnerClassDeclaration());
        }

        FieldNode enumConstant =
                EnumHelper.addEnumConstant(
                        classNode,
                        this.visitIdentifier(ctx.identifier()),
                        createEnumConstantInitExpression(ctx.arguments(), anonymousInnerClassNode));

        this.visitAnnotationsOpt(ctx.annotationsOpt()).forEach(enumConstant::addAnnotation);

        groovydocManager.handle(enumConstant, ctx);

        return this.configureAST(enumConstant, ctx);
    }

    private Expression createEnumConstantInitExpression(ArgumentsContext ctx, InnerClassNode anonymousInnerClassNode) {
        if (!asBoolean(ctx) && !asBoolean(anonymousInnerClassNode)) {
            return null;
        }

        TupleExpression argumentListExpression = (TupleExpression) this.visitArguments(ctx);
        List<Expression> expressions = argumentListExpression.getExpressions();

        if (expressions.size() == 1) {
            Expression expression = expressions.get(0);

            if (expression instanceof NamedArgumentListExpression) { // e.g. SOME_ENUM_CONSTANT(a: "1", b: "2")
                List<MapEntryExpression> mapEntryExpressionList = ((NamedArgumentListExpression) expression).getMapEntryExpressions();
                ListExpression listExpression =
                        new ListExpression(
                                mapEntryExpressionList.stream()
                                        .map(e -> (Expression) e)
                                        .collect(Collectors.toList()));

                if (asBoolean(anonymousInnerClassNode)) {
                    listExpression.addExpression(
                            this.configureAST(
                                    new ClassExpression(anonymousInnerClassNode),
                                    anonymousInnerClassNode));
                }

                if (mapEntryExpressionList.size() > 1) {
                    listExpression.setWrapped(true);
                }

                return this.configureAST(listExpression, ctx);
            }

            if (!asBoolean(anonymousInnerClassNode)) {
                if (expression instanceof ListExpression) {
                    ListExpression listExpression = new ListExpression();
                    listExpression.addExpression(expression);

                    return this.configureAST(listExpression, ctx);
                }

                return expression;
            }

            ListExpression listExpression = new ListExpression();

            if (expression instanceof ListExpression) {
                ((ListExpression) expression).getExpressions().forEach(listExpression::addExpression);
            } else {
                listExpression.addExpression(expression);
            }

            listExpression.addExpression(
                    this.configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));

            return this.configureAST(listExpression, ctx);
        }

        ListExpression listExpression = new ListExpression(expressions);
        if (asBoolean(anonymousInnerClassNode)) {
            listExpression.addExpression(
                    this.configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));
        }

        if (asBoolean(ctx)) {
            listExpression.setWrapped(true);
        }

        return asBoolean(ctx)
                ? this.configureAST(listExpression, ctx)
                : this.configureAST(listExpression, anonymousInnerClassNode);
    }


    @Override
    public Void visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        if (asBoolean(ctx.memberDeclaration())) {
            ctx.memberDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitMemberDeclaration(ctx.memberDeclaration());
        } else if (asBoolean(ctx.block())) {
            Statement statement = this.visitBlock(ctx.block());

            if (asBoolean(ctx.STATIC())) { // e.g. static { }
                classNode.addStaticInitializerStatements(Collections.singletonList(statement), false);
            } else { // e.g.  { }
                classNode.addObjectInitializerStatements(
                        this.configureAST(
                                this.createBlockStatement(statement),
                                statement));
            }
        }

        return null;
    }

    @Override
    public Void visitMemberDeclaration(MemberDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        if (asBoolean(ctx.methodDeclaration())) {
            ctx.methodDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitMethodDeclaration(ctx.methodDeclaration());
        } else if (asBoolean(ctx.fieldDeclaration())) {
            ctx.fieldDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitFieldDeclaration(ctx.fieldDeclaration());
        } else if (asBoolean(ctx.classDeclaration())) {
            ctx.classDeclaration().putNodeMetaData(TYPE_DECLARATION_MODIFIERS, this.visitModifiersOpt(ctx.modifiersOpt()));
            ctx.classDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitClassDeclaration(ctx.classDeclaration());
        }

        return null;
    }

    @Override
    public GenericsType[] visitTypeParameters(TypeParametersContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return ctx.typeParameter().stream()
                .map(this::visitTypeParameter)
                .toArray(GenericsType[]::new);
    }

    @Override
    public GenericsType visitTypeParameter(TypeParameterContext ctx) {
        return this.configureAST(
                new GenericsType(
                        ClassHelper.make(this.visitClassName(ctx.className())),
                        this.visitTypeBound(ctx.typeBound()),
                        null
                ),
                ctx);
    }

    @Override
    public ClassNode[] visitTypeBound(TypeBoundContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return ctx.type().stream()
                .map(this::visitType)
                .toArray(ClassNode[]::new);
    }

    @Override
    public Void visitFieldDeclaration(FieldDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        ctx.variableDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
        this.visitVariableDeclaration(ctx.variableDeclaration());

        return null;
    }

    private ConstructorCallExpression checkThisAndSuperConstructorCall(Statement statement) {
        if (!(statement instanceof BlockStatement)) { // method code must be a BlockStatement
            return null;
        }

        BlockStatement blockStatement = (BlockStatement) statement;
        List<Statement> statementList = blockStatement.getStatements();

        for (int i = 0, n = statementList.size(); i < n; i++) {
            Statement s = statementList.get(i);
            if (s instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) s).getExpression();
                if ((expression instanceof ConstructorCallExpression) && 0 != i) {
                    return (ConstructorCallExpression) expression;
                }
            }
        }

        return null;
    }

    private ModifierManager createModifierManager(MethodDeclarationContext ctx) {
        List<ModifierNode> modifierNodeList = Collections.emptyList();

        if (asBoolean(ctx.modifiers())) {
            modifierNodeList = this.visitModifiers(ctx.modifiers());
        } else if (asBoolean(ctx.modifiersOpt())) {
            modifierNodeList = this.visitModifiersOpt(ctx.modifiersOpt());
        }

        return new ModifierManager(this, modifierNodeList);
    }

    private void validateParametersOfMethodDeclaration(Parameter[] parameters, ClassNode classNode) {
        if (!classNode.isInterface()) {
            return;
        }

        Arrays.stream(parameters).forEach(e -> {
            if (e.hasInitialExpression()) {
                throw createParsingFailedException("Cannot specify default value for method parameter '" + e.getName() + " = " + e.getInitialExpression().getText() + "' inside an interface", e);
            }
        });
    }

    @Override
    public MethodNode visitMethodDeclaration(MethodDeclarationContext ctx) {
        ModifierManager modifierManager = createModifierManager(ctx);
        String methodName = this.visitMethodName(ctx.methodName());
        ClassNode returnType = this.visitReturnType(ctx.returnType());
        Parameter[] parameters = this.visitFormalParameters(ctx.formalParameters());
        ClassNode[] exceptions = this.visitQualifiedClassNameList(ctx.qualifiedClassNameList());

        anonymousInnerClassesDefinedInMethodStack.push(new LinkedList<>());
        Statement code = this.visitMethodBody(ctx.methodBody());
        List<InnerClassNode> anonymousInnerClassList = anonymousInnerClassesDefinedInMethodStack.pop();

        MethodNode methodNode;
        // if classNode is not null, the method declaration is for class declaration
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        if (asBoolean(classNode)) {
            validateParametersOfMethodDeclaration(parameters, classNode);

            methodNode = createConstructorOrMethodNodeForClass(ctx, modifierManager, methodName, returnType, parameters, exceptions, code, classNode);
        } else { // script method declaration
            methodNode = createScriptMethodNode(modifierManager, methodName, returnType, parameters, exceptions, code);
        }
        anonymousInnerClassList.forEach(e -> e.setEnclosingMethod(methodNode));

        methodNode.setGenericsTypes(this.visitTypeParameters(ctx.typeParameters()));
        methodNode.setSyntheticPublic(
                this.isSyntheticPublic(
                        this.isAnnotationDeclaration(classNode),
                        classNode instanceof EnumConstantClassNode,
                        asBoolean(ctx.returnType()),
                        modifierManager));

        if (modifierManager.contains(STATIC)) {
            Arrays.stream(methodNode.getParameters()).forEach(e -> e.setInStaticContext(true));
            methodNode.getVariableScope().setInStaticContext(true);
        }

        this.configureAST(methodNode, ctx);

        validateMethodDeclaration(ctx, methodNode, modifierManager, classNode);

        groovydocManager.handle(methodNode, ctx);

        return methodNode;
    }

    private void validateMethodDeclaration(MethodDeclarationContext ctx, MethodNode methodNode, ModifierManager modifierManager, ClassNode classNode) {
        boolean isAbstractMethod = methodNode.isAbstract();
        boolean hasMethodBody = asBoolean(methodNode.getCode());

        if (9 == ctx.ct) { // script
            if (isAbstractMethod || !hasMethodBody) { // method should not be declared abstract in the script
                throw createParsingFailedException("You can not define a " + (isAbstractMethod ? "abstract" : "") + " method[" + methodNode.getName() + "] " + (!hasMethodBody ? "without method body" : "") + " in the script. Try " + (isAbstractMethod ? "removing the 'abstract'" : "") + (isAbstractMethod && !hasMethodBody ? " and" : "") + (!hasMethodBody ? " adding a method body" : ""), methodNode);
            }
        } else {
            if (!isAbstractMethod && !hasMethodBody) { // non-abstract method without body in the non-script(e.g. class, enum, trait) is not allowed!
                throw createParsingFailedException("You defined a method[" + methodNode.getName() + "] without body. Try adding a method body, or declare it abstract", methodNode);
            }

            boolean isInterfaceOrAbstractClass = asBoolean(classNode) && classNode.isAbstract() && !classNode.isAnnotationDefinition();
            if (isInterfaceOrAbstractClass && !modifierManager.contains(DEFAULT) && isAbstractMethod && hasMethodBody) {
                throw createParsingFailedException("You defined an abstract method[" + methodNode.getName() + "] with body. Try removing the method body" + (classNode.isInterface() ? ", or declare it default" : ""), methodNode);
            }
        }

        modifierManager.validate(methodNode);

        if (methodNode instanceof ConstructorNode) {
            modifierManager.validate((ConstructorNode) methodNode);
        }
    }

    private MethodNode createScriptMethodNode(ModifierManager modifierManager, String methodName, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        MethodNode methodNode;
        methodNode =
                new MethodNode(
                        methodName,
                        modifierManager.contains(PRIVATE) ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PUBLIC,
                        returnType,
                        parameters,
                        exceptions,
                        code);

        modifierManager.processMethodNode(methodNode);
        return methodNode;
    }

    private MethodNode createConstructorOrMethodNodeForClass(MethodDeclarationContext ctx, ModifierManager modifierManager, String methodName, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code, ClassNode classNode) {
        MethodNode methodNode;
        String className = classNode.getNodeMetaData(CLASS_NAME);
        int modifiers = modifierManager.getClassMemberModifiersOpValue();

        if (!asBoolean(ctx.returnType())
                && asBoolean(ctx.methodBody())
                && methodName.equals(className)) { // constructor declaration

            methodNode = createConstructorNodeForClass(methodName, parameters, exceptions, code, classNode, modifiers);
        } else { // class memeber method declaration
            methodNode = createMethodNodeForClass(ctx, modifierManager, methodName, returnType, parameters, exceptions, code, classNode, modifiers);
        }

        modifierManager.attachAnnotations(methodNode);
        return methodNode;
    }

    private MethodNode createMethodNodeForClass(MethodDeclarationContext ctx, ModifierManager modifierManager, String methodName, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code, ClassNode classNode, int modifiers) {
        MethodNode methodNode;
        if (asBoolean(ctx.elementValue())) { // the code of annotation method
            code = this.configureAST(
                    new ExpressionStatement(
                            this.visitElementValue(ctx.elementValue())),
                    ctx.elementValue());

        }

        modifiers |= !modifierManager.contains(STATIC) && (classNode.isInterface() || (isTrue(classNode, IS_INTERFACE_WITH_DEFAULT_METHODS) && !modifierManager.contains(DEFAULT))) ? Opcodes.ACC_ABSTRACT : 0;

        checkWhetherMethodNodeWithSameSignatureExists(classNode, methodName, parameters, ctx);

        methodNode = classNode.addMethod(methodName, modifiers, returnType, parameters, exceptions, code);

        methodNode.setAnnotationDefault(asBoolean(ctx.elementValue()));
        return methodNode;
    }

    private void checkWhetherMethodNodeWithSameSignatureExists(ClassNode classNode, String methodName, Parameter[] parameters, MethodDeclarationContext ctx) {
        MethodNode sameSigMethodNode = classNode.getDeclaredMethod(methodName, parameters);

        if (null == sameSigMethodNode) {
            return;
        }

        throw createParsingFailedException("The method " +  sameSigMethodNode.getText() + " duplicates another method of the same signature", ctx);
    }

    private ConstructorNode createConstructorNodeForClass(String methodName, Parameter[] parameters, ClassNode[] exceptions, Statement code, ClassNode classNode, int modifiers) {
        ConstructorCallExpression thisOrSuperConstructorCallExpression = this.checkThisAndSuperConstructorCall(code);
        if (asBoolean(thisOrSuperConstructorCallExpression)) {
            throw createParsingFailedException(thisOrSuperConstructorCallExpression.getText() + " should be the first statement in the constructor[" + methodName + "]", thisOrSuperConstructorCallExpression);
        }

        return classNode.addConstructor(
                modifiers,
                parameters,
                exceptions,
                code);
    }

    @Override
    public String visitMethodName(MethodNameContext ctx) {
        if (asBoolean(ctx.identifier())) {
            return this.visitIdentifier(ctx.identifier());
        }

        if (asBoolean(ctx.stringLiteral())) {
            return this.visitStringLiteral(ctx.stringLiteral()).getText();
        }

        throw createParsingFailedException("Unsupported method name: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitReturnType(ReturnTypeContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassHelper.OBJECT_TYPE;
        }

        if (asBoolean(ctx.type())) {
            return this.visitType(ctx.type());
        }

        if (asBoolean(ctx.VOID())) {
            return ClassHelper.VOID_TYPE;
        }

        throw createParsingFailedException("Unsupported return type: " + ctx.getText(), ctx);
    }

    @Override
    public Statement visitMethodBody(MethodBodyContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return this.configureAST(this.visitBlock(ctx.block()), ctx);
    }

    @Override
    public DeclarationListStatement visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
        return this.configureAST(this.visitVariableDeclaration(ctx.variableDeclaration()), ctx);
    }

    private ModifierManager createModifierManager(VariableDeclarationContext ctx) {
        List<ModifierNode> modifierNodeList = Collections.emptyList();

        if (asBoolean(ctx.variableModifiers())) {
            modifierNodeList = this.visitVariableModifiers(ctx.variableModifiers());
        } else if (asBoolean(ctx.variableModifiersOpt())) {
            modifierNodeList = this.visitVariableModifiersOpt(ctx.variableModifiersOpt());
        } else if (asBoolean(ctx.modifiers())) {
            modifierNodeList = this.visitModifiers(ctx.modifiers());
        } else if (asBoolean(ctx.modifiersOpt())) {
            modifierNodeList = this.visitModifiersOpt(ctx.modifiersOpt());
        }

        return new ModifierManager(this, modifierNodeList);
    }

    private DeclarationListStatement createMultiAssignmentDeclarationListStatement(VariableDeclarationContext ctx, ModifierManager modifierManager) {
        if (!modifierManager.contains(DEF)) {
            throw createParsingFailedException("keyword def is required to declare tuple, e.g. def (int a, int b) = [1, 2]", ctx);
        }

        return this.configureAST(
                new DeclarationListStatement(
                        this.configureAST(
                                modifierManager.attachAnnotations(
                                        new DeclarationExpression(
                                                new ArgumentListExpression(
                                                        this.visitTypeNamePairs(ctx.typeNamePairs()).stream()
                                                                .peek(e -> modifierManager.processVariableExpression((VariableExpression) e))
                                                                .collect(Collectors.toList())
                                                ),
                                                this.createGroovyTokenByType(ctx.ASSIGN().getSymbol(), Types.ASSIGN),
                                                this.visitVariableInitializer(ctx.variableInitializer())
                                        )
                                ),
                                ctx
                        )
                ),
                ctx
        );
    }

    @Override
    public DeclarationListStatement visitVariableDeclaration(VariableDeclarationContext ctx) {
        ModifierManager modifierManager = this.createModifierManager(ctx);

        if (asBoolean(ctx.typeNamePairs())) { // e.g. def (int a, int b) = [1, 2]
            return this.createMultiAssignmentDeclarationListStatement(ctx, modifierManager);
        }

        ClassNode variableType = this.visitType(ctx.type());
        ctx.variableDeclarators().putNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE, variableType);
        List<DeclarationExpression> declarationExpressionList = this.visitVariableDeclarators(ctx.variableDeclarators());

        // if classNode is not null, the variable declaration is for class declaration. In other words, it is a field declaration
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);

        if (asBoolean(classNode)) {
            return createFieldDeclarationListStatement(ctx, modifierManager, variableType, declarationExpressionList, classNode);
        }

        declarationExpressionList.forEach(e -> {
            VariableExpression variableExpression = (VariableExpression) e.getLeftExpression();

            modifierManager.processVariableExpression(variableExpression);
            modifierManager.attachAnnotations(e);
        });

        int size = declarationExpressionList.size();
        if (size > 0) {
            DeclarationExpression declarationExpression = declarationExpressionList.get(0);

            if (1 == size) {
                this.configureAST(declarationExpression, ctx);
            } else {
                // Tweak start of first declaration
                declarationExpression.setLineNumber(ctx.getStart().getLine());
                declarationExpression.setColumnNumber(ctx.getStart().getCharPositionInLine() + 1);
            }
        }

        return this.configureAST(new DeclarationListStatement(declarationExpressionList), ctx);
    }

    private DeclarationListStatement createFieldDeclarationListStatement(VariableDeclarationContext ctx, ModifierManager modifierManager, ClassNode variableType, List<DeclarationExpression> declarationExpressionList, ClassNode classNode) {
        declarationExpressionList.forEach(e -> {
            VariableExpression variableExpression = (VariableExpression) e.getLeftExpression();

            int modifiers = modifierManager.getClassMemberModifiersOpValue();

            Expression initialValue = EmptyExpression.INSTANCE.equals(e.getRightExpression()) ? null : e.getRightExpression();
            Object defaultValue = findDefaultValueByType(variableType);

            if (classNode.isInterface()) {
                if (!asBoolean(initialValue)) {
                    initialValue = !asBoolean(defaultValue) ? null : new ConstantExpression(defaultValue);
                }

                modifiers |= Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            }

            if (classNode.isInterface() || modifierManager.containsVisibilityModifier()) {
                FieldNode fieldNode =
                        classNode.addField(
                                variableExpression.getName(),
                                modifiers,
                                variableType,
                                initialValue);
                modifierManager.attachAnnotations(fieldNode);

                groovydocManager.handle(fieldNode, ctx);

                this.configureAST(fieldNode, ctx);
            } else {
                PropertyNode propertyNode =
                        classNode.addProperty(
                                variableExpression.getName(),
                                modifiers | Opcodes.ACC_PUBLIC,
                                variableType,
                                initialValue,
                                null,
                                null);

                FieldNode fieldNode = propertyNode.getField();
                fieldNode.setModifiers(modifiers & ~Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE);
                fieldNode.setSynthetic(!classNode.isInterface());
                modifierManager.attachAnnotations(fieldNode);

                groovydocManager.handle(fieldNode, ctx);
                groovydocManager.handle(propertyNode, ctx);

                this.configureAST(fieldNode, ctx);
                this.configureAST(propertyNode, ctx);
            }

        });

        return null;
    }

    @Override
    public List<Expression> visitTypeNamePairs(TypeNamePairsContext ctx) {
        return ctx.typeNamePair().stream().map(this::visitTypeNamePair).collect(Collectors.toList());
    }

    @Override
    public VariableExpression visitTypeNamePair(TypeNamePairContext ctx) {
        return this.configureAST(
                new VariableExpression(
                        this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName(),
                        this.visitType(ctx.type())),
                ctx);
    }

    @Override
    public List<DeclarationExpression> visitVariableDeclarators(VariableDeclaratorsContext ctx) {
        ClassNode variableType = ctx.getNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE);
        Objects.requireNonNull(variableType, "variableType should not be null");

        return ctx.variableDeclarator().stream()
                .map(e -> {
                    e.putNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE, variableType);
                    return this.visitVariableDeclarator(e);
//                    return this.configureAST(this.visitVariableDeclarator(e), ctx);
                })
                .collect(Collectors.toList());
    }

    @Override
    public DeclarationExpression visitVariableDeclarator(VariableDeclaratorContext ctx) {
        ClassNode variableType = ctx.getNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE);
        Objects.requireNonNull(variableType, "variableType should not be null");

        org.codehaus.groovy.syntax.Token token;
        if (asBoolean(ctx.ASSIGN())) {
            token = createGroovyTokenByType(ctx.ASSIGN().getSymbol(), Types.ASSIGN);
        } else {
            token = new org.codehaus.groovy.syntax.Token(Types.ASSIGN, ASSIGN_STR, ctx.start.getLine(), 1);
        }

        return this.configureAST(
                new DeclarationExpression(
                        this.configureAST(
                                new VariableExpression(
                                        this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName(),
                                        variableType
                                ),
                                ctx.variableDeclaratorId()),
                        token,
                        this.visitVariableInitializer(ctx.variableInitializer())),
                ctx);
    }

    @Override
    public Expression visitVariableInitializer(VariableInitializerContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        if (asBoolean(ctx.statementExpression())) {
            return this.configureAST(
                    ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression(),
                    ctx);
        }

        if (asBoolean(ctx.standardLambda())) {
            return this.configureAST(this.visitStandardLambda(ctx.standardLambda()), ctx);
        }

        throw createParsingFailedException("Unsupported variable initializer: " + ctx.getText(), ctx);
    }

    @Override
    public List<Expression> visitVariableInitializers(VariableInitializersContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.variableInitializer().stream()
                        .map(this::visitVariableInitializer)
                        .collect(Collectors.toList());
    }

    @Override
    public List<Expression> visitArrayInitializer(ArrayInitializerContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return this.visitVariableInitializers(ctx.variableInitializers());
    }

    @Override
    public Statement visitBlock(BlockContext ctx) {
        if (!asBoolean(ctx)) {
            return this.createBlockStatement();
        }

        return this.configureAST(
                this.visitBlockStatementsOpt(ctx.blockStatementsOpt()),
                ctx);
    }


    @Override
    public ExpressionStatement visitNormalExprAlt(NormalExprAltContext ctx) {
        return this.configureAST(new ExpressionStatement((Expression) this.visit(ctx.expression())), ctx);
    }

    @Override
    public ExpressionStatement visitCommandExprAlt(CommandExprAltContext ctx) {
        return this.configureAST(new ExpressionStatement(this.visitCommandExpression(ctx.commandExpression())), ctx);
    }

    @Override
    public Expression visitCommandExpression(CommandExpressionContext ctx) {
        Expression baseExpr = this.visitPathExpression(ctx.pathExpression());
        Expression arguments = this.visitEnhancedArgumentList(ctx.enhancedArgumentList());

        MethodCallExpression methodCallExpression;
        if (baseExpr instanceof PropertyExpression) { // e.g. obj.a 1, 2
            methodCallExpression =
                    this.configureAST(
                            this.createMethodCallExpression(
                                    (PropertyExpression) baseExpr, arguments),
                            arguments);

        } else if (baseExpr instanceof MethodCallExpression && !isTrue(baseExpr, IS_INSIDE_PARENTHESES)) { // e.g. m {} a, b  OR  m(...) a, b
            if (asBoolean(arguments)) {
                // The error should never be thrown.
                throw new GroovyBugError("When baseExpr is a instance of MethodCallExpression, which should follow NO argumentList");
            }

            methodCallExpression = (MethodCallExpression) baseExpr;
        } else if (
                !isTrue(baseExpr, IS_INSIDE_PARENTHESES)
                        && (baseExpr instanceof VariableExpression /* e.g. m 1, 2 */
                        || baseExpr instanceof GStringExpression /* e.g. "$m" 1, 2 */
                        || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING)) /* e.g. "m" 1, 2 */)
                ) {
            methodCallExpression =
                    this.configureAST(
                            this.createMethodCallExpression(baseExpr, arguments),
                            arguments);
        } else { // e.g. a[x] b, new A() b, etc.
            methodCallExpression =
                    this.configureAST(
                            new MethodCallExpression(
                                    baseExpr,
                                    CALL_STR,
                                    arguments
                            ),
                            arguments
                    );

            methodCallExpression.setImplicitThis(false);
        }

        if (!asBoolean(ctx.commandArgument())) {
            return this.configureAST(methodCallExpression, ctx);
        }

        return this.configureAST(
                (Expression) ctx.commandArgument().stream()
                        .map(e -> (Object) e)
                        .reduce(methodCallExpression,
                                (r, e) -> {
                                    CommandArgumentContext commandArgumentContext = (CommandArgumentContext) e;
                                    commandArgumentContext.putNodeMetaData(CMD_EXPRESSION_BASE_EXPR, r);

                                    return this.visitCommandArgument(commandArgumentContext);
                                }
                        ),
                ctx);
    }

    @Override
    public Expression visitCommandArgument(CommandArgumentContext ctx) {
        // e.g. x y a b     we call "x y" as the base expression
        Expression baseExpr = ctx.getNodeMetaData(CMD_EXPRESSION_BASE_EXPR);

        Expression primaryExpr = (Expression) this.visit(ctx.primary());

        if (asBoolean(ctx.enhancedArgumentList())) { // e.g. x y a b
            if (baseExpr instanceof PropertyExpression) { // the branch should never reach, because a.b.c will be parsed as a path expression, not a method call
                throw createParsingFailedException("Unsupported command argument: " + ctx.getText(), ctx);
            }

            // the following code will process "a b" of "x y a b"
            MethodCallExpression methodCallExpression =
                    new MethodCallExpression(
                            baseExpr,
                            this.createConstantExpression(primaryExpr),
                            this.visitEnhancedArgumentList(ctx.enhancedArgumentList())
                    );
            methodCallExpression.setImplicitThis(false);

            return this.configureAST(methodCallExpression, ctx);
        } else if (asBoolean(ctx.pathElement())) { // e.g. x y a.b
            Expression pathExpression =
                    this.createPathExpression(
                            this.configureAST(
                                    new PropertyExpression(baseExpr, this.createConstantExpression(primaryExpr)),
                                    primaryExpr
                            ),
                            ctx.pathElement()
                    );

            return this.configureAST(pathExpression, ctx);
        }

        // e.g. x y a
        return this.configureAST(
                new PropertyExpression(
                        baseExpr,
                        primaryExpr instanceof VariableExpression
                                ? this.createConstantExpression(primaryExpr)
                                : primaryExpr
                ),
                primaryExpr
        );
    }


    // expression {    --------------------------------------------------------------------

    @Override
    public ClassNode visitCastParExpression(CastParExpressionContext ctx) {
        return this.visitType(ctx.type());
    }

    @Override
    public Expression visitParExpression(ParExpressionContext ctx) {
        Expression expression;

        if (asBoolean(ctx.statementExpression())) {
            expression = ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression();
        } else if (asBoolean(ctx.standardLambda())) {
            expression = this.visitStandardLambda(ctx.standardLambda());
        } else {
            throw createParsingFailedException("Unsupported parentheses expression: " + ctx.getText(), ctx);
        }

        expression.putNodeMetaData(IS_INSIDE_PARENTHESES, true);

        Integer insideParenLevel = expression.getNodeMetaData(INSIDE_PARENTHESES_LEVEL);
        if (asBoolean((Object) insideParenLevel)) {
            insideParenLevel++;
        } else {
            insideParenLevel = 1;
        }
        expression.putNodeMetaData(INSIDE_PARENTHESES_LEVEL, insideParenLevel);

        return this.configureAST(expression, ctx);
    }

    @Override
    public Expression visitPathExpression(PathExpressionContext ctx) {
        return this.configureAST(
                this.createPathExpression((Expression) this.visit(ctx.primary()), ctx.pathElement()),
                ctx);
    }

    @Override
    public Expression visitPathElement(PathElementContext ctx) {
        Expression baseExpr = ctx.getNodeMetaData(PATH_EXPRESSION_BASE_EXPR);
        Objects.requireNonNull(baseExpr, "baseExpr is required!");

        if (asBoolean(ctx.namePart())) {
            Expression namePartExpr = this.visitNamePart(ctx.namePart());
            GenericsType[] genericsTypes = this.visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());


            if (asBoolean(ctx.DOT())) {
                if (asBoolean(ctx.AT())) { // e.g. obj.@a
                    return this.configureAST(new AttributeExpression(baseExpr, namePartExpr), ctx);
                } else { // e.g. obj.p
                    PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr);
                    propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);

                    return this.configureAST(propertyExpression, ctx);
                }
            } else if (asBoolean(ctx.SAFE_DOT())) {
                if (asBoolean(ctx.AT())) { // e.g. obj?.@a
                    return this.configureAST(new AttributeExpression(baseExpr, namePartExpr, true), ctx);
                } else { // e.g. obj?.p
                    PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, true);
                    propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);

                    return this.configureAST(propertyExpression, ctx);
                }
            } else if (asBoolean(ctx.METHOD_POINTER())) { // e.g. obj.&m
                return this.configureAST(new MethodPointerExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.METHOD_REFERENCE())) { // e.g. obj::m
                return this.configureAST(new MethodReferenceExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.SPREAD_DOT())) {
                if (asBoolean(ctx.AT())) { // e.g. obj*.@a
                    AttributeExpression attributeExpression = new AttributeExpression(baseExpr, namePartExpr, true);

                    attributeExpression.setSpreadSafe(true);

                    return this.configureAST(attributeExpression, ctx);
                } else { // e.g. obj*.p
                    PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, true);
                    propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);

                    propertyExpression.setSpreadSafe(true);

                    return this.configureAST(propertyExpression, ctx);
                }
            }
        }

        if (asBoolean(ctx.indexPropertyArgs())) { // e.g. list[1, 3, 5]
            Pair<Token, Expression> pair = this.visitIndexPropertyArgs(ctx.indexPropertyArgs());

            return this.configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(pair.getKey()), pair.getValue(), asBoolean(ctx.indexPropertyArgs().QUESTION())),
                    ctx);
        }

        if (asBoolean(ctx.namedPropertyArgs())) { // this is a special way to new instance, e.g. Person(name: 'Daniel.Sun', location: 'Shanghai')
            List<MapEntryExpression> mapEntryExpressionList =
                    this.visitNamedPropertyArgs(ctx.namedPropertyArgs());

            Expression right;
            if (mapEntryExpressionList.size() == 1) {
                MapEntryExpression mapEntryExpression = mapEntryExpressionList.get(0);

                if (mapEntryExpression.getKeyExpression() instanceof SpreadMapExpression) {
                    right = mapEntryExpression.getKeyExpression();
                } else {
                    right = mapEntryExpression;
                }
            } else {
                ListExpression listExpression =
                        this.configureAST(
                                new ListExpression(
                                        mapEntryExpressionList.stream()
                                                .map(
                                                        e -> {
                                                            if (e.getKeyExpression() instanceof SpreadMapExpression) {
                                                                return e.getKeyExpression();
                                                            }

                                                            return e;
                                                        }
                                                )
                                                .collect(Collectors.toList())),
                                ctx.namedPropertyArgs()
                        );
                listExpression.setWrapped(true);
                right = listExpression;
            }

            return this.configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(ctx.namedPropertyArgs().LBRACK().getSymbol()), right),
                    ctx);
        }

        if (asBoolean(ctx.arguments())) {
            Expression argumentsExpr = this.visitArguments(ctx.arguments());
            this.configureAST(argumentsExpr, ctx);

            if (isTrue(baseExpr, IS_INSIDE_PARENTHESES)) { // e.g. (obj.x)(), (obj.@x)()
                MethodCallExpression methodCallExpression =
                        new MethodCallExpression(
                                baseExpr,
                                CALL_STR,
                                argumentsExpr
                        );

                methodCallExpression.setImplicitThis(false);

                return this.configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof AttributeExpression) { // e.g. obj.@a(1, 2)
                AttributeExpression attributeExpression = (AttributeExpression) baseExpr;
                attributeExpression.setSpreadSafe(false); // whether attributeExpression is spread safe or not, we must reset it as false

                MethodCallExpression methodCallExpression =
                        new MethodCallExpression(
                                attributeExpression,
                                CALL_STR,
                                argumentsExpr
                        );

                return this.configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.a(1, 2)
                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression((PropertyExpression) baseExpr, argumentsExpr);

                return this.configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof VariableExpression) { // void and primitive type AST node must be an instance of VariableExpression
                String baseExprText = baseExpr.getText();
                if (VOID_STR.equals(baseExprText)) { // e.g. void()
                    MethodCallExpression methodCallExpression =
                            new MethodCallExpression(
                                    this.createConstantExpression(baseExpr),
                                    CALL_STR,
                                    argumentsExpr
                            );

                    methodCallExpression.setImplicitThis(false);

                    return this.configureAST(methodCallExpression, ctx);
                } else if (PRIMITIVE_TYPE_SET.contains(baseExprText)) { // e.g. int(), long(), float(), etc.
                    throw createParsingFailedException("Primitive type literal: " + baseExprText + " cannot be used as a method name", ctx);
                }
            }

            if (baseExpr instanceof VariableExpression
                    || baseExpr instanceof GStringExpression
                    || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING))) { // e.g. m(), "$m"(), "m"()

                String baseExprText = baseExpr.getText();
                if (SUPER_STR.equals(baseExprText) || THIS_STR.equals(baseExprText)) { // e.g. this(...), super(...)
                    // class declaration is not allowed in the closure,
                    // so if this and super is inside the closure, it will not be constructor call.
                    // e.g. src/test/org/codehaus/groovy/transform/MapConstructorTransformTest.groovy:
                    // @MapConstructor(pre={ super(args?.first, args?.last); args = args ?: [:] }, post = { first = first?.toUpperCase() })
                    if (ctx.isInsideClosure) {
                        return this.configureAST(
                                new MethodCallExpression(
                                        baseExpr,
                                        baseExprText,
                                        argumentsExpr
                                ),
                                ctx);
                    }

                    return this.configureAST(
                            new ConstructorCallExpression(
                                    SUPER_STR.equals(baseExprText)
                                            ? ClassNode.SUPER
                                            : ClassNode.THIS,
                                    argumentsExpr
                            ),
                            ctx);
                }

                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression(baseExpr, argumentsExpr);

                return this.configureAST(methodCallExpression, ctx);
            }

            // e.g. 1(), 1.1(), ((int) 1 / 2)(1, 2), {a, b -> a + b }(1, 2), m()()
            MethodCallExpression methodCallExpression =
                    new MethodCallExpression(baseExpr, CALL_STR, argumentsExpr);
            methodCallExpression.setImplicitThis(false);

            return this.configureAST(methodCallExpression, ctx);
        }

        if (asBoolean(ctx.closure())) {
            ClosureExpression closureExpression = this.visitClosure(ctx.closure());

            if (baseExpr instanceof MethodCallExpression) {
                MethodCallExpression methodCallExpression = (MethodCallExpression) baseExpr;
                Expression argumentsExpression = methodCallExpression.getArguments();

                if (argumentsExpression instanceof ArgumentListExpression) { // normal arguments, e.g. 1, 2
                    ArgumentListExpression argumentListExpression = (ArgumentListExpression) argumentsExpression;
                    argumentListExpression.getExpressions().add(closureExpression);

                    return this.configureAST(methodCallExpression, ctx);
                }

                if (argumentsExpression instanceof TupleExpression) { // named arguments, e.g. x: 1, y: 2
                    TupleExpression tupleExpression = (TupleExpression) argumentsExpression;
                    NamedArgumentListExpression namedArgumentListExpression = (NamedArgumentListExpression) tupleExpression.getExpression(0);

                    if (asBoolean(tupleExpression.getExpressions())) {
                        methodCallExpression.setArguments(
                                this.configureAST(
                                        new ArgumentListExpression(
                                                Stream.of(
                                                        this.configureAST(
                                                                new MapExpression(namedArgumentListExpression.getMapEntryExpressions()),
                                                                namedArgumentListExpression
                                                        ),
                                                        closureExpression
                                                ).collect(Collectors.toList())
                                        ),
                                        tupleExpression
                                )
                        );
                    } else {
                        // the branch should never reach, because named arguments must not be empty
                        methodCallExpression.setArguments(
                                this.configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        tupleExpression));
                    }


                    return this.configureAST(methodCallExpression, ctx);
                }

            }

            // e.g. 1 {}, 1.1 {}
            if (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_NUMERIC)) {
                MethodCallExpression methodCallExpression =
                        new MethodCallExpression(
                                baseExpr,
                                CALL_STR,
                                this.configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        closureExpression
                                )
                        );
                methodCallExpression.setImplicitThis(false);

                return this.configureAST(methodCallExpression, ctx);
            }


            if (baseExpr instanceof PropertyExpression) { // e.g. obj.m {  }
                PropertyExpression propertyExpression = (PropertyExpression) baseExpr;

                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression(
                                propertyExpression,
                                this.configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        closureExpression
                                )
                        );

                return this.configureAST(methodCallExpression, ctx);
            }

            // e.g.  m { return 1; }
            MethodCallExpression methodCallExpression =
                    new MethodCallExpression(
                            VariableExpression.THIS_EXPRESSION,

                            (baseExpr instanceof VariableExpression)
                                    ? this.createConstantExpression((VariableExpression) baseExpr)
                                    : baseExpr,

                            this.configureAST(
                                    new ArgumentListExpression(closureExpression),
                                    closureExpression)
                    );


            return this.configureAST(methodCallExpression, ctx);
        }

        throw createParsingFailedException("Unsupported path element: " + ctx.getText(), ctx);
    }


    @Override
    public GenericsType[] visitNonWildcardTypeArguments(NonWildcardTypeArgumentsContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return Arrays.stream(this.visitTypeList(ctx.typeList()))
                .map(this::createGenericsType)
                .toArray(GenericsType[]::new);
    }

    @Override
    public ClassNode[] visitTypeList(TypeListContext ctx) {
        if (!asBoolean(ctx)) {
            return new ClassNode[0];
        }

        return ctx.type().stream()
                .map(this::visitType)
                .toArray(ClassNode[]::new);
    }

    @Override
    public Expression visitArguments(ArgumentsContext ctx) {
        if (!asBoolean(ctx) || !asBoolean(ctx.enhancedArgumentList())) {
            return new ArgumentListExpression();
        }

        return this.configureAST(this.visitEnhancedArgumentList(ctx.enhancedArgumentList()), ctx);
    }

    @Override
    public Expression visitEnhancedArgumentList(EnhancedArgumentListContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        List<Expression> expressionList = new LinkedList<>();
        List<MapEntryExpression> mapEntryExpressionList = new LinkedList<>();

        ctx.enhancedArgumentListElement().stream()
                .map(this::visitEnhancedArgumentListElement)
                .forEach(e -> {

                    if (e instanceof MapEntryExpression) {
                        mapEntryExpressionList.add((MapEntryExpression) e);
                    } else {
                        expressionList.add(e);
                    }
                });

        if (!asBoolean(mapEntryExpressionList)) { // e.g. arguments like  1, 2 OR  someArg, e -> e
            return this.configureAST(
                    new ArgumentListExpression(expressionList),
                    ctx);
        }

        if (!asBoolean(expressionList)) { // e.g. arguments like  x: 1, y: 2
            return this.configureAST(
                    new TupleExpression(
                            this.configureAST(
                                    new NamedArgumentListExpression(mapEntryExpressionList),
                                    ctx)),
                    ctx);
        }

        if (asBoolean(mapEntryExpressionList) && asBoolean(expressionList)) { // e.g. arguments like x: 1, 'a', y: 2, 'b', z: 3
            ArgumentListExpression argumentListExpression = new ArgumentListExpression(expressionList);
            argumentListExpression.getExpressions().add(0, new MapExpression(mapEntryExpressionList)); // TODO: confirm BUG OR NOT? All map entries will be put at first, which is not friendly to Groovy developers

            return this.configureAST(argumentListExpression, ctx);
        }

        throw createParsingFailedException("Unsupported argument list: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitEnhancedArgumentListElement(EnhancedArgumentListElementContext ctx) {
        if (asBoolean(ctx.expressionListElement())) {
            return this.configureAST(this.visitExpressionListElement(ctx.expressionListElement()), ctx);
        }

        if (asBoolean(ctx.standardLambda())) {
            return this.configureAST(this.visitStandardLambda(ctx.standardLambda()), ctx);
        }

        if (asBoolean(ctx.mapEntry())) {
            return this.configureAST(this.visitMapEntry(ctx.mapEntry()), ctx);
        }

        throw createParsingFailedException("Unsupported enhanced argument list element: " + ctx.getText(), ctx);
    }


    @Override
    public ConstantExpression visitStringLiteral(StringLiteralContext ctx) {
        String text = ctx.StringLiteral().getText();

        int slashyType = text.startsWith("/") ? StringUtils.SLASHY :
                text.startsWith("$/") ? StringUtils.DOLLAR_SLASHY : StringUtils.NONE_SLASHY;

        if (text.startsWith("'''") || text.startsWith("\"\"\"")) {
            text = StringUtils.removeCR(text); // remove CR in the multiline string

            text = text.length() == 6 ? "" : text.substring(3, text.length() - 3);
        } else if (text.startsWith("'") || text.startsWith("/") || text.startsWith("\"")) {
            if (text.startsWith("/")) { // the slashy string can span rows, so we have to remove CR for it
                text = StringUtils.removeCR(text); // remove CR in the multiline string
            }

            text = text.length() == 2 ? "" : text.substring(1, text.length() - 1);
        } else if (text.startsWith("$/")) {
            text = StringUtils.removeCR(text);

            text = text.length() == 4 ? "" : text.substring(2, text.length() - 2);
        }

        //handle escapes.
        text = StringUtils.replaceEscapes(text, slashyType);

        ConstantExpression constantExpression = new ConstantExpression(text, true);
        constantExpression.putNodeMetaData(IS_STRING, true);

        return this.configureAST(constantExpression, ctx);
    }


    @Override
    public Pair<Token, Expression> visitIndexPropertyArgs(IndexPropertyArgsContext ctx) {
        List<Expression> expressionList = this.visitExpressionList(ctx.expressionList());


        if (expressionList.size() == 1) {
            Expression expr = expressionList.get(0);

            Expression indexExpr;
            if (expr instanceof SpreadExpression) { // e.g. a[*[1, 2]]
                ListExpression listExpression = new ListExpression(expressionList);
                listExpression.setWrapped(false);

                indexExpr = listExpression;
            } else { // e.g. a[1]
                indexExpr = expr;
            }

            return new Pair<>(ctx.LBRACK().getSymbol(), indexExpr);
        }

        // e.g. a[1, 2]
        ListExpression listExpression = new ListExpression(expressionList);
        listExpression.setWrapped(true);

        return new Pair<>(ctx.LBRACK().getSymbol(), this.configureAST(listExpression, ctx));
    }

    @Override
    public List<MapEntryExpression> visitNamedPropertyArgs(NamedPropertyArgsContext ctx) {
        return this.visitMapEntryList(ctx.mapEntryList());
    }

    @Override
    public Expression visitNamePart(NamePartContext ctx) {
        if (asBoolean(ctx.identifier())) {
            return this.configureAST(new ConstantExpression(this.visitIdentifier(ctx.identifier())), ctx);
        } else if (asBoolean(ctx.stringLiteral())) {
            return this.configureAST(this.visitStringLiteral(ctx.stringLiteral()), ctx);
        } else if (asBoolean(ctx.dynamicMemberName())) {
            return this.configureAST(this.visitDynamicMemberName(ctx.dynamicMemberName()), ctx);
        } else if (asBoolean(ctx.keywords())) {
            return this.configureAST(new ConstantExpression(ctx.keywords().getText()), ctx);
        }

        throw createParsingFailedException("Unsupported name part: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitDynamicMemberName(DynamicMemberNameContext ctx) {
        if (asBoolean(ctx.parExpression())) {
            return this.configureAST(this.visitParExpression(ctx.parExpression()), ctx);
        } else if (asBoolean(ctx.gstring())) {
            return this.configureAST(this.visitGstring(ctx.gstring()), ctx);
        }

        throw createParsingFailedException("Unsupported dynamic member name: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitPostfixExpression(PostfixExpressionContext ctx) {
        Expression pathExpr = this.visitPathExpression(ctx.pathExpression());

        if (asBoolean(ctx.op)) {
            PostfixExpression postfixExpression = new PostfixExpression(pathExpr, createGroovyToken(ctx.op));

            if (ctx.isInsideAssert) {
                // powerassert requires different column for values, so we have to copy the location of op
                return this.configureAST(postfixExpression, ctx.op);
            } else {
                return this.configureAST(postfixExpression, ctx);
            }
        }

        return this.configureAST(pathExpr, ctx);
    }

    @Override
    public Expression visitPostfixExprAlt(PostfixExprAltContext ctx) {
        return this.visitPostfixExpression(ctx.postfixExpression());
    }

    @Override
    public Expression visitUnaryNotExprAlt(UnaryNotExprAltContext ctx) {
        if (asBoolean(ctx.NOT())) {
            return this.configureAST(
                    new NotExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        if (asBoolean(ctx.BITNOT())) {
            return this.configureAST(
                    new BitwiseNegationExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        throw createParsingFailedException("Unsupported unary expression: " + ctx.getText(), ctx);
    }

    @Override
    public CastExpression visitCastExprAlt(CastExprAltContext ctx) {
        return this.configureAST(
                new CastExpression(
                        this.visitCastParExpression(ctx.castParExpression()),
                        (Expression) this.visit(ctx.expression())
                ),
                ctx
        );
    }

    @Override
    public BinaryExpression visitPowerExprAlt(PowerExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public Expression visitUnaryAddExprAlt(UnaryAddExprAltContext ctx) {
        ExpressionContext expressionCtx = ctx.expression();
        Expression expression = (Expression) this.visit(expressionCtx);

        Boolean insidePar = isTrue(expression, IS_INSIDE_PARENTHESES);

        switch (ctx.op.getType()) {
            case ADD: {
                if (expression instanceof ConstantExpression && !insidePar) {
                    return this.configureAST(expression, ctx);
                }

                return this.configureAST(new UnaryPlusExpression(expression), ctx);
            }
            case SUB: {
                if (expression instanceof ConstantExpression && !insidePar) {
                    ConstantExpression constantExpression = (ConstantExpression) expression;

                    String integerLiteralText = constantExpression.getNodeMetaData(INTEGER_LITERAL_TEXT);
                    if (asBoolean((Object) integerLiteralText)) {
                        return this.configureAST(new ConstantExpression(Numbers.parseInteger(null, SUB_STR + integerLiteralText)), ctx);
                    }

                    String floatingPointLiteralText = constantExpression.getNodeMetaData(FLOATING_POINT_LITERAL_TEXT);
                    if (asBoolean((Object) floatingPointLiteralText)) {
                        return this.configureAST(new ConstantExpression(Numbers.parseDecimal(SUB_STR + floatingPointLiteralText)), ctx);
                    }

                    throw new GroovyBugError("Failed to find the original number literal text: " + constantExpression.getText());
                }

                return this.configureAST(new UnaryMinusExpression(expression), ctx);
            }

            case INC:
            case DEC:
                return this.configureAST(new PrefixExpression(this.createGroovyToken(ctx.op), expression), ctx);

            default:
                throw createParsingFailedException("Unsupported unary operation: " + ctx.getText(), ctx);
        }
    }

    @Override
    public BinaryExpression visitMultiplicativeExprAlt(MultiplicativeExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitAdditiveExprAlt(AdditiveExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public Expression visitShiftExprAlt(ShiftExprAltContext ctx) {
        Expression left = (Expression) this.visit(ctx.left);
        Expression right = (Expression) this.visit(ctx.right);

        if (asBoolean(ctx.rangeOp)) {
            return this.configureAST(new RangeExpression(left, right, !ctx.rangeOp.getText().endsWith("<")), ctx);
        }

        org.codehaus.groovy.syntax.Token op = null;

        if (asBoolean(ctx.dlOp)) {
            op = this.createGroovyToken(ctx.dlOp, 2);
        } else if (asBoolean(ctx.dgOp)) {
            op = this.createGroovyToken(ctx.dgOp, 2);
        } else if (asBoolean(ctx.tgOp)) {
            op = this.createGroovyToken(ctx.tgOp, 3);
        } else {
            throw createParsingFailedException("Unsupported shift expression: " + ctx.getText(), ctx);
        }

        return this.configureAST(
                new BinaryExpression(left, op, right),
                ctx);
    }

    @Override
    public Expression visitRelationalExprAlt(RelationalExprAltContext ctx) {
        switch (ctx.op.getType()) {
            case AS:
                return this.configureAST(
                        CastExpression.asExpression(this.visitType(ctx.type()), (Expression) this.visit(ctx.left)),
                        ctx);

            case INSTANCEOF:
            case NOT_INSTANCEOF:
                ctx.type().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, true);
                return this.configureAST(
                        new BinaryExpression((Expression) this.visit(ctx.left),
                                this.createGroovyToken(ctx.op),
                                this.configureAST(new ClassExpression(this.visitType(ctx.type())), ctx.type())),
                        ctx);

            case LE:
            case GE:
            case GT:
            case LT:
            case IN:
            case NOT_IN:
                return this.configureAST(
                        this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                        ctx);

            default:
                throw createParsingFailedException("Unsupported relational expression: " + ctx.getText(), ctx);
        }
    }

    @Override
    public Expression visitEqualityExprAlt(EqualityExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitRegexExprAlt(RegexExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitAndExprAlt(AndExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitExclusiveOrExprAlt(ExclusiveOrExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitInclusiveOrExprAlt(InclusiveOrExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitLogicalAndExprAlt(LogicalAndExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitLogicalOrExprAlt(LogicalOrExprAltContext ctx) {
        return this.configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public Expression visitConditionalExprAlt(ConditionalExprAltContext ctx) {
        if (asBoolean(ctx.ELVIS())) { // e.g. a == 6 ?: 0
            return this.configureAST(
                    new ElvisOperatorExpression((Expression) this.visit(ctx.con), (Expression) this.visit(ctx.fb)),
                    ctx);
        }

        return this.configureAST(
                new TernaryExpression(
                        this.configureAST(new BooleanExpression((Expression) this.visit(ctx.con)),
                                ctx.con),
                        (Expression) this.visit(ctx.tb),
                        (Expression) this.visit(ctx.fb)),
                ctx);
    }

    @Override
    public BinaryExpression visitMultipleAssignmentExprAlt(MultipleAssignmentExprAltContext ctx) {
        return this.configureAST(
                new BinaryExpression(
                        this.visitVariableNames(ctx.left),
                        this.createGroovyToken(ctx.op),
                        ((ExpressionStatement) this.visit(ctx.right)).getExpression()),
                ctx);
    }

    @Override
    public BinaryExpression visitAssignmentExprAlt(AssignmentExprAltContext ctx) {
        Expression leftExpr = (Expression) this.visit(ctx.left);

        if (leftExpr instanceof VariableExpression
                && isTrue(leftExpr, IS_INSIDE_PARENTHESES)) { // it is a special multiple assignment whose variable count is only one, e.g. (a) = [1]

            if ((Integer) leftExpr.getNodeMetaData(INSIDE_PARENTHESES_LEVEL) > 1) {
                throw createParsingFailedException("Nested parenthesis is not allowed in multiple assignment, e.g. ((a)) = b", ctx);
            }

            return this.configureAST(
                    new BinaryExpression(
                            this.configureAST(new TupleExpression(leftExpr), ctx.left),
                            this.createGroovyToken(ctx.op),
                            asBoolean(ctx.statementExpression())
                                    ? ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression()
                                    : this.visitStandardLambda(ctx.standardLambda())),
                    ctx);
        }

        // the LHS expression should be a variable which is not inside any parentheses
        if (
                !(
                        (leftExpr instanceof VariableExpression
//                                && !(THIS_STR.equals(leftExpr.getText()) || SUPER_STR.equals(leftExpr.getText()))     // commented, e.g. this = value // this will be transformed to $this
                                && !isTrue(leftExpr, IS_INSIDE_PARENTHESES)) // e.g. p = 123

                                || leftExpr instanceof PropertyExpression // e.g. obj.p = 123

                                || (leftExpr instanceof BinaryExpression
//                                && !(((BinaryExpression) leftExpr).getRightExpression() instanceof ListExpression)    // commented, e.g. list[1, 2] = [11, 12]
                                && Types.LEFT_SQUARE_BRACKET == ((BinaryExpression) leftExpr).getOperation().getType()) // e.g. map[a] = 123 OR map['a'] = 123 OR map["$a"] = 123
                )

                ) {

            throw createParsingFailedException("The LHS of an assignment should be a variable or a field accessing expression", ctx);
        }

        return this.configureAST(
                new BinaryExpression(
                        leftExpr,
                        this.createGroovyToken(ctx.op),
                        asBoolean(ctx.statementExpression())
                                ? ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression()
                                : this.visitStandardLambda(ctx.standardLambda())),
                ctx);
    }

// } expression    --------------------------------------------------------------------


    // primary {       --------------------------------------------------------------------
    @Override
    public VariableExpression visitIdentifierPrmrAlt(IdentifierPrmrAltContext ctx) {
        return this.configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public ConstantExpression visitLiteralPrmrAlt(LiteralPrmrAltContext ctx) {
        return this.configureAST((ConstantExpression) this.visit(ctx.literal()), ctx);
    }

    @Override
    public GStringExpression visitGstringPrmrAlt(GstringPrmrAltContext ctx) {
        return this.configureAST((GStringExpression) this.visit(ctx.gstring()), ctx);
    }

    @Override
    public Expression visitNewPrmrAlt(NewPrmrAltContext ctx) {
        return this.configureAST(this.visitCreator(ctx.creator()), ctx);
    }

    @Override
    public VariableExpression visitThisPrmrAlt(ThisPrmrAltContext ctx) {
        return this.configureAST(new VariableExpression(ctx.THIS().getText()), ctx);
    }

    @Override
    public VariableExpression visitSuperPrmrAlt(SuperPrmrAltContext ctx) {
        return this.configureAST(new VariableExpression(ctx.SUPER().getText()), ctx);
    }


    @Override
    public Expression visitParenPrmrAlt(ParenPrmrAltContext ctx) {
        return this.configureAST(this.visitParExpression(ctx.parExpression()), ctx);
    }

    @Override
    public ClosureExpression visitClosurePrmrAlt(ClosurePrmrAltContext ctx) {
        return this.configureAST(this.visitClosure(ctx.closure()), ctx);
    }

    @Override
    public ClosureExpression visitLambdaPrmrAlt(LambdaPrmrAltContext ctx) {
        return this.configureAST(this.visitStandardLambda(ctx.standardLambda()), ctx);
    }

    @Override
    public ListExpression visitListPrmrAlt(ListPrmrAltContext ctx) {
        return this.configureAST(
                this.visitList(ctx.list()),
                ctx);
    }

    @Override
    public MapExpression visitMapPrmrAlt(MapPrmrAltContext ctx) {
        return this.configureAST(this.visitMap(ctx.map()), ctx);
    }

    @Override
    public VariableExpression visitTypePrmrAlt(TypePrmrAltContext ctx) {
        return this.configureAST(
                this.visitBuiltInType(ctx.builtInType()),
                ctx);
    }


// } primary       --------------------------------------------------------------------

    @Override
    public Expression visitCreator(CreatorContext ctx) {
        ClassNode classNode = this.visitCreatedName(ctx.createdName());
        Expression arguments = this.visitArguments(ctx.arguments());

        if (asBoolean(ctx.arguments())) { // create instance of class
            if (asBoolean(ctx.anonymousInnerClassDeclaration())) {
                ctx.anonymousInnerClassDeclaration().putNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS, classNode);
                InnerClassNode anonymousInnerClassNode = this.visitAnonymousInnerClassDeclaration(ctx.anonymousInnerClassDeclaration());

                List<InnerClassNode> anonymousInnerClassList = anonymousInnerClassesDefinedInMethodStack.peek();
                if (asBoolean((Object) anonymousInnerClassList)) { // if the anonymous class is created in a script, no anonymousInnerClassList is available.
                    anonymousInnerClassList.add(anonymousInnerClassNode);
                }

                ConstructorCallExpression constructorCallExpression = new ConstructorCallExpression(anonymousInnerClassNode, arguments);
                constructorCallExpression.setUsingAnonymousInnerClass(true);

                return this.configureAST(constructorCallExpression, ctx);
            }

            return this.configureAST(
                    new ConstructorCallExpression(classNode, arguments),
                    ctx);
        }

        if (asBoolean(ctx.LBRACK())) { // create array
            if (asBoolean(ctx.arrayInitializer())) {
                ClassNode arrayType = classNode;
                for (int i = 0, n = ctx.b.size() - 1; i < n; i++) {
                    arrayType = arrayType.makeArray();
                }

                return this.configureAST(
                        new ArrayExpression(
                                arrayType,
                                this.visitArrayInitializer(ctx.arrayInitializer())),
                        ctx);
            } else {
                Expression[] empties;
                if (asBoolean(ctx.b)) {
                    empties = new Expression[ctx.b.size()];
                    Arrays.setAll(empties, i -> ConstantExpression.EMPTY_EXPRESSION);
                } else {
                    empties = new Expression[0];
                }

                return this.configureAST(
                        new ArrayExpression(
                                classNode,
                                null,
                                Stream.concat(
                                        ctx.expression().stream()
                                                .map(e -> (Expression) this.visit(e)),
                                        Arrays.stream(empties)
                                ).collect(Collectors.toList())),
                        ctx);
            }
        }

        throw createParsingFailedException("Unsupported creator: " + ctx.getText(), ctx);
    }


    private String genAnonymousClassName(String outerClassName) {
        return outerClassName + "$" + this.anonymousInnerClassCounter++;
    }

    @Override
    public InnerClassNode visitAnonymousInnerClassDeclaration(AnonymousInnerClassDeclarationContext ctx) {
        ClassNode superClass = ctx.getNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS);
        Objects.requireNonNull(superClass, "superClass should not be null");

        InnerClassNode anonymousInnerClass;

        ClassNode outerClass = this.classNodeStack.peek();
        outerClass = asBoolean(outerClass) ? outerClass : moduleNode.getScriptClassDummy();

        String fullName = this.genAnonymousClassName(outerClass.getName());
        if (1 == ctx.t) { // anonymous enum
            anonymousInnerClass = new EnumConstantClassNode(outerClass, fullName, superClass.getModifiers() | Opcodes.ACC_FINAL, superClass.getPlainNodeReference());

            // and remove the final modifier from classNode to allow the sub class
            superClass.setModifiers(superClass.getModifiers() & ~Opcodes.ACC_FINAL);
        } else { // anonymous inner class
            anonymousInnerClass = new InnerClassNode(outerClass, fullName, Opcodes.ACC_PUBLIC, superClass);
        }

        anonymousInnerClass.setUsingGenerics(false);
        anonymousInnerClass.setAnonymous(true);
        this.configureAST(anonymousInnerClass, ctx);

        classNodeStack.push(anonymousInnerClass);
        ctx.classBody().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, anonymousInnerClass);
        this.visitClassBody(ctx.classBody());
        classNodeStack.pop();

        classNodeList.add(anonymousInnerClass);

        return anonymousInnerClass;
    }


    @Override
    public ClassNode visitCreatedName(CreatedNameContext ctx) {
        if (asBoolean(ctx.qualifiedClassName())) {
            ClassNode classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());

            if (asBoolean(ctx.typeArgumentsOrDiamond())) {
                classNode.setGenericsTypes(
                        this.visitTypeArgumentsOrDiamond(ctx.typeArgumentsOrDiamond()));
            }

            return this.configureAST(classNode, ctx);
        }

        if (asBoolean(ctx.primitiveType())) {
            return this.configureAST(
                    this.visitPrimitiveType(ctx.primitiveType()),
                    ctx);
        }

        throw createParsingFailedException("Unsupported created name: " + ctx.getText(), ctx);
    }


    @Override
    public MapExpression visitMap(MapContext ctx) {
        return this.configureAST(
                new MapExpression(this.visitMapEntryList(ctx.mapEntryList())),
                ctx);
    }

    @Override
    public List<MapEntryExpression> visitMapEntryList(MapEntryListContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return this.createMapEntryList(ctx.mapEntry());
    }

    private List<MapEntryExpression> createMapEntryList(List<? extends MapEntryContext> mapEntryContextList) {
        if (!asBoolean(mapEntryContextList)) {
            return Collections.emptyList();
        }

        return mapEntryContextList.stream()
                .map(this::visitMapEntry)
                .collect(Collectors.toList());
    }

    @Override
    public MapEntryExpression visitMapEntry(MapEntryContext ctx) {
        Expression keyExpr;
        Expression valueExpr = (Expression) this.visit(ctx.expression());

        if (asBoolean(ctx.MUL())) {
            keyExpr = this.configureAST(new SpreadMapExpression(valueExpr), ctx);
        } else if (asBoolean(ctx.mapEntryLabel())) {
            keyExpr = this.visitMapEntryLabel(ctx.mapEntryLabel());
        } else {
            throw createParsingFailedException("Unsupported map entry: " + ctx.getText(), ctx);
        }

        return this.configureAST(
                new MapEntryExpression(keyExpr, valueExpr),
                ctx);
    }

    @Override
    public Expression visitMapEntryLabel(MapEntryLabelContext ctx) {
        if (asBoolean(ctx.keywords())) {
            return this.configureAST(this.visitKeywords(ctx.keywords()), ctx);
        } else if (asBoolean(ctx.primary())) {
            Expression expression = (Expression) this.visit(ctx.primary());

            // if the key is variable and not inside parentheses, convert it to a constant, e.g. [a:1, b:2]
            if (expression instanceof VariableExpression && !isTrue(expression, IS_INSIDE_PARENTHESES)) {
                expression =
                        this.configureAST(
                                new ConstantExpression(((VariableExpression) expression).getName()),
                                expression);
            }

            return this.configureAST(expression, ctx);
        }

        throw createParsingFailedException("Unsupported map entry label: " + ctx.getText(), ctx);
    }

    @Override
    public ConstantExpression visitKeywords(KeywordsContext ctx) {
        return this.configureAST(new ConstantExpression(ctx.getText()), ctx);
    }

    /*
    @Override
    public VariableExpression visitIdentifier(IdentifierContext ctx) {
        return this.configureAST(new VariableExpression(ctx.getText()), ctx);
    }
    */

    @Override
    public VariableExpression visitBuiltInType(BuiltInTypeContext ctx) {
        String text;
        if (asBoolean(ctx.VOID())) {
            text = ctx.VOID().getText();
        } else if (asBoolean(ctx.BuiltInPrimitiveType())) {
            text = ctx.BuiltInPrimitiveType().getText();
        } else {
            throw createParsingFailedException("Unsupported built-in type: " + ctx, ctx);
        }

        return this.configureAST(new VariableExpression(text), ctx);
    }


    @Override
    public ListExpression visitList(ListContext ctx) {
        return this.configureAST(
                new ListExpression(
                        this.visitExpressionList(ctx.expressionList())),
                ctx);
    }

    @Override
    public List<Expression> visitExpressionList(ExpressionListContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return this.createExpressionList(ctx.expressionListElement());
    }

    private List<Expression> createExpressionList(List<? extends ExpressionListElementContext> expressionListElementContextList) {
        if (!asBoolean(expressionListElementContextList)) {
            return Collections.emptyList();
        }

        return expressionListElementContextList.stream()
                .map(this::visitExpressionListElement)
                .collect(Collectors.toList());
    }

    @Override
    public Expression visitExpressionListElement(ExpressionListElementContext ctx) {
        Expression expression = (Expression) this.visit(ctx.expression());

        if (asBoolean(ctx.MUL())) {
            return this.configureAST(new SpreadExpression(expression), ctx);
        }

        return this.configureAST(expression, ctx);
    }


    // literal {       --------------------------------------------------------------------
    @Override
    public ConstantExpression visitIntegerLiteralAlt(IntegerLiteralAltContext ctx) {
        String text = ctx.IntegerLiteral().getText();

        ConstantExpression constantExpression = new ConstantExpression(Numbers.parseInteger(null, text), !text.startsWith(SUB_STR));
        constantExpression.putNodeMetaData(IS_NUMERIC, true);
        constantExpression.putNodeMetaData(INTEGER_LITERAL_TEXT, text);

        return this.configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitFloatingPointLiteralAlt(FloatingPointLiteralAltContext ctx) {
        String text = ctx.FloatingPointLiteral().getText();

        ConstantExpression constantExpression = new ConstantExpression(Numbers.parseDecimal(text), !text.startsWith(SUB_STR));
        constantExpression.putNodeMetaData(IS_NUMERIC, true);
        constantExpression.putNodeMetaData(FLOATING_POINT_LITERAL_TEXT, text);

        return this.configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitStringLiteralAlt(StringLiteralAltContext ctx) {
        return this.configureAST(
                this.visitStringLiteral(ctx.stringLiteral()),
                ctx);
    }

    @Override
    public ConstantExpression visitBooleanLiteralAlt(BooleanLiteralAltContext ctx) {
        return this.configureAST(new ConstantExpression("true".equals(ctx.BooleanLiteral().getText()), true), ctx);
    }

    @Override
    public ConstantExpression visitNullLiteralAlt(NullLiteralAltContext ctx) {
        return this.configureAST(new ConstantExpression(null), ctx);
    }


// } literal       --------------------------------------------------------------------


    // gstring {       --------------------------------------------------------------------
    @Override
    public GStringExpression visitGstring(GstringContext ctx) {
        List<ConstantExpression> strings = new LinkedList<>();

        String begin = ctx.GStringBegin().getText();
        final int slashyType = begin.startsWith("/")
                ? StringUtils.SLASHY
                : begin.startsWith("$/") ? StringUtils.DOLLAR_SLASHY : StringUtils.NONE_SLASHY;

        {
            String it = begin;
            if (it.startsWith("\"\"\"")) {
                it = StringUtils.removeCR(it);
                it = it.substring(2); // translate leading """ to "
            } else if (it.startsWith("$/")) {
                it = StringUtils.removeCR(it);
                it = "\"" + it.substring(2); // translate leading $/ to "
            } else if (it.startsWith("/")) {
                it = StringUtils.removeCR(it);
            }

            it = StringUtils.replaceEscapes(it, slashyType);
            it = (it.length() == 2)
                    ? ""
                    : StringGroovyMethods.getAt(it, new IntRange(true, 1, -2));

            strings.add(this.configureAST(new ConstantExpression(it), ctx.GStringBegin()));
        }

        List<ConstantExpression> partStrings =
                ctx.GStringPart().stream()
                        .map(e -> {
                            String it = e.getText();

                            it = StringUtils.removeCR(it);
                            it = StringUtils.replaceEscapes(it, slashyType);
                            it = it.length() == 1 ? "" : StringGroovyMethods.getAt(it, new IntRange(true, 0, -2));

                            return this.configureAST(new ConstantExpression(it), e);
                        }).collect(Collectors.toList());
        strings.addAll(partStrings);

        {
            String it = ctx.GStringEnd().getText();
            if (it.endsWith("\"\"\"")) {
                it = StringUtils.removeCR(it);
                it = StringGroovyMethods.getAt(it, new IntRange(true, 0, -3)); // translate tailing """ to "
            } else if (it.endsWith("/$")) {
                it = StringUtils.removeCR(it);
                it = StringGroovyMethods.getAt(it, new IntRange(false, 0, -2)) + "\""; // translate tailing /$ to "
            } else if (it.endsWith("/")) {
                it = StringUtils.removeCR(it);
            }

            it = StringUtils.replaceEscapes(it, slashyType);
            it = (it.length() == 1)
                    ? ""
                    : StringGroovyMethods.getAt(it, new IntRange(true, 0, -2));

            strings.add(this.configureAST(new ConstantExpression(it), ctx.GStringEnd()));
        }

        List<Expression> values = ctx.gstringValue().stream()
                .map(e -> {
                    Expression expression = this.visitGstringValue(e);

                    if (expression instanceof ClosureExpression && !asBoolean(e.closure().ARROW())) {
                        List<Statement> statementList = ((BlockStatement) ((ClosureExpression) expression).getCode()).getStatements();

                        if (statementList.stream().allMatch(x -> !asBoolean(x))) {
                            return this.configureAST(new ConstantExpression(null), e);
                        }

                        return this.configureAST(new MethodCallExpression(expression, CALL_STR, new ArgumentListExpression()), e);
                    }

                    return expression;
                })
                .collect(Collectors.toList());

        StringBuilder verbatimText = new StringBuilder(ctx.getText().length());
        for (int i = 0, n = strings.size(), s = values.size(); i < n; i++) {
            verbatimText.append(strings.get(i).getValue());

            if (i == s) {
                continue;
            }

            Expression value = values.get(i);
            if (!asBoolean(value)) {
                continue;
            }

            verbatimText.append(DOLLAR_STR);
            verbatimText.append(value.getText());
        }

        return this.configureAST(new GStringExpression(verbatimText.toString(), strings, values), ctx);
    }

    @Override
    public Expression visitGstringValue(GstringValueContext ctx) {
        if (asBoolean(ctx.gstringPath())) {
            return this.configureAST(this.visitGstringPath(ctx.gstringPath()), ctx);
        }

        if (asBoolean(ctx.LBRACE())) {
            if (asBoolean(ctx.statementExpression())) {
                return this.configureAST(((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression(), ctx.statementExpression());
            } else { // e.g. "${}"
                return this.configureAST(new ConstantExpression(null), ctx);
            }
        }

        if (asBoolean(ctx.closure())) {
            return this.configureAST(this.visitClosure(ctx.closure()), ctx);
        }

        throw createParsingFailedException("Unsupported gstring value: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitGstringPath(GstringPathContext ctx) {
        VariableExpression variableExpression = new VariableExpression(this.visitIdentifier(ctx.identifier()));

        if (asBoolean(ctx.GStringPathPart())) {
            Expression propertyExpression = ctx.GStringPathPart().stream()
                    .map(e -> this.configureAST((Expression) new ConstantExpression(e.getText().substring(1)), e))
                    .reduce(this.configureAST(variableExpression, ctx.identifier()), (r, e) -> this.configureAST(new PropertyExpression(r, e), e));

            return this.configureAST(propertyExpression, ctx);
        }

        return this.configureAST(variableExpression, ctx);
    }
// } gstring       --------------------------------------------------------------------

    @Override
    public LambdaExpression visitStandardLambda(StandardLambdaContext ctx) {
        return this.configureAST(this.createLambda(ctx.standardLambdaParameters(), ctx.lambdaBody()), ctx);
    }

    private LambdaExpression createLambda(StandardLambdaParametersContext standardLambdaParametersContext, LambdaBodyContext lambdaBodyContext) {
        return new LambdaExpression(
                this.visitStandardLambdaParameters(standardLambdaParametersContext),
                this.visitLambdaBody(lambdaBodyContext));
    }

    @Override
    public Parameter[] visitStandardLambdaParameters(StandardLambdaParametersContext ctx) {
        if (asBoolean(ctx.variableDeclaratorId())) {
            return new Parameter[]{
                    this.configureAST(
                            new Parameter(
                                    ClassHelper.OBJECT_TYPE,
                                    this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName()
                            ),
                            ctx.variableDeclaratorId()
                    )
            };
        }

        Parameter[] parameters = this.visitFormalParameters(ctx.formalParameters());

        if (0 == parameters.length) {
            return null;
        }

        return parameters;
    }

    @Override
    public Statement visitLambdaBody(LambdaBodyContext ctx) {
        if (asBoolean(ctx.statementExpression())) {
            return this.configureAST((ExpressionStatement) this.visit(ctx.statementExpression()), ctx);
        }

        if (asBoolean(ctx.block())) {
            return this.configureAST(this.visitBlock(ctx.block()), ctx);
        }

        throw createParsingFailedException("Unsupported lambda body: " + ctx.getText(), ctx);
    }

    @Override
    public ClosureExpression visitClosure(ClosureContext ctx) {
        Parameter[] parameters = asBoolean(ctx.formalParameterList())
                ? this.visitFormalParameterList(ctx.formalParameterList())
                : null;

        if (!asBoolean(ctx.ARROW())) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        Statement code = this.visitBlockStatementsOpt(ctx.blockStatementsOpt());

        return this.configureAST(new ClosureExpression(parameters, code), ctx);
    }

    @Override
    public Parameter[] visitFormalParameters(FormalParametersContext ctx) {
        if (!asBoolean(ctx)) {
            return new Parameter[0];
        }

        return this.visitFormalParameterList(ctx.formalParameterList());
    }

    @Override
    public Parameter[] visitFormalParameterList(FormalParameterListContext ctx) {
        if (!asBoolean(ctx)) {
            return new Parameter[0];
        }

        List<Parameter> parameterList = new LinkedList<>();

        if (asBoolean(ctx.formalParameter())) {
            parameterList.addAll(
                    ctx.formalParameter().stream()
                            .map(this::visitFormalParameter)
                            .collect(Collectors.toList()));
        }

        if (asBoolean(ctx.lastFormalParameter())) {
            parameterList.add(this.visitLastFormalParameter(ctx.lastFormalParameter()));
        }

        return parameterList.toArray(new Parameter[0]);
    }

    @Override
    public Parameter visitFormalParameter(FormalParameterContext ctx) {
        return this.processFormalParameter(ctx, ctx.variableModifiersOpt(), ctx.type(), null, ctx.variableDeclaratorId(), ctx.expression());
    }

    @Override
    public Parameter visitLastFormalParameter(LastFormalParameterContext ctx) {
        return this.processFormalParameter(ctx, ctx.variableModifiersOpt(), ctx.type(), ctx.ELLIPSIS(), ctx.variableDeclaratorId(), ctx.expression());
    }

    @Override
    public List<ModifierNode> visitClassOrInterfaceModifiersOpt(ClassOrInterfaceModifiersOptContext ctx) {
        if (asBoolean(ctx.classOrInterfaceModifiers())) {
            return this.visitClassOrInterfaceModifiers(ctx.classOrInterfaceModifiers());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ModifierNode> visitClassOrInterfaceModifiers(ClassOrInterfaceModifiersContext ctx) {
        return ctx.classOrInterfaceModifier().stream()
                .map(this::visitClassOrInterfaceModifier)
                .collect(Collectors.toList());
    }


    @Override
    public ModifierNode visitClassOrInterfaceModifier(ClassOrInterfaceModifierContext ctx) {
        if (asBoolean(ctx.annotation())) {
            return this.configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return this.configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported class or interface modifier: " + ctx.getText(), ctx);
    }

    @Override
    public ModifierNode visitModifier(ModifierContext ctx) {
        if (asBoolean(ctx.classOrInterfaceModifier())) {
            return this.configureAST(this.visitClassOrInterfaceModifier(ctx.classOrInterfaceModifier()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return this.configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported modifier: " + ctx.getText(), ctx);
    }

    @Override
    public List<ModifierNode> visitModifiers(ModifiersContext ctx) {
        return ctx.modifier().stream()
                .map(this::visitModifier)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModifierNode> visitModifiersOpt(ModifiersOptContext ctx) {
        if (asBoolean(ctx.modifiers())) {
            return this.visitModifiers(ctx.modifiers());
        }

        return Collections.emptyList();
    }


    @Override
    public ModifierNode visitVariableModifier(VariableModifierContext ctx) {
        if (asBoolean(ctx.annotation())) {
            return this.configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return this.configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported variable modifier", ctx);
    }

    @Override
    public List<ModifierNode> visitVariableModifiersOpt(VariableModifiersOptContext ctx) {
        if (asBoolean(ctx.variableModifiers())) {
            return this.visitVariableModifiers(ctx.variableModifiers());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ModifierNode> visitVariableModifiers(VariableModifiersContext ctx) {
        return ctx.variableModifier().stream()
                .map(this::visitVariableModifier)
                .collect(Collectors.toList());
    }


    // type {       --------------------------------------------------------------------
    @Override
    public ClassNode visitType(TypeContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassHelper.OBJECT_TYPE;
        }

        ClassNode classNode = null;

        if (asBoolean(ctx.classOrInterfaceType())) {
            ctx.classOrInterfaceType().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, ctx.getNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR));
            classNode = this.visitClassOrInterfaceType(ctx.classOrInterfaceType());
        }

        if (asBoolean(ctx.primitiveType())) {
            classNode = this.visitPrimitiveType(ctx.primitiveType());
        }

        if (asBoolean(ctx.LBRACK())) {
            // clear array's generics type info. Groovy's bug? array's generics type will be ignored. e.g. List<String>[]... p
            classNode.setGenericsTypes(null);
            classNode.setUsingGenerics(false);

            for (int i = 0, n = ctx.LBRACK().size(); i < n; i++) {
                classNode = this.configureAST(classNode.makeArray(), classNode);
            }
        }

        if (!asBoolean(classNode)) {
            throw createParsingFailedException("Unsupported type: " + ctx.getText(), ctx);
        }

        return this.configureAST(classNode, ctx);
    }

    @Override
    public ClassNode visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
        ClassNode classNode;
        if (asBoolean(ctx.qualifiedClassName())) {
            ctx.qualifiedClassName().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, ctx.getNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR));
            classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());
        } else {
            ctx.qualifiedStandardClassName().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, ctx.getNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR));
            classNode = this.visitQualifiedStandardClassName(ctx.qualifiedStandardClassName());
        }

        if (asBoolean(ctx.typeArguments())) {
            classNode.setGenericsTypes(
                    this.visitTypeArguments(ctx.typeArguments()));
        }

        return this.configureAST(classNode, ctx);
    }

    @Override
    public GenericsType[] visitTypeArgumentsOrDiamond(TypeArgumentsOrDiamondContext ctx) {
        if (asBoolean(ctx.typeArguments())) {
            return this.visitTypeArguments(ctx.typeArguments());
        }

        if (asBoolean(ctx.LT())) { // e.g. <>
            return new GenericsType[0];
        }

        throw createParsingFailedException("Unsupported type arguments or diamond: " + ctx.getText(), ctx);
    }


    @Override
    public GenericsType[] visitTypeArguments(TypeArgumentsContext ctx) {
        return ctx.typeArgument().stream().map(this::visitTypeArgument).toArray(GenericsType[]::new);
    }

    @Override
    public GenericsType visitTypeArgument(TypeArgumentContext ctx) {
        if (asBoolean(ctx.QUESTION())) {
            ClassNode baseType = this.configureAST(ClassHelper.makeWithoutCaching(QUESTION_STR), ctx.QUESTION());

            if (!asBoolean(ctx.type())) {
                GenericsType genericsType = new GenericsType(baseType);
                genericsType.setWildcard(true);
                genericsType.setName(QUESTION_STR);

                return this.configureAST(genericsType, ctx);
            }

            ClassNode[] upperBounds = null;
            ClassNode lowerBound = null;

            ClassNode classNode = this.visitType(ctx.type());
            if (asBoolean(ctx.EXTENDS())) {
                upperBounds = new ClassNode[]{classNode};
            } else if (asBoolean(ctx.SUPER())) {
                lowerBound = classNode;
            }

            GenericsType genericsType = new GenericsType(baseType, upperBounds, lowerBound);
            genericsType.setWildcard(true);
            genericsType.setName(QUESTION_STR);

            return this.configureAST(genericsType, ctx);
        } else if (asBoolean(ctx.type())) {
            return this.configureAST(
                    this.createGenericsType(
                            this.visitType(ctx.type())),
                    ctx);
        }

        throw createParsingFailedException("Unsupported type argument: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitPrimitiveType(PrimitiveTypeContext ctx) {
        return this.configureAST(ClassHelper.make(ctx.getText()), ctx);
    }
// } type       --------------------------------------------------------------------

    @Override
    public VariableExpression visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        return this.configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public TupleExpression visitVariableNames(VariableNamesContext ctx) {
        return this.configureAST(
                new TupleExpression(
                        ctx.variableDeclaratorId().stream()
                                .map(this::visitVariableDeclaratorId)
                                .collect(Collectors.toList())
                ),
                ctx);
    }

    @Override
    public BlockStatement visitBlockStatementsOpt(BlockStatementsOptContext ctx) {
        if (asBoolean(ctx.blockStatements())) {
            return this.configureAST(this.visitBlockStatements(ctx.blockStatements()), ctx);
        }

        return this.configureAST(this.createBlockStatement(), ctx);
    }

    @Override
    public BlockStatement visitBlockStatements(BlockStatementsContext ctx) {
        return this.configureAST(
                this.createBlockStatement(
                        ctx.blockStatement().stream()
                                .map(this::visitBlockStatement)
                                .filter(e -> asBoolean(e))
                                .collect(Collectors.toList())),
                ctx);
    }

    @Override
    public Statement visitBlockStatement(BlockStatementContext ctx) {
        if (asBoolean(ctx.localVariableDeclaration())) {
            return this.configureAST(this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()), ctx);
        }

        if (asBoolean(ctx.statement())) {
            Object astNode = this.visit(ctx.statement()); //this.configureAST((Statement) this.visit(ctx.statement()), ctx);

            if (astNode instanceof MethodNode) {
                throw createParsingFailedException("Method definition not expected here", ctx);
            } else {
                return (Statement) astNode;
            }
        }

        throw createParsingFailedException("Unsupported block statement: " + ctx.getText(), ctx);
    }

    @Override
    public List<AnnotationNode> visitAnnotationsOpt(AnnotationsOptContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.annotation().stream()
                .map(this::visitAnnotation)
                .collect(Collectors.toList());
    }

    @Override
    public AnnotationNode visitAnnotation(AnnotationContext ctx) {
        String annotationName = this.visitAnnotationName(ctx.annotationName());
        AnnotationNode annotationNode = new AnnotationNode(ClassHelper.make(annotationName));
        List<Pair<String, Expression>> annotationElementValues = this.visitElementValues(ctx.elementValues());

        annotationElementValues.forEach(e -> annotationNode.addMember(e.getKey(), e.getValue()));

        return this.configureAST(annotationNode, ctx);
    }

    @Override
    public List<Pair<String, Expression>> visitElementValues(ElementValuesContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        List<Pair<String, Expression>> annotationElementValues = new LinkedList<>();

        if (asBoolean(ctx.elementValuePairs())) {
            this.visitElementValuePairs(ctx.elementValuePairs()).entrySet().forEach(e -> {
                annotationElementValues.add(new Pair<>(e.getKey(), e.getValue()));
            });
        } else if (asBoolean(ctx.elementValue())) {
            annotationElementValues.add(new Pair<>(VALUE_STR, this.visitElementValue(ctx.elementValue())));
        }

        return annotationElementValues;
    }


    @Override
    public String visitAnnotationName(AnnotationNameContext ctx) {
        return this.visitQualifiedClassName(ctx.qualifiedClassName()).getName();
    }

    @Override
    public Map<String, Expression> visitElementValuePairs(ElementValuePairsContext ctx) {
        return ctx.elementValuePair().stream()
                .map(this::visitElementValuePair)
                .collect(Collectors.toMap(
                        Pair::getKey,
                        Pair::getValue,
                        (k, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", k));
                        },
                        LinkedHashMap::new
                ));
    }

    @Override
    public Pair<String, Expression> visitElementValuePair(ElementValuePairContext ctx) {
        return new Pair<>(ctx.elementValuePairName().getText(), this.visitElementValue(ctx.elementValue()));
    }

    @Override
    public Expression visitElementValue(ElementValueContext ctx) {
        if (asBoolean(ctx.expression())) {
            return this.configureAST((Expression) this.visit(ctx.expression()), ctx);
        }

        if (asBoolean(ctx.annotation())) {
            return this.configureAST(new AnnotationConstantExpression(this.visitAnnotation(ctx.annotation())), ctx);
        }

        if (asBoolean(ctx.elementValueArrayInitializer())) {
            return this.configureAST(this.visitElementValueArrayInitializer(ctx.elementValueArrayInitializer()), ctx);
        }

        throw createParsingFailedException("Unsupported element value: " + ctx.getText(), ctx);
    }

    @Override
    public ListExpression visitElementValueArrayInitializer(ElementValueArrayInitializerContext ctx) {
        return this.configureAST(new ListExpression(ctx.elementValue().stream().map(this::visitElementValue).collect(Collectors.toList())), ctx);
    }

    @Override
    public String visitClassName(ClassNameContext ctx) {
        String text = ctx.getText();

        if (!text.contains("\\")) {
            return text;
        }

        return StringUtils.replaceHexEscapes(text);
    }

    @Override
    public String visitIdentifier(IdentifierContext ctx) {
        String text = ctx.getText();

        if (!text.contains("\\")) {
            return text;
        }

        return StringUtils.replaceHexEscapes(text);
    }


    @Override
    public String visitQualifiedName(QualifiedNameContext ctx) {
        return ctx.qualifiedNameElement().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining(DOT_STR));
    }

    @Override
    public ClassNode[] visitQualifiedClassNameList(QualifiedClassNameListContext ctx) {
        if (!asBoolean(ctx)) {
            return new ClassNode[0];
        }

        return ctx.qualifiedClassName().stream()
                .map(this::visitQualifiedClassName)
                .toArray(ClassNode[]::new);
    }

    @Override
    public ClassNode visitQualifiedClassName(QualifiedClassNameContext ctx) {
        return this.createClassNode(ctx);
    }

    @Override
    public ClassNode visitQualifiedStandardClassName(QualifiedStandardClassNameContext ctx) {
        return this.createClassNode(ctx);
    }

    private ClassNode createClassNode(GroovyParserRuleContext ctx) {
        ClassNode result = ClassHelper.make(ctx.getText());

        if (!isTrue(ctx, IS_INSIDE_INSTANCEOF_EXPR)) { // type in the "instanceof" expression should not have proxy to redirect to it
            result = this.proxyClassNode(result);
        }

        return this.configureAST(result, ctx);
    }

    private ClassNode proxyClassNode(ClassNode classNode) {
        if (!classNode.isUsingGenerics()) {
            return classNode;
        }

        ClassNode cn = ClassHelper.makeWithoutCaching(classNode.getName());
        cn.setRedirect(classNode);

        return cn;
    }

    /**
     * Visit tree safely, no NPE occurred when the tree is null.
     *
     * @param tree an AST node
     * @return the visiting result
     */
    @Override
    public Object visit(ParseTree tree) {
        if (!asBoolean(tree)) {
            return null;
        }

        return super.visit(tree);
    }


    // e.g. obj.a(1, 2) or obj.a 1, 2
    private MethodCallExpression createMethodCallExpression(PropertyExpression propertyExpression, Expression arguments) {
        MethodCallExpression methodCallExpression =
                new MethodCallExpression(
                        propertyExpression.getObjectExpression(),
                        propertyExpression.getProperty(),
                        arguments
                );

        methodCallExpression.setImplicitThis(false);
        methodCallExpression.setSafe(propertyExpression.isSafe());
        methodCallExpression.setSpreadSafe(propertyExpression.isSpreadSafe());

        // method call obj*.m(): "safe"(false) and "spreadSafe"(true)
        // property access obj*.p: "safe"(true) and "spreadSafe"(true)
        // so we have to reset safe here.
        if (propertyExpression.isSpreadSafe()) {
            methodCallExpression.setSafe(false);
        }

        // if the generics types meta data is not empty, it is a generic method call, e.g. obj.<Integer>a(1, 2)
        methodCallExpression.setGenericsTypes(
                propertyExpression.getNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES));

        return methodCallExpression;
    }

    // e.g. m(1, 2) or m 1, 2
    private MethodCallExpression createMethodCallExpression(Expression baseExpr, Expression arguments) {
        MethodCallExpression methodCallExpression =
                new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,

                        (baseExpr instanceof VariableExpression)
                                ? this.createConstantExpression((VariableExpression) baseExpr)
                                : baseExpr,

                        arguments
                );

        return methodCallExpression;
    }

    private Parameter processFormalParameter(GroovyParserRuleContext ctx,
                                             VariableModifiersOptContext variableModifiersOptContext,
                                             TypeContext typeContext,
                                             TerminalNode ellipsis,
                                             VariableDeclaratorIdContext variableDeclaratorIdContext,
                                             ExpressionContext expressionContext) {

        ClassNode classNode = this.visitType(typeContext);

        if (asBoolean(ellipsis)) {
            classNode = this.configureAST(classNode.makeArray(), classNode);
        }

        Parameter parameter =
                new ModifierManager(this, this.visitVariableModifiersOpt(variableModifiersOptContext))
                        .processParameter(
                                this.configureAST(
                                        new Parameter(classNode, this.visitVariableDeclaratorId(variableDeclaratorIdContext).getName()),
                                        ctx)
                        );

        if (asBoolean(expressionContext)) {
            parameter.setInitialExpression((Expression) this.visit(expressionContext));
        }

        return parameter;
    }

    private Expression createPathExpression(Expression primaryExpr, List<? extends PathElementContext> pathElementContextList) {
        return (Expression) pathElementContextList.stream()
                .map(e -> (Object) e)
                .reduce(primaryExpr,
                        (r, e) -> {
                            PathElementContext pathElementContext = (PathElementContext) e;

                            pathElementContext.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR, r);

                            return this.visitPathElement(pathElementContext);
                        }
                );
    }

    private GenericsType createGenericsType(ClassNode classNode) {
        return this.configureAST(new GenericsType(classNode), classNode);
    }

    private ConstantExpression createConstantExpression(Expression expression) {
        if (expression instanceof ConstantExpression) {
            return (ConstantExpression) expression;
        }

        return this.configureAST(new ConstantExpression(expression.getText()), expression);
    }

    private BinaryExpression createBinaryExpression(ExpressionContext left, Token op, ExpressionContext right) {
        return new BinaryExpression((Expression) this.visit(left), this.createGroovyToken(op), (Expression) this.visit(right));
    }

    private Statement unpackStatement(Statement statement) {
        if (statement instanceof DeclarationListStatement) {
            List<ExpressionStatement> expressionStatementList = ((DeclarationListStatement) statement).getDeclarationStatements();

            if (1 == expressionStatementList.size()) {
                return expressionStatementList.get(0);
            }

            return this.configureAST(this.createBlockStatement(statement), statement); // if DeclarationListStatement contains more than 1 declarations, maybe it's better to create a block to hold them
        }

        return statement;
    }

    public BlockStatement createBlockStatement(Statement... statements) {
        return this.createBlockStatement(Arrays.asList(statements));
    }

    private BlockStatement createBlockStatement(List<Statement> statementList) {
        return this.appendStatementsToBlockStatement(new BlockStatement(), statementList);
    }

    public BlockStatement appendStatementsToBlockStatement(BlockStatement bs, Statement... statements) {
        return this.appendStatementsToBlockStatement(bs, Arrays.asList(statements));
    }

    private BlockStatement appendStatementsToBlockStatement(BlockStatement bs, List<Statement> statementList) {
        return (BlockStatement) statementList.stream()
                .reduce(bs, (r, e) -> {
                    BlockStatement blockStatement = (BlockStatement) r;

                    if (e instanceof DeclarationListStatement) {
                        ((DeclarationListStatement) e).getDeclarationStatements().forEach(blockStatement::addStatement);
                    } else {
                        blockStatement.addStatement(e);
                    }

                    return blockStatement;
                });
    }

    private boolean isAnnotationDeclaration(ClassNode classNode) {
        return asBoolean(classNode) && classNode.isAnnotationDefinition();
    }

    private boolean isSyntheticPublic(
            boolean isAnnotationDeclaration,
            boolean isAnonymousInnerEnumDeclaration,
            boolean hasReturnType,
            ModifierManager modifierManager
    ) {
        return this.isSyntheticPublic(
                isAnnotationDeclaration,
                isAnonymousInnerEnumDeclaration,
                modifierManager.containsAnnotations(),
                modifierManager.containsVisibilityModifier(),
                modifierManager.containsNonVisibilityModifier(),
                hasReturnType,
                modifierManager.contains(DEF));
    }

    /**
     * @param isAnnotationDeclaration         whether the method is defined in an annotation
     * @param isAnonymousInnerEnumDeclaration whether the method is defined in an anonymous inner enum
     * @param hasAnnotation                   whether the method declaration has annotations
     * @param hasVisibilityModifier           whether the method declaration contains visibility modifier(e.g. public, protected, private)
     * @param hasModifier                     whether the method declaration has modifier(e.g. visibility modifier, final, static and so on)
     * @param hasReturnType                   whether the method declaration has an return type(e.g. String, generic types)
     * @param hasDef                          whether the method declaration using def keyword
     * @return the result
     */
    private boolean isSyntheticPublic(
            boolean isAnnotationDeclaration,
            boolean isAnonymousInnerEnumDeclaration,
            boolean hasAnnotation,
            boolean hasVisibilityModifier,
            boolean hasModifier,
            boolean hasReturnType,
            boolean hasDef) {

        if (hasVisibilityModifier) {
            return false;
        }

        if (isAnnotationDeclaration) {
            return true;
        }

        if (hasDef && hasReturnType) {
            return true;
        }

        if (hasModifier || hasAnnotation || !hasReturnType) {
            return true;
        }

        if (isAnonymousInnerEnumDeclaration) {
            return true;
        }

        return false;
    }

    // the mixins of interface and annotation should be null
    private void hackMixins(ClassNode classNode) {
        try {
            // FIXME Hack with visibility.
            Field field = ClassNode.class.getDeclaredField("mixins");
            field.setAccessible(true);
            field.set(classNode, null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new GroovyBugError("Failed to access mixins field", e);
        }
    }

    private static final Map<ClassNode, Object> TYPE_DEFAULT_VALUE_MAP = Collections.unmodifiableMap(new HashMap<ClassNode, Object>() {
        {
            this.put(ClassHelper.int_TYPE, 0);
            this.put(ClassHelper.long_TYPE, 0L);
            this.put(ClassHelper.double_TYPE, 0.0D);
            this.put(ClassHelper.float_TYPE, 0.0F);
            this.put(ClassHelper.short_TYPE, (short) 0);
            this.put(ClassHelper.byte_TYPE, (byte) 0);
            this.put(ClassHelper.char_TYPE, (char) 0);
            this.put(ClassHelper.boolean_TYPE, Boolean.FALSE);
        }
    });

    private Object findDefaultValueByType(ClassNode type) {
        return TYPE_DEFAULT_VALUE_MAP.get(type);
    }

    private boolean isPackageInfoDeclaration() {
        String name = this.sourceUnit.getName();

        if (asBoolean((Object) name) && name.endsWith(PACKAGE_INFO_FILE_NAME)) {
            return true;
        }

        return false;
    }

    private boolean isBlankScript(CompilationUnitContext ctx) {
        return moduleNode.getStatementBlock().isEmpty() && moduleNode.getMethods().isEmpty() && moduleNode.getClasses().isEmpty();
    }

    private void addEmptyReturnStatement() {
        moduleNode.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
    }

    private void addPackageInfoClassNode() {
        List<ClassNode> classNodeList = moduleNode.getClasses();
        ClassNode packageInfoClassNode = ClassHelper.make(moduleNode.getPackageName() + PACKAGE_INFO);

        if (!classNodeList.contains(packageInfoClassNode)) {
            moduleNode.addClass(packageInfoClassNode);
        }
    }

    private org.codehaus.groovy.syntax.Token createGroovyTokenByType(Token token, int type) {
        if (null == token) {
            throw new IllegalArgumentException("token should not be null");
        }

        return new org.codehaus.groovy.syntax.Token(type, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    private org.codehaus.groovy.syntax.Token createGroovyToken(Token token) {
        return this.createGroovyToken(token, 1);
    }

    private org.codehaus.groovy.syntax.Token createGroovyToken(Token token, int cardinality) {
        String text = StringGroovyMethods.multiply((CharSequence) token.getText(), cardinality);
        return new org.codehaus.groovy.syntax.Token(
                "..<".equals(token.getText()) || "..".equals(token.getText())
                        ? Types.RANGE_OPERATOR
                        : Types.lookup(text, Types.ANY),
                text,
                token.getLine(),
                token.getCharPositionInLine() + 1
        );
    }

    /*
    private org.codehaus.groovy.syntax.Token createGroovyToken(String text, int startLine, int startColumn) {
        return new org.codehaus.groovy.syntax.Token(
                Types.lookup(text, Types.ANY),
                text,
                startLine,
                startColumn
        );
    }
    */

    /**
     * set the script source position
     */
    private void configureScriptClassNode() {
        ClassNode scriptClassNode = moduleNode.getScriptClassDummy();

        if (!asBoolean(scriptClassNode)) {
            return;
        }

        List<Statement> statements = moduleNode.getStatementBlock().getStatements();
        if (!statements.isEmpty()) {
            Statement firstStatement = statements.get(0);
            Statement lastStatement = statements.get(statements.size() - 1);

            scriptClassNode.setSourcePosition(firstStatement);
            scriptClassNode.setLastColumnNumber(lastStatement.getLastColumnNumber());
            scriptClassNode.setLastLineNumber(lastStatement.getLastLineNumber());
        }

    }

    /**
     * Sets location(lineNumber, colNumber, lastLineNumber, lastColumnNumber) for node using standard context information.
     * Note: this method is implemented to be closed over ASTNode. It returns same node as it received in arguments.
     *
     * @param astNode Node to be modified.
     * @param ctx     Context from which information is obtained.
     * @return Modified astNode.
     */
    private <T extends ASTNode> T configureAST(T astNode, GroovyParserRuleContext ctx) {
        Token start = ctx.getStart();
        Token stop = ctx.getStop();

        String stopText = stop.getText();
        int stopTextLength = 0;
        int newLineCnt = 0;
        if (asBoolean((Object) stopText)) {
            stopTextLength = stopText.length();
            newLineCnt = (int) StringUtils.countChar(stopText, '\n');
        }

        astNode.setLineNumber(start.getLine());
        astNode.setColumnNumber(start.getCharPositionInLine() + 1);

        if (0 == newLineCnt) {
            astNode.setLastLineNumber(stop.getLine());
            astNode.setLastColumnNumber(stop.getCharPositionInLine() + 1 + stop.getText().length());
        } else { // e.g. GStringEnd contains newlines, we should fix the location info
            astNode.setLastLineNumber(stop.getLine() + newLineCnt);
            astNode.setLastColumnNumber(stopTextLength - stopText.lastIndexOf('\n'));
        }

        return astNode;
    }

    private <T extends ASTNode> T configureAST(T astNode, TerminalNode terminalNode) {
        return this.configureAST(astNode, terminalNode.getSymbol());
    }

    private <T extends ASTNode> T configureAST(T astNode, Token token) {
        astNode.setLineNumber(token.getLine());
        astNode.setColumnNumber(token.getCharPositionInLine() + 1);
        astNode.setLastLineNumber(token.getLine());
        astNode.setLastColumnNumber(token.getCharPositionInLine() + 1 + token.getText().length());

        return astNode;
    }

    private <T extends ASTNode> T configureAST(T astNode, ASTNode source) {
        astNode.setLineNumber(source.getLineNumber());
        astNode.setColumnNumber(source.getColumnNumber());
        astNode.setLastLineNumber(source.getLastLineNumber());
        astNode.setLastColumnNumber(source.getLastColumnNumber());

        return astNode;
    }

    private boolean isTrue(GroovyParserRuleContext ctx, String key) {
        Object nmd = ctx.getNodeMetaData(key);

        if (null == nmd) {
            return false;
        }

        if (!(nmd instanceof Boolean)) {
            throw new GroovyBugError(ctx + " ctx meta data[" + key + "] is not an instance of Boolean");
        }

        return (Boolean) nmd;
    }

    private boolean isTrue(ASTNode node, String key) {
        Object nmd = node.getNodeMetaData(key);

        if (null == nmd) {
            return false;
        }

        if (!(nmd instanceof Boolean)) {
            throw new GroovyBugError(node + " node meta data[" + key + "] is not an instance of Boolean");
        }

        return (Boolean) nmd;
    }

    private CompilationFailedException createParsingFailedException(String msg, GroovyParserRuleContext ctx) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine() + 1,
                        ctx.stop.getLine(),
                        ctx.stop.getCharPositionInLine() + 1 + ctx.stop.getText().length()));
    }

    public CompilationFailedException createParsingFailedException(String msg, ASTNode node) {
        Objects.requireNonNull(node, "node passed into createParsingFailedException should not be null");

        return createParsingFailedException(
                new SyntaxException(msg,
                        node.getLineNumber(),
                        node.getColumnNumber(),
                        node.getLastLineNumber(),
                        node.getLastColumnNumber()));
    }

    /*
    private CompilationFailedException createParsingFailedException(String msg, Token token) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        token.getLine(),
                        token.getCharPositionInLine() + 1,
                        token.getLine(),
                        token.getCharPositionInLine() + 1 + token.getText().length()));
    }
    */

    private CompilationFailedException createParsingFailedException(Throwable t) {
        if (t instanceof SyntaxException) {
            this.collectSyntaxError((SyntaxException) t);
        } else if (t instanceof GroovySyntaxError) {
            GroovySyntaxError groovySyntaxError = (GroovySyntaxError) t;

            this.collectSyntaxError(
                    new SyntaxException(
                            groovySyntaxError.getMessage(),
                            groovySyntaxError,
                            groovySyntaxError.getLine(),
                            groovySyntaxError.getColumn()));
        } else if (t instanceof Exception) {
            this.collectException((Exception) t);
        }

        return new CompilationFailedException(
                CompilePhase.PARSING.getPhaseNumber(),
                this.sourceUnit,
                t);
    }

    private void collectSyntaxError(SyntaxException e) {
        sourceUnit.getErrorCollector().addFatalError(new SyntaxErrorMessage(e, sourceUnit));
    }

    private void collectException(Exception e) {
        sourceUnit.getErrorCollector().addException(e, this.sourceUnit);
    }

    private String readSourceCode(SourceUnit sourceUnit) {
        String text = null;
        try {
            text = IOGroovyMethods.getText(
                    new BufferedReader(
                            sourceUnit.getSource().getReader()));
        } catch (IOException e) {
            LOGGER.severe(createExceptionMessage(e));
            throw new RuntimeException("Error occurred when reading source code.", e);
        }

        return text;
    }

    private ANTLRErrorListener createANTLRErrorListener() {
        return new ANTLRErrorListener() {
            @Override
            public void syntaxError(
                    Recognizer recognizer,
                    Object offendingSymbol, int line, int charPositionInLine,
                    String msg, RecognitionException e) {

                collectSyntaxError(new SyntaxException(msg, line, charPositionInLine + 1));
            }
        };
    }

    private void removeErrorListeners() {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
    }

    private void addErrorListeners() {
        lexer.removeErrorListeners();
        lexer.addErrorListener(this.createANTLRErrorListener());

        parser.removeErrorListeners();
        parser.addErrorListener(this.createANTLRErrorListener());
    }

    private String createExceptionMessage(Throwable t) {
        StringWriter sw = new StringWriter();

        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }

        return sw.toString();
    }

    private class DeclarationListStatement extends Statement {
        private List<ExpressionStatement> declarationStatements;

        public DeclarationListStatement(DeclarationExpression... declarations) {
            this(Arrays.asList(declarations));
        }

        public DeclarationListStatement(List<DeclarationExpression> declarations) {
            this.declarationStatements =
                    declarations.stream()
                            .map(e -> configureAST(new ExpressionStatement(e), e))
                            .collect(Collectors.toList());
        }

        public List<ExpressionStatement> getDeclarationStatements() {
            List<String> declarationListStatementLabels = this.getStatementLabels();

            this.declarationStatements.forEach(e -> {
                if (asBoolean((Object) declarationListStatementLabels)) {
                    // clear existing statement labels before setting labels
                    if (asBoolean((Object) e.getStatementLabels())) {
                        e.getStatementLabels().clear();
                    }

                    declarationListStatementLabels.forEach(e::addStatementLabel);
                }
            });

            return this.declarationStatements;
        }

        public List<DeclarationExpression> getDeclarationExpressions() {
            return this.declarationStatements.stream()
                    .map(e -> (DeclarationExpression) e.getExpression())
                    .collect(Collectors.toList());
        }
    }

    private static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) &&
                    Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    private final ModuleNode moduleNode;
    private final SourceUnit sourceUnit;
    private final ClassLoader classLoader; // Our ClassLoader, which provides information on external types
    private final GroovyLangLexer lexer;
    private final GroovyLangParser parser;
    private final TryWithResourcesASTTransformation tryWithResourcesASTTransformation;
    private final GroovydocManager groovydocManager;
    private final List<ClassNode> classNodeList = new LinkedList<>();
    private final Deque<ClassNode> classNodeStack = new ArrayDeque<>();
    private final Deque<List<InnerClassNode>> anonymousInnerClassesDefinedInMethodStack = new ArrayDeque<>();
    private int anonymousInnerClassCounter = 1;
    private static final String QUESTION_STR = "?";
    private static final String DOT_STR = ".";
    private static final String SUB_STR = "-";
    private static final String ASSIGN_STR = "=";
    private static final String VALUE_STR = "value";
    private static final String DOLLAR_STR = "$";
    private static final String CALL_STR = "call";
    private static final String THIS_STR = "this";
    private static final String SUPER_STR = "super";
    private static final String VOID_STR = "void";
    private static final String PACKAGE_INFO = "package-info";
    private static final String PACKAGE_INFO_FILE_NAME = PACKAGE_INFO + ".groovy";
    private static final String GROOVY_TRANSFORM_TRAIT = "groovy.transform.Trait";
    private static final Set<String> PRIMITIVE_TYPE_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("boolean", "char", "byte", "short", "int", "long", "float", "double")));
    private static final Logger LOGGER = Logger.getLogger(AstBuilder.class.getName());

    private static final String IS_INSIDE_PARENTHESES = "_IS_INSIDE_PARENTHESES";
    private static final String INSIDE_PARENTHESES_LEVEL = "_INSIDE_PARENTHESES_LEVEL";

    private static final String IS_INSIDE_INSTANCEOF_EXPR = "_IS_INSIDE_INSTANCEOF_EXPR";
    private static final String IS_SWITCH_DEFAULT = "_IS_SWITCH_DEFAULT";
    private static final String IS_NUMERIC = "_IS_NUMERIC";
    private static final String IS_STRING = "_IS_STRING";
    private static final String IS_INTERFACE_WITH_DEFAULT_METHODS = "_IS_INTERFACE_WITH_DEFAULT_METHODS";

    private static final String PATH_EXPRESSION_BASE_EXPR = "_PATH_EXPRESSION_BASE_EXPR";
    private static final String PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES = "_PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES";
    private static final String CMD_EXPRESSION_BASE_EXPR = "_CMD_EXPRESSION_BASE_EXPR";
    private static final String TYPE_DECLARATION_MODIFIERS = "_TYPE_DECLARATION_MODIFIERS";
    private static final String CLASS_DECLARATION_CLASS_NODE = "_CLASS_DECLARATION_CLASS_NODE";
    private static final String VARIABLE_DECLARATION_VARIABLE_TYPE = "_VARIABLE_DECLARATION_VARIABLE_TYPE";
    private static final String ANONYMOUS_INNER_CLASS_SUPER_CLASS = "_ANONYMOUS_INNER_CLASS_SUPER_CLASS";
    private static final String INTEGER_LITERAL_TEXT = "_INTEGER_LITERAL_TEXT";
    private static final String FLOATING_POINT_LITERAL_TEXT = "_FLOATING_POINT_LITERAL_TEXT";

    private static final String CLASS_NAME = "_CLASS_NAME";
}
