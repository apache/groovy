/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.compiler.injection;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * This is substantially the same code from Grails, except some references de-referenced
 * and the macro class added.
 *
 * Default implementation of domain class injector interface that adds the 'id'
 * and 'version' properties and other previously boilerplate code
 *
 * @author Graeme Rocher
 *
 * @since 0.2
 *
 * Created: 20th June 2006
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class DefaultGrailsDomainClassInjector implements ASTTransformation {
        //GrailsDomainClassInjector {

    //private static final Log LOG  = LogFactory.getLog(DefaultGrailsDomainClassInjector.class);


    public void visit(ASTNode[] nodes, SourceUnit source) {
        performInjection((ClassNode) nodes[1]);
    }

    public void performInjection(ClassNode classNode) {
        if(shouldInject(classNode)) {
			injectIdProperty(classNode);

			injectVersionProperty(classNode);

			injectToStringMethod(classNode);

			injectAssociations(classNode);

        }
	}

    public boolean shouldInject(URL url) {
        return true; //return GrailsResourceUtils.isDomainClass(url);
    }

    private boolean shouldInject(ClassNode classNode) {
		//String fullName = GrailsASTUtils.getFullName(classNode);
		//String mappingFile = GrailsDomainConfigurationUtil.getMappingFileName(fullName);

		//if(getClass().getResource(mappingFile)!=null) {
			//if(LOG.isDebugEnabled()) {
				//LOG.debug("[GrailsDomainInjector] Mapping file ["+mappingFile+"] found. Skipping property injection.");
			//}
			//return false;
		//}
		return true;
	}

	private void injectAssociations(ClassNode classNode) {

		List properties = classNode.getProperties();
		List propertiesToAdd = new ArrayList();
		for (Iterator p = properties.iterator(); p.hasNext();) {
			PropertyNode pn = (PropertyNode) p.next();
			final boolean isHasManyProperty = pn.getName().equals(/*GrailsDomainClassProperty.*/RELATES_TO_MANY) || pn.getName().equals(/*GrailsDomainClassProperty.*/HAS_MANY);
			if(isHasManyProperty) {
				Expression e = pn.getInitialExpression();
				propertiesToAdd.addAll(createPropertiesForHasManyExpression(e,classNode));
			}
            final boolean isBelongsTo = pn.getName().equals(/*GrailsDomainClassProperty.*/BELONGS_TO);
            if(isBelongsTo) {
                Expression e = pn.getInitialExpression();
                propertiesToAdd.addAll(createPropertiesForBelongsToExpression(e,classNode));
            }
        }
		injectAssociationProperties(classNode,propertiesToAdd);
	}

    private Collection createPropertiesForBelongsToExpression(Expression e, ClassNode classNode)
    {
        List properties = new ArrayList();
        if(e instanceof MapExpression) {
            MapExpression me = (MapExpression)e;
            List mapEntries = me.getMapEntryExpressions();
            for (Iterator i = mapEntries.iterator(); i.hasNext();) {
                MapEntryExpression mme = (MapEntryExpression) i.next();
                String key = mme.getKeyExpression().getText();

                String type = mme.getValueExpression().getText();

                properties.add(new PropertyNode(key,Modifier.PUBLIC, ClassHelper.make(type) , classNode, null,null,null));
            }
        }

        return properties;
    }

    private void injectAssociationProperties(ClassNode classNode, List propertiesToAdd) {
		for (Iterator i = propertiesToAdd.iterator(); i.hasNext();) {
			PropertyNode pn = (PropertyNode) i.next();
			if(!/*GrailsASTUtils.*/hasProperty(classNode, pn.getName())) {
				//if(LOG.isDebugEnabled()) {
				//	LOG.debug("[GrailsDomainInjector] Adding property [" + pn.getName() + "] to class [" + classNode.getName() + "]");
				//}
				classNode.addProperty(pn);
			}
		}
	}

	private List createPropertiesForHasManyExpression(Expression e, ClassNode classNode) {
		List properties = new ArrayList();
		if(e instanceof MapExpression) {
			MapExpression me = (MapExpression)e;
			List mapEntries = me.getMapEntryExpressions();
			for (Iterator j = mapEntries.iterator(); j.hasNext();) {
				MapEntryExpression mee = (MapEntryExpression) j.next();
				Expression keyExpression = mee.getKeyExpression();
				String key = keyExpression.getText();
				addAssociationForKey(key,properties,classNode);
			}
		}
		return properties;
	}

	private void addAssociationForKey(String key, List properties, ClassNode classNode) {
			properties.add(new PropertyNode(key, Modifier.PUBLIC, new ClassNode(Set.class), classNode,null, null, null));
	}

	private void injectToStringMethod(ClassNode classNode) {
		final boolean hasToString = /*GrailsASTUtils.*/implementsZeroArgMethod(classNode, "toString");

		if(!hasToString) {
			GStringExpression ge = new GStringExpression(classNode.getName() + " : ${id}");
			ge.addString(new ConstantExpression(classNode.getName()+" : "));
			ge.addValue(new VariableExpression("id"));
			Statement s = new ReturnStatement(ge);
			MethodNode mn = new MethodNode("toString",Modifier.PUBLIC,new ClassNode(String.class), new Parameter[0],new ClassNode[0],s);
			//if(LOG.isDebugEnabled()) {
			//	LOG.debug("[GrailsDomainInjector] Adding method [toString()] to class [" + classNode.getName() + "]");
			//}
			classNode.addMethod(mn);
		}
	}

	private void injectVersionProperty(ClassNode classNode) {
		final boolean hasVersion = /*GrailsASTUtils.*/hasProperty(classNode, /*GrailsDomainClassProperty.*/VERSION);

		if(!hasVersion) {
			//if(LOG.isDebugEnabled()) {
			//	LOG.debug("[GrailsDomainInjector] Adding property [" + GrailsDomainClassProperty.VERSION + "] to class [" + classNode.getName() + "]");
			//}
			classNode.addProperty( /*GrailsDomainClassProperty.*/VERSION, Modifier.PUBLIC, new ClassNode(Long.class), null, null, null);
		}
	}

	private void injectIdProperty(ClassNode classNode) {
		final boolean hasId = /*GrailsASTUtils.*/hasProperty(classNode,/*GrailsDomainClassProperty.*/IDENTITY);

		if(!hasId) {
			//if(LOG.isDebugEnabled()) {
			//	LOG.debug("[GrailsDomainInjector] Adding property [" + GrailsDomainClassProperty.IDENTITY + "] to class [" + classNode.getName() + "]");
			//}
			classNode.addProperty( /*GrailsDomainClassProperty.*/IDENTITY, Modifier.PUBLIC, new ClassNode(Long.class), null, null, null);
		}
	}


    //***************************************************************
    // from GrailsASTUtils
    //***************************************************************
    /**
	 * Returns whether a classNode has the specified property or not
	 *
	 * @param classNode The ClassNode
	 * @param propertyName The name of the property
	 * @return True if the property exists in the ClassNode
	 */
	public static boolean hasProperty(ClassNode classNode, String propertyName) {
		if(classNode == null || propertyName == null || "".equals(propertyName.trim()))
			return false;

		List properties = classNode.getProperties();
		for (Iterator i = properties.iterator(); i.hasNext();) {
			PropertyNode pn = (PropertyNode) i.next();
			if(pn.getName().equals(propertyName))
				return true;
		}
		return false;
	}

	/**
	 * Tests whether the ClasNode implements the specified method name
	 *
	 * @param classNode The ClassNode
	 * @param methodName The method name
	 * @return True if it does implement the method
	 */
	public static boolean implementsZeroArgMethod(ClassNode classNode, String methodName) {
		return implementsMethod(classNode, methodName, new Class[0]);
	}


    /**
     * Tests whether the ClassNode implements the specified method name
     *
     * @param classNode The ClassNode
     * @param methodName The method name
     * @param argTypes
     * @return True if it implements the method
     */
    private static boolean implementsMethod(ClassNode classNode, String methodName, Class[] argTypes) {
        List methods = classNode.getMethods();
        for (Iterator i = methods.iterator(); i.hasNext();) {
            MethodNode mn = (MethodNode) i.next();
            final boolean isZeroArg = (argTypes == null || argTypes.length ==0);
            boolean methodMatch = mn.getName().equals(methodName) && isZeroArg;
            if(methodMatch)return true;
            // TODO Implement further parameter analysis
        }
        return false;
    }

    //***************************************************************
    // from GrailsDomainClassProperty
    //***************************************************************
    private static final String RELATES_TO_MANY = "relatesToMany";
    private static final String BELONGS_TO = "belongsTo";
    private static final String HAS_MANY = "hasMany";
    private static final String IDENTITY = "id";
    private static final String VERSION = "version";
}
