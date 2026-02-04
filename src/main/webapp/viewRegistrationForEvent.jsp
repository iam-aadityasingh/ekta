<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Event Registrations</title>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="css/viewRegistration_style.css">
        <link rel="stylesheet" href="css/global_style.css">
    </head>
    <body>

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

        if ("user".equals(role)) {
    %>
        <h2>Access denied</h2>
        <p>You do not have permission to view registrations for this event.</p>
    <%
            return;
        }

        ArrayList<String[]> registeredUsers =
            (ArrayList<String[]>) request.getAttribute("registered_users");
        Integer eventId = (Integer) request.getAttribute("event_id");
    %>

    <div class="header-row">
        <h1>Registered Users for Event</h1>
        <form method="get" action="DownloadEventRegistrationsServlet">
            <input type="hidden" name="event_id" value="<%= eventId %>" />
            <button type="submit" class="download-btn">Download Excel</button>
        </form>
    </div>

    <%
        if (registeredUsers == null || registeredUsers.isEmpty()) {
    %>
        <p class="no-data">No users have registered for this event yet.</p>
    <%
        } else {
    %>
        <table>
            <thead>
            <tr>
                <th>#</th>
                <th>Username</th>
                <th>Email</th>
                <th>Phone</th>
            </tr>
            </thead>
            <tbody>
            <%
                int rowNum = 1;
                for (String[] u : registeredUsers) {
                    String username = u[0];
                    String email = u[1];
                    String phone = u[2];
            %>
            <tr>
                <td><%= rowNum++ %></td>
                <td><%= username %></td>
                <td><%= email %></td>
                <td><%= phone %></td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
    <%
        }
    %>

    <form action="HomepageServlet" method="get">
        <button type="submit" class="back-btn">Back to Homepage</button>
    </form>

    </body>
</html>
