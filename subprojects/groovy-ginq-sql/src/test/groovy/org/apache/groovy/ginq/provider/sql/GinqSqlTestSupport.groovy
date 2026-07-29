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
package org.apache.groovy.ginq.provider.sql

import groovy.sql.Sql

import java.util.concurrent.atomic.AtomicInteger

/**
 * Creates uniquely-named in-memory H2 databases populated with the
 * employees/departments test schema.
 */
class GinqSqlTestSupport {
    private static final AtomicInteger COUNTER = new AtomicInteger()

    static String newDbUrl() {
        // keep the database alive between connections; closeDb shuts it down
        "jdbc:h2:mem:ginqSqlTest${COUNTER.getAndIncrement()};DB_CLOSE_DELAY=-1"
    }

    static Sql newPopulatedDb(String url = newDbUrl()) {
        def db = Sql.newInstance(url, 'sa', '', 'org.h2.Driver')
        db.execute 'create table employees (id integer, name varchar(50), salary integer, deptId integer, active boolean)'
        db.execute 'create table departments (id integer, name varchar(50))'
        [[1, 'Dev'], [2, 'Sales'], [3, 'Support']].each {
            db.execute('insert into departments (id, name) values (?, ?)', it)
        }
        [[1, 'Alice', 5000, 1, true],
         [2, 'Bob', 3000, 1, true],
         [3, 'Carol', 4000, 2, true],
         [4, 'Dave', 2000, null, false],
         [5, 'Eve', 5000, 2, true],
         [6, 'Frank', 6000, 3, true]].each {
            db.execute('insert into employees (id, name, salary, deptId, active) values (?, ?, ?, ?, ?)', it)
        }
        return db
    }

    static void closeDb(Sql db) {
        try {
            db.execute 'SHUTDOWN'
        } catch (java.sql.SQLException ignore) {
            // H2 reports the connection as closed once SHUTDOWN completes
        }
        db.close()
    }
}
