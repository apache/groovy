package groovy.bugs.vm5

class Groovy3851Bug extends GroovyTestCase {
    
    void testExecuteASpockTestWithSpockJUnitGrabbed() {
        def shell = new GroovyShell()
        
        // first run of the spock script fails right now, but successfully grabs spock/junit4 jars and brings them on GCL's classpath
        def result = shell.run(getSpockScript(), "Groovy3857BugV1.groovy", [])
        assertTrue result.failures.size() == 1
        assertNotNull result.failures[0].trace
        assertTrue result.failures[0].trace.contains('org.spockframework.runtime.InvalidSpeckError')
        
        // now GCL has the spock/Junit4 jars in, so the spock script should get executed successfully
        result = shell.run(getSpockScript(), "Groovy3857BugV2.groovy", [])
        assertTrue result.failures.size() == 0
    }
    
    private String getSpockScript() {
        return """
            import spock.lang.*
            @Grab(group='org.spockframework', module='spock-core', version='0.2')
            class HelloSpock extends Specification {
                def "can you figure out what I'm up to?"() {
                    expect:
                        name.size() == length
                    where:
                        name << ["Kirk", "Spock", "Scotty"]
                        length << [4, 5, 6]
                }
            }
        """
    }
}

