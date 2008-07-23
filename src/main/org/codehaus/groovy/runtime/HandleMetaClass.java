package org.codehaus.groovy.runtime;

import groovy.lang.*;

import java.lang.reflect.Method;

public class HandleMetaClass extends DelegatingMetaClass {
    private Object object;
    private static MetaClass myMetaClass;
    private static final Object NONE = new Object();

    public HandleMetaClass(MetaClass mc) {
        this(mc, null);
    }

    public HandleMetaClass(MetaClass mc, Object obj) {
        super(mc);
        if (obj != null) {
            if (InvokerHelper.getMetaClass(obj.getClass()) == mc)
              object = obj; // object has default meta class, so we need to replace it on demand
            else
              object = NONE; // object already has per instance meta class
        }

        if (myMetaClass == null)
          myMetaClass = InvokerHelper.getMetaClass(getClass());
    }

    public void initialize() {
        replaceDelegate();
        delegate.initialize();
    }

    public GroovyObject replaceDelegate() {
        if (object == null) {
            if (!(delegate instanceof ExpandoMetaClass)) {
              delegate = ExpandoMetaClassCreationHandle.instance.create(delegate.getTheClass(), GroovySystem.getMetaClassRegistry());
              delegate.initialize();
              DefaultGroovyMethods.setMetaClass(delegate.getTheClass(), delegate);
            }
        }
        else {
          if (object != NONE) {
              delegate = ExpandoMetaClassCreationHandle.instance.create(object.getClass(), GroovySystem.getMetaClassRegistry());
              delegate.initialize();
              DefaultGroovyMethods.setMetaClass(object, delegate);
              object = NONE;
          }
        }
        return (GroovyObject)delegate;
    }

    public Object invokeMethod(String name, Object args) {
        return replaceDelegate().invokeMethod(name, args);
    }

    // this method mimics EMC behavior
    public Object getProperty(String property) {
        if(ExpandoMetaClass.isValidExpandoProperty(property)) {
            if(property.equals(ExpandoMetaClass.STATIC_QUALIFIER) ||
               property.equals(ExpandoMetaClass.CONSTRUCTOR) ||
               myMetaClass.hasProperty(this, property) == null) {
                  return replaceDelegate().getProperty(property);
            }
        }
        return myMetaClass.getProperty(this, property);
    }

    public void setProperty(String property, Object newValue) {
        replaceDelegate().setProperty(property, newValue);
    }

    public void addNewInstanceMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    public void addNewStaticMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    public void addMetaMethod(MetaMethod metaMethod) {
        throw new UnsupportedOperationException();
    }

    public void addMetaBeanProperty(MetaBeanProperty metaBeanProperty) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        return super.equals(obj) || getAdaptee().equals(obj) || (obj instanceof HandleMetaClass && equals(((HandleMetaClass)obj).getAdaptee()));
    }
}
