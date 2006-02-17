<% if (value == null) value = "" %>
<% if(constraints != null) {
    if(constraints.maxLength < 150) { %>
        <input type="<% if(constraints.password) { %>password<% } else { %>text<%}%>"
               <% if(!constraints.editable){ %>readonly="readonly"<% } %>
               maxlength="<%=constraints.maxLength%>"
               name="<%=name%>"
               value="<%=value%>"
               <% constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } %>
               />
    <% } else { %>
    <textarea
          rows="1"
          cols="1"
          name="<%=name%>"
           <% constraints.attributes.each { k,v -> %>
             <%=k%>="<%=v%>"
           <% } %>><%=value%></textarea>
    <% } %>
<% } else { %><textarea rows="1" cols="1" name="<%=name%>"><%=value%></textarea><% } %>