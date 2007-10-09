package groovy

/** 
 * Tests the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class PropertyTest extends GroovyTestCase {

    void testNormalPropertyGettersAndSetters() {
        
        println("About to create Foo")
        
        def foo = new Foo()

        println("created ${foo}")
        
        def value = foo.getMetaClass()
        
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

    // GROOVY-1809
    void testClassWithPrivateFieldAndGetter() {
        assert java.awt.Font.getName() == 'java.awt.Font'
        assert java.awt.Font.name == 'java.awt.Font'
    }

    void testOverloadedGetter() {
        
        def foo = new Foo()

        println("count ${foo.count}")
        
        assert foo.getCount() == 1
        assert foo.count == 1
        
        foo.count = 7
        
        assert foo.count == 7
        assert foo.getCount() == 7
    }

    void testNoSetterAvailableOnPrivateProperty() {
        def foo = new Foo()
        
        // methods should fail on non-existent method calls
        //shouldFail { foo.blah = 4 }
        shouldFail { foo.setBlah(4) }
    }
    
    void testCannotSeePrivateProperties() {
        def foo = new Foo()

        // def access fails on non-existent def
        //shouldFail { def x = foo.invisible } //todo: correct handling of access rules

        // methods should fail on non-existent method calls
        shouldFail { foo.getQ() }
    }

    void testConstructorWithNamedProperties() {
        def foo = new Foo(name:'Gromit', location:'Moon')
        
        assert foo.name == 'Gromit'
        assert foo.location == 'Moon'
        
        println("created bean ${foo.inspect()}")
    }
    
    void testToString() {
        def foo = new Foo(name:'Gromit', location:'Moon')

        println foo
    }

    void testArrayLengthProperty() {
        // create two arrays, since all use the same MetaArrayLengthProperty object -
        // make sure it can work for all types and sizes
        def i = new Integer[5]
        def s = new String[10]

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
        def foo = new Foo()
        foo.body = "${foo.name}"
        assert foo.body == "James"
    }
    
    void testFinalProperty() {
      def shell = new GroovyShell();
      assertScript """
        class A {
           final foo = 1
        }
        A.class.declaredMethods.each {
          assert it.name!="setFoo"
          
        }
        assert new A().foo==1
      """
      shouldFail { 
        shell.execute """
          class A {
            final foo = 1
          }
          new A().foo = 2
        """
      }
   }   
}

