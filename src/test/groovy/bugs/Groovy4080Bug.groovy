package groovy.bugs

class Groovy4080Bug extends GroovyTestCase {
    public void testClassGeneratedWithAnnotationHavingGrabOnImport() {
        GroovyShell shell = new GroovyShell(new GroovyClassLoader())
        shell.evaluate """
            @Grab(group='commons-primitives', module='commons-primitives', version='1.0')
            import java.lang.annotation.*
            
            @Retention(RetentionPolicy.RUNTIME)
            @interface Require {
                String value()
            }
            
            assert Require.name == 'Require' // cause loading of the class
        """
    }    
}
