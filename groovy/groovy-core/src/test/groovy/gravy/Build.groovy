package groovy.gravy


/**
 * Represents a build process
 */
class Build implements Runnable {
	ant = new AntBuilder()
	args
	pom
	defaultTargets = ['clean', 'compile']
	
	static void main(args) {
		// autogenerate this
		b = new Build(args)
		//b.args = args
		b.run()
	}
	
	Build(someArgs) {
		args = someArgs;
		if (args instanceof String) {
			args = [args]
		}
		/*
		if (args == null || args.size() == 0) {
			args = defaultTargets
		}
		*/
	}
	
	void run() {
		for (a in args) {
			println "Target: ${a}"
			invokeMethod(a.toString(), null)
		}
	}
	
	getPom() {
		if (pom == null) {
			pom = parsePOM()
		}
		return pom
	}


	// Default goals
	clean() {
		ant.rmdir(dir:'gravy')
	}
	
	compile() {
		ant.mkdir(dir:'gravy/classes')
		ant.compile(srdir:'src/main/java', destdir:'gravy/classes') {
			fileset {
				includes(name:'**/*.java')
			}
		}
	}
		
	protected parsePOM() {
		return new XmlParser().parse("project.xml")
	}
}