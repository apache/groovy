package groovy.bugs

class ClassInNamedParamsBug extends GroovyTestCase {
    
    void testBug() {
        def foo = method(class:'cheese', name:'cheddar')
        
        assert foo.name == "cheddar"
        assert foo.class == "cheese"
        
        foo = method(name:'cheddar', class:'cheese')
        
        assert foo.name == "cheddar"
        assert foo.class == "cheese"
    }
    
    def method(Map data) {
        data
    }
}