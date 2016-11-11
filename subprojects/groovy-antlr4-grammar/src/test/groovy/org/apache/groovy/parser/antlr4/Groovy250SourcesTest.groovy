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

import org.apache.groovy.parser.antlr4.util.ASTComparatorCategory

/**
 * Add Groovy 2.5.0 sources as test cases
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/09/22
 */
class Groovy250SourcesTest extends GroovyTestCase {


    void "test benchmark/bench.groovy"() {
        unzipAndTest("benchmark/bench.groovy", [])
    }

    void "test benchmark/bench/ackermann.groovy"() {
        unzipAndTest("benchmark/bench/ackermann.groovy", [])
    }

    void "test benchmark/bench/ary.groovy"() {
        unzipAndTest("benchmark/bench/ary.groovy", [])
    }

    void "test benchmark/bench/binarytrees.groovy"() {
        unzipAndTest("benchmark/bench/binarytrees.groovy", [])
    }

    void "test benchmark/bench/fannkuch.groovy"() {
        unzipAndTest("benchmark/bench/fannkuch.groovy", [])
    }

    void "test benchmark/bench/fibo.groovy"() {
        unzipAndTest("benchmark/bench/fibo.groovy", [])
    }

    void "test benchmark/bench/heapsort.groovy"() {
        unzipAndTest("benchmark/bench/heapsort.groovy", [])
    }

    void "test benchmark/bench/hello.groovy"() {
        unzipAndTest("benchmark/bench/hello.groovy", [])
    }

    void "test benchmark/bench/mandelbrot.groovy"() {
        unzipAndTest("benchmark/bench/mandelbrot.groovy", [])
    }

    void "test benchmark/bench/nsieve.groovy"() {
        unzipAndTest("benchmark/bench/nsieve.groovy", [])
    }

    void "test benchmark/bench/random.groovy"() {
        unzipAndTest("benchmark/bench/random.groovy", [])
    }

    void "test benchmark/bench/recursive.groovy"() {
        unzipAndTest("benchmark/bench/recursive.groovy", [])
    }

    void "test benchmark/bench/regexdna.groovy"() {
        unzipAndTest("benchmark/bench/regexdna.groovy", [])
    }

    void "test benchmark/bench/revcomp.groovy"() {
        unzipAndTest("benchmark/bench/revcomp.groovy", [])
    }

    void "test benchmark/bench/spectralnorm.groovy"() {
        unzipAndTest("benchmark/bench/spectralnorm.groovy", [])
    }

    void "test benchmark/bench/threadring.groovy"() {
        unzipAndTest("benchmark/bench/threadring.groovy", [])
    }

    void "test benchmark/bench/wordfreq.groovy"() {
        unzipAndTest("benchmark/bench/wordfreq.groovy", [])
    }

    void "test buildSrc/src/main/groovy/org/codehaus/groovy/gradle/WriteExtensionDescriptorTask.groovy"() {
        unzipAndTest("buildSrc/src/main/groovy/org/codehaus/groovy/gradle/WriteExtensionDescriptorTask.groovy", [])
    }

    void "test config/binarycompatibility/binarycompat-report.groovy"() {
        unzipAndTest("config/binarycompatibility/binarycompat-report.groovy", [])
    }

    void "test config/checkstyle/checkstyle-report.groovy"() {
        unzipAndTest("config/checkstyle/checkstyle-report.groovy", [])
    }

    void "test config/codenarc/codenarc.groovy"() {
        unzipAndTest("config/codenarc/codenarc.groovy", [])
    }

    void "test src/examples/astbuilder/Main.groovy"() {
        unzipAndTest("src/examples/astbuilder/Main.groovy", [])
    }

    void "test src/examples/astbuilder/MainExample.groovy"() {
        unzipAndTest("src/examples/astbuilder/MainExample.groovy", [])
    }

    void "test src/examples/astbuilder/MainIntegrationTest.groovy"() {
        unzipAndTest("src/examples/astbuilder/MainIntegrationTest.groovy", [])
    }

    void "test src/examples/astbuilder/MainTransformation.groovy"() {
        unzipAndTest("src/examples/astbuilder/MainTransformation.groovy", [])
    }

    void "test src/examples/commandLineTools/AntMap.groovy"() {
        unzipAndTest("src/examples/commandLineTools/AntMap.groovy", [])
    }

    void "test src/examples/commandLineTools/BigTests.groovy"() {
        unzipAndTest("src/examples/commandLineTools/BigTests.groovy", [])
    }

    void "test src/examples/commandLineTools/ListFiles.groovy"() {
        unzipAndTest("src/examples/commandLineTools/ListFiles.groovy", [])
    }

    void "test src/examples/commandLineTools/Reflections.groovy"() {
        unzipAndTest("src/examples/commandLineTools/Reflections.groovy", [])
    }

    void "test src/examples/commandLineTools/SimpleWebServer.groovy"() {
        unzipAndTest("src/examples/commandLineTools/SimpleWebServer.groovy", [])
    }

    void "test src/examples/console/MortgageCalculator.groovy"() {
        unzipAndTest("src/examples/console/MortgageCalculator.groovy", [])
    }

    void "test src/examples/console/knowYourTables.groovy"() {
        unzipAndTest("src/examples/console/knowYourTables.groovy", [])
    }

    void "test src/examples/console/thinkOfANumber.groovy"() {
        unzipAndTest("src/examples/console/thinkOfANumber.groovy", [])
    }

    void "test src/examples/groovy2d/paintingByNumbers.groovy"() {
        unzipAndTest("src/examples/groovy2d/paintingByNumbers.groovy", [])
    }

    void "test src/examples/groovyShell/ArithmeticShell.groovy"() {
        unzipAndTest("src/examples/groovyShell/ArithmeticShell.groovy", [])
    }

    void "test src/examples/groovyShell/ArithmeticShellTest.groovy"() {
        unzipAndTest("src/examples/groovyShell/ArithmeticShellTest.groovy", [])
    }

    void "test src/examples/groovyShell/BlacklistingShell.groovy"() {
        unzipAndTest("src/examples/groovyShell/BlacklistingShell.groovy", [])
    }

    void "test src/examples/groovyShell/BlacklistingShellTest.groovy"() {
        unzipAndTest("src/examples/groovyShell/BlacklistingShellTest.groovy", [])
    }

    void "test src/examples/groovy/j2ee/CreateData.groovy"() {
        unzipAndTest("src/examples/groovy/j2ee/CreateData.groovy", [])
    }

    void "test src/examples/groovy/model/MvcDemo.groovy"() {
        unzipAndTest("src/examples/groovy/model/MvcDemo.groovy", [])
    }

    void "test src/examples/groovy/swing/SwingDemo.groovy"() {
        unzipAndTest("src/examples/groovy/swing/SwingDemo.groovy", [])
    }

    void "test src/examples/groovy/swing/TableDemo.groovy"() {
        unzipAndTest("src/examples/groovy/swing/TableDemo.groovy", [])
    }

    void "test src/examples/groovy/swing/TableLayoutDemo.groovy"() {
        unzipAndTest("src/examples/groovy/swing/TableLayoutDemo.groovy", [])
    }

    void "test src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/Activator.groovy"() {
        unzipAndTest("src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/Activator.groovy", [])
    }

    void "test src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/GroovyGreeter.groovy"() {
        unzipAndTest("src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/GroovyGreeter.groovy", [])
    }

    void "test src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/GroovyGreeterImpl.groovy"() {
        unzipAndTest("src/examples/osgi/hello-groovy-bundle/org/codehaus/groovy/osgi/GroovyGreeterImpl.groovy", [])
    }

    void "test src/examples/osgi/hello-groovy-test-harness/org/codehaus/groovy/osgi/harness/HarnessActivator.groovy"() {
        unzipAndTest("src/examples/osgi/hello-groovy-test-harness/org/codehaus/groovy/osgi/harness/HarnessActivator.groovy", [])
    }

    void "test src/examples/searchEngine/Indexer.groovy"() {
        unzipAndTest("src/examples/searchEngine/Indexer.groovy", [])
    }

    void "test src/examples/searchEngine/Searcher.groovy"() {
        unzipAndTest("src/examples/searchEngine/Searcher.groovy", [])
    }

    void "test src/examples/swing/BindingExample.groovy"() {
        unzipAndTest("src/examples/swing/BindingExample.groovy", [])
    }

    void "test src/examples/swing/BloglinesClient.groovy"() {
        unzipAndTest("src/examples/swing/BloglinesClient.groovy", [])
    }

    void "test src/examples/swing/ModelNodeExample.groovy"() {
        unzipAndTest("src/examples/swing/ModelNodeExample.groovy", [])
    }

    void "test src/examples/swing/RegexCoach.groovy"() {
        unzipAndTest("src/examples/swing/RegexCoach.groovy", [])
    }

    void "test src/examples/swing/RegexCoachController.groovy"() {
        unzipAndTest("src/examples/swing/RegexCoachController.groovy", [])
    }

    void "test src/examples/swing/RegexCoachView.groovy"() {
        unzipAndTest("src/examples/swing/RegexCoachView.groovy", [])
    }

    void "test src/examples/swing/Widgets.groovy"() {
        unzipAndTest("src/examples/swing/Widgets.groovy", [])
    }

    void "test src/examples/swing/binding/caricature/Caricature.groovy"() {
        unzipAndTest("src/examples/swing/binding/caricature/Caricature.groovy", [])
    }

    void "test src/examples/swing/greet/Greet.groovy"() {
        unzipAndTest("src/examples/swing/greet/Greet.groovy", [])
    }

    void "test src/examples/swing/greet/TwitterAPI.groovy"() {
        unzipAndTest("src/examples/swing/greet/TwitterAPI.groovy", [])
    }

    void "test src/examples/swing/greet/View.groovy"() {
        unzipAndTest("src/examples/swing/greet/View.groovy", [])
    }

    void "test src/examples/swing/timelog/TimeLogMain.groovy"() {
        unzipAndTest("src/examples/swing/timelog/TimeLogMain.groovy", [])
    }

    void "test src/examples/swing/timelog/TimeLogModel.groovy"() {
        unzipAndTest("src/examples/swing/timelog/TimeLogModel.groovy", [])
    }

    void "test src/examples/swing/timelog/TimeLogView.groovy"() {
        unzipAndTest("src/examples/swing/timelog/TimeLogView.groovy", [])
    }

    void "test src/examples/transforms/global/CompiledAtASTTransformation.groovy"() {
        unzipAndTest("src/examples/transforms/global/CompiledAtASTTransformation.groovy", [])
    }

    void "test src/examples/transforms/global/CompiledAtExample.groovy"() {
        unzipAndTest("src/examples/transforms/global/CompiledAtExample.groovy", [])
    }

    void "test src/examples/transforms/global/CompiledAtIntegrationTest.groovy"() {
        unzipAndTest("src/examples/transforms/global/CompiledAtIntegrationTest.groovy", [])
    }

    void "test src/examples/transforms/global/LoggingASTTransformation.groovy"() {
        unzipAndTest("src/examples/transforms/global/LoggingASTTransformation.groovy", [])
    }

    void "test src/examples/transforms/global/LoggingExample.groovy"() {
        unzipAndTest("src/examples/transforms/global/LoggingExample.groovy", [])
    }

    void "test src/examples/transforms/local/LoggingASTTransformation.groovy"() {
        unzipAndTest("src/examples/transforms/local/LoggingASTTransformation.groovy", [])
    }

    void "test src/examples/transforms/local/LoggingExample.groovy"() {
        unzipAndTest("src/examples/transforms/local/LoggingExample.groovy", [])
    }

    void "test src/examples/transforms/local/WithLogging.groovy"() {
        unzipAndTest("src/examples/transforms/local/WithLogging.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/WEB-INF/groovy/Animal.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/WEB-INF/groovy/Animal.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/Fish.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/Fish.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/fish/Shark.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/fish/Shark.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/fish/Trout.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/WEB-INF/groovy/zoo/fish/Trout.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/hello/hello.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/hello/hello.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/index.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/index.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/zoo/HommingbergerGepardenforelle.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/zoo/HommingbergerGepardenforelle.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/zoo/visit.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/zoo/visit.groovy", [])
    }

    void "test src/examples/webapps/groovlet-examples/zoo/zoo.groovy"() {
        unzipAndTest("src/examples/webapps/groovlet-examples/zoo/zoo.groovy", [])
    }

    void "test src/main/groovy/beans/ListenerList.groovy"() {
        unzipAndTest("src/main/groovy/beans/ListenerList.groovy", [])
    }

    void "test src/main/groovy/beans/ListenerListASTTransformation.groovy"() {
        unzipAndTest("src/main/groovy/beans/ListenerListASTTransformation.groovy", [])
    }

    void "test src/main/groovy/cli/CliBuilderException.groovy"() {
        unzipAndTest("src/main/groovy/cli/CliBuilderException.groovy", [])
    }

    void "test src/main/groovy/cli/OptionField.groovy"() {
        unzipAndTest("src/main/groovy/cli/OptionField.groovy", [])
    }

    void "test src/main/groovy/cli/UnparsedField.groovy"() {
        unzipAndTest("src/main/groovy/cli/UnparsedField.groovy", [])
    }

    void "test src/main/groovy/grape/GrapeIvy.groovy"() {
        unzipAndTest("src/main/groovy/grape/GrapeIvy.groovy", [])
    }

    void "test src/main/groovy/transform/AutoExternalize.groovy"() {
        unzipAndTest("src/main/groovy/transform/AutoExternalize.groovy", [])
    }

    void "test src/main/groovy/transform/Canonical.groovy"() {
        unzipAndTest("src/main/groovy/transform/Canonical.groovy", [])
    }

    void "test src/main/groovy/transform/CompileDynamic.groovy"() {
        unzipAndTest("src/main/groovy/transform/CompileDynamic.groovy", [])
    }

    void "test src/main/groovy/transform/ConditionalInterrupt.groovy"() {
        unzipAndTest("src/main/groovy/transform/ConditionalInterrupt.groovy", [])
    }

    void "test src/main/groovy/transform/TailRecursive.groovy"() {
        unzipAndTest("src/main/groovy/transform/TailRecursive.groovy", [])
    }

    void "test src/main/groovy/transform/ThreadInterrupt.groovy"() {
        unzipAndTest("src/main/groovy/transform/ThreadInterrupt.groovy", [])
    }

    void "test src/main/groovy/transform/TimedInterrupt.groovy"() {
        unzipAndTest("src/main/groovy/transform/TimedInterrupt.groovy", [])
    }

    void "test src/main/groovy/util/CliBuilder.groovy"() {
        unzipAndTest("src/main/groovy/util/CliBuilder.groovy", [])
    }

    void "test src/main/groovy/util/ConfigSlurper.groovy"() {
        unzipAndTest("src/main/groovy/util/ConfigSlurper.groovy", [])
    }

    void "test src/main/groovy/util/FileNameByRegexFinder.groovy"() {
        unzipAndTest("src/main/groovy/util/FileNameByRegexFinder.groovy", [])
    }

    void "test src/main/groovy/util/FileTreeBuilder.groovy"() {
        unzipAndTest("src/main/groovy/util/FileTreeBuilder.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/ast/builder/AstBuilder.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/ast/builder/AstBuilder.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/ast/builder/AstSpecificationCompiler.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/ast/builder/AstSpecificationCompiler.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/ast/builder/AstStringCompiler.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/ast/builder/AstStringCompiler.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/classgen/genArrayAccess.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/classgen/genArrayAccess.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/classgen/genArrays.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/classgen/genArrays.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/classgen/genDgmMath.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/classgen/genDgmMath.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/classgen/genMathModification.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/classgen/genMathModification.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/control/customizers/ASTTransformationCustomizer.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/control/customizers/ASTTransformationCustomizer.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/control/customizers/builder/ASTTransformationCustomizerFactory.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/control/customizers/builder/ASTTransformationCustomizerFactory.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/control/customizers/builder/CompilerCustomizationBuilder.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/control/customizers/builder/CompilerCustomizationBuilder.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/tools/GrapeMain.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/tools/GrapeMain.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/tools/ast/TransformTestHelper.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/tools/ast/TransformTestHelper.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/ASTTestTransformation.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/ASTTestTransformation.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/ConditionalInterruptibleASTTransformation.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/ConditionalInterruptibleASTTransformation.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/ThreadInterruptibleASTTransformation.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/ThreadInterruptibleASTTransformation.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/TimedInterruptibleASTTransformation.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/TimedInterruptibleASTTransformation.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/AstHelper.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/AstHelper.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/CollectRecursiveCalls.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/CollectRecursiveCalls.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/HasRecursiveCalls.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/HasRecursiveCalls.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/InWhileLoopWrapper.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/InWhileLoopWrapper.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/RecursivenessTester.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/RecursivenessTester.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/ReturnAdderForClosures.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/ReturnAdderForClosures.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/ReturnStatementToIterationConverter.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/ReturnStatementToIterationConverter.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/StatementReplacer.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/StatementReplacer.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/TailRecursiveASTTransformation.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/TailRecursiveASTTransformation.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/TernaryToIfStatementConverter.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/TernaryToIfStatementConverter.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/VariableAccessReplacer.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/VariableAccessReplacer.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/VariableExpressionReplacer.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/VariableExpressionReplacer.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/transform/tailrec/VariableExpressionTransformer.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/transform/tailrec/VariableExpressionTransformer.groovy", [])
    }

    void "test src/main/org/codehaus/groovy/util/StringUtil.groovy"() {
        unzipAndTest("src/main/org/codehaus/groovy/util/StringUtil.groovy", [])
    }

    void "test src/spec/test-resources/aftermethodcall.groovy"() {
        unzipAndTest("src/spec/test-resources/aftermethodcall.groovy", [])
    }

    void "test src/spec/test-resources/aftervisitclass.groovy"() {
        unzipAndTest("src/spec/test-resources/aftervisitclass.groovy", [])
    }

    void "test src/spec/test-resources/aftervisitmethod.groovy"() {
        unzipAndTest("src/spec/test-resources/aftervisitmethod.groovy", [])
    }

    void "test src/spec/test-resources/ambiguousmethods.groovy"() {
        unzipAndTest("src/spec/test-resources/ambiguousmethods.groovy", [])
    }

    void "test src/spec/test-resources/beforemethodcall.groovy"() {
        unzipAndTest("src/spec/test-resources/beforemethodcall.groovy", [])
    }

    void "test src/spec/test-resources/beforevisitclass.groovy"() {
        unzipAndTest("src/spec/test-resources/beforevisitclass.groovy", [])
    }

    void "test src/spec/test-resources/beforevisitmethod.groovy"() {
        unzipAndTest("src/spec/test-resources/beforevisitmethod.groovy", [])
    }

    void "test src/spec/test-resources/finish.groovy"() {
        unzipAndTest("src/spec/test-resources/finish.groovy", [])
    }

    void "test src/spec/test-resources/incompatibleassignment.groovy"() {
        unzipAndTest("src/spec/test-resources/incompatibleassignment.groovy", [])
    }

    void "test src/spec/test-resources/methodnotfound.groovy"() {
        unzipAndTest("src/spec/test-resources/methodnotfound.groovy", [])
    }

