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

import groovy.lang.Tuple;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which delegates to a PreparedStatement but keeps track of
 * a batch count size. If the batch count reaches the predefined number,
 * this Statement does an executeBatch() automatically. If batchSize is
 * zero, then no batching is performed.
 */
public class BatchingPreparedStatementWrapper extends BatchingStatementWrapper {

    private final PreparedStatement delegate;
    private final List<Tuple> indexPropList;
    private final Sql sql;

    /**
     * Creates a batching wrapper for a prepared statement.
     *
     * @param delegate the prepared statement to wrap
     * @param indexPropList indexed-property descriptors for named-parameter batches, or {@code null}
     * @param batchSize the automatic execution threshold; {@code 0} disables automatic partitioning
     * @param log the logger to use for batch diagnostics
     * @param sql the owning {@link Sql} instance used to bind parameters
     */
    public BatchingPreparedStatementWrapper(PreparedStatement delegate, List<Tuple> indexPropList, int batchSize, Logger log, Sql sql) {
        super(delegate, batchSize, log);
        this.delegate = delegate;
        this.indexPropList = indexPropList;
        this.sql = sql;
    }

    /**
     * Adds one parameter set to the batch using varargs-style arguments.
     *
     * @param parameters the parameter values for a single batch entry
     * @throws SQLException if the batch entry cannot be added
     */
    public void addBatch(Object[] parameters) throws SQLException {
        addBatch(Arrays.asList(parameters));
    }

    /**
     * Adds one parameter set to the batch.
     * <p>
     * When the associated SQL uses named or named-ordinal parameters, the
     * supplied values are first flattened into JDBC positional order.
     *
     * @param parameters the parameter values for a single batch entry
     * @throws SQLException if the parameters cannot be bound or the batch entry cannot be added
     */
    public void addBatch(List<Object> parameters) throws SQLException {
        if (indexPropList != null) {
            sql.setParameters(sql.getUpdatedParams(parameters, indexPropList), delegate);
        } else {
            sql.setParameters(parameters, delegate);
        }
        delegate.addBatch();
        incrementBatchCount();
    }
}
