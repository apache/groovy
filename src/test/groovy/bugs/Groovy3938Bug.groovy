package groovy.bugs

import org.codehaus.groovy.classgen.GroovyCompilerVersion

class Groovy3938Bug extends GroovyTestCase {
    void testGroovyCompilerVersionAnnotation() {
        def gcl = new GroovyClassLoader()
        
        verifyCompilerVersionAnnotation(gcl.parseClass("class C3938 {}"))

        verifyCompilerVersionAnnotation(gcl.parseClass("interface I3938 {}"))

        verifyCompilerVersionAnnotation(gcl.parseClass("enum E3938 {}"))
    }
    
    void verifyCompilerVersionAnnotation(Class klazz) {
        def ann = klazz.getAnnotation(GroovyCompilerVersion) 
        assertTrue (ann != null)
        assertTrue ann.value().equals(GroovySystem.version)
    }
}