    void "test src/spec/test-resources/newmethod.groovy"() {
        unzipAndTest("src/spec/test-resources/newmethod.groovy", [])
    }

    void "test src/spec/test-resources/onmethodselection.groovy"() {
        unzipAndTest("src/spec/test-resources/onmethodselection.groovy", [])
    }

    void "test src/spec/test-resources/reloading/dependency1.groovy"() {
        unzipAndTest("src/spec/test-resources/reloading/dependency1.groovy", [])
    }

    void "test src/spec/test-resources/reloading/dependency2.groovy"() {
        unzipAndTest("src/spec/test-resources/reloading/dependency2.groovy", [])
    }

    void "test src/spec/test-resources/reloading/source1.groovy"() {
        unzipAndTest("src/spec/test-resources/reloading/source1.groovy", [])
    }

    void "test src/spec/test-resources/reloading/source2.groovy"() {
        unzipAndTest("src/spec/test-resources/reloading/source2.groovy", [])
    }

    void "test src/spec/test-resources/reloading/source3.groovy"() {
        unzipAndTest("src/spec/test-resources/reloading/source3.groovy", [])
    }

    void "test src/spec/test-resources/robotextension.groovy"() {
        unzipAndTest("src/spec/test-resources/robotextension.groovy", [])
    }

    void "test src/spec/test-resources/robotextension2.groovy"() {
        unzipAndTest("src/spec/test-resources/robotextension2.groovy", [])
    }

    void "test src/spec/test-resources/robotextension3.groovy"() {
        unzipAndTest("src/spec/test-resources/robotextension3.groovy", [])
    }

    void "test src/spec/test-resources/scoping.groovy"() {
        unzipAndTest("src/spec/test-resources/scoping.groovy", [])
    }

    void "test src/spec/test-resources/scoping_alt.groovy"() {
        unzipAndTest("src/spec/test-resources/scoping_alt.groovy", [])
    }

    void "test src/spec/test-resources/selfcheck.groovy"() {
        unzipAndTest("src/spec/test-resources/selfcheck.groovy", [])
    }

    void "test src/spec/test-resources/setup.groovy"() {
        unzipAndTest("src/spec/test-resources/setup.groovy", [])
    }

    void "test src/spec/test-resources/unresolvedattribute.groovy"() {
        unzipAndTest("src/spec/test-resources/unresolvedattribute.groovy", [])
    }

    void "test src/spec/test-resources/unresolvedproperty.groovy"() {
        unzipAndTest("src/spec/test-resources/unresolvedproperty.groovy", [])
    }

    void "test src/spec/test-resources/unresolvedvariable.groovy"() {
        unzipAndTest("src/spec/test-resources/unresolvedvariable.groovy", [])
    }

    void "test src/spec/test/BaseScriptSpecTest.groovy"() {
        unzipAndTest("src/spec/test/BaseScriptSpecTest.groovy", [])
    }

    void "test src/spec/test/ClassDesignASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/ClassDesignASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/ClassTest.groovy"() {
        unzipAndTest("src/spec/test/ClassTest.groovy", [])
    }

    void "test src/spec/test/CloningASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/CloningASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/ClosuresSpecTest.groovy"() {
        unzipAndTest("src/spec/test/ClosuresSpecTest.groovy", [])
    }

    void "test src/spec/test/CodeGenerationASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/CodeGenerationASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/CoercionTest.groovy"() {
        unzipAndTest("src/spec/test/CoercionTest.groovy", [])
    }

    void "test src/spec/test/CommandChainsTest.groovy"() {
        unzipAndTest("src/spec/test/CommandChainsTest.groovy", [])
    }

    void "test src/spec/test/CompilerDirectivesASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/CompilerDirectivesASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/CustomizersTest.groovy"() {
        unzipAndTest("src/spec/test/CustomizersTest.groovy", [])
    }

    void "test src/spec/test/DeclarativeConcurrencyASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/DeclarativeConcurrencyASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/DelegatesToSpecTest.groovy"() {
        unzipAndTest("src/spec/test/DelegatesToSpecTest.groovy", [])
    }

    void "test src/spec/test/DesignPatternsTest.groovy"() {
        unzipAndTest("src/spec/test/DesignPatternsTest.groovy", [])
    }

    void "test src/spec/test/DifferencesFromJavaTest.groovy"() {
        unzipAndTest("src/spec/test/DifferencesFromJavaTest.groovy", [])
    }

    void "test src/spec/test/ExtensionModuleSpecTest.groovy"() {
        unzipAndTest("src/spec/test/ExtensionModuleSpecTest.groovy", [])
    }

    void "test src/spec/test/IntegrationTest.groovy"() {
        unzipAndTest("src/spec/test/IntegrationTest.groovy", [])
    }

    void "test src/spec/test/LogImprovementsASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/LogImprovementsASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/OperatorsTest.groovy"() {
        unzipAndTest("src/spec/test/OperatorsTest.groovy", [])
    }

    void "test src/spec/test/PackageTest.groovy"() {
        unzipAndTest("src/spec/test/PackageTest.groovy", [])
    }

    void "test src/spec/test/PrimitiveTest.groovy"() {
        unzipAndTest("src/spec/test/PrimitiveTest.groovy", [])
    }

    void "test src/spec/test/SaferScriptingASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/SaferScriptingASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/ScriptsAndClassesSpecTest.groovy"() {
        unzipAndTest("src/spec/test/ScriptsAndClassesSpecTest.groovy", [])
    }

    void "test src/spec/test/SemanticsTest.groovy"() {
        unzipAndTest("src/spec/test/SemanticsTest.groovy", [])
    }

    void "test src/spec/test/SwingASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/SwingASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/SyntaxTest.groovy"() {
        unzipAndTest("src/spec/test/SyntaxTest.groovy", [])
    }

    void "test src/spec/test/TestingASTTransformsTest.groovy"() {
        unzipAndTest("src/spec/test/TestingASTTransformsTest.groovy", [])
    }

    void "test src/spec/test/TraitsSpecificationTest.groovy"() {
        unzipAndTest("src/spec/test/TraitsSpecificationTest.groovy", [])
    }

    void "test src/spec/test/asciidoctor/Utils.groovy"() {
        unzipAndTest("src/spec/test/asciidoctor/Utils.groovy", [])
    }

    void "test src/spec/test/builder/CliBuilderTest.groovy"() {
        unzipAndTest("src/spec/test/builder/CliBuilderTest.groovy", [])
    }

    void "test src/spec/test/builder/FileTreeBuilderTest.groovy"() {
        unzipAndTest("src/spec/test/builder/FileTreeBuilderTest.groovy", [])
    }

    void "test src/spec/test/builder/NodeBuilderTest.groovy"() {
        unzipAndTest("src/spec/test/builder/NodeBuilderTest.groovy", [])
    }

    void "test src/spec/test/builder/ObjectGraphBuilderTest.groovy"() {
        unzipAndTest("src/spec/test/builder/ObjectGraphBuilderTest.groovy", [])
    }

    void "test src/spec/test/gdk/ConfigSlurperTest.groovy"() {
        unzipAndTest("src/spec/test/gdk/ConfigSlurperTest.groovy", [])
    }

    void "test src/spec/test/gdk/ExpandoTest.groovy"() {
        unzipAndTest("src/spec/test/gdk/ExpandoTest.groovy", [])
    }

    void "test src/spec/test/gdk/ObservableTest.groovy"() {
        unzipAndTest("src/spec/test/gdk/ObservableTest.groovy", [])
    }

    void "test src/spec/test/gdk/WorkingWithCollectionsTest.groovy"() {
        unzipAndTest("src/spec/test/gdk/WorkingWithCollectionsTest.groovy", [])
    }

    void "test src/spec/test/gdk/WorkingWithIOSpecTest.groovy"() {
        unzipAndTest("src/spec/test/gdk/WorkingWithIOSpecTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/ASTXFormSpecTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/ASTXFormSpecTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/CategoryTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/CategoryTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/ExpandoMetaClassTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/ExpandoMetaClassTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/GroovyObjectTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/GroovyObjectTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/InterceptableTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/InterceptableTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/InterceptionThroughMetaClassTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/InterceptionThroughMetaClassTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/MethodPropertyMissingTest.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/MethodPropertyMissingTest.groovy", [])
    }

    void "test src/spec/test/metaprogramming/MyTransformToDebug.groovy"() {
        unzipAndTest("src/spec/test/metaprogramming/MyTransformToDebug.groovy", [])
    }

    void "test src/spec/test/objectorientation/MethodsTest.groovy"() {
        unzipAndTest("src/spec/test/objectorientation/MethodsTest.groovy", [])
    }

    void "test src/spec/test/semantics/GPathTest.groovy"() {
        unzipAndTest("src/spec/test/semantics/GPathTest.groovy", [])
    }

    void "test src/spec/test/semantics/LabelsTest.groovy"() {
        unzipAndTest("src/spec/test/semantics/LabelsTest.groovy", [])
    }

    void "test src/spec/test/semantics/OptionalityTest.groovy"() {
        unzipAndTest("src/spec/test/semantics/OptionalityTest.groovy", [])
    }

    void "test src/spec/test/semantics/PowerAssertTest.groovy"() {
        unzipAndTest("src/spec/test/semantics/PowerAssertTest.groovy", [])
    }

    void "test src/spec/test/semantics/TheGroovyTruthTest.groovy"() {
        unzipAndTest("src/spec/test/semantics/TheGroovyTruthTest.groovy", [])
    }

    void "test src/spec/test/support/MaxRetriesExtension.groovy"() {
        unzipAndTest("src/spec/test/support/MaxRetriesExtension.groovy", [])
    }

    void "test src/spec/test/support/StaticStringExtension.groovy"() {
        unzipAndTest("src/spec/test/support/StaticStringExtension.groovy", [])
    }

    void "test src/spec/test/testingguide/GDKMethodTests.groovy"() {
        unzipAndTest("src/spec/test/testingguide/GDKMethodTests.groovy", [])
    }

    void "test src/spec/test/testingguide/GroovyTestCaseExampleTests.groovy"() {
        unzipAndTest("src/spec/test/testingguide/GroovyTestCaseExampleTests.groovy", [])
    }

    void "test src/spec/test/testingguide/JUnit4ExampleTests.groovy"() {
        unzipAndTest("src/spec/test/testingguide/JUnit4ExampleTests.groovy", [])
    }

    void "test src/spec/test/testingguide/MockingExampleTests.groovy"() {
        unzipAndTest("src/spec/test/testingguide/MockingExampleTests.groovy", [])
    }

    void "test src/spec/test/typing/OptionalTypingTest.groovy"() {
        unzipAndTest("src/spec/test/typing/OptionalTypingTest.groovy", [])
    }

    void "test src/spec/test/typing/PrecompiledExtension.groovy"() {
        unzipAndTest("src/spec/test/typing/PrecompiledExtension.groovy", [])
    }

    void "test src/spec/test/typing/Robot.groovy"() {
        unzipAndTest("src/spec/test/typing/Robot.groovy", [])
    }

    void "test src/spec/test/typing/StaticCompilationIntroTest.groovy"() {
        unzipAndTest("src/spec/test/typing/StaticCompilationIntroTest.groovy", [])
    }

    void "test src/spec/test/typing/TypeCheckingExtensionSpecTest.groovy"() {
        unzipAndTest("src/spec/test/typing/TypeCheckingExtensionSpecTest.groovy", [])
    }

    void "test src/spec/test/typing/TypeCheckingHintsTest.groovy"() {
        unzipAndTest("src/spec/test/typing/TypeCheckingHintsTest.groovy", [])
    }

    void "test src/spec/test/typing/TypeCheckingTest.groovy"() {
        unzipAndTest("src/spec/test/typing/TypeCheckingTest.groovy", [])
    }

    void "test src/tck/src/org/codehaus/groovy/tck/BatchGenerate.groovy"() {
        unzipAndTest("src/tck/src/org/codehaus/groovy/tck/BatchGenerate.groovy", [])
    }

    void "test src/tck/src/org/codehaus/groovy/tck/TestGenerator.groovy"() {
        unzipAndTest("src/tck/src/org/codehaus/groovy/tck/TestGenerator.groovy", [])
    }

