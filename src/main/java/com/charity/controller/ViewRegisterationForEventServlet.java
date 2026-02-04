package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Objects;

public class ViewRegisterationForEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
                
        if (session.getAttribute("role") == null || !Objects.equals(session.getAttribute("role"), "admin")){
            response.sendRedirect("login.jsp");
            return;
        }
        
        int eventId = Integer.parseInt(request.getParameter("event_id"));
        ArrayList registered_users = new ArrayList();
        
        PrintWriter out = response.getWriter();
        try (Connection conn = DatabaseConnection.getConnection()){
  
            int event_id = Integer.parseInt(request.getParameter("event_id"));

            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.username, u.email, u.phone " +
                "FROM users u " +
                "JOIN registrations r ON u.id = r.user_id " +
                "WHERE r.event_id = ? AND r.is_deleted = 0"
            );
            ps.setInt(1, event_id);
            ResultSet rs = ps.executeQuery(); 

            registered_users = new ArrayList<>();
            while (rs.next()) {
                registered_users.add(new String[]{
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("phone")
                });
            }

            request.setAttribute("event_id", event_id);
            request.setAttribute("registered_users", registered_users);
            request.getRequestDispatcher("viewRegistrationForEvent.jsp").forward(request, response);

            
        } catch (Exception e) {
            out.println("Error ocured:" + e); 
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
        
    }
}
