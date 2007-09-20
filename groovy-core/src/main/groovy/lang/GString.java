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
package groovy.lang;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents a String which contains embedded values such as "hello there
 * ${user} how are you?" which can be evaluated lazily. Advanced users can
 * iterate over the text and values to perform special processing, such as for
 * performing SQL operations, the values can be substituted for ? and the
 * actual value objects can be bound to a JDBC statement. The lovely name of
 * this class was suggested by Jules Gosnell and was such a good idea, I
 * couldn't resist :)
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class GString extends GroovyObjectSupport implements Comparable, CharSequence, Writable, Buildable {

    private Object[] values;

    public GString(Object values) {
        this.values = (Object[]) values;
    }

    public GString(Object[] values) {
        this.values = values;
    }

    // will be static in an instance
    public abstract String[] getStrings();

    /**
     * Overloaded to implement duck typing for Strings 
     * so that any method that can't be evaluated on this
     * object will be forwarded to the toString() object instead.
     */
    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        }
        catch (MissingMethodException e) {
            // lets try invoke the method on the real String
            return InvokerHelper.invokeMethod(toString(), name, args);
        }
    }

    public Object[] getValues() {
        return values;
    }

    public GString plus(GString that) {
        List stringList = new ArrayList();
        List valueList = new ArrayList();

        stringList.addAll(Arrays.asList(getStrings()));
        valueList.addAll(Arrays.asList(getValues()));

        if (stringList.size() > valueList.size()) {
            valueList.add("");
        }

        stringList.addAll(Arrays.asList(that.getStrings()));
        valueList.addAll(Arrays.asList(that.getValues()));

        final String[] newStrings = new String[stringList.size()];
        stringList.toArray(newStrings);
        Object[] newValues = valueList.toArray();

        return new GString(newValues) {
            public String[] getStrings() {
                return newStrings;
            }
        };
    }

    public GString plus(String that) {
        String[] currentStrings = getStrings();
        String[] newStrings = null;
        Object[] newValues = null;

        newStrings = new String[currentStrings.length + 1];
        newValues = new Object[getValues().length + 1];
        int lastIndex = currentStrings.length;
        System.arraycopy(currentStrings, 0, newStrings, 0, lastIndex);
        System.arraycopy(getValues(), 0, newValues, 0, getValues().length);
        newStrings[lastIndex] = that;
        newValues[getValues().length] = "";

        final String[] finalStrings = newStrings;
        return new GString(newValues) {

            public String[] getStrings() {
                return finalStrings;
            }
        };
    }

    public int getValueCount() {
        return values.length;
    }

    public Object getValue(int idx) {
        return values[idx];
    }

    public String toString() {
        StringWriter buffer = new StringWriter();
        try {
            writeTo(buffer);
        }
        catch (IOException e) {
            throw new StringWriterIOException(e);
        }
        return buffer.toString();
    }

    public Writer writeTo(Writer out) throws IOException {
    	String[] s = getStrings();
    	int numberOfValues = values.length;
    	for (int i = 0, size = s.length; i < size; i++) {
    		out.write(s[i]);
    		if (i < numberOfValues) {
    			final Object value = values[i];

    			if (value instanceof Closure) {
    				final Closure c = (Closure)value;

    				if (c.getMaximumNumberOfParameters() == 0) {
    					InvokerHelper.write(out, c.call(null));
    				} else if (c.getMaximumNumberOfParameters() == 1) {
    					c.call(new Object[]{out});
    				} else {
    					throw new GroovyRuntimeException("Trying to evaluate a GString containing a Closure taking "
    							+ c.getMaximumNumberOfParameters() + " parameters");
    				}
    			} else {
    				InvokerHelper.write(out, value);
    			}
    		}
    	}
    	return out;
    }

    /* (non-Javadoc)
     * @see groovy.lang.Buildable#build(groovy.lang.GroovyObject)
     */
    public void build(final GroovyObject builder) {
    final String[] s = getStrings();
    final int numberOfValues = values.length;
        
        for (int i = 0, size = s.length; i < size; i++) {
            builder.getProperty("mkp");
            builder.invokeMethod("yield", new Object[]{s[i]});
            if (i < numberOfValues) {
                builder.getProperty("mkp");
                builder.invokeMethod("yield", new Object[]{values[i]});
            }
        }
    }

    public boolean equals(Object that) {
        if (that instanceof GString) {
            return equals((GString) that);
        }
        return false;
    }

    public boolean equals(GString that) {
        return toString().equals(that.toString());
    }

    public int hashCode() {
        return 37 + toString().hashCode();
    }

    public int compareTo(Object that) {
        return toString().compareTo(that.toString());
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public int length() {
        return toString().length();
    }

    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Turns a String into a regular expression pattern
     *
     * @return the regular expression pattern
     */
    public Pattern negate() {
        return DefaultGroovyMethods.negate(toString());
    }
}
