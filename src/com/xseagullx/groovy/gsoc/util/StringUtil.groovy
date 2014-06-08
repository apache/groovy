package com.xseagullx.groovy.gsoc.util

class StringUtil {
    static { //test
        def text = /\123 \1\34\5\377\678/
        def resText = "\123 \1\34\5\377\678"
        assert replaceOctalEscapes(text) == resText

        text = '\\' + 'u1234' + '\\' + 'uaBcd'
        resText = "\u1234\uabcd"
        assert replaceHexEscapes(text) == resText


        text = /\b\t\n\f\r\"\'\ /
        resText = "\b\t\n\f\r\"\'\\ "
        assert replaceStandardEscapes(text) == resText
    }

    static String replaceHexEscapes(String text) {
        def p = ~/\\u([0-9abcdefABCDEF]{4})/
        text.replaceAll(p) { String _0, String _1 ->
            Character.toChars(Integer.parseInt(_1, 16))
        }
    }

    static String replaceOctalEscapes(String text) {
        def p = ~/\\([0-3]?[0-7]?[0-7])/
        text.replaceAll(p) { String _0, String _1 ->
            Character.toChars(Integer.parseInt(_1, 8))
        }
    }

    private static standardEscapes = [
        'b': '\b',
        't': '\t',
        'n': '\n',
        'f': '\f',
        'r': '\r',
    ]

    static String replaceStandardEscapes(String text) {
        def p = ~/\\([btnfr"'\\])/
        text.replaceAll(p) { String _0, String _1 ->
            standardEscapes[_1] ?: _1
        }
    }

    static void main(args) {
        def \u1234 = 12
        println(ሴ)
        println("Self-testing completed!")

    }
}
