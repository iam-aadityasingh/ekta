package com.charity.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class VerifyOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String sessionOtp = (String) session.getAttribute("authOTP");
        String userEnteredOtp = request.getParameter("otp");
        String email = (String) session.getAttribute("userEmail");

        if (sessionOtp != null && sessionOtp.equals(userEnteredOtp)) {
            // SUCCESS : Remove OTP from session so it can't be reused
            session.removeAttribute("authOTP");
            
            // 2. Mark user as "Logged In" by saving their email/ID in session
            session.setAttribute("user", email); 
            response.sendRedirect("HomepageServlet");
        } else {
            request.setAttribute("error", "Invalid OTP. Please try again.");
            request.getRequestDispatcher("verify_otp.jsp").forward(request, response);
        }
    }
}