package org.codehaus.groovy.dummy;

/**
 * Class used by groovy.bugs.StaticMethodImportBug.
 * Bug reference: Explicit import needed to call static method, GROOVY-935
 */
class ClassWithStaticMethod {
    static boolean staticMethod() { return true }
}