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
package groovy.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents a sequence of objects which represents zero or many instances of
 * of objects of a given type. The type can be ommitted in which case any type of
 * object can be added.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Sequence extends ArrayList implements GroovyObject {

    private MetaClass metaClass = InvokerHelper.getMetaClass(this);
    private Class type;
    private int hashCode;

    public Sequence() {
        this(null);
    }

    public Sequence(Class type) {
        this.type = type;
    }

    public Sequence(Class type, List content) {
        super(content.size());
        this.type = type;
        addAll(content);
    }

    /**
     * Sets the contents of this sequence to that
     * of the given collection.
     */
    public void set(Collection collection) {
        checkCollectionType(collection);
        clear();
        addAll(collection);
    }
    
    public boolean equals(Object that) {
        if (that instanceof Sequence) {
            return equals((Sequence) that);
        }
        return false;
    }

    public boolean equals(Sequence that) {
        if (size() == that.size()) {
            for (int i = 0; i < size(); i++) {
                if (!InvokerHelper.compareEqual(this.get(i), that.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < size(); i++) {
                Object value = get(i);
                int hash = (value != null) ? value.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
            if (hashCode == 0) {
                hashCode = 0xbabe;
            }
        }
        return hashCode;
    }

    public int minimumSize() {
        return 0;
    }

    /**
     * @return the type of the elements in the sequence or null if there is no
     * type constraint on this sequence
     */
    public Class type() {
        return type;
    }
    
    public void add(int index, Object element) {
        checkType(element);
        hashCode = 0;
        super.add(index, element);
    }

    public boolean add(Object element) {
        checkType(element);
        hashCode = 0;
        return super.add(element);
    }

    public boolean addAll(Collection c) {
        checkCollectionType(c);
        hashCode = 0;
        return super.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        checkCollectionType(c);
        hashCode = 0;
        return super.addAll(index, c);
    }

    public void clear() {
        hashCode = 0;
        super.clear();
    }

    public Object remove(int index) {
        hashCode = 0;
        return super.remove(index);
    }

    protected void removeRange(int fromIndex, int toIndex) {
        hashCode = 0;
        super.removeRange(fromIndex, toIndex);
    }

    public Object set(int index, Object element) {
        hashCode = 0;
        return super.set(index, element);
    }

    // GroovyObject interface
    //-------------------------------------------------------------------------
    public Object invokeMethod(String name, Object args) {
        try {
        return getMetaClass().invokeMethod(this, name, args);
        }
        catch (MissingMethodException e) {
            // lets apply the method to each item in the collection
            List answer = new ArrayList(size());
            for (Iterator iter = iterator(); iter.hasNext(); ) {
                Object element = iter.next();
                Object value = InvokerHelper.invokeMethod(element, name, args);
                answer.add(value);
            }
            return answer;
        }
    }

    public Object getProperty(String property) {
        return getMetaClass().getProperty(this, property);
    }

    public void setProperty(String property, Object newValue) {
        getMetaClass().setProperty(this, property, newValue);
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    
    /**
     * Checks that each member of the given collection are of the correct
     * type
     */
    protected void checkCollectionType(Collection c) {
        if (type != null) {
            for (Iterator iter = c.iterator(); iter.hasNext(); ) {
                Object element = iter.next();
                checkType(element);
            }
        }
    }


    /** 
     * Checks that the given object instance is of the correct type
     * otherwise a runtime exception is thrown
     */
    protected void checkType(Object object) {
        if (object == null) {
            throw new NullPointerException("Sequences cannot contain null, use a List instead");
        }
        if (type != null) {
            if (!type.isInstance(object)) {
                throw new IllegalArgumentException(
                    "Invalid type of argument for sequence of type: "
                        + type.getName()
                        + " cannot add object: "
                        + object);
            }
        }
    }
}