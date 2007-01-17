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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;


/**
 * Represents an extent of objects
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:ivan_ganza@yahoo.com">Ivan Ganza</a>
 * @version $Revision$
 * @Author Chris Stevenson
 */
public interface GroovyResultSet extends GroovyObject, ResultSet {
    /**
     * Supports integer based subscript operators for accessing at numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     */
    public Object getAt(int index) throws SQLException;
    
    /**
     * Supports integer based subscript operators for updating the values of numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     */
    public void putAt(int index, Object newValue) throws SQLException;

    /**
     * Adds a new row to this result set
     *
     * @param values
     */
    public void add(Map values) throws SQLException;

    /**
     * Call the closure once for each row in the result set.
     *
     * @param closure
     * @throws SQLException
     */
    public void eachRow(Closure closure) throws SQLException;
    
}
