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
import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.CommandRegistry

import java.awt.Desktop

/**
 * The 'doc' command.
 *
 * @version $Id$
 * @author <a href="mailto:me@masatonagai.com">Masato Nagai</a>
 */
class DocCommand
    extends CommandSupport
{
    DocCommand(final Shell shell) {
        super(shell, 'doc', '\\D')
    }

    Object execute(final List args) {
        if (args?.size() == 1) {
            doc(args[0])
            return
        }
        fail(messages.format('error.unexpected_args', args.join(' ')))
    }

    void doc(String className) {
        try {
            Class.forName(className)
        } catch (ClassNotFoundException e) {
            fail("$className is not found.")
        }
        def urls = urlsFor(className)
        if (urls.empty) {
            fail("$className is not documented.")
        }
        // Print the URLs.
        // It is useful especially when the browsing fails.
        urls.each { url -> println url }
        browse(urls)
    }

    void browse(List urls) {
        def browser = System.getenv("GROOVYSH_BROWSER") ?: System.getenv("BROWSER")
        if (browser) {
            urls.each { url -> "$browser $url".execute() }
            return
        }
        try {
            def desktopClass = Class.forName("java.awt.Desktop")
            if (desktopClass.desktopSupported) {
                def desktop = desktopClass.desktop
                urls.each { url -> desktop.browse(url.toURI()) }
                return
            }
        } catch (ClassNotFoundException e) {}
        fail("Desktop API is not supported. Set GROOVY_BROWSER or BROWSER environment variable.")
    }

    List urlsFor(String className) {
        def path = className.replaceAll(/\./, '/') + ".html"
        def urls = []
        if (className.matches(/^(java|javax)\..+/)) {
            def url = new URL("http://groovy.codehaus.org/groovy-jdk/$path")
            if (url.openConnection().responseCode != 404) {
                urls << url
            }
            url = new URL("http://docs.oracle.com/javase/${System.getProperty("java.version")}/docs/api/$path")
            if (url.openConnection().responseCode != 404) {
                urls << url
            }
        } else if (className.matches(/^(groovy|org.codehaus.groovy|)\..+/)) {
            def url = new URL("http://groovy.codehaus.org/gapi/$path")
            if (url.openConnection().responseCode != 404) {
                urls << url
            }
        }
        urls
    }
}

