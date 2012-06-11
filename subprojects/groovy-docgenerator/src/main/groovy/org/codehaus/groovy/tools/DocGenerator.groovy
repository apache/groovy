/*
 * Copyright 2003-2012 the original author or authors.
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

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import com.thoughtworks.qdox.JavaDocBuilder
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * Generate documentation about the methods provided by the Groovy Development Kit
 * that enhance the standard JDK classes.
 *
 * @author Guillaume Laforge, John Wilson, Bernhard Huber, Paul King
 */
class DocGenerator {
    private static final String PRIMITIVE_TYPE_PSEUDO_PACKAGE = 'primitive-types'
    private static final Logger log = Logger.create(DocGenerator)
    private final String TITLE = "Groovy JDK"
    def sourceFiles = []
    File outputFolder
    JavaDocBuilder builder
    // categorize all groovy methods per core JDK class to which it applies
    def jdkEnhancedClasses = [:]
    def packages = [:]
    def sortedPackages

    DocGenerator(sourceFiles, File outputFolder) {
        this.sourceFiles = sourceFiles
        this.outputFolder = outputFolder
        parse()
    }

    /**
     * Parse the DefaultGroovyMethods class to build a graph representing the structure of the class,
     * with its methods, javadoc comments and tags.
     */
    private void parse() {
        builder = new JavaDocBuilder()
        sourceFiles.each {
            if (it.exists()) {
                builder.addSource(it.newReader())
                log.debug "adding reader for $it"
            } else log.debug "not found, skipping: $it.path"
        }

        def sources = builder.getSources()

        def methods = []
        sources.each {source ->
            def classes = source.getClasses()
            classes.each {aClass ->
                methods.addAll(aClass.methods.findAll { !it.annotations.any { it.type.fullQualifiedName == 'java.lang.Deprecated' } })
            }
        }

        for (method in methods) {
            if (method.isPublic() && method.isStatic()) {
                def parameters = method.getParameters()
                def jdkClass = parameters[0].getType().toString()
                if (jdkClass.equals('T') || jdkClass.equals('U') || jdkClass.equals('K') || jdkClass.equals('V') || jdkClass.equals('G')) {
                    jdkClass = 'java.lang.Object'
                } else if (jdkClass.equals('T[]')) {
                    jdkClass = 'java.lang.Object[]'
                }
                if (jdkClass.startsWith('groovy')) {
                    // nothing, skip it
                }
                else if (jdkEnhancedClasses.containsKey(jdkClass)) {
                    List l = jdkEnhancedClasses[jdkClass];
                    l.add(method)
                }
                else
                    jdkEnhancedClasses[jdkClass] = [method]
            }
        }

        jdkEnhancedClasses.keySet().each {className ->
            def thePackage = className.contains(".") ? className.replaceFirst(/\.[^\.]*$/, "") : ""
            if (!packages.containsKey(thePackage)) {
                packages[thePackage] = []
            }
            packages[thePackage] << className
        }
        sortedPackages = new TreeSet(packages.keySet())
    }

    /**
     * Builds an HTML page from the structure of DefaultGroovyMethods.
     */
    def generateNew() {
        def engine = new groovy.text.SimpleTemplateEngine()

        // the index
        def templateIndex = createTemplate(engine, 'index.html')
        def out = new File(outputFolder, 'index.html')
        def binding = [packages: sortedPackages]
        out.withWriter {
            it << templateIndex.make(binding)
        }
        // the overview
        def templateOverview = createTemplate(engine, 'overview-summary.html')
        out = new File(outputFolder, 'overview-summary.html')
        binding = [packages: sortedPackages]
        out.withWriter {
            it << templateOverview.make(binding)
        }

        def templateOverviewFrame = createTemplate(engine, 'template.overview-frame.html')
        out = new File(outputFolder, 'overview-frame.html')
        binding = [packages: sortedPackages, title: TITLE]
        out.withWriter {
            it << templateOverviewFrame.make(binding)
        }
        
        // the package list
        out = new File(outputFolder, 'package-list')
        out.withWriter { writer ->
            packages.keySet().findAll{ it }.each{ writer.println it }
        }
        
        // the allclasses-frame.html
        def templateAllClasses = createTemplate(engine, 'template.allclasses-frame.html')
        out = new File(outputFolder, 'allclasses-frame.html')
        def fixPrimitivePackage = {className -> className.contains('.') ? className : "${PRIMITIVE_TYPE_PSEUDO_PACKAGE}.$className"}
        binding = [classes: jdkEnhancedClasses.keySet().collect(fixPrimitivePackage).sort {it.replaceAll('.*\\.', '')}]
        out.withWriter {
            it << templateAllClasses.make(binding)
        }

        // the package-frame.html for each package
        def templatePackageFrame = createTemplate(engine, 'template.package-frame.html')
        packages.each {curPackage, packageClasses ->
            def packageName = curPackage ?: PRIMITIVE_TYPE_PSEUDO_PACKAGE
            generatePackageFrame(templatePackageFrame, packageName, packageClasses)
        }

        // the class.html for each class
        def templateClass = createTemplate(engine, 'template.class.html')
        packages.each {curPackage, packageClasses ->
            def packageName = curPackage ?: PRIMITIVE_TYPE_PSEUDO_PACKAGE
            packageClasses.each {
                generateClassDetails(templateClass, packageName, it)
            }
        }

        // the index-all.html
        def templateIndexAll = createTemplate(engine, 'template.index-all.html')
        out = new File(outputFolder, 'index-all.html')
        binding = ['indexMap': generateIndex(packages), title: TITLE]
        out.withWriter {
            it << templateIndexAll.make(binding)
        }
    }

