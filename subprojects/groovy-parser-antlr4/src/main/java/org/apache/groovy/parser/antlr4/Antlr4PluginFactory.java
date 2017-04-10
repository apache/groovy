package org.apache.groovy.parser.antlr4;

import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.ParserPluginFactory;

/**
 * A parser plugin factory for the new parser
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/08/14
 */
public class Antlr4PluginFactory extends ParserPluginFactory {
    @Override
    public ParserPlugin createParserPlugin() {
        return new Antlr4ParserPlugin();
    }
}
