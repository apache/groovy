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
package org.codehaus.groovy.tools.groovydoc

import groovy.cli.internal.CliBuilderInternal
import groovy.io.FileType
import org.apache.groovy.util.SystemUtil
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.MessageSource

/**
 * Main CLI entry-point for <tt>groovydoc</tt>.
 */
class Main {
    private static final MessageSource messages = new MessageSource(Main)
    private static File styleSheetFile;
    private static List<File> addStylesheetFiles = []
    private static File overviewFile
    private static File destDir
    private static String windowTitle
    private static String docTitle
    private static String header
    private static String footer
    private static String charset
    private static String fileEncoding
    private static Boolean author
    private static Boolean noScripts
    private static Boolean noMainForScripts
    private static Boolean noTimestamp
    private static Boolean noVersionStamp
    private static Boolean noIndex
    private static Boolean noDeprecatedList
    private static Boolean noHelp
    private static String syntaxHighlighter
    private static String theme
    private static Boolean privateScope
    private static Boolean packageScope
    private static Boolean publicScope
    private static Boolean protectedScope
    private static Boolean debug = false
    private static Boolean showInternal
    private static String[] sourcepath
    private static String javaVersion
    private static List<String> sourceFilesToDoc
    private static List<String> remainingArgs
    private static List<String> exclusions

