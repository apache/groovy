package groovy

class CastTest extends GroovyTestCase {

    Short b = 1
    
    void testCast() {
        def x = (Short) 5

        println("Cast Integer to ${x} with type ${x.class}")
        
        assert x.class == Short
        
        methodWithShort(x)
    }
    
    void testImplicitCast() {
        Short x = 6
        
        println("Created ${x} with type ${x.class}")
        
        assert x.class == Short , "Type is ${x.class}"
        
		methodWithShort(x)
        
        x = 7
        
        println("Updated ${x} with type ${x.class}")
        
        assert x.class == Short , "Type is ${x.class}"
    }

    void testImplicitCastOfField() {

        println("Field is ${b} with type ${b.class}")
        
        assert b.class == Short , "Type is ${b.class}"
        
        b = 5
        
        println("Updated field ${b} with type ${b.class}")
 
        assert b.class == Short , "Type is ${b.class}"
    }
    
    void testIntCast() {
        def i = (Integer) 'x'
        
        assert i instanceof Integer
    }
    
    void testCharCompare() {
        def i = (Integer) 'x'
        def c = 'x'
        
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
        def c = (Character) 'x'
        
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
        
        def text = "text"
        def idx = text.indexOf(x)
        
        assert idx == 2
    }
    // br
    void testPrimitiveCasting() {
        def d = 1.23
        def i1 = (int)d
        def i2 = (Integer)d
        assert i1.class.name == 'java.lang.Integer'
        assert i2.class.name == 'java.lang.Integer'

        def ch = (char) i1
        assert ch.class.name == 'java.lang.Character'

        def dd = (double)d
        assert dd.class.name == 'java.lang.Double'

    }

    void testAsSet() {
    	def mySet = [2, 3, 4, 3] as SortedSet
    	assert mySet instanceof SortedSet
    	
    	// identity test
    	mySet = {} as SortedSet
    	assert mySet.is ( mySet as SortedSet )
    	
    	mySet = [2, 3, 4, 3] as Set
    	assert mySet instanceof HashSet
    	
        // identitiy test
    	mySet = {} as Set
    	assert mySet.is ( mySet as Set )

        // array test
        mySet = new String[2] as Set // Array of 2 null Strings
        assert mySet instanceof Set
        assert mySet.size() == 1
        assert mySet.iterator().next() == null

        mySet = "a,b".split(",") as Set // Array of 2 different Strings
        assert mySet instanceof Set
        assert mySet.size() == 2
        assert mySet == new HashSet([ "a", "b" ])

        mySet = "a,a".split(",") as Set // Array of 2 different Strings
        assert mySet instanceof Set
        assert mySet.size() == 1
        assert mySet == new HashSet([ "a" ])
    }

    void testCastToAbstractClass() {
        def closure = { 42 }
        def myList = closure as AbstractList
        assert myList[-1] == 42
        assert myList.size() == 42
    }
}
