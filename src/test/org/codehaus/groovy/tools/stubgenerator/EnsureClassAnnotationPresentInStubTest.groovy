package org.codehaus.groovy.tools.stubgenerator

/**
 * @author Guillaume Laforge
 */
class EnsureClassAnnotationPresentInStubTest extends StringSourcesStubTestCase {
    @Override
    Map<String, String> provideSources() {
        [
                'Foo.java': '''
                    package foo;

                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Retention;
                    import java.lang.annotation.RetentionPolicy;
                    import java.lang.annotation.Target;

                    @Retention(RetentionPolicy.RUNTIME)
                    @Target({ElementType.TYPE})
                    public @interface Foo {}
                ''',

                'bar/Bar.groovy': '''
                    package bar

                    import foo.Foo
                    @Foo
                    class Bar {}
                '''
        ]
    }

    @Override
    void verifyStubs() {
        assert classes['bar.Bar'].annotations[0].type.toString() == 'foo.Foo'
    }
}
