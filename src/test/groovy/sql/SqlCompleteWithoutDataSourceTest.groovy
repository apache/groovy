/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.sql

import java.sql.DriverManager

import static groovy.sql.SqlTestConstants.DB_DRIVER

/**
 * Tests the use of the Sql class using just a Connection 
 * rather than a DataSource
 */
class SqlCompleteWithoutDataSourceTest extends SqlCompleteTest {
    protected Sql newSql(String uri) {
        Class.forName(DB_DRIVER.name)
        return new Sql(DriverManager.getConnection(uri, [user:'sa', password:''] as Properties))
    }
}
