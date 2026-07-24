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
package org.apache.groovy.parser.antlr4

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.InputMismatchException
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.apache.groovy.parser.antlr4.internal.DescriptiveErrorStrategy
import org.apache.groovy.parser.antlr4.internal.RecoveringDescriptiveErrorStrategy
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.StringReaderSource
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.control.CompilerConfiguration.ERROR_RECOVERY
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-9192: optional ANTLR error recovery for the Parrot parser.
 * <p>
 * Recovery is off by default (fail-fast). Enabling {@code errorRecovery}
 * resynchronizes after recognition errors so multiple diagnostics can be
 * collected in one pass (IDE editing). Hosts should trust
 * {@link ErrorCollector} as the multi-error source of truth.
 * </p>
 */
final class Groovy9192 {

    //--------------------------------------------------------------------------
    // Configuration
    //--------------------------------------------------------------------------

    @Test
    void 'error recovery is disabled by default'() {
        assertFalse new CompilerConfiguration().errorRecoveryEnabled
        assertFalse CompilerConfiguration.DEFAULT.errorRecoveryEnabled
        assertEquals 'errorRecovery', ERROR_RECOVERY
    }

    @Test
    void 'error recovery can be enabled via optimization option'() {
        def config = new CompilerConfiguration()
        config.optimizationOptions[ERROR_RECOVERY] = true
        assertTrue config.errorRecoveryEnabled

        config.optimizationOptions[ERROR_RECOVERY] = false
        assertFalse config.errorRecoveryEnabled

        config.optimizationOptions[ERROR_RECOVERY] = null
        assertFalse config.errorRecoveryEnabled
    }

    @Test
    void 'copy constructor preserves error recovery option'() {
        def source = new CompilerConfiguration()
        source.optimizationOptions[ERROR_RECOVERY] = true
        assertTrue new CompilerConfiguration(source).errorRecoveryEnabled
    }

    @Test
    void 'DescriptiveErrorStrategy create selects fail-fast or recovering strategy'() {
        def stream = CharStreams.fromString('1')
        assertInstanceOf DescriptiveErrorStrategy, DescriptiveErrorStrategy.create(stream, false)
        assertInstanceOf RecoveringDescriptiveErrorStrategy, DescriptiveErrorStrategy.create(stream, true)
    }

    //--------------------------------------------------------------------------
    // Fail-fast (default) — compatibility with existing contracts
    //--------------------------------------------------------------------------

    @Test
    void 'default fail-fast still reports a friendly syntax error'() {
        def errors = collectErrors('class C {\n  def x = (\n', false)
        assertEquals 1, errors.size(), "fail-fast must stop at first fatal: $errors"
        assertTrue errors.any { it.contains("Missing ')'") || it.toLowerCase().contains('unexpected') },
                "unexpected diagnostics: $errors"
    }

    @Test
    void 'default fail-fast cancels parse via ParseCancellationException'() {
        def charStream = CharStreams.fromString('class C {')
        def strategy = new DescriptiveErrorStrategy(charStream)

        def lexer = new GroovyLangLexer(charStream)
        def parser = new GroovyLangParser(new CommonTokenStream(lexer))
        parser.errorHandler = strategy
        parser.interpreter.predictionMode = PredictionMode.LL
        parser.removeErrorListeners()

        assertThrows(ParseCancellationException) {
            parser.compilationUnit()
        }
    }

    @Test
    void 'fail-fast recover marks incomplete rule contexts before cancelling'() {
        // Under SLL the strategy does not report (avoids needing a live ATN state);
        // it only marks contexts and cancels — the BailErrorStrategy contract.
        def charStream = CharStreams.fromString('}')
        def strategy = new DescriptiveErrorStrategy(charStream)
        def lexer = new GroovyLangLexer(charStream)
        def parser = new GroovyLangParser(new CommonTokenStream(lexer))
        parser.errorHandler = strategy
        parser.interpreter.predictionMode = PredictionMode.SLL
        parser.removeErrorListeners()

        def parent = new ParserRuleContext()
        def child = new ParserRuleContext(parent, 0)
        parser.context = child

        def e = new InputMismatchException(parser)
        def pce = assertThrows(ParseCancellationException) {
            strategy.recover(parser, e)
        }
        assertTrue pce.cause instanceof InputMismatchException
        assertEquals e, child.exception
        assertEquals e, parent.exception
    }

