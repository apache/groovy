/*
 * Created on May 7, 2004
 *
 */
package groovy.text;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;


/**
 * @author tug@wilson.co.uk
 *
 */
public class GStringTemplateEngine extends TemplateEngine {
	/* (non-Javadoc)
	 * @see groovy.text.TemplateEngine#createTemplate(java.io.Reader)
	 */
	public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
		return new GStringTemplate(reader);
	}
	
	private static class GStringTemplate implements Template {
		final Closure template;
		
		/**
		 * Turn the template into a writable Closure
		 * When executed the closure evaluates all the code embedded in the 
		 * template and then writes a GString containing the fixed and variable items 
		 * to the writer passed as a paramater
		 * 
		 * For example:
		 * 
		 * '<%= "test" %> of expr and <% test = 1 %>${test} script.'
		 * 
		 * would compile into:
		 * 
		 * { |out| $var1 = ( "test" ); test = 1 ; $var2 = (test); out << "${$var1} of expr and ${$var2} script."}.asWritable()
		 * 
		 * @param reader
		 * @throws CompilationFailedException
		 * @throws ClassNotFoundException
		 * @throws IOException
		 */
		public GStringTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
			final StringBuffer templateExpressions = new StringBuffer("class C { getTemplate() { { |out| ");	
			final StringBuffer templateGString = new StringBuffer("out << \"");
			int varNumber = 1;
        
	        while(true) {
	        	int c = reader.read();
	        	
	            if (c == '<') {
	                c = reader.read();
	                
	                if (c == '%') {
		                c = reader.read();
		                
		                if (c == '=') {
		                		parseExpression(reader, varNumber++, templateExpressions, templateGString);
		                		continue;
		                } else {
		                		parseSection(reader, templateExpressions);
		                		continue;
		                }
	                } else {
	                		templateGString.append('<');
	                }
	            } else if (c == '$') {
	                c = reader.read();
	                
	                if (c == '{') {
	                		parseGStringEpression(reader, varNumber++, templateExpressions, templateGString);
	                		continue;
	                } else {
	                		templateGString.append('$');
	                }
	            }
	            
            		if (c == '"') {
            			templateGString.append('\\');
            		} if (c == -1) {
            			break;
            		}
            		
            		templateGString.append((char)c);
	        }
	        
	        templateExpressions.append(templateGString).append("\"}.asWritable()}}");
	        
//	        System.out.println(templateExpressions.toString());
	        
	        final ClassLoader parentLoader = getClass().getClassLoader();
	        final GroovyClassLoader loader = 
	        	(GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
	        		public Object run() {
	        			return new GroovyClassLoader(parentLoader); 
	        		}
	        	});
	        final Class groovyClass = loader.parseClass(new GroovyCodeSource(templateExpressions.toString(), "C", "x"));

			try {
				final GroovyObject object = (GroovyObject) groovyClass.newInstance();
				
				this.template = (Closure)object.invokeMethod("getTemplate", null);
			} catch (InstantiationException e) {
				throw new ClassNotFoundException(e.getMessage());
			} catch (IllegalAccessException e) {
				throw new ClassNotFoundException(e.getMessage());
			}
		}
		
		/**
		 * Parse a <% .... %> section
		 * make the code in the section a statement at the begining of the closure
		 * 
		 * @param reader
		 * @param templateExpressions
		 * @throws IOException
		 */
		private static void parseSection(final Reader reader,
				                         final StringBuffer templateExpressions)
			throws IOException
		{
	    		while (true) {
	    			int c = reader.read();
	    			
	    			if (c == -1) break;
	    			
	    			if (c =='%') {
	        			c = reader.read();
	    				
	        			if (c == '>') break;
	    			}
	    			
	    			templateExpressions.append((char)c);
	    		}
	    		
	    		templateExpressions.append("; ");		                		
		}
		
		/**
		 * Parse a <%= .... %> expression
		 * make the expression the RHS of an assignment at the begining of the closure
		 * embed the variable on the LHS of the expression in the GString
		 * 
		 * @param reader
		 * @param varNumber
		 * @param templateExpressions
		 * @param templateGString
		 * @throws IOException
		 */
		private static void parseExpression(final Reader reader,
											final int varNumber,
											final StringBuffer templateExpressions,
											final StringBuffer templateGString)
			throws IOException
		{
	    		templateGString.append("${$var" + varNumber).append("}");
	    		
	    		templateExpressions.append("$var" + varNumber).append(" = (");
	    		
	    		while (true) {
	    			int c = reader.read();
	    			
	    			if (c == -1) break;
	    			
	    			if (c =='%') {
	        			c = reader.read();
	    				
	        			if (c == '>') break;
	    			}
	    			
	    			templateExpressions.append((char)c);
	    		}
	    		
	    		templateExpressions.append("); ");
		}
		
		/**
		 * Parse a ${ .... } expression
		 * make the expression the RHS of an assignment at the begining of the closure
		 * embed the variable on the LHS of the expression in the GString
		 * 
		 * @param reader
		 * @param varNumber
		 * @param templateExpressions
		 * @param templateGString
		 * @throws IOException
		 */
		private static void parseGStringEpression(final Reader reader,
												  final int varNumber,
												  final StringBuffer templateExpressions,
												  final StringBuffer templateGString)
		throws IOException
		{
	    		templateGString.append("${$var" + varNumber).append("}");
	    		
	    		templateExpressions.append("$var" + varNumber).append(" = (");
	    		
	    		int nestDepth = 0;
	    		
	    		while (true) {
	    			int c = reader.read();
	    			
	    			if (c == -1) break;
	    			
	    			if (c == '}' && nestDepth-- == 0) break;
	    			
					if (c == '{') nestDepth++;
					
					templateExpressions.append((char)c);
	    		}
	    		
	    		templateExpressions.append("); ");		                		
		}
		
		/* (non-Javadoc)
		 * @see groovy.text.Template#setBinding(java.util.Map)
		 */
		public void setBinding(Map map) {
			((Closure)this.template).setDelegate(new Binding(map));
		}
		
		/* (non-Javadoc)
		 * @see groovy.lang.Writable#writeTo(java.io.Writer)
		 */
		public Writer writeTo(final Writer writer) throws IOException {
			this.template.call(new Object[] {new PrintWriter(writer)});
			
			return writer;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
		final StringWriter stringWriter = new StringWriter();
		
            try {
                writeTo(stringWriter);
                
                return stringWriter.toString();
            } catch (final IOException e) {
                return e.toString();
            }
		}
	}
}
