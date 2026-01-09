package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.sql.Time;

public class RegisterForEventServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
         
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String userEmail = (String) session.getAttribute("email");
        int eventId = Integer.parseInt(request.getParameter("event_id"));

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String query = "SELECT id FROM users WHERE email = ? AND is_deleted = 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            ResultSet get_user_rs = stmt.executeQuery();
            if (!get_user_rs.next()) {
                response.sendRedirect("login.jsp");
                return;
            }
            int curr_user_id = get_user_rs.getInt(1);
            
            String checkQuery = "SELECT * FROM registrations WHERE user_id = ? AND event_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, curr_user_id);
            checkStmt.setInt(2, eventId);

            int updateRegStatusRow = 0;
            int rowsInserted = 0;
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                if(rs.getInt("is_deleted") == 1) {
                    String updateRegStatusQuery = "UPDATE registrations SET is_deleted = 0 WHERE user_id = ? AND event_id = ?";
                    PreparedStatement updateRegStatusStmt = conn.prepareStatement(updateRegStatusQuery);
                    updateRegStatusStmt.setInt(1, curr_user_id);
                    updateRegStatusStmt.setInt(2, eventId);

                    updateRegStatusRow = updateRegStatusStmt.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO registrations (user_id, event_id) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, curr_user_id);
                insertStmt.setInt(2, eventId);
                rowsInserted = insertStmt.executeUpdate();
            }
            if (rowsInserted > 0 || updateRegStatusRow > 0) {

                String selectQuery =
                    "SELECT name, description, date, time, location, image_url, registered_count " +
                    "FROM events WHERE id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                selectStmt.setInt(1, eventId);
                ResultSet old_event_rs = selectStmt.executeQuery();
                old_event_rs.next();

                String oldName = old_event_rs.getString("name");
                String oldDescription = old_event_rs.getString("description");
                Date oldDate = old_event_rs.getDate("date");
                Time oldTime = old_event_rs.getTime("time");
                String oldLocation = old_event_rs.getString("location");
                String oldImageUrl = old_event_rs.getString("image_url");
                int oldRegisteredCount = old_event_rs.getInt("registered_count");

                String updateCountQuery = "UPDATE events SET registered_count = registered_count + 1 WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateCountQuery);
                updateStmt.setInt(1, eventId);
                updateStmt.executeUpdate();
                response.sendRedirect("UserProfileServlet");

                String insert_log_Query = "INSERT INTO registrations_audit_log (action, user_id, event_id) VALUES ('Registered for Event', ?, ?)";
                PreparedStatement insert_log_Stmt = conn.prepareStatement(insert_log_Query);
                insert_log_Stmt.setInt(1, curr_user_id);
                insert_log_Stmt.setInt(2, eventId);
                int audit_Rows_Inserted = insert_log_Stmt.executeUpdate();

                String insertLog =
                    "INSERT INTO event_audit_log (" +
                    "action, event_id, " +
                    "old_eventname, new_eventname, " +
                    "old_description, new_description, " +
                    "old_location, new_location, " +
                    "old_date, new_date, " +
                    "old_time, new_time, " +
                    "old_image_url, new_image_url, " +
                    "old_registered_count, new_registered_count" +
                    ") VALUES (" +
                    "'Registered for Event', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement logStmt = conn.prepareStatement(insertLog);
                int idx = 1;
                logStmt.setInt(idx++, eventId); 
                logStmt.setString(idx++, oldName);
                logStmt.setString(idx++, oldName);
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
                logStmt.setInt(idx++, ++oldRegisteredCount); 
                logStmt.executeUpdate();
                
                response.sendRedirect("UserProfileServlet");
            } else {
                out.println("<h3>Failed to register for the event!</h3>");
            }

        } catch (SQLException e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
