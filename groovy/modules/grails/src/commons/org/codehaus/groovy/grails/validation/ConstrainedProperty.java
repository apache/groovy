/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.validation;


import groovy.lang.MissingPropertyException;
import groovy.lang.Range;
import groovy.lang.IntRange;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.CreditCardValidator;
import org.apache.commons.validator.EmailValidator;
import org.apache.commons.validator.UrlValidator;
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;

/**
 * Provides the ability to set contraints against a properties of a class. Constraints can either be
 * set via the property setters or via the <pre>applyConstraint(String constraintName, Object constrainingValue)</pre>
 * in combination with a constraint constant. Example:
 * 
 * <code>
 *      ...
 *      
 * 		ConstrainedProperty cp = new ConstrainedProperty(owningClass, propertyName, propertyType);
 *      if(cp.supportsConstraint( ConstrainedProperty.EMAIL_CONSTRAINT ) ) {
 *      	cp.applyConstraint( ConstrainedProperty.EMAIL_CONSTRAINT, new Boolean(true) );      
 *      }
 * </code>
 *
 * Alternatively constraints can be applied directly using the java bean getters/setters if a static (as oposed to dynamic)
 * approach to constraint creation is possible:
 *
 * <code>
 *       cp.setEmail(true)
 * </code>
 * @author Graeme Rocher
 * @since 07-Nov-2005
 */
public class ConstrainedProperty   {

    public static final String CREDIT_CARD_CONSTRAINT = "creditCard";
    public static final String EMAIL_CONSTRAINT = "email";
	public static final String BLANK_CONSTRAINT = "blank";
	public static final String RANGE_CONSTRAINT = "range";
	public static final String IN_LIST_CONSTRAINT = "inList";
	public static final String URL_CONSTRAINT = "url";
	public static final String MATCHES_CONSTRAINT = "matches";
	public static final String LENGTH_CONSTRAINT = "length";
	public static final String SIZE_CONSTRAINT = "size";
	public static final String MIN_CONSTRAINT = "min";
	public static final String MAX_CONSTRAINT = "max";
	public static final String MAX_SIZE_CONSTRAINT = "maxSize";
	public static final String MIN_SIZE_CONSTRAINT = "minSize";
	public static final String MAX_LENGTH_CONSTRAINT = "maxLength";
	public static final String MIN_LENGTH_CONSTRAINT = "minLength";	
	public static final String NOT_EQUAL_CONSTRAINT = "notEqual";
	public static final String NULLABLE_CONSTRAINT = "nullable";
	
	protected static final String INVALID_SUFFIX = ".invalid";
	protected static final String EXCEEDED_SUFFIX = ".exceeded";
	protected static final String NOTMET_SUFFIX = ".notmet";
	protected static final String NOT_PREFIX = ".not.";
	protected static final String TOOBIG_SUFFIX = ".toobig";
	protected static final String TOOLONG_SUFFIX = ".toolong";
	protected static final String TOOSMALL_SUFFIX = ".toosmall";
	protected static final String TOOSHORT_SUFFIX = ".tooshort";
	
	protected static Map constraints = new HashMap();
	
	static {
        constraints.put( CREDIT_CARD_CONSTRAINT, CreditCardConstraint.class );
        constraints.put( EMAIL_CONSTRAINT, EmailConstraint.class );
		constraints.put( BLANK_CONSTRAINT, BlankConstraint.class );
		constraints.put( RANGE_CONSTRAINT, RangeConstraint.class );
		constraints.put( IN_LIST_CONSTRAINT, InListConstraint.class );
		constraints.put( URL_CONSTRAINT, UrlConstraint.class );
		constraints.put( LENGTH_CONSTRAINT, SizeConstraint.class );
		constraints.put( SIZE_CONSTRAINT, SizeConstraint.class );
		constraints.put( MATCHES_CONSTRAINT, MatchesConstraint.class );
		constraints.put( MIN_CONSTRAINT, MinConstraint.class );
		constraints.put( MAX_CONSTRAINT, MaxConstraint.class );
		constraints.put( MAX_SIZE_CONSTRAINT, MaxSizeConstraint.class );
		constraints.put( MAX_LENGTH_CONSTRAINT, MaxSizeConstraint.class );
		constraints.put( MIN_SIZE_CONSTRAINT, MinSizeConstraint.class );
		constraints.put( MIN_LENGTH_CONSTRAINT, MinSizeConstraint.class );
		constraints.put( NULLABLE_CONSTRAINT, NullableConstraint.class );
		constraints.put( NOT_EQUAL_CONSTRAINT, NotEqualConstraint.class );
	}
	
	protected static final ResourceBundle bundle = ResourceBundle.getBundle( "org.codehaus.groovy.grails.validation.DefaultErrorMessages" );
	protected static final Log LOG = LogFactory.getLog(ConstrainedProperty.class);
	
