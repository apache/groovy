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

import groovy.lang.Tuple2;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.groovy.parser.antlr4.internal.DescriptiveErrorStrategy;
import org.apache.groovy.parser.antlr4.internal.atnmanager.AtnManager;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.groovy.util.Maps;
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
import org.codehaus.groovy.ast.NodeMetaDataHandler;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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

import static groovy.lang.Tuple.tuple;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ADD;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AS;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AdditiveExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AndExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AnnotatedQualifiedClassNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AnnotationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AnnotationNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AnnotationsOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AnonymousInnerClassDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ArgumentsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ArrayInitializerContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AssertStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AssertStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.AssignmentExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BlockContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BlockStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BlockStatementsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BlockStatementsOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BooleanLiteralAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BreakStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BreakStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BuiltInTypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.BuiltInTypePrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CASE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CastExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CastParExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CatchClauseContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CatchTypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassBodyContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassBodyDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassOrInterfaceModifierContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassOrInterfaceModifiersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassOrInterfaceModifiersOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassOrInterfaceTypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassicalForControlContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClassifiedModifiersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClosureContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClosureOrLambdaExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ClosureOrLambdaExpressionPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CommandArgumentContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CommandExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CommandExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CompilationUnitContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ConditionalExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ConditionalStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ConditionalStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ContinueStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ContinueStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CreatedNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.CreatorContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DEC;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DEF;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DEFAULT;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DimsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DimsOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DoWhileStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.DynamicMemberNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ElementValueArrayInitializerContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ElementValueContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ElementValuePairContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ElementValuePairsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ElementValuesContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnhancedArgumentListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnhancedArgumentListElementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnhancedForControlContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnhancedStatementExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnumConstantContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EnumConstantsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.EqualityExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExclusiveOrExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExpressionInParContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExpressionListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExpressionListElementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ExpressionStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FieldDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FinallyBlockContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FloatingPointLiteralAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ForControlContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ForInitContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ForStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ForUpdateContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FormalParameterContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FormalParameterListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FormalParametersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GT;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GroovyParserRuleContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GstringContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GstringPathContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GstringPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.GstringValueContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IN;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.INC;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.INSTANCEOF;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IdentifierContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IdentifierPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IfElseStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ImportDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ImportStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.InclusiveOrExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IndexPropertyArgsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.IntegerLiteralAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.KeywordsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LT;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LabeledStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LambdaBodyContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ListPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LiteralPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LocalVariableDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LocalVariableDeclarationStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LogicalAndExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LogicalOrExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.LoopStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MapContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MapEntryContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MapEntryLabelContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MapEntryListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MapPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MemberDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MethodBodyContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MethodDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MethodDeclarationStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MethodNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ModifierContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ModifiersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ModifiersOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MultipleAssignmentExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.MultiplicativeExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NOT_IN;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NOT_INSTANCEOF;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NamePartContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NamedPropertyArgsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NewPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NonWildcardTypeArgumentsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NullLiteralAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PRIVATE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PackageDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ParExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ParenPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PathElementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PathExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PostfixExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PostfixExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PowerExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.PrimitiveTypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.QualifiedClassNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.QualifiedClassNameListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.QualifiedNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.QualifiedStandardClassNameContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.RegexExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.RelationalExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ResourceContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ResourceListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ResourcesContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ReturnStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ReturnTypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.STATIC;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SUB;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ShiftExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StandardLambdaExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StandardLambdaParametersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StatementsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StringLiteralAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.StringLiteralContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SuperPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SwitchBlockStatementGroupContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SwitchLabelContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SwitchStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.SynchronizedStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ThisFormalParameterContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ThisPrmrAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.ThrowStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TryCatchStatementContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TryCatchStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeArgumentContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeArgumentsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeArgumentsOrDiamondContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeBoundContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeDeclarationStmtAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeListContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeNamePairContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeNamePairsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeParameterContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.TypeParametersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.UnaryAddExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.UnaryNotExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VAR;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableDeclarationContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableDeclaratorContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableDeclaratorIdContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableDeclaratorsContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableInitializerContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableInitializersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableModifierContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableModifiersContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableModifiersOptContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VariableNamesContext;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.WhileStmtAltContext;
import static org.apache.groovy.parser.antlr4.util.PositionConfigureUtils.configureAST;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

/**
 * Building the AST from the parse tree generated by Antlr4
 */
public class AstBuilder extends GroovyParserBaseVisitor<Object> implements GroovyParserVisitor<Object> {
    public AstBuilder(SourceUnit sourceUnit, CompilerConfiguration compilerConfiguration) {
        this.sourceUnit = sourceUnit;
        this.compilerConfiguration = compilerConfiguration;
        this.moduleNode = new ModuleNode(sourceUnit);

        this.lexer = new GroovyLangLexer(createCharStream(sourceUnit));
        this.parser =
                new GroovyLangParser(
                    new CommonTokenStream(this.lexer));

        boolean errorRecovery = compilerConfiguration.getOptimizationOptions().getOrDefault(ERROR_RECOVERY, false);
        this.lexer.setErrorRecovery(errorRecovery);
        this.parser.setErrorRecovery(errorRecovery);
        this.parser.setErrorHandler(errorRecovery ? new DefaultErrorStrategy() : new DescriptiveErrorStrategy(this.lexer.getInputStream()));

        this.tryWithResourcesASTTransformation = new TryWithResourcesASTTransformation(this);
        this.groovydocManager = new GroovydocManager(compilerConfiguration);
    }

    private CharStream createCharStream(SourceUnit sourceUnit) {
        CharStream charStream;

        try {
            charStream = CharStreams.fromReader(
                    new BufferedReader(sourceUnit.getSource().getReader()),
                    sourceUnit.getName());
        } catch (IOException e) {
            throw new RuntimeException("Error occurred when reading source code.", e);
        }

        return charStream;
    }

    private GroovyParserRuleContext buildCST() throws CompilationFailedException {
        GroovyParserRuleContext result;

        try {
            // parsing have to wait util clearing is complete.
            AtnManager.READ_LOCK.lock();
            try {
                result = buildCST(PredictionMode.SLL);
            } catch (Throwable t) {
                // if some syntax error occurred in the lexer, no need to retry the powerful LL mode
                if (t instanceof GroovySyntaxThrowable && GroovySyntaxThrowable.LEXER == ((GroovySyntaxThrowable) t).getSource()) {
                    throw t;
                }

                result = buildCST(PredictionMode.LL);
            } finally {
                AtnManager.READ_LOCK.unlock();
            }
        } catch (Throwable t) {
            throw convertException(t);
        }

        return result;
    }

    private GroovyParserRuleContext buildCST(PredictionMode predictionMode) {
        parser.getInterpreter().setPredictionMode(predictionMode);

        if (PredictionMode.SLL.equals(predictionMode)) {
            this.removeErrorListeners();
        } else {
            parser.getInputStream().seek(0);
            this.addErrorListeners();
        }

        return parser.compilationUnit();
    }

    private CompilationFailedException convertException(Throwable t) {
        CompilationFailedException cfe;

        if (t instanceof CompilationFailedException) {
            cfe = (CompilationFailedException) t;
        } else if (t instanceof ParseCancellationException) {
            cfe = createParsingFailedException(t.getCause());
        } else {
            cfe = createParsingFailedException(t);
        }

        return cfe;
    }

    public ModuleNode buildAST() {
        try {
            return (ModuleNode) this.visit(this.buildCST());
        } catch (Throwable t) {
            throw convertException(t);
        }
    }

    @Override
    public ModuleNode visitCompilationUnit(CompilationUnitContext ctx) {
        this.visit(ctx.packageDeclaration());

        this.visitStatements(ctx.statements())
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
            if (this.isBlankScript()) {
                this.addEmptyReturnStatement();
            }
        }

        this.configureScriptClassNode();

        if (null != this.numberFormatError) {
            throw createParsingFailedException(this.numberFormatError.getV2().getMessage(), this.numberFormatError.getV1());
        }

