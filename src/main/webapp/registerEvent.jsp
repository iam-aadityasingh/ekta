<%@page import="java.util.Objects"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
        
    <%
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        if (session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String role = (String) session.getAttribute("role");
    %>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">   
        <title>Register Event Page</title>
        <link rel="stylesheet" href="css/registerEvent_style.css">
        <link rel="stylesheet" href="css/global_style.css">
    </head>
    <body>
        <div class="header">
            <h1>Event Details</h1>
            <form method="get" action="HomepageServlet">
                <button type="submit" class="home-button">
                    <img src="./images/home.png"  alt="home"/>
                </button>
            </form>
            <form action="UserProfileServlet" method="get"> 
                <button type="submit"> 
                    <img src="./images/user.png" alt="profile"/> 
                </button> 
            </form> 
        </div>
        <%
            ArrayList event = (ArrayList)request.getAttribute("event");
            if (event != null && !event.isEmpty()) {
        %> 
        <div class="single-event-card">
            <img src="<%= event.get(9) %>" alt="Event Image" class="event-image" />

            <h2><%= event.get(4) %></h2>
            <p><%= event.get(5) %></p>
            <p><strong>Created by:</strong> <%= event.get(0) %>,<strong>phone: </strong> <%= event.get(1) %> ,<strong>email: </strong> <%= event.get(2) %></p>
            <p><strong>Location:</strong> <%= event.get(6) %></p>
            <p><strong>Date:</strong> <%= event.get(7) %></p>
            <p><strong>Time:</strong> <%= event.get(8) %></p>
            <p><strong>Registered People:</strong> <%= event.get(10) %></p>

            <%
                if(Objects.equals(role, "admin")) {
            %>
            <form method="post" action="ViewRegisterationForEventServlet">
                <input type="hidden" name="event_id" value="<%= event.get(3) %>" />
                <button class="upBtn">View Registrations</button>
            </form>
            <% 
                } else { 
            %>
            <form method="post" action="RegisterForEventServlet">
                <input type="hidden" name="event_id" value="<%= event.get(3) %>" />
                <button class="upBtn">Register</button>
            </form>
            <% } %>
        </div>
        
        <% 
            } else { 
        %>
        <p>Event not available at the moment.</p>
        <% } %>
    </body>
</html>
