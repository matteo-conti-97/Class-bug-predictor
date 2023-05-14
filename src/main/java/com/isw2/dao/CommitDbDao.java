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
    private static final Logger myLogger = Logger.getLogger("logger");
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
                "INSERT INTO project(name, author) VALUES(?,?) ON DUPLICATE KEY UPDATE author = ?")) {

            ps.setString(1, name);
            ps.setString(2, author);
            ps.setString(3, author);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            //e.printStackTrace();
            myLogger.info("Salvataggio progetto fallito");// definire un eccezione apposita con logger serio
        }
    }

    public void insertCommit(String sha, String id, String message, String author, String date, String treeUrl, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO commit(sha, id, message, author, cdate, treeUrl, project_name) VALUES(?,?,?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE author = ?")) {

            ps.setString(1, sha);
            ps.setString(2, id);
            ps.setString(3, message);
            ps.setString(4, author);
            ps.setString(5, date);
            ps.setString(6, treeUrl);
            ps.setString(7, project);
            ps.setString(8, author);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio commit fallito");// definire un eccezione apposita con logger serio
        }
    }

    public List<JavaFile> getTouchedFiles(String commitSha, String project, String commitId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<JavaFile> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM touchedfiles WHERE commit_sha = ? AND commit_project_name = ? AND commit_id = ?");

            ps.setString(1, commitSha);
            ps.setString(2, project);
            ps.setString(3, commitId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new JavaFile(rs.getString("filename"), rs.getString("adds"), rs.getString("dels"), rs.getString("content")));
            }
            rs.close();
            return ret;

        } catch (SQLException e) {
            //e.printStackTrace();
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
                String commitId = rs.getString("id");
                String commitSha = rs.getString("sha");
                String commitMessage = rs.getString("message");
                String commitDate = rs.getString("cdate");
                String commitAuthor = rs.getString("author");
                String commitTreeUrl = rs.getString("treeUrl");
                List<JavaFile> touchedFile = getTouchedFiles(commitSha, project, commitId);
                ret.add(new Commit(commitId, commitSha, commitMessage, commitDate, commitAuthor, commitTreeUrl, touchedFile));
            }
            rs.close();
        } catch (SQLException e) {
            //e.printStackTrace();
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
                "INSERT INTO treefile(filename, content, release_project_name, release_number) VALUES(?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE content = ?")) {

            ps.setString(1, filename);
            ps.setString(2, content);
            ps.setString(3, project);
            ps.setString(4, releaseNum);
            ps.setString(5, content);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            //e.printStackTrace();
            myLogger.info("Salvataggio tree fallito");// definire un eccezione apposita con logger serio
        }
    }

    public void insertTouchedFile(String filename, String commitSha, String commitId, String add, String del, String content, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO touchedfiles(commit_sha, commit_id, commit_project_name, filename, adds, dels, content) VALUES(?,?,?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE adds = ?")) {

            ps.setString(1, commitSha);
            ps.setString(2, commitId);
            ps.setString(3, project);
            ps.setString(4, filename);
            ps.setString(5, add);
            ps.setString(6, del);
            ps.setString(7, content);
            ps.setString(8, add);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            myLogger.info("Salvataggio touched files fallito");// definire un eccezione apposita con logger serio
            myLogger.info("commitSha: " + commitSha + "commitId" + commitId + filename + " add: " + add + " del: " + del + " content: " + content + " project: " + project);
        }
    }

    public void insertRelease(String name, String number, String start, String end, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO release(name, number, rstart, rend, project_name) VALUES(?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE rstart = ?")) {

            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, start);
            ps.setString(4, end);
            ps.setString(5, project);
            ps.setString(6, start);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            //e.printStackTrace();
            myLogger.info("Salvataggio release fallito");// definire un eccezione apposita con logger serio
        }
    }

}
