package org.codehaus.groovy.reflection;

import groovy.lang.MetaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.*;
import java.util.*;

import org.apache.tools.ant.util.ReflectWrapper;

public abstract class GeneratedMetaMethod extends MetaMethod {
    private final String name;
    private final CachedClass declaringClass;
    private final Class returnType;

    public GeneratedMetaMethod(String name, CachedClass declaringClass, Class returnType, Class [] parameters) {
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

        public final synchronized MetaMethod proxy() {
            if (proxy == null) {
                createProxy();
            }
            return proxy;
        }

        private void createProxy() {
            try {
                Class<?> aClass = getClass().getClassLoader().loadClass(className.replace('/','.'));
                Constructor<?> constructor = aClass.getConstructor(String.class, CachedClass.class, Class.class, Class[].class);
                proxy = (MetaMethod) constructor.newInstance(getName(), getDeclaringClass(), getReturnType(), getNativeParameterTypes());
            }
            catch (Throwable t) {
                t.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static class DgmMethodRecord implements Serializable {
        public String  className;
        public String  methodName;
        public Class   returnType;
        public Class[] parameters;

        private static final Class[] primitiveClasses = new Class[] {
            Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
            Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,

            boolean [].class, char [].class, byte [].class, short [].class,
            int [].class, long [].class, double[].class, float [].class,

            Object [].class, String [].class, Class  [].class, Byte[].class
        };

        public static void saveDgmInfo(ArrayList<DgmMethodRecord> records, String file) throws IOException {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            LinkedHashMap<String,Integer> classes = new LinkedHashMap<String,Integer> ();

            int nextClassId = 0;
            for (int i = 0; i < primitiveClasses.length; i++) {
                classes.put(primitiveClasses[i].getName(), nextClassId++);
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
            out.close();
        }

        public static ArrayList<DgmMethodRecord>  loadDgmInfo() throws IOException, ClassNotFoundException {

            ClassLoader loader = DgmMethodRecord.class.getClassLoader();
            DataInputStream in = new DataInputStream(new BufferedInputStream(loader.getResourceAsStream("META-INF/dgminfo")));

            HashMap<Integer,Class> classes = new HashMap<Integer, Class>();
            for (int i = 0; i < primitiveClasses.length; i++) {
                classes.put(i, primitiveClasses[i]);
            }

            int skip = 0;
            for (;;) {
                String name = in.readUTF();
                if (name.length() == 0)
                    break;

                int key = in.readInt();

                if (skip++ < primitiveClasses.length)
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
            ArrayList<DgmMethodRecord> res = new ArrayList<DgmMethodRecord>(size);
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

            in.close();

            return res;
        }
    }
}
