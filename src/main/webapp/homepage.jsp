<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>   
    
    <%
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>
    
<head>
    <title>Homepage</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">    
    <link rel="stylesheet" href="css/home_style.css">
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    <header>
        <h1 id="title">Available Charity Events</h1>
        <div class="profile-icon"> 
            <form action="UserProfileServlet" method="get"> 
                <button type="submit"> 
                    <img src="./images/user.png" alt="profile"/> 
                </button> 
            </form> 
        </div>
    </header>
    <div class="event-list">
        <%
            ArrayList<String[]> events = (ArrayList<String[]>) request.getAttribute("events");
            if (events != null && !events.isEmpty()) {
                for (String[] event : events) {
        %>
        <div class="event-card">
            <h2><%= event[1] %></h2>
            <p><%= event[2] %></p>
            <p><strong>Location:</strong> <%= event[3] %></p>
            <p><strong>Date:</strong> <%= event[4] %></p>
            <p><strong>Time:</strong> <%= event[5] %></p>
            <form method="post" action="EventDetailsServlet" >
                <input type="hidden" name="event_id" value="<%= event[0]%>" />
                <button class="upBtn">View More</button>
            </form>
        </div>
        <% 
                }
            } else { 
        %>
        <p>No events available at the moment.</p>
        <% } %>
    </div>
</body>
</html>
