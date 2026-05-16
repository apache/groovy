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
package org.codehaus.groovy.control.customizers;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
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
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This customizer allows securing source code by controlling what code constructs are permitted.
 * This is typically done when using Groovy for its scripting or domain specific language (DSL) features.
 * For example, if you only want to allow arithmetic operations in a groovy shell,
 * you can configure this customizer to restrict package imports, method calls and so on.
 * <p>
 * Most of the security customization options found in this class work with either <i>allowed</i> or <i>disallowed</i> lists.
 * This means that, for a single option, you can set an allowed list OR a disallowed list, but not both.
 * You can mix allowed/disallowed strategies for different options.
 * For example, you can have an allowed import list and a disallowed tokens list.
 * <p>
 * The recommended way of securing shells is to use allowed lists because it is guaranteed that future features of the
 * Groovy language won't be accidentally allowed unless explicitly added to the allowed list.
 * Using disallowed lists, you can limit the features of the language constructs supported by your shell by opting
 * out, but new language features are then implicitly also available and this may not be desirable.
 * The implication is that you might need to update your configuration with each new release.
 * <p>
 * If neither an allowed list nor a disallowed list is set, then everything is permitted.
 * <p>
 * Combinations of import and star import constraints are authorized as long as you use the same type of list for both.
 * For example, you may use an import allowed list and a star import allowed list together, but you cannot use an import
 * allowed list with a star import disallowed list. Static imports are handled separately, meaning that disallowing an
 * import <b>does not</b> prevent from allowing a static import.
 * <p>
 * Eventually, if the features provided here are not sufficient, you may implement custom AST filtering handlers, either
 * implementing the {@link StatementChecker} interface or {@link ExpressionChecker} interface then register your
 * handlers thanks to the {@link #addExpressionCheckers(ExpressionChecker...)}
 * and {@link #addStatementCheckers(StatementChecker...)}
 * methods.
 * <p>
 * Here is an example of usage. We will create a groovy classloader which only supports arithmetic operations and imports
 * the {@code java.lang.Math} classes by default.
 *
 * <pre>
 * final ImportCustomizer imports = new ImportCustomizer().addStaticStars('java.lang.Math') // add static import of java.lang.Math
 * final SecureASTCustomizer secure = new SecureASTCustomizer()
 * secure.with {
 *     closuresAllowed = false
 *     methodDefinitionAllowed = false
 *
 *     allowedImports = []
 *     allowedStaticImports = []
 *     allowedStaticStarImports = ['java.lang.Math'] // only java.lang.Math is allowed
 *
 *     allowedTokens = [
 *             PLUS,
 *             MINUS,
 *             MULTIPLY,
 *             DIVIDE,
 *             REMAINDER,
 *             POWER,
 *             PLUS_PLUS,
 *             MINUS_MINUS,
 *             COMPARE_EQUAL,
 *             COMPARE_NOT_EQUAL,
 *             COMPARE_LESS_THAN,
 *             COMPARE_LESS_THAN_EQUAL,
 *             COMPARE_GREATER_THAN,
 *             COMPARE_GREATER_THAN_EQUAL,
 *     ].asImmutable()
 *
 *     allowedConstantTypesClasses = [
 *             Integer,
 *             Float,
 *             Long,
 *             Double,
 *             BigDecimal,
 *             Integer.TYPE,
 *             Long.TYPE,
 *             Float.TYPE,
 *             Double.TYPE
 *     ].asImmutable()
 *
 *     allowedReceiversClasses = [
 *             Math,
 *             Integer,
 *             Float,
 *             Double,
 *             Long,
 *             BigDecimal
 *     ].asImmutable()
 * }
 * CompilerConfiguration config = new CompilerConfiguration()
 * config.addCompilationCustomizers(imports, secure)
 * GroovyClassLoader loader = new GroovyClassLoader(this.class.classLoader, config)
 *  </pre>
 * <p>
 *  Note: {@code SecureASTCustomizer} allows you to lock down the grammar of scripts but by itself isn't intended
 *  to be the complete solution of all security issues when running scripts on the JVM. You might also want to
 *  consider setting the {@code groovy.grape.enable} System property to false, augmenting use of the customizer
 *  with additional techniques, and following standard security principles for JVM applications.
 *  <p>
 *  For more information, please read:
 *  <li><a href="https://melix.github.io/blog/2015/03/sandboxing.html">Improved sandboxing of Groovy scripts</a></li>
 *  <li><a href="https://www.oracle.com/java/technologies/javase/seccodeguide.html">Oracle's Secure Coding Guidelines</a></li>
 *  <li><a href="https://snyk.io/blog/10-java-security-best-practices/">10 Java security best practices</a></li>
 *  <li><a href="https://www.infoworld.com/article/2076837/twelve-rules-for-developing-more-secure-java-code.html">Thirteen rules for developing secure Java applications</a></li>
 *  <li><a href="https://www.guardrails.io/blog/12-java-security-best-practices/">12 Java Security Best Practices</a></li>
 *
 * @since 1.8.0
 */
public class SecureASTCustomizer extends CompilationCustomizer {

    private boolean isPackageAllowed = true;
    private boolean isClosuresAllowed = true;
    private boolean isMethodDefinitionAllowed = true;

    // imports
    private List<String> allowedImports;
    private List<String> disallowedImports;

    // static imports
    private List<String> allowedStaticImports;
    private List<String> disallowedStaticImports;

    // star imports
    private List<String> allowedStarImports;
    private List<String> disallowedStarImports;

    // static star imports
    private List<String> allowedStaticStarImports;
    private List<String> disallowedStaticStarImports;

    // indirect import checks
    // if set to true, then security rules on imports will also be applied on classnodes.
    // Direct instantiation of classes without imports will therefore also fail if this option is enabled
    private boolean isIndirectImportCheckEnabled;

    // statements
    private List<Class<? extends Statement>> allowedStatements;
    private List<Class<? extends Statement>> disallowedStatements;
    private final List<StatementChecker> statementCheckers = new LinkedList<>();

    // expressions
    private List<Class<? extends Expression>> allowedExpressions;
    private List<Class<? extends Expression>> disallowedExpressions;
    private final List<ExpressionChecker> expressionCheckers = new LinkedList<>();

    // tokens from Types
    private List<Integer> allowedTokens;
    private List<Integer> disallowedTokens;

    // constant types
    private List<String> allowedConstantTypes;
    private List<String> disallowedConstantTypes;

    // receivers
    private List<String> allowedReceivers;
    private List<String> disallowedReceivers;

    /**
     * Creates a secure AST customizer that runs during canonicalization.
     */
    public SecureASTCustomizer() {
        super(CompilePhase.CANONICALIZATION);
    }

    /**
     * Indicates whether explicit method definitions are allowed.
     *
     * @return {@code true} if method definitions are allowed
     */
    public boolean isMethodDefinitionAllowed() {
        return isMethodDefinitionAllowed;
    }

    /**
     * Sets whether explicit method definitions are allowed.
     *
     * @param methodDefinitionAllowed {@code true} to allow method definitions
     */
    public void setMethodDefinitionAllowed(final boolean methodDefinitionAllowed) {
        isMethodDefinitionAllowed = methodDefinitionAllowed;
    }

    /**
     * Indicates whether package declarations are allowed.
     *
     * @return {@code true} if package declarations are allowed
     */
    public boolean isPackageAllowed() {
        return isPackageAllowed;
    }

    /**
     * Indicates whether closures are allowed.
     *
     * @return {@code true} if closures are allowed
     */
    public boolean isClosuresAllowed() {
        return isClosuresAllowed;
    }

    /**
     * Sets whether closures are allowed.
     *
     * @param closuresAllowed {@code true} to allow closures
     */
    public void setClosuresAllowed(final boolean closuresAllowed) {
        isClosuresAllowed = closuresAllowed;
    }

    /**
     * Sets whether package declarations are allowed.
     *
     * @param packageAllowed {@code true} to allow package declarations
     */
    public void setPackageAllowed(final boolean packageAllowed) {
        isPackageAllowed = packageAllowed;
    }

    /**
     * Returns the list of explicitly disallowed imports.
     *
     * @return the disallowed imports, or {@code null}
     */
    public List<String> getDisallowedImports() {
        return disallowedImports;
    }

    /**
     * Legacy alias for {@link #getDisallowedImports()}
     */
    @Deprecated
    public List<String> getImportsBlacklist() {
        return getDisallowedImports();
    }

    /**
     * Sets the list of explicitly disallowed imports.
     *
     * @param disallowedImports the imports to reject
     */
    public void setDisallowedImports(final List<String> disallowedImports) {
        if (allowedImports != null || allowedStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedImports = disallowedImports;
    }

    /**
     * Legacy alias for {@link #setDisallowedImports(List)}
     */
    @Deprecated
    public void setImportsBlacklist(final List<String> disallowedImports) {
        setDisallowedImports(disallowedImports);
    }

    /**
     * Returns the list of explicitly allowed imports.
     *
     * @return the allowed imports, or {@code null}
     */
    public List<String> getAllowedImports() {
        return allowedImports;
    }

    /**
     * Legacy alias for {@link #getAllowedImports()}
     */
    @Deprecated
    public List<String> getImportsWhitelist() {
        return getAllowedImports();
    }

    /**
     * Sets the list of explicitly allowed imports.
     *
     * @param allowedImports the imports to allow
     */
    public void setAllowedImports(final List<String> allowedImports) {
        if (disallowedImports != null || disallowedStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedImports = allowedImports;
    }

    /**
     * Legacy alias for {@link #setAllowedImports(List)}
     */
    @Deprecated
    public void setImportsWhitelist(final List<String> allowedImports) {
        setAllowedImports(allowedImports);
    }

    /**
     * Returns the list of disallowed star imports.
     *
     * @return the disallowed star imports, or {@code null}
     */
    public List<String> getDisallowedStarImports() {
        return disallowedStarImports;
    }

    /**
     * Legacy alias for {@link #getDisallowedStarImports()}
     */
    @Deprecated
    public List<String> getStarImportsBlacklist() {
        return getDisallowedStarImports();
    }

    /**
     * Sets the list of disallowed star imports.
     *
     * @param disallowedStarImports the star imports to reject
     */
    public void setDisallowedStarImports(final List<String> disallowedStarImports) {
        if (allowedImports != null || allowedStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedStarImports = normalizeStarImports(disallowedStarImports);
        if (this.disallowedImports == null) disallowedImports = Collections.emptyList();
    }

    /**
     * Legacy alias for {@link #setDisallowedStarImports(List)}
     */
    @Deprecated
    public void setStarImportsBlacklist(final List<String> disallowedStarImports) {
        setDisallowedStarImports(disallowedStarImports);
    }

    /**
     * Returns the list of allowed star imports.
     *
     * @return the allowed star imports, or {@code null}
     */
    public List<String> getAllowedStarImports() {
        return allowedStarImports;
    }

    /**
     * Legacy alias for {@link #getAllowedStarImports()}
     */
    @Deprecated
    public List<String> getStarImportsWhitelist() {
        return getAllowedStarImports();
    }

    /**
     * Sets the list of allowed star imports.
     *
     * @param allowedStarImports the star imports to allow
     */
    public void setAllowedStarImports(final List<String> allowedStarImports) {
        if (disallowedImports != null || disallowedStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedStarImports = normalizeStarImports(allowedStarImports);
        if (this.allowedImports == null) allowedImports = Collections.emptyList();
    }

    /**
     * Legacy alias for {@link #setAllowedStarImports(List)}
     */
    @Deprecated
    public void setStarImportsWhitelist(final List<String> allowedStarImports) {
        setAllowedStarImports(allowedStarImports);
    }

    private static List<String> normalizeStarImports(List<String> starImports) {
        List<String> result = new ArrayList<>(starImports.size());
        for (String starImport : starImports) {
            if (starImport.endsWith(".*")) {
                result.add(starImport);
            } else if (starImport.endsWith("**")) {
                result.add(starImport.replaceFirst("\\*+$", ""));
            } else if (starImport.endsWith(".")) {
                result.add(starImport + "*");
            } else {
                result.add(starImport + ".*");
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the list of disallowed static imports.
     *
     * @return the disallowed static imports, or {@code null}
     */
    public List<String> getDisallowedStaticImports() {
        return disallowedStaticImports;
    }

    /**
     * Legacy alias for {@link #getDisallowedStaticImports()}
     */
    @Deprecated
    public List<String> getStaticImportsBlacklist() {
        return getDisallowedStaticImports();
    }

    /**
     * Sets the list of disallowed static imports.
     *
     * @param disallowedStaticImports the static imports to reject
     */
    public void setDisallowedStaticImports(final List<String> disallowedStaticImports) {
        if (allowedStaticImports != null || allowedStaticStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedStaticImports = disallowedStaticImports;
    }

    /**
     * Legacy alias for {@link #setDisallowedStaticImports(List)}
     */
    @Deprecated
    public void setStaticImportsBlacklist(final List<String> disallowedStaticImports) {
        setDisallowedStaticImports(disallowedStaticImports);
    }

    /**
     * Returns the list of allowed static imports.
     *
     * @return the allowed static imports, or {@code null}
     */
    public List<String> getAllowedStaticImports() {
        return allowedStaticImports;
    }

    /**
     * Legacy alias for {@link #getAllowedStaticImports()}
     */
    @Deprecated
    public List<String> getStaticImportsWhitelist() {
        return getAllowedStaticImports();
    }

    /**
     * Sets the list of allowed static imports.
     *
     * @param allowedStaticImports the static imports to allow
     */
    public void setAllowedStaticImports(final List<String> allowedStaticImports) {
        if (disallowedStaticImports != null || disallowedStaticStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedStaticImports = allowedStaticImports;
    }

    /**
     * Legacy alias for {@link #setAllowedStaticImports(List)}
     */
    @Deprecated
    public void setStaticImportsWhitelist(final List<String> allowedStaticImports) {
        setAllowedStaticImports(allowedStaticImports);
    }

    /**
     * Returns the list of disallowed static star imports.
     *
     * @return the disallowed static star imports, or {@code null}
     */
    public List<String> getDisallowedStaticStarImports() {
        return disallowedStaticStarImports;
    }

    /**
     * Legacy alias for {@link #getDisallowedStaticStarImports()}
     */
    @Deprecated
    public List<String> getStaticStarImportsBlacklist() {
        return getDisallowedStaticStarImports();
    }

    /**
     * Sets the list of disallowed static star imports.
     *
     * @param disallowedStaticStarImports the static star imports to reject
     */
    public void setDisallowedStaticStarImports(final List<String> disallowedStaticStarImports) {
        if (allowedStaticImports != null || allowedStaticStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedStaticStarImports = normalizeStarImports(disallowedStaticStarImports);
        if (this.disallowedStaticImports == null) this.disallowedStaticImports = Collections.emptyList();
    }

    /**
     * Legacy alias for {@link #setDisallowedStaticStarImports(List)}
     */
    @Deprecated
    public void setStaticStarImportsBlacklist(final List<String> disallowedStaticStarImports) {
        setDisallowedStaticStarImports(disallowedStaticStarImports);
    }

    /**
     * Returns the list of allowed static star imports.
     *
     * @return the allowed static star imports, or {@code null}
     */
    public List<String> getAllowedStaticStarImports() {
        return allowedStaticStarImports;
    }

    /**
     * Legacy alias for {@link #getAllowedStaticStarImports()}
     */
    @Deprecated
    public List<String> getStaticStarImportsWhitelist() {
        return getAllowedStaticStarImports();
    }

    /**
     * Sets the list of allowed static star imports.
     *
     * @param allowedStaticStarImports the static star imports to allow
     */
    public void setAllowedStaticStarImports(final List<String> allowedStaticStarImports) {
        if (disallowedStaticImports != null || disallowedStaticStarImports != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedStaticStarImports = normalizeStarImports(allowedStaticStarImports);
        if (this.allowedStaticImports == null) this.allowedStaticImports = Collections.emptyList();
    }

    /**
     * Legacy alias for {@link #setAllowedStaticStarImports(List)}
     */
    @Deprecated
    public void setStaticStarImportsWhitelist(final List<String> allowedStaticStarImports) {
        setAllowedStaticStarImports(allowedStaticStarImports);
    }

    /**
     * Returns the list of disallowed expression node types.
     *
     * @return the disallowed expression types, or {@code null}
     */
    public List<Class<? extends Expression>> getDisallowedExpressions() {
        return disallowedExpressions;
    }

    /**
     * Legacy alias for {@link #getDisallowedExpressions()}
     */
    @Deprecated
    public List<Class<? extends Expression>> getExpressionsBlacklist() {
        return getDisallowedExpressions();
    }

    /**
     * Sets the list of disallowed expression node types.
     *
     * @param disallowedExpressions the expression types to reject
     */
    public void setDisallowedExpressions(final List<Class<? extends Expression>> disallowedExpressions) {
        if (allowedExpressions != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedExpressions = disallowedExpressions;
    }

    /**
     * Legacy alias for {@link #setDisallowedExpressions(List)}
     */
    @Deprecated
    public void setExpressionsBlacklist(final List<Class<? extends Expression>> disallowedExpressions) {
        setDisallowedExpressions(disallowedExpressions);
    }

    /**
     * Returns the list of allowed expression node types.
     *
     * @return the allowed expression types, or {@code null}
     */
    public List<Class<? extends Expression>> getAllowedExpressions() {
        return allowedExpressions;
    }

    /**
     * Legacy alias for {@link #getAllowedExpressions()}
     */
    @Deprecated
    public List<Class<? extends Expression>> getExpressionsWhitelist() {
        return getAllowedExpressions();
    }

    /**
     * Sets the list of allowed expression node types.
     *
     * @param allowedExpressions the expression types to allow
     */
    public void setAllowedExpressions(final List<Class<? extends Expression>> allowedExpressions) {
        if (disallowedExpressions != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedExpressions = allowedExpressions;
    }

    /**
     * Legacy alias for {@link #setAllowedExpressions(List)}
     */
    @Deprecated
    public void setExpressionsWhitelist(final List<Class<? extends Expression>> allowedExpressions) {
        setAllowedExpressions(allowedExpressions);
    }

    /**
     * Returns the list of disallowed statement node types.
     *
     * @return the disallowed statement types, or {@code null}
     */
    public List<Class<? extends Statement>> getDisallowedStatements() {
        return disallowedStatements;
    }

    /**
     * Legacy alias for {@link #getDisallowedStatements()}
     */
    @Deprecated
    public List<Class<? extends Statement>> getStatementsBlacklist() {
        return getDisallowedStatements();
    }

    /**
     * Sets the list of disallowed statement node types.
     *
     * @param disallowedStatements the statement types to reject
     */
    public void setDisallowedStatements(final List<Class<? extends Statement>> disallowedStatements) {
        if (allowedStatements != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedStatements = disallowedStatements;
    }

    /**
     * Legacy alias for {@link #setDisallowedStatements(List)}
     */
    @Deprecated
    public void setStatementsBlacklist(final List<Class<? extends Statement>> disallowedStatements) {
        setDisallowedStatements(disallowedStatements);
    }

    /**
     * Returns the list of allowed statement node types.
     *
     * @return the allowed statement types, or {@code null}
     */
    public List<Class<? extends Statement>> getAllowedStatements() {
        return allowedStatements;
    }

    /**
     * Legacy alias for {@link #getAllowedStatements()}
     */
    @Deprecated
    public List<Class<? extends Statement>> getStatementsWhitelist() {
        return getAllowedStatements();
    }

    /**
     * Sets the list of allowed statement node types.
     *
     * @param allowedStatements the statement types to allow
     */
    public void setAllowedStatements(final List<Class<? extends Statement>> allowedStatements) {
        if (disallowedStatements != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedStatements = allowedStatements;
    }

    /**
     * Legacy alias for {@link #setAllowedStatements(List)}
     */
    @Deprecated
    public void setStatementsWhitelist(final List<Class<? extends Statement>> allowedStatements) {
        setAllowedStatements(allowedStatements);
    }

    /**
     * Indicates whether indirect import checks are enabled.
     *
     * @return {@code true} if indirect import checks are enabled
     */
    public boolean isIndirectImportCheckEnabled() {
        return isIndirectImportCheckEnabled;
    }

    /**
     * Set this option to true if you want your import rules to be checked against every class node. This means that if
     * someone uses a fully qualified class name, then it will also be checked against the import rules, preventing, for
     * example, instantiation of classes without imports thanks to FQCN.
     *
     * @param indirectImportCheckEnabled set to true to enable indirect checks
     */
    public void setIndirectImportCheckEnabled(final boolean indirectImportCheckEnabled) {
        isIndirectImportCheckEnabled = indirectImportCheckEnabled;
    }

    /**
     * Returns the list of disallowed token types.
     *
     * @return the disallowed token types, or {@code null}
     */
    public List<Integer> getDisallowedTokens() {
        return disallowedTokens;
    }

    /**
     * Legacy alias for {@link #getDisallowedTokens()}
     */
    @Deprecated
    public List<Integer> getTokensBlacklist() {
        return getDisallowedTokens();
    }

    /**
     * Sets the list of tokens which are not permitted.
     *
     * @param disallowedTokens the tokens. The values of the tokens must be those of {@link org.codehaus.groovy.syntax.Types}
     */
    public void setDisallowedTokens(final List<Integer> disallowedTokens) {
        if (allowedTokens != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedTokens = disallowedTokens;
    }

    /**
     * Legacy alias for {@link #setDisallowedTokens(List)}.
     */
    @Deprecated
    public void setTokensBlacklist(final List<Integer> disallowedTokens) {
        setDisallowedTokens(disallowedTokens);
    }

    /**
     * Returns the list of allowed token types.
     *
     * @return the allowed token types, or {@code null}
     */
    public List<Integer> getAllowedTokens() {
        return allowedTokens;
    }

    /**
     * Legacy alias for {@link #getAllowedTokens()}
     */
    @Deprecated
    public List<Integer> getTokensWhitelist() {
        return getAllowedTokens();
    }

    /**
     * Sets the list of tokens which are permitted.
     *
     * @param allowedTokens the tokens. The values of the tokens must be those of {@link org.codehaus.groovy.syntax.Types}
     */
    public void setAllowedTokens(final List<Integer> allowedTokens) {
        if (disallowedTokens != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedTokens = allowedTokens;
    }

    /**
     * Legacy alias for {@link #setAllowedTokens(List)}
     */
    @Deprecated
    public void setTokensWhitelist(final List<Integer> allowedTokens) {
        setAllowedTokens(allowedTokens);
    }

    /**
     * Adds statement checkers consulted in addition to the allow/disallow lists.
     *
     * @param checkers the statement checkers to add
     */
    public void addStatementCheckers(StatementChecker... checkers) {
        statementCheckers.addAll(Arrays.asList(checkers));
    }

    /**
     * Adds expression checkers consulted in addition to the allow/disallow lists.
     *
     * @param checkers the expression checkers to add
     */
    public void addExpressionCheckers(ExpressionChecker... checkers) {
        expressionCheckers.addAll(Arrays.asList(checkers));
    }

    /**
     * Returns the list of disallowed constant or variable types.
     *
     * @return the disallowed constant types, or {@code null}
     */
    public List<String> getDisallowedConstantTypes() {
        return disallowedConstantTypes;
    }

    /**
     * Legacy alias for {@link #getDisallowedConstantTypes()}
     */
    @Deprecated
    public List<String> getConstantTypesBlackList() {
        return getDisallowedConstantTypes();
    }

    /**
     * Sets the list of disallowed constant or variable types.
     *
     * @param constantTypesBlackList the type names to reject
     */
    public void setConstantTypesBlackList(final List<String> constantTypesBlackList) {
        if (allowedConstantTypes != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedConstantTypes = constantTypesBlackList;
    }

    /**
     * Returns the list of allowed constant or variable types.
     *
     * @return the allowed constant types, or {@code null}
     */
    public List<String> getAllowedConstantTypes() {
        return allowedConstantTypes;
    }

    /**
     * Legacy alias for {@link #getAllowedStatements()}
     */
    @Deprecated
    public List<String> getConstantTypesWhiteList() {
        return getAllowedConstantTypes();
    }

    /**
     * Sets the list of allowed constant or variable types.
     *
     * @param allowedConstantTypes the type names to allow
     */
    public void setAllowedConstantTypes(final List<String> allowedConstantTypes) {
        if (disallowedConstantTypes != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedConstantTypes = allowedConstantTypes;
    }

    /**
     * Legacy alias for {@link #setAllowedConstantTypes(List)}
     */
    @Deprecated
    public void setConstantTypesWhiteList(final List<String> allowedConstantTypes) {
        setAllowedConstantTypes(allowedConstantTypes);
    }

    /**
     * An alternative way of setting constant types.
     *
     * @param allowedConstantTypes a list of classes.
     */
    public void setAllowedConstantTypesClasses(final List<Class> allowedConstantTypes) {
        List<String> values = new LinkedList<>();
        for (Class aClass : allowedConstantTypes) {
            values.add(aClass.getName());
        }
        setConstantTypesWhiteList(values);
    }

    /**
     * Legacy alias for {@link #setAllowedConstantTypesClasses(List)}
     */
    @Deprecated
    public void setConstantTypesClassesWhiteList(final List<Class> allowedConstantTypes) {
        setAllowedConstantTypesClasses(allowedConstantTypes);
    }

    /**
     * An alternative way of setting constant types.
     *
     * @param disallowedConstantTypes a list of classes.
     */
    public void setDisallowedConstantTypesClasses(final List<Class> disallowedConstantTypes) {
        List<String> values = new LinkedList<>();
        for (Class aClass : disallowedConstantTypes) {
            values.add(aClass.getName());
        }
        setConstantTypesBlackList(values);
    }

    /**
     * Legacy alias for {@link #setDisallowedConstantTypesClasses(List)}
     */
    @Deprecated
    public void setConstantTypesClassesBlackList(final List<Class> disallowedConstantTypes) {
        setDisallowedConstantTypesClasses(disallowedConstantTypes);
    }

    /**
     * Returns the list of receiver types on which calls are disallowed.
     *
     * @return the disallowed receiver types, or {@code null}
     */
    public List<String> getDisallowedReceivers() {
        return disallowedReceivers;
    }

    /**
     * Legacy alias for {@link #getDisallowedReceivers()}
     */
    @Deprecated
    public List<String> getReceiversBlackList() {
        return getDisallowedReceivers();
    }

    /**
     * Sets the list of classes which deny method calls.
     *
     * Please note that since Groovy is a dynamic language, and
     * this class performs a static type check, it will be relatively
     * simple to bypass any disallowed list unless the disallowed receivers list contains, at
     * a minimum, Object, Script, GroovyShell, and Eval. Additionally,
     * it is necessary to also have MethodPointerExpression in the
     * disallowed expressions list for the disallowed receivers list to function
     * as a security check.
     *
     * @param disallowedReceivers the list of refused classes, as fully qualified names
     */
    public void setDisallowedReceivers(final List<String> disallowedReceivers) {
        if (allowedReceivers != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.disallowedReceivers = disallowedReceivers;
    }

    /**
     * Legacy alias for {@link #setDisallowedReceivers(List)}
     */
    @Deprecated
    public void setReceiversBlackList(final List<String> disallowedReceivers) {
        setDisallowedReceivers(disallowedReceivers);
    }

    /**
     * An alternative way of setting {@link #setDisallowedReceivers(java.util.List) receiver classes}.
     *
     * @param disallowedReceivers a list of classes.
     */
    public void setDisallowedReceiversClasses(final List<Class> disallowedReceivers) {
        List<String> values = new LinkedList<>();
        for (Class aClass : disallowedReceivers) {
            values.add(aClass.getName());
        }
        setReceiversBlackList(values);
    }

    /**
     * Legacy alias for {@link #setDisallowedReceiversClasses(List)}.
     */
    @Deprecated
    public void setReceiversClassesBlackList(final List<Class> disallowedReceivers) {
        setDisallowedReceiversClasses(disallowedReceivers);
    }

    /**
     * Returns the list of receiver types on which calls are allowed.
     *
     * @return the allowed receiver types, or {@code null}
     */
    public List<String> getAllowedReceivers() {
        return allowedReceivers;
    }

    /**
     * Legacy alias for {@link #getAllowedReceivers()}
     */
    @Deprecated
    public List<String> getReceiversWhiteList() {
        return getAllowedReceivers();
    }

    /**
     * Sets the list of classes which may accept method calls.
     *
     * @param allowedReceivers the list of accepted classes, as fully qualified names
     */
    public void setAllowedReceivers(final List<String> allowedReceivers) {
        if (disallowedReceivers != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedReceivers = allowedReceivers;
    }

    /**
     * Legacy alias for {@link #setAllowedReceivers(List)}
     */
    @Deprecated
    public void setReceiversWhiteList(final List<String> allowedReceivers) {
        if (disallowedReceivers != null) {
            throw new IllegalArgumentException("You are not allowed to set both an allowed list and a disallowed list");
        }
        this.allowedReceivers = allowedReceivers;
    }

    /**
     * An alternative way of setting {@link #setReceiversWhiteList(java.util.List) receiver classes}.
     *
     * @param allowedReceivers a list of classes.
     */
    public void setAllowedReceiversClasses(final List<Class> allowedReceivers) {
        List<String> values = new LinkedList<>();
        for (Class aClass : allowedReceivers) {
            values.add(aClass.getName());
        }
        setReceiversWhiteList(values);
    }

    /**
     * Legacy alias for {@link #setAllowedReceiversClasses(List)}
     */
    @Deprecated
    public void setReceiversClassesWhiteList(final List<Class> allowedReceivers) {
        setAllowedReceiversClasses(allowedReceivers);
    }

    /**
     * Verifies the configured security rules against the current source unit and class.
     *
     * @param source the source unit being customized
     * @param context the current generator context
     * @param classNode the class node being customized
     * @throws CompilationFailedException if verification fails
     */
    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        ModuleNode ast = source.getAST();
        if (!isPackageAllowed && ast.getPackage() != null) {
            throw new SecurityException("Package definitions are not allowed");
        }
        checkMethodDefinitionAllowed(classNode);

        // verify imports
        if (disallowedImports != null || allowedImports != null || disallowedStarImports != null || allowedStarImports != null) {
            for (ImportNode importNode : ast.getImports()) {
                assertImportIsAllowed(importNode.getClassName());
            }
            for (ImportNode importNode : ast.getStarImports()) {
                assertStarImportIsAllowed(importNode.getPackageName() + "*");
            }
        }

        // verify static imports
        if (disallowedStaticImports != null || allowedStaticImports != null || disallowedStaticStarImports != null || allowedStaticStarImports != null) {
            for (Map.Entry<String, ImportNode> entry : ast.getStaticImports().entrySet()) {
                final String className = entry.getValue().getClassName();
                assertStaticImportIsAllowed(entry.getKey(), className);
            }
            for (Map.Entry<String, ImportNode> entry : ast.getStaticStarImports().entrySet()) {
                final String className = entry.getValue().getClassName();
                assertStaticImportIsAllowed(entry.getKey(), className);
            }
        }

        GroovyCodeVisitor visitor = createGroovyCodeVisitor();
        ast.getStatementBlock().visit(visitor);
        for (ClassNode clNode : ast.getClasses()) {
            if (clNode!=classNode) {
                checkMethodDefinitionAllowed(clNode);
                for (MethodNode methodNode : clNode.getMethods()) {
                    if (!methodNode.isSynthetic() && methodNode.getCode() != null) {
                        methodNode.getCode().visit(visitor);
                    }
                }
            }
        }

        List<MethodNode> methods = filterMethods(classNode);
        if (isMethodDefinitionAllowed) {
            for (MethodNode method : methods) {
                if (method.getDeclaringClass() == classNode && method.getCode() != null) {
                    method.getCode().visit(visitor);
                }
            }
        }
    }

    /**
     * Creates the visitor that enforces statement and expression restrictions.
     *
     * @return the security-checking visitor
     */
    protected GroovyCodeVisitor createGroovyCodeVisitor() {
        return new SecuringCodeVisitor();
    }

    /**
     * Ensures the supplied class does not declare methods when such definitions are forbidden.
     *
     * @param owner the class to inspect
     */
    protected void checkMethodDefinitionAllowed(ClassNode owner) {
        if (isMethodDefinitionAllowed) return;
        List<MethodNode> methods = filterMethods(owner);
        if (!methods.isEmpty()) throw new SecurityException("Method definitions are not allowed");
    }

    /**
     * Returns the non-synthetic methods declared directly by the supplied class.
     *
     * @param owner the class to inspect
     * @return the directly declared, non-synthetic methods
     */
    protected static List<MethodNode> filterMethods(ClassNode owner) {
        List<MethodNode> result = new LinkedList<>();
        List<MethodNode> methods = owner.getMethods();
        for (MethodNode method : methods) {
            if (method.getDeclaringClass() == owner && !method.isSynthetic()) {
                if (("main".equals(method.getName()) || "run".equals(method.getName())) && method.isScriptBody()) continue;
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Verifies that a star import is allowed by the current configuration.
     *
     * @param packageName the star import to check
     */
    protected void assertStarImportIsAllowed(final String packageName) {
        if (allowedStarImports != null && !(allowedStarImports.contains(packageName)
                || allowedStarImports.stream().filter(it -> it.endsWith(".")).anyMatch(packageName::startsWith))) {
            throw new SecurityException("Importing [" + packageName + "] is not allowed");
        }
        if (disallowedStarImports != null && (disallowedStarImports.contains(packageName)
                || disallowedStarImports.stream().filter(it -> it.endsWith(".")).anyMatch(packageName::startsWith))) {
            throw new SecurityException("Importing [" + packageName + "] is not allowed");
        }
    }

    /**
     * Verifies that a regular import is allowed by the current configuration.
     *
     * @param className the imported class name
     */
    protected void assertImportIsAllowed(final String className) {
        if (allowedImports != null || allowedStarImports != null) {
            if (allowedImports != null && allowedImports.contains(className)) {
                return;
            }
            if (allowedStarImports != null) {
                String packageName = getWildCardImport(className);
                if (allowedStarImports.contains(packageName)
                        || allowedStarImports.stream().filter(it -> it.endsWith(".")).anyMatch(packageName::startsWith)) {
                    return;
                }
            }
            throw new SecurityException("Importing [" + className + "] is not allowed");
        } else {
            if (disallowedImports != null && disallowedImports.contains(className)) {
                throw new SecurityException("Importing [" + className + "] is not allowed");
            }
            if (disallowedStarImports != null) {
                String packageName = getWildCardImport(className);
                if (disallowedStarImports.contains(packageName) ||
                        disallowedStarImports.stream().filter(it -> it.endsWith(".")).anyMatch(packageName::startsWith)) {
                    throw new SecurityException("Importing [" + className + "] is not allowed");
                }
            }
        }
    }

    private String getWildCardImport(String className) {
        return className.substring(0, className.lastIndexOf('.') + 1) + "*";
    }

    /**
     * Verifies that a static import is allowed by the current configuration.
     *
     * @param member the imported member name
     * @param className the declaring class name
     */
    protected void assertStaticImportIsAllowed(final String member, final String className) {
        final String fqn = className.equals(member) ? className : className + "." + member;
        if (allowedStaticImports != null && !allowedStaticImports.contains(fqn)) {
            if (allowedStaticStarImports != null) {
                // we should now check if the import is in the star imports
                String packageName = getWildCardImport(className);
                if (!allowedStaticStarImports.contains(className + ".*")
                        && allowedStaticStarImports.stream().filter(it -> it.endsWith(".")).noneMatch(packageName::startsWith)) {
                    throw new SecurityException("Importing [" + fqn + "] is not allowed");
                }
            } else {
                throw new SecurityException("Importing [" + fqn + "] is not allowed");
            }
        }
        if (disallowedStaticImports != null && disallowedStaticImports.contains(fqn)) {
            throw new SecurityException("Importing [" + fqn + "] is not allowed");
        }
        // check that there's no star import blacklist
        if (disallowedStaticStarImports != null) {
            String packageName = getWildCardImport(className);
            if (disallowedStaticStarImports.contains(className + ".*")
                    || disallowedStaticStarImports.stream().filter(it -> it.endsWith(".")).anyMatch(packageName::startsWith)) {
                throw new SecurityException("Importing [" + fqn + "] is not allowed");
            }
        }
    }

    /**
     * This visitor directly implements the {@link GroovyCodeVisitor} interface instead of using the {@link
     * CodeVisitorSupport} class to make sure that future features of the language gets managed by this visitor. Thus,
     * adding a new feature would result in a compilation error if this visitor is not updated.
     */
    protected class SecuringCodeVisitor implements GroovyCodeVisitor {

        /**
         * Checks that a given statement is either in the allowed list or not in the disallowed list.
         *
         * @param statement the statement to be checked
         * @throws SecurityException if usage of this statement class is forbidden
         */
        protected void assertStatementAuthorized(final Statement statement) throws SecurityException {
            final Class<? extends Statement> clazz = statement.getClass();
            if (disallowedStatements != null && disallowedStatements.contains(clazz)) {
                throw new SecurityException(clazz.getSimpleName() + "s are not allowed");
            } else if (allowedStatements != null && !allowedStatements.contains(clazz)) {
                throw new SecurityException(clazz.getSimpleName() + "s are not allowed");
            }
            for (StatementChecker statementChecker : statementCheckers) {
                if (!statementChecker.isAuthorized(statement)) {
                    throw new SecurityException("Statement [" + clazz.getSimpleName() + "] is not allowed");
                }
            }
        }

        /**
         * Checks that a given expression is either in the allowed list or not in the disallowed list.
         *
         * @param expression the expression to be checked
         * @throws SecurityException if usage of this expression class is forbidden
         */
        protected void assertExpressionAuthorized(final Expression expression) throws SecurityException {
            final Class<? extends Expression> clazz = expression.getClass();
            if (disallowedExpressions != null && disallowedExpressions.contains(clazz)) {
                throw new SecurityException(clazz.getSimpleName() + "s are not allowed: " + expression.getText());
            } else if (allowedExpressions != null && !allowedExpressions.contains(clazz)) {
                throw new SecurityException(clazz.getSimpleName() + "s are not allowed: " + expression.getText());
            }
            for (ExpressionChecker expressionChecker : expressionCheckers) {
                if (!expressionChecker.isAuthorized(expression)) {
                    throw new SecurityException("Expression [" + clazz.getSimpleName() + "] is not allowed: " + expression.getText());
                }
            }
            if (isIndirectImportCheckEnabled) {
                try {
                    if (expression instanceof ConstructorCallExpression) {
                        assertImportIsAllowed(expression.getType().getName());
                    } else if (expression instanceof MethodCallExpression expr) {
                        ClassNode objectExpressionType = expr.getObjectExpression().getType();
                        final String typename = getExpressionType(objectExpressionType).getName();
                        assertImportIsAllowed(typename);
                        assertStaticImportIsAllowed(expr.getMethodAsString(), typename);
                    } else if (expression instanceof StaticMethodCallExpression expr) {
                        final String typename = expr.getOwnerType().getName();
                        assertImportIsAllowed(typename);
                        assertStaticImportIsAllowed(expr.getMethod(), typename);
                    } else if (expression instanceof MethodPointerExpression expr) {
                        final String typename = expr.getType().getName();
                        assertImportIsAllowed(typename);
                        assertStaticImportIsAllowed(expr.getText(), typename);
                    }
                } catch (SecurityException e) {
                    throw new SecurityException("Indirect import checks prevents usage of expression", e);
                }
            }
        }

        /**
         * Returns the effective receiver type for nested array expressions.
         *
         * @param objectExpressionType the candidate receiver type
         * @return the component type for arrays, otherwise the original type
         */
        protected ClassNode getExpressionType(ClassNode objectExpressionType) {
            return objectExpressionType.isArray() ? getExpressionType(objectExpressionType.getComponentType()) : objectExpressionType;
        }

        /**
         * Checks that a given token is either in the allowed list or not in the disallowed list.
         *
         * @param token the token to be checked
         * @throws SecurityException if usage of this token is forbidden
         */
        protected void assertTokenAuthorized(final Token token) throws SecurityException {
            final int value = token.getType();
            if (disallowedTokens != null && disallowedTokens.contains(value)) {
                throw new SecurityException("Token " + token + " is not allowed");
            } else if (allowedTokens != null && !allowedTokens.contains(value)) {
                throw new SecurityException("Token " + token + " is not allowed");
            }
        }

        /**
         * Validates a block statement and then visits each nested statement.
         *
         * @param block the block statement to visit
         */
        @Override
        public void visitBlockStatement(final BlockStatement block) {
            assertStatementAuthorized(block);
            for (Statement statement : block.getStatements()) {
                statement.visit(this);
            }
        }

        /**
         * Validates a {@code for} loop and then visits its collection and body.
         *
         * @param forLoop the loop to visit
         */
        @Override
        public void visitForLoop(final ForStatement forLoop) {
            assertStatementAuthorized(forLoop);
            forLoop.getCollectionExpression().visit(this);
            forLoop.getLoopBlock().visit(this);
        }

        /**
         * Validates a {@code while} loop and then visits its condition and body.
         *
         * @param loop the loop to visit
         */
        @Override
        public void visitWhileLoop(final WhileStatement loop) {
            assertStatementAuthorized(loop);
            loop.getBooleanExpression().visit(this);
            loop.getLoopBlock().visit(this);
        }

        /**
         * Validates a {@code do}/{@code while} loop and then visits its body and condition.
         *
         * @param loop the loop to visit
         */
        @Override
        public void visitDoWhileLoop(final DoWhileStatement loop) {
            assertStatementAuthorized(loop);
            loop.getBooleanExpression().visit(this);
            loop.getLoopBlock().visit(this);
        }

        /**
         * Validates an if-else statement and then visits its condition and branches.
         *
         * @param ifElse the conditional statement to visit
         */
        @Override
        public void visitIfElse(final IfStatement ifElse) {
            assertStatementAuthorized(ifElse);
            ifElse.getBooleanExpression().visit(this);
            ifElse.getIfBlock().visit(this);
            ifElse.getElseBlock().visit(this);
        }

        /**
         * Validates an expression statement and then visits its expression.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitExpressionStatement(final ExpressionStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
        }

        /**
         * Validates a return statement and then visits its return value.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitReturnStatement(final ReturnStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
        }

        /**
         * Validates an assert statement and then visits its condition and message.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitAssertStatement(final AssertStatement statement) {
            assertStatementAuthorized(statement);
            statement.getBooleanExpression().visit(this);
            statement.getMessageExpression().visit(this);
        }

        /**
         * Validates a try-catch-finally statement and then visits all nested blocks.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitTryCatchFinally(final TryCatchStatement statement) {
            assertStatementAuthorized(statement);
            statement.getTryStatement().visit(this);
            for (CatchStatement catchStatement : statement.getCatchStatements()) {
                catchStatement.visit(this);
            }
            statement.getFinallyStatement().visit(this);
        }

        /**
         * Ignores empty statements.
         *
         * @param statement the empty statement
         */
        @Override
        public void visitEmptyStatement(EmptyStatement statement) {
            // noop
        }

        /**
         * Validates a switch statement and then visits its selector and branches.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitSwitch(final SwitchStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
            for (CaseStatement caseStatement : statement.getCaseStatements()) {
                caseStatement.visit(this);
            }
            statement.getDefaultStatement().visit(this);
        }

        /**
         * Validates a case statement and then visits its condition and body.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitCaseStatement(final CaseStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
            statement.getCode().visit(this);
        }

        /**
         * Validates a break statement.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitBreakStatement(final BreakStatement statement) {
            assertStatementAuthorized(statement);
        }

        /**
         * Validates a continue statement.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitContinueStatement(final ContinueStatement statement) {
            assertStatementAuthorized(statement);
        }

        /**
         * Validates a throw statement and then visits the thrown expression.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitThrowStatement(final ThrowStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
        }

        /**
         * Validates a synchronized statement and then visits its expression and body.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitSynchronizedStatement(final SynchronizedStatement statement) {
            assertStatementAuthorized(statement);
            statement.getExpression().visit(this);
            statement.getCode().visit(this);
        }

        /**
         * Validates a catch block and then visits its body.
         *
         * @param statement the statement to visit
         */
        @Override
        public void visitCatchStatement(final CatchStatement statement) {
            assertStatementAuthorized(statement);
            statement.getCode().visit(this);
        }

        /**
         * Validates a method call and then visits its receiver, name, and arguments.
         *
         * @param call the method call expression to visit
         */
        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            assertExpressionAuthorized(call);
            Expression receiver = call.getObjectExpression();
            final String typeName = receiver.getType().getName();
            if (allowedReceivers != null && !allowedReceivers.contains(typeName)) {
                throw new SecurityException("Method calls not allowed on [" + typeName + "]");
            } else if (disallowedReceivers != null && disallowedReceivers.contains(typeName)) {
                throw new SecurityException("Method calls not allowed on [" + typeName + "]");
            }
            receiver.visit(this);
            final Expression method = call.getMethod();
            checkConstantTypeIfNotMethodNameOrProperty(method);
            call.getArguments().visit(this);
        }

        /**
         * Validates a static method call and then visits its arguments.
         *
         * @param call the static method call to visit
         */
        @Override
        public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
            assertExpressionAuthorized(call);
            final String typeName = call.getOwnerType().getName();
            if (allowedReceivers != null && !allowedReceivers.contains(typeName)) {
                throw new SecurityException("Method calls not allowed on [" + typeName + "]");
            } else if (disallowedReceivers != null && disallowedReceivers.contains(typeName)) {
                throw new SecurityException("Method calls not allowed on [" + typeName + "]");
            }
            call.getArguments().visit(this);
        }

        /**
         * Validates a constructor call and then visits its arguments.
         *
         * @param call the constructor call to visit
         */
        @Override
        public void visitConstructorCallExpression(final ConstructorCallExpression call) {
            assertExpressionAuthorized(call);
            call.getArguments().visit(this);
        }

        /**
         * Validates a ternary expression and then visits all branches.
         *
         * @param expression the ternary expression to visit
         */
        @Override
        public void visitTernaryExpression(final TernaryExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getBooleanExpression().visit(this);
            expression.getTrueExpression().visit(this);
            expression.getFalseExpression().visit(this);
        }

        /**
         * Validates an Elvis expression and then delegates to ternary-expression handling.
         *
         * @param expression the Elvis expression to visit
         */
        @Override
        public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
            assertExpressionAuthorized(expression);
            visitTernaryExpression(expression);
        }

        /**
         * Validates a binary expression, its operator token, and both operands.
         *
         * @param expression the binary expression to visit
         */
        @Override
        public void visitBinaryExpression(final BinaryExpression expression) {
            assertExpressionAuthorized(expression);
            assertTokenAuthorized(expression.getOperation());
            expression.getLeftExpression().visit(this);
            expression.getRightExpression().visit(this);
        }

        /**
         * Validates a prefix expression and then visits its operand.
         *
         * @param expression the prefix expression to visit
         */
        @Override
        public void visitPrefixExpression(final PrefixExpression expression) {
            assertExpressionAuthorized(expression);
            assertTokenAuthorized(expression.getOperation());
            expression.getExpression().visit(this);
        }

        /**
         * Validates a postfix expression and then visits its operand.
         *
         * @param expression the postfix expression to visit
         */
        @Override
        public void visitPostfixExpression(final PostfixExpression expression) {
            assertExpressionAuthorized(expression);
            assertTokenAuthorized(expression.getOperation());
            expression.getExpression().visit(this);
        }

        /**
         * Validates a boolean expression and then visits its wrapped expression.
         *
         * @param expression the boolean expression to visit
         */
        @Override
        public void visitBooleanExpression(final BooleanExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a closure expression and then visits its body when closures are allowed.
         *
         * @param expression the closure expression to visit
         */
        @Override
        public void visitClosureExpression(final ClosureExpression expression) {
            assertExpressionAuthorized(expression);
            if (!isClosuresAllowed) throw new SecurityException("Closures are not allowed");
            expression.getCode().visit(this);
        }

        /**
         * Delegates lambda-expression validation to closure-expression handling.
         *
         * @param expression the lambda expression to visit
         */
        @Override
        public void visitLambdaExpression(LambdaExpression expression) {
            visitClosureExpression(expression);
        }

        /**
         * Validates a tuple expression and then visits its elements.
         *
         * @param expression the tuple expression to visit
         */
        @Override
        public void visitTupleExpression(final TupleExpression expression) {
            assertExpressionAuthorized(expression);
            visitListOfExpressions(expression.getExpressions());
        }

        /**
         * Validates a map expression and then visits its entries.
         *
         * @param expression the map expression to visit
         */
        @Override
        public void visitMapExpression(final MapExpression expression) {
            assertExpressionAuthorized(expression);
            visitListOfExpressions(expression.getMapEntryExpressions());
        }

        /**
         * Validates a map entry expression and then visits its key and value.
         *
         * @param expression the map entry expression to visit
         */
        @Override
        public void visitMapEntryExpression(final MapEntryExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getKeyExpression().visit(this);
            expression.getValueExpression().visit(this);
        }

        /**
         * Validates a list expression and then visits its elements.
         *
         * @param expression the list expression to visit
         */
        @Override
        public void visitListExpression(final ListExpression expression) {
            assertExpressionAuthorized(expression);
            visitListOfExpressions(expression.getExpressions());
        }

        /**
         * Validates a range expression and then visits both endpoints.
         *
         * @param expression the range expression to visit
         */
        @Override
        public void visitRangeExpression(final RangeExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getFrom().visit(this);
            expression.getTo().visit(this);
        }

        /**
         * Validates a property expression and then visits its receiver and property.
         *
         * @param expression the property expression to visit
         */
        @Override
        public void visitPropertyExpression(final PropertyExpression expression) {
            assertExpressionAuthorized(expression);
            Expression receiver = expression.getObjectExpression();
            final String typeName = receiver.getType().getName();
            if (allowedReceivers != null && !allowedReceivers.contains(typeName)) {
                throw new SecurityException("Property access not allowed on [" + typeName + "]");
            } else if (disallowedReceivers != null && disallowedReceivers.contains(typeName)) {
                throw new SecurityException("Property access not allowed on [" + typeName + "]");
            }
            receiver.visit(this);
            final Expression property = expression.getProperty();
            checkConstantTypeIfNotMethodNameOrProperty(property);
        }

        private void checkConstantTypeIfNotMethodNameOrProperty(final Expression expr) {
            if (expr instanceof ConstantExpression) {
                if (!"java.lang.String".equals(expr.getType().getName())) {
                    expr.visit(this);
                }
            } else {
                expr.visit(this);
            }
        }

        /**
         * Validates an attribute expression and then visits its receiver and attribute.
         *
         * @param expression the attribute expression to visit
         */
        @Override
        public void visitAttributeExpression(final AttributeExpression expression) {
            assertExpressionAuthorized(expression);
            Expression receiver = expression.getObjectExpression();
            final String typeName = receiver.getType().getName();
            if (allowedReceivers != null && !allowedReceivers.contains(typeName)) {
                throw new SecurityException("Attribute access not allowed on [" + typeName + "]");
            } else if (disallowedReceivers != null && disallowedReceivers.contains(typeName)) {
                throw new SecurityException("Attribute access not allowed on [" + typeName + "]");
            }
            receiver.visit(this);
            final Expression property = expression.getProperty();
            checkConstantTypeIfNotMethodNameOrProperty(property);
        }

        /**
         * Validates a field expression.
         *
         * @param expression the field expression to visit
         */
        @Override
        public void visitFieldExpression(final FieldExpression expression) {
            assertExpressionAuthorized(expression);
        }

        /**
         * Validates a method-pointer expression and then visits its target and method name.
         *
         * @param expression the method-pointer expression to visit
         */
        @Override
        public void visitMethodPointerExpression(final MethodPointerExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
            expression.getMethodName().visit(this);
        }

        /**
         * Delegates method-reference validation to method-pointer handling.
         *
         * @param expression the method-reference expression to visit
         */
        @Override
        public void visitMethodReferenceExpression(final MethodReferenceExpression expression) {
            visitMethodPointerExpression(expression);
        }

        /**
         * Validates a constant expression and its constant type.
         *
         * @param expression the constant expression to visit
         */
        @Override
        public void visitConstantExpression(final ConstantExpression expression) {
            assertExpressionAuthorized(expression);
            final String type = expression.getType().getName();
            if (allowedConstantTypes != null && !allowedConstantTypes.contains(type)) {
                throw new SecurityException("Constant expression type [" + type + "] is not allowed");
            }
            if (disallowedConstantTypes != null && disallowedConstantTypes.contains(type)) {
                throw new SecurityException("Constant expression type [" + type + "] is not allowed");
            }
        }

        /**
         * Validates a class expression.
         *
         * @param expression the class expression to visit
         */
        @Override
        public void visitClassExpression(final ClassExpression expression) {
            assertExpressionAuthorized(expression);
        }

        /**
         * Validates a variable expression and its inferred type.
         *
         * @param expression the variable expression to visit
         */
        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            assertExpressionAuthorized(expression);
            final String type = expression.getType().getName();
            if (allowedConstantTypes != null && !allowedConstantTypes.contains(type)) {
                throw new SecurityException("Usage of variables of type [" + type + "] is not allowed");
            }
            if (disallowedConstantTypes != null && disallowedConstantTypes.contains(type)) {
                throw new SecurityException("Usage of variables of type [" + type + "] is not allowed");
            }
        }

        /**
         * Validates a declaration expression via binary-expression handling.
         *
         * @param expression the declaration expression to visit
         */
        @Override
        public void visitDeclarationExpression(final DeclarationExpression expression) {
            assertExpressionAuthorized(expression);
            visitBinaryExpression(expression);
        }

        /**
         * Validates a GString expression and then visits its string and value parts.
         *
         * @param expression the GString expression to visit
         */
        @Override
        public void visitGStringExpression(final GStringExpression expression) {
            assertExpressionAuthorized(expression);
            visitListOfExpressions(expression.getStrings());
            visitListOfExpressions(expression.getValues());
        }

        /**
         * Validates an array expression and then visits its elements and size expressions.
         *
         * @param expression the array expression to visit
         */
        @Override
        public void visitArrayExpression(final ArrayExpression expression) {
            assertExpressionAuthorized(expression);
            visitListOfExpressions(expression.getExpressions());
            visitListOfExpressions(expression.getSizeExpression());
        }

        /**
         * Validates a spread expression and then visits its nested expression.
         *
         * @param expression the spread expression to visit
         */
        @Override
        public void visitSpreadExpression(final SpreadExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a spread-map expression and then visits its nested expression.
         *
         * @param expression the spread-map expression to visit
         */
        @Override
        public void visitSpreadMapExpression(final SpreadMapExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a logical-not expression and then visits its operand.
         *
         * @param expression the not expression to visit
         */
        @Override
        public void visitNotExpression(final NotExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a unary-minus expression and then visits its operand.
         *
         * @param expression the unary-minus expression to visit
         */
        @Override
        public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a unary-plus expression and then visits its operand.
         *
         * @param expression the unary-plus expression to visit
         */
        @Override
        public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a bitwise-negation expression and then visits its operand.
         *
         * @param expression the bitwise-negation expression to visit
         */
        @Override
        public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates a cast expression and then visits its operand.
         *
         * @param expression the cast expression to visit
         */
        @Override
        public void visitCastExpression(final CastExpression expression) {
            assertExpressionAuthorized(expression);
            expression.getExpression().visit(this);
        }

        /**
         * Validates an argument-list expression and then visits its elements.
         *
         * @param expression the argument-list expression to visit
         */
        @Override
        public void visitArgumentlistExpression(final ArgumentListExpression expression) {
            assertExpressionAuthorized(expression);
            visitTupleExpression(expression);
        }

        /**
         * Validates a closure-list expression and then visits its nested expressions.
         *
         * @param closureListExpression the closure-list expression to visit
         */
        @Override
        public void visitClosureListExpression(final ClosureListExpression closureListExpression) {
            assertExpressionAuthorized(closureListExpression);
            if (!isClosuresAllowed) throw new SecurityException("Closures are not allowed");
            visitListOfExpressions(closureListExpression.getExpressions());
        }

        /**
         * Validates a bytecode expression.
         *
         * @param expression the bytecode expression to visit
         */
        @Override
        public void visitBytecodeExpression(final BytecodeExpression expression) {
            assertExpressionAuthorized(expression);
        }
    }

    /**
     * This interface allows the user to provide a custom expression checker if the dis/allowed expression lists are not
     * sufficient
     */
    @FunctionalInterface
    public interface ExpressionChecker {
        /**
         * Determines whether the supplied expression is authorized.
         *
         * @param expression the expression to inspect
         * @return {@code true} if the expression is allowed
         */
        boolean isAuthorized(Expression expression);
    }

    /**
     * This interface allows the user to provide a custom statement checker if the dis/allowed statement lists are not
     * sufficient
     */
    @FunctionalInterface
    public interface StatementChecker {
        /**
         * Determines whether the supplied statement is authorized.
         *
         * @param expression the statement to inspect
         * @return {@code true} if the statement is allowed
         */
        boolean isAuthorized(Statement expression);
    }
}
