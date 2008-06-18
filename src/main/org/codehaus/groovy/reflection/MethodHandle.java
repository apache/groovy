package org.codehaus.groovy.reflection;

import org.codehaus.groovy.runtime.ArrayUtil;


public class MethodHandle {

    protected MethodHandle () {
    }

    public Object invoke(Object receiver, Object [] args) throws Throwable { throw new AbstractMethodError(); }
    public Object invoke(Object receiver) throws Throwable { return invoke(receiver,ArrayUtil.createArray()); }
    public Object invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3,arg4)); }

    public Object invoke(Object receiver, boolean arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, boolean arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, char arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, char arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver, ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, char arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, byte arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, short arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, short arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, short arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, int arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, int arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, int arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, long arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, long arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, long arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, float arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, float arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, float arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, double arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, double arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, double arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, boolean arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, char arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, char arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, byte arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, short arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, short arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, int arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, int arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, long arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, long arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, float arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, float arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, double arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, double arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, boolean arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, char arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, byte arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, short arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, int arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, long arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, float arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, double arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
    public Object invoke(Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }
}
