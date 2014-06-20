
class A {
    private def method() {
        23 * 12 + 2 || '23' + !23 && 4 + 2 / 2 - 3 || 23
    }

    private def method2() {
        !5
        ~5
        -5
        +5
        // -a FIXME Return unary operators.
        // +a

        5 + 10
        5 - 10
        5 * 10
        5 / 10
        5 % 10
        5 ** 10

        5--
        5++
        --5
        ++5

        5 >> 10
        5 >>> 10
        5 << 10
        5 > 10
        5 < 10

        5 ^ 10

        5 | 10
        5 & 10

        5 || 10
        5 && 10
        5 ==  10
        5 !=  10
        5 <=>  10

        5..10
        5..10
        5..<10

        5 in null
        5 as Integer
        5 instanceof Integer

        5.properties
        5*.properties
        5?.properties
        5.@properties


        5 =~ 'pattern'
        5 ==~ 'pattern'

        // ?:
        // assignment and it's variations +=
    }
}
