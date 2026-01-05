package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;

public class UserProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        
        String userEmail = (String) session.getAttribute("email");

        if (userEmail == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String userQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, userEmail);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                request.setAttribute("username", userRs.getString("username"));
                request.setAttribute("ph_no", userRs.getLong("phone"));
                request.setAttribute("email", userRs.getString("email"));
                request.setAttribute("password", userRs.getString("password"));
            }
            
            String registeredEventsQuery = "SELECT e.id, e.name, e.description, e.location, e.date, e.time " +
                                 "FROM registrations r " +
                                 "JOIN events e ON r.event_id = e.id " +
                                 "WHERE r.user_id = ? " +
                                 "order by e.date asc";
            PreparedStatement RegisteredEventsStmt = conn.prepareStatement(registeredEventsQuery);
            RegisteredEventsStmt.setInt(1, userRs.getInt("id"));
            ResultSet RegisteredEventsRs = RegisteredEventsStmt.executeQuery();

            ArrayList<String[]> registeredEvents = new ArrayList<>();
            while (RegisteredEventsRs.next()) {
                registeredEvents.add(new String[]{
                        RegisteredEventsRs.getInt("id") + "",
                        RegisteredEventsRs.getString("name"),
                        RegisteredEventsRs.getString("description"),
                        RegisteredEventsRs.getString("location"),
                        RegisteredEventsRs.getDate("date").toString(),
                        RegisteredEventsRs.getTime("time").toString()
                });
            }

            String createdEventsQuery = "SELECT * FROM events WHERE creator_id = ?";
            PreparedStatement CreatedEventsStmt = conn.prepareStatement(createdEventsQuery);
            CreatedEventsStmt.setInt(1,userRs.getInt("id"));
            ResultSet CreatedEventsRs = CreatedEventsStmt.executeQuery();

            ArrayList<String[]> createdEvents = new ArrayList<>();
            while (CreatedEventsRs.next()) {
                createdEvents.add(new String[]{
                        CreatedEventsRs.getInt("id") + "",
                        CreatedEventsRs.getString("name"),
                        CreatedEventsRs.getString("description"),
                        CreatedEventsRs.getString("location"),
                        CreatedEventsRs.getDate("date").toString(),
                        CreatedEventsRs.getTime("time").toString(),
                        CreatedEventsRs.getString("image_url")
                });
            }
            
            int totalRegisteredEvents = registeredEvents.size();
            request.setAttribute("totalRegisteredEvents", totalRegisteredEvents);
            
            int totalCreatedEvents = createdEvents.size();
            request.setAttribute("totalCreatedEvents", totalCreatedEvents);
            
            request.setAttribute("registeredEvents", registeredEvents);
            request.setAttribute("createdEvents", createdEvents);
            request.getRequestDispatcher("userProfile.jsp").forward(request, response);
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
}