        return moduleNode;
    }

    @Override
    public List<ASTNode> visitStatements(StatementsContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.statement().stream()
                .map(e -> (ASTNode) visit(e))
                .collect(Collectors.toList());
    }

    @Override
    public PackageNode visitPackageDeclaration(PackageDeclarationContext ctx) {
        String packageName = this.visitQualifiedName(ctx.qualifiedName());
        moduleNode.setPackageName(packageName + DOT_STR);

        PackageNode packageNode = moduleNode.getPackage();

        packageNode.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        return configureAST(packageNode, ctx);
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
                configureAST(type, ctx);

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
                configureAST(classNode, ctx);

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
                configureAST(classNode, ctx);

                moduleNode.addImport(alias, classNode, annotationNodeList);

                importNode = last(moduleNode.getImports());
            }
        }

        return configureAST(importNode, ctx);
    }

    // statement {    --------------------------------------------------------------------

    @Override
    public AssertStatement visitAssertStatement(AssertStatementContext ctx) {
        visitingAssertStatementCnt++;

        Expression conditionExpression = (Expression) this.visit(ctx.ce);

        if (conditionExpression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) conditionExpression;

            if (binaryExpression.getOperation().getType() == Types.ASSIGN) {
                throw createParsingFailedException("Assignment expression is not allowed in the assert statement", conditionExpression);
            }
        }

        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        if (!asBoolean(ctx.me)) {
            return configureAST(
                    new AssertStatement(booleanExpression), ctx);
        }

        AssertStatement result = configureAST(new AssertStatement(booleanExpression,
                        (Expression) this.visit(ctx.me)),
                ctx);

        visitingAssertStatementCnt--;

        return result;
    }

    @Override
    public AssertStatement visitAssertStmtAlt(AssertStmtAltContext ctx) {
        return configureAST(this.visitAssertStatement(ctx.assertStatement()), ctx);
    }

    @Override
    public Statement visitConditionalStmtAlt(ConditionalStmtAltContext ctx) {
        return configureAST(this.visitConditionalStatement(ctx.conditionalStatement()), ctx);
    }

    @Override
    public Statement visitConditionalStatement(ConditionalStatementContext ctx) {
        if (asBoolean(ctx.ifElseStatement())) {
            return configureAST(this.visitIfElseStatement(ctx.ifElseStatement()), ctx);
        } else if (asBoolean(ctx.switchStatement())) {
            return configureAST(this.visitSwitchStatement(ctx.switchStatement()), ctx);
        }

        throw createParsingFailedException("Unsupported conditional statement", ctx);
    }

    @Override
    public IfStatement visitIfElseStatement(IfElseStatementContext ctx) {
        Expression conditionExpression = this.visitExpressionInPar(ctx.expressionInPar());
        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        Statement ifBlock =
                this.unpackStatement(
                        (Statement) this.visit(ctx.tb));
        Statement elseBlock =
                this.unpackStatement(
                        asBoolean(ctx.ELSE())
                                ? (Statement) this.visit(ctx.fb)
                                : EmptyStatement.INSTANCE);

        return configureAST(new IfStatement(booleanExpression, ifBlock, elseBlock), ctx);
    }

    @Override
    public Statement visitLoopStmtAlt(LoopStmtAltContext ctx) {
        visitingLoopStatementCnt++;
        Statement result = configureAST((Statement) this.visit(ctx.loopStatement()), ctx);
        visitingLoopStatementCnt--;

        return result;
    }

    @Override
    public ForStatement visitForStmtAlt(ForStmtAltContext ctx) {
        Tuple2<Parameter, Expression> controlTuple = this.visitForControl(ctx.forControl());

        Statement loopBlock = this.unpackStatement((Statement) this.visit(ctx.statement()));

        return configureAST(
                new ForStatement(controlTuple.getV1(), controlTuple.getV2(), asBoolean(loopBlock) ? loopBlock : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public Tuple2<Parameter, Expression> visitForControl(ForControlContext ctx) {
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
            List<? extends Expression> declarationExpressionList = declarationListStatement.getDeclarationExpressions();

            if (declarationExpressionList.size() == 1) {
                return configureAST((Expression) declarationExpressionList.get(0), ctx);
            } else {
                return configureAST(new ClosureListExpression((List<Expression>) declarationExpressionList), ctx);
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
            return configureAST(expressionList.get(0), ctx);
        } else {
            return configureAST(new ClosureListExpression(expressionList), ctx);
        }
    }

    @Override
    public Tuple2<Parameter, Expression> visitEnhancedForControl(EnhancedForControlContext ctx) {
        Parameter parameter = configureAST(
                new Parameter(this.visitType(ctx.type()), this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName()),
                ctx.variableDeclaratorId());

        // FIXME Groovy will ignore variableModifier of parameter in the for control
        // In order to make the new parser behave same with the old one, we do not process variableModifier*

        return tuple(parameter, (Expression) this.visit(ctx.expression()));
    }

    @Override
    public Tuple2<Parameter, Expression> visitClassicalForControl(ClassicalForControlContext ctx) {
        ClosureListExpression closureListExpression = new ClosureListExpression();

        closureListExpression.addExpression(this.visitForInit(ctx.forInit()));
        closureListExpression.addExpression(asBoolean(ctx.expression()) ? (Expression) this.visit(ctx.expression()) : EmptyExpression.INSTANCE);
        closureListExpression.addExpression(this.visitForUpdate(ctx.forUpdate()));

        return tuple(ForStatement.FOR_LOOP_DUMMY, closureListExpression);
    }

    @Override
    public WhileStatement visitWhileStmtAlt(WhileStmtAltContext ctx) {
        Tuple2<BooleanExpression, Statement> conditionAndBlock = createLoopConditionExpressionAndBlock(ctx.expressionInPar(), ctx.statement());

        return configureAST(
                new WhileStatement(conditionAndBlock.getV1(), asBoolean(conditionAndBlock.getV2()) ? conditionAndBlock.getV2() : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public DoWhileStatement visitDoWhileStmtAlt(DoWhileStmtAltContext ctx) {
        Tuple2<BooleanExpression, Statement> conditionAndBlock = createLoopConditionExpressionAndBlock(ctx.expressionInPar(), ctx.statement());

        return configureAST(
                new DoWhileStatement(conditionAndBlock.getV1(), asBoolean(conditionAndBlock.getV2()) ? conditionAndBlock.getV2() : EmptyStatement.INSTANCE),
                ctx);
    }

    private Tuple2<BooleanExpression, Statement> createLoopConditionExpressionAndBlock(ExpressionInParContext eipc, StatementContext sc) {
        Expression conditionExpression = this.visitExpressionInPar(eipc);

        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression),
                        conditionExpression
                );

        Statement loopBlock = this.unpackStatement((Statement) this.visit(sc));

        return tuple(booleanExpression, loopBlock);
    }

    @Override
    public Statement visitTryCatchStmtAlt(TryCatchStmtAltContext ctx) {
        return configureAST(this.visitTryCatchStatement(ctx.tryCatchStatement()), ctx);
    }

    @Override
    public Statement visitTryCatchStatement(TryCatchStatementContext ctx) {
        boolean resourcesExists = asBoolean(ctx.resources());
        boolean catchExists = asBoolean(ctx.catchClause());
        boolean finallyExists = asBoolean(ctx.finallyBlock());

        if (!(resourcesExists || catchExists || finallyExists)) {
            throw createParsingFailedException("Either a catch or finally clause or both is required for a try-catch-finally statement", ctx);
        }

        TryCatchStatement tryCatchStatement =
                new TryCatchStatement((Statement) this.visit(ctx.block()),
                        this.visitFinallyBlock(ctx.finallyBlock()));

        if (resourcesExists) {
            this.visitResources(ctx.resources()).forEach(tryCatchStatement::addResource);
        }

        ctx.catchClause().stream().map(this::visitCatchClause)
                .reduce(new LinkedList<CatchStatement>(), (r, e) -> {
                    r.addAll(e); // merge several LinkedList<CatchStatement> instances into one LinkedList<CatchStatement> instance
                    return r;
                })
                .forEach(tryCatchStatement::addCatch);

        return configureAST(
                tryWithResourcesASTTransformation.transform(
                        configureAST(tryCatchStatement, ctx)),
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
            boolean isVariableDeclaration = expression instanceof BinaryExpression
                    && Types.ASSIGN == ((BinaryExpression) expression).getOperation().getType()
                    && ((BinaryExpression) expression).getLeftExpression() instanceof VariableExpression;
            boolean isVariableAccess = expression instanceof VariableExpression;

            if (!(isVariableDeclaration || isVariableAccess)) {
                throw createParsingFailedException("Only variable declarations or variable access are allowed to declare resource", ctx);
            }
            BinaryExpression assignmentExpression;

            if (isVariableDeclaration) {
                assignmentExpression = (BinaryExpression) expression;
            } else if (isVariableAccess) {
                assignmentExpression = tryWithResourcesASTTransformation.transformResourceAccess(expression);
            } else {
                throw createParsingFailedException("Unsupported resource declaration", ctx);
            }

            return configureAST(
                    new ExpressionStatement(
                            configureAST(
                                    new DeclarationExpression(
                                            configureAST(
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
     * @return a list of CatchStatement instances
     */
    @Override
    public List<CatchStatement> visitCatchClause(CatchClauseContext ctx) {
        // FIXME Groovy will ignore variableModifier of parameter in the catch clause
        // In order to make the new parser behave same with the old one, we do not process variableModifier*

        return this.visitCatchType(ctx.catchType()).stream()
                .map(e -> configureAST(
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

        return configureAST(
                this.createBlockStatement((Statement) this.visit(ctx.block())),
                ctx);
    }



    @Override
    public SwitchStatement visitSwitchStatement(SwitchStatementContext ctx) {
        visitingSwitchStatementCnt++;

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

        SwitchStatement result = configureAST(
                new SwitchStatement(
                        this.visitExpressionInPar(ctx.expressionInPar()),
                        caseStatementList,
                        defaultStatementListSize == 0 ? EmptyStatement.INSTANCE : defaultStatementList.get(0)
                ),
                ctx);

        visitingSwitchStatementCnt--;

        return result;
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
                    Tuple2<Token, Expression> tuple = (Tuple2<Token, Expression>) e;

                    boolean isLast = labelCnt - 1 == statementList.size();

                    switch (tuple.getV1().getType()) {
                        case CASE: {
                            if (!asBoolean(statementList)) {
                                firstLabelHolder.add(tuple.getV1());
                            }

                            statementList.add(
                                    configureAST(
                                            new CaseStatement(
                                                    tuple.getV2(),

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
                                    // this.configureAST(blockStatement, tuple.getKey())
                                    blockStatement
                            );

                            break;
                        }
                    }

                    return statementList;
                });

    }

    @Override
    public Tuple2<Token, Expression> visitSwitchLabel(SwitchLabelContext ctx) {
        if (asBoolean(ctx.CASE())) {
            return tuple(ctx.CASE().getSymbol(), (Expression) this.visit(ctx.expression()));
        } else if (asBoolean(ctx.DEFAULT())) {
            return tuple(ctx.DEFAULT().getSymbol(), EmptyExpression.INSTANCE);
        }

        throw createParsingFailedException("Unsupported switch label: " + ctx.getText(), ctx);
    }

    @Override
    public SynchronizedStatement visitSynchronizedStmtAlt(SynchronizedStmtAltContext ctx) {
        return configureAST(
                new SynchronizedStatement(this.visitExpressionInPar(ctx.expressionInPar()), this.visitBlock(ctx.block())),
                ctx);
    }

    @Override
    public ExpressionStatement visitExpressionStmtAlt(ExpressionStmtAltContext ctx) {
        return (ExpressionStatement) this.visit(ctx.statementExpression());
    }

    @Override
    public ReturnStatement visitReturnStmtAlt(ReturnStmtAltContext ctx) {
        return configureAST(new ReturnStatement(asBoolean(ctx.expression())
                        ? (Expression) this.visit(ctx.expression())
                        : ConstantExpression.EMPTY_EXPRESSION),
                ctx);
    }

    @Override
    public ThrowStatement visitThrowStmtAlt(ThrowStmtAltContext ctx) {
        return configureAST(
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
        if (0 == visitingLoopStatementCnt && 0 == visitingSwitchStatementCnt) {
            throw createParsingFailedException("break statement is only allowed inside loops or switches", ctx);
        }

        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return configureAST(new BreakStatement(label), ctx);
    }

    @Override
    public BreakStatement visitBreakStmtAlt(BreakStmtAltContext ctx) {
        return configureAST(this.visitBreakStatement(ctx.breakStatement()), ctx);
    }

    @Override
    public ContinueStatement visitContinueStatement(ContinueStatementContext ctx) {
        if (0 == visitingLoopStatementCnt) {
            throw createParsingFailedException("continue statement is only allowed inside loops", ctx);
        }

        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return configureAST(new ContinueStatement(label), ctx);

    }

    @Override
    public ContinueStatement visitContinueStmtAlt(ContinueStmtAltContext ctx) {
        return configureAST(this.visitContinueStatement(ctx.continueStatement()), ctx);
    }

    @Override
    public ImportNode visitImportStmtAlt(ImportStmtAltContext ctx) {
        return configureAST(this.visitImportDeclaration(ctx.importDeclaration()), ctx);
    }

    @Override
    public ClassNode visitTypeDeclarationStmtAlt(TypeDeclarationStmtAltContext ctx) {
        return configureAST(this.visitTypeDeclaration(ctx.typeDeclaration()), ctx);
    }


    @Override
    public Statement visitLocalVariableDeclarationStmtAlt(LocalVariableDeclarationStmtAltContext ctx) {
        return configureAST(this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()), ctx);
    }

    @Override
    public MethodNode visitMethodDeclarationStmtAlt(MethodDeclarationStmtAltContext ctx) {
        return configureAST(this.visitMethodDeclaration(ctx.methodDeclaration()), ctx);
    }

    // } statement    --------------------------------------------------------------------

    @Override
    public ClassNode visitTypeDeclaration(TypeDeclarationContext ctx) {
        if (asBoolean(ctx.classDeclaration())) { // e.g. class A {}
            ctx.classDeclaration().putNodeMetaData(TYPE_DECLARATION_MODIFIERS, this.visitClassOrInterfaceModifiersOpt(ctx.classOrInterfaceModifiersOpt()));
            return configureAST(this.visitClassDeclaration(ctx.classDeclaration()), ctx);
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

        if (!classNode.isUsingGenerics() && null != classNode.getInterfaces()) {
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
        packageName = null != packageName ? packageName : "";

        List<ModifierNode> modifierNodeList = ctx.getNodeMetaData(TYPE_DECLARATION_MODIFIERS);
        Objects.requireNonNull(modifierNodeList, "modifierNodeList should not be null");

        ModifierManager modifierManager = new ModifierManager(this, modifierNodeList);
        int modifiers = modifierManager.getClassModifiersOpValue();

        boolean syntheticPublic = ((modifiers & Opcodes.ACC_SYNTHETIC) != 0);
        modifiers &= ~Opcodes.ACC_SYNTHETIC;

        final ClassNode outerClass = classNodeStack.peek();
        ClassNode classNode;
        String className = this.visitIdentifier(ctx.identifier());

        if (VAR_STR.equals(className)) {
            throw createParsingFailedException("var cannot be used for type declarations", ctx.identifier());
        }

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

        configureAST(classNode, ctx);
        classNode.putNodeMetaData(CLASS_NAME, className);
        classNode.setSyntheticPublic(syntheticPublic);

        if (asBoolean(ctx.TRAIT())) {
            attachTraitAnnotation(classNode);
        }
        classNode.addAnnotations(modifierManager.getAnnotations());
        classNode.setGenericsTypes(this.visitTypeParameters(ctx.typeParameters()));

        boolean isInterface = asBoolean(ctx.INTERFACE()) && !asBoolean(ctx.AT());
        boolean isInterfaceWithDefaultMethods = false;

        // declaring interface with default method
        if (isInterface && this.containsDefaultMethods(ctx)) {
            isInterfaceWithDefaultMethods = true;
            attachTraitAnnotation(classNode);
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

    private void attachTraitAnnotation(ClassNode classNode) {
        attachAnnotation(classNode, GROOVY_TRANSFORM_TRAIT);
    }

    private void attachAnnotation(ClassNode classNode, String annotationClassName) {
        classNode.addAnnotation(new AnnotationNode(ClassHelper.make(annotationClassName)));
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

                    if (createModifierManager(methodDeclarationContext).containsAny(DEFAULT)) {
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

        enumConstant.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        groovydocManager.handle(enumConstant, ctx);

        return configureAST(enumConstant, ctx);
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
                            configureAST(
                                    new ClassExpression(anonymousInnerClassNode),
                                    anonymousInnerClassNode));
                }

                if (mapEntryExpressionList.size() > 1) {
                    listExpression.setWrapped(true);
                }

                return configureAST(listExpression, ctx);
            }

            if (!asBoolean(anonymousInnerClassNode)) {
                if (expression instanceof ListExpression) {
                    ListExpression listExpression = new ListExpression();
                    listExpression.addExpression(expression);

                    return configureAST(listExpression, ctx);
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
                    configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));

            return configureAST(listExpression, ctx);
        }

        ListExpression listExpression = new ListExpression(expressions);
        if (asBoolean(anonymousInnerClassNode)) {
            listExpression.addExpression(
                    configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));
        }

        if (asBoolean(ctx)) {
            listExpression.setWrapped(true);
        }

        return asBoolean(ctx)
                ? configureAST(listExpression, ctx)
                : configureAST(listExpression, anonymousInnerClassNode);
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
                        configureAST(
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
        return configureAST(
                new GenericsType(
                        configureAST(ClassHelper.make(this.visitClassName(ctx.className())), ctx),
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

        if (asBoolean(ctx.modifiersOpt())) {
            modifierNodeList = this.visitModifiersOpt(ctx.modifiersOpt());
        }

        return new ModifierManager(this, modifierNodeList);
    }

    private void validateParametersOfMethodDeclaration(Parameter[] parameters, ClassNode classNode) {
        if (!classNode.isInterface()) {
            return;
        }

        for (Parameter parameter : parameters) {
            if (parameter.hasInitialExpression()) {
                throw createParsingFailedException("Cannot specify default value for method parameter '" + parameter.getName() + " = " + parameter.getInitialExpression().getText() + "' inside an interface", parameter);
            }
        }
    }

    @Override
    public MethodNode visitMethodDeclaration(MethodDeclarationContext ctx) {
        ModifierManager modifierManager = createModifierManager(ctx);

        if (modifierManager.containsAny(VAR)) {
            throw createParsingFailedException("var cannot be used for method declarations", ctx);
        }

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

        if (modifierManager.containsAny(STATIC)) {
            for (Parameter parameter : methodNode.getParameters()) {
                parameter.setInStaticContext(true);
            }

            methodNode.getVariableScope().setInStaticContext(true);
        }

        configureAST(methodNode, ctx);

        validateMethodDeclaration(ctx, methodNode, modifierManager, classNode);

        groovydocManager.handle(methodNode, ctx);

        return methodNode;
    }

    private void validateMethodDeclaration(MethodDeclarationContext ctx, MethodNode methodNode, ModifierManager modifierManager, ClassNode classNode) {
        if (1 == ctx.t || 2 == ctx.t || 3 == ctx.t) { // 1: normal method declaration; 2: abstract method declaration; 3: normal method declaration OR abstract method declaration
            if (!(asBoolean(ctx.modifiersOpt().modifiers()) || asBoolean(ctx.returnType()))) {
                throw createParsingFailedException("Modifiers or return type is required", ctx);
            }
        }

        if (1 == ctx.t) {
            if (!asBoolean(ctx.methodBody())) {
                throw createParsingFailedException("Method body is required", ctx);
            }
        }

        if (2 == ctx.t) {
            if (asBoolean(ctx.methodBody())) {
                throw createParsingFailedException("Abstract method should not have method body", ctx);
            }
        }

        boolean isAbstractMethod = methodNode.isAbstract();
        boolean hasMethodBody = asBoolean(methodNode.getCode());

        if (9 == ctx.ct) { // script
            if (isAbstractMethod || !hasMethodBody) { // method should not be declared abstract in the script
                throw createParsingFailedException("You cannot define " + (isAbstractMethod ? "an abstract" : "a") + " method[" + methodNode.getName() + "] " + (!hasMethodBody ? "without method body " : "") + "in the script. Try " + (isAbstractMethod ? "removing the 'abstract'" : "") + (isAbstractMethod && !hasMethodBody ? " and" : "") + (!hasMethodBody ? " adding a method body" : ""), methodNode);
            }
        } else {
            if (4 == ctx.ct) { // trait
                if (isAbstractMethod && hasMethodBody) {
                    throw createParsingFailedException("Abstract method should not have method body", ctx);
                }
            }

            if (!isAbstractMethod && !hasMethodBody) { // non-abstract method without body in the non-script(e.g. class, enum, trait) is not allowed!
                throw createParsingFailedException("You defined a method[" + methodNode.getName() + "] without a body. Try adding a method body, or declare it abstract", methodNode);
            }

            boolean isInterfaceOrAbstractClass = asBoolean(classNode) && classNode.isAbstract() && !classNode.isAnnotationDefinition();
            if (isInterfaceOrAbstractClass && !modifierManager.containsAny(DEFAULT) && isAbstractMethod && hasMethodBody) {
                throw createParsingFailedException("You defined an abstract method[" + methodNode.getName() + "] with a body. Try removing the method body" + (classNode.isInterface() ? ", or declare it default" : ""), methodNode);
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
                        modifierManager.containsAny(PRIVATE) ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PUBLIC,
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

        boolean hasReturnType = asBoolean(ctx.returnType());
        boolean hasMethodBody = asBoolean(ctx.methodBody());

        if (!hasReturnType
                && hasMethodBody
                && methodName.equals(className)) { // constructor declaration

            methodNode = createConstructorNodeForClass(methodName, parameters, exceptions, code, classNode, modifiers);
        } else { // class memeber method declaration
            if (!hasReturnType && hasMethodBody && (0 == modifierManager.getModifierCount())) {
                throw createParsingFailedException("Invalid method declaration: " + methodName, ctx);
            }

            methodNode = createMethodNodeForClass(ctx, modifierManager, methodName, returnType, parameters, exceptions, code, classNode, modifiers);
        }

        modifierManager.attachAnnotations(methodNode);
        return methodNode;
    }

    private MethodNode createMethodNodeForClass(MethodDeclarationContext ctx, ModifierManager modifierManager, String methodName, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code, ClassNode classNode, int modifiers) {
        MethodNode methodNode;
        if (asBoolean(ctx.elementValue())) { // the code of annotation method
            code = configureAST(
                    new ExpressionStatement(
                            this.visitElementValue(ctx.elementValue())),
                    ctx.elementValue());

        }

        modifiers |= !modifierManager.containsAny(STATIC) && (classNode.isInterface() || (isTrue(classNode, IS_INTERFACE_WITH_DEFAULT_METHODS) && !modifierManager.containsAny(DEFAULT))) ? Opcodes.ACC_ABSTRACT : 0;

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

        return configureAST(this.visitBlock(ctx.block()), ctx);
    }

    @Override
    public DeclarationListStatement visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
        return configureAST(this.visitVariableDeclaration(ctx.variableDeclaration()), ctx);
    }

    private ModifierManager createModifierManager(VariableDeclarationContext ctx) {
        return new ModifierManager(this, this.visitClassifiedModifiers(ctx.classifiedModifiers()));
    }

    private DeclarationListStatement createMultiAssignmentDeclarationListStatement(VariableDeclarationContext ctx, ModifierManager modifierManager) {
        /*
        if (!modifierManager.contains(DEF)) {
            throw createParsingFailedException("keyword def is required to declare tuple, e.g. def (int a, int b) = [1, 2]", ctx);
        }
        */

        return configureAST(
                new DeclarationListStatement(
                        configureAST(
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
    public List<ModifierNode> visitClassifiedModifiers(ClassifiedModifiersContext ctx) {
        List<ModifierNode> modifierNodeList = Collections.emptyList();

        if (!asBoolean(ctx)) {
            return modifierNodeList;
        }

        if (asBoolean(ctx.variableModifiers())) {
            modifierNodeList = this.visitVariableModifiers(ctx.variableModifiers());
        } if (asBoolean(ctx.modifiers())) {
            modifierNodeList = this.visitModifiers(ctx.modifiers());
        }

        return modifierNodeList;
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
                configureAST(declarationExpression, ctx);
            } else {
                // Tweak start of first declaration
                declarationExpression.setLineNumber(ctx.getStart().getLine());
                declarationExpression.setColumnNumber(ctx.getStart().getCharPositionInLine() + 1);
            }
        }

        return configureAST(new DeclarationListStatement(declarationExpressionList), ctx);
    }

    private DeclarationListStatement createFieldDeclarationListStatement(VariableDeclarationContext ctx, ModifierManager modifierManager, ClassNode variableType, List<DeclarationExpression> declarationExpressionList, ClassNode classNode) {
        for (int i = 0, n = declarationExpressionList.size(); i < n; i++) {
            DeclarationExpression declarationExpression = declarationExpressionList.get(i);
            VariableExpression variableExpression = (VariableExpression) declarationExpression.getLeftExpression();

            String fieldName = variableExpression.getName();

            int modifiers = modifierManager.getClassMemberModifiersOpValue();

            Expression initialValue = EmptyExpression.INSTANCE.equals(declarationExpression.getRightExpression()) ? null : declarationExpression.getRightExpression();
            Object defaultValue = findDefaultValueByType(variableType);

            if (classNode.isInterface()) {
                if (!asBoolean(initialValue)) {
                    initialValue = !asBoolean(defaultValue) ? null : new ConstantExpression(defaultValue);
                }

                modifiers |= Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            }

            if (isFieldDeclaration(modifierManager, classNode)) {
                declareField(ctx, modifierManager, variableType, classNode, i, variableExpression, fieldName, modifiers, initialValue);
            } else {
                declareProperty(ctx, modifierManager, variableType, classNode, i, variableExpression, fieldName, modifiers, initialValue);
            }
        }

        return null;
    }

    private void declareProperty(VariableDeclarationContext ctx, ModifierManager modifierManager, ClassNode variableType, ClassNode classNode, int i, VariableExpression variableExpression, String fieldName, int modifiers, Expression initialValue) {
        if (classNode.hasProperty(fieldName)) {
            throw createParsingFailedException("The property '" + fieldName + "' is declared multiple times", ctx);
        }

        PropertyNode propertyNode;
        FieldNode fieldNode = classNode.getDeclaredField(fieldName);

        if (fieldNode != null && !classNode.hasProperty(fieldName)) {
            classNode.getFields().remove(fieldNode);

            propertyNode = new PropertyNode(fieldNode, modifiers | Opcodes.ACC_PUBLIC, null, null);
            classNode.addProperty(propertyNode);
        } else {
            propertyNode =
                    classNode.addProperty(
                            fieldName,
                            modifiers | Opcodes.ACC_PUBLIC,
                            variableType,
                            initialValue,
                            null,
                            null);

            fieldNode = propertyNode.getField();
        }

        fieldNode.setModifiers(modifiers & ~Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE);
        fieldNode.setSynthetic(!classNode.isInterface());
        modifierManager.attachAnnotations(fieldNode);

        groovydocManager.handle(fieldNode, ctx);
        groovydocManager.handle(propertyNode, ctx);

        if (0 == i) {
            configureAST(fieldNode, ctx, initialValue);
            configureAST(propertyNode, ctx, initialValue);
        } else {
            configureAST(fieldNode, variableExpression, initialValue);
            configureAST(propertyNode, variableExpression, initialValue);
        }
    }

    private void declareField(VariableDeclarationContext ctx, ModifierManager modifierManager, ClassNode variableType, ClassNode classNode, int i, VariableExpression variableExpression, String fieldName, int modifiers, Expression initialValue) {
        FieldNode existingFieldNode = classNode.getDeclaredField(fieldName);
        if (null != existingFieldNode && !existingFieldNode.isSynthetic()) {
            throw createParsingFailedException("The field '" + fieldName + "' is declared multiple times", ctx);
        }

        FieldNode fieldNode;
        PropertyNode propertyNode = classNode.getProperty(fieldName);

        if (null != propertyNode && propertyNode.getField().isSynthetic()) {
            classNode.getFields().remove(propertyNode.getField());
            fieldNode = new FieldNode(fieldName, modifiers, variableType, classNode.redirect(), initialValue);
            propertyNode.setField(fieldNode);
            classNode.addField(fieldNode);
        } else {
            fieldNode =
                    classNode.addField(
                            fieldName,
                            modifiers,
                            variableType,
                            initialValue);
        }

        modifierManager.attachAnnotations(fieldNode);
        groovydocManager.handle(fieldNode, ctx);

        if (0 == i) {
            configureAST(fieldNode, ctx, initialValue);
        } else {
            configureAST(fieldNode, variableExpression, initialValue);
        }
    }

    private boolean isFieldDeclaration(ModifierManager modifierManager, ClassNode classNode) {
        return classNode.isInterface() || modifierManager.containsVisibilityModifier();
    }

    @Override
    public List<Expression> visitTypeNamePairs(TypeNamePairsContext ctx) {
        return ctx.typeNamePair().stream().map(this::visitTypeNamePair).collect(Collectors.toList());
    }

    @Override
    public VariableExpression visitTypeNamePair(TypeNamePairContext ctx) {
        return configureAST(
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

        return configureAST(
                new DeclarationExpression(
                        configureAST(
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

        return configureAST(
                this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression()),
                ctx);
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

    private int visitingArrayInitializerCnt = 0;

    @Override
    public List<Expression> visitArrayInitializer(ArrayInitializerContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        try {
            visitingArrayInitializerCnt++;
            return this.visitVariableInitializers(ctx.variableInitializers());
        } finally {
            visitingArrayInitializerCnt--;
        }
    }

    @Override
    public Statement visitBlock(BlockContext ctx) {
        if (!asBoolean(ctx)) {
            return this.createBlockStatement();
        }

        return configureAST(
                this.visitBlockStatementsOpt(ctx.blockStatementsOpt()),
                ctx);
    }

/*
    @Override
    public Expression visitEnhancedExpression(EnhancedExpressionContext ctx) {
        Expression expression;

        if (asBoolean(ctx.expression())) {
            expression = (Expression) this.visit(ctx.expression());
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            expression = this.visitStandardLambdaExpression(ctx.standardLambdaExpression());
        } else {
            throw createParsingFailedException("Unsupported enhanced expression: " + ctx.getText(), ctx);
        }

        return configureAST(expression, ctx);
    }
*/

    @Override
    public ExpressionStatement visitCommandExprAlt(CommandExprAltContext ctx) {
        return configureAST(new ExpressionStatement(this.visitCommandExpression(ctx.commandExpression())), ctx);
    }

    @Override
    public Expression visitCommandExpression(CommandExpressionContext ctx) {
        boolean hasArgumentList = asBoolean(ctx.enhancedArgumentList());
        boolean hasCommandArgument = asBoolean(ctx.commandArgument());

        if (visitingArrayInitializerCnt > 0 && (hasArgumentList || hasCommandArgument)) {
            // To avoid ambiguities, command chain expression should not be used in array initializer
            // the old parser does not support either, so no breaking changes
            // SEE http://groovy.329449.n5.nabble.com/parrot-Command-expressions-in-array-initializer-tt5752273.html
            throw createParsingFailedException("Command chain expression can not be used in array initializer", ctx);
        }

        Expression baseExpr = (Expression) this.visit(ctx.expression());


        if (hasArgumentList || hasCommandArgument) {
            if (baseExpr instanceof BinaryExpression) {
                if (!"[".equals(((BinaryExpression) baseExpr).getOperation().getText()) && !isInsideParentheses(baseExpr)) {
                    throw createParsingFailedException("Unexpected input: '" + getOriginalText(ctx.expression()) + "'", ctx.expression());
                }
            }
        }

        MethodCallExpression methodCallExpression = null;

        if (hasArgumentList) {
            Expression arguments = this.visitEnhancedArgumentList(ctx.enhancedArgumentList());

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.a 1, 2
                methodCallExpression =
                        configureAST(
                                this.createMethodCallExpression(
                                        (PropertyExpression) baseExpr, arguments),
                                arguments);

            } else if (baseExpr instanceof MethodCallExpression && !isInsideParentheses(baseExpr)) { // e.g. m {} a, b  OR  m(...) a, b
                if (asBoolean(arguments)) {
                    // The error should never be thrown.
                    throw new GroovyBugError("When baseExpr is a instance of MethodCallExpression, which should follow NO argumentList");
                }

                methodCallExpression = (MethodCallExpression) baseExpr;
            } else if (
                    !isInsideParentheses(baseExpr)
                            && (baseExpr instanceof VariableExpression /* e.g. m 1, 2 */
                            || baseExpr instanceof GStringExpression /* e.g. "$m" 1, 2 */
                            || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING)) /* e.g. "m" 1, 2 */)
            ) {
                methodCallExpression =
                        configureAST(
                                this.createMethodCallExpression(baseExpr, arguments),
                                arguments);
            } else { // e.g. a[x] b, new A() b, etc.
                methodCallExpression = configureAST(this.createCallMethodCallExpression(baseExpr, arguments), arguments);
            }

            methodCallExpression.putNodeMetaData(IS_COMMAND_EXPRESSION, true);

            if (!hasCommandArgument) {
                return configureAST(methodCallExpression, ctx);
            }
        }

        if (hasCommandArgument) {
            baseExpr.putNodeMetaData(IS_COMMAND_EXPRESSION, true);
        }

        return configureAST(
                (Expression) ctx.commandArgument().stream()
                        .map(e -> (Object) e)
                        .reduce(null == methodCallExpression ? baseExpr : methodCallExpression,
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

            return configureAST(methodCallExpression, ctx);
        } else if (asBoolean(ctx.pathElement())) { // e.g. x y a.b
            Expression pathExpression =
                    this.createPathExpression(
                            configureAST(
                                    new PropertyExpression(baseExpr, this.createConstantExpression(primaryExpr)),
                                    primaryExpr
                            ),
                            ctx.pathElement()
                    );

            return configureAST(pathExpression, ctx);
        }

        // e.g. x y a
        return configureAST(
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
        Expression expression = this.visitExpressionInPar(ctx.expressionInPar());

        Integer insideParenLevel = expression.getNodeMetaData(INSIDE_PARENTHESES_LEVEL);
        if (null != insideParenLevel) {
            insideParenLevel++;
        } else {
            insideParenLevel = 1;
        }
        expression.putNodeMetaData(INSIDE_PARENTHESES_LEVEL, insideParenLevel);

        return configureAST(expression, ctx);
    }

    @Override
    public Expression visitExpressionInPar(ExpressionInParContext ctx) {
        //return this.visitEnhancedExpression(ctx.enhancedExpression());
        return this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression());
    }

    @Override
    public Expression visitEnhancedStatementExpression(EnhancedStatementExpressionContext ctx) {
        Expression expression;

        if (asBoolean(ctx.statementExpression())) {
            expression = ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression();
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            expression = this.visitStandardLambdaExpression(ctx.standardLambdaExpression());
        } else {
            throw createParsingFailedException("Unsupported enhanced statement expression: " + ctx.getText(), ctx);
        }

        return configureAST(expression, ctx);
    }


    @Override
    public Expression visitPathExpression(PathExpressionContext ctx) {
        return //this.configureAST(
                this.createPathExpression((Expression) this.visit(ctx.primary()), ctx.pathElement());
                //ctx);
    }

    @Override
    public Expression visitPathElement(PathElementContext ctx) {
        Expression baseExpr = ctx.getNodeMetaData(PATH_EXPRESSION_BASE_EXPR);
        Objects.requireNonNull(baseExpr, "baseExpr is required!");

        if (asBoolean(ctx.namePart())) {
            Expression namePartExpr = this.visitNamePart(ctx.namePart());
            GenericsType[] genericsTypes = this.visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());


            if (asBoolean(ctx.DOT())) {
                boolean isSafeChain = isTrue(baseExpr, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN);

                return createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, isSafeChain);
            } else if (asBoolean(ctx.SAFE_DOT())) {
                return createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, true);
            } else if (asBoolean(ctx.SAFE_CHAIN_DOT())) { // e.g. obj??.a  OR obj??.@a
                Expression expression = createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, true);
                expression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN, true);

                return expression;
            } else if (asBoolean(ctx.METHOD_POINTER())) { // e.g. obj.&m
                return configureAST(new MethodPointerExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.METHOD_REFERENCE())) { // e.g. obj::m
                return configureAST(new MethodReferenceExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.SPREAD_DOT())) {
                if (asBoolean(ctx.AT())) { // e.g. obj*.@a
                    AttributeExpression attributeExpression = new AttributeExpression(baseExpr, namePartExpr, true);

                    attributeExpression.setSpreadSafe(true);

                    return configureAST(attributeExpression, ctx);
                } else { // e.g. obj*.p
                    PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, true);
                    propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);

                    propertyExpression.setSpreadSafe(true);

                    return configureAST(propertyExpression, ctx);
                }
            }
        } else if (asBoolean(ctx.creator())) {
            CreatorContext creatorContext = ctx.creator();
            creatorContext.putNodeMetaData(ENCLOSING_INSTANCE_EXPRESSION, baseExpr);

            return configureAST(this.visitCreator(creatorContext), ctx);
        } else if (asBoolean(ctx.indexPropertyArgs())) { // e.g. list[1, 3, 5]
            Tuple2<Token, Expression> tuple = this.visitIndexPropertyArgs(ctx.indexPropertyArgs());
            boolean isSafeChain = isTrue(baseExpr, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN);

            return configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(tuple.getV1()), tuple.getV2(), isSafeChain || asBoolean(ctx.indexPropertyArgs().QUESTION())),
                    ctx);
        } else if (asBoolean(ctx.namedPropertyArgs())) { // this is a special way to signify a cast, e.g. Person[name: 'Daniel.Sun', location: 'Shanghai']
            List<MapEntryExpression> mapEntryExpressionList =
                    this.visitNamedPropertyArgs(ctx.namedPropertyArgs());

            Expression right;
            Expression firstKeyExpression;
            int mapEntryExpressionListSize = mapEntryExpressionList.size();
            if (mapEntryExpressionListSize == 0) {
                // expecting list of MapEntryExpressions later so use SpreadMap to smuggle empty MapExpression to later stages
                right = configureAST(
                        new SpreadMapExpression(configureAST(new MapExpression(), ctx.namedPropertyArgs())),
                        ctx.namedPropertyArgs());
            } else if (mapEntryExpressionListSize == 1 && (firstKeyExpression = mapEntryExpressionList.get(0).getKeyExpression()) instanceof SpreadMapExpression) {
                right = firstKeyExpression;
            } else {
                ListExpression listExpression =
                        configureAST(
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

            return configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(ctx.namedPropertyArgs().LBRACK().getSymbol()), right),
                    ctx);
        } else if (asBoolean(ctx.arguments())) {
            Expression argumentsExpr = this.visitArguments(ctx.arguments());
            configureAST(argumentsExpr, ctx);

            if (isInsideParentheses(baseExpr)) { // e.g. (obj.x)(), (obj.@x)()
                return configureAST(createCallMethodCallExpression(baseExpr, argumentsExpr), ctx);
            }

            if (baseExpr instanceof AttributeExpression) { // e.g. obj.@a(1, 2)
                AttributeExpression attributeExpression = (AttributeExpression) baseExpr;
                attributeExpression.setSpreadSafe(false); // whether attributeExpression is spread safe or not, we must reset it as false

                return configureAST(createCallMethodCallExpression(attributeExpression, argumentsExpr, true), ctx);
            }

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.a(1, 2)
                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression((PropertyExpression) baseExpr, argumentsExpr);

                return configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof VariableExpression) { // void and primitive type AST node must be an instance of VariableExpression
                String baseExprText = baseExpr.getText();
                if (VOID_STR.equals(baseExprText)) { // e.g. void()
                    return configureAST(createCallMethodCallExpression(this.createConstantExpression(baseExpr), argumentsExpr), ctx);
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
                    if (visitingClosureCnt > 0) {
                        return configureAST(
                                new MethodCallExpression(
                                        baseExpr,
                                        baseExprText,
                                        argumentsExpr
                                ),
                                ctx);
                    }

                    return configureAST(
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

                return configureAST(methodCallExpression, ctx);
            }

            // e.g. 1(), 1.1(), ((int) 1 / 2)(1, 2), {a, b -> a + b }(1, 2), m()()
            return configureAST(createCallMethodCallExpression(baseExpr, argumentsExpr), ctx);
        } else if (asBoolean(ctx.closureOrLambdaExpression())) {
            ClosureExpression closureExpression = this.visitClosureOrLambdaExpression(ctx.closureOrLambdaExpression());

            if (baseExpr instanceof MethodCallExpression) {
                MethodCallExpression methodCallExpression = (MethodCallExpression) baseExpr;
                Expression argumentsExpression = methodCallExpression.getArguments();

                if (argumentsExpression instanceof ArgumentListExpression) { // normal arguments, e.g. 1, 2
                    ArgumentListExpression argumentListExpression = (ArgumentListExpression) argumentsExpression;
                    argumentListExpression.getExpressions().add(closureExpression);

                    return configureAST(methodCallExpression, ctx);
                }

                if (argumentsExpression instanceof TupleExpression) { // named arguments, e.g. x: 1, y: 2
                    TupleExpression tupleExpression = (TupleExpression) argumentsExpression;
                    NamedArgumentListExpression namedArgumentListExpression = (NamedArgumentListExpression) tupleExpression.getExpression(0);

                    if (asBoolean(tupleExpression.getExpressions())) {
                        methodCallExpression.setArguments(
                                configureAST(
                                        new ArgumentListExpression(
                                                Stream.of(
                                                        configureAST(
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
                                configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        tupleExpression));
                    }


                    return configureAST(methodCallExpression, ctx);
                }

            }

            // e.g. 1 {}, 1.1 {}
            if (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_NUMERIC)) {
                return configureAST(this.createCallMethodCallExpression(
                        baseExpr,
                        configureAST(
                                new ArgumentListExpression(closureExpression),
                                closureExpression)
                ), ctx);
            }


            if (baseExpr instanceof PropertyExpression) { // e.g. obj.m {  }
                PropertyExpression propertyExpression = (PropertyExpression) baseExpr;

                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression(
                                propertyExpression,
                                configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        closureExpression
                                )
                        );

                return configureAST(methodCallExpression, ctx);
            }

            // e.g.  m { return 1; }
            MethodCallExpression methodCallExpression =
                    new MethodCallExpression(
                            VariableExpression.THIS_EXPRESSION,

                            (baseExpr instanceof VariableExpression)
                                    ? this.createConstantExpression(baseExpr)
                                    : baseExpr,

                            configureAST(
                                    new ArgumentListExpression(closureExpression),
                                    closureExpression)
                    );


            return configureAST(methodCallExpression, ctx);
        }

        throw createParsingFailedException("Unsupported path element: " + ctx.getText(), ctx);
    }

    private Expression createDotExpression(PathElementContext ctx, Expression baseExpr, Expression namePartExpr, GenericsType[] genericsTypes, boolean safe) {
        if (asBoolean(ctx.AT())) { // e.g. obj.@a  OR  obj?.@a
            return configureAST(new AttributeExpression(baseExpr, namePartExpr, safe), ctx);
        } else { // e.g. obj.p  OR  obj?.p
            PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, safe);
            propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);

            return configureAST(propertyExpression, ctx);
        }
    }

    private MethodCallExpression createCallMethodCallExpression(Expression baseExpr, Expression argumentsExpr) {
        return createCallMethodCallExpression(baseExpr, argumentsExpr, false);
    }

    private MethodCallExpression createCallMethodCallExpression(Expression baseExpr, Expression argumentsExpr, boolean implicitThis) {
        MethodCallExpression methodCallExpression =
                new MethodCallExpression(baseExpr, CALL_STR, argumentsExpr);

        methodCallExpression.setImplicitThis(implicitThis);

        return methodCallExpression;
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
            return ClassNode.EMPTY_ARRAY;
        }

        return ctx.type().stream()
                .map(this::visitType)
                .toArray(ClassNode[]::new);
    }

    @Override
    public Expression visitArguments(ArgumentsContext ctx) {
        if (asBoolean(ctx) && asBoolean(ctx.COMMA()) && !asBoolean(ctx.enhancedArgumentList())) {
            throw createParsingFailedException("Expression expected", ctx.COMMA());
        }

        if (!asBoolean(ctx) || !asBoolean(ctx.enhancedArgumentList())) {
            return new ArgumentListExpression();
        }

        return configureAST(this.visitEnhancedArgumentList(ctx.enhancedArgumentList()), ctx);
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
                        MapEntryExpression mapEntryExpression = (MapEntryExpression) e;
                        validateDuplicatedNamedParameter(mapEntryExpressionList, mapEntryExpression);

                        mapEntryExpressionList.add(mapEntryExpression);
                    } else {
                        expressionList.add(e);
                    }
                });

        if (!asBoolean(mapEntryExpressionList)) { // e.g. arguments like  1, 2 OR  someArg, e -> e
            return configureAST(
                    new ArgumentListExpression(expressionList),
                    ctx);
        }

        if (!asBoolean(expressionList)) { // e.g. arguments like  x: 1, y: 2
            return configureAST(
                    new TupleExpression(
                            configureAST(
                                    new NamedArgumentListExpression(mapEntryExpressionList),
                                    ctx)),
                    ctx);
        }

        if (asBoolean(mapEntryExpressionList) && asBoolean(expressionList)) { // e.g. arguments like x: 1, 'a', y: 2, 'b', z: 3
            ArgumentListExpression argumentListExpression = new ArgumentListExpression(expressionList);
            argumentListExpression.getExpressions().add(0, configureAST(new MapExpression(mapEntryExpressionList), ctx));
            return configureAST(argumentListExpression, ctx);
        }

        throw createParsingFailedException("Unsupported argument list: " + ctx.getText(), ctx);
    }

    private void validateDuplicatedNamedParameter(List<MapEntryExpression> mapEntryExpressionList, MapEntryExpression mapEntryExpression) {
        Expression keyExpression = mapEntryExpression.getKeyExpression();

        if (null == keyExpression) {
            return;
        }

        if (isInsideParentheses(keyExpression)) {
            return;
        }

        String parameterName = keyExpression.getText();

        boolean isDuplicatedNamedParameter =
                mapEntryExpressionList.stream()
                        .anyMatch(m -> m.getKeyExpression().getText().equals(parameterName));

        if (!isDuplicatedNamedParameter) {
            return;
        }

        throw createParsingFailedException("Duplicated named parameter '" + parameterName + "' found", mapEntryExpression);
    }

    @Override
    public Expression visitEnhancedArgumentListElement(EnhancedArgumentListElementContext ctx) {
        if (asBoolean(ctx.expressionListElement())) {
            return configureAST(this.visitExpressionListElement(ctx.expressionListElement()), ctx);
        }

        if (asBoolean(ctx.standardLambdaExpression())) {
            return configureAST(this.visitStandardLambdaExpression(ctx.standardLambdaExpression()), ctx);
        }

        if (asBoolean(ctx.mapEntry())) {
            return configureAST(this.visitMapEntry(ctx.mapEntry()), ctx);
        }

        throw createParsingFailedException("Unsupported enhanced argument list element: " + ctx.getText(), ctx);
    }

    @Override
    public ConstantExpression visitStringLiteral(StringLiteralContext ctx) {
        String text = parseStringLiteral(ctx.StringLiteral().getText());

        ConstantExpression constantExpression = new ConstantExpression(text, true);
        constantExpression.putNodeMetaData(IS_STRING, true);

        return configureAST(constantExpression, ctx);
    }

    private String parseStringLiteral(String text) {
        int slashyType = getSlashyType(text);
        boolean startsWithSlash = false;

        if (text.startsWith(TSQ_STR) || text.startsWith(TDQ_STR)) {
            text = StringUtils.removeCR(text); // remove CR in the multiline string

            text = StringUtils.trimQuotations(text, 3);
        } else if (text.startsWith(SQ_STR) || text.startsWith(DQ_STR) || (startsWithSlash = text.startsWith(SLASH_STR))) {
            if (startsWithSlash) { // the slashy string can span rows, so we have to remove CR for it
                text = StringUtils.removeCR(text); // remove CR in the multiline string
            }

            text = StringUtils.trimQuotations(text, 1);
        } else if (text.startsWith(DOLLAR_SLASH_STR)) {
            text = StringUtils.removeCR(text);

            text = StringUtils.trimQuotations(text, 2);
        }

        //handle escapes.
        return StringUtils.replaceEscapes(text, slashyType);
    }

    private int getSlashyType(String text) {
        return text.startsWith(SLASH_STR) ? StringUtils.SLASHY :
                    text.startsWith(DOLLAR_SLASH_STR) ? StringUtils.DOLLAR_SLASHY : StringUtils.NONE_SLASHY;
    }

    @Override
    public Tuple2<Token, Expression> visitIndexPropertyArgs(IndexPropertyArgsContext ctx) {
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

            return tuple(ctx.LBRACK().getSymbol(), indexExpr);
        }

        // e.g. a[1, 2]
        ListExpression listExpression = new ListExpression(expressionList);
        listExpression.setWrapped(true);

        return tuple(ctx.LBRACK().getSymbol(), configureAST(listExpression, ctx));
    }

    @Override
    public List<MapEntryExpression> visitNamedPropertyArgs(NamedPropertyArgsContext ctx) {
        return this.visitMapEntryList(ctx.mapEntryList());
    }

    @Override
    public Expression visitNamePart(NamePartContext ctx) {
        if (asBoolean(ctx.identifier())) {
            return configureAST(new ConstantExpression(this.visitIdentifier(ctx.identifier())), ctx);
        } else if (asBoolean(ctx.stringLiteral())) {
            return configureAST(this.visitStringLiteral(ctx.stringLiteral()), ctx);
        } else if (asBoolean(ctx.dynamicMemberName())) {
            return configureAST(this.visitDynamicMemberName(ctx.dynamicMemberName()), ctx);
        } else if (asBoolean(ctx.keywords())) {
            return configureAST(new ConstantExpression(ctx.keywords().getText()), ctx);
        }

        throw createParsingFailedException("Unsupported name part: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitDynamicMemberName(DynamicMemberNameContext ctx) {
        if (asBoolean(ctx.parExpression())) {
            return configureAST(this.visitParExpression(ctx.parExpression()), ctx);
        } else if (asBoolean(ctx.gstring())) {
            return configureAST(this.visitGstring(ctx.gstring()), ctx);
        }

        throw createParsingFailedException("Unsupported dynamic member name: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitPostfixExpression(PostfixExpressionContext ctx) {
        Expression pathExpr = this.visitPathExpression(ctx.pathExpression());

        if (asBoolean(ctx.op)) {
            PostfixExpression postfixExpression = new PostfixExpression(pathExpr, createGroovyToken(ctx.op));

            if (visitingAssertStatementCnt > 0) {
                // powerassert requires different column for values, so we have to copy the location of op
                return configureAST(postfixExpression, ctx.op);
            } else {
                return configureAST(postfixExpression, ctx);
            }
        }

        return configureAST(pathExpr, ctx);
    }

    @Override
    public Expression visitPostfixExprAlt(PostfixExprAltContext ctx) {
        return this.visitPostfixExpression(ctx.postfixExpression());
    }

    @Override
    public Expression visitUnaryNotExprAlt(UnaryNotExprAltContext ctx) {
        if (asBoolean(ctx.NOT())) {
            return configureAST(
                    new NotExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        if (asBoolean(ctx.BITNOT())) {
            return configureAST(
                    new BitwiseNegationExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        throw createParsingFailedException("Unsupported unary expression: " + ctx.getText(), ctx);
    }

    @Override
    public CastExpression visitCastExprAlt(CastExprAltContext ctx) {
        return configureAST(
                new CastExpression(
                        this.visitCastParExpression(ctx.castParExpression()),
                        (Expression) this.visit(ctx.expression())
                ),
                ctx
        );
    }

    @Override
    public BinaryExpression visitPowerExprAlt(PowerExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public Expression visitUnaryAddExprAlt(UnaryAddExprAltContext ctx) {
        ExpressionContext expressionCtx = ctx.expression();
        Expression expression = (Expression) this.visit(expressionCtx);

        switch (ctx.op.getType()) {
            case ADD: {
                if (isNonStringConstantOutsideParentheses(expression)) {
                    return configureAST(expression, ctx);
                }

                return configureAST(new UnaryPlusExpression(expression), ctx);
            }
            case SUB: {
                if (isNonStringConstantOutsideParentheses(expression)) {
                    ConstantExpression constantExpression = (ConstantExpression) expression;

                    try {
                        String integerLiteralText = constantExpression.getNodeMetaData(INTEGER_LITERAL_TEXT);
                        if (null != integerLiteralText) {

                            ConstantExpression result = new ConstantExpression(Numbers.parseInteger(null, SUB_STR + integerLiteralText));

                            this.numberFormatError = null; // reset the numberFormatError

                            return configureAST(result, ctx);
                        }

                        String floatingPointLiteralText = constantExpression.getNodeMetaData(FLOATING_POINT_LITERAL_TEXT);
                        if (null != floatingPointLiteralText) {
                            ConstantExpression result = new ConstantExpression(Numbers.parseDecimal(SUB_STR + floatingPointLiteralText));

                            this.numberFormatError = null; // reset the numberFormatError

                            return configureAST(result, ctx);
                        }
                    } catch (Exception e) {
                        throw createParsingFailedException(e.getMessage(), ctx);
                    }

                    throw new GroovyBugError("Failed to find the original number literal text: " + constantExpression.getText());
                }

                return configureAST(new UnaryMinusExpression(expression), ctx);
            }

            case INC:
            case DEC:
                return configureAST(new PrefixExpression(this.createGroovyToken(ctx.op), expression), ctx);

            default:
                throw createParsingFailedException("Unsupported unary operation: " + ctx.getText(), ctx);
        }
    }

    private boolean isNonStringConstantOutsideParentheses(Expression expression) {
        return expression instanceof ConstantExpression
                && !(((ConstantExpression) expression).getValue() instanceof String)
                && !isInsideParentheses(expression);
    }

    @Override
    public BinaryExpression visitMultiplicativeExprAlt(MultiplicativeExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitAdditiveExprAlt(AdditiveExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public Expression visitShiftExprAlt(ShiftExprAltContext ctx) {
        Expression left = (Expression) this.visit(ctx.left);
        Expression right = (Expression) this.visit(ctx.right);

        if (asBoolean(ctx.rangeOp)) {
            return configureAST(new RangeExpression(left, right, !ctx.rangeOp.getText().endsWith("<")), ctx);
        }

        org.codehaus.groovy.syntax.Token op;
        Token antlrToken;

        if (asBoolean(ctx.dlOp)) {
            op = this.createGroovyToken(ctx.dlOp, 2);
            antlrToken = ctx.dlOp;
        } else if (asBoolean(ctx.dgOp)) {
            op = this.createGroovyToken(ctx.dgOp, 2);
            antlrToken = ctx.dgOp;
        } else if (asBoolean(ctx.tgOp)) {
            op = this.createGroovyToken(ctx.tgOp, 3);
            antlrToken = ctx.tgOp;
        } else {
            throw createParsingFailedException("Unsupported shift expression: " + ctx.getText(), ctx);
        }

        BinaryExpression binaryExpression = new BinaryExpression(left, op, right);
        if (isTrue(ctx, IS_INSIDE_CONDITIONAL_EXPRESSION)) {
            return configureAST(binaryExpression, antlrToken);
        }

        return configureAST(binaryExpression, ctx);
    }

    @Override
    public Expression visitRelationalExprAlt(RelationalExprAltContext ctx) {
        switch (ctx.op.getType()) {
            case AS:
                return configureAST(
                        CastExpression.asExpression(this.visitType(ctx.type()), (Expression) this.visit(ctx.left)),
                        ctx);

            case INSTANCEOF:
            case NOT_INSTANCEOF:
                ctx.type().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, true);
                return configureAST(
                        new BinaryExpression((Expression) this.visit(ctx.left),
                                this.createGroovyToken(ctx.op),
                                configureAST(new ClassExpression(this.visitType(ctx.type())), ctx.type())),
                        ctx);

            case LE:
            case GE:
            case GT:
            case LT:
            case IN:
            case NOT_IN: {
                if (ctx.op.getType() == IN || ctx.op.getType() == NOT_IN ) {
                    return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
                }

                return configureAST(
                        this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                        ctx);
            }

            default:
                throw createParsingFailedException("Unsupported relational expression: " + ctx.getText(), ctx);
        }
    }

    @Override
    public BinaryExpression visitEqualityExprAlt(EqualityExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitRegexExprAlt(RegexExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitAndExprAlt(AndExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitExclusiveOrExprAlt(ExclusiveOrExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitInclusiveOrExprAlt(InclusiveOrExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitLogicalAndExprAlt(LogicalAndExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitLogicalOrExprAlt(LogicalOrExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public Expression visitConditionalExprAlt(ConditionalExprAltContext ctx) {
        ctx.fb.putNodeMetaData(IS_INSIDE_CONDITIONAL_EXPRESSION, true);

        if (asBoolean(ctx.ELVIS())) { // e.g. a == 6 ?: 0
            return configureAST(
                    new ElvisOperatorExpression((Expression) this.visit(ctx.con), (Expression) this.visit(ctx.fb)),
                    ctx);
        }

        ctx.tb.putNodeMetaData(IS_INSIDE_CONDITIONAL_EXPRESSION, true);

        return configureAST(
                new TernaryExpression(
                        configureAST(new BooleanExpression((Expression) this.visit(ctx.con)),
                                ctx.con),
                        (Expression) this.visit(ctx.tb),
                        (Expression) this.visit(ctx.fb)),
                ctx);
    }

    @Override
    public BinaryExpression visitMultipleAssignmentExprAlt(MultipleAssignmentExprAltContext ctx) {
        return configureAST(
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
                && isInsideParentheses(leftExpr)) { // it is a special multiple assignment whose variable count is only one, e.g. (a) = [1]

            if ((Integer) leftExpr.getNodeMetaData(INSIDE_PARENTHESES_LEVEL) > 1) {
                throw createParsingFailedException("Nested parenthesis is not allowed in multiple assignment, e.g. ((a)) = b", ctx);
            }

            return configureAST(
                    new BinaryExpression(
                            configureAST(new TupleExpression(leftExpr), ctx.left),
                            this.createGroovyToken(ctx.op),
                            this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression())),
                    ctx);
        }

        // the LHS expression should be a variable which is not inside any parentheses
        if (
                !(
                        (leftExpr instanceof VariableExpression
//                                && !(THIS_STR.equals(leftExpr.getText()) || SUPER_STR.equals(leftExpr.getText()))     // commented, e.g. this = value // this will be transformed to $this
                                && !isInsideParentheses(leftExpr)) // e.g. p = 123

                                || leftExpr instanceof PropertyExpression // e.g. obj.p = 123

                                || (leftExpr instanceof BinaryExpression
//                                && !(((BinaryExpression) leftExpr).getRightExpression() instanceof ListExpression)    // commented, e.g. list[1, 2] = [11, 12]
                                && Types.LEFT_SQUARE_BRACKET == ((BinaryExpression) leftExpr).getOperation().getType()) // e.g. map[a] = 123 OR map['a'] = 123 OR map["$a"] = 123
                )

                ) {

            throw createParsingFailedException("The LHS of an assignment should be a variable or a field accessing expression", ctx);
        }

        return configureAST(
                new BinaryExpression(
                        leftExpr,
                        this.createGroovyToken(ctx.op),
                        this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression())),
                ctx);
    }

// } expression    --------------------------------------------------------------------


    // primary {       --------------------------------------------------------------------
    @Override
    public Expression visitIdentifierPrmrAlt(IdentifierPrmrAltContext ctx) {
        if (asBoolean(ctx.typeArguments())) {
            ClassNode classNode = ClassHelper.make(ctx.identifier().getText());

            classNode.setGenericsTypes(
                    this.visitTypeArguments(ctx.typeArguments()));

            return configureAST(new ClassExpression(classNode), ctx);
        }

        return configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public ConstantExpression visitLiteralPrmrAlt(LiteralPrmrAltContext ctx) {
        return configureAST((ConstantExpression) this.visit(ctx.literal()), ctx);
    }

    @Override
    public GStringExpression visitGstringPrmrAlt(GstringPrmrAltContext ctx) {
        return configureAST((GStringExpression) this.visit(ctx.gstring()), ctx);
    }

    @Override
    public Expression visitNewPrmrAlt(NewPrmrAltContext ctx) {
        return configureAST(this.visitCreator(ctx.creator()), ctx);
    }

    @Override
    public VariableExpression visitThisPrmrAlt(ThisPrmrAltContext ctx) {
        return configureAST(new VariableExpression(ctx.THIS().getText()), ctx);
    }

    @Override
    public VariableExpression visitSuperPrmrAlt(SuperPrmrAltContext ctx) {
        return configureAST(new VariableExpression(ctx.SUPER().getText()), ctx);
    }


    @Override
    public Expression visitParenPrmrAlt(ParenPrmrAltContext ctx) {
        return configureAST(this.visitParExpression(ctx.parExpression()), ctx);
    }

    @Override
    public ClosureExpression visitClosureOrLambdaExpressionPrmrAlt(ClosureOrLambdaExpressionPrmrAltContext ctx) {
        return configureAST(this.visitClosureOrLambdaExpression(ctx.closureOrLambdaExpression()), ctx);
    }

    @Override
    public ListExpression visitListPrmrAlt(ListPrmrAltContext ctx) {
        return configureAST(
                this.visitList(ctx.list()),
                ctx);
    }

    @Override
    public MapExpression visitMapPrmrAlt(MapPrmrAltContext ctx) {
        return configureAST(this.visitMap(ctx.map()), ctx);
    }

    @Override
    public VariableExpression visitBuiltInTypePrmrAlt(BuiltInTypePrmrAltContext ctx) {
        return configureAST(this.visitBuiltInType(ctx.builtInType()), ctx);
    }


// } primary       --------------------------------------------------------------------

    @Override
    public Expression visitCreator(CreatorContext ctx) {
        ClassNode classNode = this.visitCreatedName(ctx.createdName());

        if (asBoolean(ctx.arguments())) { // create instance of class
            Expression arguments = this.visitArguments(ctx.arguments());
            Expression enclosingInstanceExpression = ctx.getNodeMetaData(ENCLOSING_INSTANCE_EXPRESSION);

            if (null != enclosingInstanceExpression) {
                if (arguments instanceof ArgumentListExpression) {
                    ((ArgumentListExpression) arguments).getExpressions().add(0, enclosingInstanceExpression);
                } else if (arguments instanceof TupleExpression) {
                    throw createParsingFailedException("Creating instance of non-static class does not support named parameters", arguments);
                } else if (arguments instanceof NamedArgumentListExpression) {
                    throw createParsingFailedException("Unexpected arguments", arguments);
                } else {
                    throw createParsingFailedException("Unsupported arguments", arguments); // should never reach here
                }
            }

            if (asBoolean(ctx.anonymousInnerClassDeclaration())) {
                ctx.anonymousInnerClassDeclaration().putNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS, classNode);
                InnerClassNode anonymousInnerClassNode = this.visitAnonymousInnerClassDeclaration(ctx.anonymousInnerClassDeclaration());

                List<InnerClassNode> anonymousInnerClassList = anonymousInnerClassesDefinedInMethodStack.peek();
                if (null != anonymousInnerClassList) { // if the anonymous class is created in a script, no anonymousInnerClassList is available.
                    anonymousInnerClassList.add(anonymousInnerClassNode);
                }

                ConstructorCallExpression constructorCallExpression = new ConstructorCallExpression(anonymousInnerClassNode, arguments);
                constructorCallExpression.setUsingAnonymousInnerClass(true);

                return configureAST(constructorCallExpression, ctx);
            }

            return configureAST(
                    new ConstructorCallExpression(classNode, arguments),
                    ctx);
        }

        if (asBoolean(ctx.LBRACK()) || asBoolean(ctx.dims())) { // create array
            ArrayExpression arrayExpression;
            List<List<AnnotationNode>> allDimList;

            if (asBoolean(ctx.arrayInitializer())) {
                ClassNode elementType = classNode;
                allDimList = this.visitDims(ctx.dims());

                for (int i = 0, n = allDimList.size() - 1; i < n; i++) {
                    elementType = elementType.makeArray();
                }

                arrayExpression =
                        new ArrayExpression(
                            elementType,
                            this.visitArrayInitializer(ctx.arrayInitializer()));

            } else {
                Expression[] empties;
                List<List<AnnotationNode>> emptyDimList = this.visitDimsOpt(ctx.dimsOpt());

                if (asBoolean(emptyDimList)) {
                    empties = new Expression[emptyDimList.size()];
                    Arrays.fill(empties, ConstantExpression.EMPTY_EXPRESSION);
                } else {
                    empties = Expression.EMPTY_ARRAY;
                }

                arrayExpression =
                        new ArrayExpression(
                                classNode,
                                null,
                                Stream.concat(
                                        ctx.expression().stream()
                                                .map(e -> (Expression) this.visit(e)),
                                        Arrays.stream(empties)
                                ).collect(Collectors.toList()));


                List<List<AnnotationNode>> exprDimList = ctx.annotationsOpt().stream().map(this::visitAnnotationsOpt).collect(Collectors.toList());
                allDimList = new ArrayList<>(exprDimList);
                Collections.reverse(emptyDimList);
                allDimList.addAll(emptyDimList);
                Collections.reverse(allDimList);
            }

            arrayExpression.setType(createArrayType(classNode, allDimList));

            return configureAST(arrayExpression, ctx);
        }

        throw createParsingFailedException("Unsupported creator: " + ctx.getText(), ctx);
    }

    private ClassNode createArrayType(ClassNode classNode, List<List<AnnotationNode>> dimList) {
        ClassNode arrayType = classNode;
        for (int i = 0, n = dimList.size(); i < n; i++) {
            arrayType = arrayType.makeArray();
            arrayType.addAnnotations(dimList.get(i));
        }
        return arrayType;
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
        anonymousInnerClass.putNodeMetaData(CLASS_NAME, fullName);
        configureAST(anonymousInnerClass, ctx);

        classNodeStack.push(anonymousInnerClass);
        ctx.classBody().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, anonymousInnerClass);
        this.visitClassBody(ctx.classBody());
        classNodeStack.pop();

        classNodeList.add(anonymousInnerClass);

        return anonymousInnerClass;
    }


    @Override
    public ClassNode visitCreatedName(CreatedNameContext ctx) {
        ClassNode classNode = null;

        if (asBoolean(ctx.qualifiedClassName())) {
            classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());

            if (asBoolean(ctx.typeArgumentsOrDiamond())) {
                classNode.setGenericsTypes(
                        this.visitTypeArgumentsOrDiamond(ctx.typeArgumentsOrDiamond()));
            }

            classNode = configureAST(classNode, ctx);
        } else if (asBoolean(ctx.primitiveType())) {
            classNode = configureAST(
                    this.visitPrimitiveType(ctx.primitiveType()),
                    ctx);
        }

        if (!asBoolean(classNode)) {
            throw createParsingFailedException("Unsupported created name: " + ctx.getText(), ctx);
        }

        classNode.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        return classNode;
    }


    @Override
    public MapExpression visitMap(MapContext ctx) {
        return configureAST(
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
            keyExpr = configureAST(new SpreadMapExpression(valueExpr), ctx);
        } else if (asBoolean(ctx.mapEntryLabel())) {
            keyExpr = this.visitMapEntryLabel(ctx.mapEntryLabel());
        } else {
            throw createParsingFailedException("Unsupported map entry: " + ctx.getText(), ctx);
        }

        return configureAST(
                new MapEntryExpression(keyExpr, valueExpr),
                ctx);
    }

    @Override
    public Expression visitMapEntryLabel(MapEntryLabelContext ctx) {
        if (asBoolean(ctx.keywords())) {
            return configureAST(this.visitKeywords(ctx.keywords()), ctx);
        } else if (asBoolean(ctx.primary())) {
            Expression expression = (Expression) this.visit(ctx.primary());

            // if the key is variable and not inside parentheses, convert it to a constant, e.g. [a:1, b:2]
            if (expression instanceof VariableExpression && !isInsideParentheses(expression)) {
                expression =
                        configureAST(
                                new ConstantExpression(((VariableExpression) expression).getName()),
                                expression);
            }

            return configureAST(expression, ctx);
        }

        throw createParsingFailedException("Unsupported map entry label: " + ctx.getText(), ctx);
    }

    @Override
    public ConstantExpression visitKeywords(KeywordsContext ctx) {
        return configureAST(new ConstantExpression(ctx.getText()), ctx);
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

        return configureAST(new VariableExpression(text), ctx);
    }

    @Override
    public ListExpression visitList(ListContext ctx) {
        if (asBoolean(ctx.COMMA()) && !asBoolean(ctx.expressionList())) {
            throw createParsingFailedException("Empty list constructor should not contain any comma(,)", ctx.COMMA());
        }

        return configureAST(
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

        validateExpressionListElement(ctx, expression);

        if (asBoolean(ctx.MUL())) {
            return configureAST(new SpreadExpression(expression), ctx);
        }

        return configureAST(expression, ctx);
    }

    private void validateExpressionListElement(ExpressionListElementContext ctx, Expression expression) {
        if (!(expression instanceof MethodCallExpression && isTrue(expression, IS_COMMAND_EXPRESSION))) {
            return;
        }

        // statements like `foo(String a)` is invalid
        MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
        String methodName = methodCallExpression.getMethodAsString();
        if (methodCallExpression.isImplicitThis() && Character.isUpperCase(methodName.codePointAt(0)) || PRIMITIVE_TYPE_SET.contains(methodName)) {
            throw createParsingFailedException("Invalid method declaration", ctx);
        }
    }


    // literal {       --------------------------------------------------------------------
    @Override
    public ConstantExpression visitIntegerLiteralAlt(IntegerLiteralAltContext ctx) {
        String text = ctx.IntegerLiteral().getText();

        Number num = null;
        try {
            num = Numbers.parseInteger(null, text);
        } catch (Exception e) {
            this.numberFormatError = tuple(ctx, e);
        }

        ConstantExpression constantExpression = new ConstantExpression(num, !text.startsWith(SUB_STR));
        constantExpression.putNodeMetaData(IS_NUMERIC, true);
        constantExpression.putNodeMetaData(INTEGER_LITERAL_TEXT, text);

        return configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitFloatingPointLiteralAlt(FloatingPointLiteralAltContext ctx) {
        String text = ctx.FloatingPointLiteral().getText();

        Number num = null;
        try {
            num = Numbers.parseDecimal(text);
        } catch (Exception e) {
            this.numberFormatError = tuple(ctx, e);
        }

        ConstantExpression constantExpression = new ConstantExpression(num, !text.startsWith(SUB_STR));
        constantExpression.putNodeMetaData(IS_NUMERIC, true);
        constantExpression.putNodeMetaData(FLOATING_POINT_LITERAL_TEXT, text);

        return configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitStringLiteralAlt(StringLiteralAltContext ctx) {
        return configureAST(
                this.visitStringLiteral(ctx.stringLiteral()),
                ctx);
    }

    @Override
    public ConstantExpression visitBooleanLiteralAlt(BooleanLiteralAltContext ctx) {
        return configureAST(new ConstantExpression("true".equals(ctx.BooleanLiteral().getText()), true), ctx);
    }

    @Override
    public ConstantExpression visitNullLiteralAlt(NullLiteralAltContext ctx) {
        return configureAST(new ConstantExpression(null), ctx);
    }


// } literal       --------------------------------------------------------------------


    // gstring {       --------------------------------------------------------------------
    @Override
    public GStringExpression visitGstring(GstringContext ctx) {
        final List<ConstantExpression> stringLiteralList = new LinkedList<>();
        final String begin = ctx.GStringBegin().getText();
        final String beginQuotation = beginQuotation(begin);
        stringLiteralList.add(configureAST(new ConstantExpression(parseGStringBegin(ctx, beginQuotation)), ctx.GStringBegin()));

        List<ConstantExpression> partStrings =
                ctx.GStringPart().stream()
                        .map(e -> configureAST(new ConstantExpression(parseGStringPart(e, beginQuotation)), e))
                        .collect(Collectors.toList());
        stringLiteralList.addAll(partStrings);

        stringLiteralList.add(configureAST(new ConstantExpression(parseGStringEnd(ctx, beginQuotation)), ctx.GStringEnd()));

        List<Expression> values = ctx.gstringValue().stream()
                .map(e -> {
                    Expression expression = this.visitGstringValue(e);

                    if (expression instanceof ClosureExpression && !hasArrow(e)) {
                        List<Statement> statementList = ((BlockStatement) ((ClosureExpression) expression).getCode()).getStatements();

                        if (statementList.stream().noneMatch(DefaultGroovyMethods::asBoolean)) {
                            return configureAST(new ConstantExpression(null), e);
                        }

                        return configureAST(this.createCallMethodCallExpression(expression, new ArgumentListExpression(), true), e);
                    }

                    return expression;
                })
                .collect(Collectors.toList());

        StringBuilder verbatimText = new StringBuilder(ctx.getText().length());
        for (int i = 0, n = stringLiteralList.size(), s = values.size(); i < n; i++) {
            verbatimText.append(stringLiteralList.get(i).getValue());

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

        return configureAST(new GStringExpression(verbatimText.toString(), stringLiteralList, values), ctx);
    }

    private boolean hasArrow(GstringValueContext e) {
        return asBoolean(e.closure().ARROW());
    }

    private String parseGStringEnd(GstringContext ctx, String beginQuotation) {
        StringBuilder text = new StringBuilder(ctx.GStringEnd().getText());
        text.insert(0, beginQuotation);

        return this.parseStringLiteral(text.toString());
    }

    private String parseGStringPart(TerminalNode e, String beginQuotation) {
        StringBuilder text = new StringBuilder(e.getText());
        text.deleteCharAt(text.length() - 1);  // remove the tailing $
        text.insert(0, beginQuotation).append(QUOTATION_MAP.get(beginQuotation));

        return this.parseStringLiteral(text.toString());
    }

    private String parseGStringBegin(GstringContext ctx, String beginQuotation) {
        StringBuilder text = new StringBuilder(ctx.GStringBegin().getText());
        text.deleteCharAt(text.length() - 1);  // remove the tailing $
        text.append(QUOTATION_MAP.get(beginQuotation));

        return this.parseStringLiteral(text.toString());
    }

    private String beginQuotation(String text) {
        if (text.startsWith(TDQ_STR)) {
            return TDQ_STR;
        } else if (text.startsWith(DQ_STR)) {
            return DQ_STR;
        } else if (text.startsWith(SLASH_STR)) {
            return SLASH_STR;
        } else if (text.startsWith(DOLLAR_SLASH_STR)) {
            return DOLLAR_SLASH_STR;
        } else {
            return String.valueOf(text.charAt(0));
        }
    }

    @Override
    public Expression visitGstringValue(GstringValueContext ctx) {
        if (asBoolean(ctx.gstringPath())) {
            return configureAST(this.visitGstringPath(ctx.gstringPath()), ctx);
        }

        if (asBoolean(ctx.LBRACE())) {
            if (asBoolean(ctx.statementExpression())) {
                return configureAST(((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression(), ctx.statementExpression());
            } else { // e.g. "${}"
                return configureAST(new ConstantExpression(null), ctx);
            }
        }

        if (asBoolean(ctx.closure())) {
            return configureAST(this.visitClosure(ctx.closure()), ctx);
        }

        throw createParsingFailedException("Unsupported gstring value: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitGstringPath(GstringPathContext ctx) {
        VariableExpression variableExpression = new VariableExpression(this.visitIdentifier(ctx.identifier()));

        if (asBoolean(ctx.GStringPathPart())) {
            Expression propertyExpression = ctx.GStringPathPart().stream()
                    .map(e -> configureAST((Expression) new ConstantExpression(e.getText().substring(1)), e))
                    .reduce(configureAST(variableExpression, ctx.identifier()), (r, e) -> configureAST(new PropertyExpression(r, e), e));

            return configureAST(propertyExpression, ctx);
        }

        return configureAST(variableExpression, ctx);
    }
// } gstring       --------------------------------------------------------------------

    @Override
    public LambdaExpression visitStandardLambdaExpression(StandardLambdaExpressionContext ctx) {
        return configureAST(this.createLambda(ctx.standardLambdaParameters(), ctx.lambdaBody()), ctx);
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
                    configureAST(
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
            return configureAST((ExpressionStatement) this.visit(ctx.statementExpression()), ctx);
        }

        if (asBoolean(ctx.block())) {
            return configureAST(this.visitBlock(ctx.block()), ctx);
        }

        throw createParsingFailedException("Unsupported lambda body: " + ctx.getText(), ctx);
    }

    @Override
    public ClosureExpression visitClosure(ClosureContext ctx) {
        visitingClosureCnt++;

        Parameter[] parameters = asBoolean(ctx.formalParameterList())
                ? this.visitFormalParameterList(ctx.formalParameterList())
                : null;

        if (!asBoolean(ctx.ARROW())) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        Statement code = this.visitBlockStatementsOpt(ctx.blockStatementsOpt());
        ClosureExpression result = configureAST(new ClosureExpression(parameters, code), ctx);

        visitingClosureCnt--;

        return result;
    }

    @Override
    public Parameter[] visitFormalParameters(FormalParametersContext ctx) {
        if (!asBoolean(ctx)) {
            return Parameter.EMPTY_ARRAY;
        }

        return this.visitFormalParameterList(ctx.formalParameterList());
    }

    @Override
    public Parameter[] visitFormalParameterList(FormalParameterListContext ctx) {
        if (!asBoolean(ctx)) {
            return Parameter.EMPTY_ARRAY;
        }

        List<Parameter> parameterList = new LinkedList<>();

        if (asBoolean(ctx.thisFormalParameter())) {
            parameterList.add(this.visitThisFormalParameter(ctx.thisFormalParameter()));
        }

        List<? extends FormalParameterContext> formalParameterList = ctx.formalParameter();
        if (asBoolean(formalParameterList)) {
            validateVarArgParameter(formalParameterList);

            parameterList.addAll(
                    formalParameterList.stream()
                            .map(this::visitFormalParameter)
                            .collect(Collectors.toList()));
        }

        validateParameterList(parameterList);

        return parameterList.toArray(Parameter.EMPTY_ARRAY);
    }

    private void validateVarArgParameter(List<? extends FormalParameterContext> formalParameterList) {
        for (int i = 0, n = formalParameterList.size(); i < n - 1; i++) {
            FormalParameterContext formalParameterContext = formalParameterList.get(i);
            if (asBoolean(formalParameterContext.ELLIPSIS())) {
                throw createParsingFailedException("The var-arg parameter strs must be the last parameter", formalParameterContext);
            }
        }
    }

    private void validateParameterList(List<Parameter> parameterList) {
        for (int n = parameterList.size(), i = n - 1; i >= 0; i--) {
            Parameter parameter = parameterList.get(i);

            for (Parameter otherParameter : parameterList) {
                if (otherParameter == parameter) {
                    continue;
                }

                if (otherParameter.getName().equals(parameter.getName())) {
                    throw createParsingFailedException("Duplicated parameter '" + parameter.getName() + "' found.", parameter);
                }
            }
        }
    }

    @Override
    public Parameter visitFormalParameter(FormalParameterContext ctx) {
        return this.processFormalParameter(ctx, ctx.variableModifiersOpt(), ctx.type(), ctx.ELLIPSIS(), ctx.variableDeclaratorId(), ctx.expression());
    }

    @Override
    public Parameter visitThisFormalParameter(ThisFormalParameterContext ctx) {
        return configureAST(new Parameter(this.visitType(ctx.type()), THIS_STR), ctx);
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
            return configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported class or interface modifier: " + ctx.getText(), ctx);
    }

    @Override
    public ModifierNode visitModifier(ModifierContext ctx) {
        if (asBoolean(ctx.classOrInterfaceModifier())) {
            return configureAST(this.visitClassOrInterfaceModifier(ctx.classOrInterfaceModifier()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
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
            return configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
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

    @Override
    public List<List<AnnotationNode>> visitDims(DimsContext ctx) {
        List<List<AnnotationNode>> dimList =
                ctx.annotationsOpt().stream()
                        .map(this::visitAnnotationsOpt)
                        .collect(Collectors.toList());

        Collections.reverse(dimList);

        return dimList;
    }

    @Override
    public List<List<AnnotationNode>> visitDimsOpt(DimsOptContext ctx) {
        if (!asBoolean(ctx.dims())) {
            return Collections.emptyList();
        }

        return this.visitDims(ctx.dims());
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
        } else if (asBoolean(ctx.primitiveType())) {
            classNode = this.visitPrimitiveType(ctx.primitiveType());
        }

        if (!asBoolean(classNode)) {
            if (VOID_STR.equals(ctx.getText())) { // TODO refine error message for `void`
                throw createParsingFailedException("void is not allowed here", ctx);
            }

            throw createParsingFailedException("Unsupported type: " + ctx.getText(), ctx);
        }

        classNode.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        List<List<AnnotationNode>> dimList = this.visitDimsOpt(ctx.dimsOpt());
        if (asBoolean(dimList)) {
            // clear array's generics type info. Groovy's bug? array's generics type will be ignored. e.g. List<String>[]... p
            classNode.setGenericsTypes(null);
            classNode.setUsingGenerics(false);

            classNode = this.createArrayType(classNode, dimList);
        }

        return configureAST(classNode, ctx);
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

        return configureAST(classNode, ctx);
    }

    @Override
    public GenericsType[] visitTypeArgumentsOrDiamond(TypeArgumentsOrDiamondContext ctx) {
        if (asBoolean(ctx.typeArguments())) {
            return this.visitTypeArguments(ctx.typeArguments());
        }

        if (asBoolean(ctx.LT())) { // e.g. <>
            return GenericsType.EMPTY_ARRAY;
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
            ClassNode baseType = configureAST(ClassHelper.makeWithoutCaching(QUESTION_STR), ctx.QUESTION());

            baseType.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

            if (!asBoolean(ctx.type())) {
                GenericsType genericsType = new GenericsType(baseType);
                genericsType.setWildcard(true);
                genericsType.setName(QUESTION_STR);

                return configureAST(genericsType, ctx);
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

            return configureAST(genericsType, ctx);
        } else if (asBoolean(ctx.type())) {
            return configureAST(
                    this.createGenericsType(
                            this.visitType(ctx.type())),
                    ctx);
        }

        throw createParsingFailedException("Unsupported type argument: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitPrimitiveType(PrimitiveTypeContext ctx) {
        return configureAST(ClassHelper.make(ctx.getText()), ctx);
    }
// } type       --------------------------------------------------------------------

    @Override
    public VariableExpression visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        return configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public TupleExpression visitVariableNames(VariableNamesContext ctx) {
        return configureAST(
                new TupleExpression(
                        ctx.variableDeclaratorId().stream()
                                .map(this::visitVariableDeclaratorId)
                                .collect(Collectors.toList())
                ),
                ctx);
    }

    @Override
    public ClosureExpression visitClosureOrLambdaExpression(ClosureOrLambdaExpressionContext ctx) {
        // GROOVY-8991: Difference in behaviour with closure and lambda
        if (asBoolean(ctx.closure())) {
            return configureAST(this.visitClosure(ctx.closure()), ctx);
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            return configureAST(this.visitStandardLambdaExpression(ctx.standardLambdaExpression()), ctx);
        }

        // should never reach here
        throw createParsingFailedException("The node is not expected here" + ctx.getText(), ctx);
    }

    @Override
    public BlockStatement visitBlockStatementsOpt(BlockStatementsOptContext ctx) {
        if (asBoolean(ctx.blockStatements())) {
            return configureAST(this.visitBlockStatements(ctx.blockStatements()), ctx);
        }

        return configureAST(this.createBlockStatement(), ctx);
    }

    @Override
    public BlockStatement visitBlockStatements(BlockStatementsContext ctx) {
        return configureAST(
                this.createBlockStatement(
                        ctx.blockStatement().stream()
                                .map(this::visitBlockStatement)
                                .filter(DefaultGroovyMethods::asBoolean)
                                .collect(Collectors.toList())),
                ctx);
    }

    @Override
    public Statement visitBlockStatement(BlockStatementContext ctx) {
        if (asBoolean(ctx.localVariableDeclaration())) {
            return configureAST(this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()), ctx);
        }

        if (asBoolean(ctx.statement())) {
            Object astNode = this.visit(ctx.statement()); //this.configureAST((Statement) this.visit(ctx.statement()), ctx);

            if (null == astNode) {
                return null;
            }

            if (astNode instanceof Statement) {
                return (Statement) astNode;
            } else if (astNode instanceof MethodNode) {
                throw createParsingFailedException("Method definition not expected here", ctx);
            } else if (astNode instanceof ImportNode) {
                throw createParsingFailedException("Import statement not expected here", ctx);
            } else {
                throw createParsingFailedException("The statement(" + astNode.getClass() + ") not expected here", ctx);
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
        List<Tuple2<String, Expression>> annotationElementValues = this.visitElementValues(ctx.elementValues());

        annotationElementValues.forEach(e -> annotationNode.addMember(e.getV1(), e.getV2()));

        return configureAST(annotationNode, ctx);
    }

    @Override
    public List<Tuple2<String, Expression>> visitElementValues(ElementValuesContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        List<Tuple2<String, Expression>> annotationElementValues = new LinkedList<>();

        if (asBoolean(ctx.elementValuePairs())) {
            this.visitElementValuePairs(ctx.elementValuePairs()).forEach((key, value) -> annotationElementValues.add(tuple(key, value)));
        } else if (asBoolean(ctx.elementValue())) {
            annotationElementValues.add(tuple(VALUE_STR, this.visitElementValue(ctx.elementValue())));
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
                        Tuple2::getV1,
                        Tuple2::getV2,
                        (k, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", k));
                        },
                        LinkedHashMap::new
                ));
    }

    @Override
    public Tuple2<String, Expression> visitElementValuePair(ElementValuePairContext ctx) {
        return tuple(ctx.elementValuePairName().getText(), this.visitElementValue(ctx.elementValue()));
    }

    @Override
    public Expression visitElementValue(ElementValueContext ctx) {
        if (asBoolean(ctx.expression())) {
            return configureAST((Expression) this.visit(ctx.expression()), ctx);
        }

        if (asBoolean(ctx.annotation())) {
            return configureAST(new AnnotationConstantExpression(this.visitAnnotation(ctx.annotation())), ctx);
        }

        if (asBoolean(ctx.elementValueArrayInitializer())) {
            return configureAST(this.visitElementValueArrayInitializer(ctx.elementValueArrayInitializer()), ctx);
        }

        throw createParsingFailedException("Unsupported element value: " + ctx.getText(), ctx);
    }

    @Override
    public ListExpression visitElementValueArrayInitializer(ElementValueArrayInitializerContext ctx) {
        return configureAST(new ListExpression(ctx.elementValue().stream().map(this::visitElementValue).collect(Collectors.toList())), ctx);
    }

    @Override
    public String visitClassName(ClassNameContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitIdentifier(IdentifierContext ctx) {
        return ctx.getText();
    }


    @Override
    public String visitQualifiedName(QualifiedNameContext ctx) {
        return ctx.qualifiedNameElement().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining(DOT_STR));
    }

    @Override
    public ClassNode visitAnnotatedQualifiedClassName(AnnotatedQualifiedClassNameContext ctx) {
        ClassNode classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());

        classNode.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        return classNode;
    }

    @Override
    public ClassNode[] visitQualifiedClassNameList(QualifiedClassNameListContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassNode.EMPTY_ARRAY;
        }

        return ctx.annotatedQualifiedClassName().stream()
                .map(this::visitAnnotatedQualifiedClassName)
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

        return configureAST(result, ctx);
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
        return new MethodCallExpression(
                VariableExpression.THIS_EXPRESSION,

                (baseExpr instanceof VariableExpression)
                        ? this.createConstantExpression(baseExpr)
                        : baseExpr,

                arguments
        );
    }

    private Parameter processFormalParameter(GroovyParserRuleContext ctx,
                                             VariableModifiersOptContext variableModifiersOptContext,
                                             TypeContext typeContext,
                                             TerminalNode ellipsis,
                                             VariableDeclaratorIdContext variableDeclaratorIdContext,
                                             ExpressionContext expressionContext) {

        ClassNode classNode = this.visitType(typeContext);

        if (asBoolean(ellipsis)) {
            classNode = configureAST(classNode.makeArray(), classNode);
        }

        Parameter parameter =
                new ModifierManager(this, this.visitVariableModifiersOpt(variableModifiersOptContext))
                        .processParameter(
                                configureAST(
                                        new Parameter(
                                                classNode,
                                                this.visitVariableDeclaratorId(variableDeclaratorIdContext).getName()
                                        ),
                                        ctx
                                )
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
                            Expression expression = this.visitPathElement(pathElementContext);

                            boolean isSafeChain = isTrue((Expression) r, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN);
                            if (isSafeChain) {
                                expression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN, true);
                            }

                            return expression;
                        }
                );
    }

    private GenericsType createGenericsType(ClassNode classNode) {
        return configureAST(new GenericsType(classNode), classNode);
    }

    private ConstantExpression createConstantExpression(Expression expression) {
        if (expression instanceof ConstantExpression) {
            return (ConstantExpression) expression;
        }

        return configureAST(new ConstantExpression(expression.getText()), expression);
    }

    private BinaryExpression createBinaryExpression(ExpressionContext left, Token op, ExpressionContext right) {
        return new BinaryExpression((Expression) this.visit(left), this.createGroovyToken(op), (Expression) this.visit(right));
    }

    private BinaryExpression createBinaryExpression(ExpressionContext left, Token op, ExpressionContext right, ExpressionContext ctx) {
        BinaryExpression binaryExpression = this.createBinaryExpression(left, op, right);

        if (isTrue(ctx, IS_INSIDE_CONDITIONAL_EXPRESSION)) {
            return configureAST(binaryExpression, op);
        }

        return configureAST(binaryExpression, ctx);
    }

    private Statement unpackStatement(Statement statement) {
        if (statement instanceof DeclarationListStatement) {
            List<ExpressionStatement> expressionStatementList = ((DeclarationListStatement) statement).getDeclarationStatements();

            if (1 == expressionStatementList.size()) {
                return expressionStatementList.get(0);
            }

            return configureAST(this.createBlockStatement(statement), statement); // if DeclarationListStatement contains more than 1 declarations, maybe it's better to create a block to hold them
        }

        return statement;
    }

    BlockStatement createBlockStatement(Statement... statements) {
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
            ModifierManager modifierManager) {
        if (modifierManager.containsVisibilityModifier()) {
            return false;
        }

        if (isAnnotationDeclaration) {
            return true;
        }

        if (hasReturnType && (modifierManager.containsAny(DEF, VAR))) {
            return true;
        }

        if (!hasReturnType || modifierManager.containsNonVisibilityModifier() || modifierManager.containsAnnotations()) {
            return true;
        }

        return isAnonymousInnerEnumDeclaration;
    }

    // the mixins of interface and annotation should be null
    private void hackMixins(ClassNode classNode) {
        classNode.setMixins(null);
    }

    private static final Map<ClassNode, Object> TYPE_DEFAULT_VALUE_MAP = Maps.of(
            ClassHelper.int_TYPE, 0,
            ClassHelper.long_TYPE, 0L,
            ClassHelper.double_TYPE, 0.0D,
            ClassHelper.float_TYPE, 0.0F,
            ClassHelper.short_TYPE, (short) 0,
            ClassHelper.byte_TYPE, (byte) 0,
            ClassHelper.char_TYPE, (char) 0,
            ClassHelper.boolean_TYPE, Boolean.FALSE
    );

    private Object findDefaultValueByType(ClassNode type) {
        return TYPE_DEFAULT_VALUE_MAP.get(type);
    }

    private boolean isPackageInfoDeclaration() {
        String name = this.sourceUnit.getName();

        return null != name && name.endsWith(PACKAGE_INFO_FILE_NAME);

    }

    private boolean isBlankScript() {
        return moduleNode.getStatementBlock().isEmpty() && moduleNode.getMethods().isEmpty() && moduleNode.getClasses().isEmpty();
    }

    private boolean isInsideParentheses(NodeMetaDataHandler nodeMetaDataHandler) {
        Integer insideParenLevel = nodeMetaDataHandler.getNodeMetaData(INSIDE_PARENTHESES_LEVEL);

        return null != insideParenLevel && insideParenLevel > 0;

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

    private String getOriginalText(ParserRuleContext context) {
        CharStream charStream = lexer.getInputStream();
        return charStream.getText(Interval.of(context.getStart().getStartIndex(), context.getStop().getStopIndex()));

    }

    private boolean isTrue(NodeMetaDataHandler nodeMetaDataHandler, String key) {
        Object nmd = nodeMetaDataHandler.getNodeMetaData(key);

        if (null == nmd) {
            return false;
        }

        if (!(nmd instanceof Boolean)) {
            throw new GroovyBugError(nodeMetaDataHandler + " node meta data[" + key + "] is not an instance of Boolean");
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

    CompilationFailedException createParsingFailedException(String msg, ASTNode node) {
        Objects.requireNonNull(node, "node passed into createParsingFailedException should not be null");

        return createParsingFailedException(
                new SyntaxException(msg,
                        node.getLineNumber(),
                        node.getColumnNumber(),
                        node.getLastLineNumber(),
                        node.getLastColumnNumber()));
    }


    private CompilationFailedException createParsingFailedException(String msg, TerminalNode node) {
        return createParsingFailedException(msg, node.getSymbol());
    }

    private CompilationFailedException createParsingFailedException(String msg, Token token) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        token.getLine(),
                        token.getCharPositionInLine() + 1,
                        token.getLine(),
                        token.getCharPositionInLine() + 1 + token.getText().length()));
    }

    private CompilationFailedException createParsingFailedException(Throwable t) {
        if (t instanceof SyntaxException) {
            this.collectSyntaxError((SyntaxException) t);
        } else if (t instanceof GroovySyntaxThrowable) {
            GroovySyntaxThrowable groovySyntaxError = (GroovySyntaxThrowable) t;

            this.collectSyntaxError(
                    new SyntaxException(
                            groovySyntaxError.getMessage(),
                            (Throwable) groovySyntaxError,
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

    /*
    private String createExceptionMessage(Throwable t) {
        StringWriter sw = new StringWriter();

        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }

        return sw.toString();
    }
    */

    private static class DeclarationListStatement extends Statement {
        private final List<ExpressionStatement> declarationStatements;

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
                if (null != declarationListStatementLabels) {
                    // clear existing statement labels before setting labels
                    if (null != e.getStatementLabels()) {
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

    private final ModuleNode moduleNode;
    private final SourceUnit sourceUnit;
    private final CompilerConfiguration compilerConfiguration;
    private final GroovyLangLexer lexer;
    private final GroovyLangParser parser;
    private final TryWithResourcesASTTransformation tryWithResourcesASTTransformation;
    private final GroovydocManager groovydocManager;
    private final List<ClassNode> classNodeList = new LinkedList<>();
    private final Deque<ClassNode> classNodeStack = new ArrayDeque<>();
    private final Deque<List<InnerClassNode>> anonymousInnerClassesDefinedInMethodStack = new ArrayDeque<>();
    private int anonymousInnerClassCounter = 1;

    private Tuple2<GroovyParserRuleContext, Exception> numberFormatError;

    private int visitingLoopStatementCnt;
    private int visitingSwitchStatementCnt;
    private int visitingAssertStatementCnt;
    private int visitingClosureCnt;

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
    private static final String SLASH_STR = "/";
    private static final String SLASH_DOLLAR_STR = "/$";
    private static final String TDQ_STR = "\"\"\"";
    private static final String TSQ_STR = "'''";
    private static final String SQ_STR = "'";
    private static final String DQ_STR = "\"";
    private static final String DOLLAR_SLASH_STR = "$/";
    private static final String VAR_STR = "var";

    private static final Map<String, String> QUOTATION_MAP = Maps.of(
            DQ_STR, DQ_STR,
            SQ_STR, SQ_STR,
            TDQ_STR, TDQ_STR,
            TSQ_STR, TSQ_STR,
            SLASH_STR, SLASH_STR,
            DOLLAR_SLASH_STR, SLASH_DOLLAR_STR
    );

    private static final String PACKAGE_INFO = "package-info";
    private static final String PACKAGE_INFO_FILE_NAME = PACKAGE_INFO + ".groovy";

    private static final String GROOVY_TRANSFORM_TRAIT = "groovy.transform.Trait";
    private static final Set<String> PRIMITIVE_TYPE_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("boolean", "char", "byte", "short", "int", "long", "float", "double")));
    private static final Logger LOGGER = Logger.getLogger(AstBuilder.class.getName());

    private static final String INSIDE_PARENTHESES_LEVEL = "_INSIDE_PARENTHESES_LEVEL";

    private static final String IS_INSIDE_INSTANCEOF_EXPR = "_IS_INSIDE_INSTANCEOF_EXPR";
    private static final String IS_SWITCH_DEFAULT = "_IS_SWITCH_DEFAULT";
    private static final String IS_NUMERIC = "_IS_NUMERIC";
    private static final String IS_STRING = "_IS_STRING";
    private static final String IS_INTERFACE_WITH_DEFAULT_METHODS = "_IS_INTERFACE_WITH_DEFAULT_METHODS";
    private static final String IS_INSIDE_CONDITIONAL_EXPRESSION = "_IS_INSIDE_CONDITIONAL_EXPRESSION";
    private static final String IS_COMMAND_EXPRESSION = "_IS_COMMAND_EXPRESSION";

    private static final String PATH_EXPRESSION_BASE_EXPR = "_PATH_EXPRESSION_BASE_EXPR";
    private static final String PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES = "_PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES";
    private static final String PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN = "_PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN";
    private static final String CMD_EXPRESSION_BASE_EXPR = "_CMD_EXPRESSION_BASE_EXPR";
    private static final String TYPE_DECLARATION_MODIFIERS = "_TYPE_DECLARATION_MODIFIERS";
    private static final String CLASS_DECLARATION_CLASS_NODE = "_CLASS_DECLARATION_CLASS_NODE";
    private static final String VARIABLE_DECLARATION_VARIABLE_TYPE = "_VARIABLE_DECLARATION_VARIABLE_TYPE";
    private static final String ANONYMOUS_INNER_CLASS_SUPER_CLASS = "_ANONYMOUS_INNER_CLASS_SUPER_CLASS";
    private static final String INTEGER_LITERAL_TEXT = "_INTEGER_LITERAL_TEXT";
    private static final String FLOATING_POINT_LITERAL_TEXT = "_FLOATING_POINT_LITERAL_TEXT";
    private static final String ENCLOSING_INSTANCE_EXPRESSION = "_ENCLOSING_INSTANCE_EXPRESSION";

    private static final String CLASS_NAME = "_CLASS_NAME";

    private static final String ERROR_RECOVERY = "errorRecovery";
}
