package groovy

class StaticAndDefaultKeywordsInPropertyNamesTest extends GroovyTestCase {

    void testKeywords() {
        def value = "returnValue"
        StaticAndDefaultClass.metaClass.static.dynStaticMethod = {-> value }
        assert value == StaticAndDefaultClass.dynStaticMethod()

        StaticAndDefaultClass.metaClass.default = value
        StaticAndDefaultClass.metaClass.goto = value
        assert value == new StaticAndDefaultClass().default
        assert value == new StaticAndDefaultClass().goto
    }

    void testKeywordsAsMapKeys() {
        def map = [goto: 1, default: 2, static: 3]

        assert 1 == map.goto
        assert 2 == map.default
        assert 3 == map.static
    }
}

class StaticAndDefaultClass {}