<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>
<%@page import="com.charity.model.DatabaseConnection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Charity Event Finder</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">   
    <link rel="stylesheet" href="./css/ind_style.css">   
    <link rel="stylesheet" href="css/global_style.css">
</head>
<body>
    
    <% 
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps1 = conn.prepareStatement("SELECT COUNT(*) FROM events");
        PreparedStatement ps2 = conn.prepareStatement("SELECT COUNT(DISTINCT location) FROM events");
        PreparedStatement ps3 = conn.prepareStatement("SELECT COUNT(*) FROM users");
        ResultSet rs1 = ps1.executeQuery();
        ResultSet rs2 = ps2.executeQuery();
        ResultSet rs3 = ps3.executeQuery();
        rs1.next(); rs2.next(); rs3.next();
        int []counts = {rs1.getInt(1), rs2.getInt(1), rs3.getInt(1)};
    %>
    <header>
        <div class="title">
            <h1>एकता</h1>
            <p>Kyuki, asli maza sab ke saath aata hai!</p>
        </div>
        <div class="logo">
            <img src="images/logo.png" alt="logo"/>
        </div>
    </header>

    <nav>
        <ul>
            <li><a href="login.jsp">Login</a></li>
            <li><a href="register.jsp">Register</a></li>
        </ul>
    </nav>

    <main>
        <section>
            <h2>Features</h2>
            <ul>
                <li>Discover upcoming charity events</li>
                <li>Manage your registrations through your profile</li>
                <li>Create events and allow people to contribute</li>
            </ul>
        </section>

        <section>
            <h2 class="main-title">We Have</h2>
            <div class="stats-container">
                <div class="stat">
                    <img src="images/places_covered.jpg" alt="Places Covered">
                    <p>We have covered over <strong><%= counts[1] %></strong> places!</p>
                </div>
                <div class="stat">
                    <img src="images/people_joined.jpg" alt="People Joined">
                    <p><strong><%= counts[2] %></strong> people have joined us till date!</p>
                </div>
                <div class="stat">
                    <img src="images/events_covered.jpeg" alt="Events Covered">
                    <p>We have covered <strong><%= counts[0] %></strong> events so far!</p>
                </div>
            </div>
        </section>

        <section>
            <h2>About Us</h2>
            <p>Charity Event Finder connects users to meaningful charity events. Explore available events and register to participate in causes that matter to you.</p>
        </section>
    </main>

    <footer>
        <p>&copy; 2025 Charity Event Finder. All rights reserved.</p>
    </footer>
</body>
</html>