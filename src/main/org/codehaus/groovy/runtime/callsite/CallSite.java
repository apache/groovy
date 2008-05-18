package org.codehaus.groovy.runtime.callsite;

import java.util.concurrent.atomic.AtomicInteger;

public interface CallSite {
    CallSiteArray getArray();

    int getIndex();

    String getName();

    AtomicInteger getUsage();


    Object getProperty(Object receiver);

    Object callGetPropertySafe (Object receiver);

    Object callGetProperty (Object receiver);

    Object callGroovyObjectGetProperty (Object receiver);


    Object call(Object receiver, Object[] args);

    Object call (Object receiver) throws Throwable;

    Object call (Object receiver, Object arg1) throws Throwable;

    Object call (Object receiver, Object arg1, Object arg2) throws Throwable;

    Object call (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    Object call (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;


    Object callSafe(Object receiver, Object[] args) throws Throwable;

    Object callSafe (Object receiver) throws Throwable;

    Object callSafe (Object receiver, Object arg1) throws Throwable;

    Object callSafe (Object receiver, Object arg1, Object arg2) throws Throwable;

    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;


    Object callCurrent (Object receiver, Object [] args) throws Throwable;

    Object callCurrent (Object receiver) throws Throwable;

    Object callCurrent (Object receiver, Object arg1) throws Throwable;

    Object callCurrent (Object receiver, Object arg1, Object arg2) throws Throwable;

    Object callCurrent (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    Object callCurrent (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;


    Object callStatic (Object receiver, Object [] args);

    Object callStatic (Object receiver) throws Throwable;

    Object callStatic (Object receiver, Object arg1) throws Throwable;

    Object callStatic (Object receiver, Object arg1, Object arg2) throws Throwable;

    Object callStatic (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    Object callStatic (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;


    Object callConstructor (Object receiver, Object [] args) throws Throwable;

    Object callConstructor (Object receiver) throws Throwable;

    Object callConstructor (Object receiver, Object arg1) throws Throwable;

    Object callConstructor (Object receiver, Object arg1, Object arg2) throws Throwable;

    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;
}
