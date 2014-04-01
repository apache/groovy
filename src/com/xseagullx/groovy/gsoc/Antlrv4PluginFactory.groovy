package com.xseagullx.groovy.gsoc

import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.ParserPlugin
import org.codehaus.groovy.control.ParserPluginFactory
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.ParserException
import org.codehaus.groovy.syntax.Reduction

class Antlrv4ParserPlugin implements ParserPlugin {
    @Override Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        null
    }

    @Override ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {

        def builder = new ASTBuilder(sourceUnit, classLoader)
        builder.moduleNode
    }
}

class Antlrv4PluginFactory extends ParserPluginFactory {
    @Override ParserPlugin createParserPlugin() {
        new Antlrv4ParserPlugin()
    }
}
