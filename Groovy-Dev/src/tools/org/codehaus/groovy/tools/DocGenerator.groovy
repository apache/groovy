package org.codehaus.groovy.tools

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import com.thoughtworks.qdox.JavaDocBuilder
import java.io.File
import java.util.*

/**
 * Generate documentation about the methods provided by the Groovy Development Kit
 * that enhance the standard JDK classes.
 *
 * @author Guillaume Laforge, John Wilson, Bernhard Huber, Paul King
 */
class DocGenerator {
    private static final String PRIMITIVE_TYPE_PSEUDO_PACKAGE = 'primitive-types'
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
            println "adding reader for $it"
            builder.addSource(it.newReader())
        }

        def sources = builder.getSources()

        def methods = []
        sources.each {source ->
            def classes = source.getClasses()
            classes.each {aClass ->
                methods.addAll(aClass.methods as List)
            }
        }

        def start = System.currentTimeMillis();
        for (method in methods) {
            if (method.isPublic() && method.isStatic()) {
                def parameters = method.getParameters()
                def jdkClass = parameters[0].getType().toString()

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
                            'shortComment': getComment(method).replaceAll('\\..*', ''),
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

    private generateClassDetails(template, curPackage, aClass) {
        def packagePath = generatePackagePath(curPackage)
        def dir = new File(outputFolder, packagePath)
        dir.mkdirs()
        def out = new File(dir, aClass.replaceAll('.*\\.', '') + '.html')
        def listOfMethods = jdkEnhancedClasses[aClass].sort {it.name}
        def methods = []
        listOfMethods.each {method ->
            def parameters = method.getTagsByName("param").collect {
                [name: it.value.replaceAll(' .*', ''), comment: it.value.replaceAll('^\\w*', '')]
            }
            if (parameters)
                parameters.remove(0) // method is static, first arg is the "real this"

            def seeComments = method.getTagsByName("see").collect {[target: getDocUrl(it.value)]}

            def returnType = getReturnType(method)
            def methodInfo = [
                    name: method.name,
                    comment: getComment(method),
                    shortComment: getComment(method).replaceAll('\\..*', ''),
                    returnComment: method.getTagByName("return")?.getValue() ?: '',
                    seeComments: seeComments,
                    returnTypeDocUrl: getDocUrl(returnType),
                    parametersSignature: getParametersDecl(method),
                    parametersDocUrl: getParametersDocUrl(method),
                    parameters: parameters,
                    isStatic: method.parentClass.name == 'DefaultGroovyStaticMethods'
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

    private String getParametersDocUrl(method) {
        getParameters(method).collect {"${getDocUrl(it.type.toString())} ${it.getName()}"}.join(", ")
    }

    // TODO make this understand @see references within the same file, i.e. beginning with #
    private String getDocUrl(type) {
        if (!type.contains('.'))
            return type

        def target = type.split('#')
        def shortClassName = target[0].replaceAll(/.*\./, "")
        def packageName = type[0..(-shortClassName.size() - 2)]
        shortClassName += (target.size() > 1 ? '#' + target[1].split('\\(')[0] : '')
        def apiBaseUrl, title
        if (type.startsWith("groovy")) {
            apiBaseUrl = "http://groovy.codehaus.org/api/"
            title = "Groovy class in $packageName"
        }
        else {
            apiBaseUrl = "http://java.sun.com/j2se/1.4.2/docs/api/"
            title = "JDK class in $packageName"
        }

        def url = apiBaseUrl + target[0].replaceAll(/\./, "/") + '.html' + (target.size() > 1 ? '#' + target[1] : '')
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
        getParameters(method).collect {"${it.getType()} ${it.getName()}"}.join(", ")
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
            System.err.println mpe.message
            // no call site change available, so ignore it
        }
        def docGen = new DocGenerator(srcFiles, outFolder)
        docGen.generateNew()
        def end = System.currentTimeMillis()
        println "Done. Took ${end - start} millis."
    }

    private static File getSourceFile(String classname) {
        new File("src/main/" + classname.replaceAll(/\./, "/") + ".java")
    }
}
