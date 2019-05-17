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

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_URL_SUFFIX
import static groovy.sql.SqlTestConstants.DB_USER

class SqlHelperTestCase extends GroovyTestCase {
    SqlHelperTestCase() {
        def testdb = System.getProperty("groovy.testdb.props")
        if (testdb && new File(testdb).exists()) {
            props = new Properties()
            new File(testdb).withReader { r ->
                props.load(r)
            }
        }
    }

    protected props = null
    static counter = 1

    static Sql makeSql() {
        def foo = new SqlHelperTestCase()
        return foo.createSql()
    }

    protected createEmptySql() {
        return newSql(getURI())
    }

    protected Sql createSql() {
        Sql sql = newSql(getURI())
        ["PERSON", "FOOD", "FEATURE"].each { tryDrop(sql, it) }
        sql.execute("create table PERSON ( firstname VARCHAR(100), lastname VARCHAR(100), id INTEGER, location_id INTEGER, location_name VARCHAR(100) )")
        sql.execute("create table FOOD ( type VARCHAR(100), name VARCHAR(100))")
        sql.execute("create table FEATURE ( id INTEGER, name VARCHAR(100))")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add(firstname: "James", lastname: "Strachan", id: 1, location_id: 10, location_name: 'London')
        people.add(firstname: "Bob", lastname: "Mcwhirter", id: 2, location_id: 20, location_name: 'Atlanta')
        people.add(firstname: "Sam", lastname: "Pullara", id: 3, location_id: 30, location_name: 'California')

        def food = sql.dataSet("FOOD")
        food.add(type: "cheese", name: "edam")
        food.add(type: "cheese", name: "brie")
        food.add(type: "cheese", name: "cheddar")
        food.add(type: "drink", name: "beer")
        food.add(type: "drink", name: "coffee")

        def features = sql.dataSet("FEATURE")
        features.add(id: 1, name: 'GDO')
        features.add(id: 2, name: 'GPath')
        features.add(id: 3, name: 'GroovyMarkup')
        return sql
    }

    protected tryDrop(Sql sql, String tableName) {
        try {
            sql.execute("drop table $tableName".toString())
        } catch (java.sql.SQLException se) {}
    }

    protected getURI() {
        if (props && props."groovy.testdb.url")
            return props."groovy.testdb.url"
        def answer = DB_URL_PREFIX
        def name = getMethodName()
        if (name == null) {
            name = ""
        }
        name += counter++
        return answer + name + DB_URL_SUFFIX
    }

    protected Sql newSql(String uri) {
        if (props) {
            def url = props."groovy.testdb.url"
            def driver = props."groovy.testdb.driver"
            def username = props."groovy.testdb.username"
            def password = props."groovy.testdb.password"
            // test the map of String/GString version of newInstance
            if (!username && !password) return Sql.newInstance(url:url, driver:"$driver")
            return Sql.newInstance(url, username, password, driver)
        }
        def ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): uri,
                user: DB_USER,
                password: DB_PASSWORD)
        return new Sql(ds)
    }
}
