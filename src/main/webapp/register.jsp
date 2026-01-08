<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">   
    <link rel="stylesheet" href="css/register_style.css">
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    <div class="header">
        <h1>Register</h1>
    </div>
    <form action="RegisterServlet" method="post">
        <label for="name">Name:</label>
        <input type="text" id="name" name="name" required><br>
        <label for="phone">Phone:</label>
        <input type="text" id="phone" name="phone" required><br>
        <label for="email">Email:</label>
        <input type="email" id="email" name="email" required><br>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required><br>
        <button type="submit">Register</button>
    </form>
    <h2><i>All ready have an account ? </i> <b><a href="login.jsp">Login</a></b> <i>here</i></h2>
</body>
</html>
