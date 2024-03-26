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
package org.codehaus.groovy.control;

import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Make sure CompilerConfiguration works.
 */
public final class CompilerConfigurationTest {

    private Properties savedProperties;

    @Before
    public void setUp() {
        savedProperties = System.getProperties();
        System.setProperties(new Properties(savedProperties));
    }

    @After
    public void tearDown() {
        System.setProperties(savedProperties);
    }

    @Test
    public void testDefaultConstructor() {
        CompilerConfiguration config = CompilerConfiguration.DEFAULT;

        assertEquals(WarningMessage.LIKELY_ERRORS, config.getWarningLevel());
        assertEquals(Boolean.getBoolean("groovy.output.debug"), config.getDebug());
        assertEquals(Boolean.getBoolean("groovy.output.verbose"), config.getVerbose());
        assertEquals(10, config.getTolerance());
        assertEquals(100, config.getMinimumRecompilationInterval());
        assertEquals(System.getProperty("file.encoding", CompilerConfiguration.DEFAULT_SOURCE_ENCODING), config.getSourceEncoding());
        assertEquals(CompilerConfiguration.DEFAULT_TARGET_BYTECODE, config.getTargetBytecode());
        assertFalse(config.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(".groovy", config.getDefaultScriptExtension());
        assertNull(config.getJointCompilationOptions());
        assertNotNull(config.getPluginFactory());
        assertNull(config.getScriptBaseClass());
        assertNull(config.getTargetDirectory());
    }

    @Test
    public void testSetViaSystemProperties() {
        System.setProperty("groovy.warnings", "PaRaNoiA");
        System.setProperty("groovy.output.verbose", "trUE");
        System.setProperty("groovy.mem.stub", "true");
        System.setProperty("groovy.generate.stub.in.memory", "true");
        System.setProperty("groovy.recompile.minimumInterval", "867892345");

        assertEquals("PaRaNoiA", System.getProperty("groovy.warnings"));

        CompilerConfiguration config = new CompilerConfiguration(System.getProperties());

        assertEquals(WarningMessage.PARANOIA, config.getWarningLevel());
        assertFalse(config.getDebug());
        assertTrue(config.getVerbose());
        assertEquals(10, config.getTolerance());
        assertEquals(867892345, config.getMinimumRecompilationInterval());
        assertEquals(CompilerConfiguration.DEFAULT.getSourceEncoding(), config.getSourceEncoding());
        assertEquals(CompilerConfiguration.DEFAULT.getTargetBytecode(), config.getTargetBytecode());
        assertFalse(config.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(".groovy", config.getDefaultScriptExtension());
        assertNotNull(config.getJointCompilationOptions());
        assertTrue((Boolean) config.getJointCompilationOptions().get(CompilerConfiguration.MEM_STUB));
        assertNotNull(config.getPluginFactory());
        assertNull(config.getScriptBaseClass());
        assertNull(config.getTargetDirectory());
    }

    @Test
    public void testCopyConstructor1() {
        CompilerConfiguration init = new CompilerConfiguration();
        init.setWarningLevel(WarningMessage.POSSIBLE_ERRORS);
        init.setDebug(true);
        init.setParameters(true);
        init.setVerbose(false);
        init.setTolerance(720);
        init.setMinimumRecompilationInterval(234);
        init.setScriptBaseClass("blarg.foo.WhatSit");
        init.setSourceEncoding("LEAD-123");
        init.setTargetBytecode(CompilerConfiguration.JDK17);
        init.setRecompileGroovySource(true);
        init.setClasspath("File1" + File.pathSeparator + "Somewhere");
        File targetDirectory = new File("A wandering path");
        init.setTargetDirectory(targetDirectory);
        init.setDefaultScriptExtension(".jpp");
        init.setJointCompilationOptions(Collections.singletonMap("somekey", "somevalue"));
        init.addCompilationCustomizers(new ImportCustomizer().addStarImports("groovy.transform"));
        ParserPluginFactory pluginFactory = ParserPluginFactory.antlr4();
        init.setPluginFactory(pluginFactory);
        init.setLogClassgen(true);
        init.setLogClassgenStackTraceMaxDepth(100);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, init.getWarningLevel());
        assertTrue(init.getDebug());
        assertTrue(init.getParameters());
        assertFalse(init.getVerbose());
        assertEquals(720, init.getTolerance());
        assertEquals(234, init.getMinimumRecompilationInterval());
        assertEquals("blarg.foo.WhatSit", init.getScriptBaseClass());
        assertEquals("LEAD-123", init.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK17, init.getTargetBytecode());
        assertTrue(init.getRecompileGroovySource());
        assertEquals("File1", init.getClasspath().get(0));
        assertEquals("Somewhere", init.getClasspath().get(1));
        assertEquals(targetDirectory, init.getTargetDirectory());
        assertEquals(".jpp", init.getDefaultScriptExtension());
        assertEquals("somevalue", init.getJointCompilationOptions().get("somekey"));
        assertNull(init.getJointCompilationOptions().get(CompilerConfiguration.MEM_STUB));
        assertEquals(pluginFactory, init.getPluginFactory());
        assertEquals(1, init.getCompilationCustomizers().size());
        assertTrue(init.isLogClassgen());
        assertEquals(100, init.getLogClassgenStackTraceMaxDepth());

        //

        CompilerConfiguration config = new CompilerConfiguration(init);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, config.getWarningLevel());
        assertTrue(config.getDebug());
        assertFalse(config.getVerbose());
        assertEquals(720, config.getTolerance());
        assertEquals(234, config.getMinimumRecompilationInterval());
        assertEquals("blarg.foo.WhatSit", config.getScriptBaseClass());
        assertEquals("LEAD-123", config.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK17, config.getTargetBytecode());
        assertTrue(config.getRecompileGroovySource());
        assertEquals("File1", config.getClasspath().get(0));
        assertEquals("Somewhere", config.getClasspath().get(1));
        assertEquals(targetDirectory, config.getTargetDirectory());
        assertEquals(".jpp", config.getDefaultScriptExtension());
        assertEquals("somevalue", config.getJointCompilationOptions().get("somekey"));
        assertEquals(pluginFactory, config.getPluginFactory());
        assertTrue(config.isLogClassgen());
        assertEquals(100, config.getLogClassgenStackTraceMaxDepth());
        // TODO GROOVY-9585: re-enable below assertion once prod code is fixed
//        assertEquals(1, config.getCompilationCustomizers().size());
    }

