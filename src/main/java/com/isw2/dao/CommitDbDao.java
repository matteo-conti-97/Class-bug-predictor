package com.isw2.dao;

import com.isw2.model.Commit;
import com.isw2.model.JavaFile;
import com.isw2.model.Release;
import com.isw2.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CommitDbDao {
    private Connection conn;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/isw2_scraping_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitDbDao.class);

    public CommitDbDao() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Connessione fallita");
        }
    }

    public Connection getConn() {
        return conn;
    }

    public void closeConnection() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            LOGGER.info("Chiusura connesione fallita");
            e.printStackTrace();
        }
    }


    public void insertProject(String name, String author) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO project(name, author) VALUES(?,?) ON DUPLICATE KEY UPDATE author = ?")) {

            ps.setString(1, name);
            ps.setString(2, author);
            ps.setString(3, author);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            LOGGER.info("Salvataggio progetto fallito");
        }
    }

    public void insertCommit(String sha, String id, String message, String author, String date, String treeUrl, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO commit(sha, id, message, author, cdate, treeUrl, project_name) VALUES(?,?,?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE author = ?")) {

            ps.setString(1, sha);
            ps.setString(2, id);
            ps.setString(3, message.substring(0,25));
            ps.setString(4, author);
            ps.setString(5, date);
            ps.setString(6, treeUrl);
            ps.setString(7, project);
            ps.setString(8, author);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            LOGGER.info("Salvataggio commit fallito");
        }
    }

    public void insertRealeaseFileTree(String filename, String content, String project, String releaseNum) {
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
            e.printStackTrace();
            LOGGER.info("Salvataggio tree fallito");
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
            LOGGER.info("Salvataggio touched files fallito");
        }
    }

    public void insertTicket(String key, String resDate, String creationDate, String projectName, String av){
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ticket(project, tkey, resDate, creationDate, av) VALUES(?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE resDate = ?")) {

            ps.setString(1, projectName);
            ps.setString(2, key);
            ps.setString(3, resDate);
            ps.setString(4, creationDate);
            ps.setString(5, av);
            ps.setString(6, resDate);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            LOGGER.info("Salvataggio ticket fallito");
        }
    }

    public void insertRelease(String name, String number, String start, String end, String project) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO releases(rname, rnumber, rstart, rend, project_name) VALUES(?,?,?,?,?)" +
                        "ON DUPLICATE KEY UPDATE rstart = ?")) {

            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, start);
            ps.setString(4, end);
            ps.setString(5, project);
            ps.setString(6, start);
            ps.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            LOGGER.info("Salvataggio release fallito");
        }
    }

    public List<Release> getReleases(String project) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Release> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM releases WHERE project_name = ? ORDER BY rstart ASC");

            ps.setString(1, project);
            rs = ps.executeQuery();
            while (rs.next()) {
                String releaseNum = rs.getString("rnumber");
                String releaseName = rs.getString("rname");
                String releaseStart = rs.getString("rstart");
                String releaseEnd = rs.getString("rend");
                ret.add(new Release(releaseName, Integer.parseInt(releaseNum), releaseStart, releaseEnd));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.info("Select commits fallito");
        } finally {
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
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
                ret.add(new JavaFile(rs.getString("filename"), rs.getString("adds"), rs.getString("dels"), rs.getString("content"), rs.getString("status"), rs.getString("pname")));
            }
            rs.close();
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.info("Select touched files fallito");
        } finally {
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
                    "SELECT * FROM commit WHERE project_name = ? ORDER BY cdate DESC");

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
            e.printStackTrace();
            LOGGER.info("Select commits fallito");
        } finally {
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
    }

    public List<JavaFile> getReleaseFileTree(String project, String releaseNum) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<JavaFile> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM treefile WHERE release_number = ? AND release_project_name = ?");

            ps.setString(1, releaseNum);
            ps.setString(2, project);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new JavaFile(rs.getString("filename"), rs.getString("content")));
            }
            rs.close();
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.info("Select touched files fallito");
        } finally {
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
    }

    private List<Ticket> getTicketSet(String project) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Ticket> ret = new ArrayList<>();

        try {
            ps = conn.prepareStatement(
                    "SELECT DISTINCT(tkey), resDate, creationDate FROM ticket WHERE ticket.project = ?");
            ps.setString(1, project);
            rs = ps.executeQuery();
            while (rs.next()) {
                Ticket ticket = new Ticket(rs.getString("tkey"));
                ticket.setCreationDate(rs.getString("creationDate"));
                ticket.setResolutionDate(rs.getString("resDate"));
                ret.add(ticket);
            }
            rs.close();
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.info("Select ticket fallito");
        } finally {
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
        return ret;
    }

    public List<Ticket> getTickets(String project) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Ticket> ticketSet= getTicketSet(project);

        for(Ticket ticket: ticketSet) {
            List<Release> av = new ArrayList<>();
            try {
                ps = conn.prepareStatement(
                        "SELECT * FROM ticket AS t JOIN releases AS r ON t.av = r.rname " +
                                "WHERE t.project = ? and r.project_name= ? and tkey= ?");
                ps.setString(1, project);
                ps.setString(2, project);
                ps.setString(3, ticket.getKey());
                rs = ps.executeQuery();
                while (rs.next()) {
                    av.add(new Release(rs.getString("rname"), Integer.parseInt(rs.getString("rnumber"))));
                }
                rs.close();
                ticket.setJiraAv(av);
            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.info("Select ticket {} fallito ", ticket.getKey());
            } finally {
                assert ps != null;
                ps.close();
                assert rs != null;
                rs.close();
            }
        }
        return ticketSet;
    }
}
