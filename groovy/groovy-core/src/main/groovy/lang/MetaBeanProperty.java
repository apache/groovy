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

/**
 * Represents a property on a bean which may have a getter and/or a setter
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Kim Pilho
 * @version $Revision$
 */
public class MetaBeanProperty extends MetaProperty {

    private MetaMethod getter;
    private MetaMethod setter;

    public MetaBeanProperty(String name, Class type, MetaMethod getter, MetaMethod setter) {
		super(name, type);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * @return the property of the given object
     * @throws Exception if the property could not be evaluated
     */
    public Object getProperty(Object object) throws Exception {
		if (getter == null) {
			//@todo we probably need a WriteOnlyException class
			throw new GroovyRuntimeException("Cannot read write-only property: " + name);
		}
        return getter.invoke(object, MetaClass.EMPTY_ARRAY);
    }

    /**
     * Sets the property on the given object to the new value
     * 
     * @param object on which to set the property
     * @param newValue the new value of the property
     * @throws Exception if the property could not be set
     */
    public void setProperty(Object object, Object newValue) {
		if(setter == null) {
			throw new GroovyRuntimeException("Cannot set read-only property: " + name);
		}

		try {
			// we'll convert a GString to String if needed
			if(getType() == String.class && !(newValue instanceof String))
				newValue = newValue.toString();

			// Set property for primitive types
			if (newValue instanceof java.math.BigDecimal) {
				if (getType() == Double.class)
					newValue = new Double(((java.math.BigDecimal) newValue).doubleValue());
				else if (getType() == Float.class)
					newValue = new Float(((java.math.BigDecimal) newValue).floatValue());
				else if (getType() == Long.class)
					newValue = new Long(((java.math.BigDecimal) newValue).longValue());
				else if (getType() == Integer.class)
					newValue = new Integer(((java.math.BigDecimal) newValue).intValue());
				else if (getType() == Short.class)
					newValue = new Short((short) ((java.math.BigDecimal) newValue).intValue());
				else if (getType() == Byte.class)
					newValue = new Byte((byte) ((java.math.BigDecimal) newValue).intValue());
				else if (getType() == Character.class)
					newValue = new Character((char) ((java.math.BigDecimal) newValue).intValue());
			}
			else if (newValue instanceof java.math.BigInteger) {
				if (getType() == Long.class)
					newValue = new Long(((java.math.BigInteger) newValue).longValue());
				else if (getType() == Integer.class)
					newValue = new Integer(((java.math.BigInteger) newValue).intValue());
				else if (getType() == Short.class)
					newValue = new Short((short) ((java.math.BigInteger) newValue).intValue());
				else if (getType() == Byte.class)
					newValue = new Byte((byte) ((java.math.BigInteger) newValue).intValue());
				else if (getType() == Character.class)
					newValue = new Character((char) ((java.math.BigInteger) newValue).intValue());
			}
			else if (newValue instanceof java.lang.Long) {
				if (getType() == Integer.class)
					newValue = new Integer(((Long) newValue).intValue());
				else if (getType() == Short.class)
					newValue = new  Short(((Long) newValue).shortValue());
				else if (getType() == Byte.class)
					newValue = new Byte(((Long) newValue).byteValue());
				else if (getType() == Character.class)
					newValue = new Character((char) ((Long) newValue).intValue());
			}
			else if (newValue instanceof java.lang.Integer) {
				if (getType() == Short.class)
					newValue = new  Short(((Integer) newValue).shortValue());
				else if (getType() == Byte.class)
					newValue = new Byte(((Integer) newValue).byteValue());
				else if (getType() == Character.class)
					newValue = new Character((char) ((Integer) newValue).intValue());
			}

			setter.invoke(object, new Object[] { newValue });
		}
		catch(Exception e) {
			throw new GroovyRuntimeException("Cannot set property: " + name +
				" reason: " + e.getMessage(), e);
		}
    }

    public MetaMethod getGetter() {
        return getter;
    }

    public MetaMethod getSetter() {
        return setter;
    }
	
	/**
	 * this is for MetaClass to patch up the object later when looking for get*() methods
	 */
	void setGetter(MetaMethod getter) {
		this.getter = getter;
	}
	
	/**
	 * this is for MetaClass to patch up the object later when looking for set*() methods
	 */
	void setSetter(MetaMethod setter) {
		this.setter = setter;
	}
}
