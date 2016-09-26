package groovy.bugs

import gls.CompilableTestSupport

class Groovy7909Bug extends CompilableTestSupport {
    void testDynamicCompile(){
        shouldCompile '''
trait Three implements One, Two {
    def postMake() {
        One.super.postMake()
        Two.super.postMake()
        println "Three"
    }
}
trait One {
    def postMake() { println "One"}
}
trait Two {
    def postMake() { println "Two"}
}
class Four implements Three {
    def make() {
        Three.super.postMake()
        println "All done?"
    }
}
Four f = new Four()
f.make()
    '''
    }
    void testStaticCompile(){
        shouldCompile '''
@groovy.transform.CompileStatic
trait Three implements One, Two {
    def postMake() {
        One.super.postMake()
        Two.super.postMake()
        println "Three"
    }
}
trait One {
    def postMake() { println "One"}
}
trait Two {
    def postMake() { println "Two"}
}
class Four implements Three {
    def make() {
        Three.super.postMake()
        println "All done?"
    }
}
Four f = new Four()
f.make()
    '''
    }
}

