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

import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.JavaParameter
import com.thoughtworks.qdox.model.Type
import groovy.cli.internal.CliBuilderInternal
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.MessageSource

import java.text.BreakIterator
import java.util.concurrent.ConcurrentHashMap

/**
 * Generate documentation about the methods provided by the Groovy Development Kit
 * that enhance the standard JDK classes.
 */
class DocGenerator {
    private static final MessageSource messages = new MessageSource(DocGenerator)
    private static final Logger log = Logger.create(DocGenerator)
    private static final Comparator SORT_KEY_COMPARATOR = [compare: { a, b -> return a.sortKey.compareTo(b.sortKey) }] as Comparator
    private static final Map<String, Object> CONFIG = new ConcurrentHashMap<String, Object>();

    List<File> sourceFiles
    File outputDir
    DocSource docSource

    DocGenerator(List<File> sourceFiles, File outputFolder) {
        this.sourceFiles = sourceFiles
        this.outputDir = outputFolder
        this.docSource = parseSource(sourceFiles)
    }

    /**
     * Parse the *GroovyMethods (DGM) classes to build a graph representing the structure of the class,
     * with its methods, javadoc comments and tags.
     */
    private static DocSource parseSource(List<File> sourceFiles) {
        JavaDocBuilder builder = new JavaDocBuilder()
        sourceFiles.each {
            if (it.exists()) {
                builder.addSource(it.newReader())
                log.debug "adding reader for $it"
            } else {
                log.debug "not found, skipping: $it.path"
            }
        }

        def methods = builder.sources.collectMany { source ->
            source.classes.collectMany { aClass ->
                aClass.methods.findAll { !it.annotations.any { it.type.fullyQualifiedName == 'java.lang.Deprecated' } }
            }
        }

        def docSource = new DocSource()
        methods.each { JavaMethod method ->
            if (!method.isPublic() || !method.isStatic()) {
                return // skip it
            }

            def firstParam = method.parameters[0]
            def firstParamType = firstParam.resolvedValue.isEmpty() ? firstParam.type : new Type(firstParam.resolvedValue, 0, firstParam.parentClass)
            docSource.add(firstParamType, method)
        }
        docSource.populateInheritedMethods()
        return docSource
    }

    /**
     * Builds an HTML page from the structure of DefaultGroovyMethods.
     */
    void generateAll() {
        def engine = new SimpleTemplateEngine()

        // the index.html
        def indexTemplate = createTemplate(engine, 'index.html')
        new File(outputDir, 'index.html').withWriter {
            it << indexTemplate.make(title: CONFIG.title)
        }

        // the overview-summary.html
        def overviewTemplate = createTemplate(engine, 'overview-summary.html')
        new File(outputDir, 'overview-summary.html').withWriter {
            it << overviewTemplate.make(title: CONFIG.title)
        }

        // the overview-frame.html
        def overviewFrameTemplate = createTemplate(engine, 'template.overview-frame.html')
        new File(outputDir, 'overview-frame.html').withWriter {
            def docPackagesExceptPrimitiveType = docSource.packages.findAll { !it.primitive }
            it << overviewFrameTemplate.make(packages: docPackagesExceptPrimitiveType, title: CONFIG.title)
        }

        // the package-list
        new File(outputDir, 'package-list').withWriter { writer ->
            docSource.packages*.name.each { writer.println it }
        }

        // the allclasses-frame.html
        def allClassesTemplate = createTemplate(engine, 'template.allclasses-frame.html')
        new File(outputDir, 'allclasses-frame.html').withWriter {
            it << allClassesTemplate.make(docTypes: docSource.allDocTypes, title: CONFIG.title)
        }

        // the package-frame.html and package-summary.html for each package
        def packageFrameTemplate = createTemplate(engine, 'template.package-frame.html')
        def packageSummaryTemplate = createTemplate(engine, 'template.package-summary.html')
        docSource.packages.each { DocPackage docPackage ->
            def dir = DocUtil.createPackageDirectory(outputDir, docPackage.name)
            new File(dir, 'package-frame.html').withWriter {
                it << packageFrameTemplate.make(docPackage: docPackage, title: CONFIG.title)
            }
            new File(dir, 'package-summary.html').withWriter {
                it << packageSummaryTemplate.make(docPackage: docPackage, title: CONFIG.title)
            }
        }

        // the class.html for each class
        def classTemplate = createTemplate(engine, 'template.class.html')
        docSource.allDocTypes.each { DocType docType ->
            def dir = DocUtil.createPackageDirectory(outputDir, docType.packageName)
            new File(dir, docType.simpleClassName + '.html').withWriter {
                it << classTemplate.make(docType: docType, title: CONFIG.title)
            }
        }

        // the index-all.html
        def indexAllTemplate = createTemplate(engine, 'template.index-all.html')
        new File(outputDir, 'index-all.html').withWriter {
            it << indexAllTemplate.make('indexMap': generateIndexMap(), title: CONFIG.title)
        }

        // copy resources
        ['groovy.ico', 'stylesheet.css'].each { String resource ->
            new File(outputDir, resource) << getClass().getResource(resource).bytes
        }
    }

