/**
 * @author Jeremy Rayner
 */
package org.codehaus.groovy.tck

import java.io.File;

class BatchGenerate {
    def generator;
    def srcDirPath;
    def targetDir;
    def srcEncoding;
    def srcs;
    def spew

    public BatchGenerate() {
        generator = new TestGenerator();
        // verbose = false;
        spew = true;
        srcDirPath = "./";
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
            println( src )
            // mung the ${test.src.dir}/gls/ch14/s4 path into ${dest.dir}/gls/ch14/s4
            // first determine the relative path e.g. gls/ch14/s4
            def relativeSrcFilePathAndName = src.getAbsolutePath().substring(srcDirPath.length() + 1)
            def relativeSrcFileNameStartIndex = relativeSrcFilePathAndName.lastIndexOf(File.separator);
            def relativeOutputPath = ""
            if (relativeSrcFileNameStartIndex >= 0) {
                relativeOutputPath = relativeSrcFilePathAndName.substring(0,relativeSrcFileNameStartIndex);
            }

            // then determine the absolute output path
            def ghostOutputFile = new File(targetDir, relativeSrcFilePathAndName)
            def ghostOutputFilePath = ghostOutputFile.getAbsolutePath()
            def fileNameStartIndex = ghostOutputFilePath.lastIndexOf(File.separator);
            def realOutputPath = ghostOutputFilePath.substring(0,fileNameStartIndex);

            // mkdir if does not exist
            File directory = new File(realOutputPath)
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            // generate a suitable java file to put there
            def fileStem = src.name.tokenize(".")[0]
            def targetFileName = "${fileStem}Test.java"
            def anOutputFile = new File(realOutputPath, targetFileName)

            System.out.println("generating " + targetFileName)
            def someOutputText = generator.generate(relativeOutputPath, targetDir, src.name,src.text);
            if (someOutputText != null && someOutputText != "") {
                anOutputFile.write(someOutputText);
            }
        }
    }
}
