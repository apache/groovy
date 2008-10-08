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
import groovy.grape.GrapeIvy
import org.apache.ivy.core.report.ArtifactDownloadReport
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message

Grape.initGrape()

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
        Message.setDefaultLogger(new DefaultMessageLogger(2))
        def ex = Grape.grab(group:arg[1], module:arg[2], version:ver, noExceptions:true)
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
        //TODO make this not specific to GrapeIvy

        GrapeIvy grapeIvy = new GrapeIvy()
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
            params.add(grapeIvy.createGrabRecord([group:iter.next(), module:iter.next(), version:iter.next()]))
        }

        try {
            ArtifactDownloadReport[] reports = grapeIvy.getDependencies(*params).getAllArtifactsReports()

            if (reports.length > 0) {
                def items = []
                for (ArtifactDownloadReport report in reports) {
                    if (report.localFile) {
                        items += report.localFile
                    }
                }
                println "${before}${items.join(between)}${after}"
            } else {
                println 'no jars were resolved'
            }
        } catch (Exception e) {
            println "Error in Resolve:\n\t$e.message"
        }
        break
}


