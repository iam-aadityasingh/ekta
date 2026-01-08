 package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;

public class EventDetailsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ArrayList event = new ArrayList();
        
        int event_id = Integer.parseInt(request.getParameter("event_id"));
        
        try (Connection conn = DatabaseConnection.getConnection()){
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM events where id = ? AND is_deleted = 0");
                ps.setInt(1, event_id);
                ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement user_details_ps = conn.prepareStatement(
                    "SELECT username, email FROM users WHERE id = ? AND is_deleted = 0"
                );
                user_details_ps.setInt(1, rs.getInt("creator_id"));
                ResultSet user_details_rs = user_details_ps.executeQuery();

                if (user_details_rs.next()) {
                    event.add(user_details_rs.getString("username"));
                    event.add(user_details_rs.getString("email"));
                } else {
                    System.out.println("No user found for creator_id");
                }

                event.add(rs.getInt("id"));
                event.add(rs.getString("name"));
                event.add(rs.getString("description"));
                event.add(rs.getString("location"));

                Date date = rs.getDate("date");
                if (date != null) event.add(date.toString());

                Time time = rs.getTime("time");
                if (time != null) event.add(time.toString());

                event.add(rs.getString("image_url"));
                event.add(rs.getInt("registered_count"));
            } else {
                System.out.println("No event found for id: " + event_id);
            }
        } catch (Exception e) {
            out.println("Error ocured:" + e); 
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
        
        request.setAttribute("event", event);
        request.getRequestDispatcher("registerEvent.jsp").forward(request, response);
    }
}
