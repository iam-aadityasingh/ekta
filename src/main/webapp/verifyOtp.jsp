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
        <link rel="stylesheet" href="css/verifyotp_style.css">
        <link rel="stylesheet" href="css/global_style.css">
        <title>Verify OTP</title>
    </head>
    <body>
        <div class="otp-container">
            <h2>Verify Your Email</h2>
            <p class="info">
                We’ve sent a 6-digit code to<br>
                <span>${userEmail}</span>
            </p>

            <form action="VerifyOtpServlet" method="post">
                <label for="otp">Enter 6-digit OTP</label>
                <div class="otp-input">
                    <input type="text" id="otp" name="otp" maxlength="6" required>
                </div>
                <button type="submit">Verify & Login</button>
            </form>

            <p class="error-msg">${error}</p>
            <p class="hint">Didn’t receive the code? Check spam or try again after a minute.</p>
        </div>
    </body>
</html>
