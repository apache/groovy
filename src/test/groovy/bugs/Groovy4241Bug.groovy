package groovy.bugs

class Groovy4241Bug extends GroovyTestCase {
    void testAsTypeWithinvokeMethodOverridden() {
        Foo4241.metaClass.invokeMethod = { String name, args ->
            println name
            for (arg in args) {
                println arg.getClass()
            }
        }
        def f = new Foo4241()
        
        def bar = [key: 'foo'] as Bar4241
        f.echo(bar) // this used to work
        
        f.echo([key: 'foo'] as Bar4241) // this used to fail with NPE
    }
}

class Foo4241 {}

class Bar4241 {}
