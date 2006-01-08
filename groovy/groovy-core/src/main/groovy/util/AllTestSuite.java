package groovy.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.ScriptTestAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * AllTestSuite can be used in extension of GroovyTestSuite to execute TestCases written in Groovy
 * from inside a Java IDE.
 * AllTestSuite collects all files below a given directory that comply to a given pattern.
 * From these files, a TestSuite is constructed that can be run via an IDE graphical Test runner.
 * The files are assumed to be Groovy source files and be either a TestCase or a Script that can
 * be wrapped transparently into a TestCase.
 * The directory and the pattern can be set via System properties (see this classes' constants for details.)
 *
 * When setting the loglevel of this class to FINEST, all file loading will be logged.
 *
 * See also groovy.util.AllTestSuiteTest.groovy
 * @author Dierk Koenig based on a prototype by Andrew Glover
 */
public class AllTestSuite extends TestSuite {

    /** The System Property to set as base directory for collection of Test Cases.
     * The pattern will be used as an Ant fileset include basedir.
     * Key is "groovy.test.dir".
     * Default value is "./test/".
     */
    public static final String SYSPROP_TEST_DIR = "groovy.test.dir";

    /** The System Property to set as the filename pattern for collection of Test Cases.
     * The pattern will be used as an Ant fileset include pattern and must comply to that
     * format. That also means that a comma-separated list of patterns is allowed as well.
     * Key is "groovy.test.pattern".
     * Default value is "&#42;&#42;/&#42;Test.groovy".
     */
    public static final String SYSPROP_TEST_PATTERN = "groovy.test.pattern";

    private static Logger LOG = Logger.getLogger(AllTestSuite.class.getName());
    private static ClassLoader JAVA_LOADER = AllTestSuite.class.getClassLoader();
    private static GroovyClassLoader GROOVY_LOADER = new GroovyClassLoader(JAVA_LOADER);

    private static final String[] EMPTY_ARGS = new String[]{};
    private static IFileNameFinder FINDER = null;

    static { // this is only needed since the Groovy Build compiles *.groovy files after *.java files
        try {
            Class finderClass = Class.forName("groovy.util.FileNameFinder");
            FINDER = (IFileNameFinder) finderClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find and instantiate class FileNameFinder", e);
        }
    }

    public static Test suite() {
        String basedir = System.getProperty(SYSPROP_TEST_DIR, "./test/");
        String pattern = System.getProperty(SYSPROP_TEST_PATTERN, "**/*Test.groovy");
        return suite(basedir, pattern);
    }    

    public static Test suite(String basedir, String pattern) {
        AllTestSuite suite = new AllTestSuite();
        String fileName = "";
        try {
            Collection filenames = FINDER.getFileNames(basedir, pattern);
            for (Iterator iter = filenames.iterator(); iter.hasNext();) {
                fileName = (String) iter.next();
                LOG.finest("trying to load "+ fileName);
                suite.loadTest(fileName);
            }
        } catch (CompilationFailedException e1) {
            e1.printStackTrace();
            throw new RuntimeException("CompilationFailedException when loading "+fileName, e1);
        } catch (IOException e2) {
            throw new RuntimeException("IOException when loading "+fileName, e2);
        }
        return suite;
    }

    protected void loadTest(String fileName) throws CompilationFailedException, IOException {
        Class type = compile(fileName);
        if (!Test.class.isAssignableFrom(type) && Script.class.isAssignableFrom(type)) {
            addTest(new ScriptTestAdapter(type, EMPTY_ARGS));
        } else {
            addTestSuite(type);
        }
    }

    protected Class compile(String fileName) throws CompilationFailedException, IOException {
        return GROOVY_LOADER.parseClass(new File(fileName));
    }
}
