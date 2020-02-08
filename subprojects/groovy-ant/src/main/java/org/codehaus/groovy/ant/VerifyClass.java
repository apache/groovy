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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Verify Class files. This task can take the following
 * arguments:
 * <ul>
 * <li>dir
 * </ul>
 * When this task executes, it will recursively scan the dir and
 * look for class files to verify.
 */
public class VerifyClass extends MatchingTask {
    private String topDir = null;
    private boolean verbose = false;

    public VerifyClass() {
    }

    public void execute() throws BuildException {
        if (topDir == null) throw new BuildException("no dir attribute is set");
        File top = new File(topDir);
        if (!top.exists()) throw new BuildException("the directory " + top + " does not exist");
        log("top dir is " + top);
        int fails = execute(top);
        if (fails == 0) {
            log("no bytecode problems found");
        } else {
            log("found " + fails + " failing classes");
        }
    }

    public void setDir(String dir) throws BuildException {
        topDir = dir;
    }

    public void setVerbose(boolean v) {
        verbose = v;
    }

    private int execute(File dir) {
        int fails = 0;
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                fails += execute(f);
            } else if (f.getName().endsWith(".class")) {
                try {
                    boolean ok = readClass(f.getCanonicalPath());
                    if (!ok) fails++;
                } catch (IOException ioe) {
                    log(ioe.getMessage());
                    throw new BuildException(ioe);
                }
            }
        }
        return fails;
    }

    private boolean readClass(String clazz) throws IOException {
        ClassNode ca;
        try (final InputStream inputStream =
                     new BufferedInputStream(
                             new FileInputStream(clazz))) {
            ClassReader cr = new ClassReader(inputStream);
            ca = new ClassNode() {
                @Override
                public void visitEnd() {
                    //accept(cv);
                }
            };
            cr.accept(new CheckClassAdapter(ca), ClassWriter.COMPUTE_MAXS);
        }

        boolean failed = false;
        List<MethodNode> methods = ca.methods;
        for (MethodNode method : methods) {
            if (method.instructions.size() > 0) {
                Analyzer<?> a = new Analyzer<>(new SimpleVerifier());
                try {
                    a.analyze(ca.name, method);
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!failed) {
                    failed = true;
                    log("verifying of class " + clazz + " failed");
                }
                if (verbose) log(method.name + method.desc);

                TraceMethodVisitor mv = new TraceMethodVisitor(null);
                for (int j = 0; j < method.instructions.size(); ++j) {
                    AbstractInsnNode insn = method.instructions.get(j);
                    if (insn != null) {
                        insn.accept(mv);
                    } else {
                        mv.visitLabel(null);
                    }
                }
                mv.visitMaxs(method.maxStack, method.maxLocals);
            }
        }
        return !failed;
    }

}
