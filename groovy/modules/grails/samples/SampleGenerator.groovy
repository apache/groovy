// start from inside samples dir

// consts in binding
CMD      = 'cmd /c '            // todo: make this os-aware
APP_NAME = 'myapp'
BO_NAME  = 'mydomain'
ANT      = new AntBuilder()


ANT.delete(dir:APP_NAME, quiet:true)

console('grails create-app', APP_NAME)

appConsole('grails create-domain-class', BO_NAME)

// do something to domain class here...

appConsole('grails generate-all', BO_NAME)

// unit testing, building etc. here?

appConsole('grails create-webtest', '')

appConsole('grails generate-webtest', BO_NAME)

// start the jetty server as external process
// or rely on tomcat hot deployment

appConsole('grails run-webtest', BO_NAME)

// --------- implementation methods -----------

void appConsole(String command, String input){
    console "cd $APP_NAME && " + command , input
}

void console(String command, String input) {
    def proc = (CMD + command).execute()

    Thread.start { System.out  << proc.in  }
    Thread.start { System.err  << proc.err }
    
    proc << input  + "\n" 
    
    proc.out.close()
    proc.waitForOrKill(0)
}