    @Test
    void 'fail-fast recoverInline cancels with InputMismatchException cause'() {
        def charStream = CharStreams.fromString('}')
        def strategy = new DescriptiveErrorStrategy(charStream)
        def lexer = new GroovyLangLexer(charStream)
        def parser = new GroovyLangParser(new CommonTokenStream(lexer))
        parser.errorHandler = strategy
        parser.interpreter.predictionMode = PredictionMode.SLL
        parser.removeErrorListeners()
        parser.context = new ParserRuleContext()

        def pce = assertThrows(ParseCancellationException) {
            strategy.recoverInline(parser)
        }
        assertTrue pce.cause instanceof InputMismatchException
    }

    @Test
    void 'fail-fast sync is a no-op'() {
        def charStream = CharStreams.fromString('1')
        def strategy = new DescriptiveErrorStrategy(charStream)
        def parser = new GroovyLangParser(new CommonTokenStream(new GroovyLangLexer(charStream)))
        parser.errorHandler = strategy
        def indexBefore = parser.inputStream.index()
        strategy.sync(parser)
        assertEquals indexBefore, parser.inputStream.index()
    }

    @Test
    void 'fail-fast collectSyntaxError path still aborts on first error'() {
        def errors = collectErrors('class C { def x = ( }', false)
        assertEquals 1, errors.size(), String.valueOf(errors)
    }

    //--------------------------------------------------------------------------
    // Recovery mode
    //--------------------------------------------------------------------------

    @Test
    void 'recovery mode reports more diagnostics than fail-fast for multi-fault source'() {
        // Two top-level broken classes so recovery can resync past the first fault.
        String source = '''\
            |class A {
            |  def x = (
            |}
            |class B {
            |  def y = [
            |}
            |'''.stripMargin()

        def failFastErrors = collectErrors(source, false)
        def recoveryErrors = collectErrors(source, true)

        assertEquals 1, failFastErrors.size(), "fail-fast: $failFastErrors"
        // Product claim: multi-error collection — recovery must surface strictly more
        // diagnostics than fail-fast (which stops at the first fatal recognition error).
        assertTrue recoveryErrors.size() > failFastErrors.size(),
                "recovery (${recoveryErrors.size()}) must report more than fail-fast (${failFastErrors.size()})\nfail-fast: $failFastErrors\nrecovery: $recoveryErrors"
        assertTrue recoveryErrors.size() >= 2, "recovery should collect multiple diagnostics: $recoveryErrors"
        assertTrue recoveryErrors.any { it.contains("Missing ')'") || it.contains("Missing ']'") || it.contains("Missing '}'") },
                "expected a Missing-delimiter diagnostic in: $recoveryErrors"
    }

    @Test
    void 'recovery mode still fails the compilation'() {
        def config = recoveryConfig()
        def cu = new CompilationUnit(config)
        cu.addSource('Broken.groovy', 'class C {\n  def x = (\n')
        assertThrows(CompilationFailedException) {
            cu.compile(Phases.CONVERSION)
        }
        assertTrue cu.errorCollector.hasErrors()
    }

    @Test
    void 'recovery mode keeps friendly Missing delimiter diagnostics'() {
        def errors = collectErrors('class C {\n  def x\n', true)
        assertTrue errors.any { it.contains("Missing '}'") }, "expected Missing '}', got: $errors"
    }

