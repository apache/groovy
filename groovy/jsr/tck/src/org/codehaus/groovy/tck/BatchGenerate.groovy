/**
 * @author Jeremy Rayner
 */
package org.codehaus.groovy.tck

import java.io.File;

class BatchGenerate {
    property generator;
    property targetDir;
    property srcEncoding;
    property srcs;
    property spew

    public BatchGenerate() {
        generator = new TestGenerator();
        verbose = false;
    }

    public void setTargetDirectory(File destDir) {
        if (spew) { println("destDir:${destDir}") }
        targetDir = destDir;
    }

    public void setSourceEncoding(String encoding) {
        if (spew) { println("encoding:${encoding}") }
        srcEncoding = encoding;
    }

    public void addSources( File[] compileList ) {
        if (spew) { println("compileList:${compileList}") }
        srcs = compileList
    }

    public void setVerbose(boolean verbose) {
        spew = verbose
    }

    public void compile() {
        if (spew) { println("compile()") }

        for (src in srcs) {
            fileStem = src.name.tokenize(".")[0]
            targetFileName = "${fileStem}Test.java"
            anOutputFile = new File(targetDir, targetFileName)
            System.out.println("generating " + targetFileName)
            someOutputText = generator.generate(targetDir, src.name,src.text);
            if (someOutputText != null && someOutputText != "") {
                anOutputFile.write(someOutputText);
            }
        }
    }
}
