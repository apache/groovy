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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a ResultSet retrieved as a callable statement out parameter.
 */
class CallResultSet extends GroovyResultSetExtension {
    /**
     * The zero-based out-parameter index.
     */
    int indx;
    /**
     * The callable statement that owns the out parameter.
     */
    CallableStatement call;
    /**
     * The lazily materialized result set for the out parameter.
     */
    ResultSet resultSet;
    /**
     * Indicates whether the out parameter still needs to be read from the callable statement.
     */
    boolean firstCall = true;

    /**
     * Creates a lazy {@link ResultSet} view over a callable-statement out parameter.
     *
     * @param call the callable statement that owns the out parameter
     * @param indx the zero-based out-parameter index
     */
    CallResultSet(CallableStatement call, int indx) {
        super(null);
        this.call = call;
        this.indx = indx;
    }

    /**
     * Returns the lazily fetched result set for the out parameter.
     *
     * @return the out-parameter result set
     * @throws SQLException if the result set cannot be retrieved
     */
    @Override
    protected ResultSet getResultSet() throws SQLException {
        if (firstCall) {
            resultSet = (ResultSet) call.getObject(indx + 1);
            firstCall = false;
        }
        return resultSet;
    }

    /**
     * Creates a proxy-backed {@link GroovyResultSet} for the specified callable-statement out parameter.
     *
     * @param call the callable statement that owns the out parameter
     * @param idx the zero-based out-parameter index
     * @return a Groovy result-set view for the out parameter
     */
    protected static GroovyResultSet getImpl(CallableStatement call, int idx) {
        GroovyResultSetProxy proxy = new GroovyResultSetProxy(new CallResultSet(call, idx));
        return proxy.getImpl();
    }
}
