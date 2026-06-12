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
package groovy.ant

/**
 * Find files according to a base directory and an includes and excludes pattern.
 * The include and exclude patterns conform to Ant's fileset pattern conventions.
 */
class FileNameFinder implements IFileNameFinder {

    /**
     * Finds files below the supplied base directory that match the given include pattern.
     *
     * @param basedir the directory to scan
     * @param pattern the Ant-style include pattern
     * @return the absolute paths of matching files
     */
    List<String> getFileNames(String basedir, String pattern) {
        getFileNames(dir: basedir, includes: pattern)
    }

    /**
     * Finds files below the supplied base directory that match the include pattern and avoid the exclude pattern.
     *
     * @param basedir the directory to scan
     * @param pattern the Ant-style include pattern
     * @param excludesPattern the Ant-style exclude pattern
     * @return the absolute paths of matching files
     */
    List<String> getFileNames(String basedir, String pattern, String excludesPattern) {
        getFileNames(dir: basedir, includes: pattern, excludes: excludesPattern)
    }

    /**
     * Finds files using the supplied Ant {@code fileset} arguments.
     *
     * @param args the arguments passed to the underlying Ant {@code fileset}
     * @return the absolute paths of matching files
     */
    List<String> getFileNames(Map args) {
        def ant = new AntBuilder()
        def scanner = ant.fileScanner {
            fileset(args)
        }
        List<String> files = []
        for (File f in scanner) {
            files << f.absolutePath
        }
        files
    }
}
