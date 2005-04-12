class CastTest extends GroovyTestCase {

    Short b = 1
    
    void testCast() {
        x = (Short) 5

        println("Cast Integer to ${x} with type ${x.class}")
        
        assert x.class == Short
        
        methodWithShort(x)
    }
    
    void testImplicitCast() {
        Short x = 6
        
        println("Created ${x} with type ${x.class}")
        
        assert x.class == Short : "Type is ${x.class}"
        
		methodWithShort(x)
        
        x = 7
        
        println("Updated ${x} with type ${x.class}")
        
        assert x.class == Short : "Type is ${x.class}"
    }

    void testImplicitCastOfField() {

        println("Field is ${b} with type ${b.class}")
        
        assert b.class == Short : "Type is ${b.class}"
        
        b = 5
        
        println("Updated field ${b} with type ${b.class}")
 
        assert b.class == Short : "Type is ${b.class}"
    }
    
    void testIntCast() {
        i = (Integer) 'x'
        
        assert i instanceof Integer
    }
    
    void testCharCompare() {
        i = (Integer) 'x'
        c = 'x'
        
        assert i == c
        assert i =='x'
        assert c == 'x'
		assert i == i
		assert c == c

        assert 'x' == 'x'
        assert 'x' == c
        assert 'x' == i
    }
    
    void testCharCast() {
        c = (Character) 'x'
        
        assert c instanceof Character
        
        c = (Character)10
        
        assert c instanceof Character
    }
    
    void methodWithShort(Short s) {
        println("Called with ${s} with type ${s.class}")
        assert s.class == Short
    }
    
    void methodWithChar(Character x) {
        println("Called with ${x} with type ${s.class}")
        
        text = "text"
        idx = text.indexOf(x)
        
        assert idx == 2
    }
    // br
    void testPrimitiveCasting() {
        d = 1.23
        i1 = (int)d
        i2 = (Integer)d
        assert i1.class.name == 'java.lang.Integer'
        assert i2.class.name == 'java.lang.Integer'

        ch = (char) i1
        assert ch.class.name == 'java.lang.Character'

        dd = (double)d
        assert dd.class.name == 'java.lang.Double'

    }

}
