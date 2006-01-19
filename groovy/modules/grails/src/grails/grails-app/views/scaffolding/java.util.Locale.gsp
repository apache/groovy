<% if(value == null) value = request.getLocale(); %>
<select name="<%=name%>"
        <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>>
<%
    locales = java.util.Locale.getAvailableLocales();
    for(l in locales) {
        %>
            <option value="<%=l.language%>_<%=l.country%>" <%if("${l.language}_${l.country}" == value) {%>selected="selected"<%}%>><%=l.language%>, <%=l.country%>, <%=l.displayName%></option>
        <%
    }
%>
</select>