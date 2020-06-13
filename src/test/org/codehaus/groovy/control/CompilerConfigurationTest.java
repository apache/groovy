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
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertEquals(CompilerConfiguration.JDK8, config.getTargetBytecode());
        assertEquals(Boolean.FALSE, config.getRecompileGroovySource());
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
        System.setProperty("groovy.generate.stub.in.memory", "true");
        System.setProperty("groovy.recompile.minimumInterval", "867892345");

        assertEquals("PaRaNoiA", System.getProperty("groovy.warnings"));

        CompilerConfiguration config = new CompilerConfiguration(System.getProperties());

        assertEquals(WarningMessage.PARANOIA, config.getWarningLevel());
        assertEquals(Boolean.FALSE, config.getDebug());
        assertEquals(Boolean.TRUE, config.getVerbose());
        assertEquals(10, config.getTolerance());
        assertEquals(867892345, config.getMinimumRecompilationInterval());
        assertEquals(CompilerConfiguration.DEFAULT.getSourceEncoding(), config.getSourceEncoding());
        assertEquals(CompilerConfiguration.DEFAULT.getTargetBytecode(), config.getTargetBytecode());
        assertEquals(Boolean.FALSE, config.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(".groovy", config.getDefaultScriptExtension());
        assertNull(config.getJointCompilationOptions());
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
        init.setTargetBytecode(CompilerConfiguration.JDK5);
        init.setRecompileGroovySource(true);
        init.setClasspath("File1" + File.pathSeparator + "Somewhere");
        File targetDirectory = new File("A wandering path");
        init.setTargetDirectory(targetDirectory);
        init.setDefaultScriptExtension(".jpp");
        init.setJointCompilationOptions(Collections.singletonMap("somekey", "somevalue"));
        init.addCompilationCustomizers(new ImportCustomizer().addStarImports("groovy.transform"));
        ParserPluginFactory pluginFactory = ParserPluginFactory.antlr4();
        init.setPluginFactory(pluginFactory);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, init.getWarningLevel());
        assertEquals(Boolean.TRUE, init.getDebug());
        assertEquals(Boolean.TRUE, init.getParameters());
        assertEquals(Boolean.FALSE, init.getVerbose());
        assertEquals(720, init.getTolerance());
        assertEquals(234, init.getMinimumRecompilationInterval());
        assertEquals("blarg.foo.WhatSit", init.getScriptBaseClass());
        assertEquals("LEAD-123", init.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK5, init.getTargetBytecode());
        assertEquals(Boolean.TRUE, init.getRecompileGroovySource());
        assertEquals("File1", init.getClasspath().get(0));
        assertEquals("Somewhere", init.getClasspath().get(1));
        assertEquals(targetDirectory, init.getTargetDirectory());
        assertEquals(".jpp", init.getDefaultScriptExtension());
        assertEquals("somevalue", init.getJointCompilationOptions().get("somekey"));
        assertEquals(pluginFactory, init.getPluginFactory());
        assertEquals(1, init.getCompilationCustomizers().size());

        //

        CompilerConfiguration config = new CompilerConfiguration(init);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, config.getWarningLevel());
        assertEquals(Boolean.TRUE, config.getDebug());
        assertEquals(Boolean.FALSE, config.getVerbose());
        assertEquals(720, config.getTolerance());
        assertEquals(234, config.getMinimumRecompilationInterval());
        assertEquals("blarg.foo.WhatSit", config.getScriptBaseClass());
        assertEquals("LEAD-123", config.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK5, config.getTargetBytecode());
        assertEquals(Boolean.TRUE, config.getRecompileGroovySource());
        assertEquals("File1", config.getClasspath().get(0));
        assertEquals("Somewhere", config.getClasspath().get(1));
        assertEquals(targetDirectory, config.getTargetDirectory());
        assertEquals(".jpp", config.getDefaultScriptExtension());
        assertEquals("somevalue", config.getJointCompilationOptions().get("somekey"));
        assertEquals(pluginFactory, config.getPluginFactory());
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
        init.setTargetBytecode(CompilerConfiguration.JDK5);
        init.setRecompileGroovySource(false);
        init.setClasspath("");
        File targetDirectory = new File("A wandering path");
        init.setTargetDirectory(targetDirectory);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, init.getWarningLevel());
        assertEquals(Boolean.FALSE, init.getDebug());
        assertEquals(Boolean.FALSE, init.getParameters());
        assertEquals(Boolean.TRUE, init.getVerbose());
        assertEquals(55, init.getTolerance());
        assertEquals(975, init.getMinimumRecompilationInterval());
        assertEquals("", init.getScriptBaseClass());
        assertEquals("Gutenberg", init.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK5, init.getTargetBytecode());
        assertEquals(Boolean.FALSE, init.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), init.getClasspath());
        assertEquals(targetDirectory, init.getTargetDirectory());

        //

        CompilerConfiguration config = new CompilerConfiguration(init);

        assertEquals(WarningMessage.POSSIBLE_ERRORS, config.getWarningLevel());
        assertEquals(Boolean.FALSE, config.getDebug());
        assertEquals(Boolean.TRUE, config.getVerbose());
        assertEquals(55, config.getTolerance());
        assertEquals(975, config.getMinimumRecompilationInterval());
        assertEquals("", config.getScriptBaseClass());
        assertEquals("Gutenberg", config.getSourceEncoding());
        assertEquals(CompilerConfiguration.JDK5, config.getTargetBytecode());
        assertEquals(Boolean.FALSE, config.getRecompileGroovySource());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(targetDirectory, config.getTargetDirectory());
    }
}
