import groovy.transform.Canonical
@Deprecated
import groovy.transform.ToString
import groovy.util.logging.Log

import java.beans.Transient

@Log
@Canonical class testNewlinedAnnotations {}

class A {
    @Deprecated testAnnotationOnlyFieldDeclaration

    private
    @Deprecated
    String typedField

    def
    field

    private
    int a() {}

    @Override() <T> T testAnnotationAndGenerics() {}

    @Transient
    private
    @Deprecated
    @Override
    def
    testAnnotationOrder() {};

    //abstract testAnnotatedParams(@Deprecated a, @Deprecated int b);
}
