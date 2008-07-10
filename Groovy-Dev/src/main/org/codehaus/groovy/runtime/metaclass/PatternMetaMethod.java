package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.DynamicMetaMethod;
import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;

public class PatternMetaMethod extends DynamicMetaMethod {

    private final Pattern pattern;
    private final Closure callable;
    private final CachedClass declaringClass;
    private final boolean hasMatcherArg;

    public PatternMetaMethod(Pattern pattern, Class declaringClass, Closure c) {
        super (createParamTypes(c));
        this.pattern = pattern;
        callable = c;
        hasMatcherArg =  (callable.getParameterTypes().length != getParameterTypes().length);
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
    }

    private static Class[] createParamTypes(Closure c) {
        final Class[] classes = c.getParameterTypes();
        if (classes.length == 0)
          return classes;

        if (classes[0] != Matcher.class)
          return classes;

        Class newClass [] = new Class [classes.length-1];
        System.arraycopy(classes, 1, newClass, 0, classes.length-1);
        return newClass;
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public Class getReturnType() {
        return Object.class;
    }

    public boolean respondsTo(String name, Object[] args) {
        final Matcher matcher = pattern.matcher(name);
        return matcher.matches() && isValidExactMethod(args);
    }

    public Object invoke(String name, Object object, Object[] args) {
        final Matcher matcher = pattern.matcher(name);
        if (!matcher.matches())
          throw new GroovyRuntimeException(name + " doesn't match pattern " + pattern.toString());

        if (hasMatcherArg) {
            Object newArgs[] = new Object[args.length+1];
            newArgs [0] = matcher;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            args = newArgs;
        }

        Closure cloned = (Closure) callable.clone();
        cloned.setDelegate(object);
        return cloned.call(args);
    }

    public CachedClass getDeclaringClass() {
        return declaringClass;
    }

  /**
     * Retrieves the closure that is invoked by this MetaMethod
     *
     * @return The closure
     */
    public Closure getClosure() {
        return callable;
    }
}
