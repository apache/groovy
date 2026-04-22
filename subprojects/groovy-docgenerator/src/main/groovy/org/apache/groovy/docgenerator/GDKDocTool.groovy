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
package org.apache.groovy.docgenerator

import groovy.cli.internal.CliBuilderInternal
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager
import org.codehaus.groovy.tools.groovydoc.FileOutputTool
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.codehaus.groovy.tools.groovydoc.LinkArgument
import org.codehaus.groovy.tools.groovydoc.PreLanguageRewriter
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo

/**
 * Runs groovydoc programmatically against a tree of mock source files produced by
 * {@link MockSourceGenerator}, writing the GDK documentation to the output dir.
 * Uses {@link GroovyDocTool} directly rather than shelling out to
 * {@link org.codehaus.groovy.tools.groovydoc.Main} so template overrides and
 * link-arg wiring can be driven from Groovy code.
 */
class GDKDocTool {

    String title
    File sourceDir
    File outputDir
    List<LinkArgument> links = []
    String preLanguage = 'groovy'
    String syntaxHighlighter = 'prism'
    String theme = 'auto'

    int run() {
        if (!sourceDir.directory) {
            throw new IllegalStateException("source directory not found: $sourceDir")
        }
        outputDir.mkdirs()

        def props = new Properties()
        props.put('windowTitle', title ?: 'Groovy JDK')
        props.put('docTitle', title ?: 'Groovy JDK')
        props.put('title', title ?: 'Groovy JDK')
        props.put('header', title ?: 'Groovy JDK')
        props.put('charset', 'UTF-8')
        props.put('fileEncoding', 'UTF-8')
        props.put('author', 'false')
        props.put('timestamp', 'false')
        props.put('versionStamp', 'false')
        props.put('syntaxHighlighter', syntaxHighlighter)
        props.put('theme', theme)
        props.put('publicScope', 'true')

        def tool = new GroovyDocTool(
                new ClasspathResourceManager(),
                [sourceDir.absolutePath] as String[],
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                links,
                null, // default to running JVM's version
                props
        )

        def sourceFiles = collectSourceFiles(sourceDir)
        tool.add(sourceFiles)
        tool.renderToOutput(new FileOutputTool(), outputDir.canonicalPath)

        PreLanguageRewriter.rewriteDirectory(outputDir, preLanguage)

        def manifest = new File(sourceDir, MockSourceGenerator.MANIFEST_FILE)
        PrimitiveNameRewriter.rewrite(outputDir, manifest)

        tool.errorCount
    }

    private static List<String> collectSourceFiles(File root) {
        def out = []
        root.eachFileRecurse { f ->
            if (f.file && f.name.endsWith('.java')) {
                def rel = root.toPath().relativize(f.toPath()).toString()
                out << rel
            }
        }
        out
    }

    static void main(String... args) {
        def cli = new CliBuilderInternal(usage: 'GDKDocTool [options]', posix: false)
        cli.help(longOpt: 'help', 'Print this help')
        cli.s(longOpt: 'sourceDir', args: 1, argName: 'path', 'Directory of mock source files')
        cli.o(longOpt: 'outputDir', args: 1, argName: 'path', 'Output directory')
        cli.title(longOpt: 'title', args: 1, argName: 'text', 'Title for generated docs')
        cli.link(args: 2, valueSeparator: '=', argName: 'comma-separated-package-prefixes=url',
                 'Link JDK / gapi packages to external javadoc')
        def options = cli.parse(args)
        if (options.help || !options.sourceDir || !options.outputDir) {
            cli.usage()
            return
        }

        def tool = new GDKDocTool()
        tool.sourceDir = new File(options.sourceDir as String)
        tool.outputDir = new File(options.outputDir as String)
        tool.title = options.title ?: 'Groovy JDK'
        if (options.links) {
            // cli.link(args:2, valueSeparator: '=') returns alternating prefix, url pairs
            def pairs = options.links.collate(2)
            pairs.each { prefixesStr, url ->
                def la = new LinkArgument()
                la.packages = prefixesStr
                la.href = url
                tool.links << la
            }
        }

        System.exit(tool.run())
    }
}
