<% include '/WEB-INF/includes/header.gtpl' %>

<table border="1px" width="70%">
   <thead>
      <tr>
         <td><b>Element</b></td>
         <td><b>Value</b></td>
      </tr>
   </thead>
   <tbody>
      <tr>
         <td>Method</td>
         <td>${request.method}</td>
      </tr>
      <tr>
         <td>Headers</td>
         <td>
            <ul>
               <% headers.each { %>
                  <li>${it.key}: ${it.value}</li>
               <% } %>
            </ul>
         </td>
      </tr>
      <tr>
         <td>Cookies</td>
         <td>
            <ul>
               <% request.cookies.each { %>
                  <li>${it.name}: ${it.value}</li>
               <% } %>
            </ul>
         </td>
      </tr>
   </tbody>
</table>




<% include '/WEB-INF/includes/footer.gtpl' %>

