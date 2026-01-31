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
        <title>Create Event</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="css/createEvent_style.css">
        <link rel="stylesheet" href="css/global_style.css">
    </head>
    <body>
        <h1>Create Event</h1>
        <form action="CreateEventServlet" method="post" enctype="multipart/form-data">

            <!-- 1. First line - event name -->
            <div class="form-row">
                <label for="name">Event Name:</label>
                <input type="text" name="name" id="name" placeholder="Event Name" required>
            </div>

            <!-- 2. Second line - description -->
            <div class="form-row">
                <label for="description">Description:</label>
                <textarea name="description" id="description" placeholder="Description"></textarea>
            </div>

            <!-- 3. Third line - location -->
            <div class="form-row">
                <label for="location">Location:</label>
                <input type="text" name="location" id="location" placeholder="Location">
            </div>

            <!-- 4. Fourth line - time and date (side by side or stacked via CSS) -->
            <div class="form-row time-date-row">
                <div class="field-group">
                    <label for="time">Time:</label>
                    <input type="time" name="time" id="time" required>
                </div>
                <div class="field-group">
                    <label for="date">Date:</label>
                    <input type="date" name="date" id="date" required>
                </div>
            </div>

            <!-- 5. Fifth line - file upload -->
            <div class="form-row">
                <label for="image_url">Upload Event Image:</label>
                <input type="file" name="image_url" id="image_url" accept="image/*" required>
            </div>

            <!-- 6. Sixth line - submit button -->
            <div class="form-row">
                <button type="submit">Create Event</button>
            </div>
        </form>
    </body>
</html>
