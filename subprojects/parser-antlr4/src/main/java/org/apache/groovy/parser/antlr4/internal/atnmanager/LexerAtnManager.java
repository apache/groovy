package org.apache.groovy.parser.antlr4.internal.atnmanager;

import org.antlr.v4.runtime.atn.ATN;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.util.SystemUtil;

/**
 * Manage ATN for lexer to avoid memory leak
 */
public class LexerAtnManager extends AtnManager {
    private static final String GROOVY_CLEAR_LEXER_DFA_CACHE = "groovy.clear.lexer.dfa.cache";
    private static final boolean TO_CLEAR_LEXER_DFA_CACHE;
    private final AtnWrapper lexerAtnWrapper = new AtnManager.AtnWrapper(GroovyLangLexer._ATN);
    public static final LexerAtnManager INSTANCE = new LexerAtnManager();

    static {
        TO_CLEAR_LEXER_DFA_CACHE = SystemUtil.getBooleanSafe(GROOVY_CLEAR_LEXER_DFA_CACHE);
    }

    @Override
    public ATN getATN() {
        return lexerAtnWrapper.checkAndClear();
    }

    @Override
    protected boolean shouldClearDfaCache() {
        return TO_CLEAR_LEXER_DFA_CACHE;
    }

    private LexerAtnManager() {}
}
