

/*
interface Comparable { int compareTo(Object o); }
abstract class GString { abstract String[] getStrings(); }
interface CharSequence {
    char charAt(int);  int length();  CharSequence subsequence(int, int);  String toString()
}

abstract  class AbstractList: { abstract Object get(int index); abstract int size(); }

*/

package org.codehaus.groovy.syntax.parser;

import groovy.lang.GroovyObject;
import groovy.lang.MissingClassException;
import groovy.lang.MissingPropertyException;

import java.io.ByteArrayInputStream;

import org.codehaus.groovy.classgen.RuntimeIncompleteClassException;
import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;

public class AbstractMethodCheckTest extends TestSupport {


    /**
     * GString literals generate an anonymous inner class which 
     * adds an implementation of the abstract String[] getStrings()
     * method. 
     */
    public void testAnonymousGStringAbstract() throws Exception {
	GroovyObject object =
	    assertCompileWorks(
               "class GStringTest {\n" +
               "    public groovy.lang.GString testGStringAbstract(Integer x, Integer y) {\n" + 
               "       return \"GStringTest of ${x} and ${y}\"\n" +
               "    }\n" +
               "}\n");

    }

    public void testCompleteComparable() throws Exception {
	GroovyObject object =
	    assertCompileWorks(
	     "class ComparableTest implements java.lang.Comparable {\n" +
	     "    int compareTo(java.lang.Object o) {\n" +
	     "	return 0;\n" +
	     "    }\n" +
	     "}\n");
    }


    public void testIncompleteCharSequence() throws Exception {
	// Should generate an error for not implementing subsequence
	MissingClassException e =
	    assertCompileFailed(
           "class IncompleteCharSequenceTest implements java.lang.CharSequence {\n" +
	       "    char charAt(int pos) {\n" +
	       "	return 'a';\n" +
	       "    }\n" +
	       "\n" +
	       "    int length() {\n" +
	       "	return 5;\n" +
	       "    }\n" +
	       "}\n");
    }

    public void testCompleteCharSequence() throws Exception {
	GroovyObject object =
	    assertCompileWorks(
              "class CompleteCharSequenceTest implements java.lang.CharSequence {\n" +
	      "    char charAt(int pos) {\n" +
	      "	return 'a';\n" +
	      "    }\n" +
	      "\n" +
	      "    int length() {\n" +
	      "	return 5;\n" +
	      "    }\n" +
	      "\n" +
	      "   CharSequence subSequence(int start, int end) {\n" +
	      "	return null;\n" +
	      "    }\n" +
	      "}\n");
    }


    public void testIncompleteList() throws Exception {
	// Should generate an error for not implementing Object get(int)
	MissingClassException e =
	    assertCompileFailed(
               "class IncompleteList extends java.util.AbstractList {\n" +
	       "    int size() { return 0; }\n" +
	       "}\n");
    }


    public void testCompleteList() throws Exception {
	GroovyObject object =
	    assertCompileWorks(
               "class CompleteList extends java.util.AbstractList {\n" +
	       "    int size() { return 0; }\n" +
	       "    Object get(int pos) { return null; }\n" +
	       "}\n");
    }

    
    protected GroovyObject assertCompileWorks(String code) throws Exception {
        Class type =
            loader.parseClass(new ByteArrayInputStream(code.getBytes()), "ValidClass_" + getMethodName() + ".groovy");
        return (GroovyObject) type.newInstance();
    }

    protected MissingClassException assertCompileFailed(String code) throws Exception {
        try {
            assertCompileWorks(code);

            fail("Should have thrown an exception");
        }
        catch( CompilationFailedException e ) {
            Exception cause = e.getUnit().getException(0);
            if( cause instanceof  RuntimeIncompleteClassException ) {
                System.out.println("Worked, threw: " + cause);
                //e.printStackTrace();
                return null;
            }
            throw e;
        }
        return null;
    }

}
