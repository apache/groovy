package org.codehaus.gram;

import groovy.lang.GroovyShell;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;

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
                System.out.println("Evaluting Groovy script: " + script);
                gram.execute(new File(script));
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
