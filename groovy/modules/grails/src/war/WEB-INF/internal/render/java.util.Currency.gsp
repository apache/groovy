<%
    if(value == null) {
        value = java.util.Currency.getInstance( request.getLocale() );
    }
%>
<select name="<%=name%>"
        <% if(constraints != null) { constraints.attributes.each { k,v -> %>
                 <%=k%>="<%=v%>"
               <% } } %>>
    <option value="EUR" <%if(value.currencyCode == "EUR") {%>selected="selected"<%}%>>EUR</option>
    <option value="XCD" <%if(value.currencyCode == "XCD") {%>selected="selected"<%}%>>XCD</option>
    <option value="USD" <%if(value.currencyCode == "USD") {%>selected="selected"<%}%>>USD</option>
    <option value="XOF" <%if(value.currencyCode == "XOF") {%>selected="selected"<%}%>>XOF</option>
    <option value="NOK" <%if(value.currencyCode == "NOK") {%>selected="selected"<%}%>>NOK</option>
    <option value="AUD" <%if(value.currencyCode == "AUD") {%>selected="selected"<%}%>>AUD</option>
    <option value="XAF" <%if(value.currencyCode == "XAF") {%>selected="selected"<%}%>>XAF</option>
    <option value="NZD" <%if(value.currencyCode == "NZD") {%>selected="selected"<%}%>>NZD</option>
    <option value="MAD" <%if(value.currencyCode == "MAD") {%>selected="selected"<%}%>>MAD</option>
    <option value="DKK" <%if(value.currencyCode == "DKK") {%>selected="selected"<%}%>>DKK</option>
    <option value="GBP" <%if(value.currencyCode == "GBP") {%>selected="selected"<%}%>>GBP</option>
    <option value="CHF" <%if(value.currencyCode == "CHF") {%>selected="selected"<%}%>>CHF</option>
    <option value="XPF" <%if(value.currencyCode == "XPF") {%>selected="selected"<%}%>>XPF</option>
    <option value="ILS" <%if(value.currencyCode == "ILS") {%>selected="selected"<%}%>>ILS</option>
    <option value="ROL" <%if(value.currencyCode == "ROL") {%>selected="selected"<%}%>>ROL</option>
    <option value="TRL" <%if(value.currencyCode == "TRL") {%>selected="selected"<%}%>>TRL</option>
</select>