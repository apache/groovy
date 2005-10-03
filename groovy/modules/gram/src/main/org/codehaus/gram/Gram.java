package org.codehaus.gram;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helper service for invoking Groovy scripts based on a JAM service
 *
 * @version $Revision$
 */
public class Gram {
    private JamService jam;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: srcDir groovyScript");
            return;
        }
        String srcDir = args[0];
        System.out.println("Parsing source files in directory: " + srcDir);

        try {
            JamServiceFactory jamServiceFactory = JamServiceFactory.getInstance();

            JamServiceParams params = jamServiceFactory.createServiceParams();
            params.includeSourcePattern(new File[]{new File(srcDir)}, "**/*.java");
            JamService service = jamServiceFactory.createService(params);

            Gram gram = new Gram(service);
            for (int i = 1; i < args.length; i++) {
                String script = args[i];
                System.out.println("Evaluating Groovy script: " + script);
                gram.execute(script);
            }
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public Gram(JamService jam) {
        this.jam = jam;
    }

    public void execute(String script) throws CompilationFailedException, IOException {
        File file = new File(script);
        if (file.isFile()) {
            execute(file);
        }
        else {
            // lets try load the script on the classpath
            InputStream in = getClass().getClassLoader().getResourceAsStream(script);
            if (in == null) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(script);
                if (in == null) {
                    throw new IOException("No script called: " + script + " could be found on the classpath or the file system");
                }
            }
            GroovyShell shell = createShell();
            shell.evaluate(in, script);
        }
    }

    public void execute(File script) throws IOException, CompilationFailedException {
        GroovyShell shell = createShell();
        shell.evaluate(script);
    }

    protected GroovyShell createShell() {
        GroovyShell answer = new GroovyShell();
        answer.setProperty("jam", jam);
        answer.setProperty("classes", jam.getAllClasses());
        return answer;
    }
}
