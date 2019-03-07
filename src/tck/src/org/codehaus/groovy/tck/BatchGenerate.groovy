/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tck

class BatchGenerate {
    def generator
    def srcDirPath
    def targetDir
    def srcEncoding
    def srcs
    def spew

    BatchGenerate() {
        generator = new TestGenerator()
        // verbose = false
        spew = true
        srcDirPath = "./"
    }

    void setSrcdirPath(String pathName) {
        if (spew) {println("srcDir:${pathName}") }
        srcDirPath = pathName
    }

    void setTargetDirectory(File destDir) {
        if (spew) { println("destDir:${destDir}") }
        targetDir = destDir
    }

    void setSourceEncoding(String encoding) {
        if (spew) { println("encoding:${encoding}") }
        srcEncoding = encoding
    }

    void addSources( File[] compileList ) {
        if (spew) { println("compileList:${compileList}") }
        srcs = compileList
    }

    void setVerbose(boolean verbose) {
        spew = verbose
    }

    void compile() {
        if (spew) { println("compile()") }


        for (src in srcs) {
            println( src )
            // mung the ${test.src.dir}/gls/ch14/s4 path into ${dest.dir}/gls/ch14/s4
            // first determine the relative path e.g. gls/ch14/s4
            def relativeSrcFilePathAndName = src.getAbsolutePath().substring(srcDirPath.length() + 1)
            def relativeSrcFileNameStartIndex = relativeSrcFilePathAndName.lastIndexOf(File.separator)
            def relativeOutputPath = ""
            if (relativeSrcFileNameStartIndex >= 0) {
                relativeOutputPath = relativeSrcFilePathAndName.substring(0,relativeSrcFileNameStartIndex)
            }

            // then determine the absolute output path
            def ghostOutputFile = new File(targetDir, relativeSrcFilePathAndName)
            def ghostOutputFilePath = ghostOutputFile.getAbsolutePath()
            def fileNameStartIndex = ghostOutputFilePath.lastIndexOf(File.separator)
            def realOutputPath = ghostOutputFilePath.substring(0,fileNameStartIndex)

            // mkdir if does not exist
            File directory = new File(realOutputPath)
            if (directory != null && !directory.exists()) {
                directory.mkdirs()
            }

            // generate a suitable java file to put there
            def fileStem = src.name.tokenize(".")[0]
            def targetFileName = "${fileStem}Test.java"
            def anOutputFile = new File(realOutputPath, targetFileName)

            System.out.println("generating " + targetFileName)
            def someOutputText = generator.generate(relativeOutputPath, targetDir, src.name,src.text)
            if (someOutputText != null && someOutputText != "") {
                anOutputFile.write(someOutputText)
            }
        }
    }
}
