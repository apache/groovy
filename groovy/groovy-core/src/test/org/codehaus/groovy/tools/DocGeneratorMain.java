package org.codehaus.groovy.tools;

import groovy.lang.GroovyShell;

public class DocGeneratorMain {

    public static void main(String[] args) {
        try {
            GroovyShell shell = new GroovyShell();
            //shell.run("src/main/org/codehaus/groovy/tools/DocGenerator.groovy", "org.codehaus.groovy.tools.DocGenerator.groovy", args);
            shell.run("src/main/org/codehaus/groovy/tools/DocGenerator.groovy", args);
        }
        catch (Exception e) {
            System.out.println("Failed: " + e);
            e.printStackTrace();
        }
    }
}