    private Template createTemplate(TemplateEngine templateEngine, String resourceFile) {
        def resourceUrl = getClass().getResource(resourceFile)
        return templateEngine.createTemplate(resourceUrl.text)
    }

    /**
     * Generate an index map for index-all.html.
     * <p>
     * This method creates a index map indexed by the first letter of the
     * method in upper case, the map value is a list of methods.
     * <p>
     * e.g.: 'A' : [ m1, m2, m3 .. ]
     * The values m1, m2, m3 are sorted by the method name, and the parameter signature.
     * The method names of m1, m2, m3 start either with 'a', or 'A'.
     *
     * @return indexMap
     */
    private Map generateIndexMap() {
        def indexItems = []
        docSource.allDocTypes.each { DocType docType ->
            // the class
            indexItems << [
                index: docType.simpleClassName.capitalize()[0],
                docType: docType,
                sortKey: docType.sortKey,
            ]

            // the methods
            docType.docMethods.each { DocMethod docMethod ->
                indexItems << [
                    index: docMethod.javaMethod.name.capitalize()[0],
                    docType: docType,
                    docMethod: docMethod,
                    sortKey: docMethod.sortKey
                ]
            }
        }
        def indexMap = new TreeMap().withDefault { new TreeSet(SORT_KEY_COMPARATOR) }
        for (indexItem in indexItems) {
            indexMap[indexItem['index']] << indexItem
        }
        return indexMap
    }

    /**
     * Main entry point.
     */
    static void main(String... args) {
        def cli = new CliBuilderInternal(usage : 'DocGenerator [options] [sourcefiles]', posix:false)
        cli.help(longOpt: 'help', messages['cli.option.help.description'])
        cli._(longOpt: 'version', messages['cli.option.version.description'])
        cli.o(longOpt: 'outputDir', args:1, argName: 'path', messages['cli.option.output.dir.description'])
        cli.title(longOpt: 'title', args:1, argName: 'text', messages['cli.option.title.description'])
        cli.link(args:2, valueSeparator:'=', argName:'comma-separated-package-prefixes=url',
                messages['cli.option.link.patterns.description'])
        def options = cli.parse(args)

        if (options.help) {
            cli.usage()
            return
        }

        if (options.links && options.links.size() % 2 == 1) {
            throw new IllegalArgumentException("Links should be specified in pattern=url pairs")
        }

        if (options.version) {
            println messages.format('cli.info.version', GroovySystem.version)
            return
        }

        def start = System.currentTimeMillis()

        def outputDir = new File(options.outputDir ?: "target/html/groovy-jdk")
        outputDir.mkdirs()
        CONFIG.title = options.title ?: "Groovy JDK"
        if (options.links) {
            CONFIG.links = options.links.collate(2).collectMany{ prefixes, url -> prefixes.tokenize(',').collect{[it, url]} }.collectEntries()
        }
        CONFIG.locale = Locale.default  // TODO allow locale to be passed in

        def srcFiles = options.arguments().collect { DocUtil.sourceFileOf(it) }
        try {
            DefaultGroovyMethods.ADDITIONAL_CLASSES.each { aClass ->
                def className = aClass.name.replaceAll(/\$.*/, '')
                def additionalFile = DocUtil.sourceFileOf(className)
                if (srcFiles.every { it.canonicalPath != additionalFile.canonicalPath }) {
                    srcFiles << additionalFile
                }
            }
        } catch (MissingPropertyException e) { // TODO is it still needed?
            // no call site change available, so ignore it
            log.error e.message, e
        }

        def docGen = new DocGenerator(srcFiles, outputDir)
        docGen.generateAll()

        def end = System.currentTimeMillis()
        log.debug "Done. Took ${end - start} milliseconds."
    }

    private static class DocSource {
        SortedSet<DocPackage> packages = new TreeSet<DocPackage>(SORT_KEY_COMPARATOR)

        void add(Type type, JavaMethod javaMethod) {
            DocType tempDocType = new DocType(type: type)

            DocPackage aPackage = packages.find { it.name == tempDocType.packageName }
            if (!aPackage) {
                aPackage = new DocPackage(name: tempDocType.packageName)
                packages << aPackage
            }

            DocType docType = aPackage.docTypes.find { it.fullyQualifiedClassName == tempDocType.fullyQualifiedClassName }
            if (!docType) {
                docType = tempDocType
                aPackage.docTypes << docType
            }

            def docMethod = new DocMethod(declaringDocType: docType, javaMethod: javaMethod)
            docType.docMethods << docMethod
        }

