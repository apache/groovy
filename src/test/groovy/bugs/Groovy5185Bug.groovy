package groovy.bugs

class Groovy5185Bug extends GroovyTestCase {
    void testShouldNotThrowMethodMissingException() {
        assertScript '''
            enum Foo {
                foo,
                bar,
                baz
            }

            List<Closure> closures = []
            500.times { int index ->
                closures << {
                    100.times {
                        String key = "bar"
                        Foo f = key as Foo
                        //Foo f = Foo.valueOf(key)
                        //Foo f = Enum.valueOf(Foo, key)
                    }
                }
            }

            List<Thread> threads = closures.collect { Thread.start(it) }
            threads.each { it.join() }

            println('done')
        '''
    }
}
