/* Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.commons;


import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * @author Graeme Rocher
 * @since 08-Jul-2005
 * 
 * Class containing utility methods for dealing with Grails class artifacts
 * 
 */
public class GrailsClassUtils {

    private static Map beanWrapperInstances = new HashMap();

    /**
     * Returns true of the specified Groovy class is a bootstrap
     * @param clazz
     * @return True if the class is a bootstrap class
     */
    public static boolean isBootstrapClass( Class clazz ) {
        return clazz.getName().endsWith(DefaultGrailsBootstrapClass.BOOT_STRAP)  && !Closure.class.isAssignableFrom(clazz);
    }

    /**
     * Returns true of the specified Groovy class is a taglib
     * @param clazz
     * @return True if the class is a taglib
     */
    public static boolean isTagLibClass( Class clazz ) {
        return isTagLibClass(clazz.getName())  && !Closure.class.isAssignableFrom(clazz);
    }

    public static boolean isTagLibClass(String className) {
        return className.endsWith( DefaultGrailsTagLibClass.TAG_LIB );
    }
    /**
     * Returns true of the specified Groovy class is a controller
     * @param clazz
     * @return True if the class is a controller
     */
    public static boolean isControllerClass( Class clazz ) {
        return isControllerClass(clazz.getName())  && !Closure.class.isAssignableFrom(clazz);
    }

    public static boolean isControllerClass(String className) {
        return className.endsWith(DefaultGrailsControllerClass.CONTROLLER);
    }

    /**
     * <p>Returns true if the specified class is a page flow class type</p>
     *
     * @param clazz
     * @return  True if the class is a page flow class
     */
    public static boolean isPageFlowClass( Class clazz ) {
        return isPageFlowClass(clazz.getName())  && !Closure.class.isAssignableFrom(clazz);
    }

    public static boolean isPageFlowClass(String className) {
        return className.endsWith(DefaultGrailsPageFlowClass.PAGE_FLOW);
    }

    /**
     * <p>Returns true if the specified class is a data source.
     *
     * @param clazz
     * @return True if the class is a data source
     */
    public static boolean isDataSource(Class clazz) {
        return clazz.getName().endsWith(DefaultGrailsDataSource.DATA_SOURCE) && !Closure.class.isAssignableFrom(clazz);
    }

    /**
     * <p>Returns true if the specified class is a service.
     *
     * @param clazz
     * @return True if the class is a service class
     */
    public static boolean isService(Class clazz) {
        return isService(clazz.getName()) && !Closure.class.isAssignableFrom(clazz);
    }
    public static boolean isService(String className) {
        return className.endsWith(DefaultGrailsServiceClass.SERVICE);
    }

