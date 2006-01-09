<input type="file"
       name="<%=name%>"
        <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>/>