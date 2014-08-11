package com.xseagullx.groovy.gsoc;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

class PositionAdjustingLexerATNSimulator extends LexerATNSimulator {
    public PositionAdjustingLexerATNSimulator(Lexer recog, ATN atn, DFA[] decisionToDFA, PredictionContextCache sharedContextCache) {
        super(recog, atn, decisionToDFA, sharedContextCache);
    }

    protected void resetAcceptPosition(CharStream input, int index, int line, int charPositionInLine) {
        input.seek(index);
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        consume(input);
    }
}

