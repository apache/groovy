
package com.xseagullx.groovy.gsoc

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit

class Main {
    public static void main(String[] args) {
        println("GSoC started!")

        if (args.size() != 2) {
            displayHelp()
            return
        }

        CompilerConfiguration configuration
        if (args[1] == '-o')
            configuration = CompilerConfiguration.DEFAULT
        else if (args[1] == '-n') {
            configuration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            configuration.pluginFactory = new Antlrv4PluginFactory()
        }
        else
            throw new RuntimeException("Unrecognized option: ${args[1]}. Use -o or -n.")

        def file = new File(args[0])
        if (!file.directory)
            process(file, configuration)
        else {
            file.eachFileRecurse {
                process(it, configuration)
            }
        }
    }

    static void process(File file, CompilerConfiguration configuration) {
        try {
            GroovyClassLoader classLoader = new GroovyClassLoader()
            def errorCollector = new ErrorCollector(configuration)
            def su = new SourceUnit(file, configuration, classLoader, errorCollector)

            System.setProperty('groovy.ast', 'xml')
            su.parse()
            su.completePhase()
            su.nextPhase()
            su.convert()
            println("Processed $file")
        } catch (any) {
            println("Failed $file")
            any.printStackTrace()
        }
    }

    static def displayHelp() {
        println('Please specify path to *.groovy file or directory with them.\n' +
                'Use -o for groovy parser and -n for Antlrv4 parser.')
    }
}
