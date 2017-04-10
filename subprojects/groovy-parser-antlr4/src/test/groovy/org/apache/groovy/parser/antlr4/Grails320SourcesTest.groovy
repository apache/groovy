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
 * Add Grails 3.2.0 sources as test cases
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/10/09
 */
class Grails320SourcesTest extends GroovyTestCase {


    void "test build.gradle"() {
        unzipAndTest("build.gradle", [])
    }

    void "test buildSrc/src/main/groovy/org/grails/gradle/GrailsBuildPlugin.groovy"() {
        unzipAndTest("buildSrc/src/main/groovy/org/grails/gradle/GrailsBuildPlugin.groovy", [])
    }

    void "test gradle/assemble.gradle"() {
        unzipAndTest("gradle/assemble.gradle", [])
    }

    void "test gradle/docs.gradle"() {
        unzipAndTest("gradle/docs.gradle", [])
    }

    void "test gradle/findbugs.gradle"() {
        unzipAndTest("gradle/findbugs.gradle", [])
    }

    void "test gradle/idea.gradle"() {
        unzipAndTest("gradle/idea.gradle", [])
    }

    void "test gradle/integration-test.gradle"() {
        unzipAndTest("gradle/integration-test.gradle", [])
    }

    void "test gradle/unit-test.gradle"() {
        unzipAndTest("gradle/unit-test.gradle", [])
    }

    void "test grails-async/build.gradle"() {
        unzipAndTest("grails-async/build.gradle", [])
    }