    void "test src/tck/test/gls/ch03/s01/Unicode1.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s01/Unicode1.groovy", [])
    }

    void "test src/tck/test/gls/ch03/s01/Unicode2.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s01/Unicode2.groovy", [])
    }

    void "test src/tck/test/gls/ch03/s02/LexicalTranslation1.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s02/LexicalTranslation1.groovy", [])
    }

    void "test src/tck/test/gls/ch03/s02/Longest1.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s02/Longest1.groovy", [])
    }

    void "test src/tck/test/gls/ch03/s03/UnicodeEscapes1.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s03/UnicodeEscapes1.groovy", [])
    }

    // https://github.com/danielsun1106/groovy-parser/issues/3
    void "test src/tck/test/gls/ch03/s03/UnicodeEscapes2.groovy"() {
        unzipAndTest("src/tck/test/gls/ch03/s03/UnicodeEscapes2.groovy", [], ['\\ufffg': '/ufffg', '\\uu006g': '/uu006g', '\\uab cd': '/uab cd'])
    }

    void "test src/test-resources/groovy/transform/sc/MixedMode.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/sc/MixedMode.groovy", [])
    }

    void "test src/test-resources/groovy/transform/sc/MixedMode2.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/sc/MixedMode2.groovy", [])
    }

    void "test src/test-resources/groovy/transform/sc/MixedModeDynamicBuilder.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/sc/MixedModeDynamicBuilder.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/AmbiguousMethods.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/AmbiguousMethods.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/AnnotatedByTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/AnnotatedByTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/ArgumentsTestingTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/ArgumentsTestingTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/BeforeAfterClassTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/BeforeAfterClassTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/BinaryOperatorTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/BinaryOperatorTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/DelegatesToTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/DelegatesToTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/FinishTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/FinishTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/FirstArgumentsTestingTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/FirstArgumentsTestingTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/Groovy6047Extension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/Groovy6047Extension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/IncompatibleAssignmentTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/IncompatibleAssignmentTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/IncompatibleReturnTypeTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/IncompatibleReturnTypeTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/MissingMethod1TestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/MissingMethod1TestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/MissingMethod2TestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/MissingMethod2TestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/NewMethodAndIsGeneratedTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/NewMethodAndIsGeneratedTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/NthArgumentTestingTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/NthArgumentTestingTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/OnMethodSelectionTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/OnMethodSelectionTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/PrefixChangerTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/PrefixChangerTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/RobotMove.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/RobotMove.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/ScopeEnterExitTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/ScopeEnterExitTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/SetupTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/SetupTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/SilentTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/SilentTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/SprintfExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/SprintfExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UndefinedVariableNoHandleTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UndefinedVariableNoHandleTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UndefinedVariableTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UndefinedVariableTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UnresolvedAttributeTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UnresolvedAttributeTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UnresolvedPropertyTestExtension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UnresolvedPropertyTestExtension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UpperCaseMethodTest1Extension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UpperCaseMethodTest1Extension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UpperCaseMethodTest2Extension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UpperCaseMethodTest2Extension.groovy", [])
    }

    void "test src/test-resources/groovy/transform/stc/UpperCaseMethodTest3Extension.groovy"() {
        unzipAndTest("src/test-resources/groovy/transform/stc/UpperCaseMethodTest3Extension.groovy", [])
    }

    void "test src/test-resources/stubgenerator/circularLanguageReference/Rectangle.groovy"() {
        unzipAndTest("src/test-resources/stubgenerator/circularLanguageReference/Rectangle.groovy", [])
    }

    void "test src/test-resources/stubgenerator/propertyUsageFromJava/somepackage/GroovyPogo.groovy"() {
        unzipAndTest("src/test-resources/stubgenerator/propertyUsageFromJava/somepackage/GroovyPogo.groovy", [])
    }

    void "test src/test/MainJavadocAssertionTest.groovy"() {
        unzipAndTest("src/test/MainJavadocAssertionTest.groovy", [])
    }

    void "test src/test/Outer3.groovy"() {
        unzipAndTest("src/test/Outer3.groovy", [])
    }

    void "test src/test/Outer4.groovy"() {
        unzipAndTest("src/test/Outer4.groovy", [])
    }

    void "test src/test/gls/CompilableTestSupport.groovy"() {
        unzipAndTest("src/test/gls/CompilableTestSupport.groovy", [])
    }

    void "test src/test/gls/annotations/AnnotationTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/AnnotationTest.groovy", [])
    }

    void "test src/test/gls/annotations/XmlEnum.groovy"() {
        unzipAndTest("src/test/gls/annotations/XmlEnum.groovy", [])
    }

    void "test src/test/gls/annotations/XmlEnumValue.groovy"() {
        unzipAndTest("src/test/gls/annotations/XmlEnumValue.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureExhaustiveTestSupport.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureExhaustiveTestSupport.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureOwnerCallTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureOwnerCallTest.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureTest.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureThisObjectCallTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureThisObjectCallTest.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureUnqualifiedCallTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureUnqualifiedCallTest.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureWithNonLocalVariable.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureWithNonLocalVariable.groovy", [])
    }

    void "test src/test/gls/annotations/closures/AnnotationClosureWithParametersTest.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/AnnotationClosureWithParametersTest.groovy", [])
    }

    void "test src/test/gls/annotations/closures/JavaCompatibility.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/JavaCompatibility.groovy", [])
    }

    void "test src/test/gls/annotations/closures/JavaCompatibilityParameterized.groovy"() {
        unzipAndTest("src/test/gls/annotations/closures/JavaCompatibilityParameterized.groovy", [])
    }

    void "test src/test/gls/ch06/s05/GName1Test.groovy"() {
        unzipAndTest("src/test/gls/ch06/s05/GName1Test.groovy", [])
    }

    void "test src/test/gls/ch08/s04/FormalParameterTest.groovy"() {
        unzipAndTest("src/test/gls/ch08/s04/FormalParameterTest.groovy", [])
    }

    void "test src/test/gls/ch08/s04/RepetitiveMethodTest.groovy"() {
        unzipAndTest("src/test/gls/ch08/s04/RepetitiveMethodTest.groovy", [])
    }

    void "test src/test/gls/enums/EnumTest.groovy"() {
        unzipAndTest("src/test/gls/enums/EnumTest.groovy", [])
    }

    void "test src/test/gls/generics/GenericsTest.groovy"() {
        unzipAndTest("src/test/gls/generics/GenericsTest.groovy", [])
    }

    void "test src/test/gls/innerClass/InnerClassTest.groovy"() {
        unzipAndTest("src/test/gls/innerClass/InnerClassTest.groovy", [])
    }

    void "test src/test/gls/innerClass/InnerInterfaceTest.groovy"() {
        unzipAndTest("src/test/gls/innerClass/InnerInterfaceTest.groovy", [])
    }

    void "test src/test/gls/invocation/ClassDuplicationTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/ClassDuplicationTest.groovy", [])
    }

    void "test src/test/gls/invocation/ClosureDelegationTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/ClosureDelegationTest.groovy", [])
    }

    void "test src/test/gls/invocation/ConstructorDelegationTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/ConstructorDelegationTest.groovy", [])
    }

    void "test src/test/gls/invocation/CovariantReturnTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/CovariantReturnTest.groovy", [])
    }

    void "test src/test/gls/invocation/DefaultParamTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/DefaultParamTest.groovy", [])
    }

    void "test src/test/gls/invocation/GroovyObjectInheritanceTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/GroovyObjectInheritanceTest.groovy", [])
    }

    void "test src/test/gls/invocation/MethodDeclarationTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/MethodDeclarationTest.groovy", [])
    }

    void "test src/test/gls/invocation/MethodSelectionTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/MethodSelectionTest.groovy", [])
    }

    void "test src/test/gls/invocation/StaticMethodInvocationTest.groovy"() {
        unzipAndTest("src/test/gls/invocation/StaticMethodInvocationTest.groovy", [])
    }

    void "test src/test/gls/property/MetaClassOverridingTest.groovy"() {
        unzipAndTest("src/test/gls/property/MetaClassOverridingTest.groovy", [])
    }

    void "test src/test/gls/scope/BlockScopeVisibilityTest.groovy"() {
        unzipAndTest("src/test/gls/scope/BlockScopeVisibilityTest.groovy", [])
    }

    void "test src/test/gls/scope/ClassVariableHidingTest.groovy"() {
        unzipAndTest("src/test/gls/scope/ClassVariableHidingTest.groovy", [])
    }

    void "test src/test/gls/scope/FinalAccessTest.groovy"() {
        unzipAndTest("src/test/gls/scope/FinalAccessTest.groovy", [])
    }

    void "test src/test/gls/scope/MultipleDefinitionOfSameVariableTest.groovy"() {
        unzipAndTest("src/test/gls/scope/MultipleDefinitionOfSameVariableTest.groovy", [])
    }

    void "test src/test/gls/scope/NameResolvingTest.groovy"() {
        unzipAndTest("src/test/gls/scope/NameResolvingTest.groovy", [])
    }

    void "test src/test/gls/scope/StaticScopeTest.groovy"() {
        unzipAndTest("src/test/gls/scope/StaticScopeTest.groovy", [])
    }

    void "test src/test/gls/scope/VariablePrecedenceTest.groovy"() {
        unzipAndTest("src/test/gls/scope/VariablePrecedenceTest.groovy", [])
    }

    void "test src/test/gls/sizelimits/StringSizeTest.groovy"() {
        unzipAndTest("src/test/gls/sizelimits/StringSizeTest.groovy", [])
    }

    void "test src/test/gls/statements/DeclarationTest.groovy"() {
        unzipAndTest("src/test/gls/statements/DeclarationTest.groovy", [])
    }

    void "test src/test/gls/statements/MultipleAssignmentDeclarationTest.groovy"() {
        unzipAndTest("src/test/gls/statements/MultipleAssignmentDeclarationTest.groovy", [])
    }

    void "test src/test/gls/statements/MultipleAssignmentTest.groovy"() {
        unzipAndTest("src/test/gls/statements/MultipleAssignmentTest.groovy", [])
    }

    void "test src/test/gls/statements/ReturnTest.groovy"() {
        unzipAndTest("src/test/gls/statements/ReturnTest.groovy", [])
    }

    void "test src/test/gls/syntax/AssertTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/AssertTest.groovy", [])
    }

    void "test src/test/gls/syntax/BinaryLiteralTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/BinaryLiteralTest.groovy", [])
    }

    void "test src/test/gls/syntax/Gep3OrderDslTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/Gep3OrderDslTest.groovy", [])
    }

    void "test src/test/gls/syntax/Gep3Test.groovy"() {
        unzipAndTest("src/test/gls/syntax/Gep3Test.groovy", [])
    }

    void "test src/test/gls/syntax/MethodCallValidationTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/MethodCallValidationTest.groovy", [])
    }

    void "test src/test/gls/syntax/NumberLiteralTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/NumberLiteralTest.groovy", [])
    }

    void "test src/test/gls/syntax/OldClosureSyntaxRemovalTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/OldClosureSyntaxRemovalTest.groovy", [])
    }

    void "test src/test/gls/syntax/OldPropertySyntaxRemovalTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/OldPropertySyntaxRemovalTest.groovy", [])
    }

    void "test src/test/gls/syntax/OldSpreadTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/OldSpreadTest.groovy", [])
    }

    void "test src/test/gls/syntax/ParsingTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/ParsingTest.groovy", [])
    }

    void "test src/test/gls/syntax/UnderscoreInNumbersTest.groovy"() {
        unzipAndTest("src/test/gls/syntax/UnderscoreInNumbersTest.groovy", [])
    }

    void "test src/test/gls/types/BooleanExpressionConversionTest.groovy"() {
        unzipAndTest("src/test/gls/types/BooleanExpressionConversionTest.groovy", [])
    }

    void "test src/test/gls/types/GroovyCastTest.groovy"() {
        unzipAndTest("src/test/gls/types/GroovyCastTest.groovy", [])
    }

    void "test src/test/gls/types/OperationsResultTypeTest.groovy"() {
        unzipAndTest("src/test/gls/types/OperationsResultTypeTest.groovy", [])
    }

    void "test src/test/groovy/AbstractClassAndInterfaceTest.groovy"() {
        unzipAndTest("src/test/groovy/AbstractClassAndInterfaceTest.groovy", [])
    }

    void "test src/test/groovy/ActorTest.groovy"() {
        unzipAndTest("src/test/groovy/ActorTest.groovy", [])
    }

    void "test src/test/groovy/AmbiguousInvocationTest.groovy"() {
        unzipAndTest("src/test/groovy/AmbiguousInvocationTest.groovy", [])
    }

    void "test src/test/groovy/ArrayAutoboxingTest.groovy"() {
        unzipAndTest("src/test/groovy/ArrayAutoboxingTest.groovy", [])
    }

    void "test src/test/groovy/ArrayCoerceTest.groovy"() {
        unzipAndTest("src/test/groovy/ArrayCoerceTest.groovy", [])
    }

    void "test src/test/groovy/ArrayParamMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/ArrayParamMethodTest.groovy", [])
    }

    void "test src/test/groovy/ArrayTest.groovy"() {
        unzipAndTest("src/test/groovy/ArrayTest.groovy", [])
    }

    void "test src/test/groovy/ArrayTypeTest.groovy"() {
        unzipAndTest("src/test/groovy/ArrayTypeTest.groovy", [])
    }

    void "test src/test/groovy/AsTest.groovy"() {
        unzipAndTest("src/test/groovy/AsTest.groovy", [])
    }

    void "test src/test/groovy/AssertNumberTest.groovy"() {
        unzipAndTest("src/test/groovy/AssertNumberTest.groovy", [])
    }

    void "test src/test/groovy/AssertTest.groovy"() {
        unzipAndTest("src/test/groovy/AssertTest.groovy", [])
    }

    void "test src/test/groovy/Bar.groovy"() {
        unzipAndTest("src/test/groovy/Bar.groovy", [])
    }

    void "test src/test/groovy/Base64Test.groovy"() {
        unzipAndTest("src/test/groovy/Base64Test.groovy", [])
    }

    void "test src/test/groovy/BinaryStreamsTest.groovy"() {
        unzipAndTest("src/test/groovy/BinaryStreamsTest.groovy", [])
    }

    void "test src/test/groovy/BindingTest.groovy"() {
        unzipAndTest("src/test/groovy/BindingTest.groovy", [])
    }

    void "test src/test/groovy/BitSetTest.groovy"() {
        unzipAndTest("src/test/groovy/BitSetTest.groovy", [])
    }

    void "test src/test/groovy/BreakContinueLabelTest.groovy"() {
        unzipAndTest("src/test/groovy/BreakContinueLabelTest.groovy", [])
    }

    void "test src/test/groovy/CallInnerClassCtorTest.groovy"() {
        unzipAndTest("src/test/groovy/CallInnerClassCtorTest.groovy", [])
    }

    void "test src/test/groovy/CastTest.groovy"() {
        unzipAndTest("src/test/groovy/CastTest.groovy", [])
    }

    void "test src/test/groovy/CategoryTest.groovy"() {
        unzipAndTest("src/test/groovy/CategoryTest.groovy", [])
    }

    void "test src/test/groovy/ChainedAssignmentTest.groovy"() {
        unzipAndTest("src/test/groovy/ChainedAssignmentTest.groovy", [])
    }

    void "test src/test/groovy/ClassExpressionTest.groovy"() {
        unzipAndTest("src/test/groovy/ClassExpressionTest.groovy", [])
    }

    void "test src/test/groovy/ClassLoaderBug.groovy"() {
        unzipAndTest("src/test/groovy/ClassLoaderBug.groovy", [])
    }

    void "test src/test/groovy/ClassTest.groovy"() {
        unzipAndTest("src/test/groovy/ClassTest.groovy", [])
    }

    void "test src/test/groovy/ClosureAsParamTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureAsParamTest.groovy", [])
    }

    void "test src/test/groovy/ClosureCloneTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureCloneTest.groovy", [])
    }

    void "test src/test/groovy/ClosureComparatorTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureComparatorTest.groovy", [])
    }

    void "test src/test/groovy/ClosureComposeTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureComposeTest.groovy", [])
    }

    void "test src/test/groovy/ClosureCurryTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureCurryTest.groovy", [])
    }

    void "test src/test/groovy/ClosureDefaultParameterTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureDefaultParameterTest.groovy", [])
    }

    void "test src/test/groovy/ClosureInClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureInClosureTest.groovy", [])
    }

    void "test src/test/groovy/ClosureInStaticMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureInStaticMethodTest.groovy", [])
    }

    void "test src/test/groovy/ClosureMethodCallTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureMethodCallTest.groovy", [])
    }

    void "test src/test/groovy/ClosureMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureMethodTest.groovy", [])
    }

    void "test src/test/groovy/ClosureMethodsOnFileTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureMethodsOnFileTest.groovy", [])
    }

    void "test src/test/groovy/ClosureMissingMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureMissingMethodTest.groovy", [])
    }

    void "test src/test/groovy/ClosureReturnTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureReturnTest.groovy", [])
    }

    void "test src/test/groovy/ClosureReturnWithoutReturnStatementTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureReturnWithoutReturnStatementTest.groovy", [])
    }

    void "test src/test/groovy/ClosureSugarTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureSugarTest.groovy", [])
    }

    void "test src/test/groovy/ClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureTest.groovy", [])
    }

    void "test src/test/groovy/ClosureUsingOuterVariablesTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureUsingOuterVariablesTest.groovy", [])
    }

    void "test src/test/groovy/ClosureWithDefaultParamTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureWithDefaultParamTest.groovy", [])
    }

    void "test src/test/groovy/ClosureWithEmptyParametersTest.groovy"() {
        unzipAndTest("src/test/groovy/ClosureWithEmptyParametersTest.groovy", [])
    }

    void "test src/test/groovy/CollateTest.groovy"() {
        unzipAndTest("src/test/groovy/CollateTest.groovy", [])
    }

    void "test src/test/groovy/CompareEqualsTest.groovy"() {
        unzipAndTest("src/test/groovy/CompareEqualsTest.groovy", [])
    }

    void "test src/test/groovy/CompareToTest.groovy"() {
        unzipAndTest("src/test/groovy/CompareToTest.groovy", [])
    }

    void "test src/test/groovy/CompareTypesTest.groovy"() {
        unzipAndTest("src/test/groovy/CompareTypesTest.groovy", [])
    }

    void "test src/test/groovy/CompileOrderTest.groovy"() {
        unzipAndTest("src/test/groovy/CompileOrderTest.groovy", [])
    }

    void "test src/test/groovy/CompilerErrorTest.groovy"() {
        unzipAndTest("src/test/groovy/CompilerErrorTest.groovy", [])
    }

    void "test src/test/groovy/Constructor2Test.groovy"() {
        unzipAndTest("src/test/groovy/Constructor2Test.groovy", [])
    }

    void "test src/test/groovy/ConstructorTest.groovy"() {
        unzipAndTest("src/test/groovy/ConstructorTest.groovy", [])
    }

    void "test src/test/groovy/CurlyBracketLayoutTest.groovy"() {
        unzipAndTest("src/test/groovy/CurlyBracketLayoutTest.groovy", [])
    }

    void "test src/test/groovy/DateTest.groovy"() {
        unzipAndTest("src/test/groovy/DateTest.groovy", [])
    }

    void "test src/test/groovy/DefaultParamClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/DefaultParamClosureTest.groovy", [])
    }

    void "test src/test/groovy/DoWhileLoopTest.groovy"() {
        unzipAndTest("src/test/groovy/DoWhileLoopTest.groovy", [])
    }

    void "test src/test/groovy/DollarEscapingTest.groovy"() {
        unzipAndTest("src/test/groovy/DollarEscapingTest.groovy", [])
    }

    void "test src/test/groovy/DownUpStepTest.groovy"() {
        unzipAndTest("src/test/groovy/DownUpStepTest.groovy", [])
    }

    void "test src/test/groovy/DummyMethodsGroovy.groovy"() {
        unzipAndTest("src/test/groovy/DummyMethodsGroovy.groovy", [])
    }

    void "test src/test/groovy/DynamicMemberTest.groovy"() {
        unzipAndTest("src/test/groovy/DynamicMemberTest.groovy", [])
    }

    void "test src/test/groovy/EqualsTest.groovy"() {
        unzipAndTest("src/test/groovy/EqualsTest.groovy", [])
    }

    /* FIXME find a better way to translate code written in unicode escapes
    void "test src/test/groovy/EscapedUnicodeTest.groovy"() {
        unzipAndTest("src/test/groovy/EscapedUnicodeTest.groovy", [])
    }
    */

    void "test src/test/groovy/ExceptionInClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/ExceptionInClosureTest.groovy", [])
    }

    void "test src/test/groovy/ExpandoPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/ExpandoPropertyTest.groovy", [])
    }

    void "test src/test/groovy/FileTest.groovy"() {
        unzipAndTest("src/test/groovy/FileTest.groovy", [])
    }

    void "test src/test/groovy/FilterLineTest.groovy"() {
        unzipAndTest("src/test/groovy/FilterLineTest.groovy", [])
    }

    void "test src/test/groovy/FinallyTest.groovy"() {
        unzipAndTest("src/test/groovy/FinallyTest.groovy", [])
    }

    void "test src/test/groovy/Foo.groovy"() {
        unzipAndTest("src/test/groovy/Foo.groovy", [])
    }

    void "test src/test/groovy/ForLoopTest.groovy"() {
        unzipAndTest("src/test/groovy/ForLoopTest.groovy", [])
    }

    void "test src/test/groovy/ForLoopWithLocalVariablesTest.groovy"() {
        unzipAndTest("src/test/groovy/ForLoopWithLocalVariablesTest.groovy", [])
    }

    void "test src/test/groovy/GStringTest.groovy"() {
        unzipAndTest("src/test/groovy/GStringTest.groovy", [])
    }

    void "test src/test/groovy/GeneratorTest.groovy"() {
        unzipAndTest("src/test/groovy/GeneratorTest.groovy", [])
    }

    void "test src/test/groovy/GlobalPrintlnTest.groovy"() {
        unzipAndTest("src/test/groovy/GlobalPrintlnTest.groovy", [])
    }

    void "test src/test/groovy/GroovyCharSequenceMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/GroovyCharSequenceMethodsTest.groovy", [])
    }

    void "test src/test/groovy/GroovyClosureMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/GroovyClosureMethodsTest.groovy", [])
    }

    void "test src/test/groovy/GroovyInterceptableTest.groovy"() {
        unzipAndTest("src/test/groovy/GroovyInterceptableTest.groovy", [])
    }

    void "test src/test/groovy/GroovyMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/GroovyMethodsTest.groovy", [])
    }

    void "test src/test/groovy/GroovyTruthTest.groovy"() {
        unzipAndTest("src/test/groovy/GroovyTruthTest.groovy", [])
    }

    void "test src/test/groovy/HeredocsTest.groovy"() {
        unzipAndTest("src/test/groovy/HeredocsTest.groovy", [])
    }

    void "test src/test/groovy/HexTest.groovy"() {
        unzipAndTest("src/test/groovy/HexTest.groovy", [])
    }

    void "test src/test/groovy/HomepageTest.groovy"() {
        unzipAndTest("src/test/groovy/HomepageTest.groovy", [])
    }

    void "test src/test/groovy/IdentityClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/IdentityClosureTest.groovy", [])
    }

    void "test src/test/groovy/IfElseCompactTest.groovy"() {
        unzipAndTest("src/test/groovy/IfElseCompactTest.groovy", [])
    }

    void "test src/test/groovy/IfElseTest.groovy"() {
        unzipAndTest("src/test/groovy/IfElseTest.groovy", [])
    }

    void "test src/test/groovy/IfPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/IfPropertyTest.groovy", [])
    }

    void "test src/test/groovy/IfTest.groovy"() {
        unzipAndTest("src/test/groovy/IfTest.groovy", [])
    }

    void "test src/test/groovy/IfWithMethodCallTest.groovy"() {
        unzipAndTest("src/test/groovy/IfWithMethodCallTest.groovy", [])
    }

    void "test src/test/groovy/ImmutableModificationTest.groovy"() {
        unzipAndTest("src/test/groovy/ImmutableModificationTest.groovy", [])
    }

    void "test src/test/groovy/ImportTest.groovy"() {
        unzipAndTest("src/test/groovy/ImportTest.groovy", [])
    }

    void "test src/test/groovy/InstanceofTest.groovy"() {
        unzipAndTest("src/test/groovy/InstanceofTest.groovy", [])
    }

    void "test src/test/groovy/InterfaceTest.groovy"() {
        unzipAndTest("src/test/groovy/InterfaceTest.groovy", [])
    }

    void "test src/test/groovy/InvokeNormalMethodsFirstTest.groovy"() {
        unzipAndTest("src/test/groovy/InvokeNormalMethodsFirstTest.groovy", [])
    }

    void "test src/test/groovy/JointGroovy.groovy"() {
        unzipAndTest("src/test/groovy/JointGroovy.groovy", [])
    }

    void "test src/test/groovy/KeywordsInPropertyNamesTest.groovy"() {
        unzipAndTest("src/test/groovy/KeywordsInPropertyNamesTest.groovy", [])
    }

    void "test src/test/groovy/LeftShiftTest.groovy"() {
        unzipAndTest("src/test/groovy/LeftShiftTest.groovy", [])
    }

    void "test src/test/groovy/ListIteratingTest.groovy"() {
        unzipAndTest("src/test/groovy/ListIteratingTest.groovy", [])
    }

    void "test src/test/groovy/ListTest.groovy"() {
        unzipAndTest("src/test/groovy/ListTest.groovy", [])
    }

    void "test src/test/groovy/LiteralTypesTest.groovy"() {
        unzipAndTest("src/test/groovy/LiteralTypesTest.groovy", [])
    }

    void "test src/test/groovy/LittleClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/LittleClosureTest.groovy", [])
    }

    void "test src/test/groovy/LocalFieldTest.groovy"() {
        unzipAndTest("src/test/groovy/LocalFieldTest.groovy", [])
    }

    void "test src/test/groovy/LocalPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/LocalPropertyTest.groovy", [])
    }

    void "test src/test/groovy/LocalVariableTest.groovy"() {
        unzipAndTest("src/test/groovy/LocalVariableTest.groovy", [])
    }

    void "test src/test/groovy/LogicTest.groovy"() {
        unzipAndTest("src/test/groovy/LogicTest.groovy", [])
    }

    void "test src/test/groovy/LoopBreakTest.groovy"() {
        unzipAndTest("src/test/groovy/LoopBreakTest.groovy", [])
    }

    void "test src/test/groovy/MapConstructionTest.groovy"() {
        unzipAndTest("src/test/groovy/MapConstructionTest.groovy", [])
    }

    void "test src/test/groovy/MapPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/MapPropertyTest.groovy", [])
    }

    void "test src/test/groovy/MapTest.groovy"() {
        unzipAndTest("src/test/groovy/MapTest.groovy", [])
    }

    void "test src/test/groovy/MethodCallTest.groovy"() {
        unzipAndTest("src/test/groovy/MethodCallTest.groovy", [])
    }

    void "test src/test/groovy/MethodCallWithoutParenthesisTest.groovy"() {
        unzipAndTest("src/test/groovy/MethodCallWithoutParenthesisTest.groovy", [])
    }

    void "test src/test/groovy/MethodInBadPositionTest.groovy"() {
        unzipAndTest("src/test/groovy/MethodInBadPositionTest.groovy", [])
    }

    void "test src/test/groovy/MethodParameterAccessWithinClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/MethodParameterAccessWithinClosureTest.groovy", [])
    }

    void "test src/test/groovy/MinMaxTest.groovy"() {
        unzipAndTest("src/test/groovy/MinMaxTest.groovy", [])
    }

    void "test src/test/groovy/MinusEqualsTest.groovy"() {
        unzipAndTest("src/test/groovy/MinusEqualsTest.groovy", [])
    }

    void "test src/test/groovy/ModifiersTest.groovy"() {
        unzipAndTest("src/test/groovy/ModifiersTest.groovy", [])
    }

    void "test src/test/groovy/ModuloTest.groovy"() {
        unzipAndTest("src/test/groovy/ModuloTest.groovy", [])
    }

    void "test src/test/groovy/MultiCatchTest.groovy"() {
        unzipAndTest("src/test/groovy/MultiCatchTest.groovy", [])
    }

    void "test src/test/groovy/MultiDimArraysTest.groovy"() {
        unzipAndTest("src/test/groovy/MultiDimArraysTest.groovy", [])
    }

    void "test src/test/groovy/MultilineChainExpressionTest.groovy"() {
        unzipAndTest("src/test/groovy/MultilineChainExpressionTest.groovy", [])
    }

    void "test src/test/groovy/MultilineStringTest.groovy"() {
        unzipAndTest("src/test/groovy/MultilineStringTest.groovy", [])
    }

    void "test src/test/groovy/MultiplyDivideEqualsTest.groovy"() {
        unzipAndTest("src/test/groovy/MultiplyDivideEqualsTest.groovy", [])
    }

    void "test src/test/groovy/NamedParameterTest.groovy"() {
        unzipAndTest("src/test/groovy/NamedParameterTest.groovy", [])
    }

    void "test src/test/groovy/NestedClassTest.groovy"() {
        unzipAndTest("src/test/groovy/NestedClassTest.groovy", [])
    }

    void "test src/test/groovy/NewExpressionTest.groovy"() {
        unzipAndTest("src/test/groovy/NewExpressionTest.groovy", [])
    }

    void "test src/test/groovy/NoPackageTest.groovy"() {
        unzipAndTest("src/test/groovy/NoPackageTest.groovy", [])
    }

    void "test src/test/groovy/NullPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/NullPropertyTest.groovy", [])
    }

    void "test src/test/groovy/OptionalReturnTest.groovy"() {
        unzipAndTest("src/test/groovy/OptionalReturnTest.groovy", [])
    }

    void "test src/test/groovy/OverloadInvokeMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/OverloadInvokeMethodTest.groovy", [])
    }

    void "test src/test/groovy/OverridePropertyGetterTest.groovy"() {
        unzipAndTest("src/test/groovy/OverridePropertyGetterTest.groovy", [])
    }

    void "test src/test/groovy/OverrideTest.groovy"() {
        unzipAndTest("src/test/groovy/OverrideTest.groovy", [])
    }

    void "test src/test/groovy/PlusEqualsTest.groovy"() {
        unzipAndTest("src/test/groovy/PlusEqualsTest.groovy", [])
    }

    void "test src/test/groovy/PostfixTest.groovy"() {
        unzipAndTest("src/test/groovy/PostfixTest.groovy", [])
    }

    void "test src/test/groovy/PrefixTest.groovy"() {
        unzipAndTest("src/test/groovy/PrefixTest.groovy", [])
    }

    void "test src/test/groovy/PrimitiveArraysTest.groovy"() {
        unzipAndTest("src/test/groovy/PrimitiveArraysTest.groovy", [])
    }

    void "test src/test/groovy/PrimitiveDefaultValueTest.groovy"() {
        unzipAndTest("src/test/groovy/PrimitiveDefaultValueTest.groovy", [])
    }

    void "test src/test/groovy/PrimitiveTypeFieldTest.groovy"() {
        unzipAndTest("src/test/groovy/PrimitiveTypeFieldTest.groovy", [])
    }

    void "test src/test/groovy/PrimitiveTypesTest.groovy"() {
        unzipAndTest("src/test/groovy/PrimitiveTypesTest.groovy", [])
    }

    void "test src/test/groovy/PrintTest.groovy"() {
        unzipAndTest("src/test/groovy/PrintTest.groovy", [])
    }

    void "test src/test/groovy/PrivateVariableAccessFromAnotherInstanceTest.groovy"() {
        unzipAndTest("src/test/groovy/PrivateVariableAccessFromAnotherInstanceTest.groovy", [])
    }

    void "test src/test/groovy/ProcessTest.groovy"() {
        unzipAndTest("src/test/groovy/ProcessTest.groovy", [])
    }

    void "test src/test/groovy/Property2Test.groovy"() {
        unzipAndTest("src/test/groovy/Property2Test.groovy", [])
    }

    void "test src/test/groovy/PropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/PropertyTest.groovy", [])
    }

    void "test src/test/groovy/PropertyWithoutDotTest.groovy"() {
        unzipAndTest("src/test/groovy/PropertyWithoutDotTest.groovy", [])
    }

    void "test src/test/groovy/RangeTest.groovy"() {
        unzipAndTest("src/test/groovy/RangeTest.groovy", [])
    }

    void "test src/test/groovy/ReadLineTest.groovy"() {
        unzipAndTest("src/test/groovy/ReadLineTest.groovy", [])
    }

    void "test src/test/groovy/RegularExpressionsTest.groovy"() {
        unzipAndTest("src/test/groovy/RegularExpressionsTest.groovy", [])
    }

    void "test src/test/groovy/ReturnTest.groovy"() {
        unzipAndTest("src/test/groovy/ReturnTest.groovy", [])
    }

    void "test src/test/groovy/SafeNavigationTest.groovy"() {
        unzipAndTest("src/test/groovy/SafeNavigationTest.groovy", [])
    }

    void "test src/test/groovy/SampleMain.groovy"() {
        unzipAndTest("src/test/groovy/SampleMain.groovy", [])
    }

    void "test src/test/groovy/SerializeTest.groovy"() {
        unzipAndTest("src/test/groovy/SerializeTest.groovy", [])
    }

    void "test src/test/groovy/SetTest.groovy"() {
        unzipAndTest("src/test/groovy/SetTest.groovy", [])
    }

    void "test src/test/groovy/ShellTest.groovy"() {
        unzipAndTest("src/test/groovy/ShellTest.groovy", [])
    }

    void "test src/test/groovy/SimplePostfixTest.groovy"() {
        unzipAndTest("src/test/groovy/SimplePostfixTest.groovy", [])
    }

    void "test src/test/groovy/SingletonBugTest.groovy"() {
        unzipAndTest("src/test/groovy/SingletonBugTest.groovy", [])
    }

    void "test src/test/groovy/SliceTest.groovy"() {
        unzipAndTest("src/test/groovy/SliceTest.groovy", [])
    }

    void "test src/test/groovy/SocketTest.groovy"() {
        unzipAndTest("src/test/groovy/SocketTest.groovy", [])
    }

    void "test src/test/groovy/SortTest.groovy"() {
        unzipAndTest("src/test/groovy/SortTest.groovy", [])
    }

    void "test src/test/groovy/SpreadDotTest.groovy"() {
        unzipAndTest("src/test/groovy/SpreadDotTest.groovy", [])
    }

    void "test src/test/groovy/SqlDateTest.groovy"() {
        unzipAndTest("src/test/groovy/SqlDateTest.groovy", [])
    }

    void "test src/test/groovy/StackTraceTest.groovy"() {
        unzipAndTest("src/test/groovy/StackTraceTest.groovy", [])
    }

    void "test src/test/groovy/StaticImportTarget.groovy"() {
        unzipAndTest("src/test/groovy/StaticImportTarget.groovy", [])
    }

    void "test src/test/groovy/StaticImportTest.groovy"() {
        unzipAndTest("src/test/groovy/StaticImportTest.groovy", [])
    }

    void "test src/test/groovy/StaticMessageTest.groovy"() {
        unzipAndTest("src/test/groovy/StaticMessageTest.groovy", [])
    }

    void "test src/test/groovy/StaticThisTest.groovy"() {
        unzipAndTest("src/test/groovy/StaticThisTest.groovy", [])
    }

    void "test src/test/groovy/StringBufferTest.groovy"() {
        unzipAndTest("src/test/groovy/StringBufferTest.groovy", [])
    }

    void "test src/test/groovy/StringTest.groovy"() {
        unzipAndTest("src/test/groovy/StringTest.groovy", [])
    }

    void "test src/test/groovy/SubscriptTest.groovy"() {
        unzipAndTest("src/test/groovy/SubscriptTest.groovy", [])
    }

    void "test src/test/groovy/SwitchTest.groovy"() {
        unzipAndTest("src/test/groovy/SwitchTest.groovy", [])
    }

    void "test src/test/groovy/SwitchWithDifferentTypesTest.groovy"() {
        unzipAndTest("src/test/groovy/SwitchWithDifferentTypesTest.groovy", [])
    }

    void "test src/test/groovy/TextPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/TextPropertyTest.groovy", [])
    }

    void "test src/test/groovy/ThisAndSuperTest.groovy"() {
        unzipAndTest("src/test/groovy/ThisAndSuperTest.groovy", [])
    }

    void "test src/test/groovy/ThreadMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/ThreadMethodsTest.groovy", [])
    }

    void "test src/test/groovy/ThrowTest.groovy"() {
        unzipAndTest("src/test/groovy/ThrowTest.groovy", [])
    }

    void "test src/test/groovy/ToArrayBugTest.groovy"() {
        unzipAndTest("src/test/groovy/ToArrayBugTest.groovy", [])
    }

    void "test src/test/groovy/TripleQuotedStringTest.groovy"() {
        unzipAndTest("src/test/groovy/TripleQuotedStringTest.groovy", [])
    }

    void "test src/test/groovy/TryCatchTest.groovy"() {
        unzipAndTest("src/test/groovy/TryCatchTest.groovy", [])
    }

    void "test src/test/groovy/TypesafeMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/TypesafeMethodTest.groovy", [])
    }

    void "test src/test/groovy/UniqueOnCollectionTest.groovy"() {
        unzipAndTest("src/test/groovy/UniqueOnCollectionTest.groovy", [])
    }

    void "test src/test/groovy/UniqueOnCollectionWithClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/UniqueOnCollectionWithClosureTest.groovy", [])
    }

    void "test src/test/groovy/UniqueOnCollectionWithComparatorTest.groovy"() {
        unzipAndTest("src/test/groovy/UniqueOnCollectionWithComparatorTest.groovy", [])
    }

    void "test src/test/groovy/UnitTestAsScriptTest.groovy"() {
        unzipAndTest("src/test/groovy/UnitTestAsScriptTest.groovy", [])
    }

    void "test src/test/groovy/UnsafeNavigationTest.groovy"() {
        unzipAndTest("src/test/groovy/UnsafeNavigationTest.groovy", [])
    }

    void "test src/test/groovy/VArgsTest.groovy"() {
        unzipAndTest("src/test/groovy/VArgsTest.groovy", [])
    }

    void "test src/test/groovy/ValidNameTest.groovy"() {
        unzipAndTest("src/test/groovy/ValidNameTest.groovy", [])
    }

    void "test src/test/groovy/VarargsMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/VarargsMethodTest.groovy", [])
    }

    void "test src/test/groovy/VerbatimGStringTest.groovy"() {
        unzipAndTest("src/test/groovy/VerbatimGStringTest.groovy", [])
    }

    void "test src/test/groovy/WhileLoopTest.groovy"() {
        unzipAndTest("src/test/groovy/WhileLoopTest.groovy", [])
    }

    void "test src/test/groovy/annotations/MyClass.groovy"() {
        unzipAndTest("src/test/groovy/annotations/MyClass.groovy", [])
    }

    void "test src/test/groovy/annotations/MyIntegerAnno.groovy"() {
        unzipAndTest("src/test/groovy/annotations/MyIntegerAnno.groovy", [])
    }

    void "test src/test/groovy/annotations/PackageAndImportAnnotationTest.groovy"() {
        unzipAndTest("src/test/groovy/annotations/PackageAndImportAnnotationTest.groovy", [])
    }

    void "test src/test/groovy/annotations/ParameterAnnotationTest.groovy"() {
        unzipAndTest("src/test/groovy/annotations/ParameterAnnotationTest.groovy", [])
    }

    void "test src/test/groovy/annotations/package-info.groovy"() {
        unzipAndTest("src/test/groovy/annotations/package-info.groovy", [])
    }

    void "test src/test/groovy/beans/BindableTest.groovy"() {
        unzipAndTest("src/test/groovy/beans/BindableTest.groovy", [])
    }

    void "test src/test/groovy/beans/ListenerListASTTest.groovy"() {
        unzipAndTest("src/test/groovy/beans/ListenerListASTTest.groovy", [])
    }

    void "test src/test/groovy/beans/ListenerListHelper.groovy"() {
        unzipAndTest("src/test/groovy/beans/ListenerListHelper.groovy", [])
    }

    void "test src/test/groovy/beans/VetoableTest.groovy"() {
        unzipAndTest("src/test/groovy/beans/VetoableTest.groovy", [])
    }

    void "test src/test/groovy/benchmarks/createLoop.groovy"() {
        unzipAndTest("src/test/groovy/benchmarks/createLoop.groovy", [])
    }

    void "test src/test/groovy/benchmarks/loop.groovy"() {
        unzipAndTest("src/test/groovy/benchmarks/loop.groovy", [])
    }

    void "test src/test/groovy/benchmarks/loop2.groovy"() {
        unzipAndTest("src/test/groovy/benchmarks/loop2.groovy", [])
    }

    void "test src/test/groovy/bugs/AmbiguousListOrMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/AmbiguousListOrMethodTest.groovy", [])
    }

    void "test src/test/groovy/bugs/ArrayMethodCallBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ArrayMethodCallBug.groovy", [])
    }

    void "test src/test/groovy/bugs/AsBoolBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/AsBoolBug.groovy", [])
    }

    void "test src/test/groovy/bugs/AssignmentInsideExpressionBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/AssignmentInsideExpressionBug.groovy", [])
    }

    void "test src/test/groovy/bugs/AttributeSetExpressionBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/AttributeSetExpressionBug.groovy", [])
    }

    void "test src/test/groovy/bugs/AutoboxingOfComparisonsBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/AutoboxingOfComparisonsBug.groovy", [])
    }

    void "test src/test/groovy/bugs/BadLineNumberOnExceptionBugTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BadLineNumberOnExceptionBugTest.groovy", [])
    }

    void "test src/test/groovy/bugs/BadScriptNameBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BadScriptNameBug.groovy", [])
    }

    void "test src/test/groovy/bugs/BenchmarkBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BenchmarkBug.groovy", [])
    }

    void "test src/test/groovy/bugs/BlockAsClosureBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BlockAsClosureBug.groovy", [])
    }

    void "test src/test/groovy/bugs/BooleanBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BooleanBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ByteIndexBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ByteIndexBug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode2Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode2Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode3Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode3Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode4Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode4Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode5Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode5Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode6Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode6Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Bytecode7Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Bytecode7Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/BytecodeBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/BytecodeBug.groovy", [])
    }

    void "test src/test/groovy/bugs/CallingClosuresWithClosuresBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/CallingClosuresWithClosuresBug.groovy", [])
    }

    void "test src/test/groovy/bugs/CastWhenUsingClosuresBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/CastWhenUsingClosuresBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ChristofsPropertyBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ChristofsPropertyBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClassGeneratorFixesTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClassGeneratorFixesTest.groovy", [])
    }

    void "test src/test/groovy/bugs/ClassInNamedParamsBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClassInNamedParamsBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureInClosureBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureInClosureBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureParameterPassingBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureParameterPassingBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureTypedVariableBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureTypedVariableBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureVariableBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureVariableBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureWithBitwiseDefaultParamTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureWithBitwiseDefaultParamTest.groovy", [])
    }

    void "test src/test/groovy/bugs/ClosureWithStaticVariablesBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ClosureWithStaticVariablesBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ConstructorBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ConstructorBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ConstructorParameterBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ConstructorParameterBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ConstructorThisCallBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ConstructorThisCallBug.groovy", [])
    }

    void "test src/test/groovy/bugs/CustomMetaClassTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/CustomMetaClassTest.groovy", [])
    }

    void "test src/test/groovy/bugs/DefVariableBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/DefVariableBug.groovy", [])
    }

    void "test src/test/groovy/bugs/DirectMethodCallWithVargsTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/DirectMethodCallWithVargsTest.groovy", [])
    }

    void "test src/test/groovy/bugs/DoubleSizeParametersBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/DoubleSizeParametersBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ForLoopBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ForLoopBug.groovy", [])
    }

    void "test src/test/groovy/bugs/FullyQualifiedClassBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/FullyQualifiedClassBug.groovy", [])
    }

    void "test src/test/groovy/bugs/FullyQualifiedMethodReturnTypeBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/FullyQualifiedMethodReturnTypeBug.groovy", [])
    }

    void "test src/test/groovy/bugs/FullyQualifiedVariableTypeBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/FullyQualifiedVariableTypeBug.groovy", [])
    }

    void "test src/test/groovy/bugs/GROOVY3934Helper.groovy"() {
        unzipAndTest("src/test/groovy/bugs/GROOVY3934Helper.groovy", [])
    }

    void "test src/test/groovy/bugs/GetterBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/GetterBug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1018_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1018_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1059_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1059_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1081_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1081_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1407_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1407_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1462_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1462_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1465Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1465Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1593.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1593.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1617_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1617_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1706_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1706_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy1759_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy1759_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2271Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2271Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2339Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2339Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2348Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2348Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2350Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2350Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2351Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2351Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2365Base.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2365Base.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2391Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2391Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy239_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy239_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2432Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2432Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2490Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2490Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy252_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy252_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2549Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2549Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2556Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2556Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2557Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2557Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2558Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2558Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2666Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2666Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2706Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2706Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy278_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy278_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2801Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2801Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2816Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2816Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2849Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2849Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2949Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2949Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy2951Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy2951Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3069Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3069Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy308_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy308_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3135Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3135Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3139Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3139Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3156And2621Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3156And2621Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3163Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3163Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3175_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3175_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3205Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3205Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3208Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3208Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3235Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3235Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3238Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3238Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy325_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy325_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3304Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3304Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3305Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3305Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3311Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3311Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3335Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3335Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3339Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3339Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3383Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3383Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3389Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3389Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3403Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3403Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3405Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3405Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3410Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3410Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3424Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3424Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3426Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3426Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3462Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3462Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3464Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3464Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3465Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3465Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3465Helper.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3465Helper.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3498Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3498Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3509Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3509Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3511Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3511Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3519Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3519Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3560Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3560Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3574Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3574Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3590Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3590Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3596Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3596Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3645Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3645Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3658Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3658Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3679Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3679Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3716Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3716Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3718Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3718Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3719Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3719Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3719Bug_script.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3719Bug_script.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3720Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3720Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3721Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3721Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3723Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3723Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3726Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3726Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3731Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3731Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3749Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3749Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3768Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3768Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3770Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3770Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3776Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3776Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3784Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3784Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3789Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3789Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3799Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3799Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3801Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3801Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3817Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3817Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3818Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3818Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3827Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3827Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3830Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3830Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3831Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3831Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3834Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3834Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3839Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3839Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3852Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3852Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3857Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3857Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3863Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3863Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3868Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3868Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3871Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3871Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3873Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3873Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3876Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3876Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3894Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3894Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy389_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy389_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3904Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3904Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3948Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3948Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3949Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3949Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy3989Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy3989Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4006Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4006Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4009Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4009Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4025Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4025Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4029Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4029Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4035Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4035Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4038Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4038Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4043Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4043Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4046Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4046Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4069Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4069Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4075Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4075Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4078Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4078Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4080Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4080Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4081Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4081Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4098Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4098Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4098Child.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4098Child.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4098Parent.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4098Parent.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4104Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4104Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4106Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4106Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4107Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4107Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4111Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4111Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4116Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4116Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4119Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4119Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4120Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4120Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4121Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4121Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4129Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4129Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4131Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4131Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4133Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4133Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4134Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4134Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4139Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4139Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4145.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4145.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4151Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4151Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4169Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4169Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4170Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4170Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4188Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4188Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4190Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4190Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4191Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4191Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4193Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4193Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4202Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4202Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4206Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4206Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4235Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4235Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4241Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4241Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4243Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4243Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4246Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4246Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4247Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4247Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4252Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4252Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4257Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4257Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4264Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4264Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4272Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4272Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4273Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4273Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4293Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4293Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4325Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4325Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4356Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4356Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4386_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4386_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4393Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4393Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4410Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4410Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4414Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4414Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4415Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4415Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4416Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4416Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4418Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4418Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4435Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4435Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4449Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4449Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4457GenericTypeDeclarationLeakTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4457GenericTypeDeclarationLeakTest.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4471Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4471Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4480Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4480Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4497Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4497Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4516Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4516Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4584Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4584Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4607Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4607Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4614Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4614Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4720Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4720Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4857Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4857Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4861Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4861Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4922Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4922Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4958Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4958Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4966Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4966Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4967Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4967Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4973Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4973Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4980Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4980Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4986Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4986Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4989Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4989Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy4999Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy4999Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5025Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5025Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5030Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5030Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5033Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5033Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5056Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5056Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5061.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5061.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5101Test.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5101Test.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5109Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5109Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5122Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5122Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5137Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5137Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy513_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy513_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5150Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5150Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5152Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5152Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5185Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5185Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5193Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5193Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5210Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5210Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5212Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5212Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5259Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5259Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5260Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5260Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5267Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5267Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5272Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5272Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5285Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5285Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5396Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5396Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5418Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5418Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5425_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5425_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5572Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5572Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy558_616_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy558_616_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5687Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5687Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5783Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5783Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5802Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5802Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5806Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5806Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy5915Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy5915Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy596_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy596_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6041Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6041Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6042Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6042Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6045Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6045Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6072Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6072Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6086Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6086Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6374Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6374Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6396Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6396Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6508Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6508Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6522Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6522Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy662Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy662Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy666_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy666_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6722Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6722Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy674_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy674_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6755Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6755Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy675_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy675_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6764Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6764Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6786Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6786Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6804Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6804Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6808Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6808Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6811Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6811Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6821Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6821Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6830Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6830Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6841Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6841Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy6932Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy6932Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7081Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7081Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7520Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7520Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7620Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7620Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7709Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7709Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy770_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy770_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy779_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy779_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7876Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7876Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7912Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7912Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7916Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7916Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7917Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7917Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7920Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7920Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7921Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7921Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7922Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7922Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7924Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7924Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7925Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7925Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7937Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7937Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy7938Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy7938Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy831_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy831_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy872Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy872Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy965_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy965_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/Groovy996_Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/Groovy996_Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/GroovyInnerEnumBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/GroovyInnerEnumBug.groovy", [])
    }

    void "test src/test/groovy/bugs/GuillaumesBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/GuillaumesBug.groovy", [])
    }

    void "test src/test/groovy/bugs/GuillaumesMapBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/GuillaumesMapBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ImportNodeLineNumberTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ImportNodeLineNumberTest.groovy", [])
    }

    void "test src/test/groovy/bugs/InconsistentStackHeightBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/InconsistentStackHeightBug.groovy", [])
    }

    void "test src/test/groovy/bugs/InterfaceImplBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/InterfaceImplBug.groovy", [])
    }

    void "test src/test/groovy/bugs/InvokeNormalMethodFromBuilder_Groovy657Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/InvokeNormalMethodFromBuilder_Groovy657Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/IterateOverCustomTypeBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/IterateOverCustomTypeBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MarkupAndMethodBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MarkupAndMethodBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MetaClassCachingBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MetaClassCachingBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MethodCallWithoutParensInStaticMethodBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MethodCallWithoutParensInStaticMethodBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MethodClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MethodClosureTest.groovy", [])
    }

    void "test src/test/groovy/bugs/MethodDispatchBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MethodDispatchBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MethodPointerBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MethodPointerBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MorgansBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MorgansBug.groovy", [])
    }

    void "test src/test/groovy/bugs/MyConstantsASTTransformation4272.groovy"() {
        unzipAndTest("src/test/groovy/bugs/MyConstantsASTTransformation4272.groovy", [])
    }

    void "test src/test/groovy/bugs/NestedClosure2Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/NestedClosure2Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/NestedClosureBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/NestedClosureBug.groovy", [])
    }

    void "test src/test/groovy/bugs/NullAsBooleanCoercionTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/NullAsBooleanCoercionTest.groovy", [])
    }

    void "test src/test/groovy/bugs/NullCompareBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/NullCompareBug.groovy", [])
    }

    void "test src/test/groovy/bugs/OverloadInvokeMethodBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/OverloadInvokeMethodBug.groovy", [])
    }

    void "test src/test/groovy/bugs/POJOCallSiteBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/POJOCallSiteBug.groovy", [])
    }

    void "test src/test/groovy/bugs/PrimitivePropertyBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/PrimitivePropertyBug.groovy", [])
    }

    void "test src/test/groovy/bugs/PrintlnWithNewBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/PrintlnWithNewBug.groovy", [])
    }

    void "test src/test/groovy/bugs/PropertyNameBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/PropertyNameBug.groovy", [])
    }

    void "test src/test/groovy/bugs/RodsBooleanBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/RodsBooleanBug.groovy", [])
    }

    void "test src/test/groovy/bugs/RodsBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/RodsBug.groovy", [])
    }

    void "test src/test/groovy/bugs/RussellsOptionalParenTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/RussellsOptionalParenTest.groovy", [])
    }

    void "test src/test/groovy/bugs/SingleEvalTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SingleEvalTest.groovy", [])
    }

    void "test src/test/groovy/bugs/StaticClosurePropertyBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/StaticClosurePropertyBug.groovy", [])
    }

    void "test src/test/groovy/bugs/StaticMethodCallBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/StaticMethodCallBug.groovy", [])
    }

    void "test src/test/groovy/bugs/StaticMethodImportBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/StaticMethodImportBug.groovy", [])
    }

    void "test src/test/groovy/bugs/StaticMethodImportGroovy935Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/StaticMethodImportGroovy935Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/StaticPropertyBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/StaticPropertyBug.groovy", [])
    }

    void "test src/test/groovy/bugs/SubscriptAndExpressionBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SubscriptAndExpressionBug.groovy", [])
    }

    void "test src/test/groovy/bugs/SubscriptOnPrimitiveTypeArrayBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SubscriptOnPrimitiveTypeArrayBug.groovy", [])
    }

    void "test src/test/groovy/bugs/SubscriptOnStringArrayBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SubscriptOnStringArrayBug.groovy", [])
    }

    void "test src/test/groovy/bugs/SuperMethod2Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SuperMethod2Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/SuperMethodBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SuperMethodBug.groovy", [])
    }

    void "test src/test/groovy/bugs/SynchronizedBytecodeBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/SynchronizedBytecodeBug.groovy", [])
    }

    void "test src/test/groovy/bugs/TernaryOperatorTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TernaryOperatorTest.groovy", [])
    }

    void "test src/test/groovy/bugs/TestBase.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TestBase.groovy", [])
    }

    void "test src/test/groovy/bugs/TestCaseBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TestCaseBug.groovy", [])
    }

    void "test src/test/groovy/bugs/TestDerived.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TestDerived.groovy", [])
    }

    void "test src/test/groovy/bugs/ToStringBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ToStringBug.groovy", [])
    }

    void "test src/test/groovy/bugs/TryCatch2Bug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TryCatch2Bug.groovy", [])
    }

    void "test src/test/groovy/bugs/TryCatchBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/TryCatchBug.groovy", [])
    }

    void "test src/test/groovy/bugs/UnknownVariableBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/UnknownVariableBug.groovy", [])
    }

    void "test src/test/groovy/bugs/UseClosureInClosureBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/UseClosureInClosureBug.groovy", [])
    }

    void "test src/test/groovy/bugs/UseStaticInClosureBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/UseStaticInClosureBug.groovy", [])
    }

    void "test src/test/groovy/bugs/VariablePrecedence.groovy"() {
        unzipAndTest("src/test/groovy/bugs/VariablePrecedence.groovy", [])
    }

    void "test src/test/groovy/bugs/VariablePrecedenceTest.groovy"() {
        unzipAndTest("src/test/groovy/bugs/VariablePrecedenceTest.groovy", [])
    }

    void "test src/test/groovy/bugs/VariableScopingBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/VariableScopingBug.groovy", [])
    }

    void "test src/test/groovy/bugs/VerifyErrorBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/VerifyErrorBug.groovy", [])
    }

    void "test src/test/groovy/bugs/WriteOnlyPropertyBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/WriteOnlyPropertyBug.groovy", [])
    }

    void "test src/test/groovy/bugs/ZoharsBug.groovy"() {
        unzipAndTest("src/test/groovy/bugs/ZoharsBug.groovy", [])
    }

    void "test src/test/groovy/bugs/bug1567_script.groovy"() {
        unzipAndTest("src/test/groovy/bugs/bug1567_script.groovy", [])
    }

    void "test src/test/groovy/execute/ExecuteTest.groovy"() {
        unzipAndTest("src/test/groovy/execute/ExecuteTest.groovy", [])
    }

    void "test src/test/groovy/execute/ExecuteTest_LinuxSolaris.groovy"() {
        unzipAndTest("src/test/groovy/execute/ExecuteTest_LinuxSolaris.groovy", [])
    }

    void "test src/test/groovy/execute/ExecuteTest_Windows.groovy"() {
        unzipAndTest("src/test/groovy/execute/ExecuteTest_Windows.groovy", [])
    }

    void "test src/test/groovy/gpath/GPathTest.groovy"() {
        unzipAndTest("src/test/groovy/gpath/GPathTest.groovy", [])
    }

    void "test src/test/groovy/gpath/NodeGPathTest.groovy"() {
        unzipAndTest("src/test/groovy/gpath/NodeGPathTest.groovy", [])
    }

    void "test src/test/groovy/grape/GrabErrorIsolationTest.groovy"() {
        unzipAndTest("src/test/groovy/grape/GrabErrorIsolationTest.groovy", [])
    }

    void "test src/test/groovy/grape/GrabExcludeTest.groovy"() {
        unzipAndTest("src/test/groovy/grape/GrabExcludeTest.groovy", [])
    }

    void "test src/test/groovy/grape/GrabResolverTest.groovy"() {
        unzipAndTest("src/test/groovy/grape/GrabResolverTest.groovy", [])
    }

    void "test src/test/groovy/grape/GrapeClassLoaderTest.groovy"() {
        unzipAndTest("src/test/groovy/grape/GrapeClassLoaderTest.groovy", [])
    }

    void "test src/test/groovy/grape/GrapeIvyTest.groovy"() {
        unzipAndTest("src/test/groovy/grape/GrapeIvyTest.groovy", [])
    }

    void "test src/test/groovy/io/LineColumnReaderTest.groovy"() {
        unzipAndTest("src/test/groovy/io/LineColumnReaderTest.groovy", [])
    }

    void "test src/test/groovy/lang/BenchmarkInterceptorTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/BenchmarkInterceptorTest.groovy", [])
    }

    void "test src/test/groovy/lang/CategoryAnnotationTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/CategoryAnnotationTest.groovy", [])
    }

    void "test src/test/groovy/lang/ClassReloadingTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ClassReloadingTest.groovy", [])
    }

    void "test src/test/groovy/lang/ClosureResolvingTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ClosureResolvingTest.groovy", [])
    }

    void "test src/test/groovy/lang/DelegatingMetaClassTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/DelegatingMetaClassTest.groovy", [])
    }

    void "test src/test/groovy/lang/ExceptionTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ExceptionTest.groovy", [])
    }

    void "test src/test/groovy/lang/ExpandoMetaClassCreationHandleTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ExpandoMetaClassCreationHandleTest.groovy", [])
    }

    void "test src/test/groovy/lang/ExpandoMetaClassTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ExpandoMetaClassTest.groovy", [])
    }

    void "test src/test/groovy/lang/GetMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/GetMethodsTest.groovy", [])
    }

    void "test src/test/groovy/lang/Groovy3406Test.groovy"() {
        unzipAndTest("src/test/groovy/lang/Groovy3406Test.groovy", [])
    }

    void "test src/test/groovy/lang/GroovyClassLoaderTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/GroovyClassLoaderTest.groovy", [])
    }

    void "test src/test/groovy/lang/GroovyCodeSourceTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/GroovyCodeSourceTest.groovy", [])
    }

    void "test src/test/groovy/lang/GroovyShellTest2.groovy"() {
        unzipAndTest("src/test/groovy/lang/GroovyShellTest2.groovy", [])
    }

    void "test src/test/groovy/lang/GroovySystemTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/GroovySystemTest.groovy", [])
    }

    void "test src/test/groovy/lang/InnerClassResolvingTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/InnerClassResolvingTest.groovy", [])
    }

    void "test src/test/groovy/lang/IntRangeTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/IntRangeTest.groovy", [])
    }

    void "test src/test/groovy/lang/InterceptorTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/InterceptorTest.groovy", [])
    }

    void "test src/test/groovy/lang/MapOfClosureTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MapOfClosureTest.groovy", [])
    }

    void "test src/test/groovy/lang/MetaClassPropertyTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MetaClassPropertyTest.groovy", [])
    }

    void "test src/test/groovy/lang/MetaClassRegistryTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MetaClassRegistryTest.groovy", [])
    }

    void "test src/test/groovy/lang/MethodMissingTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MethodMissingTest.groovy", [])
    }

    void "test src/test/groovy/lang/MixinAnnotationTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MixinAnnotationTest.groovy", [])
    }

    void "test src/test/groovy/lang/MixinTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/MixinTest.groovy", [])
    }

    void "test src/test/groovy/lang/PropertyMissingTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/PropertyMissingTest.groovy", [])
    }

    void "test src/test/groovy/lang/ReferenceSerializationTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ReferenceSerializationTest.groovy", [])
    }

    void "test src/test/groovy/lang/RespondsToTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/RespondsToTest.groovy", [])
    }

    void "test src/test/groovy/lang/ScriptCacheTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ScriptCacheTest.groovy", [])
    }

    void "test src/test/groovy/lang/ScriptSourcePositionInAstTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/ScriptSourcePositionInAstTest.groovy", [])
    }

    void "test src/test/groovy/lang/StringConcatTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/StringConcatTest.groovy", [])
    }

    void "test src/test/groovy/lang/StripMarginTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/StripMarginTest.groovy", [])
    }

    void "test src/test/groovy/lang/SyntheticReturnTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/SyntheticReturnTest.groovy", [])
    }

    void "test src/test/groovy/lang/WithMethodTest.groovy"() {
        unzipAndTest("src/test/groovy/lang/WithMethodTest.groovy", [])
    }

    void "test src/test/groovy/lang/gcldeadlock/DeadlockBugUtil.groovy"() {
        unzipAndTest("src/test/groovy/lang/gcldeadlock/DeadlockBugUtil.groovy", [])
    }

    void "test src/test/groovy/lang/gcldeadlock/script0.groovy"() {
        unzipAndTest("src/test/groovy/lang/gcldeadlock/script0.groovy", [])
    }

    void "test src/test/groovy/lang/gcldeadlock/script1.groovy"() {
        unzipAndTest("src/test/groovy/lang/gcldeadlock/script1.groovy", [])
    }

    void "test src/test/groovy/mock/example/CheeseSlicer.groovy"() {
        unzipAndTest("src/test/groovy/mock/example/CheeseSlicer.groovy", [])
    }

    void "test src/test/groovy/mock/example/SandwichMaker.groovy"() {
        unzipAndTest("src/test/groovy/mock/example/SandwichMaker.groovy", [])
    }

    void "test src/test/groovy/mock/example/SandwichMakerTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/example/SandwichMakerTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/Caller.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/Caller.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/Collaborator.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/Collaborator.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/HalfMockTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/HalfMockTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/MockCallSequenceTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/MockCallSequenceTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/MockForJavaTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/MockForJavaTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/MockNestedCallTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/MockNestedCallTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/MockSingleCallTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/MockSingleCallTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/MockWithZeroRangeTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/MockWithZeroRangeTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/StubCallSequenceTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/StubCallSequenceTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/StubForJavaTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/StubForJavaTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/StubSingleCallTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/StubSingleCallTest.groovy", [])
    }

    void "test src/test/groovy/mock/interceptor/StubTest.groovy"() {
        unzipAndTest("src/test/groovy/mock/interceptor/StubTest.groovy", [])
    }

    void "test src/test/groovy/operator/BigDecimalOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/BigDecimalOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/BigIntegerOperationsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/BigIntegerOperationsTest.groovy", [])
    }

    void "test src/test/groovy/operator/BitwiseOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/BitwiseOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/BooleanOperationsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/BooleanOperationsTest.groovy", [])
    }

    void "test src/test/groovy/operator/DoubleOperationTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/DoubleOperationTest.groovy", [])
    }

    void "test src/test/groovy/operator/IntegerOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/IntegerOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/MyColor.groovy"() {
        unzipAndTest("src/test/groovy/operator/MyColor.groovy", [])
    }

    void "test src/test/groovy/operator/MyColorCategory.groovy"() {
        unzipAndTest("src/test/groovy/operator/MyColorCategory.groovy", [])
    }

    void "test src/test/groovy/operator/MyColorOperatorOverloadingTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/MyColorOperatorOverloadingTest.groovy", [])
    }

    void "test src/test/groovy/operator/NegateListsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/NegateListsTest.groovy", [])
    }

    void "test src/test/groovy/operator/PowerOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/PowerOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/SpreadListOperatorTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/SpreadListOperatorTest.groovy", [])
    }

    void "test src/test/groovy/operator/SpreadMapOperatorTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/SpreadMapOperatorTest.groovy", [])
    }

    void "test src/test/groovy/operator/StringOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/StringOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/TernaryOperatorsTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/TernaryOperatorsTest.groovy", [])
    }

    void "test src/test/groovy/operator/UnaryMinusNumberTests.groovy"() {
        unzipAndTest("src/test/groovy/operator/UnaryMinusNumberTests.groovy", [])
    }

    void "test src/test/groovy/operator/UnaryMinusOperatorTest.groovy"() {
        unzipAndTest("src/test/groovy/operator/UnaryMinusOperatorTest.groovy", [])
    }

    void "test src/test/groovy/runtime/metaclass/groovy/bugs/CustomMetaClassTestMetaClass.groovy"() {
        unzipAndTest("src/test/groovy/runtime/metaclass/groovy/bugs/CustomMetaClassTestMetaClass.groovy", [])
    }

    void "test src/test/groovy/script/CallAnotherScript.groovy"() {
        unzipAndTest("src/test/groovy/script/CallAnotherScript.groovy", [])
    }

    void "test src/test/groovy/script/ClassWithScript.groovy"() {
        unzipAndTest("src/test/groovy/script/ClassWithScript.groovy", [])
    }

    void "test src/test/groovy/script/EvalInScript.groovy"() {
        unzipAndTest("src/test/groovy/script/EvalInScript.groovy", [])
    }

    void "test src/test/groovy/script/HelloWorld.groovy"() {
        unzipAndTest("src/test/groovy/script/HelloWorld.groovy", [])
    }

    void "test src/test/groovy/script/HelloWorld2.groovy"() {
        unzipAndTest("src/test/groovy/script/HelloWorld2.groovy", [])
    }

    void "test src/test/groovy/script/MethodTestScript.groovy"() {
        unzipAndTest("src/test/groovy/script/MethodTestScript.groovy", [])
    }

    void "test src/test/groovy/script/PackageScript.groovy"() {
        unzipAndTest("src/test/groovy/script/PackageScript.groovy", [])
    }

    void "test src/test/groovy/script/ScriptTest.groovy"() {
        unzipAndTest("src/test/groovy/script/ScriptTest.groovy", [])
    }

    void "test src/test/groovy/script/ScriptWithFunctions.groovy"() {
        unzipAndTest("src/test/groovy/script/ScriptWithFunctions.groovy", [])
    }

    void "test src/test/groovy/script/ShowArgs.groovy"() {
        unzipAndTest("src/test/groovy/script/ShowArgs.groovy", [])
    }

    void "test src/test/groovy/script/StreamClassloaderInScriptTest.groovy"() {
        unzipAndTest("src/test/groovy/script/StreamClassloaderInScriptTest.groovy", [])
    }

    void "test src/test/groovy/script/UseClosureInScript.groovy"() {
        unzipAndTest("src/test/groovy/script/UseClosureInScript.groovy", [])
    }

    void "test src/test/groovy/time/DurationTest.groovy"() {
        unzipAndTest("src/test/groovy/time/DurationTest.groovy", [])
    }

    void "test src/test/groovy/time/TimeCategoryTest.groovy"() {
        unzipAndTest("src/test/groovy/time/TimeCategoryTest.groovy", [])
    }

    void "test src/test/groovy/transform/AnnotationCollectorTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/AnnotationCollectorTest.groovy", [])
    }

    void "test src/test/groovy/transform/ConditionalInterruptTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/ConditionalInterruptTest.groovy", [])
    }

    void "test src/test/groovy/transform/LazyTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/LazyTest.groovy", [])
    }

    void "test src/test/groovy/transform/ReadWriteLockTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/ReadWriteLockTest.groovy", [])
    }

    void "test src/test/groovy/transform/ThreadInterruptTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/ThreadInterruptTest.groovy", [])
    }

    void "test src/test/groovy/transform/TimedInterruptTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/TimedInterruptTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/AnonymousInnerClassSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/AnonymousInnerClassSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ArraysAndCollectionsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ArraysAndCollectionsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/BugsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/BugsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/CategoriesSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/CategoriesSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ClosureParamTypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ClosureParamTypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ClosureParamTypeResolverSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ClosureParamTypeResolverSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ClosuresSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ClosuresSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/CoercionSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/CoercionSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ConstructorsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ConstructorsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/CustomErrorCollectorSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/CustomErrorCollectorSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/DefaultGroovyMethodsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/DefaultGroovyMethodsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/DelegatesToSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/DelegatesToSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/FieldsAndPropertiesSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/FieldsAndPropertiesSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/GenericsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/GenericsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7184Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7184Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7542Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7542Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7774Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7774Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7880Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7880Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7888Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7888Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7907Bug.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7907Bug.groovy", [])
    }

    void "test src/test/groovy/transform/stc/Groovy7907HelperPrecompiledGroovy.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/Groovy7907HelperPrecompiledGroovy.groovy", [])
    }

    void "test src/test/groovy/transform/stc/IOGMClosureParamTypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/IOGMClosureParamTypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/LoopsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/LoopsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/MethodCallsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/MethodCallsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/MiscSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/MiscSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/PrecompiledExtension.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/PrecompiledExtension.groovy", [])
    }

    void "test src/test/groovy/transform/stc/PrecompiledExtensionNotExtendingDSL.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/PrecompiledExtensionNotExtendingDSL.groovy", [])
    }

    void "test src/test/groovy/transform/stc/RangesSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/RangesSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ResourceGMClosureParamTypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ResourceGMClosureParamTypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/ReturnsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/ReturnsSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/STCAssignmentTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/STCAssignmentTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/STCExtensionMethodsTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/STCExtensionMethodsTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/STCnAryExpressionTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/STCnAryExpressionTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/STCwithTransformationsTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/STCwithTransformationsTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/SocketGMClosureParamTypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/SocketGMClosureParamTypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/StaticTypeCheckingTestCase.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/StaticTypeCheckingTestCase.groovy", [])
    }

    void "test src/test/groovy/transform/stc/StringGMClosureParamTypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/StringGMClosureParamTypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/TernaryOperatorSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/TernaryOperatorSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/TypeCheckingExtensionsTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/TypeCheckingExtensionsTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/TypeCheckingModeTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/TypeCheckingModeTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/TypeInferenceSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/TypeInferenceSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/UnaryOperatorSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/UnaryOperatorSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/WithSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/WithSTCTest.groovy", [])
    }

    void "test src/test/groovy/transform/stc/vm6/MethodCallsSTCTest.groovy"() {
        unzipAndTest("src/test/groovy/transform/stc/vm6/MethodCallsSTCTest.groovy", [])
    }

    void "test src/test/groovy/tree/ClosureClassLoaderBug.groovy"() {
        unzipAndTest("src/test/groovy/tree/ClosureClassLoaderBug.groovy", [])
    }

    void "test src/test/groovy/tree/NavigationTest.groovy"() {
        unzipAndTest("src/test/groovy/tree/NavigationTest.groovy", [])
    }

    void "test src/test/groovy/tree/NestedClosureBugTest.groovy"() {
        unzipAndTest("src/test/groovy/tree/NestedClosureBugTest.groovy", [])
    }

    void "test src/test/groovy/tree/SmallTreeTest.groovy"() {
        unzipAndTest("src/test/groovy/tree/SmallTreeTest.groovy", [])
    }

    void "test src/test/groovy/tree/TreeTest.groovy"() {
        unzipAndTest("src/test/groovy/tree/TreeTest.groovy", [])
    }

    void "test src/test/groovy/tree/VerboseTreeTest.groovy"() {
        unzipAndTest("src/test/groovy/tree/VerboseTreeTest.groovy", [])
    }

    void "test src/test/groovy/txn/TransactionTest.groovy"() {
        unzipAndTest("src/test/groovy/txn/TransactionTest.groovy", [])
    }

    void "test src/test/groovy/ui/GroovyMainTest.groovy"() {
        unzipAndTest("src/test/groovy/ui/GroovyMainTest.groovy", [])
    }

    void "test src/test/groovy/util/BufferedIteratorTest.groovy"() {
        unzipAndTest("src/test/groovy/util/BufferedIteratorTest.groovy", [])
    }

    void "test src/test/groovy/util/BuilderSupportTest.groovy"() {
        unzipAndTest("src/test/groovy/util/BuilderSupportTest.groovy", [])
    }

    void "test src/test/groovy/util/CliBuilderTest.groovy"() {
        unzipAndTest("src/test/groovy/util/CliBuilderTest.groovy", [])
    }

    void "test src/test/groovy/util/ConfigObjectTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ConfigObjectTest.groovy", [])
    }

    void "test src/test/groovy/util/ConfigSlurperTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ConfigSlurperTest.groovy", [])
    }

    void "test src/test/groovy/util/DelegatingScriptTest.groovy"() {
        unzipAndTest("src/test/groovy/util/DelegatingScriptTest.groovy", [])
    }

    void "test src/test/groovy/util/FactoryBuilderSupportTest.groovy"() {
        unzipAndTest("src/test/groovy/util/FactoryBuilderSupportTest.groovy", [])
    }

    void "test src/test/groovy/util/FileTreeBuilderTest.groovy"() {
        unzipAndTest("src/test/groovy/util/FileTreeBuilderTest.groovy", [])
    }

    void "test src/test/groovy/util/GroovyCollectionsStarImportTest.groovy"() {
        unzipAndTest("src/test/groovy/util/GroovyCollectionsStarImportTest.groovy", [])
    }

    void "test src/test/groovy/util/GroovyCollectionsTest.groovy"() {
        unzipAndTest("src/test/groovy/util/GroovyCollectionsTest.groovy", [])
    }

    void "test src/test/groovy/util/GroovyScriptEngineReloadingTest.groovy"() {
        unzipAndTest("src/test/groovy/util/GroovyScriptEngineReloadingTest.groovy", [])
    }

    void "test src/test/groovy/util/GroovyScriptEngineTest.groovy"() {
        unzipAndTest("src/test/groovy/util/GroovyScriptEngineTest.groovy", [])
    }

    void "test src/test/groovy/util/HeadlessTestSupport.groovy"() {
        unzipAndTest("src/test/groovy/util/HeadlessTestSupport.groovy", [])
    }

    void "test src/test/groovy/util/IndentPrinterTest.groovy"() {
        unzipAndTest("src/test/groovy/util/IndentPrinterTest.groovy", [])
    }

    void "test src/test/groovy/util/JavadocAssertionTestBuilderTest.groovy"() {
        unzipAndTest("src/test/groovy/util/JavadocAssertionTestBuilderTest.groovy", [])
    }

    void "test src/test/groovy/util/MiscScriptTest.groovy"() {
        unzipAndTest("src/test/groovy/util/MiscScriptTest.groovy", [])
    }

    void "test src/test/groovy/util/NodeTest.groovy"() {
        unzipAndTest("src/test/groovy/util/NodeTest.groovy", [])
    }

    void "test src/test/groovy/util/ObjectGraphBuilderTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ObjectGraphBuilderTest.groovy", [])
    }

    void "test src/test/groovy/util/ObservableListTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ObservableListTest.groovy", [])
    }

    void "test src/test/groovy/util/ObservableMapTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ObservableMapTest.groovy", [])
    }

    void "test src/test/groovy/util/ObservableSetTests.groovy"() {
        unzipAndTest("src/test/groovy/util/ObservableSetTests.groovy", [])
    }

    void "test src/test/groovy/util/OrderByTest.groovy"() {
        unzipAndTest("src/test/groovy/util/OrderByTest.groovy", [])
    }

    void "test src/test/groovy/util/ProxyGeneratorAdapterTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ProxyGeneratorAdapterTest.groovy", [])
    }

    void "test src/test/groovy/util/ProxyGeneratorTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ProxyGeneratorTest.groovy", [])
    }

    void "test src/test/groovy/util/ProxyTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ProxyTest.groovy", [])
    }

    void "test src/test/groovy/util/ResourceBundleTest.groovy"() {
        unzipAndTest("src/test/groovy/util/ResourceBundleTest.groovy", [])
    }

    void "test src/test/groovy/util/logging/CommonsTest.groovy"() {
        unzipAndTest("src/test/groovy/util/logging/CommonsTest.groovy", [])
    }

    void "test src/test/groovy/util/logging/Log4j2Test.groovy"() {
        unzipAndTest("src/test/groovy/util/logging/Log4j2Test.groovy", [])
    }

    void "test src/test/groovy/util/logging/Log4jTest.groovy"() {
        unzipAndTest("src/test/groovy/util/logging/Log4jTest.groovy", [])
    }

    void "test src/test/groovy/util/logging/LogTest.groovy"() {
        unzipAndTest("src/test/groovy/util/logging/LogTest.groovy", [])
    }

    void "test src/test/groovy/util/logging/Slf4jTest.groovy"() {
        unzipAndTest("src/test/groovy/util/logging/Slf4jTest.groovy", [])
    }

    void "test src/test/indy/IndyUsageTest.groovy"() {
        unzipAndTest("src/test/indy/IndyUsageTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ClosureAndInnerClassNodeStructureTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ClosureAndInnerClassNodeStructureTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/antlr/AntlrParserPluginTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/antlr/AntlrParserPluginTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/antlr/GStringEndTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/antlr/GStringEndTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/CodeVisitorSupportTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/CodeVisitorSupportTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/GenericsTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/GenericsTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/GenericsTypeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/GenericsTypeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/LazyInitOnClassNodeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/LazyInitOnClassNodeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/LineColumnCheckTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/LineColumnCheckTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/MethodNodeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/MethodNodeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/AstAssert.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/AstAssert.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/AstBuilderFromCodeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/AstBuilderFromCodeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/AstBuilderFromSpecificationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/AstBuilderFromSpecificationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/AstBuilderFromStringTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/AstBuilderFromStringTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/WithAstBuilder.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/WithAstBuilder.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/builder/testpackage/AstBuilderFromCodePackageImportTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/builder/testpackage/AstBuilderFromCodePackageImportTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/decompiled/AsmDecompilerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/decompiled/AsmDecompilerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/decompiled/IncrementalRecompilationWithStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/decompiled/IncrementalRecompilationWithStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/expr/ClosureExpressionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/expr/ClosureExpressionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/expr/MapExpressionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/expr/MapExpressionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/expr/MethodCallExpressionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/expr/MethodCallExpressionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/expr/PropertyExpressionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/expr/PropertyExpressionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/source/Groovy3049Test.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/source/Groovy3049Test.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/source/Groovy3050Test.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/source/Groovy3050Test.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/source/Groovy3051Test.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/source/Groovy3051Test.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/source/SourceBaseTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/source/SourceBaseTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/ast/tools/WideningCategoriesTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/ast/tools/WideningCategoriesTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/binarytrees.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/binarytrees.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/fannkuch.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/fannkuch.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/partialsums.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/partialsums.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/rayTracer.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/rayTracer.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/recursive.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/recursive.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/alioth/spectralnorm.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/alioth/spectralnorm.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script120.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script120.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script240.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script240.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script30.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script30.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script300.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script300.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script300WithCategory.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script300WithCategory.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script60.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/benchmarks/vm5/b2394/script60.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/BytecodeHelperTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/BytecodeHelperTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/CallClosureFieldAsMethodTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/CallClosureFieldAsMethodTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/CallSiteTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/CallSiteTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/CastTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/CastTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/CastToStringTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/CastToStringTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/ConstructorIssueTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/ConstructorIssueTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/FinalVariableAnalyzerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/FinalVariableAnalyzerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/GenericsGenTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/GenericsGenTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/InterfaceTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/InterfaceTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/Main.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/Main.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/MetaClassTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/MetaClassTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/MyBean.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/MyBean.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/ReflectorLoaderTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/ReflectorLoaderTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/AbstractBytecodeTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/AbstractBytecodeTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/BinaryOperationsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/BinaryOperationsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/CovariantReturnBytecodeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/CovariantReturnBytecodeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/DirectMethodCallTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/DirectMethodCallTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/HotSwapTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/HotSwapTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/InstructionSequenceHelperClassTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/InstructionSequenceHelperClassTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/MethodPatternsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/MethodPatternsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/PrintlnLoadsAConstantTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/PrintlnLoadsAConstantTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/AnonymousInnerClassStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/AnonymousInnerClassStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/ArraysAndCollectionsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/ArraysAndCollectionsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/AssignmentsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/AssignmentsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/BugsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/BugsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/ClosureParamTypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/ClosureParamTypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/ClosuresStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/ClosuresStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/CombinedIndyAndStaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/CombinedIndyAndStaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/CompatWithASTXFormStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/CompatWithASTXFormStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/CompileDynamicTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/CompileDynamicTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/DelegatesToStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/DelegatesToStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/FieldsAndPropertiesStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/FieldsAndPropertiesStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/GenericsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/GenericsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/GetAnnotationStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/GetAnnotationStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/Groovy7222OptimizationsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/Groovy7222OptimizationsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/IOGMClosureParamTypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/IOGMClosureParamTypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/LoopsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/LoopsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/MethodCallsStaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/MethodCallsStaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/MiscStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/MiscStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/MixedModeStaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/MixedModeStaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/NaryExpressionTestStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/NaryExpressionTestStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/RangesStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/RangesStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/ResourceGMClosureParamTypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/ResourceGMClosureParamTypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/ReturnsStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/ReturnsStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/SocketGMClosureParamTypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/SocketGMClosureParamTypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilationTestSupport.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilationTestSupport.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileArrayLengthAndGet.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileArrayLengthAndGet.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileCastOptimizationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileCastOptimizationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileClosureCallTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileClosureCallTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileComparisonTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileComparisonTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileConstructorsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileConstructorsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileDGMMethodTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileDGMMethodTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileDGMTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileDGMTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileFieldAccessTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileFieldAccessTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileFlowTypingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileFlowTypingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileInnerClassTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileInnerClassTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileMathTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileMathTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileNullCompareOptimizationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompileNullCompareOptimizationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilePostfixPrefixTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StaticCompilePostfixPrefixTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/StringGMClosureParamTypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/StringGMClosureParamTypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/TupleConstructorStaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/TupleConstructorStaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/TypeCheckingModeStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/TypeCheckingModeStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/TypeInferenceStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/TypeInferenceStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/UnaryOperatorStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/UnaryOperatorStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/WithStaticCompileTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/WithStaticCompileTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6240Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6240Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6276Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6276Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6411Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6411Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6475Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6475Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6533Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6533Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6541Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6541Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6558Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6558Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6564Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6564Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6568Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6568Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6627Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6627Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6650Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6650Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6657Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6657Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6670Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6670Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6671Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6671Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6676Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6676Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6693Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6693Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6724Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6724Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6733Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6733Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6757Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6757Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6782Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6782Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6962Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy6962Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7039Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7039Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7041Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7041Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7042Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7042Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7072Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7072Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7075Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7075Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7093Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7093Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7098Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7098Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7133Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7133Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7138Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7138Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7145Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7145Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7149Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7149Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7169Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7169Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7210Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7210Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7211Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7211Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7242Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7242Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7276Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7276Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7298Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7298Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7300Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7300Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7307Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7307Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7316Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7316Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7322Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7322Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7324Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7324Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7325Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7325Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7327Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7327Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7333Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7333Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7343Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7343Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7355Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7355Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7356Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7356Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7357Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7357Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7358Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7358Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7361Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7361Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7363Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7363Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7364Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7364Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7365Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7365Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7420Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7420Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7538Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7538Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7870Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/bugs/Groovy7870Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/classgen/asm/sc/vm6/MethodCallsStaticCompilationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/classgen/asm/sc/vm6/MethodCallsStaticCompilationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/customizers/ASTTransformationCustomizerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/customizers/ASTTransformationCustomizerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/customizers/ImportCustomizerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/customizers/ImportCustomizerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/customizers/SecureASTCustomizerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/customizers/SecureASTCustomizerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/customizers/builder/CompilerCustomizationBuilderTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/customizers/builder/CompilerCustomizationBuilderTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/io/FileReaderTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/io/FileReaderTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/io/NullWriterTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/io/NullWriterTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/control/io/StringReaderSourceTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/control/io/StringReaderSourceTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/dummy/ClassWithStaticMethod.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/dummy/ClassWithStaticMethod.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/reflection/CachedMethodTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/reflection/CachedMethodTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/reflection/GroovyClassValueFactoryTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/reflection/GroovyClassValueFactoryTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/reflection/utils/ReflectionUtilsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/reflection/utils/ReflectionUtilsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/AppendableDgmMethodsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/AppendableDgmMethodsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/CategoryForIteratorTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/CategoryForIteratorTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/CustomBooleanCoercionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/CustomBooleanCoercionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/DateGDKTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/DateGDKTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/DefaultGroovyMethodsSupportTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/DefaultGroovyMethodsSupportTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/DefaultGroovyMethodsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/DefaultGroovyMethodsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/DirectoryDeleteTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/DirectoryDeleteTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/EachLineTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/EachLineTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/EachWithReaderAndInputStreamTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/EachWithReaderAndInputStreamTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/FileAppendTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/FileAppendTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/FileLeftShiftTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/FileLeftShiftTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/FileStaticGroovyMethodsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/FileStaticGroovyMethodsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/GroovyCategoryTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/GroovyCategoryTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/InterfaceConversionTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/InterfaceConversionTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/InvokerHelperFormattingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/InvokerHelperFormattingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/JdkDynamicProxyServiceBeanImpl1.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/JdkDynamicProxyServiceBeanImpl1.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/JdkDynamicProxyServiceBeanImpl2.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/JdkDynamicProxyServiceBeanImpl2.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/MinusTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/MinusTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/NestedCategoryTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/NestedCategoryTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/NullObjectTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/NullObjectTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/PerInstanceMetaClassTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/PerInstanceMetaClassTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/ResourceGroovyMethodsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/ResourceGroovyMethodsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/StaticPrintlnTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/StaticPrintlnTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/StringAsClassTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/StringAsClassTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/URLGetBytesTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/URLGetBytesTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/URLGetTextTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/URLGetTextTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/WithResourceStreamClosedTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/WithResourceStreamClosedTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/WriterAppendTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/WriterAppendTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/m12n/ExtensionModuleHelperForTests.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/m12n/ExtensionModuleHelperForTests.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/m12n/ExtensionModuleTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/m12n/ExtensionModuleTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/AbstractMemoizeTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/AbstractMemoizeTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/CacheCleanupCollectedSoftReferencesTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/CacheCleanupCollectedSoftReferencesTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/CacheCleanupTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/CacheCleanupTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/LRUProtectionStorageTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/LRUProtectionStorageTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/MemoizeAtLeastTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/MemoizeAtLeastTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/MemoizeAtMostTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/MemoizeAtMostTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/MemoizeBetweenTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/MemoizeBetweenTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/MemoizeTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/MemoizeTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/memoize/NullValueTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/memoize/NullValueTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/AssertionRenderingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/AssertionRenderingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/AssertionTestUtil.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/AssertionTestUtil.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/AssertionsInDifferentLocationsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/AssertionsInDifferentLocationsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/EvaluationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/EvaluationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/ImplicitClosureCallRenderingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/ImplicitClosureCallRenderingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/NotTransformedAssertionsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/NotTransformedAssertionsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/PowerAssertASTTransformationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/PowerAssertASTTransformationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/ScriptEvaluationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/ScriptEvaluationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/powerassert/ValueRenderingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/powerassert/ValueRenderingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/trampoline/TrampolineTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/trampoline/TrampolineTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/typehandling/NumberMathTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/typehandling/NumberMathTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/runtime/typehandling/ShortTypeHandlingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/runtime/typehandling/ShortTypeHandlingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/LoaderConfigurationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/LoaderConfigurationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/StringHelperTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/StringHelperTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/UtilitiesTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/UtilitiesTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/ast/TransformTestHelperTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/ast/TransformTestHelperTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/rootloadersync/AbstractGenericGroovySuperclass.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/rootloadersync/AbstractGenericGroovySuperclass.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/rootloadersync/AbstractGroovySuperclass.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/rootloadersync/AbstractGroovySuperclass.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/rootloadersync/SubclassingInGroovyTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/rootloadersync/SubclassingInGroovyTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationCollectorStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationCollectorStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationDefaultValuesStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationDefaultValuesStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV1StubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV1StubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV2StubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV2StubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV3StubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV3StubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV4StubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/AnnotationMemberValuesResolutionV4StubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ArrayAnnotationsShouldAppearInStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ArrayAnnotationsShouldAppearInStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/BadGenericsExpansionOnInnerClassStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/BadGenericsExpansionOnInnerClassStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/CircularLanguageReferenceTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/CircularLanguageReferenceTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/DefaultValueReturnTypeShouldUseGenericsStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/DefaultValueReturnTypeShouldUseGenericsStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/DuplicateMethodAdditionInStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/DuplicateMethodAdditionInStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/EnsureClassAnnotationPresentInStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/EnsureClassAnnotationPresentInStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/EscapingOfStringAnnotationValuesTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/EscapingOfStringAnnotationValuesTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ExceptionThrowingSuperConstructorTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ExceptionThrowingSuperConstructorTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/GenericsTypesHavePackageNamesStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/GenericsTypesHavePackageNamesStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/GenericsWithExtendsStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/GenericsWithExtendsStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy4248Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy4248Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy5859Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy5859Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6302Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6302Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6404Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6404Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6617Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6617Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6855Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy6855Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7052Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7052Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7113Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7113Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7366Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7366Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7366BugVariant.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7366BugVariant.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7747Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/Groovy7747Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ImmutableWithJointCompilationGroovy6836StubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ImmutableWithJointCompilationGroovy6836StubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ImmutableWithJointCompilationStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ImmutableWithJointCompilationStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ImportAliasesShouldNotAppearInStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ImportAliasesShouldNotAppearInStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/ImportStaticAliasTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/ImportStaticAliasTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/InnerAnnotationStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/InnerAnnotationStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/InterfaceWithPrimitiveFieldsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/InterfaceWithPrimitiveFieldsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/MultilineStringStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/MultilineStringStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/NestedGenericsTypesStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/NestedGenericsTypesStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/NoStaticGetMetaClassSyntheticMethodInStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/NoStaticGetMetaClassSyntheticMethodInStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/PropertyUsageFromJavaTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/PropertyUsageFromJavaTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/PropertyWithCustomSetterHavingReturnTypeStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/PropertyWithCustomSetterHavingReturnTypeStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/QDoxCategory.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/QDoxCategory.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/RedundantCastInStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/RedundantCastInStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/StringSourcesStubTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/StringSourcesStubTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/StubGenerationForAnAnnotationStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/StubGenerationForAnAnnotationStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/StubGenerationForConstructorWithOptionalArgsStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/StubGenerationForConstructorWithOptionalArgsStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/StubTestCase.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/StubTestCase.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/UnAmbigousSuperConstructorCallStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/UnAmbigousSuperConstructorCallStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/VarargsMethodParamsStubTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/VarargsMethodParamsStubTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/tools/stubgenerator/WrongCastForGenericReturnValueOfMethodStubsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/tools/stubgenerator/WrongCastForGenericReturnValueOfMethodStubsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/AutoCloneTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/AutoCloneTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/AutoImplementTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/AutoImplementTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/BaseScriptTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/BaseScriptTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/BuilderTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/BuilderTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/CanonicalComponentsTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/CanonicalComponentsTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/CanonicalTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/CanonicalTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/DelegateTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/DelegateTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/EqualsAndHashCodeTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/EqualsAndHashCodeTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/ExternalizeMethodsTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/ExternalizeMethodsTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/ExternalizeVerifierTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/ExternalizeVerifierTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/FakeURLFactory.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/FakeURLFactory.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/FieldTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/FieldTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/GlobalTestTransformClassLoader.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/GlobalTestTransformClassLoader.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/GlobalTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/GlobalTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/ImmutableTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/ImmutableTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/IndexedPropertyTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/IndexedPropertyTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/InheritConstructorsTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/InheritConstructorsTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/LazyTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/LazyTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/LocalASTTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/LocalASTTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/MapConstructorTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/MapConstructorTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/MemoizedASTTransformationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/MemoizedASTTransformationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/NewifyTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/NewifyTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/PackageScopeTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/PackageScopeTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/ReadWriteLockTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/ReadWriteLockTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/SingletonTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/SingletonTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/SortableTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/SortableTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/SourceURITransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/SourceURITransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/SynchronizedTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/SynchronizedTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/TestTransform.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/TestTransform.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/ToStringTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/ToStringTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/TupleConstructorTransformTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/TupleConstructorTransformTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/classloading/TransformsAndCustomClassLoadersTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/classloading/TransformsAndCustomClassLoadersTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/stc/SignatureCodecTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/stc/SignatureCodecTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/InWhileLoopWrapperTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/InWhileLoopWrapperTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/ParameterMappingTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/ParameterMappingTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/RecursiveListExamples.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/RecursiveListExamples.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/RecursivenessTesterTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/RecursivenessTesterTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/ReturnAdderForClosuresTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/ReturnAdderForClosuresTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/ReturnStatementToIterationConverterTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/ReturnStatementToIterationConverterTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/StatementReplacerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/StatementReplacerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveCompilationFailuresTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveCompilationFailuresTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveExamples.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveExamples.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveTogetherWithOtherASTsTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveTogetherWithOtherASTsTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveTransformationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/TailRecursiveTransformationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/TernaryToIfStatementConverterTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/TernaryToIfStatementConverterTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/tailrec/VariableExpressionReplacerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/tailrec/VariableExpressionReplacerTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy6697Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy6697Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy6736Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy6736Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy6741Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy6741Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7011Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7011Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7190Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7190Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7196Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7196Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7196SupportTrait.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7196SupportTrait.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7196SupportTraitImpl.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7196SupportTraitImpl.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7206Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7206Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7213Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7213Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7214Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7214Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7215Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7215Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7215SupportTrait.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7215SupportTrait.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7217Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7217Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7255Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7255Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7269Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7269Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7275Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7275Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7285Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7285Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7456Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7456Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7846Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7846Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/Groovy7926Bug.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/Groovy7926Bug.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/TestTrait2.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/TestTrait2.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/transform/traitx/TraitASTTransformationTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/transform/traitx/TraitASTTransformationTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/AbstractConcurrentMapSegmentTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/AbstractConcurrentMapSegmentTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/ListHashMapTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/ListHashMapTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/ManagedConcurrentMapTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/ManagedConcurrentMapTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/ManagedConcurrentValueMapTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/ManagedConcurrentValueMapTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/ManagedLinkedlistTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/ManagedLinkedlistTest.groovy", [])
    }

    void "test src/test/org/codehaus/groovy/util/ReferenceManagerTest.groovy"() {
        unzipAndTest("src/test/org/codehaus/groovy/util/ReferenceManagerTest.groovy", [])
    }

    void "test subprojects/groovy-ant/src/main/groovy/groovy/util/FileNameFinder.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/main/groovy/groovy/util/FileNameFinder.groovy", [])
    }

    void "test subprojects/groovy-ant/src/spec/test/builder/AntBuilderSpecTest.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/spec/test/builder/AntBuilderSpecTest.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest1.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest1.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest2.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest2.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest_errorMessage.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovyTest_errorMessage.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovycTest1.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/GroovycTest1.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/groovytest3/GroovyTest3Class.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test-resources/org/codehaus/groovy/ant/groovytest3/GroovyTest3Class.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test/groovy/groovy/util/AntTest.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test/groovy/groovy/util/AntTest.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test/groovy/groovy/util/FileNameFinderTest.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test/groovy/groovy/util/FileNameFinderTest.groovy", [])
    }

    void "test subprojects/groovy-ant/src/test/groovy/org/codehaus/groovy/ant/GroovyTest2Class.groovy"() {
        unzipAndTest("subprojects/groovy-ant/src/test/groovy/org/codehaus/groovy/ant/GroovyTest2Class.groovy", [])
    }

    void "test subprojects/groovy-bsf/src/test/resources/groovy/script/MapFromList.groovy"() {
        unzipAndTest("subprojects/groovy-bsf/src/test/resources/groovy/script/MapFromList.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/TextNode.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/TextNode.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/TextTreeNodeMaker.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/TextTreeNodeMaker.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/AstBrowser.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/AstBrowser.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/AstNodeToScriptAdapter.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/AstNodeToScriptAdapter.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ButtonOrDefaultRenderer.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ButtonOrDefaultRenderer.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ButtonOrTextEditor.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ButtonOrTextEditor.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ObjectBrowser.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ObjectBrowser.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ScriptToTreeNodeAdapter.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/ScriptToTreeNodeAdapter.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/Console.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/Console.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleActions.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleActions.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleApplet.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleApplet.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleIvyPlugin.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleIvyPlugin.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleView.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleView.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/HistoryRecord.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/HistoryRecord.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/OutputTransforms.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/OutputTransforms.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/text/AutoIndentAction.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/text/AutoIndentAction.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicContentPane.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicContentPane.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicMenuBar.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicMenuBar.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicStatusBar.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicStatusBar.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicToolBar.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/BasicToolBar.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/Defaults.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/Defaults.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/GTKDefaults.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/GTKDefaults.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/MacOSXDefaults.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/MacOSXDefaults.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/MacOSXMenuBar.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/MacOSXMenuBar.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/groovy/groovy/ui/view/WindowsDefaults.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/groovy/groovy/ui/view/WindowsDefaults.groovy", [])
    }

    void "test subprojects/groovy-console/src/main/resources/groovy/inspect/swingui/AstBrowserProperties.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/main/resources/groovy/inspect/swingui/AstBrowserProperties.groovy", [])
    }

    void "test subprojects/groovy-console/src/test/groovy/groovy/inspect/swingui/AstNodeToScriptAdapterTest.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/test/groovy/groovy/inspect/swingui/AstNodeToScriptAdapterTest.groovy", [])
    }

    void "test subprojects/groovy-console/src/test/groovy/groovy/inspect/swingui/ScriptToTreeNodeAdapterTest.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/test/groovy/groovy/inspect/swingui/ScriptToTreeNodeAdapterTest.groovy", [])
    }

    void "test subprojects/groovy-console/src/test/groovy/groovy/swing/SwingBuilderConsoleTest.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/test/groovy/groovy/swing/SwingBuilderConsoleTest.groovy", [])
    }

    void "test subprojects/groovy-console/src/test/groovy/groovy/ui/HistoryRecordGetTextToRunTests.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/test/groovy/groovy/ui/HistoryRecordGetTextToRunTests.groovy", [])
    }

    void "test subprojects/groovy-console/src/test/groovy/groovy/ui/text/GroovyFilterTests.groovy"() {
        unzipAndTest("subprojects/groovy-console/src/test/groovy/groovy/ui/text/GroovyFilterTests.groovy", [])
    }

    void "test subprojects/groovy-docgenerator/src/main/groovy/org/codehaus/groovy/tools/DocGenerator.groovy"() {
        unzipAndTest("subprojects/groovy-docgenerator/src/main/groovy/org/codehaus/groovy/tools/DocGenerator.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/main/groovy/org/codehaus/groovy/tools/groovydoc/Main.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/main/groovy/org/codehaus/groovy/tools/groovydoc/Main.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/SimpleGroovyClassDocTests.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/SimpleGroovyClassDocTests.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDocTests.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDocTests.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/Alias.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/Alias.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DeprecatedClass.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DeprecatedClass.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DeprecatedField.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DeprecatedField.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DocumentedClass.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/DocumentedClass.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/EnumWithDeprecatedConstants.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/EnumWithDeprecatedConstants.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/ExampleVisibilityG.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/ExampleVisibilityG.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GeneratePropertyFromGetSet.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GeneratePropertyFromGetSet.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/InnerEnum.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/InnerEnum.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/Script.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/Script.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/TestConstructors.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/TestConstructors.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/Base.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/Base.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantA.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantA.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantC.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantC.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantD.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantD.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/b/Base.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/b/Base.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/b/DescendantB.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/b/DescendantB.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantE.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantE.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantF.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantF.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/GroovyWithFailingStaticInit.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/GroovyWithFailingStaticInit.groovy", [])
    }

    void "test subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.groovy"() {
        unzipAndTest("subprojects/groovy-groovydoc/src/test/groovy/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/AnsiDetector.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/AnsiDetector.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/BufferManager.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/BufferManager.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Command.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Command.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandAlias.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandAlias.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandException.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandException.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandRegistry.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandRegistry.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/CommandSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ComplexCommandSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ComplexCommandSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ExitNotification.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ExitNotification.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Groovysh.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Groovysh.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/InteractiveShellRunner.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/InteractiveShellRunner.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Interpreter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Interpreter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Main.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Main.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Parser.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Parser.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Shell.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/Shell.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ShellRunner.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/ShellRunner.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/AliasCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/AliasCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ClearCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ClearCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/DisplayCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/DisplayCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/DocCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/DocCommand.groovy", [], ['for \'java.awt.Desktop\'.': 'for "java.awt.Desktop".'])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/EditCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/EditCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ExitCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ExitCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/GrabCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/GrabCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/HelpCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/HelpCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/HistoryCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/HistoryCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ImportCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ImportCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/InspectCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/InspectCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/LoadCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/LoadCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/PurgeCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/PurgeCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/RecordCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/RecordCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/RegisterCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/RegisterCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/SaveCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/SaveCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/SetCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/SetCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ShadowCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ShadowCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ShowCommand.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/commands/ShowCommand.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/CommandNameCompleter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/CommandNameCompleter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/CustomClassSyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/CustomClassSyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/FileNameCompleter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/FileNameCompleter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/GroovySyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/GroovySyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/IdentifierCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/IdentifierCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ImportsSyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ImportsSyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/InfixKeywordSyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/InfixKeywordSyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/KeywordSyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/KeywordSyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/NavigablePropertiesCompleter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/NavigablePropertiesCompleter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletionCandidate.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletionCandidate.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/StricterArgumentCompleter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/StricterArgumentCompleter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/VariableSyntaxCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/completion/VariableSyntaxCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/CommandArgumentParser.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/CommandArgumentParser.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/CurlyCountingGroovyLexer.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/CurlyCountingGroovyLexer.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/DefaultCommandsRegistrar.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/DefaultCommandsRegistrar.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/HelpFormatter.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/HelpFormatter.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/NoExitSecurityManager.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/NoExitSecurityManager.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/PackageHelper.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/PackageHelper.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/PackageHelperImpl.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/PackageHelperImpl.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/ScriptVariableAnalyzer.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/ScriptVariableAnalyzer.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/SimpleCompletor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/SimpleCompletor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/WrappedInputStream.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/WrappedInputStream.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/XmlCommandRegistrar.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/main/groovy/org/codehaus/groovy/tools/shell/util/XmlCommandRegistrar.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/AllCompletorsTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/AllCompletorsTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/AnsiDetectorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/AnsiDetectorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/CommandCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/CommandCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/CompletorTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/CompletorTestSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ErrorDisplayTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ErrorDisplayTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/GroovyshParsersTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/GroovyshParsersTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/GroovyshTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/GroovyshTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ImportCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ImportCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellRunnerTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellRunnerTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellRunnerTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellRunnerTestSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/ShellTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/AliasCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/AliasCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ClearCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ClearCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/CommandTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/CommandTestSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ComplexCommandSupportTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ComplexCommandSupportTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/DisplayCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/DisplayCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/DocCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/DocCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/EditCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/EditCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ExitCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ExitCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/GrabCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/GrabCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/HelpCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/HelpCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/HistoryCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/HistoryCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ImportCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ImportCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/InspectCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/InspectCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/LoadCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/LoadCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/PurgeCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/PurgeCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/RecordCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/RecordCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/RegisterCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/RegisterCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/SaveCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/SaveCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/SetCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/SetCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ShowCommandTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/commands/ShowCommandTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/CustomClassCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/CustomClassCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/FileNameCompleterTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/FileNameCompleterTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/GroovySyntaxCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/GroovySyntaxCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/ImportsSyntaxCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/ImportsSyntaxCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/KeywordCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/KeywordCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/NavigablePropertiesCompleterTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/NavigablePropertiesCompleterTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/ReflectionCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/TokenUtilTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/TokenUtilTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/VariableCompletorTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/completion/VariableCompletorTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/ClassWithPrivateConstructor.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/ClassWithPrivateConstructor.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/ExprTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/ExprTestSupport.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/TimeItTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/expr/TimeItTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/CommandArgumentParserTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/CommandArgumentParserTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/CurlyCountingGroovyLexerTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/CurlyCountingGroovyLexerTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/MessageSourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/MessageSourceTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/PackageHelperImplTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/PackageHelperImplTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/ScriptVariableAnalyzerTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/ScriptVariableAnalyzerTest.groovy", [])
    }

    void "test subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/WrappedInputStreamTest.groovy"() {
        unzipAndTest("subprojects/groovy-groovysh/src/test/groovy/org/codehaus/groovy/tools/shell/util/WrappedInputStreamTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxAttributeInfoManager.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxAttributeInfoManager.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanExportFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanExportFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanInfoManager.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeanInfoManager.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeansFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBeansFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBuilder.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBuilderTools.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxBuilderTools.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxClientConnectorFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxClientConnectorFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxEmitterFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxEmitterFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxListenerFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxListenerFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxMetaMapBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxMetaMapBuilder.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxOperationInfoManager.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxOperationInfoManager.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxServerConnectorFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxServerConnectorFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxTimerFactory.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/JmxTimerFactory.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/package-info.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/main/groovy/groovy/jmx/builder/package-info.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/spec/test/JmxTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/spec/test/JmxTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxAttributeInfoManagerTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxAttributeInfoManagerTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanExportFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanExportFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanInfoManagerTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeanInfoManagerTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeansFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBeansFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBuilderToolsTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxBuilderToolsTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxClientConnectorFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxClientConnectorFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxEmbeddedMetaMapBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxEmbeddedMetaMapBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxEmitterFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxEmitterFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxListenerFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxListenerFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxMetaMapBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxMetaMapBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxOperationInfoManagerTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxOperationInfoManagerTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxServerConnectorFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxServerConnectorFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxTimerFactoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/JmxTimerFactoryTest.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/MockEmbeddedClass.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/MockEmbeddedClass.groovy", [])
    }

    void "test subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/MockManagedGroovyObject.groovy"() {
        unzipAndTest("subprojects/groovy-jmx/src/test/groovy/groovy/jmx/builder/MockManagedGroovyObject.groovy", [])
    }

    void "test subprojects/groovy-json/src/spec/test/json/JsonBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/spec/test/json/JsonBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/spec/test/json/JsonTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/spec/test/json/JsonTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/spec/test/json/StreamingJsonBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/spec/test/json/StreamingJsonBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/CharBufTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/CharBufTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/IOTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/IOTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonJavadocAssertionTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonJavadocAssertionTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonLexerTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonLexerTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonOutputTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonOutputTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperCharSourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperCharSourceTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperIndexOverlayTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperIndexOverlayTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperLaxTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperLaxTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonSlurperTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonTokenTypeTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonTokenTypeTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/JsonTokenValueTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/JsonTokenValueTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/RealJsonPayloadsTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/RealJsonPayloadsTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/StreamingJsonBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/StreamingJsonBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/ArrayUtilsTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/ArrayUtilsTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/CharScannerTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/CharScannerTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/ChrTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/ChrTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/DatesTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/DatesTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/FastStringUtilsTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/FastStringUtilsTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/FastStringUtilsUnsafeDisabledTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/FastStringUtilsUnsafeDisabledTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/LazyMapTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/LazyMapTest.groovy", [])
    }

    void "test subprojects/groovy-json/src/test/groovy/groovy/json/internal/ReaderCharacterSourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-json/src/test/groovy/groovy/json/internal/ReaderCharacterSourceTest.groovy", [])
    }

    void "test subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/JSR223Test.groovy"() {
        unzipAndTest("subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/JSR223Test.groovy", [])
    }

    void "test subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/SugarTest.groovy"() {
        unzipAndTest("subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/SugarTest.groovy", [])
    }

    void "test subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/vm6/JavascriptTest.groovy"() {
        unzipAndTest("subprojects/groovy-jsr223/src/test/groovy/org/codehaus/groovy/jsr223/vm6/JavascriptTest.groovy", [])
    }

    void "test subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/ASTMatcher.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/ASTMatcher.groovy", [])
    }

    void "test subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/MatchingConstraints.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/MatchingConstraints.groovy", [])
    }

    void "test subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/AnyTokenMatch.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/AnyTokenMatch.groovy", [])
    }

    void "test subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/ConstraintPredicate.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/ConstraintPredicate.groovy", [])
    }

    void "test subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/MatchingConstraintsBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/main/groovy/org/codehaus/groovy/macro/matcher/internal/MatchingConstraintsBuilder.groovy", [])
    }

    void "test subprojects/groovy-macro/src/test/groovy/org/codehaus/groovy/macro/MacroTest.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/test/groovy/org/codehaus/groovy/macro/MacroTest.groovy", [])
    }

    void "test subprojects/groovy-macro/src/test/groovy/org/codehaus/groovy/macro/matcher/ASTMatcherTest.groovy"() {
        unzipAndTest("subprojects/groovy-macro/src/test/groovy/org/codehaus/groovy/macro/matcher/ASTMatcherTest.groovy", [])
    }

    void "test subprojects/groovy-nio/src/test/groovy/org/codehaus/groovy/runtime/NioGroovyMethodsTest.groovy"() {
        unzipAndTest("subprojects/groovy-nio/src/test/groovy/org/codehaus/groovy/runtime/NioGroovyMethodsTest.groovy", [])
    }

    void "test subprojects/groovy-servlet/src/spec/test/servlet/GroovyServletTest.groovy"() {
        unzipAndTest("subprojects/groovy-servlet/src/spec/test/servlet/GroovyServletTest.groovy", [])
    }

    void "test subprojects/groovy-servlet/src/test/groovy/groovy/servlet/AbstractHttpServletTest.groovy"() {
        unzipAndTest("subprojects/groovy-servlet/src/test/groovy/groovy/servlet/AbstractHttpServletTest.groovy", [])
    }

    void "test subprojects/groovy-servlet/src/test/groovy/groovy/servlet/ServletBindingTest.groovy"() {
        unzipAndTest("subprojects/groovy-servlet/src/test/groovy/groovy/servlet/ServletBindingTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/spec/test/SqlTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/spec/test/SqlTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/bugs/ForAndSqlBug.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/bugs/ForAndSqlBug.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/bugs/Groovy5041Bug.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/bugs/Groovy5041Bug.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/ExtractIndexAndSqlTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/ExtractIndexAndSqlTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/GroovyRowResultTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/GroovyRowResultTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/Person.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/Person.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/PersonTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/PersonTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlBatchTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlBatchTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCacheTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCacheTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCallTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCallTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCompleteTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCompleteTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCompleteWithoutDataSourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlCompleteWithoutDataSourceTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlHelperTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlHelperTestCase.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlRowsTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlRowsTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlStatementTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlStatementTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTestConstants.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTestConstants.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionConnectionTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionConnectionTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionDataSourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionDataSourceTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlTransactionTestCase.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlWithBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlWithBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlWithTypedResultsTest.groovy"() {
        unzipAndTest("subprojects/groovy-sql/src/test/groovy/groovy/sql/SqlWithTypedResultsTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/LookAndFeelHelper.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/LookAndFeelHelper.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/SwingBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/SwingBuilder.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/AbstractSyntheticMetaMethods.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/AbstractSyntheticMetaMethods.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JComboBoxMetaMethods.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JComboBoxMetaMethods.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JListMetaMethods.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JListMetaMethods.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JListProperties.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JListProperties.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JTableMetaMethods.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/binding/JTableMetaMethods.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ActionFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ActionFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BeanFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BeanFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BevelBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BevelBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindGroupFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindGroupFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindProxyFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BindProxyFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BoxFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BoxFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BoxLayoutFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/BoxLayoutFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ButtonGroupFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ButtonGroupFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CellEditorFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CellEditorFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CollectionFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CollectionFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ColumnFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ColumnFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ColumnModelFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ColumnModelFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ComboBoxFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ComboBoxFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ComponentFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ComponentFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CompoundBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/CompoundBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/DialogFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/DialogFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/EmptyBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/EmptyBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/EtchedBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/EtchedBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/FormattedTextFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/FormattedTextFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/FrameFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/FrameFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/GridBagFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/GridBagFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ImageIconFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ImageIconFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/InternalFrameFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/InternalFrameFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/LayoutFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/LayoutFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/LineBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/LineBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ListFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ListFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/MapFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/MapFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/MatteBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/MatteBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RendererFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RendererFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RichActionWidgetFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RichActionWidgetFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RootPaneContainerFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/RootPaneContainerFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ScrollPaneFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/ScrollPaneFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SeparatorFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SeparatorFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SplitPaneFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SplitPaneFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SwingBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/SwingBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TabbedPaneFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TabbedPaneFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableLayoutFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableLayoutFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableModelFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TableModelFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TextArgWidgetFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TextArgWidgetFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TitledBorderFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/TitledBorderFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/WidgetFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/WidgetFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/WindowFactory.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/factory/WindowFactory.groovy", [])
    }

    void "test subprojects/groovy-swing/src/main/groovy/groovy/swing/impl/ClosureCellEditor.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/main/groovy/groovy/swing/impl/ClosureCellEditor.groovy", [])
    }

    void "test subprojects/groovy-swing/src/spec/test/SwingBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/spec/test/SwingBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/ClosureSwingListenerTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/ClosureSwingListenerTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/beans/BindableSwingTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/beans/BindableSwingTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/beans/VetoableSwingTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/beans/VetoableSwingTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/bugs/Groovy303_Bug.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/bugs/Groovy303_Bug.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/bugs/PropertyBug.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/bugs/PropertyBug.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/model/TableModelTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/model/TableModelTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/BindPathTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/BindPathTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderBindingsTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderBindingsTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderTableTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderTableTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingMetaMethodsTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/SwingMetaMethodsTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/swing/TitledBorderFactoryJustificationTest.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/swing/TitledBorderFactoryJustificationTest.groovy", [])
    }

    void "test subprojects/groovy-swing/src/test/groovy/groovy/util/GroovySwingTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-swing/src/test/groovy/groovy/util/GroovySwingTestCase.groovy", [])
    }

    void "test subprojects/groovy-templates/src/main/groovy/groovy/text/markup/MarkupTemplateTypeCheckingExtension.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/main/groovy/groovy/text/markup/MarkupTemplateTypeCheckingExtension.groovy", [])
    }

    void "test subprojects/groovy-templates/src/main/groovy/groovy/text/markup/TagLibAdapter.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/main/groovy/groovy/text/markup/TagLibAdapter.groovy", [])
    }

    void "test subprojects/groovy-templates/src/spec/test/MarkupTemplateEngineSpecTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/spec/test/MarkupTemplateEngineSpecTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/spec/test/MyTemplate.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/spec/test/MyTemplate.groovy", [])
    }

    void "test subprojects/groovy-templates/src/spec/test/TemplateEnginesTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/spec/test/TemplateEnginesTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/SimpleGStringTemplateEngineTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/SimpleGStringTemplateEngineTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/SimpleTemplateEngineTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/SimpleTemplateEngineTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/text/MarkupTemplateEngineTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/text/MarkupTemplateEngineTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/text/SimpleTemplateTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/text/SimpleTemplateTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/text/StreamingTemplateEngineSpecification.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/text/StreamingTemplateEngineSpecification.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/text/StreamingTemplateEngineTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/text/StreamingTemplateEngineTest.groovy", [])
    }

    void "test subprojects/groovy-templates/src/test/groovy/groovy/text/markup/TemplateResourceTest.groovy"() {
        unzipAndTest("subprojects/groovy-templates/src/test/groovy/groovy/text/markup/TemplateResourceTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/lang/GroovyLogTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/lang/GroovyLogTestCase.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/Demand.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/Demand.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/Ignore.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/Ignore.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/LooseExpectation.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/LooseExpectation.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/MockFor.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/MockFor.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/MockInterceptor.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/MockInterceptor.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/StrictExpectation.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/StrictExpectation.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/StubFor.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/mock/interceptor/StubFor.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/util/GroovyShellTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/util/GroovyShellTestCase.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/util/JavadocAssertionTestBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/util/JavadocAssertionTestBuilder.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/util/JavadocAssertionTestSuite.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/util/JavadocAssertionTestSuite.groovy", [])
    }

    void "test subprojects/groovy-test/src/main/groovy/groovy/util/StringTestUtil.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/main/groovy/groovy/util/StringTestUtil.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/GroovyTestCaseTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/GroovyTestCaseTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/groovy/lang/GroovyLogTestCaseTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/groovy/lang/GroovyLogTestCaseTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/groovy/test/GroovyAssertTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/groovy/test/GroovyAssertTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/groovy/test/GroovyTestJavadocAssertionTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/groovy/test/GroovyTestJavadocAssertionTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/groovy/util/AllTestSuiteTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/groovy/util/AllTestSuiteTest.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/groovy/util/suite/ATestScriptThatsNoTestCase.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/groovy/util/suite/ATestScriptThatsNoTestCase.groovy", [])
    }

    void "test subprojects/groovy-test/src/test/groovy/org/codehaus/groovy/transform/NotYetImplementedTransformTest.groovy"() {
        unzipAndTest("subprojects/groovy-test/src/test/groovy/org/codehaus/groovy/transform/NotYetImplementedTransformTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/Entity.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/Entity.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/StaxBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/StaxBuilder.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingDOMBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingDOMBuilder.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingMarkupBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingMarkupBuilder.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingSAXBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/StreamingSAXBuilder.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/jaxb/JaxbGroovyMethods.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/jaxb/JaxbGroovyMethods.groovy", [])
    }

    void "test subprojects/groovy-xml/src/main/groovy/groovy/xml/streamingmarkupsupport/AbstractStreamingBuilder.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/main/groovy/groovy/xml/streamingmarkupsupport/AbstractStreamingBuilder.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/DOMBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/DOMBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/SaxBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/SaxBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/StaxBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/StaxBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideDOMCategory.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideDOMCategory.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideMarkupBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideMarkupBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideStreamingMarkupBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideStreamingMarkupBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideXmlParserTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideXmlParserTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideXmlSlurperTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideXmlSlurperTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/spec/test/UserGuideXmlUtilTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/spec/test/UserGuideXmlUtilTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy249_Bug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy249_Bug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy4285Bug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy4285Bug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy593_Bug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy593_Bug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy_2473Bug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/Groovy_2473Bug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/StaticMarkupBug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/StaticMarkupBug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/bugs/TedsClosureBug.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/bugs/TedsClosureBug.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/util/XmlNodePrinterTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/util/XmlNodePrinterTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/util/XmlParserTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/util/XmlParserTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/util/XmlSlurperTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/util/XmlSlurperTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/BuilderTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/BuilderTestSupport.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/DOMTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/DOMTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/GpathSyntaxTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/GpathSyntaxTestSupport.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/MarkupBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/MarkupBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/MarkupWithWriterTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/MarkupWithWriterTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/MixedMarkupTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/MixedMarkupTestSupport.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/NamespaceNodeGPathTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/NamespaceNodeGPathTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/NamespaceNodeTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/NamespaceNodeTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/SAXTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/SAXTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/StreamingMarkupBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/StreamingMarkupBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/StreamingSAXBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/StreamingSAXBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/TraversalTestSupport.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/TraversalTestSupport.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/UseMarkupWithWriterScript.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/UseMarkupWithWriterScript.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/VerboseDOMTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/VerboseDOMTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/XmlJavadocAssertionTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/XmlJavadocAssertionTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/XmlUtilTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/XmlUtilTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/DOMCategoryTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/DOMCategoryTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/DOMTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/DOMTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/NamespaceDOMTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/dom/NamespaceDOMTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/jaxb/JaxbGroovyMethodsTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/jaxb/JaxbGroovyMethodsTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/jaxb/Person.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/jaxb/Person.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/groovy/xml/vm6/StaxBuilderTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/groovy/xml/vm6/StaxBuilderTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/org/codehaus/groovy/benchmarks/BuilderPerfTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/org/codehaus/groovy/benchmarks/BuilderPerfTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/org/codehaus/groovy/tools/xml/DomToGroovyTest.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/org/codehaus/groovy/tools/xml/DomToGroovyTest.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/script/AtomTestScript.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/script/AtomTestScript.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/script/MarkupTestScript.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/script/MarkupTestScript.groovy", [])
    }

    void "test subprojects/groovy-xml/src/test/groovy/util/NavToWiki.groovy"() {
        unzipAndTest("subprojects/groovy-xml/src/test/groovy/util/NavToWiki.groovy", [])
    }


    /*************************************/
    static unzipAndTest(String entryName, List ignoreClazzList, Map<String, String> replacementsMap=[:]) {
        ignoreClazzList.addAll(TestUtils.COMMON_IGNORE_CLASS_LIST)

        TestUtils.unzipAndTest(ZIP_PATH, entryName, TestUtils.addIgnore(ignoreClazzList, ASTComparatorCategory.LOCATION_IGNORE_LIST), replacementsMap)
    }

    /*
    static unzipAndTest(String entryName) {
        TestUtils.unzipAndTest(ZIP_PATH, entryName);
    }
    */

    public static final String ZIP_PATH = "$TestUtils.RESOURCES_PATH/groovy-2.5.0/groovy-2.5.0-SNAPSHOT-20160921-allsources.zip";

}
