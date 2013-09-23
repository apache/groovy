package groovy.transform

class ImmutableTest extends GroovyTestCase {
    public void testCopyWith() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
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

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    public void testGenericsCopyWith() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |class Person {
            |    List<String> names
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( [ 'Tim', 'Yates' ] )
        assert tim.names == [ 'Tim', 'Yates' ]

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( names:[ 'Tim', 'Yates' ] )
        assert tim.is( tim2 )

        // This should be a new instance
        def alice = tim.copyWith( names:[ 'Alice', 'Yates' ] )
        assert tim != alice
        assert alice.names == [ 'Alice', 'Yates' ]
        assert !alice.is( tim )
    }

    public void testStaticCopyWith() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.CompileStatic
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

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    public void testTypedCopyWith() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.TypeChecked
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

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    public void testCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |class Person {
            |    String first, last
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
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

    public void testStaticCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.CompileStatic
            |class Person {
            |    String first, last
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
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

    public void testTypedCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass(
          '''@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.TypeChecked
            |class Person {
            |    String first, last
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
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