var Grails = {
  Version: '0.1a-alphha'
}
// additional prototype
String.prototype.trim = function() {
   return this.replace(/^\s*|\s*$/g,"");
}

Grails.InnerHTMLUpdater = Class.create();
Grails.InnerHTMLUpdater.prototype = {
	
	initialize: function(element) {
		this.element = element;
		this.onComplete = null;
		this.options = null;
		this.debug = false;
		
		if(arguments.length > 1) {
			this.options = arguments[1];
		}
		if(this.options != null) {
			this.onComplete = this.options.complete;	
			if(this.options.debug != undefined) {
				this.debug = this.options.debug;
			}
		}
	},
	
	ajaxUpdate: function(ajaxResponse) {
		if(this.debug) {
			alert( "Ajax Results: " + RicoUtil.getContentAsString(ajaxResponse) );
		}
		$(this.element).innerHTML = RicoUtil.getContentAsString(ajaxResponse);

		if(this.onComplete != null) {
			this.onComplete( this.element );
		}	
		
	}
}

/**
 * DHTMLHelper class for attaching events, locating positions and creating elements etc.
 */
Grails.DHTMLHelper = Class.create();
Grails.DHTMLHelper.prototype = {
	initialize: function(){
		this.isIE = navigator.userAgent.toLowerCase().indexOf("msie") >= 0;
	},
	createElement: function(type,container) {
		var el = null;
		if (document.createElementNS) {
			el = document.createElementNS("http://www.w3.org/1999/xhtml", type);
		} else {
			el = document.createElement(type);
		}
		if (container != undefined) {
			container.appendChild(el);
		}
		return el;	
	},
	getAbsolutePos: function(el) {
		var SL = 0, ST = 0;
		var is_div = /^div$/i.test(el.tagName);
		if (is_div && el.scrollLeft)
			SL = el.scrollLeft;
		if (is_div && el.scrollTop)
			ST = el.scrollTop;
		var r = { x: el.offsetLeft - SL, y: el.offsetTop - ST };
		if (el.offsetParent) {
			var tmp = this.getAbsolutePos(el.offsetParent);
			r.x += tmp.x;
			r.y += tmp.y;
		}
		return r;
	},	
	removeClass: function(element, className) {
		if (!(element && element.className)) {
			return;
		}
		var classNames = element.className.split(" ");
		var a = new Array();
		for(var i = 0; i < classNames.length; i++) {
			if(classNames[i] != className)
				a.push(classNames[i]);
		}
		element.className = a.join(" ");
	},
	addClass: function(element, className) {
		this.removeClass(element, className);
		element.className += " " + className;
	},
	addEvent: function(el, evname, func) {
		if (el.attachEvent) { // IE
			el.attachEvent("on" + evname, func);
		} else if (el.addEventListener) { // Gecko / W3C
			el.addEventListener(evname, func, true);
		} else {
			el["on" + evname] = func;
		}
	},
	removeEvent: function(el, evname, func) {
		if (el.detachEvent) { // IE
			el.detachEvent("on" + evname, func);
		} else if (el.removeEventListener) { // Gecko / W3C
			el.removeEventListener(evname, func, true);
		} else {
			el["on" + evname] = null;
		}
	}
}
var dhtmlHelper = new Grails.DHTMLHelper();

/**
 * Creates an auto-complete field from the supplied parameters
 * @param container The parent of the element to place the results into 
 * @param id The id of the div to place the results into
 * @param field The field which requires auto-complete
 * @param delegate A function which takes the field as an argument
 * @param parent An options map
 */
