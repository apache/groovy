<%@ page import="org.springframework.web.servlet.support.BindStatus"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<html>
   <head>
      <title>Author detail</title>
   </head>

   <body>
      <h1>Author detail</h1>
      <spring:hasBindErrors name="authorDetail">
         <c:forEach items="${errors.allErrors}" var="error">
            <span><c:out value="${error}"/></span>
         </c:forEach>
      </spring:hasBindErrors>
      <form method="POST">
         <fieldset>
            <spring:bind path="authorDetail.id">
               <input type="hidden" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" id="authorId"/>
            </spring:bind>
            <label>name</label>
            <spring:bind path="authorDetail.name">
               <input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" id="authorName">
            </spring:bind>
            <br>
            <br>
            <input type="submit" name="_eventId_save" value="Save" id="save">
         </fieldset>
      </form>
      <form method="POST">
         <input type="submit" value="Close" name="_eventId_close" id="close">
      </form>
   </body>
</html>
