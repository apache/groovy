import groovy.xml.MarkupBuilder

/**
 * @author Ted Leung
 * @version $Revision$
 */
class TedsClosureBug extends GroovyTestCase {
    
    void testBug() {
		f = new Feed()
		
		f.author = new Person(name:'Ted Leung',url:'http://www.sauria.com/blog', email:'twl@sauria.com')
		
		f.entries = [ new Entry(title:'one',summary:'first post'), new Entry(title:'two',summary:'the second post'), new Entry(title:'three', summary:'post the third'), new Entry(title:'four',summary:'the ponderous fourth post') ]
		
		//f.entries.each { println it.title }
		//println f.author.name
		
		xml = new MarkupBuilder()
		
		atom = xml.atom() {
		  title("Ted Leung off the air")
		  link("http://www.sauria.com/noblog")
		  author() {
		    person() {
		      name(f.author.name)
		      url(f.author.url)
		      email(f.author.email)
		    }
		  }
		  for (e in f.entries) {
		    entry() {
		    	/** @todo if use the non-existent title() method then a confusing exception is thrown */
		    	
		      title(e.title)
		      summary(e.summary)
		    }
		  }
		}
	}   
}


class Feed {
    @Property String title
    @Property String link
    @Property Person author
    @Property String tagline
    @Property String generator
    @Property String copyright
    @Property String modified
    @Property List entries
}

class Entry {
    @Property String title
    @Property String link
    @Property String id
    @Property String summary
    @Property String content
    @Property Person author
    @Property String created
    @Property String issued
    @Property String modified
}

class Person {
    @Property String name
    @Property String url
    @Property String email
}