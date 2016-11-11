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
 * Add Geb 1.0 sources as test cases
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/10/12
 */
class Geb10SourcesTest extends GroovyTestCase {


    void "test buildSrc/build.gradle"() {
        unzipAndTest("buildSrc/build.gradle", [])
    }

    void "test compatibility/groovy-2.3.7/groovy-2.3.7.gradle"() {
        unzipAndTest("compatibility/groovy-2.3.7/groovy-2.3.7.gradle", [])
    }

    void "test compatibility/groovy-2.3.7/src/test/groovy/geb/NavigatorCompatibilitySpec.groovy"() {
        unzipAndTest("compatibility/groovy-2.3.7/src/test/groovy/geb/NavigatorCompatibilitySpec.groovy", [])
    }

    void "test compatibility/spock-1.1/spock-1.1.gradle"() {
        unzipAndTest("compatibility/spock-1.1/spock-1.1.gradle", [])
    }

    void "test compatibility/spock-1.1/src/test/groovy/geb/transform/implicitassertions/Spock1Dot1IntegrationSpec.groovy"() {
        unzipAndTest("compatibility/spock-1.1/src/test/groovy/geb/transform/implicitassertions/Spock1Dot1IntegrationSpec.groovy", [])
    }

    void "test doc/manual-snippets/manual-snippets.gradle"() {
        unzipAndTest("doc/manual-snippets/manual-snippets.gradle", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/fixture/GebSpecWithServerUsingJavascript.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/fixture/GebSpecWithServerUsingJavascript.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/javascript/JQuerySupportSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/javascript/JQuerySupportSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/navigator/BackspaceSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/navigator/BackspaceSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/navigator/ControlClickSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/navigator/ControlClickSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/navigator/DragAndDropSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/navigator/DragAndDropSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/navigator/InteractionsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/navigator/InteractionsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/navigator/NonCharacterKeystrokesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/navigator/NonCharacterKeystrokesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/groovy/pages/ToWaitOptionSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/groovy/pages/ToWaitOptionSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/realBrowserTest/resources/GebConfig.groovy"() {
        unzipAndTest("doc/manual-snippets/src/realBrowserTest/resources/GebConfig.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsExamplePage.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsExamplePage.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsIntroductionSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsIntroductionSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/assertions/ImplicitAssertionsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/binding/BindingUpdatingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/binding/BindingUpdatingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/BrowserCreationSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/BrowserCreationSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/ContentDslToParameterSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/ContentDslToParameterSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/DriveSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/DriveSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/GoSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/GoSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/GoogleSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/GoogleSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/PageChangeListenerSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/PageChangeListenerSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/PageSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/PageSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/QuitSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/QuitSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/ViaSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/ViaSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/WithNewWindowSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/WithNewWindowSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/browser/WithWindowSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/browser/WithWindowSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/AtCheckWaitingConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/AtCheckWaitingConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/AutoClearCookiesConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/AutoClearCookiesConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/BaseNavigatorWaitingConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/BaseNavigatorWaitingConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/DriverConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/DriverConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/FunctionalSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/FunctionalSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/InlineConfigurationLoader.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/InlineConfigurationLoader.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/NavigatorFactoryConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/NavigatorFactoryConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/ReportOnTestFailuresConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/ReportOnTestFailuresConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/ReporterConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/ReporterConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/ReportsDirConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/ReportsDirConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/TemporaryFolderProvider.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/TemporaryFolderProvider.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/UnexpectedPagesConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/UnexpectedPagesConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/configuration/WaitingConfigSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/configuration/WaitingConfigSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/downloading/DownloadingConfigurationSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/downloading/DownloadingConfigurationSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/downloading/DownloadingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/downloading/DownloadingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/downloading/FineGrainedRequestSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/downloading/FineGrainedRequestSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/downloading/UntrustedCertificatesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/downloading/UntrustedCertificatesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/driver/HtmlUnitRefreshHandlerSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/driver/HtmlUnitRefreshHandlerSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/fixture/Browser.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/fixture/Browser.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/fixture/DriveMethodSupportingSpecWithServer.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/fixture/DriveMethodSupportingSpecWithServer.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/ide/StrongTypingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/ide/StrongTypingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/GebHomepageSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/GebHomepageSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/IntroSamplesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/IntroSamplesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/ScriptingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/ScriptingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/module/HighlightsModule.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/module/HighlightsModule.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/module/SelectableLinkModule.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/module/SelectableLinkModule.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/intro/page/GebHomePage.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/intro/page/GebHomePage.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/AccessingVariablesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/AccessingVariablesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/AlertSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/AlertSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/CallingMethodsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/CallingMethodsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/ConfirmSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/ConfirmSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/CustomMessageSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/CustomMessageSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/ExecutingArbitraryCodeSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/ExecutingArbitraryCodeSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/JavaScriptExecutorSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/JavaScriptExecutorSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/javascript/WaitingExamplesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/javascript/WaitingExamplesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/BaseAndContextSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/BaseAndContextSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/CheckboxSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/CheckboxSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/CombinedModuleBaseSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/CombinedModuleBaseSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/FileInputSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/FileInputSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/FormContentSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/FormContentSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/FormElementSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/FormElementSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/IntroductionSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/IntroductionSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/ModuleIsANavigatorSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/ModuleIsANavigatorSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/MultipleSelectSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/MultipleSelectSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/RadioButtonsSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/RadioButtonsSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/RepeatingContentSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/RepeatingContentSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/ReusingModulesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/ReusingModulesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/SelectSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/SelectSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/TextInputSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/TextInputSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/TextareaSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/TextareaSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/modules/UnwrappingModulesSnippetSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/modules/UnwrappingModulesSnippetSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/CheckboxSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/CheckboxSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/ClickingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/ClickingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/CompositionSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/CompositionSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/CssPropertiesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/CssPropertiesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/DollarExamplesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/DollarExamplesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/EqualsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/EqualsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/FileUploadSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/FileUploadSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/FindingAndFilteringSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/FindingAndFilteringSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/FormShortcutsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/FormShortcutsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/IterableSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/IterableSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/KeystrokesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/KeystrokesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/MultiSelectSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/MultiSelectSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/RadioSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/RadioSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/SelectSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/SelectSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/SizeAndLocationSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/SizeAndLocationSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/TagTextClassesAttributesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/TagTextClassesAttributesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/TextInputAndAreaSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/TextInputAndAreaSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/navigator/TraversingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/navigator/TraversingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/AdvancedNavigationSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/AdvancedNavigationSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/AliasingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/AliasingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/AtSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/AtSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/ContentDslSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/ContentDslSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/FramesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/FramesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/LifecycleHooksSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/LifecycleHooksSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/PageAtCheckWaitingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/PageAtCheckWaitingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/PageInheritanceSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/PageInheritanceSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/PageObjectPatternSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/PageObjectPatternSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/PageUrlSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/PageUrlSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/ParameterizedPageSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/ParameterizedPageSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/TemplateOptionsSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/TemplateOptionsSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/pages/UnexpectedPagesSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/pages/UnexpectedPagesSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/reporting/ReportingListenerSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/reporting/ReportingListenerSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/reporting/ReportingSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/reporting/ReportingSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/testing/FunctionalSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/testing/FunctionalSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/groovy/testing/ReportingFunctionalSpec.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/groovy/testing/ReportingFunctionalSpec.groovy", [])
    }

    void "test doc/manual-snippets/src/test/resources/gebScript.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/resources/gebScript.groovy", [])
    }

    void "test doc/manual-snippets/src/test/resources/gebScriptUsingPages.groovy"() {
        unzipAndTest("doc/manual-snippets/src/test/resources/gebScriptUsingPages.groovy", [])
    }

    void "test doc/manual/manual.gradle"() {
        unzipAndTest("doc/manual/manual.gradle", [])
    }

    void "test doc/site/site.gradle"() {
        unzipAndTest("doc/site/site.gradle", [])
    }

    void "test doc/site/src/main/groovy/geb/site/Manuals.groovy"() {
        unzipAndTest("doc/site/src/main/groovy/geb/site/Manuals.groovy", [])
    }

    void "test doc/site/src/ratpack/Ratpack.groovy"() {
        unzipAndTest("doc/site/src/ratpack/Ratpack.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/LinkCrawlSpec.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/LinkCrawlSpec.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/SiteSmokeSpec.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/SiteSmokeSpec.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/crawl/Crawler.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/crawl/Crawler.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/crawl/PrettyPrintCollection.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/crawl/PrettyPrintCollection.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/modules/MenuItemModule.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/modules/MenuItemModule.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/pages/ApiPage.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/pages/ApiPage.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/pages/ContentPage.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/pages/ContentPage.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/pages/ManualPage.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/pages/ManualPage.groovy", [])
    }

    void "test doc/site/src/test/groovy/geb/pages/NotFoundPage.groovy"() {
        unzipAndTest("doc/site/src/test/groovy/geb/pages/NotFoundPage.groovy", [])
    }

    void "test geb.gradle"() {
        unzipAndTest("geb.gradle", [])
    }

    void "test gradle/codenarc/rulesets.groovy"() {
        unzipAndTest("gradle/codenarc/rulesets.groovy", [])
    }

    void "test gradle/idea.gradle"() {
        unzipAndTest("gradle/idea.gradle", [])
    }

    void "test gradle/pom.gradle"() {
        unzipAndTest("gradle/pom.gradle", [])
    }

    void "test integration/geb-gradle/geb-gradle.gradle"() {
        unzipAndTest("integration/geb-gradle/geb-gradle.gradle", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackAccount.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackAccount.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackExtension.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackExtension.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackPlugin.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackPlugin.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackTunnel.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/BrowserStackTunnel.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/task/DownloadBrowserStackTunnel.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/browserstack/task/DownloadBrowserStackTunnel.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/cloud/BrowserSpec.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/cloud/BrowserSpec.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/cloud/ExternalTunnel.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/cloud/ExternalTunnel.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/cloud/task/StartExternalTunnel.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/cloud/task/StartExternalTunnel.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/cloud/task/StopExternalTunnel.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/cloud/task/StopExternalTunnel.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceAccount.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceAccount.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceConnect.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceConnect.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceConnectOperations.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceConnectOperations.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceLabsExtension.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SauceLabsExtension.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SaucePlugin.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/SaucePlugin.groovy", [])
    }

    void "test integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/UnpackSauceConnect.groovy"() {
        unzipAndTest("integration/geb-gradle/src/main/groovy/geb/gradle/saucelabs/UnpackSauceConnect.groovy", [])
    }

    void "test integration/geb-grails/geb-grails.gradle"() {
        unzipAndTest("integration/geb-grails/geb-grails.gradle", [])
    }

    void "test integration/geb-grails/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("integration/geb-grails/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test integration/geb-grails/grails-app/conf/DataSource.groovy"() {
        unzipAndTest("integration/geb-grails/grails-app/conf/DataSource.groovy", [])
    }

    void "test integration/geb-grails/grails-app/conf/UrlMappings.groovy"() {
        unzipAndTest("integration/geb-grails/grails-app/conf/UrlMappings.groovy", [])
    }

    void "test integration/geb-grails/grails-app/controllers/TheController.groovy"() {
        unzipAndTest("integration/geb-grails/grails-app/controllers/TheController.groovy", [])
    }

    void "test integration/geb-grails/scripts/_Events.groovy"() {
        unzipAndTest("integration/geb-grails/scripts/_Events.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/DivModule.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/DivModule.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/GEB32Spec.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/GEB32Spec.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/IndexPage.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/IndexPage.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/JUnit4SmokeTest.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/JUnit4SmokeTest.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/OtherPage.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/OtherPage.groovy", [])
    }

    void "test integration/geb-grails/test/functional/grails/plugin/geb/SmokeSpec.groovy"() {
        unzipAndTest("integration/geb-grails/test/functional/grails/plugin/geb/SmokeSpec.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/GebConfig.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/GebConfig.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/Android.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/Android.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/CallbackAndWebDriverServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/CallbackAndWebDriverServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/CallbackHttpServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/CallbackHttpServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/CallbackHttpsServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/CallbackHttpsServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/CallbackServlet.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/CallbackServlet.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/GebSpec.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/GebSpec.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/GebSpecWithCallbackServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/GebSpecWithCallbackServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/GebSpecWithServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/GebSpecWithServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/RemoteWebDriverWithExpectations.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/RemoteWebDriverWithExpectations.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/RequiresRealBrowser.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/RequiresRealBrowser.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/TestHttpServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/TestHttpServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/test/WebDriverServer.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/test/WebDriverServer.groovy", [])
    }

    void "test internal/test-support/src/main/groovy/geb/transform/implicitassertions/TransformTestHelper.groovy"() {
        unzipAndTest("internal/test-support/src/main/groovy/geb/transform/implicitassertions/TransformTestHelper.groovy", [])
    }

    void "test internal/test-support/src/main/resources/SpockConfig.groovy"() {
        unzipAndTest("internal/test-support/src/main/resources/SpockConfig.groovy", [])
    }

    void "test internal/test-support/test-support.gradle"() {
        unzipAndTest("internal/test-support/test-support.gradle", [])
    }

    void "test module/geb-ast/geb-ast.gradle"() {
        unzipAndTest("module/geb-ast/geb-ast.gradle", [])
    }

    void "test module/geb-ast/src/main/groovy/geb/navigator/AttributeAccessingMetaClass.groovy"() {
        unzipAndTest("module/geb-ast/src/main/groovy/geb/navigator/AttributeAccessingMetaClass.groovy", [])
    }

    void "test module/geb-ast/src/main/groovy/geb/transform/AttributeAccessingMetaClassRegisteringTransformation.groovy"() {
        unzipAndTest("module/geb-ast/src/main/groovy/geb/transform/AttributeAccessingMetaClassRegisteringTransformation.groovy", [])
    }

    void "test module/geb-ast/src/main/groovy/geb/transform/AttributeAccessingMetaClassRegistrar.groovy"() {
        unzipAndTest("module/geb-ast/src/main/groovy/geb/transform/AttributeAccessingMetaClassRegistrar.groovy", [])
    }

    void "test module/geb-core/geb-core.gradle"() {
        unzipAndTest("module/geb-core/geb-core.gradle", [])
    }

    void "test module/geb-core/src/main/groovy/geb/AtVerificationResult.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/AtVerificationResult.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/Browser.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/Browser.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/BuildAdapter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/BuildAdapter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/Configuration.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/Configuration.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/ConfigurationLoader.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/ConfigurationLoader.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/Initializable.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/Initializable.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/Module.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/Module.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/Page.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/Page.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/PageChangeListener.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/PageChangeListener.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/binding/BindingUpdater.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/binding/BindingUpdater.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/buildadapter/BuildAdapterFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/buildadapter/BuildAdapterFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/buildadapter/SystemPropertiesBuildAdapter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/buildadapter/SystemPropertiesBuildAdapter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/DefaultPageContentSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/DefaultPageContentSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/ModuleBaseCalculator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/ModuleBaseCalculator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/ModuleBaseDefinitionDelegate.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/ModuleBaseDefinitionDelegate.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/Navigable.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/Navigable.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/NavigableSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/NavigableSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentContainer.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentContainer.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentNames.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentNames.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentTemplate.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentTemplate.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentTemplateBuilder.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentTemplateBuilder.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentTemplateFactoryDelegate.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentTemplateFactoryDelegate.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/PageContentTemplateParams.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/PageContentTemplateParams.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/StringRepresentationProvider.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/StringRepresentationProvider.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/TemplateDerivedContentStringRepresentationProvider.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/TemplateDerivedContentStringRepresentationProvider.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/TemplateDerivedPageContent.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/TemplateDerivedPageContent.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/UninitializedNavigableSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/UninitializedNavigableSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/content/UninitializedPageContentSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/content/UninitializedPageContentSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/download/DefaultDownloadSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/download/DefaultDownloadSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/download/DownloadSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/download/DownloadSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/download/UninitializedDownloadSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/download/UninitializedDownloadSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/download/helper/SelfSignedCertificateHelper.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/download/helper/SelfSignedCertificateHelper.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/BrowserStackDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/BrowserStackDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/CachingDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/CachingDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/CallbackDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/CallbackDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/CloudDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/CloudDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/DefaultDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/DefaultDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/DriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/DriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/DriverRegistry.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/DriverRegistry.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/NameBasedDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/NameBasedDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/RemoteDriverOperations.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/RemoteDriverOperations.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/driver/SauceLabsDriverFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/driver/SauceLabsDriverFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/error/SingleElementNavigatorOnlyMethodException.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/error/SingleElementNavigatorOnlyMethodException.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/frame/DefaultFrameSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/frame/DefaultFrameSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/frame/FrameSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/frame/FrameSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/frame/UninitializedFrameSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/frame/UninitializedFrameSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/interaction/DefaultInteractionsSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/interaction/DefaultInteractionsSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/interaction/InteractDelegate.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/interaction/InteractDelegate.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/interaction/InteractionsSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/interaction/InteractionsSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/interaction/UninitializedInteractionSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/interaction/UninitializedInteractionSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/js/AlertAndConfirmSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/js/AlertAndConfirmSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/js/DefaultAlertAndConfirmSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/js/DefaultAlertAndConfirmSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/js/JQueryAdapter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/js/JQueryAdapter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/js/JavascriptInterface.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/js/JavascriptInterface.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/js/UninitializedAlertAndConfirmSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/js/UninitializedAlertAndConfirmSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/AbstractInput.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/AbstractInput.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/Checkbox.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/Checkbox.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/FileInput.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/FileInput.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/FormElement.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/FormElement.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/MultipleSelect.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/MultipleSelect.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/RadioButtons.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/RadioButtons.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/Select.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/Select.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/TextInput.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/TextInput.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/module/Textarea.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/module/Textarea.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/AbstractNavigator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/AbstractNavigator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/BasicLocator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/BasicLocator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/CssSelector.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/CssSelector.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/DefaultLocator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/DefaultLocator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/EmptyNavigator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/EmptyNavigator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/Locator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/Locator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/Navigator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/Navigator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/NonEmptyNavigator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/NonEmptyNavigator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/SearchContextBasedBasicLocator.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/SearchContextBasedBasicLocator.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/SelectFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/SelectFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/AbstractNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/AbstractNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/BrowserBackedNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/BrowserBackedNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/ClosureInnerNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/ClosureInnerNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/DefaultInnerNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/DefaultInnerNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/InnerNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/InnerNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/NavigatorBackedNavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/NavigatorBackedNavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/navigator/factory/NavigatorFactory.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/navigator/factory/NavigatorFactory.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/CompositeReporter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/CompositeReporter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/ExceptionToPngConverter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/ExceptionToPngConverter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/PageSourceReporter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/PageSourceReporter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/ReportState.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/ReportState.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/Reporter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/Reporter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/ReporterSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/ReporterSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/ReportingListener.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/ReportingListener.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/report/ScreenshotReporter.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/report/ScreenshotReporter.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/textmatching/NegatedTextMatcher.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/textmatching/NegatedTextMatcher.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/textmatching/PatternTextMatcher.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/textmatching/PatternTextMatcher.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/textmatching/TextMatcher.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/textmatching/TextMatcher.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/textmatching/TextMatchingSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/textmatching/TextMatchingSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/waiting/DefaultWaitingSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/waiting/DefaultWaitingSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/waiting/UninitializedWaitingSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/waiting/UninitializedWaitingSupport.groovy", [])
    }

    void "test module/geb-core/src/main/groovy/geb/waiting/WaitingSupport.groovy"() {
        unzipAndTest("module/geb-core/src/main/groovy/geb/waiting/WaitingSupport.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/GebConfigBothScriptAndClass.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/GebConfigBothScriptAndClass.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/GebConfigClassOnly.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/GebConfigClassOnly.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/AlertAndConfirmHandlingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/AlertAndConfirmHandlingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/BadContentDefinitionsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/BadContentDefinitionsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/BrowserSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/BrowserSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/CallingWithMethodOnPageSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/CallingWithMethodOnPageSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ConfigurationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ConfigurationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ContentAccessShortcutsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ContentAccessShortcutsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ContentToStringSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ContentToStringSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ContentUnwrappingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ContentUnwrappingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/DriveSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/DriveSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/JavascriptInterfaceSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/JavascriptInterfaceSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ModulesSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ModulesSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/NavigableSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/NavigableSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/PageChangeListeningSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/PageChangeListeningSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/PageContentNameSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/PageContentNameSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/PageLoadUnloadListeningSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/PageLoadUnloadListeningSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/PageOrientedSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/PageOrientedSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/PropertiesInModuleContentSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/PropertiesInModuleContentSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/RemoteDriverSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/RemoteDriverSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/RemovedModuleMethodsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/RemovedModuleMethodsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/UnexpectedPagesSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/UnexpectedPagesSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/UrlCalculationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/UrlCalculationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/ViaSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/ViaSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/binding/BindingUpdaterSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/binding/BindingUpdaterSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/BaseUrlConfigurationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/BaseUrlConfigurationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/ConfigurationDriverCreationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/ConfigurationDriverCreationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/ConfigurationLoaderSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/ConfigurationLoaderSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/ConfigurationNavigatorFactorySpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/ConfigurationNavigatorFactorySpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/DriverCachingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/DriverCachingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/conf/WaitingConfigurationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/conf/WaitingConfigurationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/download/DownloadingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/download/DownloadingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/download/HttpsDownloadingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/download/HttpsDownloadingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/driver/DriverWithInvalidGetCurrentUrlImplementationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/driver/DriverWithInvalidGetCurrentUrlImplementationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/driver/RemoteDriverOperationsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/driver/RemoteDriverOperationsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/driver/WebDriverCommandsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/driver/WebDriverCommandsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/frame/BaseFrameSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/frame/BaseFrameSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/frame/BasicFrameSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/frame/BasicFrameSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/frame/FrameSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/frame/FrameSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/interaction/InteractionsSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/interaction/InteractionsSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/js/JQueryAdapterSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/js/JQueryAdapterSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/CheckboxBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/CheckboxBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/CheckboxSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/CheckboxSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/FileInputBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/FileInputBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/FileInputSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/FileInputSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/FormElementBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/FormElementBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/FormElementSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/FormElementSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/InputBasedModuleSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/InputBasedModuleSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/MultipleSelectBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/MultipleSelectBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/MultipleSelectSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/MultipleSelectSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/RadioButtonsBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/RadioButtonsBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/RadioButtonsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/RadioButtonsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/SelectBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/SelectBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/SelectSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/SelectSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/TextInputBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/TextInputBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/TextInputSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/TextInputSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/TextareaBaseSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/TextareaBaseSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/module/TextareaSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/module/TextareaSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/CssSelectorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/CssSelectorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/CustomNavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/CustomNavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/EmptyNavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/EmptyNavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/ExoticAttributeValuesSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/ExoticAttributeValuesSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/FindAndFilterNavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/FindAndFilterNavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/FindViaTextSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/FindViaTextSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/FormControlSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/FormControlSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/ModuleFromNavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/ModuleFromNavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorClickSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorClickSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorCssSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorCssSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorElementsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorElementsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorEqualsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorEqualsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorGroovySpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorGroovySpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorSizeAndLocationSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorSizeAndLocationSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/NavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/NavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/RelativeContentNavigatorSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/RelativeContentNavigatorSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/ReloadOnValueChangeSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/ReloadOnValueChangeSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/SelectControlSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/SelectControlSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/SelectFactorySpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/SelectFactorySpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/navigator/SingleElementNavigatorOnlyMethodsSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/navigator/SingleElementNavigatorOnlyMethodsSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/report/ReporterSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/report/ReporterSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/textmatching/TextMatchingSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/textmatching/TextMatchingSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/waiting/AtCheckWaitingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/waiting/AtCheckWaitingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/waiting/BaseNavigatorWaitingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/waiting/BaseNavigatorWaitingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/waiting/WaitingContentSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/waiting/WaitingContentSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/waiting/WaitingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/waiting/WaitingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/waiting/WaitingSupportSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/waiting/WaitingSupportSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/window/BaseWindowHandlingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/window/BaseWindowHandlingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/window/BasicWindowHandlingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/window/BasicWindowHandlingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/groovy/geb/window/WindowHandlingSpec.groovy"() {
        unzipAndTest("module/geb-core/src/test/groovy/geb/window/WindowHandlingSpec.groovy", [])
    }

    void "test module/geb-core/src/test/resources/geb/conf/good-conf.groovy"() {
        unzipAndTest("module/geb-core/src/test/resources/geb/conf/good-conf.groovy", [])
    }

    void "test module/geb-exceptions/geb-exceptions.gradle"() {
        unzipAndTest("module/geb-exceptions/geb-exceptions.gradle", [])
    }

    void "test module/geb-implicit-assertions/geb-implicit-assertions.gradle"() {
        unzipAndTest("module/geb-implicit-assertions/geb-implicit-assertions.gradle", [])
    }

    void "test module/geb-implicit-assertions/src/main/groovy/geb/transform/implicitassertions/ImplicitAssertionsTransformationVisitor.groovy"() {
        unzipAndTest("module/geb-implicit-assertions/src/main/groovy/geb/transform/implicitassertions/ImplicitAssertionsTransformationVisitor.groovy", [])
    }

    void "test module/geb-implicit-assertions/src/test/groovy/geb/transform/implicitassertions/ImplicitAssertionsTransformationSpec.groovy"() {
        unzipAndTest("module/geb-implicit-assertions/src/test/groovy/geb/transform/implicitassertions/ImplicitAssertionsTransformationSpec.groovy", [])
    }

    void "test module/geb-implicit-assertions/src/test/groovy/geb/transform/implicitassertions/SpockIntegrationSpec.groovy"() {
        unzipAndTest("module/geb-implicit-assertions/src/test/groovy/geb/transform/implicitassertions/SpockIntegrationSpec.groovy", [])
    }

    void "test module/geb-junit3/geb-junit3.gradle"() {
        unzipAndTest("module/geb-junit3/geb-junit3.gradle", [])
    }

    void "test module/geb-junit3/src/main/groovy/geb/junit3/GebReportingTest.groovy"() {
        unzipAndTest("module/geb-junit3/src/main/groovy/geb/junit3/GebReportingTest.groovy", [])
    }

    void "test module/geb-junit3/src/main/groovy/geb/junit3/GebTest.groovy"() {
        unzipAndTest("module/geb-junit3/src/main/groovy/geb/junit3/GebTest.groovy", [])
    }

    void "test module/geb-junit3/src/test/groovy/geb/junit3/GebReportingTestTest.groovy"() {
        unzipAndTest("module/geb-junit3/src/test/groovy/geb/junit3/GebReportingTestTest.groovy", [])
    }

    void "test module/geb-junit3/src/test/groovy/geb/junit3/GebTestTest.groovy"() {
        unzipAndTest("module/geb-junit3/src/test/groovy/geb/junit3/GebTestTest.groovy", [])
    }

    void "test module/geb-junit4/geb-junit4.gradle"() {
        unzipAndTest("module/geb-junit4/geb-junit4.gradle", [])
    }

    void "test module/geb-junit4/src/main/groovy/geb/junit4/GebReportingTest.groovy"() {
        unzipAndTest("module/geb-junit4/src/main/groovy/geb/junit4/GebReportingTest.groovy", [])
    }

    void "test module/geb-junit4/src/main/groovy/geb/junit4/GebTest.groovy"() {
        unzipAndTest("module/geb-junit4/src/main/groovy/geb/junit4/GebTest.groovy", [])
    }

    void "test module/geb-junit4/src/test/groovy/geb/junit4/GebReportingTestTest.groovy"() {
        unzipAndTest("module/geb-junit4/src/test/groovy/geb/junit4/GebReportingTestTest.groovy", [])
    }

    void "test module/geb-junit4/src/test/groovy/geb/junit4/GebTestTest.groovy"() {
        unzipAndTest("module/geb-junit4/src/test/groovy/geb/junit4/GebTestTest.groovy", [])
    }

    void "test module/geb-spock/geb-spock.gradle"() {
        unzipAndTest("module/geb-spock/geb-spock.gradle", [])
    }

    void "test module/geb-spock/src/main/groovy/geb/spock/GebReportingSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/main/groovy/geb/spock/GebReportingSpec.groovy", [])
    }

    void "test module/geb-spock/src/main/groovy/geb/spock/GebSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/main/groovy/geb/spock/GebSpec.groovy", [])
    }

    void "test module/geb-spock/src/main/groovy/geb/spock/OnFailureReporter.groovy"() {
        unzipAndTest("module/geb-spock/src/main/groovy/geb/spock/OnFailureReporter.groovy", [])
    }

    void "test module/geb-spock/src/main/groovy/geb/spock/ReportingOnFailureExtension.groovy"() {
        unzipAndTest("module/geb-spock/src/main/groovy/geb/spock/ReportingOnFailureExtension.groovy", [])
    }

    void "test module/geb-spock/src/test/groovy/geb/spock/ExceptionOnReportScreenshotSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/test/groovy/geb/spock/ExceptionOnReportScreenshotSpec.groovy", [])
    }

    void "test module/geb-spock/src/test/groovy/geb/spock/GebReportingSpecSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/test/groovy/geb/spock/GebReportingSpecSpec.groovy", [])
    }

    void "test module/geb-spock/src/test/groovy/geb/spock/GebSpecSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/test/groovy/geb/spock/GebSpecSpec.groovy", [])
    }

    void "test module/geb-spock/src/test/groovy/geb/spock/GebSpecStepwiseSpec.groovy"() {
        unzipAndTest("module/geb-spock/src/test/groovy/geb/spock/GebSpecStepwiseSpec.groovy", [])
    }

    void "test module/geb-test-common/geb-test-common.gradle"() {
        unzipAndTest("module/geb-test-common/geb-test-common.gradle", [])
    }

    void "test module/geb-test-common/src/main/groovy/geb/junit4/rule/FailureTracker.groovy"() {
        unzipAndTest("module/geb-test-common/src/main/groovy/geb/junit4/rule/FailureTracker.groovy", [])
    }

    void "test module/geb-testng/geb-testng.gradle"() {
        unzipAndTest("module/geb-testng/geb-testng.gradle", [])
    }

    void "test module/geb-testng/src/main/groovy/geb/testng/GebReportingTestTrait.groovy"() {
        unzipAndTest("module/geb-testng/src/main/groovy/geb/testng/GebReportingTestTrait.groovy", [])
    }

    void "test module/geb-testng/src/main/groovy/geb/testng/GebTestTrait.groovy"() {
        unzipAndTest("module/geb-testng/src/main/groovy/geb/testng/GebTestTrait.groovy", [])
    }

    void "test module/geb-testng/src/test/groovy/geb/testng/GebReportingTestCleanupTest.groovy"() {
        unzipAndTest("module/geb-testng/src/test/groovy/geb/testng/GebReportingTestCleanupTest.groovy", [])
    }

    void "test module/geb-testng/src/test/groovy/geb/testng/GebReportingTestTest.groovy"() {
        unzipAndTest("module/geb-testng/src/test/groovy/geb/testng/GebReportingTestTest.groovy", [])
    }

    void "test module/geb-testng/src/test/groovy/geb/testng/GebTestTest.groovy"() {
        unzipAndTest("module/geb-testng/src/test/groovy/geb/testng/GebTestTest.groovy", [])
    }

    void "test module/geb-waiting/geb-waiting.gradle"() {
        unzipAndTest("module/geb-waiting/geb-waiting.gradle", [])
    }

    void "test module/geb-waiting/src/main/groovy/geb/waiting/UnknownWaitForEvaluationResult.groovy"() {
        unzipAndTest("module/geb-waiting/src/main/groovy/geb/waiting/UnknownWaitForEvaluationResult.groovy", [])
    }

    void "test module/geb-waiting/src/main/groovy/geb/waiting/Wait.groovy"() {
        unzipAndTest("module/geb-waiting/src/main/groovy/geb/waiting/Wait.groovy", [])
    }

    void "test module/geb-waiting/src/test/groovy/geb/waiting/UnknownWaitForEvaluationResultSpec.groovy"() {
        unzipAndTest("module/geb-waiting/src/test/groovy/geb/waiting/UnknownWaitForEvaluationResultSpec.groovy", [])
    }

    void "test module/geb-waiting/src/test/groovy/geb/waiting/WaitSpec.groovy"() {
        unzipAndTest("module/geb-waiting/src/test/groovy/geb/waiting/WaitSpec.groovy", [])
    }

    void "test module/module.gradle"() {
        unzipAndTest("module/module.gradle", [])
    }

    void "test settings.gradle"() {
        unzipAndTest("settings.gradle", [])
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

    public static final String ZIP_PATH = "$TestUtils.RESOURCES_PATH/geb-1.0/geb-1.0-allsources.zip";

}
