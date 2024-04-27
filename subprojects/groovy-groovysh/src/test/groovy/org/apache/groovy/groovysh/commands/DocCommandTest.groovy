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
package org.apache.groovy.groovysh.commands

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.apache.groovy.groovysh.Groovysh

/**
 * Tests for the {@link DocCommand} class.
 */
final class DocCommandTest extends CommandTestSupport {

    private DocCommand newDocCommand_sendHEADRequest(
            @ClosureParams(value=SimpleType, options='java.net.URL') Closure<Boolean> requestStrategy) {
        new DocCommand(new Groovysh()) {
            @Override
            boolean sendHEADRequest(URL url, String path) {
                requestStrategy.call(url)
            }
        }
    }

    private String getGroovyVersion() {
        GroovySystem.getVersion()
    }

    private String getJavaVersion() {
        Runtime.version().feature()
    }

    //--------------------------------------------------------------------------

    void testUrlsForJavaClass1() {
        def command = newDocCommand_sendHEADRequest{ return !it.host.contains('docs.groovy-lang.org') }

        // no module specified
        def urls = command.urlsFor('org.ietf.jgss.GSSContext')*.toString()
        assert urls.size() == 1
        assert urls[0] == "https://docs.oracle.com/en/java/javase/$javaVersion/docs/api/java.base/org/ietf/jgss/GSSContext.html"

        // yes module specified
        urls = command.urlsFor('org.ietf.jgss.GSSContext', 'java.security.jgss')*.toString()
        assert urls.size() == 1
        assert urls[0] == "https://docs.oracle.com/en/java/javase/$javaVersion/docs/api/java.security.jgss/org/ietf/jgss/GSSContext.html"
    }

    void testUrlsForJavaClass2() {
        def command = newDocCommand_sendHEADRequest{ return true }

        def urls = command.urlsFor('java.util.List')*.toString()

        assert urls.size() == 2
        assert urls[1] == "https://docs.groovy-lang.org/$groovyVersion/html/groovy-jdk/java/util/List.html"
        assert urls[0] == "https://docs.oracle.com/en/java/javase/$javaVersion/docs/api/java.base/java/util/List.html"
    }

    void testUrlsForGroovyClass() {
        def command = newDocCommand_sendHEADRequest{ return it.host.contains('docs.groovy-lang.org') }

        def urls = command.urlsFor('groovy.console.TextNode')*.toString()

        assert urls.size() == 2
        assert urls[0] == "https://docs.groovy-lang.org/$groovyVersion/html/gapi/groovy/console/TextNode.html"
        assert urls[1] == "https://docs.groovy-lang.org/$groovyVersion/html/groovy-jdk/groovy/console/TextNode.html"
    }

    void testUrlsForUnknownClass() {
        def command = newDocCommand_sendHEADRequest{ return false }

        def urls = command.urlsFor('com.dummy.List')*.toString()

        assert urls.isEmpty()
    }

    //--------------------------------------------------------------------------

    void testInitializeAWTDesktopPlatformSupportFlag() {
        def desktopClass = Class.forName('java.awt.Desktop')
        boolean hasSupport = desktopClass.desktopSupported
                && desktopClass.desktop.isSupported(desktopClass.declaredClasses.find{ it.simpleName == 'Action' }.BROWSE)

        assert DocCommand.hasAWTDesktopPlatformSupport == hasSupport
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

        command.browse([new URL("http://docs.oracle.com/javase/$javaVersion/docs/api/java/util/List.html")])

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

        command.browse([new URL("http://docs.oracle.com/javase/$javaVersion/docs/api/java/util/List.html")])

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
}
