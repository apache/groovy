<% if(value == null) value = 0; %>
<% if(constraints != null) {
    if(constraints.range != null) {
        %>
        <select name="<%=name%>"
                <% constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } %>>
            <% for(i in constraints.range) {%>
                 <option value="<%=i%>"  <%if(i == value) {%>selected="selected"<%}%>><%=i%></option>
            <%}%>
        </select>
    <%} else {%>
       <input name="<%=name%>"
              value="<%=value%>"
               <% constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } %>/>
    <%}%>
<% } else {
    %><input name="<%=name%>" value="<%=value%>" /><%
   } %>