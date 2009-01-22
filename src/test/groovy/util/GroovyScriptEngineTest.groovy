/*
	@author Chuck Tassoni
*/
package groovy.util

import org.codehaus.groovy.control.CompilerConfiguration;

class GroovyScriptEngineTest extends GroovyTestCase {

	private File currentDir
	private File srcDir;
	private File script
	private File com
	private File company
	private File util
	private File makeMeSuper
	private File makeMe
	private File helperIntf
	private File helper

    /**
    * Here we have inheritance and delegation-- where the delegate implements an
    * interface-- all used by a dynamically instantiated class named 'MakeMe'.  
    */
	public void setUp(){
		locateCurrentDir();
		srcDir = new File(currentDir, 'dynamicSrcRootToBeDeleted')
		srcDir.mkdir();
		
		script = new File(srcDir, 'script.groovy')
		script << """
		    def obj = dynaInstantiate.instantiate(className, getClass().getClassLoader())
		    obj.modifyWidth(dim, addThis)
		    returnedMessage = obj.message
		"""
		
		com = new File(srcDir, 'com')
		com.mkdir()
		company = new File(com, 'company')
		company.mkdir()
		
		makeMeSuper = new File(company, "MakeMeSuper.groovy")
		makeMeSuper << """
		    package com.company
		    import com.company.util.*
		    class MakeMeSuper{
		       private HelperIntf helper = new Helper()
		       def getMessage(){
		       		helper.getMessage()
		       }
		    }    
		 """
		
		makeMe = new File(company, "MakeMe.groovy")
		makeMe << """
		    package com.company

		    class MakeMe extends MakeMeSuper{
		       def modifyWidth(dim, addThis){
		          dim.width += addThis
		       }
		    }    
		 """
		 
		 util = new File(company, 'util')
		 util.mkdir()
		 
		 helperIntf = new File(util, "HelperIntf.groovy")
		 helperIntf << """
		    package com.company.util
		    interface HelperIntf{
		       public String getMessage();
		    }    
		 """
		 
		 helper = new File(util, "Helper.groovy")
		 helper << """
		    package com.company.util
		    class Helper implements HelperIntf{
		       public String getMessage(){
		       	  'worked'
		       }
		    }    
		 """
	}
	
	public void tearDown(){
	    try{
	    	helperIntf.delete()
	    	helper.delete()
	    	util.delete()
	    	makeMeSuper.delete()
			makeMe.delete()
 			company.delete()
 			com.delete()
 			script.delete()
 			srcDir.delete()
 		}catch(Exception ex){
 			throw new RuntimeException("Could not delete entire dynamic tree inside " + currentDir, ex)
 		}
	}

    public void testDynamicInstantiation() throws Exception{
		//Code run in the script will modify this dimension object.
    	MyDimension dim = new MyDimension();
    	
    	String[] roots = new String[1]
    	roots[0] = srcDir.getAbsolutePath()
    	GroovyScriptEngine gse = new GroovyScriptEngine(roots);
    	Binding binding = new Binding();
    	binding.setVariable("dim", dim);
    	binding.setVariable("dynaInstantiate", this);
    	
    	binding.setVariable("className", "com.company.MakeMe");
    	
    	int addThis = 3;
    	binding.setVariable("addThis", addThis);
    	
    	gse.run("script.groovy", binding);
    	
    	//The script instantiated com.company.MakeMe via our own
    	//instantiate method.  The instantiated object modified the
    	//width of our Dimension object, adding the value of our
    	//'addThis' variable to it.
    	assertEquals(new MyDimension(addThis, 0), dim);
    	
    	assertEquals('worked', binding.getVariable("returnedMessage") )
	}

    /**
     * Test for GROOVY-3281, to ensure details passed through CompilerConfiguration are inherited by GSE.
     */
    void testCompilerConfigurationInheritance() {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.scriptBaseClass = CustomBaseClass.name

        GroovyClassLoader cl = new GroovyClassLoader(this.class.getClassLoader(), cc)
        GroovyScriptEngine engine = new GroovyScriptEngine("src/test/groovy/util", cl)
        def aScript = engine.createScript("groovyScriptEngineSampleScript.groovy", new Binding())

        assert aScript instanceof CustomBaseClass
    }

	/*
	 * The script passes the className of the class it's supposed to
	 * instantiate to this method, expecting a newly instantiated object
	 * in return.  The reason this is not done in the script is that
	 * we want to ensure that no unforeseen problems occur if 
	 * the instantiation is not actually done inside the script,
	 * since real-world usages will likely require delegating that
	 * job.
	 */
	public Object instantiate(String className, ClassLoader classLoader){
		Class clazz = null;
		try {
			clazz = Class.forName(className, true, classLoader) ;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class.forName failed for  " + className, ex);
		}
		try {
			return clazz.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Could not instantiate object of class " + className, ex);
		}		
		
	}
	
	private void locateCurrentDir(){
		String bogusFile = "bogusFile";
	   	File f = new File(bogusFile);
	   	String path = f.getAbsolutePath();
	   	path = path.substring(0, path.length() - bogusFile.length());
	   	currentDir = new File(path);
	}

}

class MyDimension {
    int width
    int height
    
    MyDimension(int x, int y) {
        width = x
        height = y
    }

    MyDimension() {
        width = 0
        height = 0
    }

    boolean equals(o) { o.width == width && o.height == height }

    int hashCode() { width + 13 * height }
}

abstract class CustomBaseClass extends Script {}