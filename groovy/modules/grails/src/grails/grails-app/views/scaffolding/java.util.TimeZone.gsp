<%
    if(value == null) {
        value = java.util.TimeZone.getDefault()
    }
%>
<select name="<%=name%>"
       <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>>
<%
    date = new Date()
    ids = TimeZone.getAvailableIDs();
    for(id in ids) {
        TimeZone tz = TimeZone.getTimeZone(id);
        String shortName = tz.getDisplayName(tz.inDaylightTime(date),TimeZone.SHORT);
        String longName = tz.getDisplayName(tz.inDaylightTime(date),TimeZone.LONG);

        offset = tz.rawOffset;
        hour = offset / (60*60*1000);
        min = Math.abs(offset / (60*1000)) % 60;
        %>
            <option value="<%=id%>" <%if(id == value.ID) {%>selected="selected"<%}%>><%=shortName%>, <%=longName%> <%=hour%>:<%=min%></option>
        <%
    }
%>
</select>