Grails.AutoComplete = Class.create();
Grails.AutoComplete.prototype = {
	initialize: function(id, field, url,options) {
		this.container = document.body;
		this.id = id;
		this.autoCompleteField = $(field);
		this.url = url;
		this.options = options;
		this.minLength = 1;
		this.onDisplay = null;
		this.onSelect = null;
		this.cache = new Array();
		this.useCache = true;
		this.useCSS = false;
		this.fontColor = "darkblue";
		this.bgColor = "white";
		this.overColor = "white";
		this.overBgColor = "darkblue";
		this.appendResult = false;
		
		if(this.options != null) {
			if(this.options.parent != null)
				this.container = $(parent);
			if(this.options.display != null)
				this.onDisplay = this.options.display;
			if(this.options.minLength != null)
				this.minLength = this.options.minLength;			
			if(this.options.appendResult != null)
				this.appendResult = this.options.appendResult;				
			if(this.options.select != null)
				this.onSelect = this.options.select;
			if(this.options.css != null)
				this.useCSS = this.options.css;
			if(this.options.cache != null)
				this.useCache = this.options.cache;			
			if(this.options.fontColor != null)
				this.fontColor = this.options.fontColor;
			if(this.options.bgColor != null)
				this.bgColor = this.options.bgColor;
			if(this.options.overColor != null)
				this.overColor = this.options.overColor;
			if(this.options.overBgColor != null)
				this.overBgColor = this.options.overBgColor;			
		}
		this._registerRequestInfo();
		this._createAutoCompleteField();
	},
	_registerRequestInfo: function() {
		ajaxEngine.registerRequest(this.id,this.url);
		ajaxEngine.registerAjaxObject(this.id, this);			
	},
	_createAutoCompleteField: function() {
		var parent = this.container;
		var el = dhtmlHelper.createElement("div", parent);
		el.id = this.id;
		el.className = "autoComplete " + this.id;
		el.style.display = "none";
		el.style.position = "absolute";
		
		dhtmlHelper.addEvent(
			this.autoCompleteField,
			"blur",
			function(e) {
				if(el != null)
				setTimeout( function() { el.style.display = "none" }, 500)
			}
		)
		dhtmlHelper.addEvent(
			this.autoCompleteField,
			"keyup",
			this._keyUpEventHandler.bindAsEventListener(this)		
		);			
	},
	_keyUpEventHandler: function(e) {
		if(!e)e = window.event;
		if(e.keyCode == 40 || e.keyCode == 38) {
			var autoCompleteResults = document.getElementsByTagAndClassName("div", "autoCompleteResult");
			var autoCompleteResultsSelected = document.getElementsByTagAndClassName("div", "autoCompleteResultOver");					
			// if none are selected select the first result
			if(autoCompleteResultsSelected.length == 0) {
				if(autoCompleteResults.length > 0){
					dhtmlHelper.removeClass(autoCompleteResults[0],"autoCompleteResult");
					dhtmlHelper.addClass(autoCompleteResults[0],"autoCompleteResultOver");
					if(this.appendResult)
						this._handleTextSelection(autoCompleteResults[0]);
				}
			}
			// otherwise get the current selected index and select the next one
			else {
				var selectedIndex = 0;
				var acHolder = autoCompleteResultsSelected[0].parentNode;
				var acHolderChildren = acHolder.getElementsByTagName("div");
				for(var i = 0; i < acHolderChildren.length;i++ ) {
					if(autoCompleteResultsSelected[0] == acHolderChildren[i]) {
						selectedIndex = i;
						break;
					}
				}
				dhtmlHelper.removeClass(acHolderChildren[selectedIndex],"autoCompleteResultOver");
				dhtmlHelper.addClass(acHolderChildren[selectedIndex],"autoCompleteResult");													
				
				if(e.keyCode == 40) {
					selectedIndex++;
				}else {
					selectedIndex--;
				}
				if(selectedIndex > -1 && selectedIndex < acHolderChildren.length) {
					dhtmlHelper.removeClass(acHolderChildren[selectedIndex],"autoCompleteResult");
					dhtmlHelper.addClass(acHolderChildren[selectedIndex],"autoCompleteResultOver");
					if(this.appendResult)
						this._handleTextSelection(acHolderChildren[selectedIndex]);
				}
			}
		}
		else if(e.keyCode == 13 && !this.appendResult) {
			var autoCompleteResultsSelected = document.getElementsByTagAndClassName("div", "autoCompleteResultOver");
			if(autoCompleteResultsSelected.length > 0) {
				// call click handler pretending this is an event
				this._resultClickHandler( { target:autoCompleteResultsSelected[0] } );
			}
		}
		// if search if enter is not pressed
		else if(e.keyCode != 13) {
			if(this.autoCompleteField.value.length > this.minLength) {
				if(this.useCache) {
					// if available in the cache
					if(this.cache[this.autoCompleteField.value] != null) {
						this._displayCachedResults(this.autoCompleteField.value);
					}
					else {
						ajaxEngine.sendRequest(this.id, 
												this.autoCompleteField.name + "=" + this.autoCompleteField.value);	
					}
				}
				else {
					ajaxEngine.sendRequest(this.id,
											this.autoCompleteField.name + "=" + this.autoCompleteField.value);
				}
			}
		}		
	},
	ajaxUpdate: function(ajaxResponse) {
		// cache the response
		var ajaxResponseAsString = RicoUtil.getContentAsString(ajaxResponse);
		this.cache[this.autoCompleteField.value] = ajaxResponseAsString;
		$(this.id).innerHTML = ajaxResponseAsString;
		
		var autoCompleteResults = document.getElementsByTagAndClassName("div", "autoCompleteResult");
		var autoCompleteResultsSelected = document.getElementsByTagAndClassName("div", "autoCompleteResultOver");					
		// if none are selected select the first result
		if(autoCompleteResultsSelected.length == 0 && this.appendResult) {
			if(autoCompleteResults.length > 0){
				dhtmlHelper.removeClass(autoCompleteResults[0],"autoCompleteResult");
				dhtmlHelper.addClass(autoCompleteResults[0],"autoCompleteResultOver");
				
				this._handleTextSelection(autoCompleteResults[0]);
			}
		}		
		// attach behaviours						
		this._attachResultBehaviours(autoCompleteResults);
		// display the results
		this._displayResults(autoCompleteResults);
	},
	_displayCachedResults: function(key) {
		var el = $(this.id);
		el.innerHTML = this.cache[this.autoCompleteField.value];
		var autoCompleteResults = document.getElementsByTagAndClassName("div", "autoCompleteResult");
		var autoCompleteResultsSelected = document.getElementsByTagAndClassName("div", "autoCompleteResultOver");					
		// if none are selected select the first result
		if(autoCompleteResultsSelected.length == 0 && this.appendResult) {
			if(autoCompleteResults.length > 0){
				dhtmlHelper.removeClass(autoCompleteResults[0],"autoCompleteResult");
				dhtmlHelper.addClass(autoCompleteResults[0],"autoCompleteResultOver");
				
				this._handleTextSelection(autoCompleteResults[0]);
			}
		}			
		// attach behaviours						
		this._attachResultBehaviours(autoCompleteResults);
		// display the results
		this._displayResults(autoCompleteResults);		
	},	
	_handleTextSelection: function(selectedResult) {

		var resultContent = selectedResult.innerHTML;
		if(resultContent.indexOf(this.autoCompleteField.value) == 0) {
			var i = resultContent.indexOf(	this.autoCompleteField.value );
			var appendValue = resultContent.substring((i + 	this.autoCompleteField.value.length),resultContent.length);
			this.autoCompleteField.value = this.autoCompleteField.value + appendValue;
		}
		else {
			if(dhtmlHelper.isIE) {
				if(document.selection != null){
					var selectionRange = document.selection.createRange();
					selectionRange.text = "";					
				}
				var oldLength = this.autoCompleteField.value.length;
				this.autoCompleteField.value = this.autoCompleteField.value + resultContent;
				var range = this.autoCompleteField.createTextRange();
				range.moveStart("character",oldLength);
				range.moveEnd("character",this.autoCompleteField.value.length);
				range.select();
			}
			else {
				if (this.autoCompleteField.selectionEnd > 0) {
						this.autoCompleteField.value = this.autoCompleteField.value.substring(0,this.autoCompleteField.selectionStart);
				}
				var oldLength = this.autoCompleteField.value.length;
				this.autoCompleteField.value = this.autoCompleteField.value + resultContent;
				this.autoCompleteField.selectionStart = oldLength;
				this.autoCompleteField.selectionEnd = this.autoCompleteField.value.length;
			}
		}		
	},
	_displayResults: function(results) {
		var pos = dhtmlHelper.getAbsolutePos(this.autoCompleteField);
		var el = $(this.id);		
		// position results pane
		el.style.left = pos.x + "px";
		el.style.top = (pos.y + this.autoCompleteField.offsetHeight) + "px";
		// if there are no results hide
		if(results.length == 0) {
			el.style.display = "none";
			return;
		}
		else {				
			el.style.display = "";	
			if(this.onDisplay != null) {
				this.onDisplay( el );
			}
		}		
	},
	_attachResultBehaviours: function(results) {
		for(var i = 0; i < results.length;i++) {
			dhtmlHelper.addEvent(
				results[i],
				"mouseover",
				this._resultMouseOverHandler.bindAsEventListener(this)
			);
			dhtmlHelper.addEvent(
				results[i],
				"mouseout",
				this._resultMouseOutHandler.bindAsEventListener(this)
			);
			dhtmlHelper.addEvent(
				results[i],
				"click",
				this._resultClickHandler.bindAsEventListener(this)
			);	
		}
	},
	_resultMouseOverHandler: function(e) {
		if(!e)e = window.event;
		var src = e.srcElement;
		if(!src)src = e.target;
		
		
		dhtmlHelper.removeClass(src,"autoCompleteResult");
		dhtmlHelper.addClass(src,"autoCompleteResultOver");
		if(!this.useCSS) {
			src.style.bgColor = this.overBgColor;
			src.style.fontColor = this.overColor;
		}
	},
	_resultMouseOutHandler: function(e) {
		if(!e)e = window.event;
		var src = e.srcElement;
		if(!src)src = e.target;					
									
		dhtmlHelper.removeClass(src,"autoCompleteResultOver");
		dhtmlHelper.addClass(src,"autoCompleteResult");
		
		if(!this.useCSS) {
			src.style.bgColor = this.bgColor;
			src.style.fontColor = this.fontColor;
		}
	},
	_resultClickHandler: function(e) {
		if(!e)e = window.event;
		
		var src = e.srcElement;
		if(!src)src = e.target;
		 
		if(this.appendResult) {
			this._handleTextSelection(src);
		}
		else {
			this.autoCompleteField.value = src.innerHTML;	
		}
		$(this.id).style.display = "none";
		if(this.onSelect != null) {
			this.onSelect( $(this.id), src );
		}		
	}
}



