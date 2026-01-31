<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
        
    <%
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>
    
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">   
    <title>Update Event</title>
    <link rel="stylesheet" href="css/updateEvent_style.css">
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    <h1>Update Event Details</h1>
    <form action="UpdateEventServlet" method="post" enctype="multipart/form-data">
        <input type="hidden" name="eventId" value="${event_id}" />

        <input type="text" name="name" placeholder="Event name" value="${event_name}" required />

        <textarea name="description" placeholder="Description" required>${event_description}</textarea>

        <input type="text" name="location" placeholder="Location" value="${event_location}" required />

        <input type="date" name="date" value="${event_date}" required />

        <input type="time" name="time" value="${event_time}" required />

        <input type="file" name="image_url" id="image_url" accept="image/*" />
        <small><i>Note: Select new to update, leave empty to keep previous.</i></small>

        <button type="submit">Update Event</button>
    </form>
</body>
</html>
