package org.apache.groovy.gradle

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

@CompileStatic
class CheckstyleHtmlReport extends DefaultTask {
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection source = project.objects.fileCollection()

    @InputFile
    final RegularFileProperty configFile = project.objects.fileProperty()

    @InputFile
    final RegularFileProperty checkstyleReportFile = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()

    @TaskAction
    void generateHtmlReport() {
        def configFile = this.configFile.get().asFile
        def configDir = configFile.parentFile
        def reportFile = checkstyleReportFile.get().asFile
        def htmlReportFile = outputFile.get().asFile
        def templateConfiguration = new TemplateConfiguration()
        templateConfiguration.with {
            autoIndent = true
            autoNewLine = true
        }
        def engine = new MarkupTemplateEngine(this.class.classLoader, configDir, templateConfiguration)
        def xml = new XmlSlurper().parse(reportFile.newReader('utf-8'))
       def model = buildModel(xml)
        htmlReportFile.parentFile.mkdirs()
        htmlReportFile.withWriter('utf-8') { wrt ->
            engine.createTemplateByPath('checkstyle-report.groovy').make(model).writeTo(wrt)
        }
    }

    @CompileDynamic
    private Map<String, Object> buildModel(GPathResult xml) {
        def files = []
        xml.file.each { f ->
            if (f.error.size() && !f.@name.toString().matches('.*[/\\\\]generated[/\\\\].*')) {
                files << [
                        name: f.@name.toString(),
                        errors: f.error.collect { e ->
                            def rule = e.@source.toString()
                            rule = rule.substring(rule.lastIndexOf('.') + 1)
                            [line: e.@line.toString(),
                             column: e.@column.toString(),
                             message: e.@message.toString(),
                             source: rule,
                             severity: e.@severity.toString()]
                        }]
            }
        }
        def model = [
                project: project,
                files: files
        ]
        model
    }
}
