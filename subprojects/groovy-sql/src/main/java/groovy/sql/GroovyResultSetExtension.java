/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * GroovyResultSetExtension implements additional logic for ResultSet. Due to
 * the version incompatibility between java6 and java5 this methods are moved
 * here from the original GroovyResultSet class. The methods in this class are
 * used by the proxy GroovyResultSetProxy, which will try to invoke methods
 * on this class before invoking it on ResultSet.
 * <p>
 * <b>This class is not intended to be used directly. Should be used through
 * GroovyResultSetProxy only!</b>
 *
 * @see GroovyResultSet
 * @see GroovyResultSetProxy
 */
public class GroovyResultSetExtension extends GroovyObjectSupport {

    private boolean updated;
    private final ResultSet resultSet;

    /**
     * Gets the current result set.
     *
     * @return the result set
     * @throws SQLException if the result set can not be returned
     */
    protected ResultSet getResultSet() throws SQLException {
        return resultSet;
    }

    /**
     * Creates a GroovyResultSet implementation.
     *
     * @param set the result set
     */
    public GroovyResultSetExtension(ResultSet set) {
        updated = false;
        resultSet = set;
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder("[");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                sb.append(metaData.getColumnName(i));
                sb.append(":");
                Object object = resultSet.getObject(i);
                if (object!=null) {
                    sb.append(object.toString());
                } else {
                    sb.append("[null]");
                }
                if (i < count) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        } catch (SQLException e) {
//            System.err.println("e.getMessage() = " + e.getMessage());
            return super.toString();
        }
    }

    public Object invokeMethod(String name, Object args) {
        try {
            return InvokerHelper.invokeMethod(getResultSet(), name, args);
        } catch (SQLException se) {
            throw new InvokerInvocationException(se);
        }
    }

    /**
     * Gets the value of the designated column in the current row
     * of as an <code>Object</code>.
     *
     * @param columnName the SQL name of the column
     * @return the returned column value
     * @throws MissingPropertyException if an SQLException happens while getting the object
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
     *
     * @param columnName the SQL name of the column
     * @param newValue   the updated value
     * @throws MissingPropertyException if an SQLException happens while setting the new value
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
     * @return the returned column value
     * @throws java.sql.SQLException if something goes wrong
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
     * @param index    is the number of the column to look at starting at 1
     * @param newValue the updated value
     * @throws java.sql.SQLException if something goes wrong
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
     * @throws java.sql.SQLException if something goes wrong
     * @see ResultSet#insertRow()
     * @see ResultSet#updateObject(java.lang.String, java.lang.Object)
     * @see ResultSet#moveToInsertRow()
     */
    public void add(Map values) throws SQLException {
        getResultSet().moveToInsertRow();
        for (Object o : values.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            getResultSet().updateObject(entry.getKey().toString(), entry.getValue());
        }
        getResultSet().insertRow();
    }

    /**
     * Takes a zero based index and convert it into an SQL based 1 based index.
     * A negative index will count backwards from the last column.
     *
     * @param index the raw requested index (may be negative)
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
     * @param closure the closure to perform on each row
     * @throws SQLException if something goes wrong
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
     * <p>
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
