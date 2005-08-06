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
package grails.pageflow;

import groovy.lang.Closure;

import java.lang.String;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.webflow.Action;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;

/**
 * <p>Simple container object for a state in a page flow.
 * 
 * <p>This class can map a view state, an action state, a sub flow state
 * or a decision state.
 * 
 * <p>One and only one of the following fields must be set, otherwise
 * the state will be regarded as an end state:
 * 
 * <ul>
 * <li>{@link #action}
 * <li>{@link #actionClass}
 * <li>{@link #actionClosure}
 * <li>{@link #actionFormDetails}
 * <li>{@link #viewName}
 * <li>{@link #viewClosure}
 * <li>{@link #decisionExpression}
 * <li>{@link #decisionClosure}
 * <li>{@link #subFlowId}
 * </ul>
 * 
 * <p>In case one of the {@link #isActionState()}, {@link #isViewState()}, {@link #isDecisionState()} or
 * {@link #isSubflowState()} methods is called before calling the {@link #validate()} method the  result
 * of those methods may be incorrect.
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class State {

	private static final String ACTION = "action";
	private static final String VIEW = "view";
	private static final String DECISION = "decision";
	private static final String SUBFLOW = "subflow";
	private static final String PROPERTIES = "properties";
	private static final String MODEL = "model";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String MAPPER = "mapper";
	private static final String TRUE_STATE = "trueState";
	private static final String FALSE_STATE = "falseState";
	private static final String METHOD = "method";
	private static final String END = "end";
	
	/**
	 * <p>Class that implements org.springframework.webflow.Action
	 * or has an non-abstract execute method that takes a
	 * RequestContext parameter and returns either String or
	 * org.springframework.webflow.Event. The class should have
	 * a public no-arg constructor. A new instance of this class
	 * will be created as used as action handler.
	 * 
	 * <p>{@link #actionProperties} will be used to set the properties
	 * of the action instance. If {@link #actionProperties} contains
	 * properties names that are not available on the action class
	 * an exception will be thrown.
	 */
	private Class actionClass = null;
	
	/**
	 * <p>A org.springframework.webflow.Action instance that will
	 * be used as action handler.
	 * 
	 * <p>{@link #actionProperties} will not be used to set the
	 * properties of this instance.
	 * 
	 */
	private Action action = null;
	
	/**
	 * <p>A closure that will handle the action logic.
	 * 
	 * <p>The closure can have one parameter, the
	 * {@link org.springframework.webflow.RequestContext}
	 * instance.
	 */
	private Closure actionClosure = null;
	
	/**
	 * <p>Delegates action handling to {@link org.springframework.webflow.action.FormAction}.
	 * 
	 * <p>This is a convenience notation for the combination of {@link #actionClass} and
	 * {@link #actionProperties}.
	 * 
	 * <p>All properties available on {@link org.springframework.webflow.action.FormAction} can
	 * be specified in the map.
	 */
	private Map actionFormDetails = null;
	
	/**
	 * <p>Holds properties names and values for instances of {@link #actionProperties}.
	 */
	private Map actionProperties = null;
	
	/**
	 * <p>Method that has to be executed on action.
	 */
	private String actionMethod = null;
	
	/**
	 * <p>Holds the view name of this state.
	 */
	private String viewName = null;
	
	/**
	 * <p>A closure that should return the view name.
	 * 
	 * <p>The return value should be not null and of
	 * type {@link String} or {@link org.springframework.webflow.Event}
	 * otherwise an exception is thrown.
	 */
	private Closure viewClosure = null;
	
	/**
	 * <p>Model names and values that can be set on the request context.
	 * 
	 * <p>If a value is a {@link Closure} it will be executed everytime the
	 * model needs to be populated. If no value is returned the name will
	 * be added to the model with a null value.
	 */
	private Map viewModel = null;
	
	/**
	 * <p>The name or id of a subflow.
	 */
	private String subFlowId = null;
	
	/**
	 * <p>A closure that maps values in the flow scope to map. This map
	 * is added to the flow scope of the subflow that is started.
	 * 
	 * <p>The closure can have one parameter, a {@link org.springframework.webflow.RequestContext}
	 * instance which is the request context of the parent flow.
	 * 
	 * <p>The return value should be null or an instance of {@link Map}.
	 * 
	 * <p>{@link #attributeMapperClass} and {@link #attributeMapper} should
	 * be null if this field is set. 
	 */
	private Closure subFlowInput = null;
	
	/**
	 * <p>A closure that puts values in the flow scope of an ending subflow
	 * in the flow scope of the resuming parent flow scope.
	 * 
	 * <p>The closure can have two parameters: the subflow scope and the
	 * parent flow scope.
	 * 
	 * <p>{@link #attributeMapperClass} and {@link #attributeMapper} should
	 * be null if this field is set. 
	 */
	private Closure subFlowOutput = null;
	
	/**
	 * <p>Class that implements {@link FlowAttributeMapper}. The class
	 * should have a public no-arg constructor. A new instance of this
	 * class will be created and used as flow attribute mapper between
	 * the parent flow and the subflow scopes.
	 * 
	 * <p>{@link #attributeMapperProperties} will be used to set the
	 * properties on this flow attribute mapper attribute. If {@link #attributeMapperProperties}
	 * contains property names that are not available on the flow attribute
	 * mapper class an exception will be thrown.
	 * 
	 * <p>{@link #subFlowInput}, {@link #subFlowOutput} and {@link #attributeMapper}
	 * should be null if this field is set. 
	 */
	private Class attributeMapperClass = null;
	
	/**
	 * <p>{@link FlowAttributeMapper} instance used as flow attribute mapper
	 * between the parent flwo and subflow scopes.
	 * 
	 * <p>{@link #attributeMapperProperties} will not be used to set
	 * the properties of this instance.
	 * 
	 * <p>{@link #subFlowInput}, {@link #subFlowOutput} and {@link #attributeMapperClass}
	 * should be null  if this field is set.
	 */
	private FlowAttributeMapper attributeMapper = null;
	
	/**
	 * <p>Hold property names and values for instances for {@link #attributeMapperClass}.
	 */
	private Map attributeMapperProperties = null;
	
	/**
	 * <p>The state to transition to in case the decision state returns true.
	 */
	private String decisionTrueStateId = null;
	
	/**
	 * <p>The state to transition to in case the decision state returns false.
	 */
	private String decisionFalseStateId = null;
	
	/**
	 * <p>OGNL expression to test the outcome of the decision state based
	 * on the request context.
	 */
	private String decisionExpression = null;
	
	/**
	 * <p>A closure that implements a test to determine the outcome of
	 * the decision state.
	 * 
	 * <p>The closure can have one parameter, an instance of {@link org.springframework.webflow.RequestContext}.
	 * 
	 * <p>Valid return values are:
	 * 
	 * <ul>
	 * <li>true
	 * <li>false
	 * <li>{@link Boolean#TRUE}
	 * <li>{@link Boolean#FALSE}
	 * </ul>
	 */
	private Closure decisionClosure = null;
	
	
	/**
	 * <p>The id or name of this state.
	 */
	private String id = null;
	
	/**
	 * <p>Transitions for this state.
	 */
	private List transitions = null;
	
	private boolean endState = false;
	
	public State(String id) {
		super();
		setId(id);
		this.transitions = new ArrayList();
	}
	
	public State(String id, Map attributes) {
		this(id);
		Assert.notNull(attributes);
		for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry)iter.next();
			
			if (ACTION.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + ACTION + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof Class) {
					actionClass = (Class)entry.getValue();
				} else if (entry.getValue() instanceof Action) {
					action = (Action)entry.getValue();
				} else if (entry.getValue() instanceof Map) {
					actionFormDetails = (Map)entry.getValue();
				} else if (entry.getValue() instanceof Closure) {
					actionClosure = (Closure)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + ACTION + "] on state [" + id + "]!");
				}
			} else if (VIEW.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + VIEW + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					viewName = (String)entry.getValue();
				} else if (entry.getValue() instanceof Closure) {
					viewClosure = (Closure)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + VIEW + "] on state [" + id + "]!");
				}
			} else if (DECISION.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + DECISION + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					decisionExpression = (String)entry.getValue();
				} else if (entry.getValue() instanceof Closure) {
					decisionClosure = (Closure)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + DECISION + "] on state [" + id + "]!");
				}
			} else if (SUBFLOW.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + SUBFLOW + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					subFlowId = (String)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + SUBFLOW + "] on state [" + id + "]!");
				}
			} else if (PROPERTIES.equals(entry.getKey())) {
				if (entry.getValue() == null) {
//					accept this
				} else if (entry.getValue() instanceof Map) {
//					if (isActionState()) {
//						actionProperties = (Map)entry.getValue();
//					} else if (isSubflowState()) {
//						attributeMapperProperties = (Map)entry.getValue();
//					} else if (isViewState()) {
//						throw new IllegalArgumentException("The [" + PROPERTIES + "] property is not supported for view state [" + id + "]!");
//					} else if (isDecisionState()) {
//						throw new IllegalArgumentException("The [" + PROPERTIES + "] property is not supported for decision state [" + id + "]!");
//					} else {
//						throw new IllegalStateException("Could not determine state type when handling [" + PROPERTIES + "] property for state [" + id +"]. Try moving the state discriminator attribute ([" + ACTION + "] or [" + SUBFLOW +"]) in front of the [" + PROPERTIES + "] property if have specified it!");
//					}
					/*
					 * This is a hack. The code above does not work as order of attribute entries is undertermined.
					 * We set both actionProperties and attributeMapperProperties.
					 * As a side effect attribute with discriminator property and with properties property will
					 * not fail in this construtor but will fail in validate method.
					 */
					actionProperties = (Map)entry.getValue();
					attributeMapperProperties = (Map)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsuppored type [" + entry.getValue().getClass().getName() + "] for attribute [" + PROPERTIES + "] on state [" + id + "]!");
				}
			} else if (MODEL.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					// accept this
				} else if (entry.getValue() instanceof Map) {
					viewModel = (Map)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + MODEL + "] on state [" + id + "]!");
				}
			} else if (INPUT.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					// accept this
				} else if (entry.getValue() instanceof Closure) {
					subFlowInput = (Closure)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + INPUT + "] on state [" + id + "]!");
				}
			} else if (OUTPUT.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					// accept this
				} else if (entry.getValue() instanceof Closure) {
					subFlowOutput = (Closure)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + OUTPUT + "] on state [" + id + "]!");
				}
			} else if (MAPPER.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					// accept this
				} else if (entry.getValue() instanceof Class) {
					attributeMapperClass = (Class)entry.getValue();
				} else if (entry.getValue() instanceof FlowAttributeMapper) {
					attributeMapper = (FlowAttributeMapper)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] attribute [" + MAPPER + "] on state [" + id + "]!");
				}
			} else if (TRUE_STATE.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + TRUE_STATE + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					decisionTrueStateId = (String)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + TRUE_STATE + "] on state [" + id + "]!");
				}
			} else if (FALSE_STATE.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + FALSE_STATE + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					decisionFalseStateId = (String)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + FALSE_STATE + "] on state [" + id + "]!");
				}
			} else if (METHOD.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + METHOD + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof String) {
					actionMethod = (String)entry.getValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + METHOD + "] on state [" + id + "]!");
				}
			} else if (END.equals(entry.getKey())) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("Value for property [" + END + "] on state [" + id + "] must not be null!");
				} else if (entry.getValue() instanceof Boolean) {
					endState = ((Boolean)entry.getValue()).booleanValue();
				} else {
					throw new IllegalArgumentException("Unsupported type [" + entry.getValue().getClass().getName() + "] for attribute [" + END + "] on state [" + id + "]!");
				}
			} else {
				throw new IllegalArgumentException("Unknow property [" + entry.getValue() + "] on state [" + id + "]!");
			}
 		}
	}

	/**
	 * <p>Adds a transition to this state.
	 */
	public void addTransition(Transition transition) {
		Assert.notNull(transition);
		for (Iterator iter = this.transitions.iterator(); iter.hasNext();) {
			Transition tmpTransition = (Transition)iter.next();
			if (tmpTransition.getTargetStateId().equals(transition.getTargetStateId())) {
				throw new IllegalArgumentException("This state [" + getId() + "] already has a transition to state [" + tmpTransition.getTargetStateId() + "]!");
			}
		}
		this.transitions.add(transition);
	}
	
	/**
	 * <p>Returns the list of transitions for this state.
	 * 
	 * @return transitions for this state.
	 */
	public List getTransitions() {
		return this.transitions;
	}
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public Class getActionClass() {
		return actionClass;
	}
	public void setActionClass(Class actionClass) {
		this.actionClass = actionClass;
	}
	public Closure getActionClosure() {
		return actionClosure;
	}
	public void setActionClosure(Closure actionClosure) {
		this.actionClosure = actionClosure;
	}
	public Map getActionFormDetails() {
		return actionFormDetails;
	}
	public void setActionFormDetails(Map actionFormDetails) {
		this.actionFormDetails = actionFormDetails;
	}
	public Map getActionProperties() {
		return actionProperties;
	}
	public void setActionProperties(Map actionProperties) {
		this.actionProperties = actionProperties;
	}
	public FlowAttributeMapper getAttributeMapper() {
		return attributeMapper;
	}
	public void setAttributeMapper(FlowAttributeMapper attributeMapper) {
		this.attributeMapper = attributeMapper;
	}
	public Class getAttributeMapperClass() {
		return attributeMapperClass;
	}
	public void setAttributeMapperClass(Class attributeMapperClass) {
		this.attributeMapperClass = attributeMapperClass;
	}
	public Map getAttributeMapperProperties() {
		return attributeMapperProperties;
	}
	public void setAttributeMapperProperties(Map attributeMapperProperties) {
		this.attributeMapperProperties = attributeMapperProperties;
	}
	public Closure getDecisionClosure() {
		return decisionClosure;
	}
	public void setDecisionClosure(Closure decisionClosure) {
		this.decisionClosure = decisionClosure;
	}
	public String getDecisionExpression() {
		return decisionExpression;
	}
	public void setDecisionExpression(String decisionExpression) {
		this.decisionExpression = decisionExpression;
	}
	public String getDecisionFalseStateId() {
		return decisionFalseStateId;
	}
	public void setDecisionFalseStateId(String decisionFalseStateId) {
		this.decisionFalseStateId = decisionFalseStateId;
	}
	public String getDecisionTrueStateId() {
		return decisionTrueStateId;
	}
	public void setDecisionTrueStateId(String decisionTrueStateId) {
		this.decisionTrueStateId = decisionTrueStateId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSubFlowId() {
		return subFlowId;
	}
	public void setSubFlowId(String subFlowId) {
		this.subFlowId = subFlowId;
	}
	public Closure getSubFlowInput() {
		return subFlowInput;
	}
	public void setSubFlowInput(Closure subFlowInput) {
		this.subFlowInput = subFlowInput;
	}
	public Closure getSubFlowOutput() {
		return subFlowOutput;
	}
	public void setSubFlowOutput(Closure subFlowOutput) {
		this.subFlowOutput = subFlowOutput;
	}
	public Closure getViewClosure() {
		return viewClosure;
	}
	public void setViewClosure(Closure viewClosure) {
		this.viewClosure = viewClosure;
	}
	public Map getViewModel() {
		return viewModel;
	}
	public void setViewModel(Map viewModel) {
		this.viewModel = viewModel;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getActionMethod() {
		return actionMethod;
	}
	public void setActionMethod(String actionMethod) {
		this.actionMethod = actionMethod;
	}
	
	/**
	 * <p>Validate the various fields of this class.
	 */
	public void validate() throws IllegalStateException {
		Object[] fields = new Object[] { action, actionClass, actionClosure, actionFormDetails, decisionClosure, decisionExpression, subFlowId, viewClosure, viewName };
		boolean oneNotNull = false;
		
		if (StringUtils.isBlank(id)) {
			throw new IllegalStateException("State id must be specified!");
		}
		
		for (int i = 0; i < fields.length; i++) {
			if (oneNotNull) {
				if (fields[i] != null) {
					throw new IllegalStateException("Could not determine state type, more than one discriminator field was specified!");
				}
			} else {
				oneNotNull = fields[i] != null; 
			}
		}
//		if (!oneNotNull) {
//			throw new IllegalStateException("Could not determine state type, no descriminator field was specified!");
//		}
		
		if (isSubflowState()) {
			if ((subFlowInput != null || subFlowOutput != null) && (attributeMapper != null || attributeMapperClass != null)) {
				throw new IllegalStateException("attribute mapper and attribute mapper class must be null if either subflow input or subflow output are set!");
			} else if (attributeMapper != null && (subFlowInput != null || subFlowOutput != null || attributeMapperClass != null)) {
				throw new IllegalStateException("subflow input and subflow output and attribute mapper class must be null if attribute mapper is set!");
			} else if (attributeMapperClass != null && (subFlowInput != null || subFlowOutput != null || attributeMapper != null)) {
				throw new IllegalStateException("subflow input and subflow output and attribute mapper class must be null if attribute mapper class is set!");
			}
		}

		if (isDecisionState()) {
			if (StringUtils.isBlank(decisionTrueStateId)) {
				throw new IllegalStateException("Decision true state id must be set!");
			} else if (StringUtils.isBlank(decisionFalseStateId)) {
				throw new IllegalStateException("Decision false state id must be set!");
			}
		}
		
		if (actionClass != null) {
			if (!Action.class.isAssignableFrom(actionClass) && !ClassUtils.hasMethod(actionClass, "execute", new Class[] { RequestContext.class })) {
				throw new IllegalStateException("Action class [" + actionClass.getName() + "] does not implement [" + Action.class.getName() + "] and has no public method [execute(" + RequestContext.class.getName() + ")]!");
			} else if (Modifier.isAbstract(actionClass.getModifiers())) {
				throw new IllegalStateException("Action class [" + actionClass.getName() + "] is abstract!");
			}
		}
		
		if (attributeMapperClass != null) {
			if (!FlowAttributeMapper.class.isAssignableFrom(attributeMapperClass)) {
				throw new IllegalStateException("Flow attribute mapper class [" + attributeMapperClass.getName() + "] does not implement [" + FlowAttributeMapper.class.getName() + "]!");
			} else if (Modifier.isAbstract(attributeMapperClass.getModifiers())) {
				throw new IllegalStateException("Flow attribute mapper class [" + attributeMapperClass.getName() + "] is abstract!");
			}
		}
		
 	}
	
	/**
	 * <p>Is this state an action state.
	 * 
	 * @return true is this state is an action state.
	 */
	public boolean isActionState() {
		return (action != null || actionClass != null || actionClosure != null || actionFormDetails != null);
	}
	
	/**
	 * <p>Is this state a view state.
	 * 
	 * @return true if this state in a view state.
	 */
	public boolean isViewState() {
		return ((viewName != null || viewClosure != null) && !endState);
	}
	
	/**
	 * <p>Is this state a decision state.
	 * 
	 * @return true if this state is a decision state.
	 */
	public boolean isDecisionState() {
		return (decisionExpression != null || decisionClosure != null);
	}
	
	/**
	 * <p>Is this state a subflow state.
	 * 
	 * @return true if this state is a subflow state.
	 */
	public boolean isSubflowState() {
		return (subFlowId != null);
	}
	
	/**
	 * <p>Is this state an end state.
	 * 
	 * @return true if this state is an end state.
	 */
	public boolean isEndState() {
		return ((endState && isViewState()) || (!isActionState() && !isViewState() && !isDecisionState() && !isSubflowState()));
	}
	
	public String toString() {
		return new ToStringCreator(this)
			.append("action=" + action)
			.append("actionClass=" + actionClass)
			.append("actionClosure=" + actionClosure)
			.append("actionFormDetail=" + actionFormDetails)
			.append("actionProperties=" + actionProperties)
			.append("attributeMapper=" + attributeMapper)
			.append("attributeMapperClass=" + attributeMapperClass)
			.append("attributeMapperProperties=" + attributeMapperProperties)
			.append("decisionClosure=" + decisionClosure)
			.append("decisionExpression=" + decisionExpression)
			.append("decisionTrueStateId=" + decisionTrueStateId)
			.append("decisionFalseStateId=" + decisionFalseStateId)
			.append("subFlowId=" + subFlowId)
			.append("subFlowInput=" + subFlowInput)
			.append("subFlowOutput" + subFlowOutput)
			.append("transitions=" + transitions)
			.append("viewClosure=" + viewClosure)
			.append("viewName=" + viewName)
			.append("viewModel=" + viewModel)
			.append("\n").toString();
		
			
	}

}
