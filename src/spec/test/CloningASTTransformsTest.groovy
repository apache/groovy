/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class CloningASTTransformsTest extends GroovyTestCase {
    void testAutoCloneAnnotation() {
        assertScript '''
// tag::example_autoclone[]
import groovy.transform.AutoClone

@AutoClone
class Book {
    String isbn
    String title
    List<String> authors
    Date publicationDate
}
// end::example_autoclone[]
/*
// tag::example_autoclone_equiv[]
class Book implements Cloneable {
    String isbn
    String title
    List<String> authors
    Date publicationDate

    public Object clone() throws CloneNotSupportedException {
        def result = super.clone()
        result.isbn = isbn.clone()
        result.title = title.clone()
        result.authors = authors instanceof Cloneable?authors.clone():authors
        result.publicationDate = publicationDate.clone()

        result
    }
}
// end::example_autoclone_equiv[]
*/
def book = new Book(isbn: 'aaa', title: 'The Definitive Guide to cloning', authors:['Dolly'], publicationDate: new Date())
def clone = book.clone()
assert book.isbn == clone.isbn
assert book.title == clone.title
assert book.authors == clone.authors
assert book.publicationDate == clone.publicationDate
assert !(book.authors.is(clone.authors))
'''
    }

    void testAutoCloneAnnotationWithExclude() {
        assertScript '''
// tag::example_autoclone_excludes[]
import groovy.transform.AutoClone
import groovy.transform.AutoCloneStyle

@AutoClone(style=AutoCloneStyle.SIMPLE,excludes='authors')
class Book {
    String isbn
    String title
    List authors
    Date publicationDate
}
// end::example_autoclone_excludes[]
def book = new Book(isbn: 'aaa', title: 'The Definitive Guide to cloning', authors:['Dolly'], publicationDate: new Date())
def clone = book.clone()
assert clone.isbn==book.isbn
assert clone.title==book.title
assert clone.authors == null // because excluded
assert clone.publicationDate==book.publicationDate
'''
    }

    void testAutoCloneAnnotationWithIncludeFields() {
        assertScript '''
// tag::example_autoclone_includeFields[]
import groovy.transform.AutoClone
import groovy.transform.AutoCloneStyle

@AutoClone(style=AutoCloneStyle.SIMPLE,includeFields=true)
class Book {
    String isbn
    String title
    List authors
    protected Date publicationDate
}
// end::example_autoclone_includeFields[]
def book = new Book(isbn: 'aaa', title: 'The Definitive Guide to cloning', authors:['Dolly'], publicationDate: new Date())
def clone = book.clone()
assert clone.isbn==book.isbn
assert clone.title==book.title
assert clone.authors == book.authors
assert clone.publicationDate==book.publicationDate
'''
    }
}
