package groovy.transform

class ImmutableTest extends GroovyTestCase {
    public void testCopyWith() {
        def tester = new GroovyClassLoader().parseClass( 
          '''@groovy.transform.Immutable(addCopyWith = true)
            |class Person {
            |    String first, last
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( first:'tim', last:'yates' )
        assert tim.first == 'tim'
        
        // This should be the same instance and no changes
        def tim2 = tim.copyWith( first:'tim' )
        assert tim.is( tim2 )
        
        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    public void testCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass( 
          '''@groovy.transform.Immutable(addCopyWith = true)
            |class Person {
            |    String first, last
            |    List<Person> copyWith( i ) {
            |        [ this ] * i    
            |    }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( first:'tim', last:'yates' )
        assert tim.first == 'tim'
        
        // Check original copyWith remains
        def result = tim.copyWith( 2 )
        assert result.size() == 2
        assert result.first == [ 'tim', 'tim' ]
    }
}