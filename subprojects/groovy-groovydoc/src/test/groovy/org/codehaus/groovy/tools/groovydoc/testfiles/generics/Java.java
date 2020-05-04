package org.codehaus.groovy.tools.groovydoc.testfiles.generics;

/**
 * Generic class.
 *
 * @param <N> Doc.
 */
public abstract class Java<N extends Number & Comparable<? extends Number>> {
    /**
     * Generic method.
     *
     * @param <A> Doc.
     * @param <B> Doc.
     * @param a Doc.
     * @param b Doc.
     * @return Doc.
     */
    public static <A, B> int compare(Class<A> a, Class<B> b) {
        return 0;
    }
}
