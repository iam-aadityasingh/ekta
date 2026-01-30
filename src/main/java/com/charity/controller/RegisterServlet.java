package com.charity.controller;

import com.charity.model.DatabaseConnection;
import com.charity.model.gmailAuth;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        String username = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            int rowsAffected = 0;
            String insertQuery = "" ;
            
            String getUserIfExsistQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement getUserIfExistStmt = conn.prepareStatement(getUserIfExsistQuery);
            getUserIfExistStmt.setString(1, email);
            ResultSet userFoundRs = getUserIfExistStmt.executeQuery();
            
            if(userFoundRs.next()) {
                int user_id = userFoundRs.getInt("id");
                String updateUserAccStateQuery = "UPDATE users SET username = ?, password = ?, phone = ?, is_deleted = 0 WHERE id = ?";
                PreparedStatement updateUserAccStateStmt = conn.prepareStatement(updateUserAccStateQuery);
                updateUserAccStateStmt.setString(1, username);
                updateUserAccStateStmt.setString(2, password);
                updateUserAccStateStmt.setString(3, phone);
                updateUserAccStateStmt.setInt(4, user_id);
                rowsAffected = updateUserAccStateStmt.executeUpdate();
                insertQuery = "INSERT INTO profile_audit_log (action, user_email, old_username, new_username, old_phone, new_phone, old_password, new_password) VALUES ('Profile Status Updated', ?, ?, ?, ?, ?, ?, ?)";
            } else {
                String insertUserQuery = "INSERT INTO users (username, password, email, phone) VALUES (?, ?, ?, ?)";
                PreparedStatement insertUserStmt = conn.prepareStatement(insertUserQuery);
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setString(3, email);
                insertUserStmt.setString(4, phone);
                rowsAffected = insertUserStmt.executeUpdate();   
                insertQuery = "INSERT INTO profile_audit_log (action, user_email, old_username, new_username, old_phone, new_phone, old_password, new_password) VALUES ('Profile Created', ?, ?, ?, ?, ?, ?, ?)";
            }

            if (rowsAffected > 0) {
                
                String generatedOTP = gmailAuth.sendOTP(email);
                HttpSession session = request.getSession();
                session.setAttribute("authOTP", generatedOTP);
                session.setAttribute("userEmail", email); 
                response.sendRedirect("verify_otp.jsp");
                
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, email);                    
                insertStmt.setString(2, username);
                insertStmt.setString(3, username);
                insertStmt.setString(4, phone);
                insertStmt.setString(5, phone);
                insertStmt.setString(6, password);
                insertStmt.setString(7, password);

                int j = insertStmt.executeUpdate();
                response.sendRedirect("login.jsp");
            } else {
                out.println("<h3>Registration failed!</h3>");
            }
        } catch (SQLException e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        } catch (MessagingException ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