    void "test grails-async/src/main/groovy/grails/async/DelegateAsync.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/DelegateAsync.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/Promise.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/Promise.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/PromiseFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/PromiseFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/PromiseList.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/PromiseList.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/PromiseMap.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/PromiseMap.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/Promises.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/Promises.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/decorator/PromiseDecorator.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/decorator/PromiseDecorator.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorLookupStrategy.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorLookupStrategy.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorProvider.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorProvider.groovy", [])
    }

    void "test grails-async/src/main/groovy/grails/async/factory/AbstractPromiseFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/grails/async/factory/AbstractPromiseFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/BoundPromise.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/BoundPromise.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromise.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromise.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromiseFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromiseFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromise.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromise.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromiseFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromiseFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/gpars/LoggingPoolFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/gpars/LoggingPoolFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromise.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromise.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromiseFactory.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromiseFactory.groovy", [])
    }

    void "test grails-async/src/main/groovy/org/grails/async/transform/internal/DelegateAsyncUtils.groovy"() {
        unzipAndTest("grails-async/src/main/groovy/org/grails/async/transform/internal/DelegateAsyncUtils.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/DelegateAsyncSpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/DelegateAsyncSpec.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/PromiseListSpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/PromiseListSpec.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/PromiseMapSpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/PromiseMapSpec.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/PromiseSpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/PromiseSpec.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/ReactorPromiseFactorySpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/ReactorPromiseFactorySpec.groovy", [])
    }

    void "test grails-async/src/test/groovy/grails/async/SynchronousPromiseFactorySpec.groovy"() {
        unzipAndTest("grails-async/src/test/groovy/grails/async/SynchronousPromiseFactorySpec.groovy", [])
    }

    void "test grails-bom/build.gradle"() {
        unzipAndTest("grails-bom/build.gradle", [])
    }

    void "test grails-bootstrap/build.gradle"() {
        unzipAndTest("grails-bootstrap/build.gradle", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/build/proxy/SystemPropertiesAuthenticator.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/build/proxy/SystemPropertiesAuthenticator.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/codegen/model/Model.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/codegen/model/Model.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/codegen/model/ModelBuilder.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/codegen/model/ModelBuilder.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/config/ConfigMap.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/config/ConfigMap.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/io/IOUtils.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/io/IOUtils.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/io/ResourceUtils.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/io/ResourceUtils.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/io/support/SystemOutErrCapturer.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/io/support/SystemOutErrCapturer.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/io/support/SystemStreamsRedirector.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/io/support/SystemStreamsRedirector.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/plugins/GrailsVersionUtils.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/plugins/GrailsVersionUtils.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/plugins/VersionComparator.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/plugins/VersionComparator.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/util/BuildSettings.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/util/BuildSettings.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/util/CosineSimilarity.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/util/CosineSimilarity.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/util/Described.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/util/Described.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/util/Metadata.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/util/Metadata.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/grails/util/Named.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/grails/util/Named.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/build/parsing/ScriptNameResolver.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/build/parsing/ScriptNameResolver.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/config/CodeGenConfig.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/config/CodeGenConfig.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/config/NavigableMap.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/config/NavigableMap.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/exceptions/ExceptionUtils.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/exceptions/ExceptionUtils.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/CodeSnippetPrinter.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/CodeSnippetPrinter.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/DefaultStackTracePrinter.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/DefaultStackTracePrinter.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/StackTracePrinter.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/StackTracePrinter.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/io/support/ByteArrayResource.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/io/support/ByteArrayResource.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/io/support/DevNullPrintStream.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/io/support/DevNullPrintStream.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/io/support/FactoriesLoaderSupport.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/io/support/FactoriesLoaderSupport.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/io/support/MainClassFinder.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/io/support/MainClassFinder.groovy", [])
    }

    void "test grails-bootstrap/src/main/groovy/org/grails/io/watch/FileExtensionFileChangeListener.groovy"() {
        unzipAndTest("grails-bootstrap/src/main/groovy/org/grails/io/watch/FileExtensionFileChangeListener.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/grails/build/logging/GrailsConsoleSpec.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/grails/build/logging/GrailsConsoleSpec.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/grails/config/ConfigMapSpec.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/grails/config/ConfigMapSpec.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/grails/config/GrailsConfigSpec.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/grails/config/GrailsConfigSpec.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/grails/io/IOUtilsSpec.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/grails/io/IOUtilsSpec.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/grails/util/EnvironmentTests.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/grails/util/EnvironmentTests.groovy", [])
    }

    void "test grails-bootstrap/src/test/groovy/org/grails/build/parsing/CommandLineParserSpec.groovy"() {
        unzipAndTest("grails-bootstrap/src/test/groovy/org/grails/build/parsing/CommandLineParserSpec.groovy", [])
    }

    void "test grails-codecs/build.gradle"() {
        unzipAndTest("grails-codecs/build.gradle", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/Base64CodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/Base64CodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/DigestUtils.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/DigestUtils.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/HexCodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/HexCodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5BytesCodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5BytesCodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5CodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5CodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1BytesCodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1BytesCodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1CodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1CodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256BytesCodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256BytesCodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256CodecExtensionMethods.groovy"() {
        unzipAndTest("grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256CodecExtensionMethods.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/Base64CodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/Base64CodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/HexCodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/HexCodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/MD5BytesCodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/MD5BytesCodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/MD5CodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/MD5CodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1BytesCodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1BytesCodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1CodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1CodecTests.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256BytesCodec.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256BytesCodec.groovy", [])
    }

    void "test grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256CodecTests.groovy"() {
        unzipAndTest("grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256CodecTests.groovy", [])
    }

    void "test grails-console/build.gradle"() {
        unzipAndTest("grails-console/build.gradle", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/command/GrailsApplicationContextCommandRunner.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/command/GrailsApplicationContextCommandRunner.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/console/GrailsSwingConsole.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/console/GrailsSwingConsole.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleApplicationContext.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleApplicationContext.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleWebApplicationContext.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleWebApplicationContext.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/script/GrailsApplicationScriptRunner.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/script/GrailsApplicationScriptRunner.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/shell/GrailsShell.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/shell/GrailsShell.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/shell/support/GroovyshApplicationContext.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/shell/support/GroovyshApplicationContext.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/shell/support/GroovyshWebApplicationContext.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/shell/support/GroovyshWebApplicationContext.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/support/DevelopmentGrailsApplication.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/support/DevelopmentGrailsApplication.groovy", [])
    }

    void "test grails-console/src/main/groovy/grails/ui/support/DevelopmentWebApplicationContext.groovy"() {
        unzipAndTest("grails-console/src/main/groovy/grails/ui/support/DevelopmentWebApplicationContext.groovy", [])
    }

    void "test grails-core/build.gradle"() {
        unzipAndTest("grails-core/build.gradle", [])
    }

    void "test grails-core/src/main/groovy/grails/beans/util/LazyBeanMap.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/beans/util/LazyBeanMap.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/GrailsApp.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/GrailsApp.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/GrailsAppBuilder.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/GrailsAppBuilder.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/GrailsPluginApplication.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/GrailsPluginApplication.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/config/GrailsApplicationPostProcessor.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/config/GrailsApplicationPostProcessor.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/config/GrailsAutoConfiguration.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/config/GrailsAutoConfiguration.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/config/tools/ProfilingGrailsApplicationPostProcessor.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/config/tools/ProfilingGrailsApplicationPostProcessor.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/boot/config/tools/SettingsFile.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/boot/config/tools/SettingsFile.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/compiler/DelegatingMethod.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/compiler/DelegatingMethod.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/compiler/GrailsCompileStatic.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/compiler/GrailsCompileStatic.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/compiler/GrailsTypeChecked.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/compiler/GrailsTypeChecked.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjector.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjector.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjectorAdapter.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjectorAdapter.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/config/Config.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/config/Config.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/config/ConfigProperties.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/config/ConfigProperties.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/config/Settings.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/config/Settings.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/core/GrailsApplicationClass.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/core/GrailsApplicationClass.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycle.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycle.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycleAdapter.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycleAdapter.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/core/events/ArtefactAdditionEvent.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/core/events/ArtefactAdditionEvent.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/Support.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/Support.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/ApplicationCommand.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/ApplicationCommand.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/ApplicationContextCommandRegistry.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/ApplicationContextCommandRegistry.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/ExecutionContext.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/ExecutionContext.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/GrailsApplicationCommand.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/GrailsApplicationCommand.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/io/FileSystemInteraction.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/io/FileSystemInteraction.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/io/FileSystemInteractionImpl.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/io/FileSystemInteractionImpl.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/template/TemplateException.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/template/TemplateException.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/template/TemplateRenderer.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/template/TemplateRenderer.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/dev/commands/template/TemplateRendererImpl.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/dev/commands/template/TemplateRendererImpl.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/persistence/support/PersistenceContextInterceptorExecutor.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/persistence/support/PersistenceContextInterceptorExecutor.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/plugins/Plugin.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/plugins/Plugin.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/plugins/PluginManagerLoader.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/plugins/PluginManagerLoader.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/transaction/GrailsTransactionTemplate.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/transaction/GrailsTransactionTemplate.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/transaction/Rollback.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/transaction/Rollback.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/util/GrailsArrayUtils.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/util/GrailsArrayUtils.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/util/GrailsStringUtils.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/util/GrailsStringUtils.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/util/MixinTargetAware.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/util/MixinTargetAware.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/util/TypeConvertingMap.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/util/TypeConvertingMap.groovy", [])
    }

    void "test grails-core/src/main/groovy/grails/validation/ValidationErrors.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/grails/validation/ValidationErrors.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/boot/internal/JavaCompiler.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/boot/internal/JavaCompiler.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/CriteriaTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/CriteriaTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/DomainMappingTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/DomainMappingTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/DynamicFinderTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/DynamicFinderTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/HttpServletRequestTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/HttpServletRequestTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/RelationshipManagementMethodTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/RelationshipManagementMethodTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/ValidateableTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/ValidateableTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/WhereQueryTypeCheckingExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/WhereQueryTypeCheckingExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/ApplicationClassInjector.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/ApplicationClassInjector.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/EnhancesTraitTransformation.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/EnhancesTraitTransformation.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformation.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformation.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/GlobalImportTransformation.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/GlobalImportTransformation.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/GroovyEclipseCompilationHelper.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/GroovyEclipseCompilationHelper.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/compiler/injection/TraitInjectionSupport.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/compiler/injection/TraitInjectionSupport.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/config/NavigableMapPropertySource.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/config/NavigableMapPropertySource.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/config/PrefixedMapPropertySource.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/config/PrefixedMapPropertySource.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/config/yaml/YamlPropertySourceLoader.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/config/yaml/YamlPropertySourceLoader.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/artefact/ApplicationArtefactHandler.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/artefact/ApplicationArtefactHandler.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/cfg/GroovyConfigPropertySourceLoader.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/cfg/GroovyConfigPropertySourceLoader.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/exceptions/DefaultErrorsPrinter.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/exceptions/DefaultErrorsPrinter.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/io/CachingPathMatchingResourcePatternResolver.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/io/CachingPathMatchingResourcePatternResolver.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/io/GrailsResource.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/io/GrailsResource.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/io/support/GrailsFactoriesLoader.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/io/support/GrailsFactoriesLoader.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/metaclass/MetaClassEnhancer.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/metaclass/MetaClassEnhancer.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/support/GrailsApplicationDiscoveryStrategy.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/support/GrailsApplicationDiscoveryStrategy.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/core/util/IncludeExcludeSupport.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/core/util/IncludeExcludeSupport.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/dev/support/DevelopmentShutdownHook.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/dev/support/DevelopmentShutdownHook.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/plugins/CoreGrailsPlugin.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/plugins/CoreGrailsPlugin.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/plugins/support/WatchPattern.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/plugins/support/WatchPattern.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/spring/beans/factory/HotSwappableTargetSourceFactoryBean.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/spring/beans/factory/HotSwappableTargetSourceFactoryBean.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/spring/context/ApplicationContextExtension.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/spring/context/ApplicationContextExtension.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/spring/context/support/MapBasedSmartPropertyOverrideConfigurer.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/spring/context/support/MapBasedSmartPropertyOverrideConfigurer.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/transaction/transform/RollbackTransform.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/transaction/transform/RollbackTransform.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/transaction/transform/TransactionalTransform.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/transaction/transform/TransactionalTransform.groovy", [])
    }

    void "test grails-core/src/main/groovy/org/grails/validation/ConstraintEvalUtils.groovy"() {
        unzipAndTest("grails-core/src/main/groovy/org/grails/validation/ConstraintEvalUtils.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/artefact/ApiDelegateSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/artefact/ApiDelegateSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/artefact/EnhancesSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/artefact/EnhancesSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/config/ConfigPropertiesSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/config/ConfigPropertiesSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/spring/GrailsPlaceHolderConfigurerCorePluginRuntimeSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/spring/GrailsPlaceHolderConfigurerCorePluginRuntimeSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/spring/GrailsPlaceholderConfigurerSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/spring/GrailsPlaceholderConfigurerSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/transaction/TransactionalTransformSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/transaction/TransactionalTransformSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/util/GrailsArrayUtilsSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/util/GrailsArrayUtilsSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/util/GrailsMetaClassUtilsSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/util/GrailsMetaClassUtilsSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/util/GrailsStringUtilsSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/util/GrailsStringUtilsSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/web/CamelCaseUrlConverterSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/web/CamelCaseUrlConverterSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/grails/web/HyphenatedUrlConverterSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/grails/web/HyphenatedUrlConverterSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/ASTValidationErrorsHelperSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/ASTValidationErrorsHelperSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/ArtefactTypeAstTransformationSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/ArtefactTypeAstTransformationSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/DefaultDomainClassInjectorSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/DefaultDomainClassInjectorSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformationSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformationSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/GrailsASTUtilsSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/GrailsASTUtilsSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/GrailsASTUtilsTests.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/GrailsASTUtilsTests.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/compiler/injection/GrailsArtefactTransformerSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/compiler/injection/GrailsArtefactTransformerSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/config/NavigableMapNestedEqualitySpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/config/NavigableMapNestedEqualitySpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/config/NavigableMapPropertySourceSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/config/NavigableMapPropertySourceSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/config/PropertySourcesConfigSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/config/PropertySourcesConfigSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/config/YamlPropertySourceLoaderSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/config/YamlPropertySourceLoaderSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/core/DefaultGrailsControllerClassSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/core/DefaultGrailsControllerClassSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/core/io/ResourceLocatorSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/core/io/ResourceLocatorSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/exception/reporting/StackTraceFiltererSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/exception/reporting/StackTraceFiltererSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/exception/reporting/StackTracePrinterSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/exception/reporting/StackTracePrinterSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/plugins/BinaryPluginSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/plugins/BinaryPluginSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/plugins/GrailsPluginTests.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/plugins/GrailsPluginTests.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/plugins/support/WatchPatternParserSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/plugins/support/WatchPatternParserSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/spring/context/ApplicationContextExtensionSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/spring/context/ApplicationContextExtensionSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/transaction/ChainedTransactionManagerPostProcessorSpec.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/transaction/ChainedTransactionManagerPostProcessorSpec.groovy", [])
    }

    void "test grails-core/src/test/groovy/org/grails/util/TypeConvertingMapTests.groovy"() {
        unzipAndTest("grails-core/src/test/groovy/org/grails/util/TypeConvertingMapTests.groovy", [])
    }

    void "test grails-databinding/build.gradle"() {
        unzipAndTest("grails-databinding/build.gradle", [])
    }

    void "test grails-databinding/src/main/groovy/grails/databinding/SimpleDataBinder.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/grails/databinding/SimpleDataBinder.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/grails/databinding/SimpleMapDataBindingSource.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/grails/databinding/SimpleMapDataBindingSource.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/ClosureValueConverter.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/ClosureValueConverter.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/IndexedPropertyReferenceDescriptor.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/IndexedPropertyReferenceDescriptor.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/AbstractStructuredDateBindingEditor.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/AbstractStructuredDateBindingEditor.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/CurrencyValueConverter.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/CurrencyValueConverter.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/DateConversionHelper.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/DateConversionHelper.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/FormattedDateValueConverter.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/FormattedDateValueConverter.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredCalendarBindingEditor.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredCalendarBindingEditor.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredDateBindingEditor.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredDateBindingEditor.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredSqlDateBindingEditor.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredSqlDateBindingEditor.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/converters/TimeZoneConverter.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/converters/TimeZoneConverter.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultCollectionDataBindingSource.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultCollectionDataBindingSource.groovy", [])
    }

    void "test grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultMap.groovy"() {
        unzipAndTest("grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultMap.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/BindUsingSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/BindUsingSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/BindingErrorSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/BindingErrorSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/BindingFormatSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/BindingFormatSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/BindingListenerSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/BindingListenerSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/CollectionBindingSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/CollectionBindingSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/CustomTypeConverterSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/CustomTypeConverterSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/IncludeExcludeBindingSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/IncludeExcludeBindingSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumBindingSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumBindingSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumValueConverterSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumValueConverterSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/grails/databinding/XMLBindingSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/grails/databinding/XMLBindingSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/org/grails/databinding/compiler/BindingFormatCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/org/grails/databinding/compiler/BindingFormatCompilationErrorsSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/org/grails/databinding/converters/CurrencyConversionSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/org/grails/databinding/converters/CurrencyConversionSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/org/grails/databinding/converters/DateConversionHelperSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/org/grails/databinding/converters/DateConversionHelperSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathCollectionDataBindingSourceSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathCollectionDataBindingSourceSpec.groovy", [])
    }

    void "test grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathResultMapSpec.groovy"() {
        unzipAndTest("grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathResultMapSpec.groovy", [])
    }

    void "test grails-dependencies/build.gradle"() {
        unzipAndTest("grails-dependencies/build.gradle", [])
    }

    void "test grails-docs/build.gradle"() {
        unzipAndTest("grails-docs/build.gradle", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/DocEngine.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/DocEngine.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/DocPublisher.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/DocPublisher.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/LegacyDocMigrator.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/LegacyDocMigrator.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/PdfBuilder.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/PdfBuilder.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/ant/DocPublisherTask.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/ant/DocPublisherTask.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/asciidoc/AsciiDocEngine.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/asciidoc/AsciiDocEngine.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/filters/HeaderFilter.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/filters/HeaderFilter.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/filters/LinkTestFilter.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/filters/LinkTestFilter.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/filters/ListFilter.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/filters/ListFilter.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/gradle/MigrateLegacyDocs.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/gradle/MigrateLegacyDocs.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/gradle/PublishGuide.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/gradle/PublishGuide.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/gradle/PublishPdf.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/gradle/PublishPdf.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/internal/FileResourceChecker.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/internal/FileResourceChecker.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/internal/LegacyTocStrategy.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/internal/LegacyTocStrategy.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/internal/UserGuideNode.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/internal/UserGuideNode.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/internal/YamlTocStrategy.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/internal/YamlTocStrategy.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/macros/GspTagSourceMacro.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/macros/GspTagSourceMacro.groovy", [])
    }

    void "test grails-docs/src/main/groovy/grails/doc/macros/HiddenMacro.groovy"() {
        unzipAndTest("grails-docs/src/main/groovy/grails/doc/macros/HiddenMacro.groovy", [])
    }

    void "test grails-docs/src/test/groovy/grails/doc/internal/LegacyTocStrategySpec.groovy"() {
        unzipAndTest("grails-docs/src/test/groovy/grails/doc/internal/LegacyTocStrategySpec.groovy", [])
    }

    void "test grails-docs/src/test/groovy/grails/doc/internal/StringEscapeCategoryTests.groovy"() {
        unzipAndTest("grails-docs/src/test/groovy/grails/doc/internal/StringEscapeCategoryTests.groovy", [])
    }

    void "test grails-docs/src/test/groovy/grails/doc/internal/YamlTocStrategySpec.groovy"() {
        unzipAndTest("grails-docs/src/test/groovy/grails/doc/internal/YamlTocStrategySpec.groovy", [])
    }

    void "test grails-docs/src/test/groovy/grails/doc/macros/GspTagSourceMacroTest.groovy"() {
        unzipAndTest("grails-docs/src/test/groovy/grails/doc/macros/GspTagSourceMacroTest.groovy", [])
    }

    void "test grails-encoder/build.gradle"() {
        unzipAndTest("grails-encoder/build.gradle", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/buffer/StreamCharBufferMetaUtils.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/buffer/StreamCharBufferMetaUtils.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/CodecMetaClassSupport.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/CodecMetaClassSupport.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/impl/HTMLCodecFactory.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/impl/HTMLCodecFactory.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/impl/JSONCodecFactory.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/impl/JSONCodecFactory.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/impl/JavaScriptCodec.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/impl/JavaScriptCodec.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/impl/StandaloneCodecLookup.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/impl/StandaloneCodecLookup.groovy", [])
    }

    void "test grails-encoder/src/main/groovy/org/grails/encoder/impl/URLCodecFactory.groovy"() {
        unzipAndTest("grails-encoder/src/main/groovy/org/grails/encoder/impl/URLCodecFactory.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/buffer/StreamCharBufferGroovyTests.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/buffer/StreamCharBufferGroovyTests.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/charsequences/CharSequencesSpec.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/charsequences/CharSequencesSpec.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/encoder/ChainedEncodersSpec.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/encoder/ChainedEncodersSpec.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/encoder/impl/BasicCodecLookupSpec.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/encoder/impl/BasicCodecLookupSpec.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/encoder/impl/HTMLEncoderSpec.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/encoder/impl/HTMLEncoderSpec.groovy", [])
    }

    void "test grails-encoder/src/test/groovy/org/grails/encoder/impl/JavaScriptCodecTests.groovy"() {
        unzipAndTest("grails-encoder/src/test/groovy/org/grails/encoder/impl/JavaScriptCodecTests.groovy", [])
    }

    void "test grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/DefaultGrailsClasspath.groovy"() {
        unzipAndTest("grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/DefaultGrailsClasspath.groovy", [])
    }

    void "test grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspath.groovy"() {
        unzipAndTest("grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspath.groovy", [])
    }

    void "test grails-gradle-plugin/build.gradle"() {
        unzipAndTest("grails-gradle-plugin/build.gradle", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/agent/AgentTasksEnhancer.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/agent/AgentTasksEnhancer.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/commands/ApplicationContextCommandTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/commands/ApplicationContextCommandTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/commands/ApplicationContextScriptTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/commands/ApplicationContextScriptTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsExtension.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsExtension.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsPluginGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsPluginGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/IntegrationTestGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/IntegrationTestGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/PluginDefiner.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/PluginDefiner.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/GrailsDocGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/GrailsDocGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/PublishGuideTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/PublishGuideTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspathToolingModelBuilder.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspathToolingModelBuilder.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfileGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfileGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfilePublishGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfilePublishGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/tasks/ProfileCompilerTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/tasks/ProfileCompilerTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsCentralPublishGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsCentralPublishGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsPublishExtension.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsPublishExtension.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/FindMainClassTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/FindMainClassTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/GrailsRunTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/GrailsRunTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/util/SourceSets.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/util/SourceSets.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/GrailsWatchPlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/GrailsWatchPlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/WatchConfig.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/WatchConfig.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/GrailsWebGradlePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/GrailsWebGradlePlugin.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPageCompileTask.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPageCompileTask.groovy", [])
    }

    void "test grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPagePlugin.groovy"() {
        unzipAndTest("grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPagePlugin.groovy", [])
    }

    void "test grails-gsp/build.gradle"() {
        unzipAndTest("grails-gsp/build.gradle", [])
    }

    void "test grails-gsp/src/main/groovy/org/grails/gsp/GroovyPagesMetaUtils.groovy"() {
        unzipAndTest("grails-gsp/src/main/groovy/org/grails/gsp/GroovyPagesMetaUtils.groovy", [])
    }

    void "test grails-gsp/src/main/groovy/org/grails/gsp/compiler/GroovyPageCompiler.groovy"() {
        unzipAndTest("grails-gsp/src/main/groovy/org/grails/gsp/compiler/GroovyPageCompiler.groovy", [])
    }

    void "test grails-gsp/src/test/groovy/org/grails/gsp/GroovyPagesTemplateEngineTests.groovy"() {
        unzipAndTest("grails-gsp/src/test/groovy/org/grails/gsp/GroovyPagesTemplateEngineTests.groovy", [])
    }

    void "test grails-logging/build.gradle"() {
        unzipAndTest("grails-logging/build.gradle", [])
    }

    void "test grails-logging/src/test/groovy/org/grails/compiler/logging/LoggingTransformerSpec.groovy"() {
        unzipAndTest("grails-logging/src/test/groovy/org/grails/compiler/logging/LoggingTransformerSpec.groovy", [])
    }

    void "test grails-plugin-async/build.gradle"() {
        unzipAndTest("grails-plugin-async/build.gradle", [])
    }

    void "test grails-plugin-async/src/main/groovy/grails/artefact/AsyncController.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/grails/artefact/AsyncController.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/grails/async/services/PersistenceContextPromiseDecorator.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/grails/async/services/PersistenceContextPromiseDecorator.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/grails/async/services/TransactionalPromiseDecorator.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/grails/async/services/TransactionalPromiseDecorator.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/grails/async/web/AsyncGrailsWebRequest.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/grails/async/web/AsyncGrailsWebRequest.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/grails/compiler/traits/AsyncControllerTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/grails/compiler/traits/AsyncControllerTraitInjector.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/async/transform/internal/DefaultDelegateAsyncTransactionalMethodTransformer.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/async/transform/internal/DefaultDelegateAsyncTransactionalMethodTransformer.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/compiler/web/async/TransactionalAsyncTransformUtils.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/compiler/web/async/TransactionalAsyncTransformUtils.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/ControllersAsyncGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/ControllersAsyncGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/GrailsAsyncContext.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/GrailsAsyncContext.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecorator.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecorator.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecoratorLookupStrategy.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecoratorLookupStrategy.groovy", [])
    }

    void "test grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/mvc/AsyncActionResultTransformer.groovy"() {
        unzipAndTest("grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/mvc/AsyncActionResultTransformer.groovy", [])
    }

    void "test grails-plugin-async/src/test/groovy/grails/async/services/AsyncTransactionalServiceSpec.groovy"() {
        unzipAndTest("grails-plugin-async/src/test/groovy/grails/async/services/AsyncTransactionalServiceSpec.groovy", [])
    }

    void "test grails-plugin-codecs/build.gradle"() {
        unzipAndTest("grails-plugin-codecs/build.gradle", [])
    }

    void "test grails-plugin-codecs/src/main/groovy/org/grails/plugins/CodecsGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/main/groovy/org/grails/plugins/CodecsGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-codecs/src/main/groovy/org/grails/plugins/codecs/URLCodec.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/main/groovy/org/grails/plugins/codecs/URLCodec.groovy", [])
    }

    void "test grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLCodecTests.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLCodecTests.groovy", [])
    }

    void "test grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLJSCodecSpec.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLJSCodecSpec.groovy", [])
    }

    void "test grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/JSONEncoderSpec.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/JSONEncoderSpec.groovy", [])
    }

    void "test grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/URLCodecTests.groovy"() {
        unzipAndTest("grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/URLCodecTests.groovy", [])
    }

    void "test grails-plugin-controllers/build.gradle"() {
        unzipAndTest("grails-plugin-controllers/build.gradle", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/artefact/Controller.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/artefact/Controller.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/AllowedMethodsHelper.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/AllowedMethodsHelper.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/RequestForwarder.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/RequestForwarder.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRedirector.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRedirector.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRenderer.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRenderer.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/compiler/traits/ControllerTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/compiler/traits/ControllerTraitInjector.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/grails/web/Controller.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/grails/web/Controller.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/ControllersGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/ControllersGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/DefaultControllerExceptionHandlerMetaData.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/DefaultControllerExceptionHandlerMetaData.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/context/BootStrapClassRunner.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/context/BootStrapClassRunner.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/InvalidResponseHandler.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/InvalidResponseHandler.groovy", [])
    }

    void "test grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/ValidResponseHandler.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/ValidResponseHandler.groovy", [])
    }

    void "test grails-plugin-controllers/src/test/groovy/grails/artefact/controller/support/AllowedMethodsHelperSpec.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/test/groovy/grails/artefact/controller/support/AllowedMethodsHelperSpec.groovy", [])
    }

    void "test grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerClosureActionOverridingSpec.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerClosureActionOverridingSpec.groovy", [])
    }

    void "test grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerCompilationErrorsSpec.groovy", [])
    }

    void "test grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerSpec.groovy"() {
        unzipAndTest("grails-plugin-controllers/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerSpec.groovy", [])
    }

    void "test grails-plugin-converters/build.gradle"() {
        unzipAndTest("grails-plugin-converters/build.gradle", [])
    }

    void "test grails-plugin-converters/src/main/groovy/grails/web/JSONBuilder.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/grails/web/JSONBuilder.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/plugins/converters/ConvertersGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/plugins/converters/ConvertersGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/web/converters/AbstractParsingParameterCreationListener.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/web/converters/AbstractParsingParameterCreationListener.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConfigurableConverter.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConfigurableConverter.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConvertersExtension.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConvertersExtension.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/web/converters/IncludeExcludeConverter.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/web/converters/IncludeExcludeConverter.groovy", [])
    }

    void "test grails-plugin-converters/src/main/groovy/org/grails/web/converters/configuration/configtest.groovy"() {
        unzipAndTest("grails-plugin-converters/src/main/groovy/org/grails/web/converters/configuration/configtest.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/grails/converters/ParsingNullJsonValuesSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/grails/converters/ParsingNullJsonValuesSpec.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/org/grails/compiler/web/converters/ConvertersDomainTransformerSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/org/grails/compiler/web/converters/ConvertersDomainTransformerSpec.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/org/grails/plugins/converters/api/ConvertersApiSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/org/grails/plugins/converters/api/ConvertersApiSpec.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/org/grails/web/converters/ConverterUtilSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/org/grails/web/converters/ConverterUtilSpec.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/DomainClassMarshallerSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/DomainClassMarshallerSpec.groovy", [])
    }

    void "test grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/ValidationErrorsMarshallerSpec.groovy"() {
        unzipAndTest("grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/ValidationErrorsMarshallerSpec.groovy", [])
    }

    void "test grails-plugin-databinding/build.gradle"() {
        unzipAndTest("grails-plugin-databinding/build.gradle", [])
    }

    void "test grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareBigDecimalConverter.groovy"() {
        unzipAndTest("grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareBigDecimalConverter.groovy", [])
    }

    void "test grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareNumberConverter.groovy"() {
        unzipAndTest("grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareNumberConverter.groovy", [])
    }

    void "test grails-plugin-databinding/src/main/groovy/org/grails/plugins/databinding/DataBindingGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-databinding/src/main/groovy/org/grails/plugins/databinding/DataBindingGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-datasource/build.gradle"() {
        unzipAndTest("grails-plugin-datasource/build.gradle", [])
    }

    void "test grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceUtils.groovy"() {
        unzipAndTest("grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceUtils.groovy", [])
    }

    void "test grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/EmbeddedDatabaseShutdownHook.groovy"() {
        unzipAndTest("grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/EmbeddedDatabaseShutdownHook.groovy", [])
    }

    void "test grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/TomcatJDBCPoolMBeanExporter.groovy"() {
        unzipAndTest("grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/TomcatJDBCPoolMBeanExporter.groovy", [])
    }

    void "test grails-plugin-domain-class/build.gradle"() {
        unzipAndTest("grails-plugin-domain-class/build.gradle", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/grails/artefact/DomainClass.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/grails/artefact/DomainClass.groovy", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/grails/compiler/traits/DomainClassTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/grails/compiler/traits/DomainClassTraitInjector.groovy", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassPluginSupport.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassPluginSupport.groovy", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GormApiSupport.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GormApiSupport.groovy", [])
    }

    void "test grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GrailsDomainClassCleaner.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GrailsDomainClassCleaner.groovy", [])
    }

    void "test grails-plugin-domain-class/src/test/groovy/grails/persistence/CircularBidirectionalMapBySpec.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/test/groovy/grails/persistence/CircularBidirectionalMapBySpec.groovy", [])
    }

    void "test grails-plugin-domain-class/src/test/groovy/grails/persistence/DomainClassTraitSpec.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/test/groovy/grails/persistence/DomainClassTraitSpec.groovy", [])
    }

    void "test grails-plugin-domain-class/src/test/groovy/grails/persistence/EntityTransformIncludesGormApiSpec.groovy"() {
        unzipAndTest("grails-plugin-domain-class/src/test/groovy/grails/persistence/EntityTransformIncludesGormApiSpec.groovy", [])
    }

    void "test grails-plugin-events/build.gradle"() {
        unzipAndTest("grails-plugin-events/build.gradle", [])
    }

    void "test grails-plugin-events/src/main/groovy/grails/events/Events.groovy"() {
        unzipAndTest("grails-plugin-events/src/main/groovy/grails/events/Events.groovy", [])
    }

    void "test grails-plugin-events/src/main/groovy/org/grails/events/ClosureEventConsumer.groovy"() {
        unzipAndTest("grails-plugin-events/src/main/groovy/org/grails/events/ClosureEventConsumer.groovy", [])
    }

    void "test grails-plugin-events/src/main/groovy/org/grails/events/reactor/GrailsReactorConfigurationReader.groovy"() {
        unzipAndTest("grails-plugin-events/src/main/groovy/org/grails/events/reactor/GrailsReactorConfigurationReader.groovy", [])
    }

    void "test grails-plugin-events/src/main/groovy/org/grails/events/spring/SpringEventTranslator.groovy"() {
        unzipAndTest("grails-plugin-events/src/main/groovy/org/grails/events/spring/SpringEventTranslator.groovy", [])
    }

    void "test grails-plugin-events/src/main/groovy/org/grails/plugins/events/EventBusGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-events/src/main/groovy/org/grails/plugins/events/EventBusGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-events/src/test/groovy/grails/events/EventsTraitSpec.groovy"() {
        unzipAndTest("grails-plugin-events/src/test/groovy/grails/events/EventsTraitSpec.groovy", [])
    }

    void "test grails-plugin-events/src/test/groovy/grails/events/SpringEventTranslatorSpec.groovy"() {
        unzipAndTest("grails-plugin-events/src/test/groovy/grails/events/SpringEventTranslatorSpec.groovy", [])
    }

    void "test grails-plugin-events/src/test/groovy/org/grails/events/reactor/GrailsReactorConfigurationReaderSpec.groovy"() {
        unzipAndTest("grails-plugin-events/src/test/groovy/org/grails/events/reactor/GrailsReactorConfigurationReaderSpec.groovy", [])
    }

    void "test grails-plugin-gsp/build.gradle"() {
        unzipAndTest("grails-plugin-gsp/build.gradle", [])
    }

    void "test grails-plugin-gsp/src/ast/groovy/grails/compiler/traits/ControllerTagLibraryTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/ast/groovy/grails/compiler/traits/ControllerTagLibraryTraitInjector.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GrailsLayoutViewResolverPostProcessor.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GrailsLayoutViewResolverPostProcessor.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GroovyPagesGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GroovyPagesGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ApplicationTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ApplicationTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/CountryTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/CountryTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormatTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormatTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/JavascriptTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/JavascriptTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/PluginTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/PluginTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/UrlMappingTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/UrlMappingTagLib.groovy", [])
    }

    void "test grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ValidationTagLib.groovy"() {
        unzipAndTest("grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ValidationTagLib.groovy", [])
    }

    void "test grails-plugin-i18n/build.gradle"() {
        unzipAndTest("grails-plugin-i18n/build.gradle", [])
    }

    void "test grails-plugin-i18n/src/main/groovy/org/grails/plugins/i18n/I18nGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-i18n/src/main/groovy/org/grails/plugins/i18n/I18nGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-interceptors/build.gradle"() {
        unzipAndTest("grails-plugin-interceptors/build.gradle", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/grails/artefact/Interceptor.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/grails/artefact/Interceptor.groovy", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/grails/compiler/traits/InterceptorTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/grails/compiler/traits/InterceptorTraitInjector.groovy", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/grails/interceptors/Matcher.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/grails/interceptors/Matcher.groovy", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/GrailsInterceptorHandlerInterceptorAdapter.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/GrailsInterceptorHandlerInterceptorAdapter.groovy", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/InterceptorsGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/InterceptorsGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcher.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcher.groovy", [])
    }

    void "test grails-plugin-interceptors/src/test/groovy/grails/artefact/GrailsInterceptorHandlerInterceptorAdapterSpec.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/test/groovy/grails/artefact/GrailsInterceptorHandlerInterceptorAdapterSpec.groovy", [])
    }

    void "test grails-plugin-interceptors/src/test/groovy/grails/artefact/InterceptorSpec.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/test/groovy/grails/artefact/InterceptorSpec.groovy", [])
    }

    void "test grails-plugin-interceptors/src/test/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcherSpec.groovy"() {
        unzipAndTest("grails-plugin-interceptors/src/test/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcherSpec.groovy", [])
    }

    void "test grails-plugin-mimetypes/build.gradle"() {
        unzipAndTest("grails-plugin-mimetypes/build.gradle", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/grails/web/mime/AcceptHeaderParser.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/grails/web/mime/AcceptHeaderParser.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/api/MimeTypesApiSupport.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/api/MimeTypesApiSupport.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/FormatInterceptor.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/FormatInterceptor.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesFactoryBean.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesFactoryBean.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultAcceptHeaderParser.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultAcceptHeaderParser.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultMimeTypeResolver.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultMimeTypeResolver.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletRequestExtension.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletRequestExtension.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletResponseExtension.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletResponseExtension.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/test/groovy/grails/web/mime/MimeUtilitySpec.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/test/groovy/grails/web/mime/MimeUtilitySpec.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/test/groovy/org/grails/web/mime/AcceptHeaderParserTests.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/test/groovy/org/grails/web/mime/AcceptHeaderParserTests.groovy", [])
    }

    void "test grails-plugin-mimetypes/src/test/groovy/org/grails/web/servlet/mvc/RequestAndResponseMimeTypesApiSpec.groovy"() {
        unzipAndTest("grails-plugin-mimetypes/src/test/groovy/org/grails/web/servlet/mvc/RequestAndResponseMimeTypesApiSpec.groovy", [])
    }

    void "test grails-plugin-rest/build.gradle"() {
        unzipAndTest("grails-plugin-rest/build.gradle", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/compiler/traits/RestResponderTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/compiler/traits/RestResponderTraitInjector.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/Link.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/Link.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/Linkable.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/Linkable.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/Resource.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/Resource.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/RestfulController.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/RestfulController.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractIncludeExcludeRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractIncludeExcludeRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderContext.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderContext.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/ContainerRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/ContainerRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/RenderContext.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/RenderContext.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/Renderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/Renderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/RendererRegistry.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/RendererRegistry.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomCollectionRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomCollectionRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/errors/AbstractVndErrorRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/errors/AbstractVndErrorRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorJsonRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorJsonRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorXmlRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorXmlRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonCollectionRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonCollectionRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlCollectionRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlCollectionRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonCollectionRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonCollectionRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/util/AbstractLinkingRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/util/AbstractLinkingRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlCollectionRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlCollectionRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/plugin/RestResponderGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/plugin/RestResponderGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistry.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistry.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/ServletRenderContext.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/ServletRenderContext.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/html/DefaultHtmlRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/html/DefaultHtmlRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/json/DefaultJsonRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/json/DefaultJsonRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/xml/DefaultXmlRenderer.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/xml/DefaultXmlRenderer.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/LinkableTransform.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/LinkableTransform.groovy", [])
    }

    void "test grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/ResourceTransform.groovy"() {
        unzipAndTest("grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/ResourceTransform.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistrySpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistrySpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/VndErrorRenderingSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/VndErrorRenderingSpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalJsonRendererSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalJsonRendererSpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/html/HtmlRendererSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/html/HtmlRendererSpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/json/JsonRendererSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/json/JsonRendererSpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/LinkableTransformSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/LinkableTransformSpec.groovy", [])
    }

    void "test grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/ResourceTransformSpec.groovy"() {
        unzipAndTest("grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/ResourceTransformSpec.groovy", [])
    }

    void "test grails-plugin-services/build.gradle"() {
        unzipAndTest("grails-plugin-services/build.gradle", [])
    }

    void "test grails-plugin-services/src/main/groovy/grails/artefact/Service.groovy"() {
        unzipAndTest("grails-plugin-services/src/main/groovy/grails/artefact/Service.groovy", [])
    }

    void "test grails-plugin-services/src/main/groovy/grails/compiler/traits/ServiceTraitInjector.groovy"() {
        unzipAndTest("grails-plugin-services/src/main/groovy/grails/compiler/traits/ServiceTraitInjector.groovy", [])
    }

    void "test grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServiceBeanAliasPostProcessor.groovy"() {
        unzipAndTest("grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServiceBeanAliasPostProcessor.groovy", [])
    }

    void "test grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServicesGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServicesGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-testing/build.gradle"() {
        unzipAndTest("grails-plugin-testing/build.gradle", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/TestMixinTargetAware.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/TestMixinTargetAware.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/DomainClassUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/DomainClassUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/MockCascadingDomainClassValidator.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/MockCascadingDomainClassValidator.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/Integration.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/Integration.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/IntegrationTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/IntegrationTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/services/ServiceUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/services/ServiceUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/support/GrailsUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/support/GrailsUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistrar.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistrar.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistry.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistry.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/web/ControllerUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/web/ControllerUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/web/GroovyPageUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/web/GroovyPageUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/web/InterceptorUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/web/InterceptorUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/web/UrlMappingsUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/web/UrlMappingsUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestMixin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestMixin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestSupport.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestSupport.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/ControllerTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/ControllerTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/CoreBeansTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/CoreBeansTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/DefaultSharedRuntimeConfigurer.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/DefaultSharedRuntimeConfigurer.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/DirtiesRuntime.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/DirtiesRuntime.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/DomainClassTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/DomainClassTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/FreshRuntime.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/FreshRuntime.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/GrailsApplicationTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/GrailsApplicationTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/GroovyPageTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/GroovyPageTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/InterceptorTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/InterceptorTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/MetaClassCleanerTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/MetaClassCleanerTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntime.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntime.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntimeConfigurer.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntimeConfigurer.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEvent.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEvent.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEventInterceptor.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEventInterceptor.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPluginUsage.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPluginUsage.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntime.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntime.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeFactory.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeFactory.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeJunitAdapter.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeJunitAdapter.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeSettings.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeSettings.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeUtil.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeUtil.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/grails/test/runtime/WebFlowTestPlugin.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/grails/test/runtime/WebFlowTestPlugin.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/org/grails/compiler/injection/test/IntegrationTestMixinTransformation.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/org/grails/compiler/injection/test/IntegrationTestMixinTransformation.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/org/grails/test/context/junit4/GrailsJunit4ClassRunner.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/org/grails/test/context/junit4/GrailsJunit4ClassRunner.groovy", [])
    }

    void "test grails-plugin-testing/src/main/groovy/org/grails/test/mixin/support/DefaultTestMixinRegistrar.groovy"() {
        unzipAndTest("grails-plugin-testing/src/main/groovy/org/grails/test/mixin/support/DefaultTestMixinRegistrar.groovy", [])
    }

    void "test grails-plugin-testing/src/test/groovy/grails/test/mixin/MetaClassCleanupSpec.groovy"() {
        unzipAndTest("grails-plugin-testing/src/test/groovy/grails/test/mixin/MetaClassCleanupSpec.groovy", [])
    }

    void "test grails-plugin-testing/src/test/groovy/grails/test/mixin/TestForSpec.groovy"() {
        unzipAndTest("grails-plugin-testing/src/test/groovy/grails/test/mixin/TestForSpec.groovy", [])
    }

    void "test grails-plugin-testing/src/test/groovy/grails/test/mixin/TestMixinSpec.groovy"() {
        unzipAndTest("grails-plugin-testing/src/test/groovy/grails/test/mixin/TestMixinSpec.groovy", [])
    }

    void "test grails-plugin-testing/src/test/groovy/grails/test/mixin/integration/compiler/IntegrationTestMixinCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-plugin-testing/src/test/groovy/grails/test/mixin/integration/compiler/IntegrationTestMixinCompilationErrorsSpec.groovy", [])
    }

    void "test grails-plugin-testing/src/test/groovy/grails/test/runtime/TestRuntimeFactorySpec.groovy"() {
        unzipAndTest("grails-plugin-testing/src/test/groovy/grails/test/runtime/TestRuntimeFactorySpec.groovy", [])
    }

    void "test grails-plugin-url-mappings/build.gradle"() {
        unzipAndTest("grails-plugin-url-mappings/build.gradle", [])
    }

    void "test grails-plugin-url-mappings/src/main/groovy/org/grails/plugins/web/mapping/UrlMappingsGrailsPlugin.groovy"() {
        unzipAndTest("grails-plugin-url-mappings/src/main/groovy/org/grails/plugins/web/mapping/UrlMappingsGrailsPlugin.groovy", [])
    }

    void "test grails-plugin-validation/build.gradle"() {
        unzipAndTest("grails-plugin-validation/build.gradle", [])
    }

    void "test grails-plugin-validation/src/main/groovy/grails/validation/Validateable.groovy"() {
        unzipAndTest("grails-plugin-validation/src/main/groovy/grails/validation/Validateable.groovy", [])
    }

    void "test grails-plugin-validation/src/main/groovy/org/grails/web/plugins/support/ValidationSupport.groovy"() {
        unzipAndTest("grails-plugin-validation/src/main/groovy/org/grails/web/plugins/support/ValidationSupport.groovy", [])
    }

    void "test grails-plugin-validation/src/test/groovy/grails/validation/DefaultASTValidateableHelperSpec.groovy"() {
        unzipAndTest("grails-plugin-validation/src/test/groovy/grails/validation/DefaultASTValidateableHelperSpec.groovy", [])
    }

    void "test grails-shell/build.gradle"() {
        unzipAndTest("grails-shell/build.gradle", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/GrailsCli.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/GrailsCli.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/boot/GrailsDependencyVersions.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/boot/GrailsDependencyVersions.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/boot/GrailsTestCompilerAutoConfiguration.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/boot/GrailsTestCompilerAutoConfiguration.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/boot/SpringInvoker.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/boot/SpringInvoker.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/ClasspathBuildAction.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/ClasspathBuildAction.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/GradleAsyncInvoker.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/GradleAsyncInvoker.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/GradleInvoker.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/GradleInvoker.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/GradleUtil.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/GradleUtil.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/cache/CachedGradleOperation.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/cache/CachedGradleOperation.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/cache/ListReadingCachedGradleOperation.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/cache/ListReadingCachedGradleOperation.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/cache/MapReadingCachedGradleOperation.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/cache/MapReadingCachedGradleOperation.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleTaskCommandAdapter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleTaskCommandAdapter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/gradle/commands/ReadGradleTasks.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/gradle/commands/ReadGradleTasks.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/AllClassCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/AllClassCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClassNameCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClassNameCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClosureCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClosureCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/DomainClassCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/DomainClassCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/EscapingFileNameCompletor.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/EscapingFileNameCompletor.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/RegexCompletor.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/RegexCompletor.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/SimpleOrFileNameCompletor.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/SimpleOrFileNameCompletor.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/interactive/completers/TestsCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/interactive/completers/TestsCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/AbstractProfile.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/AbstractProfile.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/AbstractStep.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/AbstractStep.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/Command.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/Command.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/CommandArgument.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/CommandArgument.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/CommandDescription.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/CommandDescription.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/CommandException.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/CommandException.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/DefaultFeature.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/DefaultFeature.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/Feature.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/Feature.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/FileSystemProfile.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/FileSystemProfile.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/MultiStepCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/MultiStepCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ProfileCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ProfileCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepository.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepository.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepositoryAware.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepositoryAware.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ProjectCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ProjectCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ProjectContextAware.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ProjectContextAware.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/ResourceProfile.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/ResourceProfile.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/Step.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/Step.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/ArgumentCompletingCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/ArgumentCompletingCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/ClosureExecutingCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/ClosureExecutingCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandCompleter.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandCompleter.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandRegistry.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandRegistry.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateAppCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateAppCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreatePluginCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreatePluginCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateProfileCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateProfileCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/DefaultMultiStepCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/DefaultMultiStepCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/HelpCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/HelpCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/ListProfilesCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/ListProfilesCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/OpenCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/OpenCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/ProfileInfoCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/ProfileInfoCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/CommandEvents.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/CommandEvents.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/EventStorage.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/EventStorage.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ApplicationContextCommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ApplicationContextCommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ClasspathCommandResourceResolver.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ClasspathCommandResourceResolver.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandResourceResolver.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandResourceResolver.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/FileSystemCommandResourceResolver.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/FileSystemCommandResourceResolver.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/GroovyScriptCommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/GroovyScriptCommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ResourceResolvingCommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ResourceResolvingCommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ServiceCommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ServiceCommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/YamlCommandFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/YamlCommandFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteraction.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteraction.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteractionImpl.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteractionImpl.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/ServerInteraction.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/ServerInteraction.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommand.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommand.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommandTransform.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommandTransform.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/SimpleTemplate.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/SimpleTemplate.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateException.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateException.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRenderer.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRenderer.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRendererImpl.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRendererImpl.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/repository/AbstractJarProfileRepository.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/repository/AbstractJarProfileRepository.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/repository/GrailsAetherGrapeEngineFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/repository/GrailsAetherGrapeEngineFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/repository/GrailsRepositoryConfiguration.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/repository/GrailsRepositoryConfiguration.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/repository/MavenProfileRepository.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/repository/MavenProfileRepository.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/repository/StaticJarProfileRepository.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/repository/StaticJarProfileRepository.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/DefaultStepFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/DefaultStepFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/ExecuteStep.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/ExecuteStep.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/GradleStep.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/GradleStep.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/MkdirStep.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/MkdirStep.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/RenderStep.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/RenderStep.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepFactory.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepFactory.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepRegistry.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepRegistry.groovy", [])
    }

    void "test grails-shell/src/main/groovy/org/grails/cli/profile/support/ArtefactVariableResolver.groovy"() {
        unzipAndTest("grails-shell/src/main/groovy/org/grails/cli/profile/support/ArtefactVariableResolver.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/interactive/completers/RegexCompletorSpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/interactive/completers/RegexCompletorSpec.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/profile/ResourceProfileSpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/profile/ResourceProfileSpec.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/profile/commands/CommandScriptTransformSpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/profile/commands/CommandScriptTransformSpec.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/profile/commands/CreateAppCommandSpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/profile/commands/CreateAppCommandSpec.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/profile/repository/MavenRepositorySpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/profile/repository/MavenRepositorySpec.groovy", [])
    }

    void "test grails-shell/src/test/groovy/org/grails/cli/profile/steps/StepRegistrySpec.groovy"() {
        unzipAndTest("grails-shell/src/test/groovy/org/grails/cli/profile/steps/StepRegistrySpec.groovy", [])
    }

    void "test grails-shell/src/test/resources/gradle-sample/build.gradle"() {
        unzipAndTest("grails-shell/src/test/resources/gradle-sample/build.gradle", [])
    }

    void "test grails-shell/src/test/resources/gradle-sample/settings.gradle"() {
        unzipAndTest("grails-shell/src/test/resources/gradle-sample/settings.gradle", [])
    }

    void "test grails-shell/src/test/resources/gradle-sample/subproj2/build.gradle"() {
        unzipAndTest("grails-shell/src/test/resources/gradle-sample/subproj2/build.gradle", [])
    }

    void "test grails-shell/src/test/resources/gradle-sample/subproj/build.gradle"() {
        unzipAndTest("grails-shell/src/test/resources/gradle-sample/subproj/build.gradle", [])
    }

    void "test grails-shell/src/test/resources/profiles-repository/profiles/web/commands/TestGroovy.groovy"() {
        unzipAndTest("grails-shell/src/test/resources/profiles-repository/profiles/web/commands/TestGroovy.groovy", [])
    }

    void "test grails-spring/build.gradle"() {
        unzipAndTest("grails-spring/build.gradle", [])
    }

    void "test grails-spring/src/main/groovy/grails/spring/DynamicElementReader.groovy"() {
        unzipAndTest("grails-spring/src/main/groovy/grails/spring/DynamicElementReader.groovy", [])
    }

    void "test grails-taglib/build.gradle"() {
        unzipAndTest("grails-taglib/build.gradle", [])
    }

    void "test grails-taglib/src/main/groovy/org/grails/taglib/NamespacedTagDispatcher.groovy"() {
        unzipAndTest("grails-taglib/src/main/groovy/org/grails/taglib/NamespacedTagDispatcher.groovy", [])
    }

    void "test grails-taglib/src/main/groovy/org/grails/taglib/TagLibraryMetaUtils.groovy"() {
        unzipAndTest("grails-taglib/src/main/groovy/org/grails/taglib/TagLibraryMetaUtils.groovy", [])
    }

    void "test grails-taglib/src/main/groovy/org/grails/taglib/TemplateNamespacedTagDispatcher.groovy"() {
        unzipAndTest("grails-taglib/src/main/groovy/org/grails/taglib/TemplateNamespacedTagDispatcher.groovy", [])
    }

    void "test grails-taglib/src/main/groovy/org/grails/taglib/encoder/OutputEncodingSettings.groovy"() {
        unzipAndTest("grails-taglib/src/main/groovy/org/grails/taglib/encoder/OutputEncodingSettings.groovy", [])
    }

    void "test grails-taglib/src/main/groovy/org/grails/taglib/encoder/WithCodecHelper.groovy"() {
        unzipAndTest("grails-taglib/src/main/groovy/org/grails/taglib/encoder/WithCodecHelper.groovy", [])
    }

    void "test grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageAttributesTests.groovy"() {
        unzipAndTest("grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageAttributesTests.groovy", [])
    }

    void "test grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageTagWriterSpec.groovy"() {
        unzipAndTest("grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageTagWriterSpec.groovy", [])
    }

    void "test grails-taglib/src/test/groovy/org/grails/taglib/encoder/WithCodecHelperSpec.groovy"() {
        unzipAndTest("grails-taglib/src/test/groovy/org/grails/taglib/encoder/WithCodecHelperSpec.groovy", [])
    }

    void "test grails-test-suite-base/build.gradle"() {
        unzipAndTest("grails-test-suite-base/build.gradle", [])
    }

    void "test grails-test-suite-base/src/main/groovy/org/grails/web/servlet/mvc/AbstractGrailsControllerTests.groovy"() {
        unzipAndTest("grails-test-suite-base/src/main/groovy/org/grails/web/servlet/mvc/AbstractGrailsControllerTests.groovy", [])
    }

    void "test grails-test-suite-base/src/main/groovy/org/grails/web/taglib/AbstractGrailsTagTests.groovy"() {
        unzipAndTest("grails-test-suite-base/src/main/groovy/org/grails/web/taglib/AbstractGrailsTagTests.groovy", [])
    }

    void "test grails-test-suite-persistence/build.gradle"() {
        unzipAndTest("grails-test-suite-persistence/build.gradle", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/artefact/DomainClassTraitSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/artefact/DomainClassTraitSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/gorm/criteri/WithCriteriaReadOnlySpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/gorm/criteri/WithCriteriaReadOnlySpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/test/mixin/domain/DomainClassUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/test/mixin/domain/DomainClassUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/test/mixin/domain/SaveDomainSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/test/mixin/domain/SaveDomainSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderBindingXmlSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderBindingXmlSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderConfigurationSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderConfigurationSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderListenerSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderListenerSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBinderSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBindingStructuredEditorSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/grails/web/databinding/GrailsWebDataBindingStructuredEditorSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/org/grails/domain/compiler/DomainPropertiesAccessorSpec.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/org/grails/domain/compiler/DomainPropertiesAccessorSpec.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/org/grails/orm/support/TransactionManagerPostProcessorTests.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/org/grails/orm/support/TransactionManagerPostProcessorTests.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/org/grails/plugins/MockHibernateGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/org/grails/plugins/MockHibernateGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/org/grails/plugins/services/ScopedProxyAndServiceClassTests.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/org/grails/plugins/services/ScopedProxyAndServiceClassTests.groovy", [])
    }

    void "test grails-test-suite-persistence/src/test/groovy/org/grails/plugins/services/ServicesGrailsPluginTests.groovy"() {
        unzipAndTest("grails-test-suite-persistence/src/test/groovy/org/grails/plugins/services/ServicesGrailsPluginTests.groovy", [])
    }

    void "test grails-test-suite-uber/build.gradle"() {
        unzipAndTest("grails-test-suite-uber/build.gradle", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/compiler/GrailsCompileStaticCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/compiler/GrailsCompileStaticCompilationErrorsSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/compiler/GrailsTypeCheckedCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/compiler/GrailsTypeCheckedCompilationErrorsSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/persistence/EntityTransformTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/persistence/EntityTransformTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/spring/BeanBuilderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/spring/BeanBuilderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/spring/DynamicElementReaderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/spring/DynamicElementReaderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/MetaTestHelper.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/MetaTestHelper.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/AddToAndServiceInjectionTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/AddToAndServiceInjectionTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/AnotherController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/AnotherController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/AstEnhancedControllerUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/AstEnhancedControllerUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/AutowireServiceViaDefineBeansTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/AutowireServiceViaDefineBeansTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/BidirectionalOneToManyUnitTestTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/BidirectionalOneToManyUnitTestTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/CascadeValidationForEmbeddedSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/CascadeValidationForEmbeddedSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerAndGroovyPageMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerAndGroovyPageMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerMockWithMultipleControllersSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerMockWithMultipleControllersSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerTestForTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerTestForTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerWithMockCollabTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ControllerWithMockCollabTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassAnnotatedSetupMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassAnnotatedSetupMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassControllerUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassControllerUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassDeepValidationSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassDeepValidationSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassMetaClassCleanupSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassMetaClassCleanupSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassSetupMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassSetupMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithAutoTimestampSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithAutoTimestampSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithCustomValidatorTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithCustomValidatorTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithDefaultConstraintsUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithDefaultConstraintsUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithUniqueConstraintSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/DomainClassWithUniqueConstraintSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/FirstController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/FirstController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/GroovyPageUnitTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/GroovyPageUnitTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/GroovyPageUnitTestMixinWithCustomViewDirSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/GroovyPageUnitTestMixinWithCustomViewDirSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/InheritanceWithValidationTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/InheritanceWithValidationTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/InterceptorUnitTestMixinSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/InterceptorUnitTestMixinSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/MainContextTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/MainContextTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/MockedBeanSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/MockedBeanSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/MyService.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/MyService.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/PartialMockWithManyToManySpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/PartialMockWithManyToManySpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/ResourceAnnotationRestfulControllerSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/ResourceAnnotationRestfulControllerSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSubclassSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSubclassSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSuperClassSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/RestfulControllerSuperClassSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/SetupTeardownInvokeTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/SetupTeardownInvokeTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/SpyBeanSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/SpyBeanSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/StaticCallbacksSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/StaticCallbacksSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/TagLibraryInvokeBodySpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/TagLibraryInvokeBodySpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestForControllerWithoutMockDomainTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestForControllerWithoutMockDomainTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestInstanceCallbacksAnnotationsSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestInstanceCallbacksAnnotationsSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestInstanceCallbacksSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestInstanceCallbacksSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestMixinSetupTeardownInvokeTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/TestMixinSetupTeardownInvokeTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/UnitTestDataBindingAssociatonTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/UnitTestDataBindingAssociatonTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/UnitTestEmbeddedPropertyQuery.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/UnitTestEmbeddedPropertyQuery.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/UrlMappingsTestMixinTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/UrlMappingsTestMixinTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/User.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/User.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/support/GrailsUnitTestMixinGrailsApplicationAwareSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/support/GrailsUnitTestMixinGrailsApplicationAwareSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/mixin/unique/UniqueConstraintOnHasOneSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/mixin/unique/UniqueConstraintOnHasOneSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/test/runtime/DirtiesRuntimeSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/test/runtime/DirtiesRuntimeSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/util/BuildScopeTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/util/BuildScopeTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/util/ClosureToMapPopulatorTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/util/ClosureToMapPopulatorTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/util/CollectionUtilsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/util/CollectionUtilsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/CommandObjectConstraintGettersSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/CommandObjectConstraintGettersSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/DomainClassValidationSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/DomainClassValidationSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/DomainConstraintGettersSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/DomainConstraintGettersSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableMockSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableMockSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableTraitAdHocSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableTraitAdHocSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableTraitSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/validation/ValidateableTraitSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/grails/web/JSONBuilderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/grails/web/JSONBuilderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/cli/ScriptNameResolverTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/cli/ScriptNameResolverTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultArtefactInfoTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultArtefactInfoTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsCodecClassTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsCodecClassTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsDomainClassPropertyTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsDomainClassPropertyTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsDomainClassTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/DefaultGrailsDomainClassTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/GrailsMetaClassUtilsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/GrailsMetaClassUtilsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/GrailsPluginManagerTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/GrailsPluginManagerTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/MultipleClassesPerFileTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/MultipleClassesPerFileTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/TestReload.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/TestReload.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/UrlMappingsArtefactHandlerTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/UrlMappingsArtefactHandlerTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/cfg/ExampleConfigClassObject.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/cfg/ExampleConfigClassObject.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/cfg/ExampleConfigCompiledClass.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/cfg/ExampleConfigCompiledClass.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/LazyMetaPropertyMapSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/LazyMetaPropertyMapSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/LazyMetaPropertyMapTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/LazyMetaPropertyMapTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/MetaClassEnhancerSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/MetaClassEnhancerSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/MetaClassEnhancerTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/metaclass/MetaClassEnhancerTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/commons/spring/OptimizedAutowireCapableBeanFactorySpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/commons/spring/OptimizedAutowireCapableBeanFactorySpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/CircularRelationship.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/CircularRelationship.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/ManyToManyTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/ManyToManyTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/OneToManyTest2.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/OneToManyTest2.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/OneToOneTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/OneToOneTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/RelationshipsTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/RelationshipsTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/Test1.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/Test1.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/Test2.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/Test2.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/domain/UniOneToManyTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/domain/UniOneToManyTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/CoreGrailsPluginTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/CoreGrailsPluginTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/PluginLoadOrderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/PluginLoadOrderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/conf/NonPooledApplicationDataSource.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/conf/NonPooledApplicationDataSource.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/conf/PooledApplicationDataSource.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/conf/PooledApplicationDataSource.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/services/TestService.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/grails-app/services/TestService.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/metadata/GrailsPluginMetadataTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/metadata/GrailsPluginMetadataTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequestSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequestSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequestTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequestTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletResponseTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpServletResponseTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpSessionTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/testing/GrailsMockHttpSessionTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/atom/AtomDomainClassRendererSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/atom/AtomDomainClassRendererSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalDomainClassJsonRendererSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalDomainClassJsonRendererSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalDomainClassXmlRendererSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalDomainClassXmlRendererSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/reload/SpringProxiedBeanReloadTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/reload/SpringProxiedBeanReloadTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/test/support/ControllerNameExtractorTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/test/support/ControllerNameExtractorTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/test/support/MockHibernatePluginHelper.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/test/support/MockHibernatePluginHelper.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/CascadingErrorCountSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/CascadingErrorCountSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstrainedPropertyBuilderForCommandsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstrainedPropertyBuilderForCommandsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstraintMessageTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstraintMessageTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstraintsBuilderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/ConstraintsBuilderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/GrailsDomainClassValidatorTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/GrailsDomainClassValidatorTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/NullableConstraintTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/NullableConstraintTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/TestConstraints.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/TestConstraints.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/validation/TestingValidationSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/validation/TestingValidationSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/codecs/HTMLJSCodecIntegrationSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/codecs/HTMLJSCodecIntegrationSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/errors/GrailsExceptionResolverTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/errors/GrailsExceptionResolverTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/filters/HiddenHttpMethodFilterTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/filters/HiddenHttpMethodFilterTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/i18n/ParamsAwareLocaleChangeInterceptorTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/i18n/ParamsAwareLocaleChangeInterceptorTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/json/JSONObjectTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/json/JSONObjectTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ChainMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ChainMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ChainMethodWithRequestDataValueProcessorSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ChainMethodWithRequestDataValueProcessorSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ForwardMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ForwardMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ForwardMethodspec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/ForwardMethodspec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/WithFormMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/metaclass/WithFormMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/DefaultGrailsApplicationAttributesTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/DefaultGrailsApplicationAttributesTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/FlashScopeWithErrorsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/FlashScopeWithErrorsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/MultipleRenderCallsContentTypeTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/MultipleRenderCallsContentTypeTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/RenderMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/RenderMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ControllerInheritanceTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ControllerInheritanceTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ParamsObjectTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ParamsObjectTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectMethodWithRequestDataValueProcessorSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectMethodWithRequestDataValueProcessorSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectToDefaultActionTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RedirectToDefaultActionTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RenderDynamicMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/RenderDynamicMethodTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ReturnModelAndViewController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/ReturnModelAndViewController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/TagLibDynamicMethodsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/TagLibDynamicMethodsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/alpha/AnotherNamespacedController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/alpha/AnotherNamespacedController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/alpha/NamespacedController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/alpha/NamespacedController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/beta/AnotherNamespacedController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/beta/AnotherNamespacedController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/beta/NamespacedController.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/beta/NamespacedController.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller1.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller1.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller2.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller2.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller4.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/servlet/mvc/controller4.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FactoryHolderTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FactoryHolderTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FullSitemeshLifeCycleTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FullSitemeshLifeCycleTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FullSitemeshLifeCycleWithNoPreprocessingTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/FullSitemeshLifeCycleWithNoPreprocessingTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/GSPSitemeshPageTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/sitemesh/GSPSitemeshPageTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/util/CodecWithClosureProperties.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/util/CodecWithClosureProperties.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/util/StreamCharBufferSpec.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/util/StreamCharBufferSpec.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/org/grails/web/util/WebUtilsTests.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/org/grails/web/util/WebUtilsTests.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/MySharedRuntimeConfigurer.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/MySharedRuntimeConfigurer.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeCheck.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeCheck.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeSample2Test.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeSample2Test.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeSampleTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/SharedRuntimeSampleTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/subpackage/SharedRuntimeByPkgSample2Test.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/subpackage/SharedRuntimeByPkgSample2Test.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/groovy/sharedruntimetest/subpackage/SharedRuntimeByPkgSampleTest.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/groovy/sharedruntimetest/subpackage/SharedRuntimeByPkgSampleTest.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/resources/grails/spring/resources1.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/resources/grails/spring/resources1.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/resources/org/grails/commons/cfg/ExampleConfigDefaults.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/resources/org/grails/commons/cfg/ExampleConfigDefaults.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/resources/org/grails/commons/cfg/ExampleConfigScript.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/resources/org/grails/commons/cfg/ExampleConfigScript.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/resources/org/grails/commons/classes.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/resources/org/grails/commons/classes.groovy", [])
    }

    void "test grails-test-suite-uber/src/test/resources/org/grails/plugins/ClassEditorGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/src/test/resources/org/grails/plugins/ClassEditorGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/LoggingGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/LoggingGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/scripts/DoSomething.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/scripts/DoSomething.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/scripts/_Install.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/global-plugins/logging-0.1/scripts/_Install.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/grails-debug/scripts/RunDebug.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/grails-debug/scripts/RunDebug.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/JSecurityGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/JSecurityGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/scripts/CreateAuthController.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/scripts/CreateAuthController.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/scripts/CreateDbRealm.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/grails-plugin-utils/plugins/jsecurity-0.3/scripts/CreateDbRealm.groovy", [])
    }

    void "test grails-test-suite-uber/test/resources/spring/test.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/resources/spring/test.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/inline-plugins/app/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/inline-plugins/app/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foo/FooGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foo/FooGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foo/grails-app/controllers/foo/FooController.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foo/grails-app/controllers/foo/FooController.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foobar/FoobarGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foobar/FoobarGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foobar/grails-app/controllers/foobar/FoobarController.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/inline-plugins/plugins/foobar/grails-app/controllers/foobar/FoobarController.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/nested-inline-plugins/app/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/nested-inline-plugins/app/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-one/PluginOneGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-one/PluginOneGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-one/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-one/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-two/PluginTwoGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/nested-inline-plugins/plugins/plugin-two/PluginTwoGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/BootStrap.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/BootStrap.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/BuildConfig.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/BuildConfig.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/Config.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/Config.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/DataSource.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/DataSource.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/UrlMappings.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/UrlMappings.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/spring/resources.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/grails-app/conf/spring/resources.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/HibernateGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/HibernateGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/dependencies.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/dependencies.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Install.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Install.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Uninstall.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Uninstall.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Upgrade.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/hibernate-1.2-SNAPSHOT/scripts/_Upgrade.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/WebflowGrailsPlugin.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/WebflowGrailsPlugin.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/dependencies.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/dependencies.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Install.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Install.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Uninstall.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Uninstall.groovy", [])
    }

    void "test grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Upgrade.groovy"() {
        unzipAndTest("grails-test-suite-uber/test/test-projects/plugin-build-settings/plugins/webflow-1.2-SNAPSHOT/scripts/_Upgrade.groovy", [])
    }

    void "test grails-test-suite-web/build.gradle"() {
        unzipAndTest("grails-test-suite-web/build.gradle", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/artefact/ControllerTraitSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/artefact/ControllerTraitSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/artefact/TagLibraryTraitSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/artefact/TagLibraryTraitSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/gsp/PageRendererSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/gsp/PageRendererSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/rest/web/RespondMethodSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/rest/web/RespondMethodSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/AbstractGrailsEnvChangingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/AbstractGrailsEnvChangingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/mixin/TagLibWithServiceMockTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/mixin/TagLibWithServiceMockTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/mixin/UrlMappingsTestForTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/mixin/UrlMappingsTestForTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/web/AsyncControllerTestSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/web/AsyncControllerTestSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/web/ControllerWithGroovyMixinSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/web/ControllerWithGroovyMixinSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/web/FordedUrlSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/web/FordedUrlSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/web/GetHeadersFromResponseSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/web/GetHeadersFromResponseSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/grails/test/web/RedirectToDomainSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/grails/test/web/RedirectToDomainSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerAllowedMethodsSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/compiler/web/ControllerActionTransformerAllowedMethodsSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/compiler/web/WithFormatSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/compiler/web/WithFormatSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/compiler/web/converters/ConvertersControllersApiSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/compiler/web/converters/ConvertersControllersApiSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/compiler/web/taglib/TagLibraryTransformerSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/compiler/web/taglib/TagLibraryTransformerSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyEachParseTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyEachParseTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyEachTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyEachTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyFindAllTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyFindAllTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyGrepTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovyGrepTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovySyntaxTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/compiler/tags/GroovySyntaxTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/GroovyPageWithJSPTagsTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/GroovyPageWithJSPTagsTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/GroovyPagesPageContextTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/GroovyPagesPageContextTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/IterativeJspTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/IterativeJspTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/SimpleJspTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/SimpleJspTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/SimpleTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/SimpleTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/TagLibraryResolverTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/TagLibraryResolverTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/TldReaderTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/TldReaderTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/WebXmlTagLibraryReaderTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/gsp/jsp/WebXmlTagLibraryReaderTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/plugins/web/rest/render/xml/DefaultXmlRendererSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/plugins/web/rest/render/xml/DefaultXmlRendererSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindStringArrayToGenericListTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindStringArrayToGenericListTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToEnumTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToEnumTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToObjectWithEmbeddableTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToObjectWithEmbeddableTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToPropertyThatIsNotReadableTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindToPropertyThatIsNotReadableTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindXmlWithAssociationTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindXmlWithAssociationTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingExcludeTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingExcludeTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingRequestMethodSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingRequestMethodSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingToNullableTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/BindingToNullableTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/CheckboxBindingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/CheckboxBindingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/ControllerActionParameterBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/ControllerActionParameterBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/DataBindingLazyMetaPropertyMapTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/DataBindingLazyMetaPropertyMapTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/DataBindingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/DataBindingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/DefaultASTDatabindingHelperDomainClassSpecialPropertiesSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/DefaultASTDatabindingHelperDomainClassSpecialPropertiesSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/EnumBindingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/EnumBindingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/GrailsParameterMapBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/GrailsParameterMapBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/JSONBindingToNullTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/JSONBindingToNullTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/JSONRequestToResponseRenderingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/JSONRequestToResponseRenderingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/NestedXmlBindingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/NestedXmlBindingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/hal/json/HalJsonBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/hal/json/HalJsonBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/hal/xml/HalXmlBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/hal/xml/HalXmlBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/json/JsonBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/json/JsonBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/json/JsonBindingWithExceptionHandlerSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/json/JsonBindingWithExceptionHandlerSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/binding/xml/XmlBindingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/binding/xml/XmlBindingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/codecs/CodecSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/codecs/CodecSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/ClassWithNoValidateMethod.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/ClassWithNoValidateMethod.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectInstantiationSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectInstantiationSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectNullabilitySpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectNullabilitySpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectsSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/CommandObjectsSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/NonValidateableCommand.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/NonValidateableCommand.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/SomeValidateableClass.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/commandobjects/SomeValidateableClass.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ContentNegotiationSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ContentNegotiationSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerCompilationErrorsSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerCompilationErrorsSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerInheritanceSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerInheritanceSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerExceptionHandlerSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerMetaProgrammingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/controllers/ControllerMetaProgrammingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/ControllerWithXmlConvertersTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/ControllerWithXmlConvertersTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/ConverterConfigurationTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/ConverterConfigurationTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/JSONArrayTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/JSONArrayTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/JSONConverterTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/JSONConverterTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/MarshallerRegistrarSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/MarshallerRegistrarSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/converters/XMLConverterTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/converters/XMLConverterTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/includes/IncludeHandlingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/includes/IncludeHandlingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/json/DomainClassCollectionRenderingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/json/DomainClassCollectionRenderingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/json/JSONWriterSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/json/JSONWriterSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/AbstractGrailsMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/AbstractGrailsMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/AdditionalParamsMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/AdditionalParamsMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DoubleWildcardUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DoubleWildcardUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DynamicActionNameEvaluatingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DynamicActionNameEvaluatingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DynamicParameterValuesTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/DynamicParameterValuesTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/IdUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/IdUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/OverlappingUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/OverlappingUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RegexUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RegexUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ResponseCodeUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ResponseCodeUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RestfulMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RestfulMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RestfulReverseUrlRenderingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RestfulReverseUrlRenderingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ReverseUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ReverseUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ReverseUrlMappingToDefaultActionTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ReverseUrlMappingToDefaultActionTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RootUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/RootUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UriUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UriUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingEvaluatorTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingEvaluatorTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingParameterTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingParameterTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingWithCustomValidatorTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingWithCustomValidatorTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingsHolderTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/UrlMappingsHolderTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ViewUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mapping/ViewUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/metaclass/CollectionBindDataMethodSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/metaclass/CollectionBindDataMethodSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mime/ContentFormatControllerTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mime/ContentFormatControllerTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/mime/WithFormatContentTypeSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/mime/WithFormatContentTypeSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/AliasedTagPropertySpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/AliasedTagPropertySpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/ElvisAndClosureGroovyPageTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/ElvisAndClosureGroovyPageTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GSPResponseWriterSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GSPResponseWriterSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageBindingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageBindingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageLineNumberTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageLineNumberTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageMethodDispatchTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageMethodDispatchTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageMethodDispatchWithNamespaceTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageMethodDispatchWithNamespaceTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageRenderingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageRenderingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageServletSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageServletSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPageTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPagesIfTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPagesIfTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPagesWhitespaceParsingTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/GroovyPagesWhitespaceParsingTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/InvokeTagWithCustomBodyClosureSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/InvokeTagWithCustomBodyClosureSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/ModifyOurScopeWithBodyTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/ModifyOurScopeWithBodyTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/NewLineRenderingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/NewLineRenderingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/OptionalTagBodySpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/OptionalTagBodySpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/ReservedWordOverrideTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/ReservedWordOverrideTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/SitemeshPreprocessorTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/SitemeshPreprocessorTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/StaticContentRenderingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/StaticContentRenderingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibMethodMissingSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibMethodMissingSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibNamespaceTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibNamespaceTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibWithGStringTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibWithGStringTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibWithNullValuesTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/pages/TagLibWithNullValuesTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/servlet/BindDataMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/servlet/BindDataMethodTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/servlet/GrailsFlashScopeSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/servlet/GrailsFlashScopeSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/servlet/mvc/SynchronizerTokensHolderTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/servlet/mvc/SynchronizerTokensHolderTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/servlet/view/GroovyPageViewTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/servlet/view/GroovyPageViewTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplicationTagLibResourcesTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplicationTagLibResourcesTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplicationTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplicationTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplyCodecTagSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ApplyCodecTagSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ControllerTagLibMethodDispatchSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ControllerTagLibMethodDispatchSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ControllerTagLibMethodInheritanceSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ControllerTagLibMethodInheritanceSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/CoreTagsTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/CoreTagsTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/CountryTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/CountryTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormRenderingTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormRenderingTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLib2Tests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLib2Tests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLib3Tests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLib3Tests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLibResourceTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLibResourceTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormatTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/FormatTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/InvokeTagLibAsMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/InvokeTagLibAsMethodTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/InvokeTagLibWithBodyAsMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/InvokeTagLibWithBodyAsMethodTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/JavascriptTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/JavascriptTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LayoutWriterStackTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LayoutWriterStackTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LinkRenderingTagLib2Tests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LinkRenderingTagLib2Tests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LinkRenderingTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/LinkRenderingTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/MessageTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/MessageTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamedTagBodyParamsTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamedTagBodyParamsTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedNamedUrlMappingTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedNamedUrlMappingTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagAndActionConflictTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagAndActionConflictTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagLibMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagLibMethodTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagLibRenderMethodTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/NamespacedTagLibRenderMethodTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/OverlappingReverseMappedLinkTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/OverlappingReverseMappedLinkTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PageScopeSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PageScopeSpec.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PageScopeTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PageScopeTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PluginTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/PluginTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/RenderTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/RenderTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ReturnValueTagLibTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ReturnValueTagLibTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/SelectTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/SelectTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/TagLibraryDynamicPropertyTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/TagLibraryDynamicPropertyTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/UploadFormTagTests.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/UploadFormTagTests.groovy", [])
    }

    void "test grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ValidationTagLibSpec.groovy"() {
        unzipAndTest("grails-test-suite-web/src/test/groovy/org/grails/web/taglib/ValidationTagLibSpec.groovy", [])
    }

    void "test grails-test/build.gradle"() {
        unzipAndTest("grails-test/build.gradle", [])
    }

    void "test grails-test/src/main/groovy/grails/boot/test/GrailsApplicationContextLoader.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/grails/boot/test/GrailsApplicationContextLoader.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/plugins/testing/AbstractGrailsMockHttpServletResponse.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/plugins/testing/AbstractGrailsMockHttpServletResponse.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequest.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/plugins/testing/GrailsMockHttpServletRequest.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/plugins/testing/GrailsMockHttpSession.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/plugins/testing/GrailsMockHttpSession.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/io/MultiplexingOutputStream.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/io/MultiplexingOutputStream.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/io/SystemOutAndErrSwapper.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/io/SystemOutAndErrSwapper.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/spock/IntegrationSpecConfigurerExtension.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/spock/IntegrationSpecConfigurerExtension.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/ControllerNameExtractor.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/ControllerNameExtractor.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/GrailsTestAutowirer.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/GrailsTestAutowirer.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/GrailsTestInterceptor.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/GrailsTestInterceptor.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/GrailsTestMode.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/GrailsTestMode.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/GrailsTestRequestEnvironmentInterceptor.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/GrailsTestRequestEnvironmentInterceptor.groovy", [])
    }

    void "test grails-test/src/main/groovy/org/grails/test/support/GrailsTestTransactionInterceptor.groovy"() {
        unzipAndTest("grails-test/src/main/groovy/org/grails/test/support/GrailsTestTransactionInterceptor.groovy", [])
    }

    void "test grails-validation/build.gradle"() {
        unzipAndTest("grails-validation/build.gradle", [])
    }

    void "test grails-validation/src/test/groovy/grails/validation/ConstraintTypeMismatchSpec.groovy"() {
        unzipAndTest("grails-validation/src/test/groovy/grails/validation/ConstraintTypeMismatchSpec.groovy", [])
    }

    void "test grails-validation/src/test/groovy/org/grails/validation/ConstrainedPropertyBuilderSpec.groovy"() {
        unzipAndTest("grails-validation/src/test/groovy/org/grails/validation/ConstrainedPropertyBuilderSpec.groovy", [])
    }

    void "test grails-validation/src/test/groovy/org/grails/validation/errors/ValidationErrorsSpec.groovy"() {
        unzipAndTest("grails-validation/src/test/groovy/org/grails/validation/errors/ValidationErrorsSpec.groovy", [])
    }

    void "test grails-web-boot/build.gradle"() {
        unzipAndTest("grails-web-boot/build.gradle", [])
    }

    void "test grails-web-boot/src/main/groovy/org/grails/boot/context/web/GrailsAppServletInitializer.groovy"() {
        unzipAndTest("grails-web-boot/src/main/groovy/org/grails/boot/context/web/GrailsAppServletInitializer.groovy", [])
    }

    void "test grails-web-boot/src/main/groovy/org/grails/compiler/boot/BootInitializerClassInjector.groovy"() {
        unzipAndTest("grails-web-boot/src/main/groovy/org/grails/compiler/boot/BootInitializerClassInjector.groovy", [])
    }

    void "test grails-web-boot/src/test/groovy/grails/boot/EmbeddedContainerWithGrailsSpec.groovy"() {
        unzipAndTest("grails-web-boot/src/test/groovy/grails/boot/EmbeddedContainerWithGrailsSpec.groovy", [])
    }

    void "test grails-web-boot/src/test/groovy/grails/boot/GrailsSpringApplicationSpec.groovy"() {
        unzipAndTest("grails-web-boot/src/test/groovy/grails/boot/GrailsSpringApplicationSpec.groovy", [])
    }

    void "test grails-web-boot/src/test/groovy/org/grails/compiler/boot/BootInitializerClassInjectorSpec.groovy"() {
        unzipAndTest("grails-web-boot/src/test/groovy/org/grails/compiler/boot/BootInitializerClassInjectorSpec.groovy", [])
    }

    void "test grails-web-common/build.gradle"() {
        unzipAndTest("grails-web-common/build.gradle", [])
    }

    void "test grails-web-common/src/main/groovy/grails/util/GrailsWebMockUtil.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/util/GrailsWebMockUtil.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/api/ServletAttributes.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/api/ServletAttributes.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/api/WebAttributes.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/api/WebAttributes.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/mime/MimeType.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/mime/MimeType.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/mime/MimeTypeProvider.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/mime/MimeTypeProvider.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/mime/MimeTypeResolver.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/mime/MimeTypeResolver.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/grails/web/mime/MimeTypeUtils.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/grails/web/mime/MimeTypeUtils.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/databinding/bindingsource/DataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/databinding/bindingsource/DataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/beans/PropertyEditorRegistryUtils.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/beans/PropertyEditorRegistryUtils.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/context/ServletEnvironmentGrailsApplicationDiscoveryStrategy.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/context/ServletEnvironmentGrailsApplicationDiscoveryStrategy.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/databinding/bindingsource/DataBindingSourceRegistry.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/databinding/bindingsource/DataBindingSourceRegistry.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/errors/ErrorsViewStackTracePrinter.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/errors/ErrorsViewStackTracePrinter.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/i18n/ParamsAwareLocaleChangeInterceptor.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/i18n/ParamsAwareLocaleChangeInterceptor.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/servlet/mvc/ActionResultTransformer.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/servlet/mvc/ActionResultTransformer.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/servlet/mvc/OutputAwareHttpServletResponse.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/servlet/mvc/OutputAwareHttpServletResponse.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/servlet/view/CompositeViewResolver.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/servlet/view/CompositeViewResolver.groovy", [])
    }

    void "test grails-web-common/src/main/groovy/org/grails/web/util/ClassAndMimeTypeRegistry.groovy"() {
        unzipAndTest("grails-web-common/src/main/groovy/org/grails/web/util/ClassAndMimeTypeRegistry.groovy", [])
    }

    void "test grails-web-common/src/test/groovy/grails/web/servlet/mvc/GrailsParameterMapTests.groovy"() {
        unzipAndTest("grails-web-common/src/test/groovy/grails/web/servlet/mvc/GrailsParameterMapTests.groovy", [])
    }

    void "test grails-web-databinding/build.gradle"() {
        unzipAndTest("grails-web-databinding/build.gradle", [])
    }

    void "test grails-web-databinding/src/main/groovy/grails/compiler/traits/WebDataBindingTraitInjector.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/grails/compiler/traits/WebDataBindingTraitInjector.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/grails/web/databinding/DataBinder.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/grails/web/databinding/DataBinder.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/grails/web/databinding/GrailsWebDataBinder.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/grails/web/databinding/GrailsWebDataBinder.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/grails/web/databinding/WebDataBinding.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/grails/web/databinding/WebDataBinding.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/DataBindingEventMulticastListener.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/DataBindingEventMulticastListener.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/GrailsWebDataBindingListener.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/GrailsWebDataBindingListener.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/SpringConversionServiceAdapter.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/SpringConversionServiceAdapter.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/AbstractRequestBodyDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/AbstractRequestBodyDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/DefaultDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/DefaultDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/DefaultDataBindingSourceRegistry.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/DefaultDataBindingSourceRegistry.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalGPathResultMap.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalGPathResultMap.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalJsonDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalJsonDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalXmlDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/HalXmlDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/JsonDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/JsonDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/XmlDataBindingSourceCreator.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/bindingsource/XmlDataBindingSourceCreator.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/converters/AbstractStructuredBindingEditor.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/converters/AbstractStructuredBindingEditor.groovy", [])
    }

    void "test grails-web-databinding/src/main/groovy/org/grails/web/databinding/converters/ByteArrayMultipartFileValueConverter.groovy"() {
        unzipAndTest("grails-web-databinding/src/main/groovy/org/grails/web/databinding/converters/ByteArrayMultipartFileValueConverter.groovy", [])
    }

    void "test grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/AbstractRequestBodyDataBindingSourceCreatorSpec.groovy"() {
        unzipAndTest("grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/AbstractRequestBodyDataBindingSourceCreatorSpec.groovy", [])
    }

    void "test grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/HalGPathResultMapSpec.groovy"() {
        unzipAndTest("grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/HalGPathResultMapSpec.groovy", [])
    }

    void "test grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/hal/json/HalJsonDataBindingSourceCreatorSpec.groovy"() {
        unzipAndTest("grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/hal/json/HalJsonDataBindingSourceCreatorSpec.groovy", [])
    }

    void "test grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/json/JsonDataBindingSourceCreatorSpec.groovy"() {
        unzipAndTest("grails-web-databinding/src/test/groovy/org/grails/web/databinding/bindingsource/json/JsonDataBindingSourceCreatorSpec.groovy", [])
    }

    void "test grails-web-fileupload/build.gradle"() {
        unzipAndTest("grails-web-fileupload/build.gradle", [])
    }

    void "test grails-web-gsp-taglib/build.gradle"() {
        unzipAndTest("grails-web-gsp-taglib/build.gradle", [])
    }

    void "test grails-web-gsp-taglib/src/main/groovy/org/grails/plugins/web/taglib/RenderTagLib.groovy"() {
        unzipAndTest("grails-web-gsp-taglib/src/main/groovy/org/grails/plugins/web/taglib/RenderTagLib.groovy", [])
    }

    void "test grails-web-gsp-taglib/src/main/groovy/org/grails/plugins/web/taglib/SitemeshTagLib.groovy"() {
        unzipAndTest("grails-web-gsp-taglib/src/main/groovy/org/grails/plugins/web/taglib/SitemeshTagLib.groovy", [])
    }

    void "test grails-web-gsp/build.gradle"() {
        unzipAndTest("grails-web-gsp/build.gradle", [])
    }

    void "test grails-web-gsp/src/main/groovy/grails/gsp/PageRenderer.groovy"() {
        unzipAndTest("grails-web-gsp/src/main/groovy/grails/gsp/PageRenderer.groovy", [])
    }

    void "test grails-web-gsp/src/main/groovy/org/grails/web/pages/GroovyPageCompilerTask.groovy"() {
        unzipAndTest("grails-web-gsp/src/main/groovy/org/grails/web/pages/GroovyPageCompilerTask.groovy", [])
    }

    void "test grails-web-gsp/src/test/groovy/org/grails/web/gsp/io/GrailsConventionGroovyPageLocatorSpec.groovy"() {
        unzipAndTest("grails-web-gsp/src/test/groovy/org/grails/web/gsp/io/GrailsConventionGroovyPageLocatorSpec.groovy", [])
    }

    void "test grails-web-gsp/src/test/groovy/org/grails/web/servlet/view/GroovyPageViewResolverSpec.groovy"() {
        unzipAndTest("grails-web-gsp/src/test/groovy/org/grails/web/servlet/view/GroovyPageViewResolverSpec.groovy", [])
    }

    void "test grails-web-jsp/build.gradle"() {
        unzipAndTest("grails-web-jsp/build.gradle", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/GroovyPagesJspFactory.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/GroovyPagesJspFactory.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/JspTagImpl.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/JspTagImpl.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/JspTagLibImpl.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/JspTagLibImpl.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/PageContextFactory.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/PageContextFactory.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/TagLibraryResolverImpl.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/TagLibraryResolverImpl.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/TldReader.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/TldReader.groovy", [])
    }

    void "test grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/WebXmlTagLibraryReader.groovy"() {
        unzipAndTest("grails-web-jsp/src/main/groovy/org/grails/gsp/jsp/WebXmlTagLibraryReader.groovy", [])
    }

    void "test grails-web-mvc/build.gradle"() {
        unzipAndTest("grails-web-mvc/build.gradle", [])
    }

    void "test grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/GrailsDispatcherServlet.groovy"() {
        unzipAndTest("grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/GrailsDispatcherServlet.groovy", [])
    }

    void "test grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/SynchronizerTokensHolder.groovy"() {
        unzipAndTest("grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/SynchronizerTokensHolder.groovy", [])
    }

    void "test grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/TokenResponseActionResultTransformer.groovy"() {
        unzipAndTest("grails-web-mvc/src/main/groovy/org/grails/web/servlet/mvc/TokenResponseActionResultTransformer.groovy", [])
    }

    void "test grails-web-sitemesh/build.gradle"() {
        unzipAndTest("grails-web-sitemesh/build.gradle", [])
    }

    void "test grails-web-taglib/build.gradle"() {
        unzipAndTest("grails-web-taglib/build.gradle", [])
    }

    void "test grails-web-taglib/src/main/groovy/grails/artefact/TagLibrary.groovy"() {
        unzipAndTest("grails-web-taglib/src/main/groovy/grails/artefact/TagLibrary.groovy", [])
    }

    void "test grails-web-taglib/src/main/groovy/grails/artefact/gsp/TagLibraryInvoker.groovy"() {
        unzipAndTest("grails-web-taglib/src/main/groovy/grails/artefact/gsp/TagLibraryInvoker.groovy", [])
    }

    void "test grails-web-taglib/src/main/groovy/grails/compiler/traits/TagLibraryTraitInjector.groovy"() {
        unzipAndTest("grails-web-taglib/src/main/groovy/grails/compiler/traits/TagLibraryTraitInjector.groovy", [])
    }

    void "test grails-web-taglib/src/test/groovy/org/grails/web/taglib/TagLibraryLookupSpec.groovy"() {
        unzipAndTest("grails-web-taglib/src/test/groovy/org/grails/web/taglib/TagLibraryLookupSpec.groovy", [])
    }

    void "test grails-web-url-mappings/build.gradle"() {
        unzipAndTest("grails-web-url-mappings/build.gradle", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/grails/web/mapping/LinkGeneratorFactory.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/grails/web/mapping/LinkGeneratorFactory.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/grails/web/mapping/ResponseRedirector.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/grails/web/mapping/ResponseRedirector.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/grails/web/mapping/UrlMappingsFactory.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/grails/web/mapping/UrlMappingsFactory.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/grails/web/mapping/reporting/UrlMappingsRenderer.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/grails/web/mapping/reporting/UrlMappingsRenderer.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ControllerActionConventions.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ControllerActionConventions.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/DefaultLinkGenerator.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/DefaultLinkGenerator.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/DefaultUrlMappings.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/DefaultUrlMappings.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ForwardUrlMappingInfo.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ForwardUrlMappingInfo.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/MetaMappingInfo.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/MetaMappingInfo.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ResponseCodeUrlMappingVisitor.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/ResponseCodeUrlMappingVisitor.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/AbstractGrailsControllerUrlMappings.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/AbstractGrailsControllerUrlMappings.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/GrailsControllerUrlMappingInfo.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/GrailsControllerUrlMappingInfo.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/UrlMappingsHandlerMapping.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/UrlMappingsHandlerMapping.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/UrlMappingsInfoHandlerAdapter.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/mvc/UrlMappingsInfoHandlerAdapter.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/reporting/AnsiConsoleUrlMappingsRenderer.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/reporting/AnsiConsoleUrlMappingsRenderer.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/reporting/UrlMappingsReportCommand.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/reporting/UrlMappingsReportCommand.groovy", [])
    }

    void "test grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/servlet/UrlMappingsErrorPageCustomizer.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/main/groovy/org/grails/web/mapping/servlet/UrlMappingsErrorPageCustomizer.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/AbstractUrlMappingsSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/AbstractUrlMappingsSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/DefaultActionUrlMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/DefaultActionUrlMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/DoubleWildcardUrlMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/DoubleWildcardUrlMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/GroupedUrlMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/GroupedUrlMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/MandatoryParamMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/MandatoryParamMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/OverlappingUrlMappingsMatchingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/OverlappingUrlMappingsMatchingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/RegisterUrlMappingsAtRuntimeSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/RegisterUrlMappingsAtRuntimeSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/RestfulResourceMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/RestfulResourceMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/UrlMappingSizeConstraintSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/UrlMappingSizeConstraintSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/UrlMappingsWithOptionalExtensionSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/UrlMappingsWithOptionalExtensionSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/grails/web/mapping/VersionedResourceMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/grails/web/mapping/VersionedResourceMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/CachingLinkGeneratorSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/CachingLinkGeneratorSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/DefaultUrlCreatorTests.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/DefaultUrlCreatorTests.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorWithFormatSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorWithFormatSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorWithUrlMappingsSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/LinkGeneratorWithUrlMappingsSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/OverlappingParametersReverseMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/OverlappingParametersReverseMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/RegexUrlMappingTests.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/RegexUrlMappingTests.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/StaticAndWildcardMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/StaticAndWildcardMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsBindingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsBindingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsHolderComparatorSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsHolderComparatorSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsWithHttpMethodSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/UrlMappingsWithHttpMethodSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/mvc/UrlMappingsHandlerMappingSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/mvc/UrlMappingsHandlerMappingSpec.groovy", [])
    }

    void "test grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/reporting/AnsiConsoleUrlMappingsRendererSpec.groovy"() {
        unzipAndTest("grails-web-url-mappings/src/test/groovy/org/grails/web/mapping/reporting/AnsiConsoleUrlMappingsRendererSpec.groovy", [])
    }

    void "test grails-web/build.gradle"() {
        unzipAndTest("grails-web/build.gradle", [])
    }

    void "test grails-web/src/main/groovy/grails/web/servlet/plugins/GrailsWebPluginManager.groovy"() {
        unzipAndTest("grails-web/src/main/groovy/grails/web/servlet/plugins/GrailsWebPluginManager.groovy", [])
    }

    void "test grails-web/src/main/groovy/org/grails/web/servlet/HttpServletRequestExtension.groovy"() {
        unzipAndTest("grails-web/src/main/groovy/org/grails/web/servlet/HttpServletRequestExtension.groovy", [])
    }

    void "test grails-web/src/main/groovy/org/grails/web/servlet/HttpServletResponseExtension.groovy"() {
        unzipAndTest("grails-web/src/main/groovy/org/grails/web/servlet/HttpServletResponseExtension.groovy", [])
    }

    void "test grails-web/src/main/groovy/org/grails/web/servlet/HttpSessionExtension.groovy"() {
        unzipAndTest("grails-web/src/main/groovy/org/grails/web/servlet/HttpSessionExtension.groovy", [])
    }

    void "test grails-web/src/main/groovy/org/grails/web/servlet/ServletContextExtension.groovy"() {
        unzipAndTest("grails-web/src/main/groovy/org/grails/web/servlet/ServletContextExtension.groovy", [])
    }

    void "test grails-web/src/test/groovy/grails/web/context/ServletContextHolderSpec.groovy"() {
        unzipAndTest("grails-web/src/test/groovy/grails/web/context/ServletContextHolderSpec.groovy", [])
    }

    void "test grails-web/src/test/groovy/org/grails/web/servlet/ServletRequestXhrApiSpec.groovy"() {
        unzipAndTest("grails-web/src/test/groovy/org/grails/web/servlet/ServletRequestXhrApiSpec.groovy", [])
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

    public static final String ZIP_PATH = "$TestUtils.RESOURCES_PATH/grails-3.2.0/grails-3.2.0-allsources.zip";

}