	private static final String DEFAULT_BLANK_MESSAGE = bundle.getString( "default.blank.message" );
	private static final String DEFAULT_DOESNT_MATCH_MESSAGE = bundle.getString( "default.doesnt.match.message" );
	private static final String DEFAULT_INVALID_URL_MESSAGE = bundle.getString( "default.invalid.url.message" );
    private static final String DEFAULT_INVALID_CREDIT_CARD_MESSAGE = bundle.getString( "default.invalid.creditCard.message" );
    //private static final String DEFAULT_INVALID_MESSAGE = bundle.getString( "default.invalid.message" );
	private static final String DEFAULT_INVALID_EMAIL_MESSAGE  = bundle.getString( "default.invalid.email.message" );
	private static final String DEFAULT_INVALID_RANGE_MESSAGE = bundle.getString( "default.invalid.range.message" );	
	private static final String DEFAULT_NOT_IN_LIST_MESSAGE = bundle.getString( "default.not.inlist.message" );
	private static final String DEFAULT_INVALID_SIZE_MESSAGE = bundle.getString( "default.invalid.size.message" );
	private static final String DEFAULT_INVALID_LENGTH_MESSAGE = bundle.getString( "default.invalid.length.message" );
	private static final String DEFAULT_INVALID_MAX_MESSAGE = bundle.getString( "default.invalid.max.message" );
	private static final String DEFAULT_INVALID_MIN_MESSAGE = bundle.getString( "default.invalid.min.message" );
	private static final String DEFAULT_NOT_EQUAL_MESSAGE = bundle.getString( "default.not.equal.message" );
	private static final String DEFAULT_INVALID_MAX_LENGTH_MESSAGE = bundle.getString( "default.invalid.max.length.message" );
	private static final String DEFAULT_INVALID_MAX_SIZE_MESSAGE = bundle.getString( "default.invalid.max.size.message" );
	private static final String DEFAULT_INVALID_MIN_LENGTH_MESSAGE = bundle.getString( "default.invalid.min.length.message" );
	private static final String DEFAULT_INVALID_MIN_SIZE_MESSAGE = bundle.getString( "default.invalid.min.size.message" );
	private static final String DEFAULT_NULL_MESSAGE = bundle.getString( "default.null.message" );
	// move these to subclass	

	protected String propertyName;
	protected Class propertyType;
	
	protected Map appliedConstraints = new HashMap();
	protected Class owningClass;
	private BeanWrapper bean;
	
	// simple constraints
	private boolean display = true; // whether the property should be displayed
	private boolean editable = true; // whether the property is editable
    private boolean file; // whether the property is a file
    private int order; // what order to property appears in
    private String format; // the format of the property (for example a date pattern)
    private String widget; // the widget to use to render the property
    private boolean password; // whether the property is a password
    private Map attributes = Collections.EMPTY_MAP; // a map of attributes of property


