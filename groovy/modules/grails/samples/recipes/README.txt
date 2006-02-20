Hibernate Example
-----------------

WARNING: This example requires Java 5.0

This example demonstrates how a class can be mapped with Hibernate and still be treated as a normal Grails domain class. Including the ability to:

- Scaffold the domain class
- Validate Against constraints
- Access dynamic methods

The domain class in question can be found in "src/java/com/recipes/Recipe.java" and is mapped using the Hibernate annotation support.

In order for this to work the Grails data source has been configured to use a special GrailsAnnotationConfiguration instance at the line:

   @Property configClass = GrailsAnnotationConfiguration.class

This is only necessary if you use Hibernate annotations, with normal XML Hibernate mapping no extra configuration is needed.

The only remaining thing to be done is the "hibernate.cfg.xml" file is placed within the "hibernate" directory and contains a mapping for the class:


<hibernate-configuration>
    <session-factory>      
        <mapping package="com.recipes" />
        <mapping class="com.recipes.Recipe" />
    </session-factory>
</hibernate-configuration>

To run the example after installing Grails type:

grails init
grails run-app

And then in your browser go to:

http://localhost:8080/recipes/recipe/list
