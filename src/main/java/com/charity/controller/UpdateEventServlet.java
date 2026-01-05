package com.charity.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.charity.model.DatabaseConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.InputStream;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 20 * 1024 * 1024 // 20 MB
)
public class UpdateEventServlet extends HttpServlet {

    private static final String AZURE_CONNECTION = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = "event-images"; 
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Session / login check
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String email = (String) session.getAttribute("email");
        String eventIdStr = request.getParameter("eventId");
        String new_name = request.getParameter("name");
        String new_Description = request.getParameter("description");
        String new_Location = request.getParameter("location");
        Part filePart = request.getPart("image_url"); 
        String imageUrl = "";
        String dateParam = request.getParameter("date");
        String timeParam = request.getParameter("time");

        Date new_Date = null;
        Time new_Time = null;

        try {
            if (dateParam != null && !dateParam.isEmpty()) {
                new_Date = Date.valueOf(dateParam); // yyyy-MM-dd
            }
            if (timeParam != null && !timeParam.isEmpty()) {
                // Time.valueOf needs HH:mm:ss, so append ":00"
                if (timeParam.length() == 5) { // HH:mm
                    timeParam = timeParam + ":00";
                }
                new_Time = Time.valueOf(timeParam);
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
            if (!rs.next()) {
                response.sendRedirect("login.jsp");
                return;
            }
            int creator_id = rs.getInt(1);

            String selectQuery =
                    "SELECT name, description, date, time, location, image_url, registered_count " +
                    "FROM events WHERE id = ? AND creator_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, Integer.parseInt(eventIdStr));
            selectStmt.setInt(2, creator_id);
            ResultSet old_event_rs = selectStmt.executeQuery();

            if (!old_event_rs.next()) {
                response.sendRedirect("UserProfileServlet");
                return;
            }

            String oldName = old_event_rs.getString("name");
            String oldDescription = old_event_rs.getString("description");
            Date oldDate = old_event_rs.getDate("date");
            Time oldTime = old_event_rs.getTime("time");
            String oldLocation = old_event_rs.getString("location");
            String oldImageUrl = old_event_rs.getString("image_url");
            int oldRegisteredCount = old_event_rs.getInt("registered_count");

            if (safeEquals(oldName, new_name) &&
                safeEquals(oldDescription, new_Description) &&
                safeEqualsDate(oldDate, new_Date) &&
                safeEqualsTime(oldTime, new_Time) &&
                safeEquals(oldLocation, new_Location) &&
                safeEquals(imageUrl, "")) {

                response.sendRedirect(request.getContextPath() + "/UserProfileServlet");
                return;
            }

            if (safeEquals(imageUrl, "")) {
                imageUrl= oldImageUrl;
            }
            
            String updateQuery =
                    "UPDATE events " +
                    "SET name = ?, description = ?, date = ?, time = ?, location = ?, image_url = ? " +
                    "WHERE id = ? AND creator_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, new_name);
            updateStmt.setString(2, new_Description);
            updateStmt.setDate(3, new_Date);
            updateStmt.setTime(4, new_Time);
            updateStmt.setString(5, new_Location);
            updateStmt.setString(6, imageUrl);
            updateStmt.setInt(7, Integer.parseInt(eventIdStr));
            updateStmt.setInt(8, creator_id);

            int updated = updateStmt.executeUpdate();

            if (updated > 0) {
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
                        "'Updated Event', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement logStmt = conn.prepareStatement(insertLog);
                int idx = 1;
                logStmt.setInt(idx++, Integer.parseInt(eventIdStr)); 
                logStmt.setString(idx++, oldName);
                logStmt.setString(idx++, new_name);
                logStmt.setString(idx++, oldDescription);
                logStmt.setString(idx++, new_Description);
                logStmt.setString(idx++, oldLocation);
                logStmt.setString(idx++, new_Location);
                logStmt.setDate(idx++, oldDate);
                logStmt.setDate(idx++, new_Date);
                logStmt.setTime(idx++, oldTime);
                logStmt.setTime(idx++, new_Time);
                logStmt.setString(idx++, oldImageUrl);
                logStmt.setString(idx++, imageUrl);
                logStmt.setInt(idx++, oldRegisteredCount);
                logStmt.setInt(idx++, oldRegisteredCount); 

                logStmt.executeUpdate();
            }

            // 6. Redirect back to profile
            response.sendRedirect(request.getContextPath() + "/UserProfileServlet");

        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean safeEqualsDate(Date a, Date b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean safeEqualsTime(Time a, Time b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
