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

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getReturnType() {
        return returnType;
    }

    @Override
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

        @Override
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
