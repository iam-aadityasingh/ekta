package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM events where id = ?");
            ps.setInt(1, event_id);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                event.add(rs.getInt("id"));
                event.add(rs.getString("name"));
                event.add(rs.getString("description"));
                event.add(rs.getString("location"));
                event.add(rs.getDate("date").toString());
                event.add(rs.getTime("time").toString()); 
                event.add(rs.getString("image_url"));              
                event.add(rs.getInt("registered_count")); 
            }
        } catch (Exception e) {
            out.println("Error ocured:" + e); 
        }
        
        request.setAttribute("event", event);
        request.getRequestDispatcher("registerEvent.jsp").forward(request, response);
    }
}
