/*
 $Id: GroovyResultSet.java 4831 2007-01-12 13:59:29Z tug $

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

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * GroovyResultSetExtension implements additional logic for ResultSet. Due to 
 * the version incompatibility between java6 and java5 this methods are moved 
 * here from the original GroovyResultSet class. The methods in this class are
 * used by the proxy GroovyResultSetProxy, which will try to invoke methods
 * on this class before invokeing it on ResultSet. 
 * 
 * <p><b>This class is not intended to be used directly. Should be used through
 *  GroovyResultSetProxy only!</b></p>
 * 
 * @see GroovyResultSet
 * @see GroovyResultSetProxy
 *  
 * @author Jochen Theodorou
 */
public class GroovyResultSetExtension {

    private boolean updated;
    private ResultSet _resultSet;
    
    /**
     * Gets the current result set.
     * @return the result set
     * @throws SQLException if the result set can not be returned
     */
    protected ResultSet getResultSet() throws SQLException{
        return _resultSet;
    }
    
    /**
     * Creats a GroovyResultSet implementation-
     *  
     * @param set the result set 
     */
    public GroovyResultSetExtension(ResultSet set) {
        updated = false;
        _resultSet = set;
    }

    /**
     * Gets the value of the designated column in the current row 
     * of as an <code>Object</code>.
     * @param columnName the SQL name of the column
     * @throws MissingPropertyException 
     *   if an SQLException happens while getting the object
     * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
     * @see ResultSet#getObject(java.lang.String)
     */
    public Object getProperty(String columnName) {
        try {
            return getResultSet().getObject(columnName);
        }
        catch (SQLException e) {
            throw new MissingPropertyException(columnName, GroovyResultSetProxy.class, e);
        }
    }


    /**
     * Updates the designated column with an <code>Object</code> value.
     * @param columnName the SQL name of the column
     * @throws MissingPropertyException 
     *   if an SQLException happens while getting the object 
     * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
     * @see ResultSet#updateObject(java.lang.String, java.lang.Object)
     */
    public void setProperty(String columnName, Object newValue) {
        try {
            getResultSet().updateObject(columnName, newValue);
            updated = true;
        }
        catch (SQLException e) {
            throw new MissingPropertyException(columnName, GroovyResultSetProxy.class, e);
        }
    }
    
    /**
     * Supports integer based subscript operators for accessing at numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     * @see ResultSet#getObject(int)
     */
    public Object getAt(int index) throws SQLException {
        index = normalizeIndex(index);
        return getResultSet().getObject(index);
    }
    
    /**
     * Supports integer based subscript operators for updating the values of numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     * @see ResultSet#updateObject(java.lang.String, java.lang.Object)
     */
    public void putAt(int index, Object newValue) throws SQLException {
        index = normalizeIndex(index);
        getResultSet().updateObject(index, newValue);
    }

    /**
     * Adds a new row to the result set
     *
     * @param values a map containing the mappings for column names and values
     * @see ResultSet#insertRow()
     * @see ResultSet#updateObject(java.lang.String, java.lang.Object)
     * @see ResultSet#moveToInsertRow()
     */
    public void add(Map values) throws SQLException {
        getResultSet().moveToInsertRow();
        for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            getResultSet().updateObject(entry.getKey().toString(), entry.getValue());
        }
        getResultSet().insertRow();
    }

    /**
     * Takes a zero based index and convert it into an SQL based 1 based index.
     * A negative index will count backwards from the last column.
     *
     * @param index
     * @return a JDBC index
     * @throws SQLException if some exception occurs finding out the column count
     */
    protected int normalizeIndex(int index) throws SQLException {
        if (index < 0) {
            int columnCount = getResultSet().getMetaData().getColumnCount();
            do {
                index += columnCount;
            }
            while (index < 0);
        }
        return index + 1;
    }


    /**
     * Call the closure once for each row in the result set.
     *
     * @param closure
     * @throws SQLException
     */
    public void eachRow(Closure closure) throws SQLException {
        while (next()) {
            closure.call(this);
        }
    }
    // Implementation of java.sql.getResultSet()
    // ------------------------------------------------------------

    /**
     * Moves the cursor down one row from its current position.
     * A <code>getResultSet()</code> cursor is initially positioned
     * before the first row; the first call to the method
     * <code>next</code> makes the first row the current row; the
     * second call makes the second row the current row, and so on.
     * <p/>
     * <P>If an input stream is open for the current row, a call
     * to the method <code>next</code> will
     * implicitly close it. A <code>getResultSet()</code> object's
     * warning chain is cleared when a new row is read.
     *
     * @return <code>true</code> if the new current row is valid;
     *         <code>false</code> if there are no more rows
     * @throws SQLException if a database access error occurs
     */
    public boolean next() throws SQLException {
        if (updated) {
            getResultSet().updateRow();
            updated = false;
        }
        return getResultSet().next();
    }
    
    /**
     * Moves the cursor to the previous row in this
     * <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     *         <code>false</code> if it is off the result set
     * @throws SQLException if a database access error
     *                      occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public boolean previous() throws SQLException {
        if (updated) {
            getResultSet().updateRow();
            updated = false;
        }
        return getResultSet().previous();
    }



}
