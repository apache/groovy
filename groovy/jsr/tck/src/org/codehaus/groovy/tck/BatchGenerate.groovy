/**
 * @author Jeremy Rayner
 */
package org.codehaus.groovy.tck

import java.io.File;

class BatchGenerate {
    property generator;
    property srcDirPath;
    property targetDir;
    property srcEncoding;
    property srcs;
    property spew

    public BatchGenerate() {
        generator = new TestGenerator();
        verbose = false;
    }

    public void setSrcdirPath(String pathName) {
        if (spew) {println("srcDir:${pathName}") }
        srcDirPath = pathName;
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

            // mung the ${test.src.dir}/gls/ch14/s4 path into ${dest.dir}/gls/ch14/s4
            // first determine the relative path e.g. gls/ch14/s4
            relativeSrcFilePathAndName = src.getAbsolutePath().substring(srcDirPath.length() + 1)
            relativeSrcFileNameStartIndex = relativeSrcFilePathAndName.lastIndexOf(File.separator);
            relativeOutputPath = ""
            if (relativeSrcFileNameStartIndex >= 0) {
                relativeOutputPath = relativeSrcFilePathAndName.substring(0,relativeSrcFileNameStartIndex);
            }

            // then determine the absolute output path
            ghostOutputFile = new File(targetDir, relativeSrcFilePathAndName)
            ghostOutputFilePath = ghostOutputFile.getAbsolutePath()
            fileNameStartIndex = ghostOutputFilePath.lastIndexOf(File.separator);
            realOutputPath = ghostOutputFilePath.substring(0,fileNameStartIndex);

            // mkdir if doesn't exist
            File directory = new File(realOutputPath)
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            // generate a suitable java file to put there
            fileStem = src.name.tokenize(".")[0]
            targetFileName = "${fileStem}Test.java"
            anOutputFile = new File(realOutputPath, targetFileName)

            System.out.println("generating " + targetFileName)
            someOutputText = generator.generate(relativeOutputPath, targetDir, src.name,src.text);
            if (someOutputText != null && someOutputText != "") {
                anOutputFile.write(someOutputText);
            }
        }
    }
}
