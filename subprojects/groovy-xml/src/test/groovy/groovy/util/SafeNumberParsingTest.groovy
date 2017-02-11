package groovy.util


class SafeNumberParsingTest extends GroovyTestCase {

    void testSafetyWhenConvertingToNumbers() {
        def xmlText = '''
                <someNumberValues>
                <someBigDecimal>123.4</someBigDecimal>
                <someEmptyBigDecimal></someEmptyBigDecimal>
                <someLong>123</someLong>
                <someEmptyLong></someEmptyLong>
                <someFloat>123.4</someFloat>
                <someEmptyFloat></someEmptyFloat>
                <someDouble>123.4</someDouble>
                <someEmptyDouble></someEmptyDouble>
                <someInteger>123</someInteger>
                <someEmptyInteger></someEmptyInteger>
            </someNumberValues>
                '''
        def xml = new XmlSlurper().parseText(xmlText)

        assert xml.'**'.find{it.name() == 'someBigDecimal'}.toBigDecimal() == 123.4
        assert xml.'**'.find{it.name() == 'someEmptyBigDecimal'}.toBigDecimal() == null
        assert xml.'**'.find{it.name() == 'someMissingBigDecimal'}?.toBigDecimal() == null
        assert xml.'**'.find{it.name() == 'someLong'}.toLong() == 123
        assert xml.'**'.find{it.name() == 'someEmptyLong'}.toLong() == null
        assert xml.'**'.find{it.name() == 'someMissingLong'}?.toLong() == null
        assert xml.'**'.find{it.name() == 'someFloat'}.toFloat() == 123.4.toFloat()
        assert xml.'**'.find{it.name() == 'someEmptyFloat'}.toFloat() == null
        assert xml.'**'.find{it.name() == 'someMissingFloat'}?.toFloat() == null
        assert xml.'**'.find{it.name() == 'someDouble'}.toDouble() == 123.4.toDouble()
        assert xml.'**'.find{it.name() == 'someEmptyDouble'}.toDouble() == null
        assert xml.'**'.find{it.name() == 'someMissingDouble'}?.toDouble() == null
        assert xml.'**'.find{it.name() == 'someInteger'}.toInteger() == 123
        assert xml.'**'.find{it.name() == 'someEmptyInteger'}.toInteger() == null
        assert xml.'**'.find{it.name() == 'someMissingInteger'}?.toInteger() == null
    }
}
