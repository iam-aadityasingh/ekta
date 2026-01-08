package com.charity.controller;

import com.charity.model.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class DeleteEventServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("email") == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            String userEmail = (String) session.getAttribute("email");
            String eventIdParam = request.getParameter("event_id");

            if (eventIdParam == null || eventIdParam.isEmpty()) {
                response.sendRedirect("userProfile.jsp");
                return;
            }

            int eventId = Integer.parseInt(eventIdParam);

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                String userQuery = "SELECT id FROM users WHERE email = ?";
                int creatorId;
                try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                    stmt.setString(1, userEmail);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            response.sendRedirect("userProfile.jsp");
                            return;
                        }
                        creatorId = rs.getInt(1);
                    }
                }

                String selectEventSql =
                        "SELECT name, description, date, time, location, image_url, registered_count " +
                        "FROM events WHERE id = ? AND creator_id = ? AND is_deleted = 0";
                String oldTitle;
                String oldDescription;
                Date oldDate;
                Time oldTime;
                String oldLocation;
                String oldImageUrl;
                int oldRegisteredCount;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectEventSql)) {
                    selectStmt.setInt(1, eventId);
                    selectStmt.setInt(2, creatorId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            response.sendRedirect("UserProfileServlet");
                            return;
                        }
                        oldTitle = rs.getString("name");
                        oldDescription = rs.getString("description");
                        oldDate = rs.getDate("date");
                        oldTime = rs.getTime("time");
                        oldLocation = rs.getString("location");
                        oldImageUrl = rs.getString("image_url");
                        oldRegisteredCount = rs.getInt("registered_count");
                    }
                }

                String softDeleteEventSql =
                        "UPDATE events SET is_deleted = 1 WHERE id = ? AND creator_id = ? AND is_deleted = 0";
                int deletedEvent;
                try (PreparedStatement pstmt = conn.prepareStatement(softDeleteEventSql)) {
                    pstmt.setInt(1, eventId);
                    pstmt.setInt(2, creatorId);
                    deletedEvent = pstmt.executeUpdate();
                }

                if (deletedEvent > 0) {

                    String deleteEventLogSql =
                            "INSERT INTO event_audit_log (" +
                            " action, event_id," +
                            " old_eventname, new_eventname," +
                            " old_description, new_description," +
                            " old_location, new_location," +
                            " old_date, new_date," +
                            " old_time, new_time," +
                            " old_image_url, new_image_url," +
                            " old_registered_count, new_registered_count" +
                            ") VALUES (" +
                            " 'Event Deleted', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                            ")";
                    try (PreparedStatement logStmt = conn.prepareStatement(deleteEventLogSql)) {
                        int idx = 1;
                        logStmt.setInt(idx++, eventId);
                        logStmt.setString(idx++, oldTitle);
                        logStmt.setString(idx++, oldTitle);
                        logStmt.setString(idx++, oldDescription);
                        logStmt.setString(idx++, oldDescription);
                        logStmt.setString(idx++, oldLocation);
                        logStmt.setString(idx++, oldLocation);
                        logStmt.setDate(idx++, oldDate);
                        logStmt.setDate(idx++, oldDate);
                        logStmt.setTime(idx++, oldTime);
                        logStmt.setTime(idx++, oldTime);
                        logStmt.setString(idx++, oldImageUrl);
                        logStmt.setString(idx++, oldImageUrl);
                        logStmt.setInt(idx++, oldRegisteredCount);
                        logStmt.setInt(idx++, oldRegisteredCount);
                        logStmt.executeUpdate();
                    }

                    String selectRegsSql =
                            "SELECT id, user_id FROM registrations " +
                            "WHERE event_id = ? AND is_deleted = 0";
                    try (PreparedStatement regsStmt = conn.prepareStatement(selectRegsSql)) {
                        regsStmt.setInt(1, eventId);
                        try (ResultSet regsRs = regsStmt.executeQuery()) {

                            String softDeleteRegSql =
                                    "UPDATE registrations SET is_deleted = 1 WHERE id = ?";
                            String regLogSql =
                                    "INSERT INTO registrations_audit_log (action, user_id, event_id) " +
                                    "VALUES ('Deleted registration due to Event Deletion', ?, ?)";

                            while (regsRs.next()) {
                                int regId = regsRs.getInt("id");
                                int regUserId = regsRs.getInt("user_id");

                                try (PreparedStatement delRegStmt = conn.prepareStatement(softDeleteRegSql)) {
                                    delRegStmt.setInt(1, regId);
                                    delRegStmt.executeUpdate();
                                }

                                try (PreparedStatement regLogStmt = conn.prepareStatement(regLogSql)) {
                                    regLogStmt.setInt(1, regUserId);
                                    regLogStmt.setInt(2, eventId);
                                    regLogStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }

                conn.commit();
                request.getRequestDispatcher("UserProfileServlet").forward(request, response);

            } catch (Exception e) {
                e.printStackTrace();
                out.println("<h3>Error: " + e.getMessage() + "</h3>");
            }
        }
    }
}