    /**
     * Generate an index.
     * <p>
     * This method creates a index map indexed by the first letter of the
     * method in upper case, the map value is a list of methods.
     * <p>
     * e.g.: 'A' : [ m1, m2, m3 .. ]
     * The values m1, m2, m3 are sorted by the method name, and the parameter signature.
     * The method names of m1, m2, m3 start either with 'a', or 'A'.
     *
     * @return index
     */
    private def generateIndex(def packages) {
        def index = []
        packages.each {curPackage, packageClasses ->
            def packageName = curPackage ? curPackage : 'primitive-types'
            packageClasses.each {className ->
                def listOfMethods = jdkEnhancedClasses[className]
                listOfMethods.each {method ->
                    def methodName = method.name
                    final String simpleClassName = className.replaceAll('.*\\.', '')
                    index.add([
                            'index': methodName.getAt(0).toUpperCase(),
                            'packageName': packageName,
                            'simpleClassName': simpleClassName,
                            'class': packageName + '.' + simpleClassName,
                            'method': method,
                            'parametersSignature': getParametersDecl(method),
                            'shortComment': linkify(getFirstSentence(getComment(method)), curPackage),
                    ])
                }
            }
        }
        def indexMap = new TreeMap()
        def methodNameComparator = [compare: {a, b ->
            final String aMethodAndSignature = a.method.name + ' ' + getParametersDecl(a.method)
            final String bMethodAndSignature = b.method.name + ' ' + getParametersDecl(b.method)

            return aMethodAndSignature.compareTo(bMethodAndSignature)
        }] as Comparator

        for (indexEntry in index) {
            final String key = indexEntry['index']
            if (indexMap.containsKey(key)) {
                def indexEntryList = indexMap.get(key)
                indexEntryList.add(indexEntry)
            } else {
                final TreeSet indexEntryList = new TreeSet(methodNameComparator)
                indexEntryList.add(indexEntry)
                indexMap.put(key, indexEntryList)
            }
        }
        return indexMap
    }

    private getFirstSentence(text) {
        def boundary = java.text.BreakIterator.getSentenceInstance(Locale.getDefault()) // todo - allow locale to be passed in
        boundary.setText(text)
        int start = boundary.first()
        int end = boundary.next()
        if (start > -1 && end > -1) {
            text = text.substring(start, end)
        }
        text
    }

    private generateClassDetails(template, curPackage, aClass) {
        def packagePath = generatePackagePath(curPackage)
        def dir = new File(outputFolder, packagePath)
        dir.mkdirs()
        def out = new File(dir, aClass.replaceAll('.*\\.', '') + '.html')
        def listOfMethods = jdkEnhancedClasses[aClass].sort {it.name}
        def methods = []
        listOfMethods.each {method ->
            def parameters = method.getTagsByName("param").collect {
                [name: it.value.replaceAll(' .*', ''), comment: linkify(it.value.replaceAll('^\\w*', ''), curPackage)]
            }
            if (parameters)
                parameters.remove(0) // method is static, first arg is the "real this"

            def seeComments = method.getTagsByName("see").collect { [target: getDocUrl(it.value, curPackage)]}

            def returnType = getReturnType(method)
            def comment = getComment(method)
            def methodInfo = [
                    name: method.name,
                    comment: linkify(comment, curPackage),
                    shortComment: linkify(getFirstSentence(comment), curPackage),
                    returnComment: method.getTagByName("return")?.getValue() ?: '',
                    seeComments: seeComments,
                    returnTypeDocUrl: getDocUrl(returnType, curPackage),
                    parametersSignature: getParametersDecl(method),
                    parametersDocUrl: getParametersDocUrl(method, curPackage),
                    parameters: parameters,
                    isStatic: method.parentClass.name == 'DefaultGroovyStaticMethods',
                    since: method.getTagByName("since")?.getValue() ?: null
            ]
            methods << methodInfo
        }

        def binding = [
                className: aClass.replaceAll(/.*\./, ''),
                packageName: curPackage,
                methods: methods,
                title: TITLE
        ]
        out.withWriter {
            it << template.make(binding)
        }
    }

    private String getParametersDocUrl(method, curPackage) {
        getParameters(method).collect {"${getDocUrl(it.type.toString(), curPackage)} $it.name"}.join(", ")
    }

