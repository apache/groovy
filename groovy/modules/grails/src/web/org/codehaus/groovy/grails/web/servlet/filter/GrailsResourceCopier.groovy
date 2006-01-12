package org.codehaus.groovy.grails.web.servlet.filter;
class GrailsResourceCopier implements Runnable {
    @Property basedir
    @Property destdir

    def ant = new AntBuilder()

    public void run() {
        ant.copy(todir:"tmp/war/WEB-INF/grails-app",failonerror:false) {
            fileset(dir:"grails-app",includes:"**")
        }
    }
}