<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>
  <head>
	  <title>Grails Runtime Exception</title>
	  <style type="text/css">
	  		.message {
	  			border: 1px solid black;
	  			padding: 5px;
	  			background-color:#E9E9E9;
	  		}
	  		.stack {
	  			border: 1px solid black;
	  			padding: 5px;	  		
	  			overflow:auto;
	  			height: 300px;
	  		}
	  </style>
  </head>
  
  <body>
    <h1>Grails Runtime Exception</h1>
    <h2>Error Details</h2>
  	<div class="message">
  		<strong>Message:</strong> <c:out value="${exception.message}" /> <br />
  		<strong>Caused by:</strong> <c:out value="${exception.cause.message}" /> <br />
  		<strong>Class:</strong> <c:out value="${exception.className}" /> <br />  		  		
  		<strong>At Line:</strong> [<c:out value="${exception.lineNumber}" />] <br />  		
  	</div>
    <h2>Stack Trace</h2>
    <div class="stack">
	    <c:out value="${exception.stackTraceText}" />
    </div>
  </body>
</html>