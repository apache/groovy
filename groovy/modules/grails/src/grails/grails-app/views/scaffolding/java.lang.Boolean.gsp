<input value="<%=value%>"
       type="checkbox"
       name="<%=name%>"
        <% if(value) { %>checked="checked"<%}%>
        <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>/>