        void populateInheritedMethods() {
            def allTypes = allDocTypes.collectEntries{ [it.fullyQualifiedClassName, it] }
            allTypes.each { name, docType ->
                if (name.endsWith('[]') || name.startsWith('primitive-types')) return
                Type next = docType.javaClass.superClass
                while (next != null) {
                    if (allTypes.keySet().contains(next.value)) {
                        docType.inheritedMethods[allTypes[next.value]] = allTypes[next.value].docMethods
                    }
                    next = next.javaClass.superClass
                }
                def remaining = docType.javaClass.implementedInterfaces.toList()
                while (!remaining.isEmpty()) {
                    def nextInt = remaining.remove(0)
                    if (allTypes.keySet().contains(nextInt.fullyQualifiedName)) {
                        docType.inheritedMethods[allTypes[nextInt.fullyQualifiedName]] = allTypes[nextInt.fullyQualifiedName].docMethods
                    }
                    remaining.addAll(nextInt.implementedInterfaces.toList())
                }
            }
        }

        SortedSet<DocType> getAllDocTypes() {
            def allSet = new TreeSet(SORT_KEY_COMPARATOR)
            allSet.addAll(packages.collectMany { it.docTypes })
            return allSet
        }
    }

    private static class DocPackage {
        static final String PRIMITIVE_TYPE_PSEUDO_PACKAGE = 'primitive-types'
        String name
        SortedSet<DocType> docTypes = new TreeSet<DocType>(SORT_KEY_COMPARATOR)

        boolean isPrimitive() {
            name == PRIMITIVE_TYPE_PSEUDO_PACKAGE
        }

        String getSortKey() {
            name
        }
    }

    private static class DocType {
        private Type type
        final String shortComment = "" // empty because cannot get a comment of JDK
        SortedSet<DocMethod> docMethods = new TreeSet<DocMethod>(SORT_KEY_COMPARATOR)
        Map<String, List<DocMethod>> inheritedMethods = new LinkedHashMap<String, List<DocMethod>>()

        JavaClass getJavaClass() {
            type.javaClass
        }

        String getPackageName() {
            if (type.primitive) {
                return DocPackage.PRIMITIVE_TYPE_PSEUDO_PACKAGE
            }
            def fqcn = fullyQualifiedClassName
            if (fqcn.indexOf(".") < 0) {
                return ""
            }
            fqcn.replaceAll(/\.[^.]*$/, '')
        }

        String getSimpleClassName() {
            fullyQualifiedClassName.replaceAll(/^.*\./, '')
        }

        String getFullyQualifiedClassName() {
            if (type.primitive) {
                return DocPackage.PRIMITIVE_TYPE_PSEUDO_PACKAGE + '.' + type.toString()
            }
            DocUtil.resolveJdkClassName(type.toString())
        }

        boolean isInterface() {
            type.javaClass.isInterface()
        }

        String getSortKey() {
            simpleClassName + ' ' + fullyQualifiedClassName
        }

        String linkAnchor(DocType otherDocType) {
            DocUtil.getLinkAnchor(otherDocType.fullyQualifiedClassName, packageName)
        }
    }

    private static class DocMethod {
        DocType declaringDocType
        JavaMethod javaMethod

        String getName() {
            javaMethod.name
        }

        /**
         * Retrieve the parameters of the method.
         *
         * @param method a method
         * @return a list of parameters without the first one
         */
        List<JavaParameter> getParameters() {
            if (javaMethod.getParameters().size() > 1) {
                return javaMethod.getParameters().toList()[1..-1]
            }
            return []
        }

        String getParametersSignature() {
            parameters.collect { DocUtil.resolveJdkClassName(it.type.toString()) }.join(", ")
        }

        String getParametersDocUrl() {
            parameters.collect { "${DocUtil.getLinkAnchor(it.type.toString(), declaringDocType.packageName)} $it.name" }.join(", ")
        }

        String getReturnTypeDocUrl() {
            def returnType = javaMethod.returnType
            def resolvedReturnType = (returnType) ? DocUtil.resolveJdkClassName(returnType.toString()) : ""
            DocUtil.getLinkAnchor(resolvedReturnType, declaringDocType.packageName)
        }

        String getComment() {
            DocUtil.formatJavadocText(javaMethod.comment ?: '', declaringDocType.packageName)
        }

        String getShortComment() {
            DocUtil.formatJavadocText(DocUtil.getFirstSentence(javaMethod.comment ?: ''), declaringDocType.packageName)
        }

