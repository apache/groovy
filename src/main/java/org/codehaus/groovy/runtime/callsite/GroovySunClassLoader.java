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
package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.reflection.SunClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class GroovySunClassLoader extends SunClassLoader {

    public static final SunClassLoader sunVM;

    static {
            sunVM = AccessController.doPrivileged((PrivilegedAction<SunClassLoader>) () -> {
                try {
                    if (SunClassLoader.sunVM != null) {
                        return new GroovySunClassLoader();
                    }
                }
                catch (Throwable t) {//
                }
                return null;
            });
    }

    protected GroovySunClassLoader () throws Throwable {
        super();
        loadAbstract ();
        loadFromRes("org.codehaus.groovy.runtime.callsite.MetaClassSite");
        loadFromRes("org.codehaus.groovy.runtime.callsite.MetaMethodSite");
        loadFromRes("org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite");
        loadFromRes("org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite");
        loadFromRes("org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite");
    }

    private void loadAbstract() throws IOException {
        final InputStream asStream = GroovySunClassLoader.class.getClassLoader().getResourceAsStream(resName("org.codehaus.groovy.runtime.callsite.AbstractCallSite"));
        ClassReader reader = new ClassReader(asStream);
        final ClassWriter cw = new ClassWriter(CompilerConfiguration.COMPUTE_MODE);
        final ClassVisitor cv = new ClassVisitor(CompilerConfiguration.ASM_API_VERSION, cw) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, "sun/reflect/GroovyMagic", interfaces);
            }            
        };
        reader.accept(cv, CompilerConfiguration.READ_MODE);
        asStream.close();
        define(cw.toByteArray(), "org.codehaus.groovy.runtime.callsite.AbstractCallSite");
    }

}
