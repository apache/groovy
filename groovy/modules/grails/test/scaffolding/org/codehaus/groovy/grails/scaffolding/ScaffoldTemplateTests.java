package org.codehaus.groovy.grails.scaffolding;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.lang.IntRange;
import junit.framework.TestCase;
import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.io.File;
import java.util.*;

public class ScaffoldTemplateTests  extends TestCase {

    public void testStringTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.lang.String.template"));
        Map binding = new HashMap();

        binding.put("name", "testName");
        binding.put("value", "testValue");
        binding.put("constraints", null);

        Document xml = DocumentHelper.parseText(template.make(binding).toString());


        assertEquals("textarea",xml.getRootElement().getName());
        assertEquals("testName",xml.getRootElement().attributeValue("name"));
        assertEquals("testValue",xml.getRootElement().getText());

        // test with a length restriction hence becoming an input field
        ConstrainedProperty cp = new ConstrainedProperty(Object.class,"testName",String.class);
        cp.setMaxLength(50);
        cp.setEditable(false);
        // test with a custom attribute
        Map attributes = new HashMap() ;
        attributes.put("class", "MyClass");
        cp.setAttributes(attributes );
        binding.put("constraints",cp);

        System.out.println(template.make(binding).toString());
        xml = DocumentHelper.parseText(template.make(binding).toString());

        assertEquals("input",xml.getRootElement().getName());
        assertEquals("text",xml.getRootElement().attributeValue("type"));
        assertEquals("50",xml.getRootElement().attributeValue("maxlength"));
        assertEquals("testName",xml.getRootElement().attributeValue("name"));
        assertEquals("testValue",xml.getRootElement().attributeValue("value"));
        assertEquals("MyClass",xml.getRootElement().attributeValue("class"));
        assertEquals("readonly",xml.getRootElement().attributeValue("readonly"));
    }

    public void testBooleanTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.lang.Boolean.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testBoolean");
        binding.put("value", new Boolean(true));
        binding.put("constraints", null);

        Document xml = DocumentHelper.parseText(template.make(binding).toString());

        assertEquals("input",xml.getRootElement().getName());
        assertEquals("checkbox",xml.getRootElement().attributeValue("type"));
        assertEquals("testBoolean",xml.getRootElement().attributeValue("name"));
        assertEquals("true",xml.getRootElement().attributeValue("value"));
        assertEquals("checked",xml.getRootElement().attributeValue("checked"));

        binding.put("value", new Boolean(false));
        xml = DocumentHelper.parseText(template.make(binding).toString());

        assertEquals("input",xml.getRootElement().getName());
        assertEquals("checkbox",xml.getRootElement().attributeValue("type"));
        assertEquals("testBoolean",xml.getRootElement().attributeValue("name"));
        assertEquals("false",xml.getRootElement().attributeValue("value"));
        assertEquals(null,xml.getRootElement().attributeValue("checked"));
    }

    public void testDateTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.util.Date.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testDate");
        binding.put("year", new Integer(2006));
        binding.put("month", new Integer(2));
        binding.put("day", new Integer(5));
        binding.put("hour", "12");
        binding.put("minute", "45");
        binding.put("constraints", null);


        Document xml = DocumentHelper.parseText(template.make(binding).toString());
    }

    public void testLocaleTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.util.Locale.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testLocale");
        binding.put("value", Locale.getDefault());
        binding.put("constraints", null);

        System.out.println(template.make(binding).toString());
        Document xml = DocumentHelper.parseText(template.make(binding).toString());
    }

    public void testTimeZoneTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.util.TimeZone.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testTimeZone");
        binding.put("value", TimeZone.getDefault());
        binding.put("constraints", null);

        System.out.println(template.make(binding).toString());
    }

    public void testCurrencyTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.util.Currency.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testCurrency");
        binding.put("value", Currency.getInstance(Locale.getDefault()));
        binding.put("constraints", null);

        System.out.println(template.make(binding).toString());
    }

    public void testNumberTemplate() throws Exception {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();

        Template template = templateEngine.createTemplate(new File("src/grails/templates/grails-app/views/scaffolding/java.lang.Number.gsp"));
        Map binding = new HashMap();

        binding.put("name", "testNumber");
        binding.put("value", new Integer(50));
        binding.put("constraints", null);
        System.out.println(template.make(binding).toString());
        Document xml = DocumentHelper.parseText(template.make(binding).toString());
        assertEquals("input",xml.getRootElement().getName());
        ConstrainedProperty cp = new ConstrainedProperty(Object.class, "testNumber", Integer.class);
        cp.setRange(new IntRange(25,125));
        binding.put("constraints", cp);
        
        xml = DocumentHelper.parseText(template.make(binding).toString());
        assertEquals("select",xml.getRootElement().getName());
        System.out.println(template.make(binding).toString());
    }
}
