package bugs

class Foo {
    <T extends Object> T foo(T t) {
        return t
    }
}

assert 'abc' == new Foo().foo('abc')