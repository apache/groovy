/*
 * $Id$version Apr 26, 2004 4:22:50 PM $user Exp $
 * 
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author sam
 * @author Paul King
 */
public class GroovyCategorySupport {

    /**
     * This method is used to pull all the new methods out of the local thread context with a particular name.
     * 
     * @param categorizedClass a class subject to the category methods in the thread context
     * @param name the method name of interest
     * @return the list of methods
     */
    public static List getCategoryMethods(Class categorizedClass, String name) {
        Map properties = getProperties();
        List methodList = new ArrayList();
        for (Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
            Class current = (Class) i.next();
            if (current.isAssignableFrom(categorizedClass)) {
                Map metaMethodsMap = (Map) properties.get(current);
                List newMethodList = (List) metaMethodsMap.get(name);
                if (newMethodList != null) {
                    methodList.addAll(newMethodList);
                }
            }
        }
        if (methodList.size() == 0) return null;
        return methodList;
    }

    /**
     * This method is used to pull all the new methods out of the local thread context.
     *
     * @param categorizedClass a class subject to the category methods in the thread context
     * @return the list of methods
     */
    public static List getCategoryMethods(Class categorizedClass) {
        Map properties = getProperties();
        List methodList = new ArrayList();
        for (Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
            Class current = (Class) i.next();
            if (current.isAssignableFrom(categorizedClass)) {
                Map metaMethodsMap = (Map) properties.get(current);
                Collection collection = metaMethodsMap.values();
                for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
                    List newMethodList = (List) iterator.next();
                    if (newMethodList != null) {
                        methodList.addAll(newMethodList);
                    }                    
                }
            }
        }
        if (methodList.size() == 0) return null;
        return methodList;
    }

    private static class CategoryMethod extends NewInstanceMetaMethod implements Comparable {
        private Class metaClass;

        public CategoryMethod(MetaMethod metaMethod, Class metaClass) {
            super(metaMethod);
            this.metaClass = metaClass;
        }

        public boolean isCacheable() { return false; }

        /**
         * Sort by most specific to least specific.
         *
         * @param o the object to compare against
         */
        public int compareTo(Object o) {
            CategoryMethod thatMethod = (CategoryMethod) o;
            Class thisClass = metaClass;
            Class thatClass = thatMethod.metaClass;
            if (thisClass == thatClass) return 0;
            Class loop = thisClass;
            while(loop != Object.class) {
                loop = thisClass.getSuperclass();
                if (loop == thatClass) {
                    return -1;
                }
            }
            loop = thatClass;
            while (loop != Object.class) {
                loop = thatClass.getSuperclass();
                if (loop == thisClass) {
                    return 1;
                }
            }
            return 0;
        }
    }

    /**
     * Create a scope based on given categoryClass and invoke closure within that scope.
     *
     * @param categoryClass the class containing category methods
	 * @param closure the closure during which to make the category class methods available
	 */
	public static void use(Class categoryClass, Closure closure) {
		newScope();
		try {
			use(categoryClass);
			closure.call();
		} finally {
			endScope();
		}
	}

    /**
     * Create a scope based on given categoryClasses and invoke closure within that scope.
     *
     * @param categoryClasses the list of classes containing category methods
     * @param closure the closure during which to make the category class methods available
     */
    public static void use(List categoryClasses, Closure closure) {
        newScope();
        try {
            for (Iterator i = categoryClasses.iterator(); i.hasNext(); ) {
                Class clazz = (Class) i.next();
                use(clazz);
            }
            closure.call();
        } finally {
            endScope();
        }
    }

    /**
     * Delegated to from the global use(CategoryClass) method.  It scans the Category class for static methods
     * that take 1 or more parameters.  The first parameter is the class you are adding the category method to,
     * additional parameters are those paramteres needed by that method.  A use statement cannot be undone and
     * is valid only for the current thread.
     *
     * @param categoryClass the class containing category methods
     */
    private static void use(Class categoryClass) {
        Map properties = getProperties();
        Method[] methods = categoryClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (Modifier.isStatic(method.getModifiers())) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    Class metaClass = paramTypes[0];
                    Map metaMethodsMap = getMetaMethods(properties, metaClass);
                    List methodList = getMethodList(metaMethodsMap, method.getName());
                    MetaMethod mmethod = new CategoryMethod(new MetaMethod(method), metaClass);
                    methodList.add(mmethod);
                    Collections.sort(methodList);
                }
            }
        }
    }

    private static ThreadLocal local = new ThreadLocal() {
        protected Object initialValue() {
        		List stack = new ArrayList();
        		stack.add(Collections.EMPTY_MAP);
        		return stack;
        	}
    };
    
    private static void newScope() {
        List stack = (List) local.get();
    	Map properties = new WeakHashMap(getProperties());
    	stack.add(properties);
    }
    
    private static void endScope() {
        List stack = (List) local.get();
    	stack.remove(stack.size() - 1);
    }
    
    private static Map getProperties() {
        List stack = (List) local.get();
        return (Map) stack.get(stack.size() - 1);
    }
    
    private static List getMethodList(Map metaMethodsMap, String name) {
        List methodList = (List) metaMethodsMap.get(name);
        if (methodList == null) {
            methodList = new ArrayList(1);
            metaMethodsMap.put(name, methodList);
        }
        return methodList;
    }

    private static Map getMetaMethods(Map properties, Class metaClass) {
        Map metaMethodsMap = (Map) properties.get(metaClass);
        if (metaMethodsMap == null) {
            metaMethodsMap = new HashMap();
            properties.put(metaClass, metaMethodsMap);
        }
        return metaMethodsMap;
    }

}
