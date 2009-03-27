package groovy.bugs

public class Groovy4230Bug extends GroovyTestCase {
  void testMe () {
new GroovyShell().evaluate """
import groovy.grape.*
import javax.persistence.*
import org.hibernate.cfg.*
import org.hibernate.annotations.LazyToOne
import org.hibernate.annotations.LazyToOneOption
import org.hibernate.annotations.Proxy



@Grab(group='org.hibernate', module='hibernate-commons-annotations', version='3.3.0.ga')
@Grab(group='org.hibernate', module='hibernate-annotations', version='3.3.0.ga')
@Grab(group='org.hibernate', module='hibernate-core', version='3.3.1.GA')
@Grab(group='hsqldb', module='hsqldb', version='1.8.0.5')
@Grab(group='antlr', module='antlr', version='2.7.6')
@Grab(group='javassist', module='javassist', version='3.3')
@Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.5.6')
@Grab(group='org.slf4j', module='slf4j-nop', version='1.5.6')


/**
 * @author Graeme Rocher
 * @since 1.0
 *
 * Created: Apr 8, 2008
 */

@Entity
@Proxy(proxyClass=ContentRevision)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE )
class ContentRevision implements Serializable {
	@Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    Long id

	@ManyToOne(fetch=FetchType.LAZY)
	@LazyToOne(value=LazyToOneOption.PROXY)
	Content content
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE )
@Proxy(proxyClass=Content)
class Content implements Serializable {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    Long id

    Date dateCreated = new Date()

	@OneToMany(cascade=CascadeType.ALL, mappedBy="content")
	List<ContentRevision> revisions = new ArrayList()
}

@Entity
@Proxy(proxyClass=ArticleRevision)
class ArticleRevision extends ContentRevision {
    String title
    String body
}

@Entity
@Proxy(proxyClass=Article)
class Article extends Content {
	String author
}

    ExpandoMetaClass.enableGlobally()

    config = new AnnotationConfiguration()
    config.addAnnotatedClass Content
    config.addAnnotatedClass ContentRevision
    config.addAnnotatedClass Article
    config.addAnnotatedClass ArticleRevision


    config.properties  = ['hibernate.connection.driver_class':'org.hsqldb.jdbcDriver',
                          'hibernate.connection.username': 'sa',
                          'hibernate.connection.password': '',
                          'hibernate.show_sql': 'true',
                          'hibernate.hbm2ddl.auto': 'create',
                          'hibernate.connection.url':'jdbc:hsqldb:mem:devDB']
    config.buildMappings()
    sessionFactory = config.buildSessionFactory()

    session = sessionFactory.openSession()

    try {
        article = new Article(author:'author1')
        article.revisions << new ArticleRevision(title:'title1', body:'body1', content:article)
        session.save(article)

        article = new Article(author:'author2')
        article.revisions << new ArticleRevision(title:'title2', body:'body2', content:article)
        article.revisions << new ArticleRevision(title:'title3', body:'body3', content:article)
        session.save(article)
        session.flush()

        session.clear()

        def revisions = session.createCriteria(ArticleRevision).list()

        for(revision in revisions) {
            println "author = \${revision.content.author}"
        }

    }
    catch(e) {
        e.printStackTrace()
        println e.message
        throw e

    }
"""
  }
}
