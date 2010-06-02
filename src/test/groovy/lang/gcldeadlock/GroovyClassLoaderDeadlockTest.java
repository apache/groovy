package groovy.lang.gcldeadlock;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Check that GroovyClassLoader (through GroovyScriptEngine) can compile scripts concurrently.
 * Test for GROOVY-4002
 *
 * @author Guillaume Laforge
 */
public class GroovyClassLoaderDeadlockTest extends TestCase {
    private static final String PATH = "./src/test/groovy/lang/gcldeadlock/";

    private static class Runner extends Thread {
        private GroovyScriptEngine gse;
        private String script;
        private int count;
        private String result = "";

        public Runner(GroovyScriptEngine gse, String script, int count) {
            this.gse = gse;
            this.script = script;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Binding b = new Binding();
                b.setVariable("number", count);
                result = (String) this.gse.run(script, b);
            } catch (Throwable t) {
                throw new RuntimeException("problem running script", t);
            }
        }

        public String getResult() {
            return result;
        }
    }

    public void testNoDeadlockWhenTwoThreadsCompileScripts() throws IOException, InterruptedException {
        String[] roots = new String[] { PATH };
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        Runner[] runners = new Runner[] {
                new Runner(gse, "script0.groovy", 0),
                new Runner(gse, "script1.groovy", 1)
        };
        for (Runner runner : runners) {
            runner.start();
        }
        for (Runner runner : runners) {
            runner.join();
        }
        assertEquals("0+0=0", runners[0].getResult());
        assertEquals("1+1=2", runners[1].getResult());
    }
}
