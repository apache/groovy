import java.util.Arrays

public class test { 

    static void main(args) {
        println("Called main with ${args}")
        listFiles(Arrays.asList(args))
    }
    
    static String getPath(file) {
        return file.absolutePath
    }

    static void listFiles(dirs) {
        println("called with ${dirs}")
        
        for(dir in dirs) {
            println("dir: ${dir}")
            
	        f = new java.io.File(dir)

    	    for (f in f.listFiles()) {
        	    println(getPath(f))
        	} 
        }
    }
}