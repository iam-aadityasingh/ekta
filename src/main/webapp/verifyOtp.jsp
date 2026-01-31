<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
        
    <%
        if (session == null || session.getAttribute("userEmail") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
    %>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="VerifyOtpServlet" method="post">
            <h3>Check your email: ${userEmail}</h3>
            <label>Enter 6-Digit OTP:</label>
            <input type="text" name="otp" maxlength="6" required>
            <button type="submit">Verify & Login</button>
        </form>
        <p style="color:red;">${error}</p>
    </body>
</html>
