package org.codehaus.groovy.ast.tools;

import org.apache.groovy.parser.antlr4.Antlr4ParserPlugin;
import org.apache.groovy.parser.antlr4.Antlr4PluginFactory;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ParserPluginFactory;

public class Antlr4Utils {
    private Antlr4Utils() {
    }

    public static ClassNode parse(String option, CompilerConfiguration configuration) {
        Antlr4PluginFactory antlr4PluginFactory = (Antlr4PluginFactory) ParserPluginFactory.antlr4(configuration);
        Antlr4ParserPlugin antlr4ParserPlugin = (Antlr4ParserPlugin) antlr4PluginFactory.createParserPlugin();
        return antlr4ParserPlugin.makeType(option);
    }
}
