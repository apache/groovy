package com.xseagullx.groovy.gsoc.util

import spock.lang.Specification

class StringUtilTest extends Specification {
    def "ReplaceHexEscapes"() {
        expect:
            StringUtil.replaceHexEscapes(text) == resText
        where:
            text | resText
            '\\' + 'u1234' + '\\' + 'uaBcd' | "\u1234\uabcd"
    }

    def "ReplaceOctalEscapes"() {
        expect:
            StringUtil.replaceOctalEscapes(text) == resText
        where:
            text | resText
            /\123 \1\34\5\377\678/ | "\123 \1\34\5\377\678"
    }

    def "ReplaceStandardEscapes"() {
        expect:
            StringUtil.replaceStandardEscapes(text) == resText
        where:
            text | resText
            /\b\t\n\f\r\"\'\ / | "\b\t\n\f\r\"\'\\ "
    }
}
