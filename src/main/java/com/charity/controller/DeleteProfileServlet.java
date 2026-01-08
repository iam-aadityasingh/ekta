package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DeleteProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String email = (String) session.getAttribute("email");
        int userId = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 1. Mark user as deleted
            String deleteUserSql = "UPDATE users SET is_deleted = 1 WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteUserSql)) {
                ps.setInt(1, userId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    System.out.println("<h3>Error: User not found!</h3>");
                    return;
                }
            }

            // 1.a Fetch user info for profile_audit_log (after marking is_deleted = 1)
            String userInfoSql = "SELECT username, phone, password FROM users WHERE id = ? AND is_deleted = 1";
            String username = null;
            String phone = null; 
            String password = null;
            try (PreparedStatement ps = conn.prepareStatement(userInfoSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        username = rs.getString("username");
                        phone = rs.getString("phone");
                        password = rs.getString("password");
                    } else {
                        conn.rollback();
                        response.sendRedirect("userProfile.jsp?error=UserDataMissing");
                        return;
                    }
                }
            }

            // 2. Get all events the user is registered for (still active)
            //    (for logs + registered_count decrement later)
            String userRegsSql =
                    "SELECT r.event_id, e.registered_count " +
                    "FROM registrations r " +
                    "JOIN events e ON r.event_id = e.id " +
                    "WHERE r.is_deleted = 0 AND r.user_id = ?";
            // We will re-run this query for logs after marking is_deleted, so keep the SQL.

            // 3. Get all registrations on events created by this user (still active)
            String regsOnUserEventsSql =
                    "SELECT r.user_id, r.event_id " +
                    "FROM registrations r " +
                    "JOIN events e ON r.event_id = e.id " +
                    "WHERE r.is_deleted = 0 AND e.creator_id = ?";

            // 4. Mark user’s own registrations as deleted
            String softDeleteUserRegsSql =
                    "UPDATE registrations SET is_deleted = 1 WHERE user_id = ? AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(softDeleteUserRegsSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 5. Mark events created by the user as deleted
            String softDeleteUserEventsSql =
                    "UPDATE events SET is_deleted = 1 WHERE creator_id = ? AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(softDeleteUserEventsSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 6. Mark registrations for events created by this user as deleted
            String softDeleteRegsOnUserEventsSql =
                    "UPDATE registrations r " +
                    "JOIN events e ON r.event_id = e.id " +
                    "SET r.is_deleted = 1 " +
                    "WHERE e.creator_id = ? AND r.is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(softDeleteRegsOnUserEventsSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 7. Insert profile_audit_log for account deletion
            String insertProfileLogSql =
                    "INSERT INTO profile_audit_log " +
                    "(action, user_email, old_username, new_username, " +
                    " old_phone, new_phone, old_password, new_password) " +
                    "VALUES ('Profile Deleted', ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProfileLogSql)) {
                ps.setString(1, email);
                ps.setString(2, username);
                ps.setString(3, username);
                ps.setString(4, phone);
                ps.setString(5, phone);
                ps.setString(6, password);
                ps.setString(7, password);
                ps.executeUpdate();
            }

            // 8. Insert registrations_audit_log for deletions of user’s own registrations
            try (PreparedStatement ps = conn.prepareStatement(userRegsSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    String insertRegLogSql =
                            "INSERT INTO registrations_audit_log (action, user_id, event_id) " +
                            "VALUES ('Deleted registration for Event', ?, ?)";
                    String updateCountSql =
                            "UPDATE events SET registered_count = ? WHERE id = ?";
                    String selectEventForCountLogSql =
                            "SELECT id, name, description, location, date, time, image_url " +
                            "FROM events WHERE id = ?";

                    while (rs.next()) {
                        int eventId = rs.getInt("event_id");
                        int oldCount = rs.getInt("registered_count");
                        int newCount = oldCount > 0 ? oldCount - 1 : 0;

                        // 8.a registration log
                        try (PreparedStatement regLogPs = conn.prepareStatement(insertRegLogSql)) {
                            regLogPs.setInt(1, userId);
                            regLogPs.setInt(2, eventId);
                            regLogPs.executeUpdate();
                        }

                        // 9. Decrease registered_count for that event
                        try (PreparedStatement updCountPs = conn.prepareStatement(updateCountSql)) {
                            updCountPs.setInt(1, newCount);
                            updCountPs.setInt(2, eventId);
                            updCountPs.executeUpdate();
                        }

                        // 10. Insert event_audit_log for registered_count change
                        try (PreparedStatement evSelectPs = conn.prepareStatement(selectEventForCountLogSql)) {
                            evSelectPs.setInt(1, eventId);
                            try (ResultSet evRs = evSelectPs.executeQuery()) {
                                if (evRs.next()) {
                                    String evLogSql =
                                            "INSERT INTO event_audit_log (" +
                                            " action, event_id, " +
                                            " old_eventname, new_eventname, " +
                                            " old_description, new_description, " +
                                            " old_location, new_location, " +
                                            " old_date, new_date, " +
                                            " old_time, new_time, " +
                                            " old_image_url, new_image_url, " +
                                            " old_registered_count, new_registered_count" +
                                            ") VALUES (" +
                                            " 'Registered Count Updated', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                                            ")";
                                    try (PreparedStatement evLogPs = conn.prepareStatement(evLogSql)) {
                                        int idx = 1;
                                        evLogPs.setInt(idx++, eventId);
                                        evLogPs.setString(idx++, evRs.getString("name"));
                                        evLogPs.setString(idx++, evRs.getString("name"));
                                        evLogPs.setString(idx++, evRs.getString("description"));
                                        evLogPs.setString(idx++, evRs.getString("description"));
                                        evLogPs.setString(idx++, evRs.getString("location"));
                                        evLogPs.setString(idx++, evRs.getString("location"));
                                        evLogPs.setDate(idx++, evRs.getDate("date"));
                                        evLogPs.setDate(idx++, evRs.getDate("date"));
                                        evLogPs.setTime(idx++, evRs.getTime("time"));
                                        evLogPs.setTime(idx++, evRs.getTime("time"));
                                        evLogPs.setString(idx++, evRs.getString("image_url"));
                                        evLogPs.setString(idx++, evRs.getString("image_url"));
                                        evLogPs.setInt(idx++, oldCount);
                                        evLogPs.setInt(idx++, newCount);
                                        evLogPs.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 11. Insert registrations_audit_log for deletions of registrations on user’s events
            try (PreparedStatement ps = conn.prepareStatement(regsOnUserEventsSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    String regsOnUserEventsLogSql =
                            "INSERT INTO registrations_audit_log (action, user_id, event_id) " +
                            "VALUES ('Deleted registration due to Event Deletion', ?, ?)";
                    while (rs.next()) {
                        int regUserId = rs.getInt("user_id");
                        int eventId = rs.getInt("event_id");

                        try (PreparedStatement logPs = conn.prepareStatement(regsOnUserEventsLogSql)) {
                            logPs.setInt(1, regUserId);
                            logPs.setInt(2, eventId);
                            logPs.executeUpdate();
                        }
                    }
                }
            }

            // 12. Insert event_audit_log for all events created by this user (deleted events)
            String selectDeletedEventsSql =
                    "SELECT id, name, description, location, date, time, image_url, registered_count " +
                    "FROM events WHERE creator_id = ? AND is_deleted = 1";
            try (PreparedStatement ps = conn.prepareStatement(selectDeletedEventsSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    String eventDeletedLogSql =
                            "INSERT INTO event_audit_log (" +
                            " action, event_id, " +
                            " old_eventname, new_eventname, " +
                            " old_description, new_description, " +
                            " old_location, new_location, " +
                            " old_date, new_date, " +
                            " old_time, new_time, " +
                            " old_image_url, new_image_url, " +
                            " old_registered_count, new_registered_count" +
                            ") VALUES (" +
                            " 'Event Deleted', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                            ")";
                    while (rs.next()) {
                        try (PreparedStatement evLogPs = conn.prepareStatement(eventDeletedLogSql)) {
                            int idx = 1;
                            evLogPs.setInt(idx++, rs.getInt("id"));
                            evLogPs.setString(idx++, rs.getString("name"));
                            evLogPs.setString(idx++, rs.getString("name"));
                            evLogPs.setString(idx++, rs.getString("description"));
                            evLogPs.setString(idx++, rs.getString("description"));
                            evLogPs.setString(idx++, rs.getString("location"));
                            evLogPs.setString(idx++, rs.getString("location"));
                            evLogPs.setDate(idx++, rs.getDate("date"));
                            evLogPs.setDate(idx++, rs.getDate("date"));
                            evLogPs.setTime(idx++, rs.getTime("time"));
                            evLogPs.setTime(idx++, rs.getTime("time"));
                            evLogPs.setString(idx++, rs.getString("image_url"));
                            evLogPs.setString(idx++, rs.getString("image_url"));
                            int regCount = rs.getInt("registered_count");
                            evLogPs.setInt(idx++, regCount);
                            evLogPs.setInt(idx++, regCount);
                            evLogPs.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
            session.invalidate();
            response.sendRedirect("register.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("userProfile.jsp?error=DeleteFailed");
        }
    }
}
