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
package groovy.xml.script

import groovy.xml.MarkupBuilder

/** note you could use Expando classes if you don't mind being dynamically typed 
f = new Expando()

f.author = new Expando(name:'Ted Leung',url:'http://www.sauria.com/blog', email:'twl@sauria.com')

f.entries = [ new Expando(title:'one',summary:'first post'), new Expando(title:'two',summary:'the second post'), new Expando(title:'three', summary:'post the third'), new Expando(title:'four',summary:'the ponderous fourth post') ]
*/

f = new Feed()

f.author = new Person(name:'Ted Leung',url:'http://www.sauria.com/blog', email:'twl@sauria.com')

f.entries = [ new Entry(title:'one',summary:'first post'), new Entry(title:'two',summary:'the second post'), new Entry(title:'three', summary:'post the third'), new Entry(title:'four',summary:'the ponderous fourth post') ]

f.entries.each { println it.title }
println f.author.name

xml = new MarkupBuilder()

atom = xml.atom {
  title("Ted Leung off the air")
  link("http://www.sauria.com/noblog")
  author {
    person {
      name(f.author.name)
      url(f.author.url)
      email(f.author.email)
    }
  }

  for (e in f.entries) {
    entry {
      title(e.title)
      summary(e.summary)
    }
  }
}


class Feed {
    String title
    String link
    Person author
    String tagline
    String generator
    String copyright
    String modified
    List entries
}

class Entry {
    String title
    String link
    String id
    String summary
    String content
    Person author
    String created
    String issued
    String modified
}

class Person {
    String name
    String url
    String email
}
