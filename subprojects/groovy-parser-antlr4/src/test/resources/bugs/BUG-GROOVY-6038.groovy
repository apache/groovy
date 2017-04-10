import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Upper {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Inner {
        String testInner()
    }
}

class X {
    @Upper.Inner(testInner='abc')
    def m() {}
}

def m = X.class.declaredMethods.find { it.name == 'm' }
assert m.declaredAnnotations[0].testInner() == 'abc'
