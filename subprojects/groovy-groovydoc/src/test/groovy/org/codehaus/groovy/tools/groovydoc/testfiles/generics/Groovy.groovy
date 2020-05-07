package org.codehaus.groovy.tools.groovydoc.testfiles.generics

/**
 * Generic class.
 *
 * @param <N> Doc.
 */
trait Groovy<N extends Number & Comparable<? extends Number>> {

    /**
     * Generic method.
     *
     * @param <A> Doc.
     * @param <B> Doc.
     * @param a Doc.
     * @param b Doc.
     * @return Doc.
     */
    static <A, B> int compare(Class<A> a, Class<B> b) {
        0
    }
}
