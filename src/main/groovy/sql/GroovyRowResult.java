/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package groovy.sql;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Represents an extent of objects.
 * It's used in the oneRow method to be able to access the result
 * of a SQL query by the name of the column, or by the column number.
 *
 * @version $Revision$
 * @Author Jean-Louis Berliet
 */
public class GroovyRowResult extends GroovyObjectSupport {

    private LinkedHashMap result;

    public GroovyRowResult(LinkedHashMap result) {
        this.result = result;
    }

    /**
     * Retrieve the value of the property by its name    *
     *
     * @param property is the name of the property to look at
     * @return the value of the property
     */
    public Object getProperty(String property) {
        try {
            Object value = result.get(property);
            if (value != null)
                return value;
            // if property exists and value is null, return null
            if (result.containsKey(property))
                return null;
            // with some databases/drivers, the columns names are stored uppercase.
            String propertyUpper = property.toUpperCase();
            value = result.get(propertyUpper);
            if (value != null)
                return value;
            // if property exists and value is null, return null
            if (result.containsKey(propertyUpper)) 
                return null;
            throw new MissingPropertyException(property, GroovyRowResult.class);
        }
        catch (Exception e) {
            throw new MissingPropertyException(property, GroovyRowResult.class, e);
        }
    }

    /**
     * Retrieve the value of the property by its index.
     * A negative index will count backwards from the last column.
     *
     * @param index is the number of the column to look at
     * @return the value of the property
     */
    public Object getAt(int index) {
        try {
            // a negative index will count backwards from the last column.
            if (index < 0)
                index += result.size();
            Iterator it = result.values().iterator();
            int i = 0;
            Object obj = null;
            while ((obj == null) && (it.hasNext())) {
                if (i == index)
                    obj = it.next();
                else
                    it.next();
                i++;
            }
            return (obj);
        }
        catch (Exception e) {
            throw new MissingPropertyException(Integer.toString(index), GroovyRowResult.class, e);
        }
    }

    public String toString() {
        return (result.toString());
    }
}
