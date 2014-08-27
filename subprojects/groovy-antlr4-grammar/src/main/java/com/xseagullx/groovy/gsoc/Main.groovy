
package com.xseagullx.groovy.gsoc

import groovy.util.logging.Log
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit

enum Configuration {
    OLD,
    NEW,
}

@Log
class Main {

    private CompilerConfiguration configuration
    static boolean dumpAstInXml = false

    Main(Configuration configuration) {
        assert configuration
        if (configuration == Configuration.OLD)
            this.configuration = CompilerConfiguration.DEFAULT
        else {
            this.configuration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            this.configuration.pluginFactory = new Antlrv4PluginFactory()
        }
    }

    public static void main(String[] args) {
        log.info("GSoC started!")

        if (args.size() != 2) {
            displayHelp()
            return
        }

        def configurationFlag = args[1]
        Configuration configuration
        if (configurationFlag == '-o')
            configuration = Configuration.OLD
        else if (configurationFlag == '-n')
            configuration = Configuration.NEW
        else
            throw new RuntimeException("Unrecognized option: $configurationFlag. Use -o or -n.")

        def main = new Main(configuration)
        def file = new File(args[0])
        if (!file.directory)
            main.process(file)
        else {
            file.eachFileRecurse {
                main.process(it)
            }
        }
    }

    ModuleNode process(File file) {
        try {
            GroovyClassLoader classLoader = new GroovyClassLoader()
            def errorCollector = new ErrorCollector(configuration)
            def su = new SourceUnit(file, configuration, classLoader, errorCollector)

            if (dumpAstInXml)
                System.setProperty('groovy.ast', 'xml')
            su.parse()
            su.completePhase()
            su.nextPhase()
            su.convert()
            log.info("Processed $file")
            return su.AST
        } catch (any) {
            log.info("Failed $file")
            any.printStackTrace()
            return null
        }
    }

    void compile(File file)
    {
        ClassLoader parent = getClass().getClassLoader()
        GroovyClassLoader loader = new GroovyClassLoader(parent)
        final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(loader)
        final CompilationUnit unit = new CompilationUnit(groovyClassLoader)
        final CompilerConfiguration config = new CompilerConfiguration(configuration)

        config.targetDirectory = file.parentFile

        unit.configure(config)
        unit.addSource(file)
        unit.compile()
        log.info(unit.toString())
    }

    static def displayHelp() {
        println('Please specify path to *.groovy file or directory with them.\n' +
                'Use -o for groovy parser and -n for Antlrv4 parser.')
    }
}
