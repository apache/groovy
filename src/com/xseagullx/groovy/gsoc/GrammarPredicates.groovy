package com.xseagullx.groovy.gsoc

import groovy.util.logging.Log
import org.antlr.v4.runtime.TokenStream
import org.codehaus.groovy.ast.ClassHelper

import java.util.logging.Level

@Log
class GrammarPredicates {
    static boolean isClassName(TokenStream nameOrPath) {
        try {
            def i = 1
            def token = nameOrPath.LT(i)
            def s = "" << ""
            if (log.isLoggable(Level.FINE))
                s << token.text
            while(nameOrPath.LT(i + 1).type == GroovyParser.DOT) {
                i = i + 2
                token = nameOrPath.LT(i)
                if (log.isLoggable(Level.FINE))
                    s << '.' << token.text
            }
            log.fine("Checking $s")
            def res = ClassHelper.isPrimitiveType(ClassHelper.make(token.text)) || Character.isUpperCase(Character.codePointAt(token.text, 0))
            log.fine("res == $res")
            res
        }
        catch (any) {
            log.warning("Exception in isClassName predicate. $nameOrPath ${ any.class.name } $any.message")
            false
        }
    }
}
