package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UpdateProfileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String email = (String) session.getAttribute("email");
        String username = request.getParameter("username");
        String phone = request.getParameter("phNo");
        String password = request.getParameter("password");

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String selectQuery = "SELECT username, phone, password FROM users where email = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1, email);
            ResultSet userRs = selectStmt.executeQuery();
            userRs.next();
            String old_username = userRs.getString("username");
            String old_phone = userRs.getString("phone");
            String old_password = userRs.getString("password");
            
            
            if ( !(old_password.equals(password) & old_username.equals(username) & old_phone.equals(phone)) ){
                String updateQuery = "UPDATE users SET username = ?, phone = ?, password = ? WHERE email = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, username);
                updateStmt.setString(2, phone);
                updateStmt.setString(3, password);
                updateStmt.setString(4, email);

                int i = updateStmt.executeUpdate();
                
                if(i > 0){
                    String insertQuery = "INSERT INTO profile_audit_log (action, user_email, old_username, new_username, old_phone, new_phone, old_password, new_password) VALUES ('Updated Profile', ?, ?, ?, ?, ?, ?, ?)";
                                        
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setString(1, email);                    
                    insertStmt.setString(2, old_username);
                    insertStmt.setString(3, username);
                    insertStmt.setString(4, old_phone);
                    insertStmt.setString(5, phone);
                    insertStmt.setString(6, old_password);
                    insertStmt.setString(7, password);

                    int j = insertStmt.executeUpdate();
                }
                
            }
            response.sendRedirect(request.getContextPath() + "/UserProfileServlet");

        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
