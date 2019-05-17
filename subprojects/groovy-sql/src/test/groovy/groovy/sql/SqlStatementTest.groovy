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
package groovy.sql

import groovy.test.GroovyTestCase

import javax.sql.DataSource

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * Unit test of Sql statement feature
 */
class SqlStatementTest extends GroovyTestCase {
    Sql sql

    void setUp() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        sql = new Sql(ds.connection)
        sql.execute("create table PERSON ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10) )")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")
    }

    void testWithStatement() {
        assert sql.rows("SELECT * FROM PERSON").size() == 5
        sql.withStatement{ it.maxRows = 3 }
        assert sql.rows("SELECT * FROM PERSON").size() == 3
    }
}
