package groovy.transform

import java.lang.ref.SoftReference
import java.lang.reflect.Modifier

/**
 * Unit tests for the Lazy annotation
 *
 * @author Tim Yates
 */
class LazyTest extends GroovyTestCase {
    public void testLazyPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass( 
          '''class MyClass {
            |    @Lazy int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private non-volatile Integer
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isVolatile(field.modifiers)
        assert field.type == Integer
    }

    public void testLazyVolatilePrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass( 
          '''class MyClass {
            |    @Lazy volatile int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private volatile Integer
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert Modifier.isVolatile(field.modifiers)
        assert field.type == Integer
    }

    public void testLazySoftPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass( 
          '''class MyClass {
            |    @Lazy(soft=true) int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private non-volatile SoftReference
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isVolatile(field.modifiers)
        assert field.type == SoftReference
    }

    public void testLazyVolatileSoftPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass( 
          '''class MyClass {
            |    @Lazy(soft=true) volatile int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private volatile SoftReference
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert Modifier.isVolatile(field.modifiers)
        assert field.type == SoftReference
    }
}