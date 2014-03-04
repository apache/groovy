/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.text.markup;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A template engine which leverages {@link groovy.xml.StreamingMarkupBuilder} to generate XML/XHTML.
 *
 * @author Cedric Champeau
 */
public class MarkupTemplateEngine extends TemplateEngine {

    final static ClassNode MARKUPTEMPLATEENGINE_CLASSNODE = ClassHelper.make(MarkupTemplateEngine.class);
    private final static Pattern LOCALIZED_RESOURCE_PATTERN = Pattern.compile("(.+?)(_[a-z]{2}(?:_[A-Z]{2,3}))?\\.(\\p{Alnum}+)");

    private final static AtomicLong counter = new AtomicLong();

    private final TemplateGroovyClassLoader groovyClassLoader;
    private final CompilerConfiguration compilerConfiguration;
    private final TemplateConfiguration templateConfiguration;

    public MarkupTemplateEngine(final TemplateConfiguration tplConfig) {
        this(MarkupTemplateEngine.class.getClassLoader(), tplConfig);
    }

    public MarkupTemplateEngine(final ClassLoader parentLoader, final TemplateConfiguration tplConfig) {
        compilerConfiguration = new CompilerConfiguration();
        templateConfiguration = tplConfig;
        compilerConfiguration.addCompilationCustomizers(new TemplateASTTransformer(tplConfig));
        compilerConfiguration.addCompilationCustomizers(
                new ASTTransformationCustomizer(Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), CompileStatic.class));
        if (templateConfiguration.isAutoNewLine()) {
            compilerConfiguration.addCompilationCustomizers(
                    new CompilationCustomizer(CompilePhase.CONVERSION) {
                        @Override
                        public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
                            new AutoNewLineTransformer(source).visitClass(classNode);
                        }
                    }
            );
        }
        groovyClassLoader = AccessController.doPrivileged(new PrivilegedAction<TemplateGroovyClassLoader>() {
            public TemplateGroovyClassLoader run() {
                return new TemplateGroovyClassLoader(parentLoader, compilerConfiguration);
            }
        });
    }

    /**
     * Convenience constructor to build a template engine which searches for templates into a directory
     *
     * @param templateDirectory directory where to find templates
     * @param tplConfig         template engine configuration
     */
    public MarkupTemplateEngine(ClassLoader parentLoader, File templateDirectory, TemplateConfiguration tplConfig) {
        this(new URLClassLoader(buildURLs(templateDirectory), parentLoader), tplConfig);
    }

    private static URL[] buildURLs(final File templateDirectory) {
        try {
            return new URL[]{templateDirectory.toURI().toURL()};
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid directory", e);
        }
    }

    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, null);
    }

    public Template createTemplateByPath(final String templatePath) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), null);
    }

    public Template createTypeCheckedModelTemplate(final String source, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(new StringReader(source), modelTypes);
    }

    public Template createTypeCheckedModelTemplate(final Reader reader, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, modelTypes);
    }

    public Template createTypeCheckedModelTemplateByPath(final String templatePath, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), modelTypes);
    }

    @Override
    public Template createTemplate(final URL resource) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, null);
    }

    public Template createTypeCheckedModelTemplate(final URL resource, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, modelTypes);
    }

    public GroovyClassLoader getTemplateLoader() {
        return groovyClassLoader;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    public TemplateConfiguration getTemplateConfiguration() {
        return templateConfiguration;
    }

    URL resolveTemplate(String templatePath) throws IOException {
        MarkupTemplateEngine.TemplateResource templateResource = MarkupTemplateEngine.TemplateResource.parse(templatePath);
        String configurationLocale = templateConfiguration.getLocale().toString().replace("-", "_");
        URL resource = templateResource.hasLocale()?groovyClassLoader.getResource(templateResource.toString()):null;
        if (resource == null) {
            // no explicit locale in the template path or resource not found
            // fallback to the default configuration locale
            resource = groovyClassLoader.getResource(templateResource.withLocale(configurationLocale).toString());
        }
        if (resource == null) {
            // no resource found with the default locale, try without any locale
            resource = groovyClassLoader.getResource(templateResource.withLocale(null).toString());
        }
        if (resource == null) {
            throw new IOException("Unable to load template:" + templatePath);
        }
        return resource;
    }

    /**
     * Implements the {@link groovy.text.Template} interface by caching a compiled template script and keeping a
     * reference to the optional map of types of the model elements.
     */
    private class MarkupTemplateMaker implements Template {
        final Class<BaseTemplate> templateClass;
        final Map<String, String> modeltypes;

        @SuppressWarnings("unchecked")
        public MarkupTemplateMaker(final Reader reader, Map<String, String> modelTypes) {
            templateClass = groovyClassLoader.parseClass(new GroovyCodeSource(reader, "GeneratedMarkupTemplate" + counter.getAndIncrement(), ""), modelTypes);
            this.modeltypes = modelTypes;
        }

        @SuppressWarnings("unchecked")
        public MarkupTemplateMaker(final URL resource, Map<String, String> modelTypes) throws IOException {
            templateClass = groovyClassLoader.parseClass(new GroovyCodeSource(resource), modelTypes);
            this.modeltypes = modelTypes;
        }

        public Writable make() {
            return make(Collections.emptyMap());
        }

        public Writable make(final Map binding) {
            return DefaultGroovyMethods.newInstance(templateClass, new Object[]{MarkupTemplateEngine.this, binding, modeltypes, templateConfiguration});
        }
    }

    /**
     * A specialized GroovyClassLoader which will support passing values to the type checking extension thanks to a
     * thread local.
     */
    static class TemplateGroovyClassLoader extends GroovyClassLoader {
        final static ThreadLocal<Map<String, String>> modelTypes = new ThreadLocal<Map<String, String>>();

        public TemplateGroovyClassLoader(final ClassLoader parentLoader, final CompilerConfiguration compilerConfiguration) {
            super(parentLoader, compilerConfiguration);
        }

        public Class parseClass(final GroovyCodeSource codeSource, Map<String, String> hints) throws CompilationFailedException {
            modelTypes.set(hints);
            try {
                return super.parseClass(codeSource);
            } finally {
                modelTypes.set(null);
            }
        }
    }

    public static class TemplateResource {
        private final String baseName;
        private final String locale;
        private final String extension;

        public static TemplateResource parse(String fullPath) {
            Matcher matcher = LOCALIZED_RESOURCE_PATTERN.matcher(fullPath);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Illegal template path: " + fullPath);
            }
            return new TemplateResource(matcher.group(1), matcher.group(2), matcher.group(3));
        }

        private TemplateResource(final String baseName, final String locale, final String extension) {
            this.baseName = baseName;
            this.locale = locale;
            this.extension = extension;
        }

        public TemplateResource withLocale(String locale) {
            return new TemplateResource(baseName, locale, extension);
        }

        public String toString() {
            return baseName + (locale != null ? "_" + locale : "") + "." + extension;
        }

        public boolean hasLocale() {
            return locale!=null && !"".equals(locale);
        }
    }

}
