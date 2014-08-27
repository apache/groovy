package com.xseagullx.groovy.gsoc
import groovy.mock.interceptor.StubFor
import groovy.util.logging.Log
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.TokenStream
import spock.lang.Specification

@Log
class GrammarPredicatesTest extends Specification {
    def "IsClassName"(String name, boolean res) {
        setup:
            def tokenStub = new StubFor(TokenStream)
            def array = name.split('\\.')
            tokenStub.demand.LT { new CommonToken(GroovyParser.IDENTIFIER, array[0]) }
            for (int j = 1; j < array.size(); j++) {
                final i = j
                tokenStub.demand.LT { new CommonToken(GroovyParser.DOT) }
                tokenStub.demand.LT { new CommonToken(GroovyParser.IDENTIFIER, array[i]) }
            }
            tokenStub.demand.LT { new CommonToken(GroovyParser.EOF) }
        expect:
            GrammarPredicates.isClassName(tokenStub.proxyDelegateInstance() as TokenStream) == res
        where:
            name    | res
            "a"     | false
            "A"     | true
            "a.a"   | false
            "a.A"   | true
            "void"  | true
            "int"   | true
            "float" | true
    }
}
