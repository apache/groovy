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

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an extent of objects
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:ivan_ganza@yahoo.com">Ivan Ganza</a>
 * @version $Revision$
 * @Author Chris Stevenson
 */
public class GroovyResultSet extends GroovyObjectSupport implements ResultSet {

    private ResultSet _resultSet;
    private boolean updated;


    public GroovyResultSet(ResultSet resultSet) {
        this._resultSet = resultSet;
    }

    protected GroovyResultSet() {
    }

    protected ResultSet getResultSet() throws SQLException {
        return _resultSet;
    }

    public Object getProperty(String property) {
        try {
            return getResultSet().getObject(property);
        }
        catch (SQLException e) {
            throw new MissingPropertyException(property, GroovyResultSet.class, e);
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            getResultSet().updateObject(property, newValue);
            updated = true;
        }
        catch (SQLException e) {
            throw new MissingPropertyException(property, GroovyResultSet.class, e);
        }
    }

    /**
     * Supports integer based subscript operators for accessing at numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     * @return
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
     * @return
     */
    public void putAt(int index, Object newValue) throws SQLException {
        index = normalizeIndex(index);
        getResultSet().updateObject(index, newValue);
    }

    /**
     * Adds a new row to this result set
     *
     * @param values
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
     * Releases this <code>getResultSet()</code> object's database and
     * JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     * <p/>
     * <P><B>Note:</B> A <code>getResultSet()</code> object
     * is automatically closed by the
     * <code>Statement</code> object that generated it when
     * that <code>Statement</code> object is closed,
     * re-executed, or is used to retrieve the next result from a
     * sequence of multiple results. A <code>getResultSet()</code> object
     * is also automatically closed when it is garbage collected.
     *
     * @throws SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        getResultSet().close();
    }

    /**
     * Reports whether
     * the last column read had a value of SQL <code>NULL</code>.
     * Note that you must first call one of the getter methods
     * on a column to try to read its value and then call
     * the method <code>wasNull</code> to see if the value read was
     * SQL <code>NULL</code>.
     *
     * @return <code>true</code> if the last column value read was SQL
     *         <code>NULL</code> and <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {
        return getResultSet().wasNull();
    }

    //======================================================================
    // Methods for accessing results by column index
    //======================================================================

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>String</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public String getString(int columnIndex) throws SQLException {
        return getResultSet().getString(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>boolean</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>false</code>
     * @throws SQLException if a database access error occurs
     */
    public boolean getBoolean(int columnIndex) throws SQLException {
        return getResultSet().getBoolean(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>byte</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public byte getByte(int columnIndex) throws SQLException {
        return getResultSet().getByte(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>short</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public short getShort(int columnIndex) throws SQLException {
        return getResultSet().getShort(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * an <code>int</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public int getInt(int columnIndex) throws SQLException {
        return getResultSet().getInt(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>long</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public long getLong(int columnIndex) throws SQLException {
        return getResultSet().getLong(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>float</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public float getFloat(int columnIndex) throws SQLException {
        return getResultSet().getFloat(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>double</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public double getDouble(int columnIndex) throws SQLException {
        return getResultSet().getDouble(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.BigDecimal</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale       the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return getResultSet().getBigDecimal(columnIndex, scale);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public byte[] getBytes(int columnIndex) throws SQLException {
        return getResultSet().getBytes(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Date</code> object in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        return getResultSet().getDate(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Time</code> object in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return getResultSet().getTime(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Timestamp</code> object in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getResultSet().getTimestamp(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a stream of ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <char>LONGVARCHAR</char> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *         as a stream of one-byte ASCII characters;
     *         if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        return getResultSet().getAsciiStream(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * as a stream of two-byte Unicode characters. The first byte is
     * the high byte; the second byte is the low byte.
     * <p/>
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code>values.  The
     * JDBC driver will do any necessary conversion from the database
     * format into Unicode.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called, whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *         as a stream of two-byte Unicode characters;
     *         if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code>
     * @throws SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> in place of
     *             <code>getUnicodeStream</code>
     */
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return getResultSet().getUnicodeStream(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a binary stream of
     * uninterpreted bytes. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code> values.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *         as a stream of uninterpreted bytes;
     *         if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.io.InputStream getBinaryStream(int columnIndex)
            throws SQLException {

        return getResultSet().getBinaryStream(columnIndex);
    }

    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>String</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public String getString(String columnName) throws SQLException {
        return getResultSet().getString(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>boolean</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>false</code>
     * @throws SQLException if a database access error occurs
     */
    public boolean getBoolean(String columnName) throws SQLException {
        return getResultSet().getBoolean(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>byte</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public byte getByte(String columnName) throws SQLException {
        return getResultSet().getByte(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>short</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public short getShort(String columnName) throws SQLException {
        return getResultSet().getShort(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * an <code>int</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public int getInt(String columnName) throws SQLException {
        return getResultSet().getInt(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>long</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public long getLong(String columnName) throws SQLException {
        return getResultSet().getLong(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>float</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public float getFloat(String columnName) throws SQLException {
        return getResultSet().getFloat(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>double</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>0</code>
     * @throws SQLException if a database access error occurs
     */
    public double getDouble(String columnName) throws SQLException {
        return getResultSet().getDouble(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.math.BigDecimal</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @param scale      the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getResultSet().getBigDecimal(columnName, scale);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public byte[] getBytes(String columnName) throws SQLException {
        return getResultSet().getBytes(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Date</code> object in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Date getDate(String columnName) throws SQLException {
        return getResultSet().getDate(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Time</code> object in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Time getTime(String columnName) throws SQLException {
        return getResultSet().getTime(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * a <code>java.sql.Timestamp</code> object.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         value returned is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        return getResultSet().getTimestamp(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a stream of
     * ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     *         as a stream of one-byte ASCII characters.
     *         If the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code>.
     * @throws SQLException if a database access error occurs
     */
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        return getResultSet().getAsciiStream(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a stream of two-byte
     * Unicode characters. The first byte is the high byte; the second
     * byte is the low byte.
     * <p/>
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC technology-enabled driver will
     * do any necessary conversion from the database format into Unicode.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code> is called, whether there
     * is data available or not.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     *         as a stream of two-byte Unicode characters.
     *         If the value is SQL <code>NULL</code>, the value returned
     *         is <code>null</code>.
     * @throws SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> instead
     */
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        return getResultSet().getUnicodeStream(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a stream of uninterpreted
     * <code>byte</code>s.
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code>
     * values.
     * <p/>
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     *         as a stream of uninterpreted bytes;
     *         if the value is SQL <code>NULL</code>, the result is <code>null</code>
     * @throws SQLException if a database access error occurs
     */
    public java.io.InputStream getBinaryStream(String columnName)
            throws SQLException {

        return getResultSet().getBinaryStream(columnName);
    }

    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * Retrieves the first warning reported by calls on this
     * <code>getResultSet()</code> object.
     * Subsequent warnings on this <code>getResultSet()</code> object
     * will be chained to the <code>SQLWarning</code> object that
     * this method returns.
     * <p/>
     * <P>The warning chain is automatically cleared each time a new
     * row is read.  This method may not be called on a <code>getResultSet()</code>
     * object that has been closed; doing so will cause an
     * <code>SQLException</code> to be thrown.
     * <p/>
     * <B>Note:</B> This warning chain only covers warnings caused
     * by <code>getResultSet()</code> methods.  Any warning caused by
     * <code>Statement</code> methods
     * (such as reading OUT parameters) will be chained on the
     * <code>Statement</code> object.
     *
     * @return the first <code>SQLWarning</code> object reported or
     *         <code>null</code> if there are none
     * @throws SQLException if a database access error occurs or this method is
     *                      called on a closed result set
     */
    public SQLWarning getWarnings() throws SQLException {
        return getResultSet().getWarnings();
    }

    /**
     * Clears all warnings reported on this <code>getResultSet()</code> object.
     * After this method is called, the method <code>getWarnings</code>
     * returns <code>null</code> until a new warning is
     * reported for this <code>getResultSet()</code> object.
     *
     * @throws SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {
        getResultSet().clearWarnings();
    }

    /**
     * Retrieves the name of the SQL cursor used by this <code>getResultSet()</code>
     * object.
     * <p/>
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result set can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's <code>SELECT</code> statement
     * should be of the form <code>SELECT FOR UPDATE</code>. If
     * <code>FOR UPDATE</code> is omitted, the positioned updates may fail.
     * <p/>
     * <P>The JDBC API supports this SQL feature by providing the name of the
     * SQL cursor used by a <code>getResultSet()</code> object.
     * The current row of a <code>getResultSet()</code> object
     * is also the current row of this SQL cursor.
     * <p/>
     * <P><B>Note:</B> If positioned update is not supported, a
     * <code>SQLException</code> is thrown.
     *
     * @return the SQL name for this <code>getResultSet()</code> object's cursor
     * @throws SQLException if a database access error occurs
     */
    public String getCursorName() throws SQLException {
        return getResultSet().getCursorName();
    }

    /**
     * Retrieves the  number, types and properties of
     * this <code>getResultSet()</code> object's columns.
     *
     * @return the description of this <code>getResultSet()</code> object's columns
     * @throws SQLException if a database access error occurs
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return getResultSet().getMetaData();
    }

    /**
     * <p>Gets the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * an <code>Object</code> in the Java programming language.
     * <p/>
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <p/>
     * <p>This method may also be used to read database-specific
     * abstract data types.
     * <p/>
     * In the JDBC 2.0 API, the behavior of method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>java.lang.Object</code> holding the column value
     * @throws SQLException if a database access error occurs
     */
    public Object getObject(int columnIndex) throws SQLException {
        return getResultSet().getObject(columnIndex);
    }

    /**
     * <p>Gets the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as
     * an <code>Object</code> in the Java programming language.
     * <p/>
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <p/>
     * This method may also be used to read database-specific
     * abstract data types.
     * <p/>
     * In the JDBC 2.0 API, the behavior of the method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>.
     *
     * @param columnName the SQL name of the column
     * @return a <code>java.lang.Object</code> holding the column value
     * @throws SQLException if a database access error occurs
     */
    public Object getObject(String columnName) throws SQLException {
        return getResultSet().getObject(columnName);
    }

    //----------------------------------------------------------------

    /**
     * Maps the given <code>getResultSet()</code> column name to its
     * <code>getResultSet()</code> column index.
     *
     * @param columnName the name of the column
     * @return the column index of the given column name
     * @throws SQLException if the <code>getResultSet()</code> object
     *                      does not contain <code>columnName</code> or a database access error occurs
     */
    public int findColumn(String columnName) throws SQLException {
        return getResultSet().findColumn(columnName);
    }

    //--------------------------JDBC 2.0-----------------------------------

    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a
     * <code>java.io.Reader</code> object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>java.io.Reader</code> object that contains the column
     *         value; if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code> in the Java programming language.
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
        return getResultSet().getCharacterStream(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a
     * <code>java.io.Reader</code> object.
     *
     * @param columnName the name of the column
     * @return a <code>java.io.Reader</code> object that contains the column
     *         value; if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        return getResultSet().getCharacterStream(columnName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a
     * <code>java.math.BigDecimal</code> with full precision.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision);
     *         if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code> in the Java programming language.
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return getResultSet().getBigDecimal(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a
     * <code>java.math.BigDecimal</code> with full precision.
     *
     * @param columnName the column name
     * @return the column value (full precision);
     *         if the value is SQL <code>NULL</code>, the value returned is
     *         <code>null</code> in the Java programming language.
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getResultSet().getBigDecimal(columnName);
    }

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * Retrieves whether the cursor is before the first row in
     * this <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is before the first row;
     *         <code>false</code> if the cursor is at any other position or the
     *         result set contains no rows
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public boolean isBeforeFirst() throws SQLException {
        return getResultSet().isBeforeFirst();
    }

    /**
     * Retrieves whether the cursor is after the last row in
     * this <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is after the last row;
     *         <code>false</code> if the cursor is at any other position or the
     *         result set contains no rows
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public boolean isAfterLast() throws SQLException {
        return getResultSet().isAfterLast();
    }

    /**
     * Retrieves whether the cursor is on the first row of
     * this <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is on the first row;
     *         <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public boolean isFirst() throws SQLException {
        return getResultSet().isFirst();
    }

    /**
     * Retrieves whether the cursor is on the last row of
     * this <code>getResultSet()</code> object.
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine
     * whether the current row is the last row in the result set.
     *
     * @return <code>true</code> if the cursor is on the last row;
     *         <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public boolean isLast() throws SQLException {
        return getResultSet().isLast();
    }

    /**
     * Moves the cursor to the front of
     * this <code>getResultSet()</code> object, just before the
     * first row. This method has no effect if the result set contains no rows.
     *
     * @throws SQLException if a database access error
     *                      occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public void beforeFirst() throws SQLException {
        getResultSet().beforeFirst();
    }

    /**
     * Moves the cursor to the end of
     * this <code>getResultSet()</code> object, just after the
     * last row. This method has no effect if the result set contains no rows.
     *
     * @throws SQLException if a database access error
     *                      occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public void afterLast() throws SQLException {
        getResultSet().afterLast();
    }

    /**
     * Moves the cursor to the first row in
     * this <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     *         <code>false</code> if there are no rows in the result set
     * @throws SQLException if a database access error
     *                      occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public boolean first() throws SQLException {
        return getResultSet().first();
    }

    /**
     * Moves the cursor to the last row in
     * this <code>getResultSet()</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     *         <code>false</code> if there are no rows in the result set
     * @throws SQLException if a database access error
     *                      occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public boolean last() throws SQLException {
        return getResultSet().last();
    }

    /**
     * Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on.
     *
     * @return the current row number; <code>0</code> if there is no current row
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public int getRow() throws SQLException {
        return getResultSet().getRow();
    }

    /**
     * Moves the cursor to the given row number in
     * this <code>getResultSet()</code> object.
     * <p/>
     * <p>If the row number is positive, the cursor moves to
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on.
     * <p/>
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling the method
     * <code>absolute(-1)</code> positions the
     * cursor on the last row; calling the method <code>absolute(-2)</code>
     * moves the cursor to the next-to-last row, and so on.
     * <p/>
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before the first row or after
     * the last row.
     * <p/>
     * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>. Calling <code>absolute(-1)</code>
     * is the same as calling <code>last()</code>.
     *
     * @param row the number of the row to which the cursor should move.
     *            A positive number indicates the row number counting from the
     *            beginning of the result set; a negative number indicates the
     *            row number counting from the end of the result set
     * @return <code>true</code> if the cursor is on the result set;
     *         <code>false</code> otherwise
     * @throws SQLException if a database access error
     *                      occurs, or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public boolean absolute(int row) throws SQLException {
        return getResultSet().absolute(row);
    }

    /**
     * Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     * <p/>
     * <p>Note: Calling the method <code>relative(1)</code>
     * is identical to calling the method <code>next()</code> and
     * calling the method <code>relative(-1)</code> is identical
     * to calling the method <code>previous()</code>.
     *
     * @param rows an <code>int</code> specifying the number of rows to
     *             move from the current row; a positive number moves the cursor
     *             forward; a negative number moves the cursor backward
     * @return <code>true</code> if the cursor is on a row;
     *         <code>false</code> otherwise
     * @throws SQLException if a database access error occurs,
     *                      there is no current row, or the result set type is
     *                      <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     */
    public boolean relative(int rows) throws SQLException {
        return getResultSet().relative(rows);
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

    /**
     * Gives a hint as to the direction in which the rows in this
     * <code>getResultSet()</code> object will be processed.
     * The initial value is determined by the
     * <code>Statement</code> object
     * that produced this <code>getResultSet()</code> object.
     * The fetch direction may be changed at any time.
     *
     * @param direction an <code>int</code> specifying the suggested
     *                  fetch direction; one of <code>getResultSet().FETCH_FORWARD</code>,
     *                  <code>getResultSet().FETCH_REVERSE</code>, or
     *                  <code>getResultSet().FETCH_UNKNOWN</code>
     * @throws SQLException if a database access error occurs or
     *                      the result set type is <code>TYPE_FORWARD_ONLY</code> and the fetch
     *                      direction is not <code>FETCH_FORWARD</code>
     * @see Statement#setFetchDirection
     * @see #getFetchDirection
     * @since 1.2
     */
    public void setFetchDirection(int direction) throws SQLException {
        getResultSet().setFetchDirection(direction);
    }

    /**
     * Retrieves the fetch direction for this
     * <code>getResultSet()</code> object.
     *
     * @return the current fetch direction for this <code>getResultSet()</code> object
     * @throws SQLException if a database access error occurs
     * @see #setFetchDirection
     * @since 1.2
     */
    public int getFetchDirection() throws SQLException {
        return getResultSet().getFetchDirection();
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for this
     * <code>getResultSet()</code> object.
     * If the fetch size specified is zero, the JDBC driver
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the
     * <code>Statement</code> object
     * that created the result set.  The fetch size may be changed at any time.
     *
     * @param rows the number of rows to fetch
     * @throws SQLException if a database access error occurs or the
     *                      condition <code>0 <= rows <= Statement.getMaxRows()</code> is not satisfied
     * @see #getFetchSize
     * @since 1.2
     */
    public void setFetchSize(int rows) throws SQLException {
        getResultSet().setFetchSize(rows);
    }

    /**
     * Retrieves the fetch size for this
     * <code>getResultSet()</code> object.
     *
     * @return the current fetch size for this <code>getResultSet()</code> object
     * @throws SQLException if a database access error occurs
     * @see #setFetchSize
     * @since 1.2
     */
    public int getFetchSize() throws SQLException {
        return getResultSet().getFetchSize();
    }

    /**
     * Retrieves the type of this <code>getResultSet()</code> object.
     * The type is determined by the <code>Statement</code> object
     * that created the result set.
     *
     * @return <code>getResultSet().TYPE_FORWARD_ONLY</code>,
     *         <code>getResultSet().TYPE_SCROLL_INSENSITIVE</code>,
     *         or <code>getResultSet().TYPE_SCROLL_SENSITIVE</code>
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public int getType() throws SQLException {
        return getResultSet().getType();
    }

    /**
     * Retrieves the concurrency mode of this <code>getResultSet()</code> object.
     * The concurrency used is determined by the
     * <code>Statement</code> object that created the result set.
     *
     * @return the concurrency type, either
     *         <code>getResultSet().CONCUR_READ_ONLY</code>
     *         or <code>getResultSet().CONCUR_UPDATABLE</code>
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public int getConcurrency() throws SQLException {
        return getResultSet().getConcurrency();
    }

    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * Retrieves whether the current row has been updated.  The value returned
     * depends on whether or not the result set can detect updates.
     *
     * @return <code>true</code> if both (1) the row has been visibly updated
     *         by the owner or another and (2) updates are detected
     * @throws SQLException if a database access error occurs
     * @see DatabaseMetaData#updatesAreDetected
     * @since 1.2
     */
    public boolean rowUpdated() throws SQLException {
        return getResultSet().rowUpdated();
    }

    /**
     * Retrieves whether the current row has had an insertion.
     * The value returned depends on whether or not this
     * <code>getResultSet()</code> object can detect visible inserts.
     *
     * @return <code>true</code> if a row has had an insertion
     *         and insertions are detected; <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @see DatabaseMetaData#insertsAreDetected
     * @since 1.2
     */
    public boolean rowInserted() throws SQLException {
        return getResultSet().rowInserted();
    }

    /**
     * Retrieves whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether
     * or not this <code>getResultSet()</code> object can detect deletions.
     *
     * @return <code>true</code> if a row was deleted and deletions are detected;
     *         <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @see DatabaseMetaData#deletesAreDetected
     * @since 1.2
     */
    public boolean rowDeleted() throws SQLException {
        return getResultSet().rowDeleted();
    }

    /**
     * Gives a nullable column a null value.
     * <p/>
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code>
     * or <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateNull(int columnIndex) throws SQLException {
        getResultSet().updateNull(columnIndex);
    }

    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        getResultSet().updateBoolean(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateByte(int columnIndex, byte x) throws SQLException {
        getResultSet().updateByte(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateShort(int columnIndex, short x) throws SQLException {
        getResultSet().updateShort(columnIndex, x);
    }

    /**
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateInt(int columnIndex, int x) throws SQLException {
        getResultSet().updateInt(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateLong(int columnIndex, long x) throws SQLException {
        getResultSet().updateLong(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>float</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateFloat(int columnIndex, float x) throws SQLException {
        getResultSet().updateFloat(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateDouble(int columnIndex, double x) throws SQLException {
        getResultSet().updateDouble(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.math.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        getResultSet().updateBigDecimal(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateString(int columnIndex, String x) throws SQLException {
        getResultSet().updateString(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>byte</code> array value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        getResultSet().updateBytes(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        getResultSet().updateDate(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        getResultSet().updateTime(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
            throws SQLException {
        getResultSet().updateTimestamp(columnIndex, x);
    }

    /**
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @param length      the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateAsciiStream(int columnIndex,
                                  java.io.InputStream x,
                                  int length) throws SQLException {
        getResultSet().updateAsciiStream(columnIndex, x, length);
    }

    /**
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @param length      the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBinaryStream(int columnIndex,
                                   java.io.InputStream x,
                                   int length) throws SQLException {
        getResultSet().updateBinaryStream(columnIndex, x, length);
    }

    /**
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @param length      the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateCharacterStream(int columnIndex,
                                      java.io.Reader x,
                                      int length) throws SQLException {
        getResultSet().updateCharacterStream(columnIndex, x, length);
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @param scale       for <code>java.sql.Types.DECIMA</code>
     *                    or <code>java.sql.Types.NUMERIC</code> types,
     *                    this is the number of digits after the decimal point.  For all other
     *                    types this value will be ignored.
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException {
        getResultSet().updateObject(columnIndex, x, scale);
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateObject(int columnIndex, Object x) throws SQLException {
        getResultSet().updateObject(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>null</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateNull(String columnName) throws SQLException {
        getResultSet().updateNull(columnName);
    }

    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        getResultSet().updateBoolean(columnName, x);
    }

    /**
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateByte(String columnName, byte x) throws SQLException {
        getResultSet().updateByte(columnName, x);
    }

    /**
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateShort(String columnName, short x) throws SQLException {
        getResultSet().updateShort(columnName, x);
    }

    /**
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateInt(String columnName, int x) throws SQLException {
        getResultSet().updateInt(columnName, x);
    }

    /**
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateLong(String columnName, long x) throws SQLException {
        getResultSet().updateLong(columnName, x);
    }

    /**
     * Updates the designated column with a <code>float    </code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateFloat(String columnName, float x) throws SQLException {
        getResultSet().updateFloat(columnName, x);
    }

    /**
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateDouble(String columnName, double x) throws SQLException {
        getResultSet().updateDouble(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        getResultSet().updateBigDecimal(columnName, x);
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateString(String columnName, String x) throws SQLException {
        getResultSet().updateString(columnName, x);
    }

    /**
     * Updates the designated column with a byte array value.
     * <p/>
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code>
     * or <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        getResultSet().updateBytes(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        getResultSet().updateDate(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        getResultSet().updateTime(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateTimestamp(String columnName, java.sql.Timestamp x)
            throws SQLException {
        getResultSet().updateTimestamp(columnName, x);
    }

    /**
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @param length     the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateAsciiStream(String columnName,
                                  java.io.InputStream x,
                                  int length) throws SQLException {
        getResultSet().updateAsciiStream(columnName, x, length);
    }

    /**
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @param length     the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateBinaryStream(String columnName,
                                   java.io.InputStream x,
                                   int length) throws SQLException {
        getResultSet().updateBinaryStream(columnName, x, length);
    }

    /**
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param reader     the <code>java.io.Reader</code> object containing
     *                   the new column value
     * @param length     the length of the stream
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateCharacterStream(String columnName,
                                      java.io.Reader reader,
                                      int length) throws SQLException {
        getResultSet().updateCharacterStream(columnName, reader, length);
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @param scale      for <code>java.sql.Types.DECIMAL</code>
     *                   or <code>java.sql.Types.NUMERIC</code> types,
     *                   this is the number of digits after the decimal point.  For all other
     *                   types this value will be ignored.
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateObject(String columnName, Object x, int scale)
            throws SQLException {
        getResultSet().updateObject(columnName, x, scale);
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public void updateObject(String columnName, Object x) throws SQLException {
        getResultSet().updateObject(columnName, x);
    }

    /**
     * Inserts the contents of the insert row into this
     * <code>getResultSet()</code> object and into the database.
     * The cursor must be on the insert row when this method is called.
     *
     * @throws SQLException if a database access error occurs,
     *                      if this method is called when the cursor is not on the insert row,
     *                      or if not all of non-nullable columns in
     *                      the insert row have been given a value
     * @since 1.2
     */
    public void insertRow() throws SQLException {
        getResultSet().insertRow();
    }

    /**
     * Updates the underlying database with the new contents of the
     * current row of this <code>getResultSet()</code> object.
     * This method cannot be called when the cursor is on the insert row.
     *
     * @throws SQLException if a database access error occurs or
     *                      if this method is called when the cursor is on the insert row
     * @since 1.2
     */
    public void updateRow() throws SQLException {
        getResultSet().updateRow();
    }

    /**
     * Deletes the current row from this <code>getResultSet()</code> object
     * and from the underlying database.  This method cannot be called when
     * the cursor is on the insert row.
     *
     * @throws SQLException if a database access error occurs
     *                      or if this method is called when the cursor is on the insert row
     * @since 1.2
     */
    public void deleteRow() throws SQLException {
        getResultSet().deleteRow();
    }

    /**
     * Refreshes the current row with its most recent value in
     * the database.  This method cannot be called when
     * the cursor is on the insert row.
     * <p/>
     * <P>The <code>refreshRow</code> method provides a way for an
     * application to
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver
     * may actually refresh multiple rows at once if the fetch size is
     * greater than one.
     * <p/>
     * <P> All values are refetched subject to the transaction isolation
     * level and cursor sensitivity.  If <code>refreshRow</code> is called after
     * calling an updater method, but before calling
     * the method <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method
     * <code>refreshRow</code> frequently will likely slow performance.
     *
     * @throws SQLException if a database access error
     *                      occurs or if this method is called when the cursor is on the insert row
     * @since 1.2
     */
    public void refreshRow() throws SQLException {
        getResultSet().refreshRow();
    }

    /**
     * Cancels the updates made to the current row in this
     * <code>getResultSet()</code> object.
     * This method may be called after calling an
     * updater method(s) and before calling
     * the method <code>updateRow</code> to roll back
     * the updates made to a row.  If no updates have been made or
     * <code>updateRow</code> has already been called, this method has no
     * effect.
     *
     * @throws SQLException if a database access error
     *                      occurs or if this method is called when the cursor is
     *                      on the insert row
     * @since 1.2
     */
    public void cancelRowUpdates() throws SQLException {
        getResultSet().cancelRowUpdates();
    }

    /**
     * Moves the cursor to the insert row.  The current cursor position is
     * remembered while the cursor is positioned on the insert row.
     * <p/>
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the updater methods prior to
     * inserting the row into the result set.
     * <p/>
     * Only the updater, getter,
     * and <code>insertRow</code> methods may be
     * called when the cursor is on the insert row.  All of the columns in
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.
     * An updater method must be called before a
     * getter method can be called on a column value.
     *
     * @throws SQLException if a database access error occurs
     *                      or the result set is not updatable
     * @since 1.2
     */
    public void moveToInsertRow() throws SQLException {
        getResultSet().moveToInsertRow();
    }

    /**
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on
     * the insert row.
     *
     * @throws SQLException if a database access error occurs
     *                      or the result set is not updatable
     * @since 1.2
     */
    public void moveToCurrentRow() throws SQLException {
        getResultSet().moveToCurrentRow();
    }

    /**
     * Retrieves the <code>Statement</code> object that produced this
     * <code>getResultSet()</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns
     * <code>null</code>.
     *
     * @return the <code>Statment</code> object that produced
     *         this <code>getResultSet()</code> object or <code>null</code>
     *         if the result set was produced some other way
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Statement getStatement() throws SQLException {
        return getResultSet().getStatement();
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved.
     *
     * @param i   the first column is 1, the second is 2, ...
     * @param map a <code>java.util.Map</code> object that contains the mapping
     *            from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> in the Java programming language
     *         representing the SQL value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Object getObject(int i, java.util.Map map) throws SQLException {
        return getResultSet().getObject(i, map);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Ref</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL <code>REF</code>
     *         value
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Ref getRef(int i) throws SQLException {
        return getResultSet().getRef(i);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Blob</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Blob</code> object representing the SQL
     *         <code>BLOB</code> value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Blob getBlob(int i) throws SQLException {
        return getResultSet().getBlob(i);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Clob</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL
     *         <code>CLOB</code> value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Clob getClob(int i) throws SQLException {
        return getResultSet().getClob(i);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as an <code>Array</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL
     *         <code>ARRAY</code> value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Array getArray(int i) throws SQLException {
        return getResultSet().getArray(i);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate.
     *
     * @param colName the name of the column from which to retrieve the value
     * @param map     a <code>java.util.Map</code> object that contains the mapping
     *                from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> representing the SQL value in the
     *         specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Object getObject(String colName, java.util.Map map) throws SQLException {
        return getResultSet().getObject(colName, map);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Ref</code> object
     * in the Java programming language.
     *
     * @param colName the column name
     * @return a <code>Ref</code> object representing the SQL <code>REF</code>
     *         value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Ref getRef(String colName) throws SQLException {
        return getResultSet().getRef(colName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Blob</code> object
     * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the SQL <code>BLOB</code>
     *         value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Blob getBlob(String colName) throws SQLException {
        return getResultSet().getBlob(colName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>Clob</code> object
     * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL <code>CLOB</code>
     *         value in the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Clob getClob(String colName) throws SQLException {
        return getResultSet().getClob(colName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as an <code>Array</code> object
     * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
     *         the specified column
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public Array getArray(String colName) throws SQLException {
        return getResultSet().getArray(colName);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal         the <code>java.util.Calendar</code> object
     *                    to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return getResultSet().getDate(columnIndex, cal);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column from which to retrieve the value
     * @param cal        the <code>java.util.Calendar</code> object
     *                   to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        return getResultSet().getDate(columnName, cal);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal         the <code>java.util.Calendar</code> object
     *                    to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return getResultSet().getTime(columnIndex, cal);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal        the <code>java.util.Calendar</code> object
     *                   to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        return getResultSet().getTime(columnName, cal);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Timestamp</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal         the <code>java.util.Calendar</code> object
     *                    to use in constructing the timestamp
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException {
        return getResultSet().getTimestamp(columnIndex, cal);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.sql.Timestamp</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal        the <code>java.util.Calendar</code> object
     *                   to use in constructing the date
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     * @since 1.2
     */
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException {
        return getResultSet().getTimestamp(columnName, cal);
    }

    //-------------------------- JDBC 3.0 ----------------------------------------

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.net.URL</code>
     * object in the Java programming language.
     *
     * @param columnIndex the index of the column 1 is the first, 2 is the second,...
     * @return the column value as a <code>java.net.URL</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs,
     *                      or if a URL is malformed
     * @since 1.4
     */
    public java.net.URL getURL(int columnIndex) throws SQLException {
        return getResultSet().getURL(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>getResultSet()</code> object as a <code>java.net.URL</code>
     * object in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value as a <code>java.net.URL</code> object;
     *         if the value is SQL <code>NULL</code>,
     *         the value returned is <code>null</code> in the Java programming language
     * @throws SQLException if a database access error occurs
     *                      or if a URL is malformed
     * @since 1.4
     */
    public java.net.URL getURL(String columnName) throws SQLException {
        return getResultSet().getURL(columnName);
    }

    /**
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException {
        getResultSet().updateRef(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateRef(String columnName, java.sql.Ref x) throws SQLException {
        getResultSet().updateRef(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException {
        getResultSet().updateBlob(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateBlob(String columnName, java.sql.Blob x) throws SQLException {
        getResultSet().updateBlob(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException {
        getResultSet().updateClob(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateClob(String columnName, java.sql.Clob x) throws SQLException {
        getResultSet().updateClob(columnName, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x           the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
        getResultSet().updateArray(columnIndex, x);
    }

    /**
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x          the new column value
     * @throws SQLException if a database access error occurs
     * @since 1.4
     */
    public void updateArray(String columnName, java.sql.Array x) throws SQLException {
        getResultSet().updateArray(columnName, x);
    }
}
