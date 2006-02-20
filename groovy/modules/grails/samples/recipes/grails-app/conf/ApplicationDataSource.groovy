import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration

class ApplicationDataSource {
   @Property configClass = GrailsAnnotationConfiguration.class
   @Property boolean pooled = true
   @Property String dbCreate = "create-drop" // one of 'create', 'create-drop','update'
   @Property String url = "jdbc:hsqldb:mem:testDB"
   @Property String driverClassName = "org.hsqldb.jdbcDriver"
   @Property String username = "sa"
   @Property String password = ""
}
