package groovy.bugs

class Groovy4861Bug extends GroovyTestCase {
    void testCallSitesUsageInNestedInterface() {
        assert Foo4861.A.j == 3
        assert Foo4861.A.j2 == 7
        assert new Foo4861()
    }
}

@groovy.transform.PackageScope class Foo4861 {
    static interface A {
        static Integer j = 3
        static Integer j2 = j + 4
    }
    static interface B{}
    static class Inner<X> {}
    static Inner<A> method() { null }
}
