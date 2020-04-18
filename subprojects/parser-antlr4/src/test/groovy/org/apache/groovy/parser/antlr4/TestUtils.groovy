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

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.apache.groovy.parser.antlr4.util.ASTComparatorCategory
import org.apache.groovy.parser.antlr4.util.AstDumper
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ParserPlugin
import org.codehaus.groovy.control.ParserPluginFactory
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token

import java.security.AccessController
import java.security.PrivilegedAction
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@CompileStatic @AutoFinal @Log
final class TestUtils {

    public static final List<Class> COMMON_IGNORE_CLASS_LIST = [AssertStatement, BreakStatement, ConstructorNode, ContinueStatement, ExpressionStatement, FieldNode, ForStatement, GenericsType, IfStatement, MethodNode, PackageNode, Parameter, PropertyNode, ReturnStatement, ThrowStatement, Token, WhileStatement].asUnmodifiable()

    public static final String RESOURCES_PATH = Optional.of('subprojects/parser-antlr4/src/test/resources').filter(path -> new File(path).exists()).orElse('src/test/resources')

    static doTest(String path) {
        doTest(path, ASTComparatorCategory.DEFAULT_CONFIGURATION)
    }

    static doTest(String path, conf) {
        doTest(path, conf, new CompilerConfiguration(CompilerConfiguration.DEFAULT))
    }

    static doTest(String path, Collection<Class> ignoreSourcePosition) {
        doTest(path, addIgnore(ignoreSourcePosition, ASTComparatorCategory.LOCATION_IGNORE_LIST))
    }

    @CompileDynamic
    static doTest(String path, conf, CompilerConfiguration compilerConfiguration) {
        def file = new File("$RESOURCES_PATH/$path")
        def (newAST, newElapsedTime) = profile { buildAST(file, getAntlr4Config(compilerConfiguration)) }
//        def (oldAST, oldElapsedTime) = profile { buildAST(file, getAntlr2Config(compilerConfiguration)) }

//        assertAST(newAST, oldAST, conf)

//        def diffInMillis = newElapsedTime - oldElapsedTime
//        if (diffInMillis >= 500) {
//            log.warning "${path}\t\t\t\t\tdiff:${diffInMillis / 1000}s,\tnew:${newElapsedTime / 1000}s,\told:${oldElapsedTime / 1000}s."
//        }

        return [newAST, null]
    }

    static void shouldFail(String path, boolean toCheckNewParserOnly = false) {
        shouldFail(path, ASTComparatorCategory.DEFAULT_CONFIGURATION, toCheckNewParserOnly)
    }

    static void shouldFail(String path, Collection<Class> ignoreSourcePosition, boolean toCheckNewParserOnly = false) {
        shouldFail(path, addIgnore(ignoreSourcePosition, ASTComparatorCategory.LOCATION_IGNORE_LIST), toCheckNewParserOnly)
    }

    @CompileDynamic
    static void shouldFail(String path, conf, boolean toCheckNewParserOnly = false) {
        def file = new File("$RESOURCES_PATH/$path")
        def (newAST, newElapsedTime) = profile { buildAST(file, antlr4Config) }
//        def (oldAST, oldElapsedTime) = profile { buildAST(file, antlr2Config) }

        assert (newAST == null || newAST.context.errorCollector.hasErrors())
//        if (!toCheckNewParserOnly) {
//            assert (oldAST == null || oldAST.context.errorCollector.hasErrors())
//        }

//        def diffInMillis = newElapsedTime - oldElapsedTime
//        if (diffInMillis >= 500) {
//            log.warning "${path}\t\t\t\t\tdiff:${diffInMillis / 1000}s,\tnew:${newElapsedTime / 1000}s,\told:${oldElapsedTime / 1000}s."
//        }
    }

    /*
    static void unzipAndTest(String path, String entryName) {
        unzipAndTest(path, entryName, ASTComparatorCategory.DEFAULT_CONFIGURATION)
    }

    static void unzipAndTest(String path, String entryName, Collection<Class> ignoreSourcePosition) {
        unzipAndTest(path, entryName, addIgnore(ignoreSourcePosition, ASTComparatorCategory.LOCATION_IGNORE_LIST))
    }

    @CompileDynamic
    static void unzipAndTest(String path, String entryName, conf, Map<String, String> replacementsMap = null) {
        String text = readZipEntry(path, entryName)
        replacementsMap?.each { k, v ->
            text = text.replace(k, v)
        }

        def (newAST, newElapsedTime) = profile { buildAST(text, antlr4Config) }
        def (oldAST, oldElapsedTime) = profile { buildAST(text, antlr2Config) }

        assertAST(newAST, oldAST, conf)

        def diffInMillis = newElapsedTime - oldElapsedTime
        if (diffInMillis >= 500) {
            log.warning "${path}!${entryName}\t\t\t\t\tdiff:${diffInMillis / 1000}s,\tnew:${newElapsedTime / 1000}s,\told:${oldElapsedTime / 1000}s."
        }
    }
    */

