package groovy

class NestedClassTest extends GroovyTestCase {

    void testStaticInnerStaticMethod () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner.it
        """).newInstance()
        assertEquals 30, script.run()
    }

    void testStaticInnerInstanceMethod () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        new JavaClass.StaticInner ().result
        """).newInstance()
        assertEquals 239, script.run()
    }

    void testParam () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        def method (JavaClass.StaticInner obj) { obj.result }

        method new JavaClass.StaticInner ()
        """).newInstance()

        assertEquals 239, script.run()
    }

    void testTypeDecl () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner method () { 239 }

        method ()
        """).newInstance()

        shouldFail (org.codehaus.groovy.runtime.typehandling.GroovyCastException) {
          assertEquals 239, script.run()
        }
    }

    void testFieldDecl () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner field = 239

        field
        """).newInstance()

        shouldFail (org.codehaus.groovy.runtime.typehandling.GroovyCastException) {
          assertEquals 239, script.run()
        }
    }

    void testInstanceof () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.CONST instanceof JavaClass.StaticInner
        """).newInstance()

        assertTrue script.run ()
    }

    void testExtends () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        class U extends JavaClass.StaticInner.Inner2 {}

        new U ()

        """).newInstance()

        assert script.run () instanceof JavaClass.StaticInner.Inner2
    }
}