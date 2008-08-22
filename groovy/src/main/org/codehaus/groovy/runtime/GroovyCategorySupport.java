/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author sam
 * @author Paul King
 */
public class GroovyCategorySupport {

    private static int categoriesInUse = 0;

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
        if (methodList.isEmpty()) return null;
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
        if (methodList.isEmpty()) return null;
        return methodList;
    }

    public static Object getClosestMatchingCategoryMethod(Class sender, MetaMethod orig, MetaMethod element) {
        // for now just compare MetaMethods
        if (orig instanceof CategoryMethod && element instanceof CategoryMethod) {
            CategoryMethod o = (CategoryMethod) orig;
            CategoryMethod e = (CategoryMethod) element;
            if (o.compareTo(e) < 0) {
                return orig;
            }
        }
        return element;
    }
    
    private static class CategoryMethod extends NewInstanceMetaMethod implements Comparable {
        private final Class metaClass;

        public CategoryMethod(CachedMethod metaMethod, Class metaClass) {
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
            if (isChildOfParent(thisClass, thatClass)) return -1;
            if (isChildOfParent(thatClass, thisClass)) return 1;
            return 0;
        }

        private boolean isChildOfParent(Class candidateChild, Class candidateParent) {
            Class loop = candidateChild;
            while(loop != null && loop != Object.class) {
                loop = loop.getSuperclass();
                if (loop == candidateParent) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Create a scope based on given categoryClass and invoke closure within that scope.
     *
     * @param categoryClass the class containing category methods
	 * @param closure the closure during which to make the category class methods available
     * @return the value returned from the closure
	 */
	public static Object use(Class categoryClass, Closure closure) {
		newScope();
		try {
			use(categoryClass);
			return closure.call();
		} finally {
			endScope();
		}
	}

    /**
     * Create a scope based on given categoryClasses and invoke closure within that scope.
     *
     * @param categoryClasses the list of classes containing category methods
     * @param closure the closure during which to make the category class methods available
     * @return the value returned from the closure
     */
    public static Object use(List categoryClasses, Closure closure) {
        newScope();
        try {
            for (Iterator i = categoryClasses.iterator(); i.hasNext(); ) {
                Class clazz = (Class) i.next();
                use(clazz);
            }
            return closure.call();
        } finally {
            endScope();
        }
    }

    /**
     * Delegated to from the global use(CategoryClass) method.  It scans the Category class for static methods
     * that take 1 or more parameters.  The first parameter is the class you are adding the category method to,
     * additional parameters are those parameters needed by that method.  A use statement cannot be undone and
     * is valid only for the current thread.
     *
     * @param categoryClass the class containing category methods
     */
    private static void use(Class categoryClass) {
        Map properties = getProperties();
        List stack = (List) LOCAL.get();
        LinkedList clonedLists = new LinkedList();
        
        Method[] methods = categoryClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (Modifier.isStatic(method.getModifiers())) {
                final CachedMethod cachedMethod = CachedMethod.find(method);
                CachedClass[] paramTypes = cachedMethod.getParameterTypes();
                if (paramTypes.length > 0) {
                    CachedClass metaClass = paramTypes[0];
                    Map metaMethodsMap = getMetaMethods(properties, metaClass.getTheClass());
                    List methodList = getMethodList(metaMethodsMap, method.getName());
                    MetaMethod mmethod = new CategoryMethod(cachedMethod, metaClass.getTheClass());
                    methodList.add(mmethod);
                    Collections.sort(methodList);
                }
            }
        }
    }

    private static final ThreadLocal LOCAL = new ThreadLocal() {
        protected Object initialValue() {
        		List stack = new ArrayList();
        		stack.add(Collections.EMPTY_MAP);
        		return stack;
        	}
    };

    private static synchronized void newScope() {
        categoriesInUse++;
        List stack = (List) LOCAL.get();
        Map properties = (Map) stack.get(stack.size() - 1);
        Map newMap = new WeakHashMap(properties.size());
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            newMap.put(entry.getKey(), copyMapOfList((Map) entry.getValue()));
        }        
        stack.add(newMap);
    }

    private static synchronized void endScope() {
        List stack = (List) LOCAL.get();
    	stack.remove(stack.size() - 1);
        categoriesInUse--;
    }
    
    private static Map copyMapOfList(Map m) {
        Map ret = new HashMap(m.size());
        for (Iterator iterator = m.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            List l = (List) entry.getValue();
            l = new ArrayList(l);
            ret.put(entry.getKey(), l);
        }
        return ret;
    }
    
    private static Map getProperties() {
        List stack = (List) LOCAL.get();
        return (Map) stack.get(stack.size() - 1);
    }

    public static boolean hasCategoryInAnyThread() {
        return categoriesInUse!=0;
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
