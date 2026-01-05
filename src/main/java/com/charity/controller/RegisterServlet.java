package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.ResultSet;

public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String username = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                
                String userQuery = "SELECT id FROM users WHERE email = ?";
                PreparedStatement userStmt = conn.prepareStatement(userQuery);
                userStmt.setString(1, email);
                ResultSet userRs = userStmt.executeQuery();
                userRs.next();
                int user_id = userRs.getInt(1);

                String insertQuery = "INSERT INTO profile_audit_log (action, user_email, old_username, new_username, old_phone, new_phone, old_password, new_password) VALUES ('Profile Created', ?, ?, ?, ?, ?, ?, ?)";
                                        
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
        }
    }
}
