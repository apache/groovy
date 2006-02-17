<select name="<%=name%>"
        <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>>
 <% if(value) { %>
    <option value="true" selected="selected">Yes</option>
    <option value="false">No</option>
 <%} else { %>
    <option value="true">Yes</option>
    <option value="false" selected="selected">No</option>
 <% } %>
</select>
