package com.charity.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.charity.model.DatabaseConnection;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;

public class HomepageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        ArrayList<String[]> events = new ArrayList<>();

        PrintWriter out = response.getWriter();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM events WHERE is_deleted = 0");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String[] event = {
                    rs.getInt("id")+"",
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getDate("date").toString(),
                    rs.getTime("time").toString()
                };
                events.add(event);
            }
        } catch (Exception e) {
            out.println("Error ocured:" + e); 
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }

        request.setAttribute("events", events);
        request.getRequestDispatcher("homepage.jsp").forward(request, response);
    }
}