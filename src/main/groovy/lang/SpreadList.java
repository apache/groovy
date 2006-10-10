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

import java.util.AbstractList;
import java.util.List;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Spreads a list as individual objects to support the spread operator (*) for lists.
 * For examples, <pre>
 *     def fn(a, b, c, d) { return a + b + c + d }
 *     println fn(1, 2, 3, 4)
 * 
 *     def x = [10, 100]
 *     def y = [1, *x, 1000, *[10000, 100000]]
 *     assert y == [1, 10, 100, 1000, 10000, 100000]
 * </pre><br>
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Pilho Kim
 * @version $Revision$
 */
public class SpreadList extends AbstractList {

    private Object[] contents;
    private int hashCode;

    /**
     * Generator.
     *
     * @param contents an array of objects to be converted to a SpreadList
     */
    public SpreadList(Object[] contents) {
        this.contents = contents;
    }

    /**
     * Returns the object in <code>this</code> of the indicated position.
     *
     * @param index the indicated position in <code>this</code>
     */
    public Object get(int index) {
        return contents[index];
    }

    /**
     * Returns the size of <code>this</code>.
     */
    public int size() {
        return contents.length;
    }

    /**
     * Compares <code>this</code> with another object.
     *
     * @param that another object to be compared with <code>this</code>
     * @return Returns <code>true</code> if this equals to <code>that</code>, <code>false</code> otherwise
     */
    public boolean equals(Object that) {
        if (that instanceof SpreadList) {
            return equals((SpreadList) that);
        }
        return false;
    }

    /**
     * Compares <code>this</code> with another spreadlist.
     *
     * @param that another spreadlist to be compared with <code>this</code>
     * @return Returns <code>true</code> if this equals to <code>that</code>, <code>false</code> otherwise
     */
    public boolean equals(SpreadList that) {
        if (contents.length == that.contents.length) {
            for (int i = 0; i < contents.length; i++) {
                if (! DefaultTypeTransformation.compareEqual(this.contents[i], that.contents[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Returns the hash code of <code>this</code>.
     *
     * @return Returns the hash code of <code>this</code>
     */
    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < contents.length; i++ ) {
                Object value = contents[i];
                int hash = (value != null) ? value.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
            if (hashCode == 0) {
                hashCode = 0xbabe;
            }
        }
        return hashCode;
    }

    /**
     * Returns a sublist of <code>this</code> from <code>fromIndex</code> to <code>toIndex</code>.
     *
     * @param fromIndex the first index in <code>this</code> to be taken
     * @param toIndex the last index in <code>this</code> to be taken
     * @return Returns the sublist of <code>thin</code> in the given scope
     */
    public List subList(int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        Object[] newContent = new Object[size];
        System.arraycopy(contents, fromIndex, newContent, 0, size);
        return new SpreadList(newContent);
    }

    /**
     * Returns the string expression of <code>this</code>.
     *
     * @return Returns the string expression of <code>this</code>
     */
    public String toString() {
        return "*" + super.toString();
    }
}
