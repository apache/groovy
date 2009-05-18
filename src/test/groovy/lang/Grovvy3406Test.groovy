class Groovy3406Test extends GroovyTestCase {
    void testBug() {    
        def str = 'hello'
        def methodName = 'toUpperCase'
        
        def methodOfInterest = str.metaClass.getMetaMethod(methodName)
        assert methodOfInterest.invoke(str) == "HELLO"
    }
}