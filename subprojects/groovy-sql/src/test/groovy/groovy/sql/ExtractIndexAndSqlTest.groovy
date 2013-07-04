/*
 * Copyright 2003-2013 the original author or authors.
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

class ExtractIndexAndSqlTest extends GroovyTestCase {

    void testDetectsNamedParameters() {
        assert !ExtractIndexAndSql.hasNamedParameters('select * from PERSON')
        assert ExtractIndexAndSql.hasNamedParameters('select * from PERSON where id=:id')
        assert ExtractIndexAndSql.hasNamedParameters('select * from PERSON where id=?.id')
        assert ExtractIndexAndSql.hasNamedParameters('select * from PERSON where id=?1.id')
        assert ExtractIndexAndSql.hasNamedParameters('select * from PERSON where id=?')
    }

    void testWithEmptyStringQuote() {
        String query = """select * from FOOD where type=:foo and category='' and calories < ?2.kcal"""
        String expected = """select * from FOOD where type=? and category='' and calories < ?"""

        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithQuoteEmbeddedInInlineComments() {
        String query = """select *
from FOOD
-- An ' apostrophe
where type=:foo
"""
        def expected = """select *
from FOOD
-- An ' apostrophe
where type=?
"""
        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithNamedParamEmbeddedInInlineComments() {
        String query = """select *
from FOOD
-- where type=:bar
where type=:foo
"""
        String expected = """select *
from FOOD
-- where type=:bar
where type=?
"""

        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithQuoteEmbeddedInMultilineComments() {
        String query = """select *
from FOOD
/* An ' apostrophe
in a 'multiline' comment */where type=:foo
"""
        String expected = """select *
from FOOD
/* An ' apostrophe
in a 'multiline' comment */where type=?
"""

        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithNamedParamEmbeddedInMultilineComments() {
        String query = """select *
from FOOD
/* An ' apostrophe
where type=:bar
in a 'multiline' comment */where type=:foo
"""
        String expected = """select *
from FOOD
/* An ' apostrophe
where type=:bar
in a 'multiline' comment */where type=?
"""
        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithMultipleEmbeddedQuotesInString() {
        String query = """select location_id, 'James O''Brian' as other_name
from PERSON
where lastname=:foo
"""
        String expected = """select location_id, 'James O''Brian' as other_name
from PERSON
where lastname=?
"""
        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testWithUnterminatedString() {
        String query = """select location_id, 'James O'''Brian' as other_name
from PERSON
where lastname=:foo
"""

        shouldFail(IllegalStateException) {
            ExtractIndexAndSql.from(query)
        }
    }

    void testUnterminatedQuoteAtEndOfString() {
        String query = "select * from FOOD where country type = :foo and country = '"
        String expected = "select * from FOOD where country type = ? and country = '"

        shouldFail(IllegalStateException) {
            ExtractIndexAndSql.from(query).newSql
        }
    }

    void testWithStringSpanningMoreThanOneLine() {
        String query = """select 'this is a ''multiline'' with a '':named :param'' string
 and spans two lines'
from PERSON
where lastname=:foo
"""
        String expected = """select 'this is a ''multiline'' with a '':named :param'' string
 and spans two lines'
from PERSON
where lastname=?
"""

        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testSingleDash() {
        String query = """select location_id
from PERSON
where lastname=?
and 3 = (4 - 1)
"""

        assert query == ExtractIndexAndSql.from(query).newSql
    }

    void testForwardSlash() {
        String query = """select location_id
from PERSON
where lastname=?
and 3 = (12 / 4)
"""

        assert query == ExtractIndexAndSql.from(query).newSql
    }

    void testWithPostgreSqlCast() {
        String query = """select 'name' from PERSON where id=:id and birth_date=:birthDate::timestamp"""
        String expected = """select 'name' from PERSON where id=? and birth_date=?::timestamp"""

        assert expected == ExtractIndexAndSql.from(query).newSql
    }

    void testNamedParameters() {
        String query = "select * from PERSON where name=:name and location=:location and building=:building"
        String expected = "select * from PERSON where name=? and location=? and building=?"

        ExtractIndexAndSql extracter = ExtractIndexAndSql.from(query)

        assert expected == extracter.newSql

        assert 3 == extracter.indexPropList.size()

        assert 0 == extracter.indexPropList.get(0)[0]
        assert 'name' == extracter.indexPropList.get(0)[1]

        assert 0 == extracter.indexPropList.get(1)[0]
        assert 'location' == extracter.indexPropList.get(1)[1]

        assert 0 == extracter.indexPropList.get(2)[0]
        assert 'building' == extracter.indexPropList.get(2)[1]
    }

    void testNamedOrdinalParameters() {
        String query = "select * from PERSON where name=?1.name and location=?2.location and building=?3.building"
        String expected = "select * from PERSON where name=? and location=? and building=?"

        ExtractIndexAndSql extracter = ExtractIndexAndSql.from(query)

        assert expected == extracter.newSql

        assert 3 == extracter.indexPropList.size()

        assert 0 == extracter.indexPropList.get(0)[0]
        assert 'name' == extracter.indexPropList.get(0)[1]

        assert 1 == extracter.indexPropList.get(1)[0]
        assert 'location' == extracter.indexPropList.get(1)[1]

        assert 2 == extracter.indexPropList.get(2)[0]
        assert 'building' == extracter.indexPropList.get(2)[1]
    }

    void testCastingNotConfusedWithNamedParameters_5111() {
        assert !ExtractIndexAndSql.hasNamedParameters("select * from TABLE where TEXTFIELD::integer = 3")
    }

}
