import groovy.transform.Canonical
import groovy.transform.ToString
import groovy.util.logging.Log

import java.beans.Transient

@Log @Canonical class testNewlinedAnnotations {}

class A {
    //@Deprecated testAnnotationOnlyFieldDeclaration

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
