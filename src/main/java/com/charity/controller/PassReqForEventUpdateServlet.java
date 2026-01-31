package com.charity.controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;

public class PassReqForEventUpdateServlet extends HttpServlet {

@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        try (PrintWriter out = response.getWriter()) {
            
            int id = Integer.parseInt(request.getParameter("event_id"));
            String name = request.getParameter("event_name");
            String description = request.getParameter("event_description");
            String location = request.getParameter("event_location");
            String dateStr = request.getParameter("event_date"); // e.g. "2023-12-31"
            String timeStr = request.getParameter("event_time"); // e.g. "14:30:00"

            // Use LocalDate/Time to validate or manipulate if needed
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            
            request.setAttribute("event_id", id);
            request.setAttribute("event_name", name); 
            request.setAttribute("event_description", description); 
            request.setAttribute("event_location", location); 
            // Pass them as attributes (Format: yyyy-MM-dd and HH:mm)
            request.setAttribute("event_date", date.toString()); 
            request.setAttribute("event_time", time.toString().substring(0, 5)); // Truncates to HH:mm
            request.getRequestDispatcher("updateEvent.jsp").forward(request, response);
        }
    }

}