    @CompileDynamic
    static unzipAndFail(String path, String entryName, conf, Map<String, String> replacementsMap = null, boolean toCheckNewParserOnly = false) {
        String text = readZipEntry(path, entryName)
        replacementsMap?.each { k, v ->
            text = text.replace(k, v)
        }

        def (newAST, newElapsedTime) = profile { buildAST(text, antlr4Config) }
//        def (oldAST, oldElapsedTime) = profile { buildAST(text, antlr2Config) }

        assert (newAST == null || newAST.context.errorCollector.hasErrors())
//        if (!toCheckNewParserOnly) {
//            assert (oldAST == null || oldAST.context.errorCollector.hasErrors())
//        }

//        def diffInMillis = newElapsedTime - oldElapsedTime
//        if (diffInMillis >= 500) {
//            log.warning "${path}!${entryName}\t\t\t\t\tdiff:${diffInMillis / 1000}s,\tnew:${newElapsedTime / 1000}s,\told:${oldElapsedTime / 1000}s."
//        }
    }

    static void assertAST(ModuleNode ast1, ModuleNode ast2, conf) {
        assert ast1 != null && ast2 != null

        ASTComparatorCategory.apply(conf) {
            assert ast1 == ast2
        }

        assert genSrc(ast1) == genSrc(ast2)
    }

    static String genSrc(ModuleNode ast) {
        return new AstDumper(ast).gen()
    }

    static Map<Class, List<String>> addIgnore(Collection<Class> c, List<String> ignore, Map<Class, List<String>> map = new HashMap<>(ASTComparatorCategory.DEFAULT_CONFIGURATION)) {
        c.each { map[it].addAll(ignore) }
        return map
    }

    static void doRunAndShouldFail(String path) {
        assert !executeScript(createAntlr4Shell(), path)
    }

    static void doRunAndTest(String path) {
//        doRunAndTestAntlr2(path)
        doRunAndTestAntlr4(path)
    }

    static void doRunAndTestAntlr4(String path, CompilerConfiguration compilerConfiguration = CompilerConfiguration.DEFAULT) {
        assert executeScript(createAntlr4Shell(compilerConfiguration), "$RESOURCES_PATH/$path")
    }

    static void doRunAndTestAntlr2(String path, CompilerConfiguration compilerConfiguration = CompilerConfiguration.DEFAULT) {
        throw new UnsupportedOperationException("Antlr2 is no longer supported")
    }

    static GroovyShell createAntlr4Shell(CompilerConfiguration compilerConfiguration = CompilerConfiguration.DEFAULT) {
        return new GroovyShell(getAntlr4Config(compilerConfiguration))
    }

    static GroovyShell createAntlr2Shell(CompilerConfiguration compilerConfiguration = CompilerConfiguration.DEFAULT) {
        throw new UnsupportedOperationException("Antlr2 is no longer supported")
    }

    //--------------------------------------------------------------------------

    private static CompilerConfiguration getAntlr4Config(CompilerConfiguration config = CompilerConfiguration.DEFAULT) {
        return new CompilerConfiguration(config).tap {
            pluginFactory = ParserPluginFactory.antlr4()
        }
    }

    private static ModuleNode buildAST(File sourceFile, CompilerConfiguration config) {
        def loader = AccessController.doPrivileged({ ->
            new GroovyClassLoader()
        } as PrivilegedAction<GroovyClassLoader>)

        try {
            new SourceUnit(sourceFile, config, loader, null).with {
                parse()
                completePhase()
                nextPhase()
                convert()

                return AST
            }
        } catch (e) {
            log.info("Failed to parse ${sourceFile.path}")
            return null
        }
    }

    private static ModuleNode buildAST(String sourceText, CompilerConfiguration config) {
        def loader = AccessController.doPrivileged({ ->
            new GroovyClassLoader()
        } as PrivilegedAction<GroovyClassLoader>)

        try {
            ParserPlugin.buildAST(sourceText, config, loader, null)
        } catch (e) {
            log.info("Failed to parse")
            return null
        }
    }

    private static boolean executeScript(GroovyShell shell, String path) {
        def file = new File(path)
        try {
            shell.evaluate(file.text)
            return true
        } catch (Throwable t) {
            log.info("Failed $path: ${t.getMessage()}")
            return false
        }
    }

    private static String readZipEntry(String path, String entryName) {
        String result = ''

        try (zf = new ZipFile(new File(path))) {
            def is = new BufferedInputStream(zf.getInputStream(new ZipEntry(entryName)))
            result = is.getText('UTF-8')
        } catch (e) {
            log.severe(e.message)
        }

        return result
    }

    private static <T> Tuple2<T, Long> profile(Closure<T> c) {
        long t0 = System.currentTimeMillis()
        def result = c.call()
        long t1 = System.currentTimeMillis()

        Tuple.tuple(result, t1 - t0)
    }
}
