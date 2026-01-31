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
    <title>Update Profile</title>
    <link rel="stylesheet" href="css/updateProfile_style.css">
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    <h1 id="title">Enter updated details</h1>
    <form action="UpdateProfileServlet" method="post">
        <input type="text" name="username" placeholder="Enter new username" value="<%=request.getParameter("username") %>" required />
        <input type="text" name="phNo" placeholder="Enter new ph.no" value="<%=request.getParameter("phNo") %>" required />
        <input type="password" name="password" placeholder="Enter new password" value="<%=request.getParameter("password") %>" required />
        <button type="submit">Update</button>
    </form>
</body>
</html>
