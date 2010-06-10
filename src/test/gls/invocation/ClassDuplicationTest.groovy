class ClassDuplicationTest extends GroovyTestCase {
    void testDuplicationOnMethodSignatureTest() {
        def shell1 = new GroovyShell(this.class.classLoader)
        def obj1 = shell1.evaluate("""
            class A {}
            def foo(A a) {}
            return this
        """)
        def shell2 = new GroovyShell(this.class.classLoader)
        def obj2 = shell2.evaluate("""
            class A {}
            return new A()
        """)
        try {
            obj1.foo(obj2)
            assert false
        } catch (MissingMethodException mme) {
            assert mme.toString().contains("A (defined by")
        }     
    }
}
