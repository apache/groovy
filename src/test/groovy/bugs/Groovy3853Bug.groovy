package groovy.bugs

class Groovy3853Bug extends GroovyTestCase {

    void testConsecutiveGrabCallsWithASharedLoaderWhereFirstGrabFails() {
        def classLoader = new GroovyClassLoader()

        grabThatShouldFail(classLoader)
        
        // currently this call also fails because GrapeIvy maintains state from previous failure 
        grabThatShouldGoThrough(classLoader) 
    }
    
    def grabThatShouldFail(classLoader) {
        try{
            // just an unlikely grab, that's all
            classLoader.parseClass """
                @Grab(group="roshan", module="dawrani", version="0.0.7")
                class Foo3853V1 {}
            """
            fail('This @Grab usage should have failed')
        }catch(ex) {
            // fine if it failed
            assertTrue ex.message.contains('unresolved dependency')
        }
    }

    def grabThatShouldGoThrough(classLoader) {
        classLoader.parseClass """
            @Grab(group="junit", module="junit", version="4.7")
            class Foo3853V2 {}
        """
    }
}
