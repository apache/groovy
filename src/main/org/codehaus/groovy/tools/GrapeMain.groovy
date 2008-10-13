/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.tools

import groovy.grape.Grape
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message

arg = args as List

// when we go to Commons-CLI 2.0 we can improve this by using commands
def options = []
argMatcher = ~/^--?(.*)$/

Iterator iter = arg.iterator()
while (iter.hasNext()) {
    def m = (iter.next() =~ argMatcher)
    if (m.matches()) {
        options += m.group(1)
        iter.remove()
    }
}


switch (arg[0]) {
    case 'install':
        if (arg.size() > 4 || arg.size() < 3) {
            println 'install requires two or three arguments, <group> <module> [<version>]'
            break
        }
        def ver = '*'
        if (arg.size() == 4) {
            ver = arg[3]
        }

        // set the instance so we can re-set the logger
        Grape.getInstance()
        try {
            // set the logger to louder
            Message.setDefaultLogger(new DefaultMessageLogger(2))
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            // doesn't matter, we just won't turn up ivy's logging
        }
        def ex = Grape.grab(autoDownload: true, group:arg[1], module:arg[2], version:ver, noExceptions:true)
        if (ex) {
            println "An error occured : $ex"
        }
        break

    case 'list':
        println ""

        int moduleCount = 0
        int versionCount = 0

        Grape.enumerateGrapes().each {String groupName, Map group ->
            group.each  { String moduleName, List<String> versions ->
                println "$groupName $moduleName  $versions"
                moduleCount++
                versionCount += versions.size()
            }
        }
        println ""
        println "$moduleCount Grape modules cached"
        println "$versionCount Grape module versions cached"
        break

    case 'resolve':

    // set the instance so we can re-set the logger
    Grape.getInstance()
    try {
        // set the logger to louder
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_DEBUG))
    } catch (Throwable e) {
        e.printStackTrace(System.out);
        // doesn't matter, we just won't turn up ivy's logging
    }

        if ((arg.size() % 3) != 1) {
            println 'There need to be a multiple of three arguments: (group module version)+'
            break
        }
        if (args.size() < 4) {
            println 'At least one Grape reference is required'
            break
        }
        def before, between, after

        if ('ant' in options) {
            before = '<pathelement location="'
            between = '">\n<pathelement location="'
            after = '">'
        } else if ('dos' in options) {
            before = 'set CLASSPATH='
            between = ';'
            after = ''
        } else if ('shell' in options) {
            before = 'export CLASSPATH='
            between = ':'
            after = ''
        } else {
            before = ''
            between = '\n'
            after = '\n'
        }

        iter = arg.iterator()
        iter.next()
        def params = [[:]]
        while (iter.hasNext()) {
            params.add([group:iter.next(), module:iter.next(), version:iter.next()])
        }

        try {
            def results = []
            for (URI uri : Grape.resolve(*params)) {
                if (uri.scheme == 'file') {
                    results += new File(uri).path
                } else {
                    results += uri.toASCIIString()
                }
            }

            if (results) {
                println "${before}${results.join(between)}${after}"
            } else {
                println 'Nothing was resolved'
            }
        } catch (Exception e) {
            println "Error in resolve:\n\t$e.message"
            if (e.message =~ /unresolved dependency/) println "Perhaps the grape is not installed?"
        }
        break
}



