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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaMethod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneratedMetaMethod extends MetaMethod {
    private final String name;
    private final CachedClass declaringClass;
    private final Class returnType;

    public GeneratedMetaMethod(String name, CachedClass declaringClass, Class returnType, Class[] parameters) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.returnType = returnType;
        nativeParamTypes = parameters;
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return returnType;
    }

    public CachedClass getDeclaringClass() {
        return declaringClass;
    }

    public static class Proxy extends GeneratedMetaMethod {
        private volatile MetaMethod proxy;
        private final String className;

        public Proxy(String className, String name, CachedClass declaringClass, Class returnType, Class[] parameters) {
            super(name, declaringClass, returnType, parameters);
            this.className = className;
        }

        @Override
        public boolean isValidMethod(Class[] arguments) {
            return proxy().isValidMethod(arguments);
        }

        @Override
        public Object doMethodInvoke(Object object, Object[] argumentArray) {
            return proxy().doMethodInvoke(object, argumentArray);
        }

        public Object invoke(Object object, Object[] arguments) {
            return proxy().invoke(object, arguments);
        }

        public final MetaMethod proxy() {
            if (proxy == null) {
                synchronized(this) {
                    if (proxy == null) createProxy();
                }
            }
            return proxy;
        }

        private void createProxy() {
            try {
                Class<?> aClass = getClass().getClassLoader().loadClass(className.replace('/', '.'));
                Constructor<?> constructor = aClass.getConstructor(String.class, CachedClass.class, Class.class, Class[].class);
                proxy = (MetaMethod) constructor.newInstance(getName(), getDeclaringClass(), getReturnType(), getNativeParameterTypes());
            } catch (Throwable t) {
                t.printStackTrace();
                throw new GroovyRuntimeException("Failed to create DGM method proxy : " + t, t);
            }
        }
    }

    public static class DgmMethodRecord implements Serializable {
        private static final long serialVersionUID = -5639988016452884450L;
        public String className;
        public String methodName;
        public Class returnType;
        public Class[] parameters;

        private static final Class[] PRIMITIVE_CLASSES = {
                Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,

                boolean[].class, char[].class, byte[].class, short[].class,
                int[].class, long[].class, double[].class, float[].class,

                Object[].class, String[].class, Class[].class, Byte[].class, CharSequence[].class,
        };

        public static void saveDgmInfo(List<DgmMethodRecord> records, String file) throws IOException {
            try (DataOutputStream out =
                         new DataOutputStream(
                                 new BufferedOutputStream(
                                         new FileOutputStream(file)))) {
                Map<String, Integer> classes = new LinkedHashMap<String, Integer>();

                int nextClassId = 0;
                for (Class primitive : PRIMITIVE_CLASSES) {
                    classes.put(primitive.getName(), nextClassId++);
                }

                for (DgmMethodRecord record : records) {
                    String name = record.returnType.getName();
                    Integer id = classes.get(name);
                    if (id == null) {
                        id = nextClassId++;
                        classes.put(name, id);
                    }

                    for (int i = 0; i < record.parameters.length; i++) {
                        name = record.parameters[i].getName();
                        id = classes.get(name);
                        if (id == null) {
                            id = nextClassId++;
                            classes.put(name, id);
                        }
                    }
                }

                for (Map.Entry<String, Integer> stringIntegerEntry : classes.entrySet()) {
                    out.writeUTF(stringIntegerEntry.getKey());
                    out.writeInt(stringIntegerEntry.getValue());
                }
                out.writeUTF("");

                out.writeInt(records.size());
                for (DgmMethodRecord record : records) {
                    out.writeUTF(record.className);
                    out.writeUTF(record.methodName);
                    out.writeInt(classes.get(record.returnType.getName()));

                    out.writeInt(record.parameters.length);
                    for (int i = 0; i < record.parameters.length; i++) {
                        Integer key = classes.get(record.parameters[i].getName());
                        out.writeInt(key);
                    }
                }
            }
        }

        /*  FIXME
            It seems that `loader.getResourceAsStream("META-INF/dgminfo")` failed to find the "META-INF/dgminfo" file, which should be generated correctly by `task dgmConverter` in gradle script
            When the file is read, IOException "Stream closed" occurred.(Reproduce the issue: Menu "Build" -> Menu item "Rebuild Project" in IntelliJ IDEA).
            Here is the error message:

Information:Groovyc: While compiling groovy-xml_main:java.io.IOException: Stream closed
	at java.io.BufferedInputStream.getInIfOpen(BufferedInputStream.java:159)
	at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
	at java.io.BufferedInputStream.read(BufferedInputStream.java:265)
	at java.io.DataInputStream.readUnsignedShort(DataInputStream.java:337)
	at java.io.DataInputStream.readUTF(DataInputStream.java:589)
	at java.io.DataInputStream.readUTF(DataInputStream.java:564)
	at org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord.loadDgmInfo(GeneratedMetaMethod.java:194)
	at org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl.registerMethods(MetaClassRegistryImpl.java:205)
	at org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl.<init>(MetaClassRegistryImpl.java:112)
	at org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl.<init>(MetaClassRegistryImpl.java:90)
	at groovy.lang.GroovySystem.<clinit>(GroovySystem.java:36)
	at org.codehaus.groovy.runtime.InvokerHelper.<clinit>(InvokerHelper.java:86)
	at groovy.lang.GroovyObjectSupport.getDefaultMetaClass(GroovyObjectSupport.java:61)
	at groovy.lang.GroovyObjectSupport.<init>(GroovyObjectSupport.java:34)
	at groovy.lang.Closure.<init>(Closure.java:214)
	at groovy.lang.Closure.<init>(Closure.java:231)
	at groovy.lang.Closure$1.<init>(Closure.java:198)
	at groovy.lang.Closure.<clinit>(Closure.java:198)
	at org.apache.groovy.parser.antlr4.util.StringUtils.replaceLineEscape(StringUtils.java:123)
	at org.apache.groovy.parser.antlr4.util.StringUtils.replaceEscapes(StringUtils.java:116)
	at org.apache.groovy.parser.antlr4.util.StringUtils.replaceEscapes(StringUtils.java:105)
	at org.apache.groovy.parser.antlr4.AstBuilder.parseStringLiteral(AstBuilder.java:2443)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitStringLiteral(AstBuilder.java:2414)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitStringLiteralAlt(AstBuilder.java:3229)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitStringLiteralAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$StringLiteralAltContext.accept(GroovyParser.java:4066)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitLiteralPrmrAlt(AstBuilder.java:2834)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitLiteralPrmrAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$LiteralPrmrAltContext.accept(GroovyParser.java:9971)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPathExpression(AstBuilder.java:2004)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExpression(AstBuilder.java:2512)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExprAlt(AstBuilder.java:2530)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExprAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$PostfixExprAltContext.accept(GroovyParser.java:8241)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitExpressionListElement(AstBuilder.java:3179)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitEnhancedArgumentListElement(AstBuilder.java:2398)
	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1382)
	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:481)
	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:471)
	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:151)
	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:174)
	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:418)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitEnhancedArgumentList(AstBuilder.java:2334)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitArguments(AstBuilder.java:2320)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitCreator(AstBuilder.java:2898)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitNewPrmrAlt(AstBuilder.java:2844)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitNewPrmrAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$NewPrmrAltContext.accept(GroovyParser.java:9997)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPathExpression(AstBuilder.java:2004)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExpression(AstBuilder.java:2512)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExprAlt(AstBuilder.java:2530)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitPostfixExprAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$PostfixExprAltContext.accept(GroovyParser.java:8241)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitNormalExprAlt(AstBuilder.java:1834)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitNormalExprAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$NormalExprAltContext.accept(GroovyParser.java:8085)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitEnhancedStatementExpression(AstBuilder.java:1990)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitVariableInitializer(AstBuilder.java:1796)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitVariableDeclarator(AstBuilder.java:1785)
	at org.apache.groovy.parser.antlr4.AstBuilder.lambda$visitVariableDeclarators$18(AstBuilder.java:1758)
	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1382)
	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:481)
	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:471)
	at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:708)
	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:499)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitVariableDeclarators(AstBuilder.java:1761)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitVariableDeclaration(AstBuilder.java:1595)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitFieldDeclaration(AstBuilder.java:1264)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitMemberDeclaration(AstBuilder.java:1215)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitClassBodyDeclaration(AstBuilder.java:1188)
	at org.apache.groovy.parser.antlr4.AstBuilder.lambda$visitClassBody$10(AstBuilder.java:1060)
	at java.util.ArrayList.forEach(ArrayList.java:1257)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitClassBody(AstBuilder.java:1058)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitClassDeclaration(AstBuilder.java:1011)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitTypeDeclaration(AstBuilder.java:886)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitTypeDeclarationStmtAlt(AstBuilder.java:866)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitTypeDeclarationStmtAlt(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$TypeDeclarationStmtAltContext.accept(GroovyParser.java:6719)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.lambda$visitStatements$1(AstBuilder.java:285)
	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1382)
	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:481)
	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:471)
	at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:708)
	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:499)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitStatements(AstBuilder.java:286)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitCompilationUnit(AstBuilder.java:247)
	at org.apache.groovy.parser.antlr4.AstBuilder.visitCompilationUnit(AstBuilder.java:151)
	at org.apache.groovy.parser.antlr4.GroovyParser$CompilationUnitContext.accept(GroovyParser.java:284)
	at org.antlr.v4.runtime.tree.AbstractParseTreeVisitor.visit(AbstractParseTreeVisitor.java:20)
	at org.apache.groovy.parser.antlr4.AstBuilder.visit(AstBuilder.java:3958)
	at org.apache.groovy.parser.antlr4.AstBuilder.buildAST(AstBuilder.java:237)
	at org.apache.groovy.parser.antlr4.Antlr4ParserPlugin.buildAST(Antlr4ParserPlugin.java:72)
	at org.codehaus.groovy.control.SourceUnit.convert(SourceUnit.java:252)
	at org.codehaus.groovy.control.CompilationUnit$15.call(CompilationUnit.java:711)
	at org.codehaus.groovy.control.CompilationUnit.applyToSourceUnits(CompilationUnit.java:971)
	at org.codehaus.groovy.control.CompilationUnit.doPhaseOperation(CompilationUnit.java:636)
	at org.codehaus.groovy.control.CompilationUnit.processPhaseOperations(CompilationUnit.java:612)
	at org.codehaus.groovy.control.CompilationUnit.compile(CompilationUnit.java:589)
	at org.jetbrains.groovy.compiler.rt.GroovyCompilerWrapper.compile(GroovyCompilerWrapper.java:62)
	at org.jetbrains.groovy.compiler.rt.DependentGroovycRunner.runGroovyc(DependentGroovycRunner.java:115)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.jetbrains.groovy.compiler.rt.GroovycRunner.intMain2(GroovycRunner.java:136)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.jetbrains.jps.incremental.groovy.InProcessGroovyc.runGroovycInThisProcess(InProcessGroovyc.java:158)
	at org.jetbrains.jps.incremental.groovy.InProcessGroovyc.lambda$runGroovyc$0(InProcessGroovyc.java:88)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
         */
        public static List<DgmMethodRecord> loadDgmInfo() throws IOException {
            ClassLoader loader = DgmMethodRecord.class.getClassLoader();

            try (DataInputStream in =
                         new DataInputStream(
                                 new BufferedInputStream(
                                         loader.getResourceAsStream("META-INF/dgminfo")))) {

                Map<Integer, Class> classes = new HashMap<Integer, Class>();
                for (int i = 0; i < PRIMITIVE_CLASSES.length; i++) {
                    classes.put(i, PRIMITIVE_CLASSES[i]);
                }

                int skip = 0;
                for (; ; ) {
                    String name = in.readUTF();
                    if (name.length() == 0)
                        break;

                    int key = in.readInt();

                    if (skip++ < PRIMITIVE_CLASSES.length)
                        continue;

                    Class cls = null;
                    try {
                        cls = loader.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // under certain restrictive environments, loading certain classes may be forbidden
                        // and could yield a ClassNotFoundException (Google App Engine)
                        continue;
                    }
                    classes.put(key, cls);
                }

                int size = in.readInt();
                List<DgmMethodRecord> res = new ArrayList<DgmMethodRecord>(size);
                for (int i = 0; i != size; ++i) {
                    boolean skipRecord = false;
                    DgmMethodRecord record = new DgmMethodRecord();
                    record.className = in.readUTF();
                    record.methodName = in.readUTF();
                    record.returnType = classes.get(in.readInt());

                    if (record.returnType == null) {
                        skipRecord = true;
                    }

                    int psize = in.readInt();
                    record.parameters = new Class[psize];
                    for (int j = 0; j < record.parameters.length; j++) {
                        record.parameters[j] = classes.get(in.readInt());

                        if (record.parameters[j] == null) {
                            skipRecord = true;
                        }
                    }
                    if (!skipRecord) {
                        res.add(record);
                    }
                }

                return res;
            }
        }
    }
}
