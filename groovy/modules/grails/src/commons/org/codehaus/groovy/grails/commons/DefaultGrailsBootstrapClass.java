package org.codehaus.groovy.grails.commons;

import javax.servlet.ServletContext;

import groovy.lang.Closure;

public class DefaultGrailsBootstrapClass extends AbstractGrailsClass implements GrailsBootstrapClass {

	
	public static final String BOOT_STRAP = "BootStrap";
	
	private static final String INIT_CLOSURE = "init";
	private static final String DESTROY_CLOSURE = "destroy";
	private static final Closure BLANK_CLOSURE = new Closure(DefaultGrailsBootstrapClass.class) {
		public Object call(Object[] args) {
			return null;
		}		
	};

	
	public DefaultGrailsBootstrapClass(Class clazz) {
		super(clazz, BOOT_STRAP);
	}

	public Closure getInitClosure() {
		Object obj = getReference().getPropertyValue(INIT_CLOSURE);
		if(obj instanceof Closure) {
			return (Closure)obj;
		}
		return BLANK_CLOSURE;
	}

	public Closure getDestroyClosure() {
		Object obj = getReference().getPropertyValue(DESTROY_CLOSURE);
		if(obj instanceof Closure) {
			return (Closure)obj;
		}
		return BLANK_CLOSURE;
	}

	public void callInit(ServletContext servletContext) {
		Closure init = getInitClosure();
		init.call( new Object[] { servletContext } );
	}

	public void callDestroy() {
		Closure destroy = getInitClosure();
		destroy.call();
	}
}
