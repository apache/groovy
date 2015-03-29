package metaprogramming

class GroovyObjectTest extends GroovyTestCase{
    void testInvokeMethod() {
        assertScript '''
// tag::groovy_invoke_method[]
class SomeGroovyClass {
    def invokeMethod(String name, Object args){
        return "called invokeMethod $name $args"
    }
    
    def test(){
        return 'exist method'
    }
}

def someGroovyClass = new SomeGroovyClass()
assert someGroovyClass.test() == 'exist method'
assert someGroovyClass.someMethod() == 'called invokeMethod someMethod []'
// end::groovy_invoke_method[]
'''
    }
    
    void testGetProperty (){
        assertScript '''
// tag::groovy_get_property[]
class SomeGroovyClass {
    def field1 = 'ha'
    def field2 = 'ho'
    def field4 = 'hu'
    public def getField1(){
        return 'getHa'
    }
    
    @Override
    def getProperty(String name) {
        if (name != 'field3')
            return metaClass.getProperty(this, name) // <1>
        else
            return 'field3'
    }
}

def someGroovyClass = new SomeGroovyClass()
assert someGroovyClass.'field1' == 'getHa'
assert someGroovyClass.'field2' == 'ho'
assert someGroovyClass.'field3' == 'field3'
assert someGroovyClass.field4 == 'hu'
// end::groovy_get_property[]
'''
    }

    void testGetAttribute (){
        assertScript '''
// tag::groovy_get_attribute[]
class SomeGroovyClass {
    def field1 = 'ha'
    def field2 = 'ho'
    public def getField1(){
        return 'getHa'
    }
}

def someGroovyClass = new SomeGroovyClass()
assert someGroovyClass.metaClass.getAttribute(someGroovyClass, 'field1') == 'ha'
assert someGroovyClass.metaClass.getAttribute(someGroovyClass, 'field2') == 'ho'
// end::groovy_get_attribute[]
'''
    }
}