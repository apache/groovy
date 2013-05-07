/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.commands

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'doc' command.
 *
 * @since 2.2.0
 * @author <a href="mailto:me@masatonagai.com">Masato Nagai</a>
 */
class DocCommand
    extends CommandSupport
{

    static String ENV_BROWSER = "BROWSER"
    static String ENV_BROWSER_GROOVYSH = "GROOVYSH_BROWSER"

    static int TIMEOUT_CONN = 1 * 1000 // ms
    static int TIMEOUT_READ = 1 * 1000 // ms

    DocCommand(final Shell shell) {
        super(shell, 'doc', '\\D')
    }

    Object execute(final List args) {
        if (args?.size() == 1) {
            doc(args[0])
            return
        }
        fail(messages.format('error.unexpected_args', args ? args.join(' ') : 'no arguments'))
    }

    void doc(String className) {
        def urls = urlsFor(className)
        if (urls.empty) {
            fail("Documentation for $className could not be found.")
        }
        // Print the URLs.
        // It is useful especially when the browsing fails.
        urls.each { url -> println url }
        browse(urls)
    }

    void browse(List urls) {
        def browser = System.getenv(ENV_BROWSER_GROOVYSH) ?: System.getenv(ENV_BROWSER)
        if (browser) {
            urls.each { url -> "$browser $url".execute() }
            return
        }
        try {
            def desktopClass = Class.forName("java.awt.Desktop")
            if (desktopClass.desktopSupported) {
                def desktop = desktopClass.desktop
                // class.enum fails with no such property error...
                if (desktop.isSupported(desktopClass.declaredClasses.find{ it.simpleName == "Action" }.BROWSE)) {
                    urls.each { url -> desktop.browse(url.toURI()) }
                }
                return
            }
        } catch (ClassNotFoundException e) {}
        fail("Browser could not be opened due to missing platform support for 'java.awt.Desktop'. " +
             "Please set a $ENV_BROWSER_GROOVYSH or $ENV_BROWSER environment variable " +
             "referring to the browser binary to solve this issue.")
    }

    boolean testUrl(URL url) {
        def conn = url.openConnection()
        conn.requestMethod = "HEAD"
        conn.connectTimeout = TIMEOUT_CONN
        conn.readTimeout = TIMEOUT_READ
        return conn.responseCode != 404
    }

    List urlsFor(String className) {
        def path = className.replaceAll(/\./, '/') + ".html"
        def urls = []
        if (className.matches(/^(java|javax)\..+/)) {
            def url = new URL("http://docs.oracle.com/javase/${System.getProperty("java.version")}/docs/api/$path")
            if (testUrl(url)) {
                urls << url
                url = new URL("http://groovy.codehaus.org/groovy-jdk/$path")
                if (testUrl(url)) {
                    urls << url
                }
            }
        } else if (className.matches(/^(groovy|org.codehaus.groovy|)\..+/)) {
            def url = new URL("http://groovy.codehaus.org/gapi/$path")
            if (testUrl(url)) {
                urls << url
            }
        }
        urls
    }
}

