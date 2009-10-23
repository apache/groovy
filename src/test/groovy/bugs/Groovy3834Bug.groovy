package groovy.bugs

class Groovy3834Bug extends GroovyTestCase {
    void testDuplicateCallsToMissingMethod() {
        def instance = new AClassWithMethodMissingMethod()
        shouldFail MissingMethodException, { instance.someMissingMethod() }
        assertEquals 1, instance.count
    }
}

class AClassWithMethodMissingMethod {
    int count = 0
    def methodMissing(String name, args) {
        count++
        throw new MissingMethodException(name, AClassWithMethodMissingMethod, args)        
    }
    
}
