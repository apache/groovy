import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

def closureClass = NestedAnnotationWithDefault.getAnnotation(AnnWithNestedAnnWithDefault).elem().elem()
def closure = closureClass.newInstance(null, null)
assert closure.call() == 3


@AnnWithNestedAnnWithDefault(elem = @AnnWithDefaultValue())
class NestedAnnotationWithDefault {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithDefaultValue {
    Class elem() default { 1 + 2 }
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithNestedAnnWithDefault {
    AnnWithDefaultValue elem()
}
