/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
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
package groovy.lang;

import java.lang.reflect.Modifier;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Pilho Kim
 * @version $Revision$
 */
public class MetaBeanProperty extends MetaProperty {

    private MetaMethod getter;
    private MetaMethod setter;
    private MetaFieldProperty field;
    
    public MetaBeanProperty(String name, Class type, MetaMethod getter, MetaMethod setter) {
        super(name, type);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Get the property of the given object.
     *
     * @param object which to be got
     * @return the property of the given object
     * @throws Exception if the property could not be evaluated
     */
    public Object getProperty(Object object) {
        if (getter == null) {
            //TODO: we probably need a WriteOnlyException class
            throw new GroovyRuntimeException("Cannot read write-only property: " + name);
        }
        return getter.invoke(object, MetaClassHelper.EMPTY_ARRAY);
    }

    /**
     * Set the property on the given object to the new value.
     *
     * @param object   on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    public void setProperty(Object object, Object newValue) {
        if (setter == null) {
            throw new GroovyRuntimeException("Cannot set read-only property: " + name);
        }
        newValue = DefaultTypeTransformation.castToType(newValue, getType());
        setter.invoke(object, new Object[] { newValue });
    }

    /**
     * Get the getter method.
     */
    public MetaMethod getGetter() {
        return getter;
    }

    /**
     * Get the setter method.
     */
    public MetaMethod getSetter() {
        return setter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for get*() methods.
     */
    void setGetter(MetaMethod getter) {
        this.getter = getter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for set*() methods.
     */
    void setSetter(MetaMethod setter) {
        this.setter = setter;
    }
    
    public int getModifiers() {
        if (setter!=null && getter==null) return setter.getModifiers();
        if (getter!=null && setter==null) return getter.getModifiers();
        int modifiers = getter.getModifiers() | setter.getModifiers();
        int visibility = 0;
        if (Modifier.isPublic(modifiers)) visibility = Modifier.PUBLIC;
        if (Modifier.isProtected(modifiers)) visibility = Modifier.PROTECTED;
        if (Modifier.isPrivate(modifiers)) visibility = Modifier.PRIVATE;
        int states = getter.getModifiers() & setter.getModifiers();
        states &= ~(Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE);
        states |= visibility;
        return states;       
    }
    
    public void setField(MetaFieldProperty f) {
        this.field = f;
    }
    
    public MetaFieldProperty getField() {
        return field;
    }
}
