package gls.annotations.closures

class AnnotationClosureWithNonLocalVariable extends GroovyTestCase {
    
    void testVanillaVariable() {
        assertScript """
            import java.lang.annotation.*
            
            @Retention(RetentionPolicy.RUNTIME)
            @interface Foo {
                Class value()
            }
            
            class X {
                @Foo({ value })
                def doit(value) {}
            }
            def clazz =  X.class.getDeclaredMethod("doit",[Object] as Class[]).getAnnotation(Foo.class).value()
            def closure = clazz.newInstance(this,this)
            closure.delegate = [value:1]
            assert closure() == 1        
        """
    }

}