function createParamsForFields(fieldClass) {
	var inputs = document.getElementsByTagAndClassName("INPUT", fieldClass);	
	var selects = document.getElementsByTagAndClassName("SELECT", fieldClass);	
	var params = new Array();
	
	// process inputs first
	for(var i = 0; i < inputs.length; i++ ) {
		// if its a radio button only include checked value
		if(inputs[i].type == "radio") {
			if(inputs[i].checked) {
				params[i] = {
					name: inputs[i].name,
					value: inputs[i].value
				};			
			}
		}
		else if(inputs[i].type == "checkbox") {
			if(inputs[i].checked) {
				params[i] = {
					name: inputs[i].name,
					value: true
				};			
			}
			else {
				params[i] = {
					name: inputs[i].name,
					value: false
				};				
			}
		}
		else {
			params[i] = {
				name: inputs[i].name,
				value: inputs[i].value
			};
		}
	}
	// process selects
	for(var i = 0, j = params.length; i < selects.length; i++,j++) {
		params[j] = {
			name: selects[i].name,
			value: selects[i].options[ selects[i].selectedIndex ].value
		};
	}
	return params;
}


/**
 * function for retrieving elements for the tag and className from an HTML element
 */
function getElementsByTagAndClassName(element,tagName, className) {
  if ( tagName == null )
     tagName = '*';

  var children = element.getElementsByTagName(tagName) || document.all;
  var elements = new Array();

  if ( className == null )
    return children;

  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    if(child.className == null) {
    	continue;
    }
    var classNames = child.className.split(' ');
    for (var j = 0; j < classNames.length; j++) {
      if (classNames[j] == className) {
        elements.push(child);
        break;
      }
    }
  }

  return elements;
}