        String getReturnComment() {
            DocUtil.formatJavadocText(javaMethod.getTagByName("return")?.value ?: '', declaringDocType.packageName)
        }

        Map getParameterComments() {
            javaMethod.getTagsByName("param").drop(1).collectEntries { // first arg is the "real this"
                def name = it.value.replaceAll(/ .*/, '')
                def comment = DocUtil.formatJavadocText(it.value.replaceAll(/^\w*/, ''), declaringDocType.packageName)
                [name, comment]
            }
        }

        List<String> getSeeComments() {
            javaMethod.getTagsByName("see").collect { DocUtil.getLinkAnchor(it.value, declaringDocType.packageName) }
        }

        String getSinceComment() {
            javaMethod.getTagByName("since")?.value
        }

        boolean isStatic() {
            javaMethod.parentClass.name == 'DefaultGroovyStaticMethods'
        }

        String getSortKey() {
            name + ' ' + parametersSignature + ' ' + declaringDocType.fullyQualifiedClassName
        }
    }

    private static class DocUtil {
        static String resolveJdkClassName(String className) {
            if (className in 'A'..'Z') {
                return 'java.lang.Object'
            }
            if (className in ('A'..'Z').collect{ it + '[]' }) {
                return 'java.lang.Object[]'
            }
            return className
        }

        static String formatJavadocText(String text, String packageName) {
            linkify(codify(text), packageName)
        }

        private static String linkify(String text, String packageName) {
            text.replaceAll(/\{@link\s+([^}]*)\s*\}/) { String all, String destination ->
                // A class name cannot be omitted: https://issues.apache.org/jira/browse/GROOVY-6740 TODO: remove DocUtil once fixed?
                DocUtil.getLinkAnchor(destination, packageName)
            }
        }

        private static String codify(String text) {
            text.replaceAll(/\{@code\s+([^}]*)\s*\}/) { String all, String code -> """<code>${code}</code>""" }
        }

        static String getFirstSentence(String text) {
            def boundary = BreakIterator.getSentenceInstance(CONFIG.locale)
            boundary.setText(text)
            int start = boundary.first()
            int end = boundary.next()
            if (start > -1 && end > -1) {
                return text.substring(start, end)
            }
            return text
        }

        static String getLinkAnchor(String destination, String originPackageName) {
            // resolving a destination if it's in GDK.
            def inGdk = destination.startsWith('#')
            if (inGdk) {
                (destination =~ /#([^(]*)\(([^)]+)\)/).each { String all, String name, String argsText ->
                    def args = argsText.split(/,\s?/).toList()
                    def first = args.remove(0)
                    destination = "$first#$name(${args.join(', ')})".toString()
                }
            }

            def fullyQualifiedClassName = resolveJdkClassName(destination.replaceFirst(/#.*$/, ''))
            def methodSignatureHash = destination.replaceFirst(/^[^#]*/, '')
            def simpleClassName = fullyQualifiedClassName.replaceFirst(/.*\./, "")
            def packageName = fullyQualifiedClassName.replaceFirst(/.?[^.]+$/, '')

            // If a package is empty, a destination text should be just returned
            // because a link to the right documentation location cannot be made.
            if (packageName.empty) {
                return destination
            }

            def apiBaseUrl, title
            if (inGdk) {
                apiBaseUrl = '../' * (originPackageName.count('.') + 1)
                title = "GDK enhancement for ${fullyQualifiedClassName}"
            } else {
                title = "Class in $packageName"
                apiBaseUrl = './'
                String key = CONFIG.links.keySet().find{ packageName.startsWith(it) }
                if (key) {
                    apiBaseUrl = CONFIG.links[key]
                    if (apiBaseUrl.startsWith('..')) apiBaseUrl = '../' * (originPackageName.count('.') + 1) + apiBaseUrl

                }
            }

            def url = "${apiBaseUrl}${packageName.replace('.', '/')}/${simpleClassName}.html${methodSignatureHash}"
            return """<a href="$url" title="$title">${simpleClassName}${methodSignatureHash}</a>"""
        }

        static File createPackageDirectory(File outputDir, String packageName) {
            def packagePath = filePathOf(packageName)
            def dir = new File(outputDir, packagePath)
            dir.mkdirs()
            return dir
        }

        private static String filePathOf(String packageName) {
            def fileSep = File.separator
            // need to escape separator on windows for regex's sake
            if (fileSep == '\\') fileSep *= 2
            return packageName.replaceAll(/\./, fileSep)
        }

        static File sourceFileOf(String pathOrClassName) {
            // TODO don't hardcode like this
            if (pathOrClassName.contains("/")) {
                return new File(pathOrClassName)
            }
            new File("src/main/java/" + pathOrClassName.replace('.', '/') + ".java")
        }
    }
}
