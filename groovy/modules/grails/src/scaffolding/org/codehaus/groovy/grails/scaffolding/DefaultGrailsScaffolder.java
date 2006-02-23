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
package org.codehaus.groovy.grails.scaffolding;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.grails.scaffolding.exceptions.ScaffoldingException;
import org.codehaus.groovy.grails.web.metaclass.ChainDynamicMethod;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
import org.codehaus.groovy.grails.web.metaclass.RedirectDynamicMethod;
import org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
/**
 * The default implementation of scaffolding for Grails domain class and controller
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public class DefaultGrailsScaffolder implements GrailsScaffolder {

    private static final String INDEX_ACTION = "index";
    private static final String LIST_ACTION = "list";
	private static final String SHOW_ACTION = "show";
	private static final String EDIT_ACTION = "edit";		
	private static final String DELETE_ACTION = "delete";
	private static final String CREATE_ACTION = "create";
	private static final String SAVE_ACTION = "save";			
	private static final String UPDATE_ACTION = "update";	
	
	// TODO: Implement search scaffolding
	private static final String SEARCH_ACTION = "search";
	private static final String FIND_ACTION = "find";	
	
	

	private ScaffoldRequestHandler scaffoldRequestHandler;
	private ScaffoldResponseHandlerFactory scaffoldResponseHandlerFactory;

	/**
	 * Abstract base class that extends closure and retrieves the necessary arguments from the controller
	 * This is used to inject closure properties into controllers so controller actions appear as if by magic.
	 */
	static abstract class AbstractAction extends Closure {
		protected GroovyObject controller;
		protected HttpServletRequest request;
		protected HttpServletResponse response;
		protected ScaffoldRequestHandler scaffoldRequestHandler;
		protected ScaffoldResponseHandlerFactory scaffoldResponseFactory;
		protected ScaffoldResponseHandler scaffoldResponseHandler;
		
		public AbstractAction(Object owner) {
			super(owner);
			controller = (GroovyObject)getOwner();
			request = (HttpServletRequest)controller.getProperty(ControllerDynamicMethods.REQUEST_PROPERTY);
			response = (HttpServletResponse)controller.getProperty(ControllerDynamicMethods.RESPONSE_PROPERTY);
		}

		/**
		 * @param scaffoldRequestHandler The scaffoldRequestHandler to set.
		 */
		public void setScaffoldRequestHandler(
				ScaffoldRequestHandler scaffoldRequestHandler) {
			this.scaffoldRequestHandler = scaffoldRequestHandler;
		}

		public void setScaffoldResponseHandlerFactory(ScaffoldResponseHandlerFactory factory) {
			this.scaffoldResponseFactory = factory;
			this.scaffoldResponseHandler = this.scaffoldResponseFactory.getScaffoldResponseHandler(request.getRequestURI());
		}					
	}
	
	/**
	 * A closure that handles a call to a scaffolded list action    
	 */
	class ListAction extends AbstractAction {
		public ListAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			Map model = this.scaffoldRequestHandler.handleList(request,response);
			return scaffoldResponseHandler.handleResponse(request,response,LIST_ACTION,model);
		}
		
	}

	/**
	 * A closure that handles a call to a scaffolded list action
	 */
	class IndexAction extends AbstractAction {
		public IndexAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
            Map arguments = new HashMap();
            arguments.put( RedirectDynamicMethod.ARGUMENT_ACTION, LIST_ACTION );
            return controller.invokeMethod(RedirectDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
		}

	}

    /**
	 * A closure that handles a call to a scaffolded list action    
	 */
	class CreateAction extends AbstractAction {
		public CreateAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
            Map model = this.scaffoldRequestHandler.handleCreate(request,response,new DefaultScaffoldCallback());
            return scaffoldResponseHandler.handleResponse(request,response,CREATE_ACTION,model);
		}
		
	}	
	
	/**
	 * A closure action that implements showing a scaffolded instance by id. If the id is not
	 * specified it redirects to the "list" action 
	 */
	class ShowAction extends AbstractAction {

		public ShowAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			
			ScaffoldCallback callback = new DefaultScaffoldCallback();
			Map model = this.scaffoldRequestHandler.handleShow(request,response, callback);
			
			if(callback.isInvoked()) {				
				return scaffoldResponseHandler.handleResponse(request,response,SHOW_ACTION,model);
			}
			else {
				Map arguments = new HashMap();
				arguments.put( RedirectDynamicMethod.ARGUMENT_ACTION, LIST_ACTION );
				return controller.invokeMethod(RedirectDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
			}
		}
		
	}
	
	/**
	 * A closure action that implements editing a scaffolded instance by id. At the moment it is the same
	 * as ShowAction, but could differ in the future as it may load other model instances to support editing
	 * an instance including the GrailsDomainClass instance
	 */
	class EditAction extends AbstractAction {

		public EditAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			
			ScaffoldCallback callback = new DefaultScaffoldCallback();
			Map model = this.scaffoldRequestHandler.handleShow(request,response, callback);
			
			if(callback.isInvoked()) {
				return scaffoldResponseHandler.handleResponse(request,response,EDIT_ACTION,model);
			}
			else {
				Closure listAction = (Closure)controller.getProperty(LIST_ACTION);
				Map arguments = new HashMap();
				arguments.put( RedirectDynamicMethod.ARGUMENT_ACTION, listAction );
				arguments.put( RedirectDynamicMethod.ARGUMENT_ERRORS, callback.getErrors() );
				return controller.invokeMethod(RedirectDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
			}
		}
		
	}	
	
	/**
	 * A closure action that implements deletion of a scaffolded instance by id. The instance is deleted and then
	 * the action redirects to "list"
	 */
	class DeleteAction extends AbstractAction {

		public DeleteAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			
			ScaffoldCallback callback = new DefaultScaffoldCallback();
			// delete
			this.scaffoldRequestHandler.handleDelete(request,response, callback);
			
			// now redirect to list
			Closure listAction = (Closure)controller.getProperty(LIST_ACTION);
			Map arguments = new HashMap();
			arguments.put( RedirectDynamicMethod.ARGUMENT_ACTION, listAction );
			return controller.invokeMethod(RedirectDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });					
		}
		
	}	
	
	/**
	 * A closure action that implements the saving of new scaffoled instances. If the instance is created successfully
	 * the action redirects to "show" for the id, otherwise it redirects to "create"
	 */
	class SaveAction extends AbstractAction {

		public SaveAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			
			ScaffoldCallback callback = new DefaultScaffoldCallback();
			// save
			Map model = this.scaffoldRequestHandler.handleSave(request,response, callback);
			if(callback.isInvoked()) {
				Closure showAction = (Closure)controller.getProperty(SHOW_ACTION);
				Map arguments = new HashMap();
				arguments.put( RedirectDynamicMethod.ARGUMENT_ACTION, showAction );
                arguments.put( RedirectDynamicMethod.ARGUMENT_ID, model.get(ChainDynamicMethod.ARGUMENT_ID) );
				return controller.invokeMethod(RedirectDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
			}
			else {
				Map arguments = new HashMap();
				arguments.put( RenderDynamicMethod.ARGUMENT_VIEW, CREATE_ACTION );
				arguments.put( RenderDynamicMethod.ARGUMENT_MODEL,model );
				return controller.invokeMethod(RenderDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
			}
			
		}
		
	}	
	
	/**
	 * A closure action that implements the updating of an existing scaffoled instances. If the instance is updated successfully
	 * the action redirects to "show" for the id, otherwise it redirects to "edit"
	 */
	class UpdateAction extends AbstractAction {

		public UpdateAction(Object owner) {
			super(owner);
		}

		/* (non-Javadoc)
		 * @see groovy.lang.Closure#call(java.lang.Object[])
		 */
		public Object call(Object[] args) {
			
			ScaffoldCallback callback = new DefaultScaffoldCallback();
			// save
			Map model = this.scaffoldRequestHandler.handleUpdate(request,response, callback);
			if(callback.isInvoked()) {
				Closure showAction = (Closure)controller.getProperty(SHOW_ACTION);
				Map arguments = new HashMap();
				arguments.put( ChainDynamicMethod.ARGUMENT_ACTION, showAction );				
				arguments.put( ChainDynamicMethod.ARGUMENT_MODEL, model );
				arguments.put( ChainDynamicMethod.ARGUMENT_PARAMS, model );
				return controller.invokeMethod(ChainDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });				
			}
			else {
				Map arguments = new HashMap();
				arguments.put( RenderDynamicMethod.ARGUMENT_VIEW, EDIT_ACTION );
				arguments.put( RenderDynamicMethod.ARGUMENT_MODEL,model );
				return controller.invokeMethod(RenderDynamicMethod.METHOD_SIGNATURE,new Object[]{ arguments });
			}
			
		}
		
	}		
	protected static Map actions = new HashMap();
	protected static Map actionClassToNameMap = new HashMap();
	
	public static String[] ACTION_NAMES;
	
	static {
		actions.put( INDEX_ACTION, IndexAction.class.getConstructors()[0] );
		actionClassToNameMap.put(IndexAction.class, INDEX_ACTION);

        actions.put( LIST_ACTION, ListAction.class.getConstructors()[0] );
		actionClassToNameMap.put(ListAction.class, LIST_ACTION);
		
		actions.put( SHOW_ACTION, ShowAction.class.getConstructors()[0] );
		actionClassToNameMap.put(ShowAction.class, SHOW_ACTION);
		
		actions.put( EDIT_ACTION, EditAction.class.getConstructors()[0] );
		actionClassToNameMap.put(EditAction.class, EDIT_ACTION);
		
		actions.put( DELETE_ACTION, DeleteAction.class.getConstructors()[0] );
		actionClassToNameMap.put(DeleteAction.class, DELETE_ACTION);
		
		actions.put( SAVE_ACTION, SaveAction.class.getConstructors()[0] );
		actionClassToNameMap.put(SaveAction.class, SAVE_ACTION);
		
		actions.put( UPDATE_ACTION, UpdateAction.class.getConstructors()[0] );
		actionClassToNameMap.put(UpdateAction.class, UPDATE_ACTION);		
		
		actions.put( CREATE_ACTION, CreateAction.class.getConstructors()[0] );
		actionClassToNameMap.put(CreateAction.class, CREATE_ACTION);				
		
		// setup the action names
		ACTION_NAMES = (String[])actions.keySet().toArray( new String[actions.keySet().size()] );
	}
	
	public boolean supportsAction(String actionName) {
		if(actions.containsKey(actionName))
			return true;
		return false;
	}

	public Closure getAction(GroovyObject controller,String actionName) {
		Constructor c = (Constructor)actions.get(actionName);
		AbstractAction action;
		try {
			action = (AbstractAction)c.newInstance(new Object[]{this, controller });
			action.setScaffoldRequestHandler(this.scaffoldRequestHandler);	
			action.setScaffoldResponseHandlerFactory(this.scaffoldResponseHandlerFactory);
		} catch (IllegalArgumentException e) {
			throw new ScaffoldingException("Illegal argument instantiating action ["+actionName+"] for controller ["+controller.getClass().getName()+"]: " + e.getMessage(),e);
		} catch (InstantiationException e) {
			throw new ScaffoldingException("Error instantiating action ["+actionName+"] for controller ["+controller.getClass().getName()+"]: " + e.getMessage(),e);
		} catch (IllegalAccessException e) {
			throw new ScaffoldingException("Illegal access instantiating action ["+actionName+"] for controller ["+controller.getClass().getName()+"]: " + e.getMessage(),e);
		} catch (InvocationTargetException e) {
			throw new ScaffoldingException("Invocation error instantiating action ["+actionName+"] for controller ["+controller.getClass().getName()+"]: " + e.getMessage(),e);
		}
		return action;
	}



	/**
	 * @param scaffoldRequestHandler The scaffoldRequestHandler to set.
	 */
	public void setScaffoldRequestHandler(
			ScaffoldRequestHandler scaffoldRequestHandler) {
		this.scaffoldRequestHandler = scaffoldRequestHandler;		
	}

	
	/**
	 * @param scaffoldResponseHandlerFactory The scaffoldResponseHandlerFactory to set.
	 */
	public void setScaffoldResponseHandlerFactory(
			ScaffoldResponseHandlerFactory scaffoldResponseHandlerFactory) {
		this.scaffoldResponseHandlerFactory = scaffoldResponseHandlerFactory;
	}

	public String[] getSupportedActionNames() {		
		return ACTION_NAMES;
	}

	public String getActionName(Closure action) {
		return (String)actionClassToNameMap.get(action.getClass());
	}

    public ScaffoldRequestHandler getScaffoldRequestHandler() {
        return this.scaffoldRequestHandler;
    }

}
