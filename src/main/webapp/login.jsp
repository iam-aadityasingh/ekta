<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">   
    <link rel="stylesheet" href="css/login_style.css">
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    <h1>Login</h1>
    <form action="LoginServlet" method="post">
    <label for="role">Role:</label>
    <select name="role" id="role">
        <option value="user" selected>User</option>
        <option value="admin">Admin</option>
    </select>
        <label for="email">Email:</label>
        <input type="email" name="email" id="email" required><br>
        <label for="password">Password:</label>
        <input type="password" name="password" id="password" required><br>
        <button type="submit">Login</button>
    </form>
    <h2><i>No account ? </i> <b><a href="register.jsp">Register</a></b> <i>here</i></h2>
</body>
</html>
