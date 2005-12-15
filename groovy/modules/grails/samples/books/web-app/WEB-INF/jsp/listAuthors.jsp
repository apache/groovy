<%@ page
        import="org.springframework.web.servlet.support.BindStatus" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags"
           prefix="spring" %>

<html>
<head>
    <title>Author list</title>
</head>

<body>
<h1>Author list</h1>

<form method="POST">
<input type="submit" name="_eventId_add" value="Add author ...">
</form>
<table>
    <thead>
        <tr>
            <td>Title</td>
            <td>Author</td>
            <td>&nbsp;</td>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${authors}" var="author">
            <tr>
                <td><c:out value="${author.name}"/></td>
                <td>
                    <form method="POST">
                        <input type="hidden" name="id"
                               value="<c:out value="${author.id}"/>">
                        <input type="submit"
                        	   name="_eventId_select"
                               value="Select"
                               id="detail<c:out value="${author.id}"/>">
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
</body>
</html>
