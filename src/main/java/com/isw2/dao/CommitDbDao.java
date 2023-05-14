package com.isw2.dao;

import com.isw2.entity.Commit;
import com.isw2.entity.JavaFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CommitDbDao {
    private String projectName;
    private Connection conn;
    private static Logger myLogger = Logger.getLogger("logger");
    private static final String DB_URL = "jdbc:mysql://localhost:3306/isw2_scraping_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";

    public CommitDbDao() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            myLogger.info("Connessione fallita");
        }
    }

    public CommitDbDao(String projectName) {
        this.projectName = projectName;
    }

    public Connection getConn() {
        return conn;
    }

    public void closeConnection() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            myLogger.info("Chiusura connesione fallita");
            e.printStackTrace();
        }
    }

    public Connection openConnection() {
        try {
            if (!conn.isClosed()) closeConnection();
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            myLogger.info("Connessione fallita");
            e.printStackTrace();
        }
        return conn;
    }

    public void insertProject(String name, String author) {
        try (PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO project(name, author) VALUES(?,?)")) {

            ps.setString(1, name);
            ps.setString(2, author);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio progetto fallito");// definire un eccezione apposita con logger serio
        }
    }

    public void insertCommit(String sha, String message, String author, String date, String treeUrl, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO commit(sha, message, author, date, treeUrl, project_name) VALUES(?,?,?,?,?,?)")) {

            ps.setString(1, sha);
            ps.setString(2, message);
            ps.setString(3, author);
            ps.setString(4, date);
            ps.setString(5, treeUrl);
            ps.setString(6, project);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio commit fallito");// definire un eccezione apposita con logger serio
        }
    }

    public List<JavaFile> getTouchedFiles(String commitSha, String project) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<JavaFile> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM touchedFiles WHERE commit_sha = ? AND commit_project_name = ?");

            ps.setString(1, commitSha);
            ps.setString(2, project);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new JavaFile(rs.getString("filename"), rs.getString("add"), rs.getString("del"), rs.getString("content")));
            }
            rs.close();
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
            myLogger.info("Select touched files fallito");// definire un eccezione apposita con logger serio
        }finally{
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
    }

    public List<Commit> getCommits(String project) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Commit> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM commit WHERE project_name = ?");

            ps.setString(1, project);
            rs = ps.executeQuery();
            while (rs.next()) {
                String commitSha = rs.getString("sha");
                String commitMessage = rs.getString("message");
                String commitDate = rs.getString("date");
                String commitAuthor = rs.getString("author");
                String commitTreeUrl = rs.getString("treeUrl");
                List<JavaFile> touchedFile = getTouchedFiles(commitSha, project);
                ret.add(new Commit(commitSha, commitMessage, commitDate, commitAuthor, commitTreeUrl, touchedFile));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            myLogger.info("Select commits fallito");// definire un eccezione apposita con logger serio
        }finally{
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
    }

    public void insertRealeaseTreeFile(String filename, String content, String project, String releaseNum) {
        try (PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO treeFile(filename, content, release_project_name, release_number) VALUES(?,?,?,?)")) {

            ps.setString(1, filename);
            ps.setString(2, content);
            ps.setString(3, project);
            ps.setString(4, releaseNum);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio tree fallito");// definire un eccezione apposita con logger serio
        }
    }

    public void insertTouchedFile(String filename, String commitSha, String add, String del, String content, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO touchedFiles(commit_sha, commit_project_name, filename, add, del, content) VALUES(?,?,?,?,?,?)")) {

            ps.setString(1, commitSha);
            ps.setString(2, project);
            ps.setString(3, filename);
            ps.setString(4, add);
            ps.setString(5, del);
            ps.setString(6, content);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio touched files fallito");// definire un eccezione apposita con logger serio
        }
    }

    public void insertRelease(String name, String number, String start, String end, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO release(name, number, start, end, project_name) VALUES(?,?,?,?,?)")) {

            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, start);
            ps.setString(4, end);
            ps.setString(5, project);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio release fallito");// definire un eccezione apposita con logger serio
        }
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


    public void createCommitTable(Connection conn) {
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
