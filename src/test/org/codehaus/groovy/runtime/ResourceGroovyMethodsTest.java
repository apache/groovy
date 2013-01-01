/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.util.GroovyTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class ResourceGroovyMethodsTest extends GroovyTestCase {

    public void testFileDirectorySizeExceptions() throws IOException {
        try {
            ResourceGroovyMethods.directorySize(new File("doesn't exist"));
            fail("directorySize() should fail when directory specified doesn't exist");
        } catch (IOException expected) {
        }

        File tempFile = File.createTempFile("testDirectorySizeExceptions", "");

        try {
            ResourceGroovyMethods.directorySize(tempFile);
            fail("directorySize() should fail when a file is specified");
        } catch (IllegalArgumentException expected) {
        }

        tempFile.delete();
    }

    public void testDirectorySize() throws IOException {
        File tempFile = File.createTempFile("__testDirectorySize__", "");
        delete(tempFile);

        File testDir = new File(tempFile.getAbsolutePath());
        testDir.mkdirs();

        final int nFiles = 3;
        final int maxFileSize = 102488;

        long totalSize = 0;
        Random r = new Random(new Random(System.currentTimeMillis()).nextLong());

        for (int j = 0; j < nFiles; j++) {
            int fileSize = r.nextInt(maxFileSize);
            String path = (r.nextBoolean() ? "a/" : "") + (r.nextBoolean() ? "b/" : "") +
                    (r.nextBoolean() ? "c/" : "") + (r.nextBoolean() ? "d/" : "") +
                    (r.nextBoolean() ? "e/" : "") + (r.nextBoolean() ? "f" : "");

            String filePath = String.format("%s/%s/%s", path, j, fileSize);
            createFile(new File(testDir, filePath), fileSize);
            totalSize += fileSize;
        }

        assertEquals(totalSize, ResourceGroovyMethods.directorySize(testDir));
        delete(testDir);
    }

    /**
     * Creates empty file of size specified.
     *
     * @param file file to create
     * @param size file size
     */
    private static void createFile(File file, int size) {
        file.getParentFile().mkdirs();

        try {
            OutputStream os = new FileOutputStream(file);
            os.write(new byte[size]);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to create [%s] of size [%s]: %s",
                    file.getAbsolutePath(), size, e),
                    e);
        }

        if (file.length() != size) {
            throw new RuntimeException(String.format("Failed to create [%s] of size [%s]",
                    file.getAbsolutePath(), size));
        }
    }

    /**
     * Deletes file or directory specified. If directory is not empty, all its files are deleted as well.
     *
     * @param file file or directory to delete
     */
    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }

        if (!file.delete()) {
            // Sometimes empty directory is not deleted on the first attempt
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            if (!file.delete()) {
                throw new RuntimeException(String.format("Failed to delete [%s]", file.getAbsolutePath()));
            }
        }

        if (file.exists()) {
            throw new RuntimeException(String.format("[%s] is not deleted", file.getAbsolutePath()));
        }
    }
}
