package com.charity.controller;

import com.charity.model.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DownloadEventRegistrationsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (role == null || !"admin".equals(role)) {
            response.sendRedirect("login.jsp");
            return;
        }

        String eventIdParam = request.getParameter("event_id");
        if (eventIdParam == null || eventIdParam.isEmpty()) {
            response.sendRedirect("ViewRegisterationForEventServlet");
            return;
        }

        int eventId = Integer.parseInt(eventIdParam);

        // Set headers so browser downloads as Excel
        response.setContentType("application/vnd.ms-excel");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=event_" + eventId + "_registrations.xls"
        );

        try (Connection conn = DatabaseConnection.getConnection();
             PrintWriter out = response.getWriter()) {

            String sql =
                    "SELECT u.username, u.email, u.phone " +
                    "FROM users u " +
                    "JOIN registrations r ON u.id = r.user_id " +
                    "WHERE r.event_id = ? AND r.is_deleted = 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            // Header row
            out.println("Username\tEmail\tPhone");

            // Data rows
            while (rs.next()) {
                out.print(rs.getString("username"));
                out.print("\t");
                out.print(rs.getString("email"));
                out.print("\t");
                out.print(rs.getString("phone"));
                out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Optionally set an error status or redirect
        }
    }
}
