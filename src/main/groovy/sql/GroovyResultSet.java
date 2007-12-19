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
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Represents an extent of objects
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:ivan_ganza@yahoo.com">Ivan Ganza</a>
 * @author Chris Stevenson
 * @version $Revision$
 */
public interface GroovyResultSet extends GroovyObject, ResultSet {
    /**
     * Supports integer-based subscript operators for accessing at numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     * @throws SQLException if a database error occurs
     * @return the object for this index in the current result set
     */
    Object getAt(int index) throws SQLException;

    /**
     * Gets the value of the designated column in the current row
     * as an <code>Object</code>.
     * @param columnName the SQL name of the column
     * @throws groovy.lang.MissingPropertyException
     *   if an SQLException happens while getting the object
     * @return the returned column value
     */
    public Object getAt(String columnName);

    /**
     * Supports integer based subscript operators for updating the values of numbered columns
     * starting at zero. Negative indices are supported, they will count from the last column backwards.
     *
     * @param index is the number of the column to look at starting at 1
     * @param newValue the new value for this index
     * @throws SQLException if a database error occurs
     */
    void putAt(int index, Object newValue) throws SQLException;

    /**
     * Updates the designated column with an <code>Object</code> value.
     * @param columnName the SQL name of the column
     * @param newValue the updated value
     * @throws MissingPropertyException
     *   if an SQLException happens while setting the new value
     */
    public void putAt(String columnName, Object newValue);

    /**
     * Adds a new row to this result set
     *
     * @param values teh new values to add
     * @throws SQLException if a database error occurs
     */
    void add(Map values) throws SQLException;

    /**
     * Call the closure once for each row in the result set.
     *
     * @param closure the closure to call for each row
     * @throws SQLException if a database error occurs
     */
    void eachRow(Closure closure) throws SQLException;

}
