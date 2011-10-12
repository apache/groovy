package groovy.bugs


class Groovy4471Bug extends GroovyTestCase {
    void testShouldNotThrowNPE() {
        assertScript """
            class X {
                private A a = new A()

                public X() {
                    new B() {
                        public A getFoo() {a}
                    }
                }
            }

            class A {}
            class B {
                B(){getFoo()}
                A getFoo(){}
            }

            def x = new X()
        """
    }

    void testShouldAllowArrayListInitialization() {
        assertScript """
            def list = new ArrayList() {{
                add('a')
            }}
        """
    }
}
