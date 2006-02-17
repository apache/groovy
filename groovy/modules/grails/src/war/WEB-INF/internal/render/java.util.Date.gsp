<%
    if(value == null) value = new Date()
    c = new GregorianCalendar();
    c.setTime(value)
    day = c.get(GregorianCalendar.DAY_OF_MONTH)
    month = c.get(GregorianCalendar.MONTH)
    year = c.get(GregorianCalendar.YEAR)
    hour = c.get(GregorianCalendar.HOUR_OF_DAY)
    minute = c.get(GregorianCalendar.MINUTE)
%>
    <input type="hidden" name="<%=name%>" value="struct" />
    <select name="<%=name%>_day">
        <% for(i in 1..(day-1)) { %>
               <option value="<%=i%>"><%=i%></option>
        <% } %>
        <option value="<%=day%>" selected="selected"><%=day%></option>
        <%
         for(i in (day+1)..31) { %>
               <option value="<%=i%>"><%=i%></option>
        <% } %>
    </select>
    <select name="<%=name%>_month">
        <option value="1" <%if(month == 1) {%>selected="selected"<%}%>>January</option>
        <option value="2" <%if(month == 2) {%>selected="selected"<%}%>>February</option>
        <option value="3" <%if(month == 3) {%>selected="selected"<%}%>>March</option>
        <option value="4" <%if(month == 4) {%>selected="selected"<%}%>>April</option>
        <option value="5" <%if(month == 5) {%>selected="selected"<%}%>>May</option>
        <option value="6" <%if(month == 6) {%>selected="selected"<%}%>>June</option>
        <option value="7" <%if(month == 7) {%>selected="selected"<%}%>>July</option>
        <option value="8" <%if(month == 8) {%>selected="selected"<%}%>>August</option>
        <option value="9" <%if(month == 9) {%>selected="selected"<%}%>>September</option>
        <option value="10" <%if(month == 10) {%>selected="selected"<%}%>>October</option>
        <option value="11" <%if(month == 11) {%>selected="selected"<%}%>>November</option>
        <option value="12" <%if(month == 12) {%>selected="selected"<%}%>>December</option>
    </select>
    <select name="<%=name%>_year">
        <% for(i in (year - 80)..(year-1)) { %>
               <option value="<%=i%>"><%=i%></option>
        <% } %>
        <option value="<%=year%>" selected="selected"><%=year%></option>
        <% for(i in (year + 1)..(year+80)) { %>
               <option value="<%=i%>"><%=i%></option>
        <% } %>
    </select> -
    <select name="<%=name%>_hour">
        <option value="00" <%if(hour == "00") {%>selected="selected"<%}%>>00</option>
        <option value="01" <%if(hour == "01") {%>selected="selected"<%}%>>01</option>
        <option value="02" <%if(hour == "02") {%>selected="selected"<%}%>>02</option>
        <option value="03" <%if(hour == "03") {%>selected="selected"<%}%>>03</option>
        <option value="04" <%if(hour == "04") {%>selected="selected"<%}%>>04</option>
        <option value="05" <%if(hour == "05") {%>selected="selected"<%}%>>05</option>
        <option value="06" <%if(hour == "06") {%>selected="selected"<%}%>>06</option>
        <option value="07" <%if(hour == "07") {%>selected="selected"<%}%>>07</option>
        <option value="08" <%if(hour == "08") {%>selected="selected"<%}%>>08</option>
        <option value="09" <%if(hour == "09") {%>selected="selected"<%}%>>09</option>
        <option value="10" <%if(hour == "10") {%>selected="selected"<%}%>>10</option>
        <option value="11" <%if(hour == "11") {%>selected="selected"<%}%>>11</option>
        <option value="12" <%if(hour == "12") {%>selected="selected"<%}%>>12</option>
        <option value="13" <%if(hour == "13") {%>selected="selected"<%}%>>13</option>
        <option value="14" <%if(hour == "14") {%>selected="selected"<%}%>>14</option>
        <option value="15" <%if(hour == "15") {%>selected="selected"<%}%>>15</option>
        <option value="16" <%if(hour == "16") {%>selected="selected"<%}%>>16</option>
        <option value="17" <%if(hour == "17") {%>selected="selected"<%}%>>17</option>
        <option value="18" <%if(hour == "18") {%>selected="selected"<%}%>>18</option>
        <option value="19" <%if(hour == "19") {%>selected="selected"<%}%>>19</option>
        <option value="10" <%if(hour == "20") {%>selected="selected"<%}%>>20</option>
        <option value="21" <%if(hour == "21") {%>selected="selected"<%}%>>21</option>
        <option value="22" <%if(hour == "22") {%>selected="selected"<%}%>>22</option>
        <option value="23" <%if(hour == "23") {%>selected="selected"<%}%>>23</option>
    </select>:
    <select name="<%=name%>_minute">
        <option value="00" <%if(minute == "00") {%>selected="selected"<%}%>>00</option>
        <option value="01" <%if(minute == "01") {%>selected="selected"<%}%>>01</option>
        <option value="02" <%if(minute == "02") {%>selected="selected"<%}%>>02</option>
        <option value="03" <%if(minute == "03") {%>selected="selected"<%}%>>03</option>
        <option value="04" <%if(minute == "04") {%>selected="selected"<%}%>>04</option>
        <option value="05" <%if(minute == "05") {%>selected="selected"<%}%>>05</option>
        <option value="06" <%if(minute == "06") {%>selected="selected"<%}%>>06</option>
        <option value="07" <%if(minute == "07") {%>selected="selected"<%}%>>07</option>
        <option value="08" <%if(minute == "08") {%>selected="selected"<%}%>>08</option>
        <option value="09" <%if(minute == "09") {%>selected="selected"<%}%>>09</option>
        <option value="10" <%if(minute == "10") {%>selected="selected"<%}%>>10</option>
        <option value="11" <%if(minute == "11") {%>selected="selected"<%}%>>11</option>
        <option value="12" <%if(minute == "12") {%>selected="selected"<%}%>>12</option>
        <option value="13" <%if(minute == "13") {%>selected="selected"<%}%>>13</option>
        <option value="14" <%if(minute == "14") {%>selected="selected"<%}%>>14</option>
        <option value="15" <%if(minute == "15") {%>selected="selected"<%}%>>15</option>
        <option value="16" <%if(minute == "16") {%>selected="selected"<%}%>>16</option>
        <option value="17" <%if(minute == "17") {%>selected="selected"<%}%>>17</option>
        <option value="18" <%if(minute == "18") {%>selected="selected"<%}%>>18</option>
        <option value="19" <%if(minute == "19") {%>selected="selected"<%}%>>19</option>
        <option value="10" <%if(minute == "20") {%>selected="selected"<%}%>>20</option>
        <option value="21" <%if(minute == "21") {%>selected="selected"<%}%>>21</option>
        <option value="22" <%if(minute == "22") {%>selected="selected"<%}%>>22</option>
        <option value="23" <%if(minute == "23") {%>selected="selected"<%}%>>23</option>
        <option value="23" <%if(minute == "24") {%>selected="selected"<%}%>>24</option>
        <option value="23" <%if(minute == "25") {%>selected="selected"<%}%>>25</option>
        <option value="23" <%if(minute == "26") {%>selected="selected"<%}%>>26</option>
        <option value="23" <%if(minute == "27") {%>selected="selected"<%}%>>27</option>
        <option value="23" <%if(minute == "28") {%>selected="selected"<%}%>>28</option>
        <option value="23" <%if(minute == "29") {%>selected="selected"<%}%>>29</option>
        <option value="23" <%if(minute == "30") {%>selected="selected"<%}%>>30</option>
        <option value="23" <%if(minute == "31") {%>selected="selected"<%}%>>31</option>
        <option value="23" <%if(minute == "32") {%>selected="selected"<%}%>>32</option>
        <option value="23" <%if(minute == "33") {%>selected="selected"<%}%>>33</option>
        <option value="23" <%if(minute == "34") {%>selected="selected"<%}%>>34</option>
        <option value="23" <%if(minute == "35") {%>selected="selected"<%}%>>35</option>
        <option value="23" <%if(minute == "36") {%>selected="selected"<%}%>>36</option>
        <option value="23" <%if(minute == "37") {%>selected="selected"<%}%>>37</option>
        <option value="23" <%if(minute == "38") {%>selected="selected"<%}%>>38</option>
        <option value="23" <%if(minute == "39") {%>selected="selected"<%}%>>39</option>
        <option value="23" <%if(minute == "40") {%>selected="selected"<%}%>>40</option>
        <option value="23" <%if(minute == "41") {%>selected="selected"<%}%>>41</option>
        <option value="23" <%if(minute == "42") {%>selected="selected"<%}%>>42</option>
        <option value="23" <%if(minute == "43") {%>selected="selected"<%}%>>43</option>
        <option value="23" <%if(minute == "44") {%>selected="selected"<%}%>>44</option>
        <option value="23" <%if(minute == "45") {%>selected="selected"<%}%>>45</option>
        <option value="23" <%if(minute == "46") {%>selected="selected"<%}%>>46</option>
        <option value="23" <%if(minute == "47") {%>selected="selected"<%}%>>47</option>
        <option value="23" <%if(minute == "48") {%>selected="selected"<%}%>>48</option>
        <option value="23" <%if(minute == "49") {%>selected="selected"<%}%>>49</option>
        <option value="23" <%if(minute == "50") {%>selected="selected"<%}%>>50</option>
        <option value="23" <%if(minute == "51") {%>selected="selected"<%}%>>51</option>
        <option value="23" <%if(minute == "52") {%>selected="selected"<%}%>>52</option>
        <option value="23" <%if(minute == "53") {%>selected="selected"<%}%>>53</option>
        <option value="23" <%if(minute == "54") {%>selected="selected"<%}%>>54</option>
        <option value="23" <%if(minute == "55") {%>selected="selected"<%}%>>55</option>
        <option value="23" <%if(minute == "56") {%>selected="selected"<%}%>>56</option>
        <option value="23" <%if(minute == "57") {%>selected="selected"<%}%>>57</option>
        <option value="23" <%if(minute == "58") {%>selected="selected"<%}%>>58</option>
        <option value="23" <%if(minute == "59") {%>selected="selected"<%}%>>59</option>
    </select>
