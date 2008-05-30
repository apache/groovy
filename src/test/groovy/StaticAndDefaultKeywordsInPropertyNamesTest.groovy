package groovy

class KeywordsInPropertyNamesTest extends GroovyTestCase {

    void testKeywords() {
        def value = "returnValue"
        StaticAndDefaultClass.metaClass.static.dynStaticMethod = {-> value }
        assert value == StaticAndDefaultClass.dynStaticMethod()

        StaticAndDefaultClass.metaClass.default = value
        StaticAndDefaultClass.metaClass.goto = value
        assert value == new StaticAndDefaultClass().default
        assert value == new StaticAndDefaultClass().goto
        assert String.package.name == 'java.lang'
    }

    void testKeywordsAsMapKeys() {
        def map = [goto: 1, default: 2, static: 3]
        assert 1 == map.goto
        assert 2 == map.default
        assert 3 == map.static
    }

    void testMapWithKeywords() {
        def map = [
          transient    : 'breeze',
          public       : 'bar',
          private      : 'message',
          static       : 'electricity',
          new          : 'car',
          true         : 'love',
          (new Date()) : 'foo',
          null         : 'bar',
          (null)       : 'baz'
        ]
        assert map.transient == 'breeze'
        assert map.new == 'car'
        assert map.static == 'electricity'
        assert map.true == 'love'
        assert map.null == 'bar'
        assert map[null] == 'baz'
    }

}

class StaticAndDefaultClass {}