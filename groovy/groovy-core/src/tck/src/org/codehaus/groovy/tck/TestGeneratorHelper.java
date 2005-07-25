package org.codehaus.groovy.tck;

/** Helper methods used by generated TCK test cases */

public interface TestGeneratorHelper {
    void evaluate(String theSrcText, String testName) throws Exception;
    void parse(String theSrcText, String testName) throws Exception;
}
