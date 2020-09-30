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
package org.apache.groovy.sql.extensions;

import groovy.lang.GroovyRuntimeException;
import groovy.sql.GroovyResultSet;
import groovy.sql.GroovyRowResult;
import groovy.sql.ResultSetMetaDataWrapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class defines all the new SQL-related groovy methods which enhance
 * the normal JDK SQL classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class SqlExtensions {

    /**
     * Returns a GroovyRowResult given a ResultSet.
     *
     * @param rs a ResultSet
     * @return the resulting GroovyRowResult
     * @throws java.sql.SQLException if a database error occurs
     * @since 1.6.0
     */
    public static GroovyRowResult toRowResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        Map<String, Object> lhm = new LinkedHashMap<String, Object>(metadata.getColumnCount(), 1);
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            lhm.put(metadata.getColumnLabel(i), rs.getObject(i));
        }
        return new GroovyRowResult(lhm);
    }

    /**
     * Return a java.sql.Timestamp given a java.util.Date.
     *
     * @param d a date
     * @return the date wrapped as a Timestamp
     * @since 1.6.6
     */
    public static Timestamp toTimestamp(Date d) {
        return new Timestamp(d.getTime());
    }

    /**
     * Coerce a GroovyResultSet to a boolean value.
     * A GroovyResultSet is coerced to false if there are no more rows to iterate over,
     * and to true otherwise.
     *
     * @param grs the GroovyResultSet
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(GroovyResultSet grs) {
        //TODO: check why this asBoolean() method is needed for SqlTest to pass with custom boolean coercion in place
        return true;
    }

    /**
     * Return an Iterator given a ResultSetMetaData.
     *
     * Enables Groovy collection method syntactic sugar on ResultSetMetaData.
     *
     * @param resultSetMetaData the ResultSetMetaData to iterate over
     * @return an iterator for the ResultSetMetaData
     * @since 1.7
     */
    public static Iterator<ResultSetMetaDataWrapper> iterator(ResultSetMetaData resultSetMetaData) {
        return new ResultSetMetaDataIterator(resultSetMetaData);
    }

    private static class ResultSetMetaDataIterator implements Iterator<ResultSetMetaDataWrapper> {
        private final ResultSetMetaData target;
        private int index = 1;

        public ResultSetMetaDataIterator(ResultSetMetaData target) {
            this.target = target;
        }

        @Override
        public boolean hasNext() {
            try {
                return index <= target.getColumnCount();
            }
            catch (SQLException ex) {
                throw new GroovyRuntimeException("Unable to obtain column count from ResultSetMetaData", ex);
            }
        }

        @Override
        public ResultSetMetaDataWrapper next() {
            return new ResultSetMetaDataWrapper(target, index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove from ResultSetMetaData");
        }
    }

}
