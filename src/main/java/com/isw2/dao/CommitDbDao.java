package com.isw2.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class CommitDbDao {
    private final String projectName;

    public CommitDbDao(String projectName) {
        this.projectName = projectName;
    }

    public Connection getConnection(String fileName) throws SQLException {
        String url = "jdbc:sqlite:./src/main/java/resource/db/" + projectName + "_" + fileName + ".db";
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }

    public void insertCommitJson(Connection conn, String commitJson) throws SQLException {
        String query = "INSERT INTO commits(json) VALUES(?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, commitJson);
        pstmt.executeUpdate();

    }

    public JSONArray getCommitsJson(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT json FROM commits";
        ResultSet rs = stmt.executeQuery(query);
        JSONArray ret = new JSONArray();
        while (rs.next()) {
            ret.put(new JSONObject(rs.getString("json")));
        }
        return ret;
    }


    public void createCommitTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "CREATE TABLE IF NOT EXISTS commits (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	json text NOT NULL\n"
                + ");";
        stmt.executeUpdate(query);
    }


}
