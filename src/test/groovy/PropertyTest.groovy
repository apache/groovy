/** 
 * Tests the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class PropertyTest extends GroovyTestCase {

    void testNormalPropertyGettersAndSetters() {
        
        println("About to create Foo")
        
        foo = new Foo()

        println("created ${foo}")
        
        value = foo.getMetaClass()
        
        println("metaClass ${value}")
        
        println(foo.inspect())
        
        println("name ${foo.name}, blah ${foo.blah}")
        
        assert foo.name == "James"
        assert foo.getName() == "James"
        
        assert foo.location == "London"
        assert foo.getLocation() == "London"
        
        assert foo.blah == 9
        assert foo.getBlah() == 9
        
        foo.name = "Bob"
        foo.location = "Atlanta"
        
        assert foo.name == "Bob"
        assert foo.getName() == "Bob"
        
        assert foo.location == "Atlanta"
        assert foo.getLocation() == "Atlanta"
    }
    
    void testOverloadedGetter() {
        
        foo = new Foo()

        println("count ${foo.count}")
        
        assert foo.getCount() == 1
        assert foo.count == 1
        
        foo.count = 7
        
        assert foo.count == 7
        assert foo.getCount() == 7
    }

    void testNoSetterAvailableOnPrivateProperty() {
        foo = new Foo()
        
        // methods should fail on non-existent method calls
        shouldFail { foo.blah = 4 }
        shouldFail { foo.setBlah(4) }
    }
    
    void testCannotSeePrivateProperties() {
        foo = new Foo()

        // def access fails on non-existent def
        shouldFail { x = foo.invisible }

        // methods should fail on non-existent method calls
        shouldFail { foo.getQ() }
    }

    void testConstructorWithNamedProperties() {
        foo = new Foo(name:'Gromit', location:'Moon')
        
        assert foo.name == 'Gromit'
        assert foo.location == 'Moon'
        
        println("created bean ${foo.inspect()}")
    }
    
    void testToString() {
        foo = new Foo(name:'Gromit', location:'Moon')

        println foo
    }

    void testArrayLengthProperty() {
        // create two arrays, since all use the same MetaArrayLengthProperty object -
        // make sure it can work for all types and sizes
        i = new Integer[5]
        s = new String[10]

        // put something in it to make sure we're returning the *allocated* length, and
        // not the *used* length
        s[0] = "hello"

        assert i.length == 5
        assert s.length == 10

        // this def does not mean there is a getLength() method
        shouldFail { i.getLength() }

        // verify we can't set this def, it's read-only
        shouldFail { i.length = 6 }
    }

    void testGstringAssignment() {
        foo = new Foo()
        foo.body = "${foo.name}"
        assert foo.body == "James"
    }
}

