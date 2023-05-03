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
        return DriverManager.getConnection(url);
    }

    public void insertCommitJson(Connection conn, String commitJson) {
        String query = "INSERT INTO commits(json) VALUES(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, commitJson);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public JSONArray getCommitsJson(Connection conn) {
        String query = "SELECT json FROM commits";
        JSONArray ret = new JSONArray();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                ret.put(new JSONObject(rs.getString("json")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public void createCommitTable(Connection conn)  {
        String query = "CREATE TABLE IF NOT EXISTS commits (\n"
                + "id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "json text NOT NULL\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
