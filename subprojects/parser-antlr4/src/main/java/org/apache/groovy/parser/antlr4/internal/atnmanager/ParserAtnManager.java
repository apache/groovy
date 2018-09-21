package org.apache.groovy.parser.antlr4.internal.atnmanager;

import org.antlr.v4.runtime.atn.ATN;
import org.apache.groovy.parser.antlr4.GroovyLangParser;

/**
 * Manage ATN for parser to avoid memory leak
 */
public class ParserAtnManager extends AtnManager {
    private final AtnWrapper parserAtnWrapper = new AtnManager.AtnWrapper(GroovyLangParser._ATN);
    public static final ParserAtnManager INSTANCE = new ParserAtnManager();

    @Override
    public ATN getATN() {
        return parserAtnWrapper.checkAndClear();
    }

    @Override
    protected boolean shouldClearDfaCache() {
        return true;
    }

    private ParserAtnManager() {}
}
