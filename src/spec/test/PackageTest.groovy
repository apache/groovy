class PackageTest extends GroovyTestCase
{
	void testPackages() {
        assertScript '''
			// tag::package_statement[]
			//Defines a package named com.yoursite.com
			package com.yoursite.com
			// end::package_statement[]

			class Foo
			{
				
			}
			
			def foo = new Foo()		
			
			assert foo != null	
			'''
			
		assertScript '''
			//tag::import_statement[]
			//imports the class MarkupBuilder
			import groovy.xml.MarkupBuilder
			
			//uses the imported class to create an object
			def xml = new MarkupBuilder( )
			
			assert xml != null
			// end::import_statement[]
			
		'''
	}
	
	void testDefaultImports()
	{
		assertScript '''
			//tag:default_import[]
			new Date()
			//end:default_import[]
		'''
	}
	
	void testMultipleImportsFromSamePackage()
	{
		assertScript '''
			
			//tag:multiple_import[]
			import groovy.xml.MarkupBuilder
			import groovy.xml.StreamingMarkupBuilder
			
			def markupBuilder = new MarkupBuilder( )
			
			assert markupBuilder != null
			
			assert new StreamingMarkupBuilder() != null 
			//end:multiple_import[]
			
		'''
	}
	
	void testStarImports()
	{
		assertScript '''
			//tag:star_import[]
			import groovy.xml.*
			
			def markupBuilder = new MarkupBuilder( )
			
			assert markupBuilder != null
			
			assert new StreamingMarkupBuilder() != null
			
			//end:star_import[]
		'''
	}
	
	/*
	void testMakeItTwice()
	{
		assertScript '''
			//tag:type_aliasing[]
			
			package com.lib
			
			public class MultiplyTwo
			{
				def multiplay(def value)
				{
					return value * 3 //intentionally wrong.
				}
			}
			
			assert 4 != new MultiplyTwo().multiplay(2)
			
			//end:type_aliasing[]
		'''
	}*/
	
	void testStaticImports()
	{
		assertScript '''
			
			//tag:static_imports[]
			
			import static Boolean.FALSE
			
			assert !FALSE //use directly, without Boolean prefix!
			
			//end:static_imports[]
			
		'''
	}
	
	void testStaticImportWithAs()
	{
		assertScript '''
			
			//tag:static_importswithas[]
			
			import static Calendar.getInstance as now
			
			assert now().class == Calendar.getInstance().class
			
			//end:static_importswithas[]
			
		'''
	}
	
	void testStaticStarImport()
	{
		assertScript '''
			//tag:static_importswithstar[]
			
			import static java.lang.Math.*
			
			assert sin(0) == 0.0
			assert cos(0) == 1.0
			
			//end:static_importswithstar[]
		'''
	}
	
	void testThirdLib()
	{
		assertScript '''
			import thirdpartylib.MultiplyTwo
			
			//tag:using_thrid_party_lib[]
			def result = new MultiplyTwo().multiplay(2)
			//end:using_thrid_party_lib[]
		
			assert 4 != new MultiplyTwo().multiplay(2)
		'''
	}
	
	void testFixThirdLib()
	{
		assertScript '''
			
			//tag:fixing_thrid_party_lib[]
			
			import thirdpartylib.MultiplyTwo as OrigMultiplyTwo
			class MultiplyTwo extends OrigMultiplyTwo {
				def multiplay(def value)
				{
					return value * 2 //corrected here
				}
			}
			// nothing changes below here
			def multiplylib = new MultiplyTwo()

			//assert passes as well
			assert 4 == new MultiplyTwo().multiplay(2)
			//end:fixing_thrid_party_lib[]
		'''
	}
	
}