    private String getDocUrl(type, curPackage) {
        def inGdk = false
        if (type.startsWith('#')) {
            def matchNameArgs = /#([^(]*)\(([^)]+)\)/
            def m = type =~ matchNameArgs
            def name = m[0][1]
            def args = m[0][2].split(/,\s?/).toList()
            def first = args.remove(0)
            type = "$first#$name(${args.join(', ')})".toString()
            inGdk = true
        }
        if (type in ['T', 'U', 'V', 'K', 'V']) {
            type = "java.lang.Object"
        } else if (type == 'T[]') {
            type = "java.lang.Object[]"
        }
        if (!type.contains('.')) {
            return type
        }
        def target = type.split('#')
        def shortClassName = target[0].replaceAll(/.*\./, "")
        def packageName = (shortClassName.size() == target[0].size()) ? "DefaultPackage" : target[0][0..(-shortClassName.size() - 2)]
        shortClassName += (target.size() > 1 ? '#' + target[1].split('\\(')[0] : '')
        def apiBaseUrl, title
        if (inGdk) {
            apiBaseUrl = ""
            curPackage.split('\\.').size().times { apiBaseUrl += '../'}
            title = "GDK enhancement for ${target[0]}"
        } else if (type.startsWith("groovy") || type.startsWith("org.codehaus.groovy")) {
            apiBaseUrl = "http://groovy.codehaus.org/api/"
            title = "Groovy class in $packageName"
        } else {
            apiBaseUrl = "http://java.sun.com/j2se/1.5.0/docs/api/"
            title = "JDK class in $packageName"
        }

        def url = apiBaseUrl + target[0].replace('.', '/') + '.html' + (target.size() > 1 ? '#' + target[1] : '')
        return "<a href='$url' title='$title'>$shortClassName</a>"
    }

    private generatePackagePath(curPackage) {
        def fileSep = File.separator
        // need to escape separator on windows for regex's sake
        if (fileSep == '\\') fileSep *= 2
        return curPackage.replaceAll('\\.', fileSep)
    }

    private generatePackageFrame(templatePackageFrame, curPackage, packageClasses) {
        def packagePath = generatePackagePath(curPackage)
        def dir = new File(outputFolder, packagePath)
        dir.mkdirs()
        def out = new File(dir, 'package-frame.html')
        def binding = [classes: packageClasses.sort().collect {it.replaceAll(/.*\./, '')},
                packageName: curPackage]
        out.withWriter {
            it << templatePackageFrame.make(binding)
        }
    }

    def createTemplate(templateEngine, resourceFile) {
//        def resourceUrl = getClass().getClassLoader().getResource(resourceFile)
        def resourceUrl = getClass().getResource(resourceFile)
        return templateEngine.createTemplate(resourceUrl.text)
    }

    /**
     * Retrieves a String representing the return type
     */
    private getReturnType(method) {
        def returnType = method.getReturns()

        if (returnType != null) {
            return returnType.toString()
        } else {
            return ""
        }
    }

    /**
     * Retrieve a String representing the declaration of the parameters of the method passed as parameter.
     *
     * @param method a method
     * @return the declaration of the method (long version)
     */
    private getParametersDecl(method) {
        getParameters(method).collect {"${it.getType()}"}.join(", ")
    }

    /**
     * Retrieve the parameters of the method.
     *
     * @param method a method
     * @return a list of parameters without the first one
     */
    private getParameters(method) {
        if (method.getParameters().size() > 1)
            return method.getParameters().toList()[1..-1]
        else
            return []
    }

    /**
     * Retrieve the JavaDoc comment associated with the method passed as parameter.
     *
     * @param method a method
     * @return the JavaDoc comment associated with this method
     */
    private getComment(method) {
        def ans = method.getComment()
        if (ans == null) return ""
        return ans
    }

    private linkify(orig, curPackage) {
        orig.replaceAll(/\{@link\s+([^}]*)\s*\}/) {all, link -> getDocUrl(link, curPackage) }
    }

    /**
     * Main entry point.
     */
    static void main(args) {
        def outFolder = new File("target/html/groovy-jdk")
        outFolder.mkdirs()
        def start = System.currentTimeMillis()
        def srcFiles = args.collect {getSourceFile(it)}
        def srcFileNames = args.collect {getSourceFile(it).canonicalPath}
        try {
            Class[] classes = DefaultGroovyMethods.additionals
            classes.each {
                def name = it.name
                if (name.indexOf('$') > 0) {
                    name = name.tokenize('$')[0]
                }
                def newFile = getSourceFile(name)
                def newFileName = newFile.canonicalPath
                if (!srcFileNames.contains(newFileName)) {
                    srcFileNames << newFileName
                    srcFiles << newFile
                }
            }
        } catch (MissingPropertyException mpe) {
            log.error mpe.message, mpe
            // no call site change available, so ignore it
        }
        def docGen = new DocGenerator(srcFiles, outFolder)
        docGen.generateNew()
        def end = System.currentTimeMillis()
        log.debug "Done. Took ${end - start} millis."
    }

    private static File getSourceFile(String classname) {
        // TODO don't hardcode like this
        if (classname.contains("/")) return new File(classname)
        new File("src/main/" + classname.replace('.', '/') + ".java")
    }
}