    /**
	 * 
	 * Abstract class for constraints to implement
	 */
	abstract protected static class AbstractConstraint implements Constraint {
		protected String constraintPropertyName;
		protected Class constraintOwningClass;
		protected Object constraintParameter;
		/**
		 * @param constraintOwningClass The constraintOwningClass to set.
		 */
		public void setOwningClass(Class constraintOwningClass) {
			this.constraintOwningClass = constraintOwningClass;
		}
		/**
		 * @param constraintPropertyName The constraintPropertyName to set.
		 */
		public void setPropertyName(String constraintPropertyName) {
			this.constraintPropertyName = constraintPropertyName;
		}				
		/**
		 * @param constraintParameter The constraintParameter to set.
		 */
		public void setParameter(Object constraintParameter) {
			this.constraintParameter = constraintParameter;
		}
		public void validate(Object propertyValue, Errors errors) {
			//ValidationUtils.rejectIfEmpty( errors, constraintPropertyName, constraintPropertyName+".empty" );
			if(StringUtils.isBlank(this.constraintPropertyName))
				throw new IllegalStateException("Property 'propertyName' must be set on the constraint");
			if(constraintOwningClass == null)
				throw new IllegalStateException("Property 'owningClass' must be set on the constraint");
			if(constraintParameter == null)
				throw new IllegalStateException("Property 'constraintParameter' must be set on the constraint");
			
			processValidate(propertyValue, errors);
		}		
		public void rejectValue(Errors errors, String code,String defaultMessage) {
			errors.rejectValue(constraintPropertyName, constraintPropertyName + '.' + code, defaultMessage);
		}
		public void rejectValue(Errors errors, String code,Object[] args,String defaultMessage) {
			errors.rejectValue(constraintPropertyName, constraintPropertyName + '.' + code, args,defaultMessage);
		}		
		protected abstract void processValidate(Object propertyValue, Errors errors);

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return new ToStringBuilder(this)
							.append( constraintParameter )
							.toString();
		}
		
		
	}
	
	/**
	 * A Constraint that validates not equal to something
	 */		
	static class NotEqualConstraint extends AbstractConstraint {
		
		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			return true;
		}

		/**
		 * @return Returns the notEqualTo.
		 */
		public Object getNotEqualTo() {
			return this.constraintParameter;
		}

		protected void processValidate(Object propertyValue, Errors errors) {			
			if(!this.constraintParameter.equals( propertyValue )) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, constraintParameter  };
				super.rejectValue( errors, NOT_EQUAL_CONSTRAINT,args, MessageFormat.format( DEFAULT_NOT_EQUAL_MESSAGE, args ) );
			}			
		}
		
	}
	/**
	 * A Constraint that validates not equal to something
	 */		
	static class NullableConstraint extends AbstractConstraint {

		private boolean nullable;

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean)) 
				throw new IllegalArgumentException("Parameter for constraint ["+NULLABLE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");
			
			this.nullable = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {			
			if(!nullable && propertyValue == null) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass};
				super.rejectValue( errors, NULLABLE_CONSTRAINT,args, MessageFormat.format( DEFAULT_NULL_MESSAGE, args ) );
			}			
		}
		
	}	
	/**
	 * A Constraint that validates a string is not blank
	 */	
	static class BlankConstraint extends AbstractConstraint {
		
		private boolean blank;
		

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return String.class.isAssignableFrom(type);
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean)) 
				throw new IllegalArgumentException("Parameter for constraint ["+BLANK_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");
			
			this.blank = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {

			if(!blank) {
				if(propertyValue instanceof String) {
					if(StringUtils.isBlank((String)propertyValue)) {
						Object[] args = new Object[] { constraintPropertyName, constraintOwningClass };
						super.rejectValue( errors, BLANK_CONSTRAINT,args, MessageFormat.format( DEFAULT_BLANK_MESSAGE, args ) );
					}
				}
			}
		}
	}

    /**
     * A constraint class that validates a credit card number
     */
    static class CreditCardConstraint extends AbstractConstraint {
        private boolean creditCard;


        protected void processValidate(Object propertyValue, Errors errors) {
            if(creditCard) {
                CreditCardValidator validator = new CreditCardValidator();

                if(!validator.isValid(propertyValue.toString())  ) {
                    Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue };
                    super.rejectValue(errors,CREDIT_CARD_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_CREDIT_CARD_MESSAGE, args ));
                }
            }
        }

        public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean))
				throw new IllegalArgumentException("Parameter for constraint ["+EMAIL_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");

			this.creditCard = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
        }

        public boolean supports(Class type) {
			if(type == null)
				return false;

			return String.class.isAssignableFrom(type);
        }
    }
    /**
	 * 
	 * A Constraint that validates an email address
	 */	
	static class EmailConstraint extends AbstractConstraint {						

		private boolean email;
		
		

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return String.class.isAssignableFrom(type);
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean)) 
				throw new IllegalArgumentException("Parameter for constraint ["+EMAIL_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");
			
			this.email = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			if(email) {
				EmailValidator emailValidator = EmailValidator.getInstance();
				if(!emailValidator.isValid(propertyValue.toString())  ) {
					Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue };
					super.rejectValue(errors,EMAIL_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_EMAIL_MESSAGE, args ));
				}
			}
		}
	}
	
	/**
	 * 
	 * A Constraint that validates a url
	 */		
	static class UrlConstraint extends AbstractConstraint {

		private boolean url;
		
	
		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return String.class.isAssignableFrom(type);
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean)) 
				throw new IllegalArgumentException("Parameter for constraint ["+URL_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");
			
			this.url = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			if(url) {
				UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_2_SLASHES);
								
				if(!urlValidator.isValid(propertyValue.toString())) {
					Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue };
					super.rejectValue(errors,URL_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_URL_MESSAGE, args ));				
				}
			}
		}
		
	}
	/**
	 * 
	 * A Constraint that validates a range
	 */	
	static class RangeConstraint extends AbstractConstraint {
		Range range;		
		/**
		 * @return Returns the range.
		 */
		public Range getRange() {
			return range;
		}

		
		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type);
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Range)) 
				throw new IllegalArgumentException("Parameter for constraint ["+RANGE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a of type [groovy.lang.Range]");
			
			this.range = (Range)constraintParameter;
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			if(!this.range.contains(propertyValue)) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, range.getFrom(), range.getTo()  };

                if(propertyValue == null) {
                    super.rejectValue(errors,RANGE_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_RANGE_MESSAGE, args ));
                }
                else if(range.getFrom().compareTo( propertyValue ) == -1) {
					super.rejectValue(errors,SIZE_CONSTRAINT + TOOSMALL_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
				}
				else if(range.getFrom().compareTo(propertyValue) == 1) {
					super.rejectValue(errors,SIZE_CONSTRAINT + TOOBIG_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
				}		
				

			}
		}				
	}
	
	/**
	 * 
	 * A Constraint that implements a maximum value constraint
	 */		
	static class MaxConstraint extends AbstractConstraint {


		private Comparable maxValue;
		
		/**
		 * @return Returns the maxValue.
		 */
		public Comparable getMaxValue() {
			return maxValue;
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type);
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Comparable)) 
				throw new IllegalArgumentException("Parameter for constraint ["+MAX_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must implement the interface [java.lang.Comparable]");
			
			this.maxValue = (Comparable)constraintParameter;
			super.setParameter(constraintParameter);
		}


		protected void processValidate(Object propertyValue, Errors errors) {
			if(maxValue.compareTo((Comparable)propertyValue) > 0) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, maxValue  };
				super.rejectValue(errors,MAX_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_MESSAGE, args ));
			}
		}		
	}
	
	/**
	 * 
	 * A Constraint that implements a minimum value constraint
	 */		
	static class MinConstraint extends AbstractConstraint {

		private Comparable minValue;

		
		/**
		 * @return Returns the minValue.
		 */
		public Comparable getMinValue() {
			return minValue;
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type);
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Comparable)) 
				throw new IllegalArgumentException("Parameter for constraint ["+MIN_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must implement the interface [java.lang.Comparable]");
			
			this.minValue = (Comparable)constraintParameter;
			super.setParameter(constraintParameter);
		}


		protected void processValidate(Object propertyValue, Errors errors)		{
			if(minValue.compareTo((Comparable)propertyValue) < 0) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, minValue  };
				super.rejectValue(errors,MIN_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_MESSAGE, args ));
			}
		}		
	}	
	/**
	 * A constraint that validates the property is contained within the supplied list
	 */	
	static class InListConstraint extends AbstractConstraint {

		List list;

		/**
		 * @return Returns the list.
		 */
		public List getList() {
			return list;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof List)) 
				throw new IllegalArgumentException("Parameter for constraint ["+IN_LIST_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must implement the interface [java.util.List]");
			
			this.list = (List)constraintParameter;
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			if(!this.list.contains(propertyValue)) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, list  };
				super.rejectValue(errors,NOT_PREFIX + IN_LIST_CONSTRAINT,args,MessageFormat.format( DEFAULT_NOT_IN_LIST_MESSAGE,args ));
			}
		}
		
	}
	
	/**
	 * A constraint that validates the property against a supplied regular expression
	 */		
	static class MatchesConstraint extends AbstractConstraint {

		private String regex;
		
		/**
		 * @return Returns the regex.
		 */
		public String getRegex() {
			return regex;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return String.class.isAssignableFrom(type);
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof String)) 
				throw new IllegalArgumentException("Parameter for constraint ["+MATCHES_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be of type [java.lang.String]");
			
			this.regex = (String)constraintParameter;
			super.setParameter(constraintParameter);
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			if(propertyValue.toString().matches( regex )) {
				Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, regex  };
				super.rejectValue(errors,MATCHES_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_DOESNT_MATCH_MESSAGE, args ));
			}
			
		}
		
	}
	
	/**
	 * A constraint that validates size of the property, for strings and arrays this is the length, collections
	 * the size and numbers the value
	 */		
	static class SizeConstraint extends AbstractConstraint {

		private IntRange sizeRange;

		/**
		 * @return Returns the sizeRange.
		 */
		public IntRange getSizeRange() {
			return sizeRange;
		}
		
		

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type) ||
					Collection.class.isAssignableFrom(type) ||
					type.isArray();
		}



		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof IntRange))
				throw new IllegalArgumentException("Parameter for constraint ["+SIZE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a of type [groovy.lang.IntRange]");
			
			this.sizeRange = (IntRange)constraintParameter;
			super.setParameter(constraintParameter);
		}



		protected void processValidate(Object propertyValue, Errors errors) {
			
			Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue,  sizeRange.getFrom(), sizeRange.getTo()  };
			if(propertyValue == null) {
				super.rejectValue(errors,SIZE_CONSTRAINT + INVALID_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
				return;
			}
			if(propertyValue.getClass().isArray()) {
				Integer length = new Integer(Array.getLength( propertyValue ));
				if(!sizeRange.contains(length)) {	
					
					if(sizeRange.getFrom().compareTo( length ) == -1) {
						super.rejectValue(errors,LENGTH_CONSTRAINT + TOOSHORT_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_LENGTH_MESSAGE, args ));
					}
					else if(sizeRange.getFrom().compareTo(length) == 1) {
						super.rejectValue(errors,LENGTH_CONSTRAINT + TOOLONG_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_LENGTH_MESSAGE, args ));
					}						
					return;
				}
			}
			if(propertyValue instanceof Collection) {
				Integer collectionSize = new Integer(((Collection)propertyValue).size());
				if(!sizeRange.contains( collectionSize )) {
					if(sizeRange.getFrom().compareTo( collectionSize ) == -1) {
						super.rejectValue(errors,SIZE_CONSTRAINT + TOOSMALL_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
					}
					else if(sizeRange.getFrom().compareTo(collectionSize) == 1) {
						super.rejectValue(errors,SIZE_CONSTRAINT + TOOBIG_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
					}						
					return;
				}
			}
			else if(propertyValue instanceof Number) {				
				if(sizeRange.getFrom().compareTo( propertyValue ) == -1) {
					super.rejectValue(errors,SIZE_CONSTRAINT + TOOSMALL_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
				}
				else if(sizeRange.getFrom().compareTo(propertyValue) == 1) {
					super.rejectValue(errors,SIZE_CONSTRAINT + TOOBIG_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_SIZE_MESSAGE, args ));
				}		
				return;
			}
			else if(propertyValue instanceof String) {
				Integer stringLength =  new Integer(((String)propertyValue ).length());
				if(!sizeRange.contains(stringLength)) {
					if(sizeRange.getFrom().compareTo( stringLength ) == -1) {
						super.rejectValue(errors,LENGTH_CONSTRAINT + TOOSHORT_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_LENGTH_MESSAGE, args ));
					}
					else if(sizeRange.getFrom().compareTo(stringLength) == 1) {
						super.rejectValue(errors,LENGTH_CONSTRAINT + TOOLONG_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_LENGTH_MESSAGE, args ));
					}											
					return;
				}
			}			
		}		
	}
	
	/**
	 * A constraint that validates maximum size of the property, for strings and arrays this is the length, collections
	 * the size and numbers the value
	 */		
	static class MaxSizeConstraint extends AbstractConstraint {

		private int maxSize;

		/**
		 * @return Returns the maxSize.
		 */
		public int getMaxSize() {
			return maxSize;
		}
		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Integer)) 
				throw new IllegalArgumentException("Parameter for constraint ["+MAX_SIZE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a of type [java.lang.Integer]");
			
			this.maxSize = ((Integer)constraintParameter).intValue();
			super.setParameter(constraintParameter);
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type) ||
					Collection.class.isAssignableFrom(type) ||
					type.isArray();
		}

		protected void processValidate(Object propertyValue, Errors errors) {
			Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, new Integer(maxSize) };
			if(propertyValue == null) {
				super.rejectValue(errors,MAX_LENGTH_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_LENGTH_MESSAGE, args ));
				return;
			}
			if(propertyValue.getClass().isArray()) {
				int length = Array.getLength( propertyValue );
				if(length > maxSize) {
					super.rejectValue(errors,MAX_LENGTH_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_LENGTH_MESSAGE, args ));
					return;
				}
			}
			if(propertyValue instanceof Collection) {
				if( ((Collection)propertyValue).size() > maxSize ) {
					super.rejectValue(errors,MAX_SIZE_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_SIZE_MESSAGE, args ));
					return;
				}
			}
			else if(propertyValue instanceof Number) {				
				int numberSize = ((Number)propertyValue).intValue();
				if( numberSize > maxSize ) {
					super.rejectValue(errors,MAX_SIZE_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_SIZE_MESSAGE, args ));
					return;
				}
			}
			else if(propertyValue instanceof String) {
				if(((String)propertyValue ).length() > maxSize) {
					super.rejectValue(errors,MAX_LENGTH_CONSTRAINT + EXCEEDED_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MAX_LENGTH_MESSAGE, args ));
					return;
				}
			}			
		}		
	}
	
	/**
	 * A constraint that validates minimum size or length of the property, for strings and arrays this is the length, collections
	 * the size and numbers the value
	 */		
	static class MinSizeConstraint extends AbstractConstraint {

		private int minSize;


		/**
		 * @return Returns the minSize.
		 */
		public int getMinSize() {
			return minSize;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Integer)) 
				throw new IllegalArgumentException("Parameter for constraint ["+MIN_SIZE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a of type [java.lang.Integer]");
			
			this.minSize = ((Integer)constraintParameter).intValue();
			super.setParameter(constraintParameter);
		}


		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
		 */
		public boolean supports(Class type) {
			if(type == null)
				return false;
			
			return Comparable.class.isAssignableFrom(type) ||
					Number.class.isAssignableFrom(type) ||
					Collection.class.isAssignableFrom(type) ||
					type.isArray();
		}
		
		protected void processValidate(Object propertyValue, Errors errors) {
			Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue, new Integer(minSize) };
			if(propertyValue == null) {
				super.rejectValue(errors,MIN_LENGTH_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_LENGTH_MESSAGE, args ));
				return;
			}
			if(propertyValue.getClass().isArray()) {
				int length = Array.getLength( propertyValue );
				if(length < minSize) {
					super.rejectValue(errors,MIN_LENGTH_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_LENGTH_MESSAGE, args ));
					return;
				}
			}
			if(propertyValue instanceof Collection) {
				if( ((Collection)propertyValue).size() < minSize ) {
					super.rejectValue(errors,MIN_SIZE_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_SIZE_MESSAGE, args ));
					return;
				}
			}
			else if(propertyValue instanceof Number) {				
				int numberSize = ((Number)propertyValue).intValue();
				if( numberSize < minSize ) {
					super.rejectValue(errors,MIN_SIZE_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_SIZE_MESSAGE, args ));
					return;
				}
			}
			else if(propertyValue instanceof String) {
				if(((String)propertyValue ).length() < minSize) {
					super.rejectValue(errors,MIN_LENGTH_CONSTRAINT + NOTMET_SUFFIX,args,MessageFormat.format( DEFAULT_INVALID_MIN_LENGTH_MESSAGE, args ));
					return;
				}
			}			
		}		
	}	
	
	public ConstrainedProperty(Class clazz,String propertyName, Class propertyType) {
		super();
		this.owningClass = clazz;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.bean = new BeanWrapperImpl(this);
	}

	
	/**
	 * @return Returns the appliedConstraints.
	 */
	public Collection getAppliedConstraints() {
		return appliedConstraints.values();
	}

	/**
	 * @return Returns the propertyType.
	 */
	public Class getPropertyType() {
		return propertyType;
	}


	/**
	 * @return Returns the max.
	 */
	public Comparable getMax() {
		MaxConstraint c = (MaxConstraint)this.appliedConstraints.get( MAX_CONSTRAINT );
		if(c == null) {
			Range r = getRange();
			if(r == null) {
				return null;
			}
			else {
				return r.getTo();
			}
		}
		return c.getMaxValue();		
	}


	/**
	 * @param max The max to set.
	 */
	public void setMax(Comparable max) {
		if(!propertyType.equals( max.getClass() )) {
			throw new MissingPropertyException(MAX_CONSTRAINT,propertyType);
		}
		if(max == null) {
			this.appliedConstraints.remove( MAX_CONSTRAINT );
			return;
		}
		Range r = getRange();
		if(r != null) {
			LOG.warn("Range constraint already set ignoring constraint ["+MAX_CONSTRAINT+"] for value ["+max+"]");
			return;
		}
		Constraint c = (MaxConstraint)this.appliedConstraints.get( MAX_CONSTRAINT );
		if(c != null) {
			c.setParameter(max);		
		}
		else {
			c = new MaxConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(max);
			this.appliedConstraints.put( MAX_CONSTRAINT, c );
		}
	}


	/**
	 * @return Returns the min.
	 */
	public Comparable getMin() {
		MinConstraint c = (MinConstraint)this.appliedConstraints.get( MIN_CONSTRAINT );
		if(c == null) {
			Range r = getRange();
			if(r == null) {
				return null;
			}
			else {
				return r.getFrom();
			}
		}
		return c.getMinValue();		
	}


	/**
	 * @param min The min to set.
	 */
	public void setMin(Comparable min) {
		if(!propertyType.equals( min.getClass() )) {
			throw new MissingPropertyException(MIN_CONSTRAINT,propertyType);
		}
		if(min == null) {
			this.appliedConstraints.remove( MIN_CONSTRAINT );
			return;
		}
		Range r = getRange();
		if(r != null) {
			LOG.warn("Range constraint already set ignoring constraint ["+MIN_CONSTRAINT+"] for value ["+min+"]");
			return;
		}
		Constraint c = (MinConstraint)this.appliedConstraints.get( MIN_CONSTRAINT );
		if(c != null) {
			c.setParameter(min);			
		}
		else {
			c = new MinConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(min);			
			this.appliedConstraints.put( MIN_CONSTRAINT, c );
		}
	}


	/**
	 * @return Returns the inList.
	 */
	public List getInList() {
		InListConstraint c = (InListConstraint)this.appliedConstraints.get( IN_LIST_CONSTRAINT );
		if(c == null)
			return null;
			
		return c.getList();
	}

	/**
	 * @param inList The inList to set.
	 */
	public void setInList(List inList) {
		Constraint c = (Constraint)this.appliedConstraints.get( IN_LIST_CONSTRAINT );
		if(inList == null) {
			this.appliedConstraints.remove( IN_LIST_CONSTRAINT );			
		}
		else {
			if(c != null) {
				c.setParameter(inList);
			}
			else {
				c = new InListConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(inList);			
				this.appliedConstraints.put( IN_LIST_CONSTRAINT, c );
			}
		}
	}

	/**
	 * @return Returns the range.
	 */
	public Range getRange() {
		RangeConstraint c = (RangeConstraint)this.appliedConstraints.get( RANGE_CONSTRAINT );
		if(c == null)
			return null;
			
		return c.getRange();
	}

	/**
	 * @param range The range to set.
	 */
	public void setRange(Range range) {				
		if(this.appliedConstraints.containsKey( MAX_CONSTRAINT )) {
			LOG.warn("Setting range constraint on property ["+propertyName+"] of class ["+owningClass+"] forced removal of max constraint");
			this.appliedConstraints.remove( MAX_CONSTRAINT );
		}
		if(this.appliedConstraints.containsKey( MIN_CONSTRAINT )) {
			LOG.warn("Setting range constraint on property ["+propertyName+"] of class ["+owningClass+"] forced removal of min constraint");
			this.appliedConstraints.remove( MIN_CONSTRAINT );
		}
		if(range == null) {
			this.appliedConstraints.remove( RANGE_CONSTRAINT );			
		}
		else {
			Constraint c = (Constraint)this.appliedConstraints.get(RANGE_CONSTRAINT);
			if(c != null) {
				c.setParameter(range);
			}
			else {
				c = new RangeConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(range);		
				
				this.appliedConstraints.put( RANGE_CONSTRAINT,c);
			}
		}
	}

	/**
	 * @return Returns the length.
	 */
	public IntRange getLength() {
		if(!String.class.isInstance( propertyType ) || !propertyType.isArray()) {
			throw new MissingPropertyException("Length constraint only applies to a String or Array property",LENGTH_CONSTRAINT,owningClass);
		}		
		SizeConstraint c = (SizeConstraint)this.appliedConstraints.get( LENGTH_CONSTRAINT );
		if(c == null)
			return null;
			
		return (IntRange)c.getSizeRange();
	}

	/**
	 * @param length The length to set.
	 */
	public void setLength(IntRange length) {
		if(!String.class.isInstance( propertyType ) || !propertyType.isArray()) {
			throw new MissingPropertyException("Length constraint can only be applied to a String or Array property",LENGTH_CONSTRAINT,owningClass);
		}			
		Constraint c = (Constraint)this.appliedConstraints.get( LENGTH_CONSTRAINT );
		if(length == null) {
			this.appliedConstraints.remove( LENGTH_CONSTRAINT );			
		}
		else {
			if(c != null) {
				c.setParameter(length);
			}
			else {
				c = new SizeConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(length);					
				this.appliedConstraints.put( LENGTH_CONSTRAINT, c );
			}			
		}
	}

	
	/**
	 * @return Returns the size.
	 */
	public Range getSize() {
		SizeConstraint c = (SizeConstraint)this.appliedConstraints.get( SIZE_CONSTRAINT );
		if(c == null)
			return null;
			
		return c.getSizeRange();
	}


	/**
	 * @param size The size to set.
	 */
	public void setSize(Range size) {
		Constraint c = (Constraint)this.appliedConstraints.get( SIZE_CONSTRAINT );
		if(size == null) {
			this.appliedConstraints.remove( SIZE_CONSTRAINT );			
		}
		else {
			if(c != null) {
				c.setParameter(size);
			}
			else {
				c = new SizeConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(size);					
				this.appliedConstraints.put( SIZE_CONSTRAINT, c );
			}			
		}
	}


	/**
	 * @return Returns the blank.
	 */
	public boolean isBlank() {
		return this.appliedConstraints.containsKey(BLANK_CONSTRAINT);
	}

	/**
	 * @param blank The blank to set.
	 */
	public void setBlank(boolean blank) {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("Blank constraint can only be applied to a String property",BLANK_CONSTRAINT,owningClass);
		}
		
		Constraint c = (Constraint)this.appliedConstraints.get( BLANK_CONSTRAINT );
		if(c != null) {
			c.setParameter( new Boolean(blank) );				
		}
		else {
			c = new BlankConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Boolean(blank));					
			this.appliedConstraints.put( BLANK_CONSTRAINT,c );				
		}		

	}



	/**
	 * @return Returns the email.
	 */
	public boolean isEmail() {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("Email constraint only applies to a String property",EMAIL_CONSTRAINT,owningClass);
		}
		
		return this.appliedConstraints.containsKey( EMAIL_CONSTRAINT );
	}

	/**
	 * @param email The email to set.
	 */
	public void setEmail(boolean email) {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("Email constraint can only be applied to a String property",EMAIL_CONSTRAINT,owningClass);
		}
		
		Constraint c = (Constraint)this.appliedConstraints.get( EMAIL_CONSTRAINT );
		if(email) {			
			if(c != null) {
				c.setParameter( new Boolean(email) );				
			}
			else {
				c = new EmailConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(new Boolean(email));					
				this.appliedConstraints.put( EMAIL_CONSTRAINT,c );				
			}
		}
		else {
			if(c != null) {
				this.appliedConstraints.remove( EMAIL_CONSTRAINT );
			}
		}		

	}


    /**
     * @return Returns the creditCard.
     */
    public boolean isCreditCard() {
        if(!String.class.isInstance( propertyType )) {
            throw new MissingPropertyException("CreditCard constraint only applies to a String property",CREDIT_CARD_CONSTRAINT,owningClass);
        }

        return this.appliedConstraints.containsKey( CREDIT_CARD_CONSTRAINT );
    }

    /**
     * @param creditCard The creditCard to set.
     */
    public void setCreditCard(boolean creditCard) {
        if(!String.class.isInstance( propertyType )) {
            throw new MissingPropertyException("CreditCard constraint only applies to a String property",CREDIT_CARD_CONSTRAINT,owningClass);
        }

        Constraint c = (Constraint)this.appliedConstraints.get( CREDIT_CARD_CONSTRAINT );
        if(creditCard) {
            if(c != null) {
                c.setParameter( new Boolean(creditCard) );
            }
            else {
                c = new CreditCardConstraint();
                c.setOwningClass(this.owningClass);
                c.setPropertyName(this.propertyName);
                c.setParameter(new Boolean(creditCard));
                this.appliedConstraints.put( CREDIT_CARD_CONSTRAINT,c );
            }
        }
        else {
            if(c != null) {
                this.appliedConstraints.remove( CREDIT_CARD_CONSTRAINT );
            }
        }

    }

    /**
	 * @return Returns the matches.
	 */
	public String getMatches() {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("Matches constraint only applies to a String property",MATCHES_CONSTRAINT,owningClass);
		}		
		MatchesConstraint c = (MatchesConstraint)this.appliedConstraints.get( MATCHES_CONSTRAINT );
		if(c == null)
			return null;
		
		return c.getRegex();
	}

	/**
	 * @param regex The matches to set.
	 */
	public void setMatches(String regex) {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("Matches constraint can only be applied to a String property",MATCHES_CONSTRAINT,owningClass);
		}
		
		Constraint c = (Constraint)this.appliedConstraints.get( MATCHES_CONSTRAINT );
		if(regex == null) {
			this.appliedConstraints.remove( MATCHES_CONSTRAINT );			
		}
		else {
			if(c != null) {
				c.setParameter( regex );				
			}
			else {
				c = new MatchesConstraint();
				c.setOwningClass(this.owningClass);
				c.setPropertyName(this.propertyName);
				c.setParameter(regex);					
				this.appliedConstraints.put( MATCHES_CONSTRAINT,c );				
			}			
		}
	}


	/**
	 * @return Returns the maxLength.
	 */
	public int getMaxLength() {
		MaxSizeConstraint c = (MaxSizeConstraint)this.appliedConstraints.get( MAX_LENGTH_CONSTRAINT );

        if(c == null) {
            if(this.appliedConstraints.containsKey( LENGTH_CONSTRAINT )) {
                SizeConstraint sc = (SizeConstraint)this.appliedConstraints.get(LENGTH_CONSTRAINT);
                return sc.getSizeRange().getToInt();
            }
            return Integer.MAX_VALUE;
        }

		
		return c.getMaxSize();
	}

	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxLength(int maxLength) {
		Constraint c = (MaxSizeConstraint)this.appliedConstraints.get( MAX_LENGTH_CONSTRAINT );
		if( c != null) {
			c.setParameter( new Integer(maxLength));
		}
		else {
			c = new MaxSizeConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Integer(maxLength));					
			this.appliedConstraints.put( MAX_LENGTH_CONSTRAINT,c );			
		}		
	}




	/**
	 * @return Returns the minLength.
	 */
	public int getMinLength() {
		MinSizeConstraint c = (MinSizeConstraint)this.appliedConstraints.get( MIN_LENGTH_CONSTRAINT );
		if(c == null) {
            if(this.appliedConstraints.containsKey( LENGTH_CONSTRAINT )) {
                SizeConstraint sc = (SizeConstraint)this.appliedConstraints.get(LENGTH_CONSTRAINT);
                return sc.getSizeRange().getFromInt();
            }
            return 0;
        }

		return c.getMinSize();
	}


	/**
	 * @param minLength The minLength to set.
	 */
	public void setMinLength(int minLength) {
		Constraint c = (MinSizeConstraint)this.appliedConstraints.get( MIN_LENGTH_CONSTRAINT );
		if( c != null) {
			c.setParameter( new Integer(minLength));
		}
		else {
			c = new MinSizeConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Integer(minLength));					
			this.appliedConstraints.put( MIN_LENGTH_CONSTRAINT,c );			
		}	
	}


	
	
	/**
	 * @return Returns the notEqual.
	 */
	public Object getNotEqual() {
		NotEqualConstraint c = (NotEqualConstraint)this.appliedConstraints.get( NOT_EQUAL_CONSTRAINT );
		if(c == null)
			return null;
			
		return c.getNotEqualTo();
	}

	/**
	 * @return Returns the maxSize.
	 */
	public int getMaxSize() {
		MaxSizeConstraint c = (MaxSizeConstraint)this.appliedConstraints.get( MAX_SIZE_CONSTRAINT );
		if(c == null)
			return Integer.MAX_VALUE;
		
		return c.getMaxSize();
	}

	/**
	 * @param mazSize The mazSize to set.
	 */
	public void setMaxSize(int mazSize) {
		Constraint c = (MaxSizeConstraint)this.appliedConstraints.get( MAX_SIZE_CONSTRAINT );
		if( c != null) {
			c.setParameter( new Integer(mazSize));
		}
		else {
			c = new MaxSizeConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Integer(mazSize));					
			this.appliedConstraints.put( MAX_SIZE_CONSTRAINT,c );			
		}		
	}




	/**
	 * @return Returns the minSize.
	 */
	public int getMinSize() {
		MinSizeConstraint c = (MinSizeConstraint)this.appliedConstraints.get( MIN_SIZE_CONSTRAINT );
		if(c == null)
			return Integer.MIN_VALUE;
		
		return c.getMinSize();
	}


	/**
	 * @param minSize The minLength to set.
	 */
	public void setMinSize(int minSize) {
		Constraint c = (MinSizeConstraint)this.appliedConstraints.get( MIN_SIZE_CONSTRAINT );
		if( c != null) {
			c.setParameter( new Integer(minSize));
		}
		else {
			c = new MinSizeConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Integer(minSize));					
			this.appliedConstraints.put( MIN_SIZE_CONSTRAINT,c );			
		}	
	}	
	/**
	 * @param notEqual The notEqual to set.
	 */
	public void setNotEqual(Object notEqual) {
		if(notEqual == null) {
			this.appliedConstraints.remove( NOT_EQUAL_CONSTRAINT );			
		}
		else {
			Constraint c = new NotEqualConstraint();
			c.setOwningClass(owningClass);
			c.setPropertyName(propertyName);
			c.setParameter(notEqual);
			this.appliedConstraints.put( NOT_EQUAL_CONSTRAINT, c );
		}
	}

	

	/**
	 * @return Returns the nullable.
	 */
	public boolean isNullable() {
		return this.appliedConstraints.containsKey(NULLABLE_CONSTRAINT);
	}


	/**
	 * @param nullable The nullable to set.
	 */
	public void setNullable(boolean nullable) {
		if(nullable) {
			this.appliedConstraints.remove(NULLABLE_CONSTRAINT);
		}
		else {
			Constraint c = new NullableConstraint();
			c.setOwningClass(owningClass);
			c.setPropertyName(propertyName);
			c.setParameter(new Boolean(nullable));
			this.appliedConstraints.put( NULLABLE_CONSTRAINT, c );			
		}
	}


	/**
	 * @return Returns the propertyName.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName The propertyName to set.
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	/**
	 * @return Returns the url.
	 */
	public boolean isUrl() {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("URL constraint can only be applied to a String property",URL_CONSTRAINT,owningClass);
		}		
		return this.appliedConstraints.containsKey( URL_CONSTRAINT );
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(boolean url) {
		if(!String.class.isInstance( propertyType )) {
			throw new MissingPropertyException("URL constraint can only be applied to a String property",URL_CONSTRAINT,owningClass);
		}		
		Constraint c = (Constraint)this.appliedConstraints.get( URL_CONSTRAINT );		
		if(url) {			
			if(c != null) {
				c.setParameter(new Boolean(url));
			}
			else {
				c = new UrlConstraint();
				c.setOwningClass(owningClass);
				c.setPropertyName(propertyName);
				c.setParameter(new Boolean(url));				
				this.appliedConstraints.put( URL_CONSTRAINT, c );
			}
		}
		else {
			if(c != null) {
				this.appliedConstraints.remove( URL_CONSTRAINT );
			}
		}
	}

	
	/**
	 * @return Returns the display.
	 */
	public boolean isDisplay() {
		return display;
	}


	/**
	 * @param display The display to set.
	 */
	public void setDisplay(boolean display) {
		this.display = display;
	}


	/**
	 * @return Returns the editable.
	 */
	public boolean isEditable() {
		return editable;
	}


	/**
	 * @param editable The editable to set.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}


	/**
	 * @return Returns the order.
	 */
	public int getOrder() {
		return order;
	}


	/**
	 * @param order The order to set.
	 */
	public void setOrder(int order) {
		this.order = order;
	}

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public Map getAttributes() {
        return attributes;
    }
    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    /**
	 * Validate this constrainted property against specified property value
	 * 
	 * @param propertyValue
	 * @param errors
	 */
	public void validate(Object propertyValue, Errors errors) {
		
		for (Iterator i = this.appliedConstraints.values().iterator(); i.hasNext();) {
			Constraint c = (Constraint) i.next();			
			c.validate( propertyValue, errors);
		}
	}

	/**
	 * Checks with this ConstraintedProperty instance supports applying the specified constraint
	 * 
	 * @param constraintName The name of the constraint
	 * @return True if the constraint is supported
	 */
	public boolean supportsContraint(String constraintName) {
		
		if(!constraints.containsKey(constraintName)) {
			if(!this.bean.isWritableProperty(constraintName))
				return false;
			else
				return true;
		}					
		Class constraintClass = (Class)constraints.get(constraintName);
		try {
			Constraint c = (Constraint)constraintClass.newInstance();
			return c.supports(propertyType);
			
		} catch (Exception e) {
			LOG.error("Exception thrown instantiating constraint ["+constraintName+"] to class ["+owningClass+"]", e);
			throw new ConstraintException("Exception thrown instantiating  constraint ["+constraintName+"] to class ["+owningClass+"]");
		}		
	}

	/**
	 * Applies a constraint for the specified name and consraint value
	 * 
	 * @param constraintName The name of the constraint
	 * @param constrainingValue The constraining value
	 * 
	 * @throws ConstraintException Thrown when the specified constraint is not supported by this ConstrainedProperty. Use <code>supportsContraint(String constraintName)</code> to check before calling 
	 */
	public void applyConstraint(String constraintName, Object constrainingValue) {
		
		if(constraints.containsKey(constraintName)) {
			if(constrainingValue == null) {
				this.appliedConstraints.remove(constraintName);
			}
			else {
				Class constraintClass = (Class)constraints.get(constraintName);
				try {
					Constraint c = (Constraint)constraintClass.newInstance();
					c.setOwningClass(this.owningClass);
					c.setPropertyName(this.propertyName);
					c.setParameter( constrainingValue );
					this.appliedConstraints.put( constraintName, c );
				} catch (Exception e) {
					LOG.error("Exception thrown applying constraint ["+constraintName+"] to class ["+owningClass+"] for value ["+constrainingValue+"]: " + e.getMessage(), e);
					throw new ConstraintException("Exception thrown applying constraint ["+constraintName+"] to class ["+owningClass+"] for value ["+constrainingValue+"]: " + e.getMessage());
				}
			}
		}
		else if(this.bean.isWritableProperty(constraintName)) {
			this.bean.setPropertyValue( constraintName, constrainingValue );
		}
		else {
			throw new ConstraintException("Constraint ["+constraintName+"] is not supported for property ["+propertyName+"] of class ["+owningClass+"] with type ["+propertyType+"]");
		}

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
						.append( this.owningClass )
						.append( this.propertyName )
						.append( this.propertyType )
						.append( this.appliedConstraints )
						.toString();
	}


}
