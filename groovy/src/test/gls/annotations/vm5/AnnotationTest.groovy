package gls.annotations.vm5

import gls.CompilableTestSupport

/**
 * Various tests for annotation support
 *
 * @author Guillaume Laforge
 */
class AnnotationTest extends CompilableTestSupport {

    /**
     * The @OneToMany cascadeparameter takes an array of CascadeType.
     * To use this annotation in Java with this parameter, you do <code>@OneToMany(cascade = { CascadeType.ALL })</code>
     * In Groovy, you do <code>@OneToMany(cascade = [ CascadeType.ALL ])</code> (brackets instead of braces)
     * But when there's just one value in the array, the curly braces or brackets can be omitted:
     * <code>@OneToMany(cascade = [ CascadeType.ALL ])</code>
     */
    void testOmittingBracketsForSingleValueArrayParameter() {
        shouldCompile """
            import gls.annotations.vm5.*

            class Book {}

            class Author {
                @OneToMany(cascade = CascadeType.ALL)
                Set<Book> books
            }

            def annotation = Author.class.getDeclaredField('books').annotations[0]

            assert annotation instanceof OneToMany
            assert annotation.cascade() == [CascadeType.ALL]
        """
    }

}