    static void main(final String[] args) {
        IO io = new IO()
        Logger.io = io

        def cli = new CliBuilderInternal(usage: 'groovydoc [options] [packagenames] [sourcefiles]', writer: io.out, posix: false,
                header: messages['cli.option.header'])

        cli._(names: ['-h', '-help', '--help'], messages['cli.option.help.description'])
        cli._(longOpt: 'version', messages['cli.option.version.description'])
        cli.verbose(messages['cli.option.verbose.description'])
        cli.quiet(messages['cli.option.quiet.description'])
        // TODO is debug needed?
        cli._(longOpt: 'debug', messages['cli.option.debug.description'])
        cli.classpath(messages['cli.option.classpath.description'])
        cli.cp(longOpt: 'classpath', messages['cli.option.cp.description'])
        cli.d(longOpt: 'destdir', args:1, argName: 'dir', messages['cli.option.destdir.description'])
        cli.author(messages['cli.option.author.description'])
        cli.noscripts(messages['cli.option.noscripts.description'])
        cli.nomainforscripts(messages['cli.option.nomainforscripts.description'])
        cli.notimestamp(messages['cli.option.notimestamp.description'])
        cli.noversionstamp(messages['cli.option.noversionstamp.description'])
        cli.noindex(messages['cli.option.noindex.description'])
        cli.nodeprecatedlist(messages['cli.option.nodeprecatedlist.description'])
        cli.nohelp(messages['cli.option.nohelp.description'])
        cli.syntaxHighlighter(args: 1, argName: 'name', messages['cli.option.syntaxHighlighter.description'])
        cli.theme(args: 1, argName: 'mode', messages['cli.option.theme.description'])
        cli.overview(args:1, argName: 'file', messages['cli.option.overview.description'])
        cli.public(messages['cli.option.public.description'])
        cli.protected(messages['cli.option.protected.description'])
        cli.package(messages['cli.option.package.description'])
        cli.private(messages['cli.option.private.description'])
        cli.charset(args:1, argName: 'charset', messages['cli.option.charset.description'])
        cli.fileEncoding(args:1, argName: 'charset', messages['cli.option.fileEncoding.description'])
        cli.windowtitle(args:1, argName: 'text', messages['cli.option.windowtitle.description'])
        cli.doctitle(args:1, argName: 'html', messages['cli.option.doctitle.description'])
        cli.header(args:1, argName: 'html', messages['cli.option.header.description'])
        cli.footer(args:1, argName: 'html', messages['cli.option.footer.description'])
        cli.exclude(args:1, argName: 'pkglist', messages['cli.option.exclude.description'])
        cli.stylesheetfile(args:1, argName: 'path', messages['cli.option.stylesheetfile.description'])
        cli.addStylesheet(args:1, argName: 'path', messages['cli.option.addStylesheet.description'])
        cli.sourcepath(args:1, argName: 'pathlist', messages['cli.option.sourcepath.description'])
        cli.javaVersion(args: 1, argName: 'javaVersion', messages['cli.option.javaVersion.description'])
        cli.showInternal(messages['cli.option.showInternal.description'])

        def options = cli.parse(args)

        if (options.help) {
            cli.usage()
            return
        }

        if (options.version) {
            io.out.println(messages.format('cli.info.version', GroovySystem.version))
            return
        }

        if (options.stylesheetfile) {
            styleSheetFile = new File(options.stylesheetfile)
        }
        if (options.addStylesheet) {
            // CliBuilder with args:1 gives a single value; use comma separator
            // to accept multiple paths in one flag. Example:
            //   --add-stylesheet custom1.css,custom2.css
            options.addStylesheet.toString().tokenize(',').each { p ->
                addStylesheetFiles << new File(p.trim())
            }
        }

        if (options.overview) {
            overviewFile = new File(options.overview)
        }

        destDir = new File(options.d ?: '.')

        if (options.exclude) {
            exclusions = options.exclude.tokenize(':')
        }

        if (options.sourcepath) {
            def list = []
            options.sourcepaths.each {
                list.addAll(it.tokenize(File.pathSeparator))
            }
            sourcepath = list.toArray()
        }

        javaVersion = options.javaVersion
        author = Boolean.valueOf(options.author) ?: false
        noScripts = Boolean.valueOf(options.noscripts) ?: false
        noMainForScripts = Boolean.valueOf(options.nomainforscripts) ?: false
        noTimestamp = Boolean.valueOf(options.notimestamp) ?: false
        noVersionStamp = Boolean.valueOf(options.noversionstamp) ?: false
        noIndex = Boolean.valueOf(options.noindex) ?: false
        noDeprecatedList = Boolean.valueOf(options.nodeprecatedlist) ?: false
        noHelp = Boolean.valueOf(options.nohelp) ?: false
        syntaxHighlighter = options.syntaxHighlighter ?: 'none'
        theme = options.theme ?: 'auto'
        if (!(theme in ['auto', 'light', 'dark'])) {
            System.err.println "groovydoc: Error - -theme must be one of auto, light, dark (was: $theme)."
            cli.usage()
            System.exit(1)
        }
        showInternal = Boolean.valueOf(options.showInternal) ?: false
        packageScope = Boolean.valueOf(options.package) ?: false
        privateScope = Boolean.valueOf(options.private) ?: false
        protectedScope = Boolean.valueOf(options.protected) ?: false
        publicScope = Boolean.valueOf(options.public) ?: false

        int scopeCount = 0
        if (packageScope) scopeCount++
        if (privateScope) scopeCount++
        if (protectedScope) scopeCount++
        if (publicScope) scopeCount++
        if (scopeCount == 0) {
            protectedScope = true
        } else if (scopeCount > 1) {
            System.err.println "groovydoc: Error - More than one of -public, -private, -package, or -protected specified."
            cli.usage()
            System.exit(1)
        }

        windowTitle = options.windowtitle ?: ''
        docTitle = options.doctitle ?: ''
        header = options.header ?: ''
        footer = options.footer ?: ''
        charset = options.charset ?: ''
        fileEncoding = options.fileEncoding ?: ''

        if (options.Ds) {
            def values = options.Ds
            values.each {
                setSystemProperty(it as String)
            }
        }

        if (options.verbose) {
            io.verbosity = IO.Verbosity.VERBOSE
        }

        if (options.debug) {
            io.verbosity = IO.Verbosity.DEBUG
            debug = true
        }

        if (options.quiet) {
            io.verbosity = IO.Verbosity.QUIET
        }
        remainingArgs = options.arguments()
        if (!remainingArgs) {
            System.err.println "groovydoc: Error - No packages or classes specified."
            cli.usage()
            System.exit(1)
        }
        int errorCount
        try {
            errorCount = execute()
        } catch (Throwable t) {
            t.printStackTrace(System.err)
            System.exit(1)
            return
        }
        if (errorCount > 0) {
            System.err.println "groovydoc: Error - $errorCount source file(s) failed to parse."
            System.exit(1)
        }
    }

