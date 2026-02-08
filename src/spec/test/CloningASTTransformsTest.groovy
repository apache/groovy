import groovy.test.GroovyTestCase

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

    public Book clone() throws CloneNotSupportedException {
        Book result = super.clone()
        result.authors = authors instanceof Cloneable ? (List) authors.clone() : authors
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

    void testAutoExternalize() {
        assertScript '''
// tag::example_autoext[]
import groovy.transform.AutoExternalize

@AutoExternalize
class Book {
    String isbn
    String title
    float price
}
// end::example_autoext[]
/*
// tag::example_autoext_equiv[]
class Book implements java.io.Externalizable {
    String isbn
    String title
    float price

    void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(isbn)
        out.writeObject(title)
        out.writeFloat( price )
    }

    public void readExternal(ObjectInput oin) {
        isbn = (String) oin.readObject()
        title = (String) oin.readObject()
        price = oin.readFloat()
    }

}
// end::example_autoext_equiv[]
*/
def book = new Book(isbn: 'xxx', title:'Auto externalization for dummies', price: 15)
def str = ''
ObjectOutput o = {
   str = "$str$it/"
} as ObjectOutput
book.writeExternal(o)
assert str == "xxx/Auto externalization for dummies/15.0/"
int idx = 0
ObjectInput i = {
    switch (idx++) {
        case 0:
            'xxx'
            break
        case 1:
            'Auto externalization for dummies'
            break
        case 2:
            1.5f
            break
    }
} as ObjectInput
book = new Book()
book.readExternal(i)
assert book.isbn == 'xxx'
assert book.title == 'Auto externalization for dummies'
assert book.price == 1.5
'''
    }

    void testAutoExternalizeWithExcludes() {
        assertScript '''
// tag::example_autoext_excludes[]
import groovy.transform.AutoExternalize

@AutoExternalize(excludes='price')
class Book {
    String isbn
    String title
    float price
}
// end::example_autoext_excludes[]

def book = new Book(isbn: 'xxx', title:'Auto externalization for dummies', price: 15)
def str = ''
ObjectOutput o = {
   str = "$str$it/"
} as ObjectOutput
book.writeExternal(o)
assert str == "xxx/Auto externalization for dummies/"
int idx = 0
ObjectInput i = {
    switch (idx++) {
        case 0:
            'xxx'
            break
        case 1:
            'Auto externalization for dummies'
            break
        case 2:
            1.5f
            break
    }
} as ObjectInput
book = new Book()
book.readExternal(i)
assert book.isbn == 'xxx'
assert book.title == 'Auto externalization for dummies'
assert book.price == 0 // because price is excluded
'''
    }
    void testAutoExternalizeWithIncludeFields() {
        assertScript '''
// tag::example_autoext_includeFields[]
import groovy.transform.AutoExternalize

@AutoExternalize(includeFields=true)
class Book {
    String isbn
    String title
    protected float price
}
// end::example_autoext_includeFields[]

def book = new Book(isbn: 'xxx', title:'Auto externalization for dummies', price: 15)
def str = ''
ObjectOutput o = {
   str = "$str$it/"
} as ObjectOutput
book.writeExternal(o)
assert str == "xxx/Auto externalization for dummies/15.0/"
int idx = 0
ObjectInput i = {
    switch (idx++) {
        case 0:
            'xxx'
            break
        case 1:
            'Auto externalization for dummies'
            break
        case 2:
            1.5f
            break
    }
} as ObjectInput
book = new Book()
book.readExternal(i)
assert book.isbn == 'xxx'
assert book.title == 'Auto externalization for dummies'
assert book.price == 1.5f
'''
    }
}
