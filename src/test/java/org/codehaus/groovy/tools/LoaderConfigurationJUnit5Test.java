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
package org.codehaus.groovy.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for LoaderConfiguration class.
 */
class LoaderConfigurationJUnit5Test {

    private LoaderConfiguration config;

    @BeforeEach
    void setUp() {
        config = new LoaderConfiguration();
    }

    // Basic configuration tests
    @Test
    void testNewConfigurationHasNoClassPath() {
        assertEquals(0, config.getClassPathUrls().length);
    }

    @Test
    void testNewConfigurationHasNoMainClass() {
        assertNull(config.getMainClass());
    }

    @Test
    void testNewConfigurationHasNoGrabUrls() {
        assertTrue(config.getGrabUrls().isEmpty());
    }

    // setMainClass tests
    @Test
    void testSetMainClass() {
        config.setMainClass("com.example.Main");
        assertEquals("com.example.Main", config.getMainClass());
    }

    @Test
    void testSetMainClassDisablesRequireMain() throws IOException {
        config.setMainClass("com.example.Main");
        // Configure without main should now work
        String configContent = "# just a comment\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        assertEquals("com.example.Main", config.getMainClass());
    }

    // setRequireMain tests
    @Test
    void testSetRequireMainFalse() throws IOException {
        config.setRequireMain(false);
        String configContent = "# just a comment\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        assertNull(config.getMainClass());
    }

