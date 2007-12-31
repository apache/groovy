package groovy.grape

import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.cache.CacheManager
import org.apache.ivy.Ivy
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.core.module.descriptor.ModuleDescriptor
import org.apache.ivy.plugins.report.XmlReportParser
import org.apache.ivy.core.module.descriptor.Artifact
import org.codehaus.groovy.tools.RootLoader
import org.apache.ivy.util.MessageLogger
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message


/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Dec 21, 2007
 * Time: 8:15:35 PM
 * To change this template use File | Settings | File Templates.
 */
class GrapeIvy {

    static boolean enableGrapes = true
    //TODO do we set this to false, and let GroovyStarter turn it on?

    private static boolean isValidTargetClassLoader(def loader) {
        return (loader != null) &&
            (
             (loader.class.name == 'groovy.lang.GroovyClassLoader') ||
             (loader.class.name == 'groovy.lang.GroovyClassLoader$InnerLoader') || 
             (loader.class.name == 'org.codehaus.groovy.tools.RootLoader')
            )
    }

    public static void grab(HashMap attrs, Object[] args) {
        // first, check the kill switch
        if (!enableGrapes) { return }

        // next, find target classloader
        // if there is no valid classloader, fail before we check dependencies
        def loader = attrs.classLoader
        if (!isValidTargetClassLoader(loader)) {
            loader = attrs.refrenceObject?.class?.classLoader
            //loader = loader.class.classLoader
            while (loader && !isValidTargetClassLoader(loader)) {
                //println ' checking parent of refrence object'
                loader = loader.parent
            }
            if (!isValidTargetClassLoader(loader)) {
                loader = Thread.currentThread().contextClassLoader
                if (!isValidTargetClassLoader(loader)) {
                    loader = GrapeIvy.class.classLoader
                    if (!isValidTargetClassLoader(loader)) {
                        throw new RuntimeException("No suitable ClassLoader found for grab")
                    //} else {
                    //    println 'using GrapeIvy loader'
                    }
                //} else {
                //    println 'using context class loader'
                }
            //} else {
            //    println 'using refrence object loader'
            }
        //} else {
        //    println 'using explicit loader'
        }

        // we have a valid classloader, now get grab details
        String groupId = attrs.group ?: attrs.groupId ?: 'groovy'
        String module = attrs.module ?: attrs.artifactId
        String version = attrs.version //TODO accept ranges and decode them


        // start ivy
        Message.setDefaultLogger(new DefaultMessageLogger(-1))
        Ivy ivy = Ivy.newInstance()
        IvySettings settings = ivy.getSettings()
        settings.setVariable("ivy.default.configuration.m2compatible", "true")

        File groovyRoot =  new File("${System.getProperty('user.home')}${File.separator}.groovy")

        // configure settigns
        File grapeConfig = new File(groovyRoot, 'grapeConfig.xml')
        if (grapeConfig) {
            ivy.configure(grapeConfig);
        } else {
            ivy.configureDefault()
        }

        // set up the cache dirs
        File cache =  new File(groovyRoot, 'grapes')
        if (!cache.exists()) {
            cache.mkdirs()
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("$cache is not a directory")
        }
        CacheManager cacheManager = CacheManager.getInstance(settings, cache)

        // create temp ivy conf for grab dependency
        File ivyfile
        ivyfile = File.createTempFile("ivy", ".xml")
        ivyfile.deleteOnExit()
        ModuleDescriptor md = DefaultModuleDescriptor
                .newDefaultInstance(ModuleRevisionId.newInstance(
                    groupId,
                    module + "-caller",
                    "working"))
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(groupId, module, version), false, false, true)
        md.addDependency(dd)
        XmlModuleDescriptorWriter.write(md, ivyfile)

        // resolve grab and dependencies
        ResolveOptions resolveOptions = new ResolveOptions().setConfs(['default'] as String[]).setCache(
            cacheManager).setValidate(true).setUseOrigin(false)
        def report = ivy.resolve(ivyfile.toURL(), resolveOptions)
        if (report.hasError()) {
            throw new RuntimeException("Error grabbing Grapes -- $report.allProblemMessages")
        }
        md = report.getModuleDescriptor()

        // extract jars from resolution
        CacheManager cacheMgr = ivy.getCacheManager(cache)
        XmlReportParser parser = new XmlReportParser()
        String resolveId = ResolveOptions.getDefaultResolveId(md)
        report = cacheMgr.getConfigurationResolveReportInCache(resolveId, 'default')
        parser.parse(report)

        // add to classloader
        Artifact[] artifacts = parser.getArtifacts()
        artifacts.each { artifact ->
            // this is where we would likely process lifecycle events for loaded grapes
            loader.addURL(cacheMgr.getArchiveFileInCache(artifact).toURL())
        }
    }

    public static void main(String[] args) {
        // more impressive example, downlaodes dependencies
        grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL')
        println Thread.currentThread().contextClassLoader.loadClass('org.apache.poi.hssf.model.Sheet')
        println Class.forName('org.apache.poi.hssf.model.Sheet', true, GrapeIvy.classLoader)
        //println Class.forName('org.apache.poi.hssf.model.Sheet')
        // fails bedause MOP dispatches, not dorectly via bytecode, MOP isn't going away, but we could
        // look into overriding Class.forName(String) somehow...

        /* You will need the following as ~/.groovy/grapeConfig.xml
        <ivysettings>
          <conf defaultResolver="chained"/>
          <resolvers>
            <chain name="chained">
              <ibiblio name="java.net2" root="http://download.java.net/maven/2/" m2compatible="true"/>
              <ibiblio name="ibiblio"/>
            </chain>
          </resolvers>
        </ivysettings>
        */
        grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)', refrenceObject:this)
        //println (new com.jidesoft.swing.JideSplitButton())
        // requires compiled class and special classloader setup, to be addressed
        println Thread.currentThread().contextClassLoader.loadClass('com.jidesoft.swing.JideSplitButton')
        println Class.forName('com.jidesoft.swing.JideSplitButton', true, GrapeIvy.classLoader)
        //println Class.forName('com.jidesoft.swing.JideSplitButton')
        // fails bedause MOP dispatches, not dorectly via bytecode, not much going to change there.

    }
}
