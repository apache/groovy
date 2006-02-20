package org.codehaus.groovy.grails.web.servlet.filter;
class GrailsResourceCopier implements ResourceCopier {

    @Property String basedir = "."
    @Property String destdir = "./tmp/war"

    def ant = new AntBuilder()

    public void copyGrailsApp() {
        if(new File("${basedir}/grails-app").exists()) {
            ant.copy(todir:"${destdir}/WEB-INF/grails-app",failonerror:false) {
                fileset(dir:"${basedir}/grails-app",includes:"**")
            }
            ant.copy(todir:"${destdir}/WEB-INF/classes",failonerror:false) {
                fileset(dir:"${basedir}/grails-app/i18n",includes:"**")
            }
        }
    }

    public void copyViews(boolean shouldOverwrite) {
        if(new File("${basedir}/grails-app/views").exists()) {
            ant.copy(todir:"${destdir}/WEB-INF/grails-app/views",failonerror:false,overwrite:shouldOverwrite) {
                fileset(dir:"${basedir}/grails-app/views",includes:"**")
            }
        }
    }
}