    @Test
    public void testCopyConstructor2() {
        final CompilerConfiguration init = new CompilerConfiguration();

        init.setWarningLevel(WarningMessage.POSSIBLE_ERRORS);
        init.setDebug(false);
        init.setParameters(false);
        init.setVerbose(true);
        init.setTolerance(55);
        init.setMinimumRecompilationInterval(975);
        init.setScriptBaseClass("");
        init.setSourceEncoding("Gutenberg");
        init.setTargetBytecode(CompilerConfiguration.JDK17);
        init.setRecompileGroovySource(false);
        init.setClasspath("");
        File targetDirectory = new File("A wandering path");
        init.setTargetDirectory(targetDirectory);
        init.setLogClassgen(true);
        init.setLogClassgenStackTraceMaxDepth(100);
        ParserPluginFactory pluginFactory = ParserPluginFactory.antlr4();
        init.setPluginFactory(pluginFactory);
        init.setDefaultScriptExtension(".jpp");

        assertEquals(WarningMessage.POSSIBLE_ERRORS, init.getWarningLevel());
        assertFalse(init.getDebug());
        assertFalse(init.getParameters());
        assertTrue(init.getVerbose());
        assertEquals(55, init.getTolerance());
        assertEquals(975, init.getMinimumRecompilationInterval());
        assertEquals("", init.getScriptBaseClass());
        assertEquals("Gutenberg", init.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK17, init.getTargetBytecode());
        assertFalse(init.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), init.getClasspath());
        assertEquals(targetDirectory, init.getTargetDirectory());
        assertTrue(init.isLogClassgen());
        assertEquals(100, init.getLogClassgenStackTraceMaxDepth());
        assertEquals(pluginFactory, init.getPluginFactory());
        assertEquals(".jpp", init.getDefaultScriptExtension());
        assertNull(init.getJointCompilationOptions());

        //

        CompilerConfiguration config = new CompilerConfiguration(init);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, config.getWarningLevel());
        assertFalse(config.getDebug());
        assertTrue(config.getVerbose());
        assertEquals(55, config.getTolerance());
        assertEquals(975, config.getMinimumRecompilationInterval());
        assertEquals("", config.getScriptBaseClass());
        assertEquals("Gutenberg", config.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK17, config.getTargetBytecode());
        assertFalse(config.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(targetDirectory, config.getTargetDirectory());
        assertTrue(config.isLogClassgen());
        assertEquals(100, config.getLogClassgenStackTraceMaxDepth());
        assertEquals(pluginFactory, config.getPluginFactory());
        assertEquals(".jpp", config.getDefaultScriptExtension());
        assertNull(config.getJointCompilationOptions());
    }

    @Test
    public void testDefaultConfigurationIsImmutable() {
        CompilerConfiguration config = CompilerConfiguration.DEFAULT;

        assertThrows(UnsupportedOperationException.class, () -> {
            config.setClasspath("");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.addCompilationCustomizers(new ImportCustomizer());
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setDebug(false);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setDefaultScriptExtension(".jpp");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setLogClassgen(true);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setLogClassgenStackTraceMaxDepth(100);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setMinimumRecompilationInterval(975);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setParameters(false);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setPluginFactory(ParserPluginFactory.antlr4());
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setRecompileGroovySource(false);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setScriptBaseClass("");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setSourceEncoding("Gutenberg");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setTargetBytecode("11");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setTargetDirectory(new File("path"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setTolerance(55);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setVerbose(true);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            config.setWarningLevel(WarningMessage.POSSIBLE_ERRORS);
        });
    }

    @Test // GROOVY-10278
    public void testTargetVersion() {
        CompilerConfiguration config = new CompilerConfiguration();
        String[] inputs = {"1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "5" , "6" , "7" , "8" , "9" , "9.0", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};
        String[] expect = {"11" , "11" , "11" , "11" , "11" , "11" , "11" , "11", "11", "11", "11", "11", "11" , "11", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "23"};
        assertArrayEquals(expect, Arrays.stream(inputs).map(v -> { config.setTargetBytecode(v); return config.getTargetBytecode(); }).toArray(String[]::new));
    }
}
