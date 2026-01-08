package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;

public class DeleteRegistrationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
            PrintWriter out = response.getWriter();
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("email") == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            String userEmail = (String) session.getAttribute("email");
            String eventId = request.getParameter("id");

            if (eventId == null || eventId.isEmpty()) {
                response.sendRedirect("userProfile.jsp");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                
                String query = "SELECT id FROM users WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, userEmail);
                ResultSet get_user_rs = stmt.executeQuery();
                get_user_rs.next();
                int curr_user_id = get_user_rs.getInt(1);
                
                String selectQuery =
                    "SELECT name, description, date, time, location, image_url, registered_count " +
                    "FROM events WHERE id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                selectStmt.setInt(1, Integer.parseInt(eventId));
                ResultSet old_event_rs = selectStmt.executeQuery();
                old_event_rs.next();

                String oldName = old_event_rs.getString("name");
                String oldDescription = old_event_rs.getString("description");
                Date oldDate = old_event_rs.getDate("date");
                Time oldTime = old_event_rs.getTime("time");
                String oldLocation = old_event_rs.getString("location");
                String oldImageUrl = old_event_rs.getString("image_url");
                int oldRegisteredCount = old_event_rs.getInt("registered_count");
                
                String deleteSQL = "UPDATE registrations SET is_deleted = 1 WHERE user_id = ? AND event_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, curr_user_id);
                    pstmt.setInt(2, Integer.parseInt(eventId));
                    int deletedEvent = pstmt.executeUpdate();
                    
                    if(deletedEvent > 0){
                        String updateCountQuery = "UPDATE events SET registered_count = registered_count - 1 WHERE id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateCountQuery);
                        updateStmt.setInt(1, Integer.parseInt(eventId));
                        updateStmt.executeUpdate();
                        
                        String delete_log_Query = "INSERT INTO registrations_audit_log (action, user_id, event_id) VALUES ('Deleted registration for Event',?, ?)";
                        PreparedStatement delete_log_Stmt = conn.prepareStatement(delete_log_Query);
                        delete_log_Stmt.setInt(1, curr_user_id);
                        delete_log_Stmt.setInt(2, Integer.parseInt(eventId));
                        int audit_Rows_Deleted = delete_log_Stmt.executeUpdate();

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
                            "'Deleted registration for Event', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement logStmt = conn.prepareStatement(insertLog);
                        int idx = 1;
                        logStmt.setInt(idx++, Integer.parseInt(eventId)); 
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
                        logStmt.setInt(idx++, --oldRegisteredCount); 
                        logStmt.executeUpdate();
        
                    }
                }
                
                request.getRequestDispatcher("UserProfileServlet").forward(request, response); 
            } catch (Exception e) {
                out.println("Exception: "+ e);
                out.println("<h3>Error: " + e.getMessage() + "</h3>");
            }
            
        }
    }