package com.charity.controller;

import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;

public class DeleteEventServlet extends HttpServlet {

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
            String eventId = request.getParameter("event_id");

            if (eventId == null || eventId.isEmpty()) {
                response.sendRedirect("userProfile.jsp");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                
                String query = "SELECT id FROM users WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, userEmail);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int creator_id = rs.getInt(1);

                String selectQuery = "SELECT name, description, date, time, location, image_url FROM events WHERE id = ? AND creator_id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                selectStmt.setInt(1, Integer.parseInt(eventId));
                selectStmt.setInt(2, creator_id);
                ResultSet old_event_rs = selectStmt.executeQuery();

                if (!old_event_rs.next()) {
                    response.sendRedirect("UserProfileServlet");
                    return;
                }

                String oldTitle = old_event_rs.getString("name");
                String oldDescription = old_event_rs.getString("description");
                Date oldDate = Date.valueOf(old_event_rs.getString("date"));
                Time oldTime = Time.valueOf(old_event_rs.getString("time"));
                String oldLocation = old_event_rs.getString("location");
                String oldImageUrl = old_event_rs.getString("image_url");

                String deleteSQL = "DELETE FROM events WHERE id = ? AND creator_id = ? ";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, Integer.parseInt(eventId));
                    pstmt.setInt(2, creator_id);
                    int deletedEvent = pstmt.executeUpdate();
                    
                    if(deletedEvent > 0){
                        String delete_log_Query = "INSERT INTO event_audit_log (action, event_id, old_eventname, old_description, old_date, old_location, old_time, old_image_url) VALUES ('Event Deleted', ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement delete_log_Stmt = conn.prepareStatement(delete_log_Query);
                        delete_log_Stmt.setInt(1, Integer.parseInt(eventId));
                        delete_log_Stmt.setString(2, oldTitle);
                        delete_log_Stmt.setString(3, oldDescription);
                        delete_log_Stmt.setDate(4, oldDate);
                        delete_log_Stmt.setString(5, oldLocation);
                        delete_log_Stmt.setTime(6, oldTime);
                        delete_log_Stmt.setString(7, oldImageUrl);
                        
                        int audit_Rows_Deleted = delete_log_Stmt.executeUpdate();
                    }
                }
                                
            request.getRequestDispatcher("UserProfileServlet").forward(request, response);
               
            } catch (Exception e) {
                out.println("Exception: "+ e);
            }
            
        }    
}