    @Test
    void 'recovery strategy resynchronizes instead of cancelling'() {
        def charStream = CharStreams.fromString('class C { def x = ( } class D {}')
        def strategy = new RecoveringDescriptiveErrorStrategy(charStream)

        def lexer = new GroovyLangLexer(charStream)
        def parser = new GroovyLangParser(new CommonTokenStream(lexer))
        parser.errorHandler = strategy
        parser.interpreter.predictionMode = PredictionMode.LL
        parser.removeErrorListeners()

        assertNotNull parser.compilationUnit()
    }

    @Test
    void 'AstBuilder wires recovery from CompilerConfiguration for valid source'() {
        def config = recoveryConfig()
        def sourceUnit = new SourceUnit(
                't.groovy',
                new StringReaderSource('class C {}', config),
                config,
                null,
                new ErrorCollector(config)
        )
        def module = new AstBuilder(sourceUnit, false, false).buildAST()
        assertNotNull module
        assertFalse sourceUnit.errorCollector.hasErrors()
    }

    @Test
    void 'recovery buildAST leaves module and collector for CompilationUnit to fail'() {
        // Recovery must not require SourceUnit to swallow CompilationFailedException:
        // AstBuilder returns a module with diagnostics already in the ErrorCollector.
        def config = recoveryConfig()
        def cu = new CompilationUnit(config)
        cu.addSource('Broken.groovy', 'class C {\n  def x = (\n')
        assertThrows(CompilationFailedException) {
            cu.compile(Phases.CONVERSION)
        }
        def unit = cu.sources.values().iterator().next()
        assertNotNull unit.AST, 'recovery should materialize a module without SourceUnit CFE catch'
        assertTrue unit.errorCollector.hasErrors()
    }

    @Test
    void 'fail-fast AstBuilder aborts via fatal ErrorCollector path'() {
        // Exercises collectSyntaxError → addFatalError (not only recovery's addErrorAndContinue).
        def config = new CompilerConfiguration()
        def sourceUnit = new SourceUnit(
                'broken.groovy',
                new StringReaderSource('class C {\n  def x = (\n', config),
                config,
                null,
                new ErrorCollector(config)
        )
        assertThrows(CompilationFailedException) {
            new AstBuilder(sourceUnit, false, false).buildAST()
        }
        assertTrue sourceUnit.errorCollector.hasErrors()
        assertEquals 1, sourceUnit.errorCollector.errorCount
    }

    @Test
    void 'recovery AstBuilder returns module with accumulated diagnostics for unclosed delimiters'() {
        // Recovering parse finishes CST; AstBuilder returns a module and keeps
        // diagnostics in the collector for CompilationUnit.failIfErrors().
        def config = recoveryConfig()
        def sourceUnit = new SourceUnit(
                'partial.groovy',
                new StringReaderSource('class C {\n  def x = (\n  def y = [\n', config),
                config,
                null,
                new ErrorCollector(config)
        )
        def module = new AstBuilder(sourceUnit, false, false).buildAST()
        assertNotNull module
        assertTrue sourceUnit.errorCollector.hasErrors()
        assertTrue sourceUnit.errorCollector.errorCount >= 1
    }

    //--------------------------------------------------------------------------
    // helpers
    //--------------------------------------------------------------------------

    private static CompilerConfiguration recoveryConfig() {
        def config = new CompilerConfiguration()
        config.optimizationOptions[ERROR_RECOVERY] = true
        config
    }

    /**
     * Compile {@code source} through CONVERSION and return syntax error messages
     * (empty if compilation somehow succeeded).
     */
    private static List<String> collectErrors(String source, boolean recovery) {
        def config = new CompilerConfiguration()
        if (recovery) {
            config.optimizationOptions[ERROR_RECOVERY] = true
        }
        def cu = new CompilationUnit(config)
        cu.addSource('test.groovy', source)
        try {
            cu.compile(Phases.CONVERSION)
            return Collections.emptyList()
        } catch (CompilationFailedException ignored) {
            return cu.errorCollector.errors
                    .findAll { it instanceof SyntaxErrorMessage }
                    .collect { ((SyntaxErrorMessage) it).cause.message }
        }
    }
}
