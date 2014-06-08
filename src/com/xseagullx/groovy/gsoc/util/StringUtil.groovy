package com.xseagullx.groovy.gsoc.util

class StringUtil {
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
}
