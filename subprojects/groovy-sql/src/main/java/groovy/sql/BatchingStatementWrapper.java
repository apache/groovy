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

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which delegates to a Statement but keeps track of a batch count size.
 * If the batch count reaches the predefined number, this Statement does an executeBatch()
 * automatically. If batchSize is zero, then no batching is performed.
 */
public class BatchingStatementWrapper extends GroovyObjectSupport implements AutoCloseable {
    private final Statement delegate;
    protected int batchSize;
    protected int batchCount;
    protected Logger log;
    protected List<Integer> results;

    public BatchingStatementWrapper(Statement delegate, int batchSize, Logger log) {
        this.delegate = delegate;
        this.batchSize = batchSize;
        this.log = log;
        reset();
    }

    protected void reset() {
        batchCount = 0;
        results = new ArrayList<>();
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        return InvokerHelper.invokeMethod(delegate, name, args);
    }

    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
        incrementBatchCount();
    }

    /**
     * Increments batch count (after addBatch(..) has been called)
     * and execute {@code delegate.executeBatch()} if batchSize has been reached.
     */
    protected void incrementBatchCount() throws SQLException {
        batchCount++;
        if (batchCount == batchSize /* never true for batchSize of 0 */) {
            int[] result = delegate.executeBatch();
            processResult(result);
            batchCount = 0;
        }
    }

    public void clearBatch() throws SQLException {
        if (batchSize != 0) {
            reset();
        }
        delegate.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        if (shouldCallDelegate()) {
            int[] lastResult = delegate.executeBatch();
            processResult(lastResult);
        }
        int[] result = new int[results.size()];
        for (int i = 0; i < results.size(); i++) {
            result[i] = results.get(i);
        }
        reset();
        return result;
    }

    private boolean shouldCallDelegate() {
        if (batchCount > 0) {
            return true;
        } else if (results.isEmpty()) {
            log.warning("Nothing has been added to batch. This might cause the JDBC driver to throw an exception.");
            return true;
        }
        // Nothing added since last delegate execution. No need to call the delegate this time.
        return false;
    }

    protected void processResult(int[] lastResult) {
        boolean foundError = false;
        for (int i : lastResult) {
            if (i == Statement.EXECUTE_FAILED) foundError = true;
            results.add(i);
        }
        // A little bit of paranoid checking here? Most drivers will throw BatchUpdateException perhaps?
        if (batchCount != lastResult.length) {
            log.warning("Problem executing batch - expected result length of " + batchCount + " but got " + lastResult.length);
        } else if (foundError) {
            log.warning("Problem executing batch - at least one result failed in: " + DefaultGroovyMethods.toList(lastResult));
        } else {
            log.fine("Successfully executed batch with " + lastResult.length + " command(s)");
        }
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }
}
