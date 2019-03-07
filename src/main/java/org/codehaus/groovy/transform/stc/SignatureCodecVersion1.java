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
package org.codehaus.groovy.transform.stc;

import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;

/**
 * First implementation of an inferred type signature codec.
 */
public class SignatureCodecVersion1 implements SignatureCodec {

    private final ClassLoader classLoader;

    public SignatureCodecVersion1(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private void doEncode(final ClassNode node, DataOutputStream dos) throws IOException {
        dos.writeUTF(node.getClass().getSimpleName());
        if (node instanceof UnionTypeClassNode) {
            UnionTypeClassNode union = (UnionTypeClassNode) node;
            ClassNode[] delegates = union.getDelegates();
            dos.writeInt(delegates.length);
            for (ClassNode delegate : delegates) {
                doEncode(delegate, dos);
            }
            return;
        } else if (node instanceof WideningCategories.LowestUpperBoundClassNode) {
            WideningCategories.LowestUpperBoundClassNode lub = (WideningCategories.LowestUpperBoundClassNode) node;
            dos.writeUTF(lub.getLubName());
            doEncode(lub.getUnresolvedSuperClass(), dos);
            ClassNode[] interfaces = lub.getInterfaces();
            if (interfaces == null) {
                dos.writeInt(-1);
            } else {
                dos.writeInt(interfaces.length);
                for (ClassNode anInterface : interfaces) {
                    doEncode(anInterface, dos);
                }
            }
            return;
        }
        if (node.isArray()) {
            dos.writeBoolean(true);
            doEncode(node.getComponentType(), dos);
        } else {
            dos.writeBoolean(false);
            dos.writeUTF(BytecodeHelper.getTypeDescription(node));
            dos.writeBoolean(node.isUsingGenerics());
            GenericsType[] genericsTypes = node.getGenericsTypes();
            if (genericsTypes == null) {
                dos.writeInt(-1);
            } else {
                dos.writeInt(genericsTypes.length);
                for (GenericsType type : genericsTypes) {
                    dos.writeBoolean(type.isPlaceholder());
                    dos.writeBoolean(type.isWildcard());
                    doEncode(type.getType(), dos);
                    ClassNode lb = type.getLowerBound();
                    if (lb == null) {
                        dos.writeBoolean(false);
                    } else {
                        dos.writeBoolean(true);
                        doEncode(lb, dos);
                    }
                    ClassNode[] upperBounds = type.getUpperBounds();
                    if (upperBounds == null) {
                        dos.writeInt(-1);
                    } else {
                        dos.writeInt(upperBounds.length);
                        for (ClassNode bound : upperBounds) {
                            doEncode(bound, dos);
                        }
                    }
                }
            }
        }
    }

    public String encode(final ClassNode node) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        DataOutputStream dos = new DataOutputStream(baos);
        Writer wrt = new StringBuilderWriter();
        String encoded = null;
        try {
            doEncode(node, dos);
            EncodingGroovyMethods.encodeBase64(baos.toByteArray()).writeTo(wrt);
            encoded = wrt.toString();
        } catch (IOException e) {
            throw new GroovyRuntimeException("Unable to serialize type information", e);
        }
        return encoded;
    }

    private ClassNode doDecode(final DataInputStream dis) throws IOException {
        String classNodeType = dis.readUTF();
        if (UnionTypeClassNode.class.getSimpleName().equals(classNodeType)) {
            int len = dis.readInt();
            ClassNode[] delegates = new ClassNode[len];
            for (int i = 0; i < len; i++) {
                delegates[i] = doDecode(dis);
            }
            return new UnionTypeClassNode(delegates);
        } else if (WideningCategories.LowestUpperBoundClassNode.class.getSimpleName().equals(classNodeType)) {
            String name = dis.readUTF();
            ClassNode upper = doDecode(dis);
            int len = dis.readInt();
            ClassNode[] interfaces = null;
            if (len >= 0) {
                interfaces = new ClassNode[len];
                for (int i = 0; i < len; i++) {
                    interfaces[i] = doDecode(dis);
                }
            }
            return new WideningCategories.LowestUpperBoundClassNode(name, upper, interfaces);
        }
        boolean makeArray = dis.readBoolean();
        if (makeArray) {
            return doDecode(dis).makeArray();
        }
        String typedesc = dis.readUTF();
        char typeCode = typedesc.charAt(0);
        ClassNode result = OBJECT_TYPE;
        if (typeCode == 'L') {
            // object type
            String className = typedesc.replace('/', '.').substring(1, typedesc.length() - 1);
            try {
                result = ClassHelper.make(Class.forName(className, false, classLoader)).getPlainNodeReference();
            } catch (ClassNotFoundException e) {
                result = ClassHelper.make(className);
            }
            result.setUsingGenerics(dis.readBoolean());
            int len = dis.readInt();
            if (len >= 0) {
                GenericsType[] gts = new GenericsType[len];
                for (int i = 0; i < len; i++) {
                    boolean placeholder = dis.readBoolean();
                    boolean wildcard = dis.readBoolean();
                    ClassNode type = doDecode(dis);
                    boolean low = dis.readBoolean();
                    ClassNode lb = null;
                    if (low) {
                        lb = doDecode(dis);
                    }
                    int upc = dis.readInt();
                    ClassNode[] ups = null;
                    if (upc >= 0) {
                        ups = new ClassNode[upc];
                        for (int j = 0; j < upc; j++) {
                            ups[j] = doDecode(dis);
                        }
                    }
                    GenericsType gt = new GenericsType(
                            type, ups, lb
                    );
                    gt.setPlaceholder(placeholder);
                    gt.setWildcard(wildcard);
                    gts[i] = gt;
                }
                result.setGenericsTypes(gts);
            }
        } else {
            // primitive type
            switch (typeCode) {
                case 'I': result = int_TYPE; break;
                case 'Z': result = boolean_TYPE; break;
                case 'B': result = byte_TYPE; break;
                case 'C': result = char_TYPE; break;
                case 'S': result = short_TYPE; break;
                case 'D': result = double_TYPE; break;
                case 'F': result = float_TYPE; break;
                case 'J': result = long_TYPE; break;
                case 'V': result = VOID_TYPE; break;
            }
        }
        return result;
    }

    public ClassNode decode(final String signature) {
        DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(EncodingGroovyMethods.decodeBase64(signature)));
        try {
            return doDecode(dis);
        } catch (IOException e) {
            throw new GroovyRuntimeException("Unable to read type information", e);
        }
    }
}
