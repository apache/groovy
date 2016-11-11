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
 * Add Spock 1.1 RC-2 sources as test cases
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/10/12
 */
class Spock11RC2SourcesTest extends GroovyTestCase {


    void "test build.gradle"() {
        unzipAndTest("build.gradle", [])
    }

    void "test buildSrc/build.gradle"() {
        unzipAndTest("buildSrc/build.gradle", [])
    }

    void "test gradle/common.gradle"() {
        unzipAndTest("gradle/common.gradle", [])
    }

    void "test gradle/ide.gradle"() {
        unzipAndTest("gradle/ide.gradle", [])
    }

    void "test gradle/publishMaven.gradle"() {
        unzipAndTest("gradle/publishMaven.gradle", [])
    }

    void "test settings.gradle"() {
        unzipAndTest("settings.gradle", [])
    }

    void "test spock-core/core.gradle"() {
        unzipAndTest("spock-core/core.gradle", [])
    }

    void "test spock-core/src/main/groovy/org/spockframework/util/GroovyUtil.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/org/spockframework/util/GroovyUtil.groovy", [])
    }

    void "test spock-core/src/main/groovy/spock/util/EmbeddedSpecCompiler.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/spock/util/EmbeddedSpecCompiler.groovy", [])
    }

    void "test spock-core/src/main/groovy/spock/util/EmbeddedSpecRunner.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/spock/util/EmbeddedSpecRunner.groovy", [])
    }

    void "test spock-core/src/main/groovy/spock/util/concurrent/BlockingVariables.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/spock/util/concurrent/BlockingVariables.groovy", [])
    }

    void "test spock-core/src/main/groovy/spock/util/matcher/HamcrestMatchers.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/spock/util/matcher/HamcrestMatchers.groovy", [])
    }

    void "test spock-core/src/main/groovy/spock/util/matcher/IsCloseTo.groovy"() {
        unzipAndTest("spock-core/src/main/groovy/spock/util/matcher/IsCloseTo.groovy", [])
    }

    void "test spock-gradle/gradle.gradle"() {
        unzipAndTest("spock-gradle/gradle.gradle", [])
    }

    void "test spock-gradle/src/main/groovy/org/spockframework/gradle/GenerateSpockReport.groovy"() {
        unzipAndTest("spock-gradle/src/main/groovy/org/spockframework/gradle/GenerateSpockReport.groovy", [])
    }

    void "test spock-gradle/src/main/groovy/org/spockframework/gradle/SpockBasePlugin.groovy"() {
        unzipAndTest("spock-gradle/src/main/groovy/org/spockframework/gradle/SpockBasePlugin.groovy", [])
    }

    void "test spock-gradle/src/main/groovy/org/spockframework/gradle/SpockReportPlugin.groovy"() {
        unzipAndTest("spock-gradle/src/main/groovy/org/spockframework/gradle/SpockReportPlugin.groovy", [])
    }

    void "test spock-guice/guice.gradle"() {
        unzipAndTest("spock-guice/guice.gradle", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/BindingAnnotation1.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/BindingAnnotation1.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/BindingAnnotation2.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/BindingAnnotation2.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/GuiceSpecInheritance.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/GuiceSpecInheritance.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/IService1.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/IService1.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/IService2.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/IService2.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/InjectionExamples.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/InjectionExamples.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/Module1.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/Module1.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/Module2.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/Module2.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/Service1.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/Service1.groovy", [])
    }

    void "test spock-guice/src/test/groovy/org/spockframework/guice/Service2.groovy"() {
        unzipAndTest("spock-guice/src/test/groovy/org/spockframework/guice/Service2.groovy", [])
    }

    void "test spock-report/SpockSampleTestConfig.groovy"() {
        unzipAndTest("spock-report/SpockSampleTestConfig.groovy", [])
    }

    void "test spock-report/report.gradle"() {
        unzipAndTest("spock-report/report.gradle", [])
    }

    void "test spock-report/src/main/groovy/org/spockframework/report/Assets.groovy"() {
        unzipAndTest("spock-report/src/main/groovy/org/spockframework/report/Assets.groovy", [])
    }

    void "test spock-report/src/main/groovy/org/spockframework/report/HtmlReportGenerator.groovy"() {
        unzipAndTest("spock-report/src/main/groovy/org/spockframework/report/HtmlReportGenerator.groovy", [])
    }

    void "test spock-report/src/test/groovy/org/spockframework/report/HtmlReportGeneratorSpec.groovy"() {
        unzipAndTest("spock-report/src/test/groovy/org/spockframework/report/HtmlReportGeneratorSpec.groovy", [])
    }

    void "test spock-report/src/test/groovy/org/spockframework/report/sample/FightOrFlightSpec.groovy"() {
        unzipAndTest("spock-report/src/test/groovy/org/spockframework/report/sample/FightOrFlightSpec.groovy", [])
    }

    void "test spock-report/src/test/groovy/org/spockframework/report/sample/FightOrFlightStory.groovy"() {
        unzipAndTest("spock-report/src/test/groovy/org/spockframework/report/sample/FightOrFlightStory.groovy", [])
    }

    void "test spock-specs/SpockTestConfig.groovy"() {
        unzipAndTest("spock-specs/SpockTestConfig.groovy", [])
    }

    void "test spock-specs/specs.gradle"() {
        unzipAndTest("spock-specs/specs.gradle", [])
    }

    void "test spock-specs/src/test.java1.8/groovy/org/spockframework/smoke/mock/PartialMockingInterfacesWithDefaultMethods.groovy"() {
        unzipAndTest("spock-specs/src/test.java1.8/groovy/org/spockframework/smoke/mock/PartialMockingInterfacesWithDefaultMethods.groovy", [])
    }

    void "test spock-specs/src/test2.4/groovy/org/spockframework/smoke/traits/BasicTraitUsage.groovy"() {
        unzipAndTest("spock-specs/src/test2.4/groovy/org/spockframework/smoke/traits/BasicTraitUsage.groovy", [])
    }

    void "test spock-specs/src/test2.4/groovy/org/spockframework/smoke/traits/MyTrait.groovy"() {
        unzipAndTest("spock-specs/src/test2.4/groovy/org/spockframework/smoke/traits/MyTrait.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/EmbeddedSpecification.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/EmbeddedSpecification.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/ExecutionLog.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/ExecutionLog.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/VerifyExecution.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/VerifyExecution.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/VerifyExecutionExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/VerifyExecutionExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/builder/PojoBuilderSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/builder/PojoBuilderSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/buildsupport/SpecClassFileFinderSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/buildsupport/SpecClassFileFinderSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/example/FeatureUnrolling.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/example/FeatureUnrolling.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/gentyref/GenericTypeReflectorSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/gentyref/GenericTypeReflectorSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/AssertStatementSourcePositionTest.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/AssertStatementSourcePositionTest.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/AstInspectorTest.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/AstInspectorTest.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/DGMMatcherIterator.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/DGMMatcherIterator.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/FieldInitializers.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/FieldInitializers.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/GroovyMopExploration.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/GroovyMopExploration.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/GroovyVarArgs.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/GroovyVarArgs.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/PackageNames.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/PackageNames.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/ReturnStatementSourcePositionTest.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/ReturnStatementSourcePositionTest.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/SourcePositionPhaseConversion.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/SourcePositionPhaseConversion.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/SourcePositionPhaseSemanticAnalysis.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/SourcePositionPhaseSemanticAnalysis.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/groovy/VarArgsSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/groovy/VarArgsSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/idea/IntelliJIdeaSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/idea/IntelliJIdeaSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/junit/DescriptionOfDerivedTestClass.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/junit/DescriptionOfDerivedTestClass.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/junit/JUnitErrorBehavior.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/junit/JUnitErrorBehavior.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/junit/JUnitRuleBehavior.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/junit/JUnitRuleBehavior.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/junit/ObservableRunner.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/junit/ObservableRunner.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/DetachedMockFactorySpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/DetachedMockFactorySpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/DetachedMockSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/DetachedMockSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/MockDetectorSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/MockDetectorSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/response/IterableResponseGeneratorSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/response/IterableResponseGeneratorSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/runtime/JavaMockFactorySpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/runtime/JavaMockFactorySpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/mock/runtime/MockConfigurationSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/mock/runtime/MockConfigurationSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogConfigurationSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogConfigurationSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogEmitterSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogEmitterSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogMergerSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/report/log/ReportLogMergerSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/AsyncRunListenerSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/AsyncRunListenerSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/ClosingOfDataProviders.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/ClosingOfDataProviders.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/ConfigurationScriptLoaderSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/ConfigurationScriptLoaderSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/EstimatedNumberOfIterations.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/EstimatedNumberOfIterations.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/GlobalExtensionRegistrySpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/GlobalExtensionRegistrySpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/GroovyMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/GroovyMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/GroovyRuntimeUtilIsVoidMethodSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/GroovyRuntimeUtilIsVoidMethodSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/GroovyRuntimeUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/GroovyRuntimeUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/JUnitDescriptionGeneratorSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/JUnitDescriptionGeneratorSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/RunContextSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/RunContextSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/SafeIterationNameProviderSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/SafeIterationNameProviderSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/SpecUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/SpecUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/SputnikSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/SputnikSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/StandardStreamsCapturerSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/StandardStreamsCapturerSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/condition/EditDistanceSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/condition/EditDistanceSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/condition/EditPathRendererSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/condition/EditPathRendererSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/runtime/extension/builtin/UnrollNameProviderSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/runtime/extension/builtin/UnrollNameProviderSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/AccessingOldValues.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/AccessingOldValues.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/AssertionErrorMessages.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/AssertionErrorMessages.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/Blocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/Blocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/BuiltInMembersOfClassSpecification.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/BuiltInMembersOfClassSpecification.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/CleanupBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/CleanupBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/CompileTimeErrorReporting.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/CompileTimeErrorReporting.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/ExpectBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/ExpectBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/FeatureFiltering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/FeatureFiltering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/FeatureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/FeatureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/FeatureSorting.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/FeatureSorting.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/FixtureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/FixtureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/GroovyCallChain.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/GroovyCallChain.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/InteractionsAndExceptionConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/InteractionsAndExceptionConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/MethodAccessibility.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/MethodAccessibility.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/MethodExecutionOrder.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/MethodExecutionOrder.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/MisspelledFixtureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/MisspelledFixtureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/MixingExpectAndWhenThenBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/MixingExpectAndWhenThenBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SetupBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SetupBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SharedFields.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SharedFields.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SharedFieldsInSuperclass.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SharedFieldsInSuperclass.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SharedVsStaticFields.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SharedVsStaticFields.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SpecFields.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SpecFields.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SpecInheritance.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SpecInheritance.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SpecRecognition.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SpecRecognition.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/SpecWithoutFeatures.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/SpecWithoutFeatures.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/StackTraceFiltering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/StackTraceFiltering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/StaticMethodsInSpecifications.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/StaticMethodsInSpecifications.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/StaticTypeChecking.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/StaticTypeChecking.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/VoidGroovyStaticMethod.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/VoidGroovyStaticMethod.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/VoidMethodCallsInExpectBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/VoidMethodCallsInExpectBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/VoidMethodCallsInThenBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/VoidMethodCallsInThenBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/WhenThenBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/WhenThenBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/WithBlockFailingConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/WithBlockFailingConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/WithBlockPassingConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/WithBlockPassingConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/WithBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/WithBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionEvaluation.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionEvaluation.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionNotSatisfiedErrors.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionNotSatisfiedErrors.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionRenderingSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionRenderingSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionsAndGroovyTruth.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionsAndGroovyTruth.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/DiffedObjectRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/DiffedObjectRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/EqualityComparisonRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/EqualityComparisonRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExceptionConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExceptionConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExceptionsInConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExceptionsInConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFeatureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFeatureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFields.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFields.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFixtureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInFixtureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInHelperMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInHelperMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInNestedPositions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsInNestedPositions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsWithMessage.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExplicitConditionsWithMessage.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ImplicitClosureCallRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ImplicitClosureCallRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/InvalidConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/InvalidConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/IsRenderedExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/IsRenderedExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/MatcherConditionRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/MatcherConditionRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/MatcherConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/MatcherConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/MethodConditionEvaluation.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/MethodConditionEvaluation.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/NegativeExceptionConditions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/NegativeExceptionConditions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/PartialConditionEvaluation.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/PartialConditionEvaluation.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/StringComparisonRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/StringComparisonRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/condition/ValueRendering.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/condition/ValueRendering.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/AutoCleanupExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/AutoCleanupExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/ConditionallyIgnoreFeature.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/ConditionallyIgnoreFeature.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/FailsWithExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/FailsWithExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/Fast.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/Fast.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreIfExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreIfExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreRestExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IgnoreRestExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeFeatures.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeFeatures.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeFeaturesWithInheritance.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeFeaturesWithInheritance.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecs.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecs.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecsAndFeatures.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecsAndFeatures.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecsWithInheritance.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IncludeExcludeSpecsWithInheritance.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/IssueExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/IssueExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/NarrativeExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/NarrativeExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/PendingFeatureExtensionSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/PendingFeatureExtensionSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/ReportLogExtensionSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/ReportLogExtensionSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/RequiresExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/RequiresExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/RestoreSystemPropertiesExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/RestoreSystemPropertiesExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/SeeExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/SeeExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/Slow.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/Slow.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/StepwiseExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/StepwiseExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/TimeoutExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/TimeoutExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/extension/TitleExtension.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/extension/TitleExtension.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/groovy/UsageOfNotYetImplemented.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/groovy/UsageOfNotYetImplemented.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/HandlingOfAssumptionViolatedException.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/HandlingOfAssumptionViolatedException.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitClassRules.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitClassRules.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitCompliance.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitCompliance.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitComplianceIgnoredTestClass.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitComplianceIgnoredTestClass.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitFixtureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitFixtureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitMethodRuleOrder.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitMethodRuleOrder.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitRules.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitRules.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitTestRuleOrder.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/JUnitTestRuleOrder.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/RulesAndInheritance.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/RulesAndInheritance.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitClassRule.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitClassRule.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitTestNameRule.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitTestNameRule.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitTimeoutRule.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/junit/UseJUnitTimeoutRule.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ArgumentCapturing.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ArgumentCapturing.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ArgumentMatching.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ArgumentMatching.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ChainedResponseGenerators.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ChainedResponseGenerators.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ErrorReporting.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ErrorReporting.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ExplicitInteractions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ExplicitInteractions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GenericMockInvocations.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GenericMockInvocations.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GlobalInteractions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GlobalInteractions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocksForGroovyClasses.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocksForGroovyClasses.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocksForInterfaces.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovyMocksForInterfaces.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovySpiesThatAreGlobal.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/GroovySpiesThatAreGlobal.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionScopes.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionScopes.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionsReferencingFieldsAndProperties.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionsReferencingFieldsAndProperties.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionsWithPropertySyntax.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/InteractionsWithPropertySyntax.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/InvalidMockCreation.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/InvalidMockCreation.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/InvokingMocksFromMultipleThreads.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/InvokingMocksFromMultipleThreads.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaMocksDefaultBehavior.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaMocksDefaultBehavior.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaMocksForGroovyClasses.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaMocksForGroovyClasses.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaSpies.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaSpies.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaStubs.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/JavaStubs.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MethodMatching.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MethodMatching.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockBasics.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockBasics.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockCreationWithClosure.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockCreationWithClosure.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockDefaultResponses.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockDefaultResponses.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockNames.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockNames.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockProxyCaching.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockProxyCaching.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingAndBridgeMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingAndBridgeMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingClosures.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingClosures.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingMethodsWithNamedParameters.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingMethodsWithNamedParameters.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingMethodsWithVarArgParameters.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingMethodsWithVarArgParameters.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingOfVarArgParametersUserContributedSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/MockingOfVarArgParametersUserContributedSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/OrderedInteractions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/OrderedInteractions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/OverlappingInteractions.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/OverlappingInteractions.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/PartialMocking.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/PartialMocking.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ResponseGenerators.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ResponseGenerators.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/SpreadWildcardUsage.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/SpreadWildcardUsage.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/SpyBasics.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/SpyBasics.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubBasics.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubBasics.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubDefaultResponses.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubDefaultResponses.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubDefaultResponsesWithGenericMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/StubDefaultResponsesWithGenericMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/TargetMatching.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/TargetMatching.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/TooFewInvocations.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/TooFewInvocations.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/TooManyInvocations.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/TooManyInvocations.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ValidMockCreation.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ValidMockCreation.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/ValidMockCreationInDerivedClass.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/ValidMockCreationInDerivedClass.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/mock/WildcardUsages.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/mock/WildcardUsages.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/DataProviders.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/DataProviders.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/DataTables.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/DataTables.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/InvalidWhereBlocks.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/InvalidWhereBlocks.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/MethodParameters.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/MethodParameters.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/ParameterizationScopes.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/ParameterizationScopes.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/Parameterizations.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/Parameterizations.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/SqlDataSource.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/SqlDataSource.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/UnrolledFeatureMethods.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/smoke/parameterization/UnrolledFeatureMethods.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/AbstractMultisetSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/AbstractMultisetSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/CollectionUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/CollectionUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/ConsoleUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/ConsoleUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/ExceptionUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/ExceptionUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/GroovyUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/GroovyUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/HashMultisetSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/HashMultisetSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/IoUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/IoUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/JsonWriterSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/JsonWriterSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/LinkedHashMultisetSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/LinkedHashMultisetSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/ObjectUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/ObjectUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/ReflectionUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/ReflectionUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/SpockReleaseInfoSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/SpockReleaseInfoSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/StringMessagePrintStreamSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/StringMessagePrintStreamSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/TeePrintStreamSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/TeePrintStreamSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/TextUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/TextUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/TimeUtilSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/TimeUtilSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/util/VersionNumberSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/util/VersionNumberSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/org/spockframework/verifyall/VerifyAllSpecification.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/org/spockframework/verifyall/VerifyAllSpecification.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/ExceptionsSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/ExceptionsSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/JvmSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/JvmSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/OperatingSystemSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/OperatingSystemSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/concurrent/AsyncConditionsSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/concurrent/AsyncConditionsSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/concurrent/BlockingVariableSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/concurrent/BlockingVariableSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/concurrent/BlockingVariablesSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/concurrent/BlockingVariablesSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/concurrent/PollingConditionsSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/concurrent/PollingConditionsSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/matcher/IsCloseToSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/matcher/IsCloseToSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/mop/ConfineMetaClassChangesSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/mop/ConfineMetaClassChangesSpec.groovy", [])
    }

    void "test spock-specs/src/test/groovy/spock/util/mop/UseSpec.groovy"() {
        unzipAndTest("spock-specs/src/test/groovy/spock/util/mop/UseSpec.groovy", [])
    }

    void "test spock-spring/boot-test/boot-test.gradle"() {
        unzipAndTest("spock-spring/boot-test/boot-test.gradle", [])
    }

    void "test spock-spring/boot-test/src/test/groovy/org/spockframework/boot/DataJpaTestIntegrationSpec.groovy"() {
        unzipAndTest("spock-spring/boot-test/src/test/groovy/org/spockframework/boot/DataJpaTestIntegrationSpec.groovy", [])
    }

    void "test spock-spring/boot-test/src/test/groovy/org/spockframework/boot/SimpleBootAppIntegrationSpec.groovy"() {
        unzipAndTest("spock-spring/boot-test/src/test/groovy/org/spockframework/boot/SimpleBootAppIntegrationSpec.groovy", [])
    }

    void "test spock-spring/boot-test/src/test/groovy/org/spockframework/boot/SpringBootTestAnnotationIntegrationSpec.groovy"() {
        unzipAndTest("spock-spring/boot-test/src/test/groovy/org/spockframework/boot/SpringBootTestAnnotationIntegrationSpec.groovy", [])
    }

    void "test spock-spring/boot-test/src/test/groovy/org/spockframework/boot/WebMvcTestIntegrationSpec.groovy"() {
        unzipAndTest("spock-spring/boot-test/src/test/groovy/org/spockframework/boot/WebMvcTestIntegrationSpec.groovy", [])
    }

    void "test spock-spring/spring.gradle"() {
        unzipAndTest("spock-spring/spring.gradle", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/ContextHierarchyExample.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/ContextHierarchyExample.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/DirtiesContextExample.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/DirtiesContextExample.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/IService1.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/IService1.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/IService2.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/IService2.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/InjectionExamples.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/InjectionExamples.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/MockInjectionExample.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/MockInjectionExample.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/MockInjectionWithEmbeddedConfig.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/MockInjectionWithEmbeddedConfig.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/Service1.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/Service1.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/Service2.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/Service2.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/SpringSpecInheritance.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/SpringSpecInheritance.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/TransactionalExample.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/TransactionalExample.groovy", [])
    }

    void "test spock-spring/src/test/groovy/org/spockframework/spring/TransactionalGroovySqlExample.groovy"() {
        unzipAndTest("spock-spring/src/test/groovy/org/spockframework/spring/TransactionalGroovySqlExample.groovy", [])
    }

    void "test spock-tapestry/src/test/groovy/org/spockframework/tapestry/BeforeRegistryCreatedMethod.groovy"() {
        unzipAndTest("spock-tapestry/src/test/groovy/org/spockframework/tapestry/BeforeRegistryCreatedMethod.groovy", [])
    }

    void "test spock-tapestry/src/test/groovy/org/spockframework/tapestry/InjectionExamples.groovy"() {
        unzipAndTest("spock-tapestry/src/test/groovy/org/spockframework/tapestry/InjectionExamples.groovy", [])
    }

    void "test spock-tapestry/src/test/groovy/org/spockframework/tapestry/TapestrySpecInheritance.groovy"() {
        unzipAndTest("spock-tapestry/src/test/groovy/org/spockframework/tapestry/TapestrySpecInheritance.groovy", [])
    }

    void "test spock-tapestry/tapestry.gradle"() {
        unzipAndTest("spock-tapestry/tapestry.gradle", [])
    }

    void "test spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/User.groovy"() {
        unzipAndTest("spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/User.groovy", [])
    }

    void "test spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/UserDao.groovy"() {
        unzipAndTest("spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/UserDao.groovy", [])
    }

    void "test spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/UserDaoSpec.groovy"() {
        unzipAndTest("spock-unitils/src/test/groovy/org/spockframework/unitils/dbunit/UserDaoSpec.groovy", [])
    }

    void "test spock-unitils/unitils.gradle"() {
        unzipAndTest("spock-unitils/unitils.gradle", [])
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

    public static final String ZIP_PATH = "$TestUtils.RESOURCES_PATH/spock-spock-1.1-rc-2/spock-spock-1.1-rc-2-allsources.zip";

}