    static int execute() {
        Properties properties = new Properties()
        properties.put("windowTitle", windowTitle)
        properties.put("docTitle", docTitle)
        properties.put("footer", footer)
        properties.put("header", header)
        properties.put("charset", charset)
        properties.put("fileEncoding", fileEncoding)
        properties.put("privateScope", privateScope.toString())
        properties.put("protectedScope", protectedScope.toString())
        properties.put("publicScope", publicScope.toString())
        properties.put("packageScope", packageScope.toString())
        properties.put("author", author.toString())
        properties.put("processScripts", (!noScripts).toString())
        properties.put("includeMainForScripts", (!noMainForScripts).toString())
        properties.put("showInternal", showInternal.toString())
        // GROOVY-11941: expose additional stylesheet basenames to templates.
        properties.put("additionalStylesheets", addStylesheetFiles*.name.join(','))
        properties.put("timestamp", (!noTimestamp).toString())
        properties.put("versionStamp", (!noVersionStamp).toString())
        // GROOVY-11943: javadoc-parity disable flags for auxiliary top-level pages.
        properties.put("noIndex", noIndex.toString())
        properties.put("noDeprecatedList", noDeprecatedList.toString())
        properties.put("noHelp", noHelp.toString())
        // GROOVY-11938 stage 4: client-side syntax highlighter ("prism"|"none").
        properties.put("syntaxHighlighter", syntaxHighlighter)
        // GROOVY-11947: generator-level theme lock ("auto"|"light"|"dark").
        properties.put("theme", theme)
        properties.put("overviewFile", overviewFile?.absolutePath ?: "")
        String phaseOverride = SystemUtil.getSystemPropertySafe("groovydoc.phase.override")
        if (phaseOverride) properties.put("phaseOverride", phaseOverride)

        def links = new ArrayList<LinkArgument>();
        collectSourceFileNames(remainingArgs, sourcepath, exclusions)
        GroovyDocTool htmlTool = new GroovyDocTool(
                new ClasspathResourceManager(), // we're gonna get the default templates out of the dist jar file
                sourcepath,
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                links,
                javaVersion,
                properties
        )

        htmlTool.add(sourceFilesToDoc)
        FileOutputTool output = new FileOutputTool()
        htmlTool.renderToOutput(output, destDir.canonicalPath)

        // try to override the default stylesheet with custom specified one if needed
        if (styleSheetFile != null) {
            try {
                new File(destDir, "stylesheet.css").text = styleSheetFile.text
            } catch (IOException e) {
                println "Warning: Unable to copy specified stylesheet '" + styleSheetFile.absolutePath + "'. Using default stylesheet instead. Due to: " + e.message
            }
        }
        // GROOVY-11941: copy any additional stylesheets alongside the default,
        // preserving each file's basename.
        addStylesheetFiles.each { f ->
            try {
                new File(destDir, f.name).text = f.text
            } catch (IOException e) {
                System.err.println "Warning: Unable to copy additional stylesheet '${f.absolutePath}': ${e.message}"
            }
        }
        return htmlTool.errorCount
    }

    static collectSourceFileNames(List<String> remainingArgs, String[] sourceDirs, List<String> exclusions) {
        sourceFilesToDoc = []
        remainingArgs.each { String pkgOrFile ->
            if (pkgOrFile in exclusions) return
            File srcFile = new File(pkgOrFile)
            if (srcFile.exists() && srcFile.isFile()) {
                sourceFilesToDoc << pkgOrFile
                return
            }
            sourceDirs.each { dirStr ->
                def dir = new File(dirStr)
                def pkgOrFileSlashes = pkgOrFile.replace(".", "/")
                def candidate = new File(dir, pkgOrFile);
                if (candidate.exists() && candidate.isFile()) {
                    // assume it is some kind of file
                    sourceFilesToDoc << pkgOrFile
                }
                candidate = new File(dir, pkgOrFileSlashes);
                if (candidate.exists() && candidate.isDirectory()) {
                    // TODO handle other extensions too, make configurable
                    candidate.eachFileMatch(FileType.FILES, ~/.*\.(?:groovy|java)/) { File f ->
                        sourceFilesToDoc << pkgOrFileSlashes + "/" + f.getName()
                    }
                }
            }
        }
    }

}
