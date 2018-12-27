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
package org.codehaus.groovy.tools.shell.commands

import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Tests for the {@link DocCommand} class.
 */
class DocCommandTest extends CommandTestSupport
{
    void testInitializeAWTDesktopPlatformSupportFlag() {
        def desktopClass = Class.forName('java.awt.Desktop')
        boolean hasSupport =
            desktopClass.desktopSupported &&
                    desktopClass.desktop.isSupported(desktopClass.declaredClasses.find { it.simpleName == 'Action' }.BROWSE)

        assert DocCommand.hasAWTDesktopPlatformSupport == hasSupport
    }

    void testUrlsForJavaOnlyClass() {
        def command = docCommandWithSendHEADRequestReturnValueOf { !it.host.contains('docs.groovy-lang.org') }

        def urls = command.urlsFor('org.ietf.jgss.GSSContext')

        assert urls ==
            [new URL("http://docs.oracle.com/javase/${simpleJavaVersion()}/docs/api/org/ietf/jgss/GSSContext.html")]
    }

    void testUrlsForJavaClass() {
        def command = docCommandWithSendHEADRequestReturnValueOf { true }

        def urls = command.urlsFor('java.util.List')

        assert urls ==
                [new URL("http://docs.oracle.com/javase/${simpleJavaVersion()}/docs/api/java/util/List.html"),
                 new URL("http://docs.groovy-lang.org/$GroovySystem.version/html/groovy-jdk/java/util/List.html")]
    }

    void testUrlsForGroovyClass() {
        def command = docCommandWithSendHEADRequestReturnValueOf { true }

        def urls = command.urlsFor('groovy.Dummy')

        assert urls ==
                [new URL("http://docs.groovy-lang.org/$GroovySystem.version/html/gapi/groovy/Dummy.html")]
    }

    void testUrlsForWithUnknownClass() {
        def command = docCommandWithSendHEADRequestReturnValueOf { false }

        def urls = command.urlsFor('com.dummy.List')

        assert urls.isEmpty()
    }

    void testFallbackToDesktopIfBrowserEnvIsMissing() {
        def browseWithAWT = false
        def command = new DocCommand(new Groovysh()) {
            protected String getBrowserEnvironmentVariable() {
                '' // there is not env variable for the browser
            }

            protected void browseWithAWT(List urls) {
                browseWithAWT = true
            }

            protected void browseWithNativeBrowser(String browser, List urls) {
                browseWithAWT = false
            }
        }
        DocCommand.hasAWTDesktopPlatformSupport = true
        DocCommand.desktop = [:]

        command.browse([new URL("http://docs.oracle.com/javase/${simpleJavaVersion()}/docs/api/java/util/List.html")])

        assert browseWithAWT
    }

    void testOpenBrowserIfBrowserEnvIsAvailable() {
        def browseWithNativeBrowser = false
        def command = new DocCommand(new Groovysh()) {
            protected String getBrowserEnvironmentVariable() {
                '/usr/local/bin/firefox'
            }

            protected void browseWithAWT(List urls) {
                browseWithNativeBrowser = false
            }

            protected void browseWithNativeBrowser(String browser, List urls) {
                browseWithNativeBrowser = true
            }
        }

        command.browse([new URL("http://docs.oracle.com/javase/${simpleJavaVersion()}/docs/api/java/util/List.html")])

        assert browseWithNativeBrowser
    }

    void testNormalizeClassName() {
        def command = new DocCommand(new Groovysh())

        assert 'java.util.List' == command.normalizeClassName(/java.util.List'/)
        assert 'java.util.List' == command.normalizeClassName(/'java.util.List'/)
        assert 'java.util.List' == command.normalizeClassName('java.util.List')
    }

    void testGetBrowserEnvironmentVariable() {
        def command = new DocCommand(new Groovysh())

        System.metaClass.static.getenv = { String variableName ->
            (variableName == DocCommand.ENV_BROWSER) ? 'firefox' : ''
        }

        assert command.browserEnvironmentVariable == 'firefox'

        System.metaClass.static.getenv = { String variableName ->
            (variableName == DocCommand.ENV_BROWSER_GROOVYSH) ? 'chrome' : ''
        }

        assert command.browserEnvironmentVariable == 'chrome'
    }

    private DocCommand docCommandWithSendHEADRequestReturnValueOf(Closure returnValue) {
        new DocCommand(new Groovysh()) {
            @Override
            boolean sendHEADRequest(URL url) {
                returnValue(url)
            }
        }
    }

    private simpleJavaVersion() {
        String javaVersion = System.getProperty('java.version')
        if (javaVersion.startsWith('1.')) {
            javaVersion.split(/\./)[1]
        } else {
            // java 9 and above
            javaVersion.replaceAll(/-.*/, '').split(/\./)[0]
        }
    }
}
