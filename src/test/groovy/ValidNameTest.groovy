package groovy

class ValidNameTest extends GroovyTestCase {

    void testFieldNamesWithDollar() {
        def $foo$foo = '3foo'
        def foo$foo = '2foo'
        def $foo = '1foo'
        def foo = '0foo'
        assert $foo$foo == '3foo'
        assert foo$foo == '2foo'
        assert $foo == '1foo'
        assert foo == '0foo'
        assert "$foo$foo${foo$foo}${$foo$foo}${$foo}$foo" == '0foo0foo2foo3foo1foo0foo'
    }

    void testClassAndMethodNamesWithDollar() {
        assert new $Temp().$method() == 'bar'
    }
}

class $Temp {
    def $method() { 'bar' }
}
