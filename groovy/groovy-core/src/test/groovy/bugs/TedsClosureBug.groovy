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
		
		println f.author.name
		
		xml = new MarkupBuilder()
		
		a = xml.atom() {
		  title("Ted Leung off the air")
		  link("http://www.sauria.com/noblog")
		  author() {
		    person() {
		      name(f.author.name)
		      url(f.author.url)
		      email(f.author.email)
		    }
		  }
		  
		  /** @todo fixme! 
		  for (e in f.entries) {
		    entry() {
		      title(e.title())
		      summary(e.summary())
		    }
		  }
		  */
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
