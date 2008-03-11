package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;

public class ArrayOperations {

         public static class BooleanArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(boolean[].class);

            public Class getReturnType() {
                return Boolean.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final boolean[] objects = (boolean[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final boolean[] objects = (boolean[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof boolean[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final boolean[] objects = (boolean[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final boolean[] objects = (boolean[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class BooleanArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(boolean[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public BooleanArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final boolean[] objects = (boolean[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                objects[index] = ((Boolean)args[1]).booleanValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Boolean))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof boolean[] && args[0] instanceof Integer && args[1] instanceof Boolean )
                                    && checkMetaClass()) {
                                final boolean[] objects = (boolean[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Boolean)args[1]).booleanValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class ByteArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(byte[].class);

            public Class getReturnType() {
                return Byte.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final byte[] objects = (byte[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final byte[] objects = (byte[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof byte[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final byte[] objects = (byte[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final byte[] objects = (byte[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class ByteArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(byte[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public ByteArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final byte[] objects = (byte[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Byte)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).byteValue();
                }
                else
                  objects[index] = ((Byte)args[1]).byteValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Byte))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof byte[] && args[0] instanceof Integer && args[1] instanceof Byte )
                                    && checkMetaClass()) {
                                final byte[] objects = (byte[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Byte)args[1]).byteValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class CharacterArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(char[].class);

            public Class getReturnType() {
                return Character.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final char[] objects = (char[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final char[] objects = (char[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof char[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final char[] objects = (char[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final char[] objects = (char[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class CharacterArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(char[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public CharacterArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final char[] objects = (char[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                objects[index] = ((Character)args[1]).charValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Character))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof char[] && args[0] instanceof Integer && args[1] instanceof Character )
                                    && checkMetaClass()) {
                                final char[] objects = (char[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Character)args[1]).charValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class ShortArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(short[].class);

            public Class getReturnType() {
                return Short.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final short[] objects = (short[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final short[] objects = (short[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof short[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final short[] objects = (short[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final short[] objects = (short[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class ShortArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(short[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public ShortArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final short[] objects = (short[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Short)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).shortValue();
                }
                else
                  objects[index] = ((Short)args[1]).shortValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Short))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof short[] && args[0] instanceof Integer && args[1] instanceof Short )
                                    && checkMetaClass()) {
                                final short[] objects = (short[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Short)args[1]).shortValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class IntegerArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(int[].class);

            public Class getReturnType() {
                return Integer.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final int[] objects = (int[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final int[] objects = (int[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof int[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final int[] objects = (int[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final int[] objects = (int[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class IntegerArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(int[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public IntegerArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final int[] objects = (int[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Integer)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).intValue();
                }
                else
                  objects[index] = ((Integer)args[1]).intValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof int[] && args[0] instanceof Integer && args[1] instanceof Integer )
                                    && checkMetaClass()) {
                                final int[] objects = (int[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Integer)args[1]).intValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class LongArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(long[].class);

            public Class getReturnType() {
                return Long.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final long[] objects = (long[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final long[] objects = (long[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof long[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final long[] objects = (long[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final long[] objects = (long[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class LongArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(long[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public LongArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final long[] objects = (long[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Long)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).longValue();
                }
                else
                  objects[index] = ((Long)args[1]).longValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Long))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof long[] && args[0] instanceof Integer && args[1] instanceof Long )
                                    && checkMetaClass()) {
                                final long[] objects = (long[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Long)args[1]).longValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class FloatArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(float[].class);

            public Class getReturnType() {
                return Float.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final float[] objects = (float[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final float[] objects = (float[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof float[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final float[] objects = (float[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final float[] objects = (float[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class FloatArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(float[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public FloatArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final float[] objects = (float[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Float)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).floatValue();
                }
                else
                  objects[index] = ((Float)args[1]).floatValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Float))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof float[] && args[0] instanceof Integer && args[1] instanceof Float )
                                    && checkMetaClass()) {
                                final float[] objects = (float[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Float)args[1]).floatValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


         public static class DoubleArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(double[].class);

            public Class getReturnType() {
                return Double.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final double[] objects = (double[]) object;
                return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final double[] objects = (double[]) receiver;
                            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof double[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final double[] objects = (double[]) receiver;
                                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final double[] objects = (double[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                    };
            }
         }


        public static class DoubleArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(double[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public DoubleArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final double[] objects = (double[]) object;
                final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof Double)) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).doubleValue();
                }
                else
                  objects[index] = ((Double)args[1]).doubleValue();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof Double))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof double[] && args[0] instanceof Integer && args[1] instanceof Double )
                                    && checkMetaClass()) {
                                final double[] objects = (double[]) receiver;
                                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Double)args[1]).doubleValue();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }


}
