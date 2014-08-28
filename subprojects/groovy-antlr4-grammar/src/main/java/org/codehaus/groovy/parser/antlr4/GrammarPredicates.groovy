package org.codehaus.groovy.parser.antlr4;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import java.util.Arrays;

public class GrammarPredicates {
    private static final String[] primitiveClassNames = new String[] {
        "boolean", "byte", "char", "double",
        "float", "int", "long", "short", "void"
    };

    public static boolean isClassName(TokenStream nameOrPath) {
        Token token = nameOrPath.LT(1);
        if (nameOrPath.LT(2).getType() == GroovyParser.DOT) return true;
        String tokenText = token.getText();
        if (Arrays.binarySearch(primitiveClassNames, tokenText)!=-1) return true;
        return Character.isUpperCase(tokenText.codePointAt(0));
    }

}
