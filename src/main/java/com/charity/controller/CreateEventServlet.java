package com.charity.controller;

import com.azure.storage.blob.*; 
import com.charity.model.DatabaseConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 20 * 1024 * 1024 // 20 MB
)
public class CreateEventServlet extends HttpServlet {

    private static final String AZURE_CONNECTION = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = "event-images"; 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String email = (String) session.getAttribute("email");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        Part filePart = request.getPart("image_url"); 
        String imageUrl = "";
        
        String dateParam = request.getParameter("date");
        String timeParam = request.getParameter("time");

        Date formated_Date = null;
        Time formated_Time = null;

        try {
            if (dateParam != null && !dateParam.isEmpty()) {
                formated_Date = Date.valueOf(dateParam); // yyyy-MM-dd
            }
            if (timeParam != null && !timeParam.isEmpty()) {
                // Time.valueOf needs HH:mm:ss, so append ":00"
                if (timeParam.length() == 5) { // HH:mm
                    timeParam = timeParam + ":00";
                }
                formated_Time = Time.valueOf(timeParam);
            }
        } catch (IllegalArgumentException e) {
            out.println("Invalid date/time format");
            return;
        }

        if (filePart != null && filePart.getSize() > 0) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + filePart.getSubmittedFileName();
                
                // 2. Initialize Azure Blob Client
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(AZURE_CONNECTION)
                        .buildClient();
                
                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
                BlobClient blobClient = containerClient.getBlobClient(fileName);

                // 3. Upload the file stream
                try (InputStream is = filePart.getInputStream()) {
                    blobClient.upload(is, filePart.getSize(), true);
                }

                // 4. Get the Public URL
                imageUrl = blobClient.getBlobUrl();
                
            } catch (Exception e) {
                out.println("Azure Upload Error: " + e.getMessage());
                return;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) { 
            
            String query = "SELECT id FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int creator_id = rs.getInt(1);
                
                String insertQuery = "INSERT INTO events (creator_id, name, description, location, date, time, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, creator_id);
                insertStmt.setString(2, name);
                insertStmt.setString(3, description);
                insertStmt.setString(4, location);
                insertStmt.setDate(5, formated_Date);
                insertStmt.setTime(6, formated_Time);
                insertStmt.setString(7, imageUrl); 

                int i = insertStmt.executeUpdate();
                
                if(i > 0){
                    String getEventQuery = "SELECT id FROM events WHERE name = ?";
                    PreparedStatement get_created_event_stmt = conn.prepareStatement(getEventQuery);
                    get_created_event_stmt.setString(1, name);
                    ResultSet get_event_rs = get_created_event_stmt.executeQuery();
                    if (!get_event_rs.next()) {
                        response.sendRedirect(request.getContextPath() + "/UserProfileServlet");
                        return;
                    }
                    int event_id = get_event_rs.getInt(1);
                    
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
                        "'Event Created', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement logStmt = conn.prepareStatement(insertLog);
                    int idx = 1;
                    logStmt.setInt(idx++, event_id); 
                    logStmt.setString(idx++, name);
                    logStmt.setString(idx++, name);
                    logStmt.setString(idx++, description);
                    logStmt.setString(idx++, description);
                    logStmt.setString(idx++, location);
                    logStmt.setString(idx++, location);
                    logStmt.setDate(idx++, formated_Date);
                    logStmt.setDate(idx++, formated_Date);
                    logStmt.setTime(idx++, formated_Time);
                    logStmt.setTime(idx++, formated_Time);
                    logStmt.setString(idx++, imageUrl);
                    logStmt.setString(idx++, imageUrl);
                    logStmt.setInt(idx++, 0);
                    logStmt.setInt(idx++, 0); 

                    logStmt.executeUpdate();
                }
                
                response.sendRedirect(request.getContextPath() + "/UserProfileServlet");
            }
        } catch (Exception e) {
            out.println("Database Error: " + e.getMessage());
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}