    /**
     * <p>Returns true if the specified class is a domain class. In Grails a domain class
     * is any class that has "id" and "version" properties</p>
     *
     * @param clazz The class to check
     * @return A boolean value
     */
    public static boolean isDomainClass( Class clazz ) {
        try {
            // make sure the identify and version field exist
            clazz.getDeclaredField( GrailsDomainClassProperty.IDENTITY );
            clazz.getDeclaredField( GrailsDomainClassProperty.VERSION );
            // and its not a closure
            if(Closure.class.isAssignableFrom(clazz)) {
                return false;
            }
            // passes all conditions return true
            return true;
        } catch (SecurityException e) {
            return false;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     *
     * Returns true if the specified property in the specified class is of the specified type
     *
     * @param clazz The class which contains the property
     * @param propertyName The property name
     * @param type The type to check
     *
     * @return A boolean value
     */
    public static boolean isPropertyOfType( Class clazz, String propertyName, Class type ) {
        try {

            Class propType = getProperyType( clazz, propertyName );
            if(propType != null && propType.equals( type ))
                return true;
            else
                return false;
        }
        catch(Exception e) {
            return false;
        }
    }


    /**
     * Returns the value of the specified property and type from an instance of the specified Grails class
     *
     * @param clazz The name of the class which contains the property
     * @param propertyName The property name
     * @param propertyType The property type
     *
     * @return The value of the property or null if none exists
     */
    public static Object getPropertyValue(Class clazz, String propertyName, Class propertyType) {
        // validate
        if(clazz == null || StringUtils.isBlank(propertyName))
            return null;

        try {
            BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
            if(wrapper == null) {
                wrapper = new BeanWrapperImpl(clazz.newInstance());
                beanWrapperInstances.put( clazz.getName(), wrapper );
            }
            Object pValue = wrapper.getPropertyValue( propertyName );
            if(pValue == null)
                return null;

            if(propertyType.isAssignableFrom(pValue.getClass())) {
                return pValue;
            }
            else {
                return null;
            }

        } catch (Exception e) {
            // if there are any errors in instantiating just return null
            return null;
        }
    }


    /**
     * Retrieves a PropertyDescriptor for the specified instance and property value
     *
     * @param instance The instance
     * @param propertyValue The value of the property
     * @return The PropertyDescriptor
     */
    public static PropertyDescriptor getPropertyDescriptorForValue(Object instance, Object propertyValue) {
        if(instance == null || propertyValue == null)
            return null;

        BeanWrapper wrapper = new BeanWrapperImpl(instance);
        PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();

        for (int i = 0; i < descriptors.length; i++) {
            Object value = wrapper.getPropertyValue( descriptors[i].getName() );
            if(propertyValue.equals(value))
                return descriptors[i];
        }
        return null;
    }
    /**
     * Returns the type of the given property contained within the specified class
     *
     * @param clazz The class which contains the property
     * @param propertyName The name of the property
     *
     * @return The property type or null if none exists
     */
    public static Class getProperyType(Class clazz, String propertyName) {
        if(clazz == null || StringUtils.isBlank(propertyName))
            return null;

        try {
            BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
            if(wrapper == null) {
                wrapper = new BeanWrapperImpl(clazz.newInstance());
                beanWrapperInstances.put( clazz.getName(), wrapper );
            }
            return wrapper.getPropertyType(propertyName);

        } catch (Exception e) {
            // if there are any errors in instantiating just return null for the moment
            return null;
        }
    }

    /**
     * Retrieves all the properties of the given class for the given type
     *
     * @param clazz The class to retrieve the properties from
     * @param propertyType The type of the properties you wish to retrieve
     *
     * @return An array of PropertyDescriptor instances
     */
    public static PropertyDescriptor[] getPropertiesOfType(Class clazz, Class propertyType) {
        if(clazz == null || propertyType == null)
            return new PropertyDescriptor[0];

        Set properties = new HashSet();
        try {
            BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
            if(wrapper == null) {
                wrapper = new BeanWrapperImpl(clazz.newInstance());
                beanWrapperInstances.put( clazz.getName(), wrapper );
            }
            PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();

            for (int i = 0; i < descriptors.length; i++) {
                if(descriptors[i].getPropertyType().equals( propertyType )  ) {
                    properties.add(descriptors[i]);
                }
            }

        } catch (Exception e) {
            // if there are any errors in instantiating just return null for the moment
            return new PropertyDescriptor[0];
        }
        return (PropertyDescriptor[])properties.toArray( new PropertyDescriptor[ properties.size() ] );
    }

    /**
     * Retrieves a property of the given class of the specified name and type
     * @param clazz The class to retrieve the property from
     * @param propertyName The name of the property
     * @param propertyType The type of the property
     *
     * @return A PropertyDescriptor instance or null if none exists
     */
    public static PropertyDescriptor getProperty(Class clazz, String propertyName, Class propertyType) {
        if(clazz == null || propertyName == null || propertyType == null)
            return null;

        try {
            BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
            if(wrapper == null) {
                wrapper = new BeanWrapperImpl(clazz.newInstance());
                beanWrapperInstances.put( clazz.getName(), wrapper );
            }
            PropertyDescriptor pd = wrapper.getPropertyDescriptor(propertyName);
            if(pd.getPropertyType().equals( propertyType )) {
                return pd;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            // if there are any errors in instantiating just return null for the moment
            return null;
        }
    }

    /**
     * Returns the class name without the package prefix
     *
     * @param targetClass
     * @return The short name of the class
     */
    public static String getShortName(Class targetClass) {
        String className = targetClass.getName();
        int i = className.lastIndexOf(".");
        if(i > -1) {
            className = className.substring( i + 1, className.length() );
        }
        return className;
    }

    /**
     * Returns the property name equivalent for the specified class
     *
     * @param targetClass The class to get the property name for
     * @return A property name reperesentation of the class name (eg. MyClass becomes myClass)
     */
    public static String getPropertyNameRepresentation(Class targetClass) {
        String shortName = getShortName(targetClass);
        return getPropertyNameRepresentation(shortName);
    }

    /**
     * Returns the property name representation of the given name
     *
     * @param name The name to convert
     * @return The property name representation
     */
    public static String getPropertyNameRepresentation(String name) {
        String propertyName = name.substring(0,1).toLowerCase() + name.substring(1);
        if(propertyName.contains(" ")) {
            propertyName = propertyName.replaceAll("\\s", "");
        }
        return propertyName;
    }

    /**
     * Converts a property name into its natural language equivalent eg ('firstName' becomes 'First Name')
     * @param name The property name to convert
     * @return The converted property name
     */
    public static String getNaturalName(String name) {
        List words = new ArrayList();
        int i = 0;
        char[] chars = name.toCharArray();
        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            String w;
            if(i >= words.size()) {
                w = "";
                words.add(i, w);
            }
            else {
                w = (String)words.get(i);
            }

            if(Character.isLowerCase(c) || Character.isDigit(c)) {
                if(Character.isLowerCase(c) && w.length() == 0)
                    c = Character.toUpperCase(c);
                else if(w.length() > 1 && Character.isUpperCase(w.charAt(w.length() - 1)) ) {
                    w = "";
                    words.add(++i,w);
                }

                words.set(i, w + c);
            }
            else if(Character.isUpperCase(c)) {
                if((i == 0 && w.length() == 0) || Character.isUpperCase(w.charAt(w.length() - 1)) ) 	{
                    words.set(i, w + c);
                }
                else {
                    words.add(++i, String.valueOf(c));
                }
            }

        }

        StringBuffer buf = new StringBuffer();
        
        for (Iterator j = words.iterator(); j.hasNext();) {
            String word = (String) j.next();
            buf.append(word);
            if(j.hasNext())
                buf.append(' ');
        }
        return buf.toString();
    }


}