    @Test
    void testRequireMainTrueThrowsWithoutMain() {
        String configContent = "# just a comment\n";
        assertThrows(IOException.class, () ->
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8))));
    }

    // configure with main is tests
    @Test
    void testConfigureWithMainIs() throws IOException {
        String configContent = "main is com.example.Main\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        assertEquals("com.example.Main", config.getMainClass());
    }

    @Test
    void testConfigureDuplicateMainThrows() {
        String configContent = "main is com.example.Main\nmain is com.example.Other\n";
        assertThrows(IOException.class, () ->
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8))));
    }

    // configure with comments and empty lines
    @Test
    void testConfigureIgnoresComments() throws IOException {
        String configContent = "# this is a comment\nmain is com.example.Main\n# another comment\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        assertEquals("com.example.Main", config.getMainClass());
    }

    @Test
    void testConfigureIgnoresEmptyLines() throws IOException {
        String configContent = "\n\nmain is com.example.Main\n\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        assertEquals("com.example.Main", config.getMainClass());
    }

    // configure with load tests
    @Test
    void testConfigureWithLoadExistingFile(@TempDir Path tempDir) throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        String configContent = "main is com.example.Main\nload " + jarFile.toAbsolutePath() + "\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(1, urls.length);
        assertTrue(urls[0].toString().contains("test.jar"));
    }

    @Test
    void testConfigureWithLoadNonExistingFile() throws IOException {
        String configContent = "main is com.example.Main\nload /non/existent/path.jar\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        // Non-existent files are silently ignored
        assertEquals(0, config.getClassPathUrls().length);
    }

    // configure with grab tests
    @Test
    void testConfigureWithGrab() throws IOException {
        String configContent = "main is com.example.Main\ngrab org.example:lib:1.0\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        assertEquals(1, config.getGrabUrls().size());
        assertEquals("org.example:lib:1.0", config.getGrabUrls().get(0));
    }

    // configure with invalid line tests
    @Test
    void testConfigureWithInvalidLineThrows() {
        String configContent = "main is com.example.Main\ninvalid line here\n";
        assertThrows(IOException.class, () ->
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8))));
    }

    // addFile tests
    @Test
    void testAddFileWithExistingFile(@TempDir Path tempDir) throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        config.addFile(jarFile.toFile());
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(1, urls.length);
    }

    @Test
    void testAddFileWithNonExistingFile() {
        config.addFile(new File("/non/existent/file.jar"));
        assertEquals(0, config.getClassPathUrls().length);
    }

    @Test
    void testAddFileWithNullFile() {
        config.addFile((File) null);
        assertEquals(0, config.getClassPathUrls().length);
    }

    @Test
    void testAddFileWithFilename(@TempDir Path tempDir) throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        config.addFile(jarFile.toAbsolutePath().toString());
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(1, urls.length);
    }

    @Test
    void testAddFileWithNullFilename() {
        config.addFile((String) null);
        assertEquals(0, config.getClassPathUrls().length);
    }

    // addClassPath tests
    @Test
    void testAddClassPath(@TempDir Path tempDir) throws IOException {
        Path jarFile1 = tempDir.resolve("test1.jar");
        Path jarFile2 = tempDir.resolve("test2.jar");
        Files.createFile(jarFile1);
        Files.createFile(jarFile2);
        
        String classpath = jarFile1.toAbsolutePath() + File.pathSeparator + jarFile2.toAbsolutePath();
        config.addClassPath(classpath);
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(2, urls.length);
    }

    @Test
    void testAddClassPathWithWildcard(@TempDir Path tempDir) throws IOException {
        Path dir = tempDir.resolve("lib");
        Files.createDirectory(dir);
        Files.createFile(dir.resolve("a.jar"));
        Files.createFile(dir.resolve("b.jar"));
        Files.createFile(dir.resolve("not-a-jar.txt"));
        
        config.addClassPath(dir.toAbsolutePath() + "/*");
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(2, urls.length); // only .jar files
    }

    // Property expansion tests
    @Test
    void testConfigureWithPropertyExpansion(@TempDir Path tempDir) throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        String originalValue = System.getProperty("user.dir");
        try {
            System.setProperty("test.loader.path", tempDir.toAbsolutePath().toString());
            
            String configContent = "main is com.example.Main\nload ${test.loader.path}/test.jar\n";
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
            
            URL[] urls = config.getClassPathUrls();
            assertEquals(1, urls.length);
        } finally {
            System.clearProperty("test.loader.path");
        }
    }

    @Test
    void testConfigureWithMissingOptionalProperty() throws IOException {
        // ${nonexistent} - optional property that doesn't exist should cause line to be skipped
        String configContent = "main is com.example.Main\nload ${nonexistent.property}/test.jar\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        // Line should be skipped, no files loaded
        assertEquals(0, config.getClassPathUrls().length);
    }

    @Test
    void testConfigureWithMissingRequiredPropertyThrows() {
        // !{nonexistent} - required property that doesn't exist should throw
        String configContent = "main is com.example.Main\nload !{nonexistent.required.property}/test.jar\n";
        assertThrows(IllegalArgumentException.class, () ->
            config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8))));
    }

    // Wildcard tests
    @Test
    void testConfigureWithWildcardLoad(@TempDir Path tempDir) throws IOException {
        Path dir = tempDir.resolve("libs");
        Files.createDirectory(dir);
        Files.createFile(dir.resolve("a.jar"));
        Files.createFile(dir.resolve("b.jar"));
        
        String configContent = "main is com.example.Main\nload " + dir.toAbsolutePath() + "/*.jar\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(2, urls.length);
    }

    @Test
    void testConfigureWithRecursiveWildcard(@TempDir Path tempDir) throws IOException {
        Path dir = tempDir.resolve("libs");
        Path subdir = dir.resolve("subdir");
        Files.createDirectories(subdir);
        Files.createFile(dir.resolve("a.jar"));
        Files.createFile(subdir.resolve("b.jar"));
        
        // ** matches one or more directories, so only subdir/b.jar should match
        String configContent = "main is com.example.Main\nload " + dir.toAbsolutePath() + "/**/*.jar\n";
        config.configure(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
        
        URL[] urls = config.getClassPathUrls();
        assertEquals(1, urls.length); // Only b.jar in subdir matches
    }

    // Multiple files test
    @Test
    void testAddMultipleFiles(@TempDir Path tempDir) throws IOException {
        for (int i = 0; i < 5; i++) {
            Path jarFile = tempDir.resolve("test" + i + ".jar");
            Files.createFile(jarFile);
            config.addFile(jarFile.toFile());
        }
        
        assertEquals(5, config.getClassPathUrls().length);
    }

    // Directory test
    @Test
    void testAddDirectory(@TempDir Path tempDir) {
        config.addFile(tempDir.toFile());
        
        // Directories can be added to classpath
        assertEquals(1, config.getClassPathUrls().length);
    }
}
