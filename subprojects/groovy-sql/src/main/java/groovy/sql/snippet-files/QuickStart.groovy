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
import groovy.sql.Sql

def db = [url: 'jdbc:hsqldb:mem:testDB',
          user: 'sa', password: '',
          driver: 'org.hsqldb.jdbc.JDBCDriver']

Sql.withInstance(db.url, db.user, db.password, db.driver) { sql ->
    sql.execute '''
        create table PROJECT (
            id integer not null,
            name varchar(50) not null,
            url varchar(100)
        )
    '''

    [[10, 'Groovy', 'https://groovy-lang.org'],
     [20, 'Grails', 'https://grails.org'],
     [40, 'Gradle', 'https://gradle.org']].each { row ->
        sql.execute 'insert into PROJECT (id, name, url) values (?, ?, ?)', row
    }

    sql.eachRow('select * from PROJECT order by id') { row ->
        println "${row.name.padRight(10)} ($row.url)"
    }
}
