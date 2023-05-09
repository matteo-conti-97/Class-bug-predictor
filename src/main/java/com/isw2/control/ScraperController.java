package com.isw2.control;

import com.isw2.dao.CommitDbDao;
import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.*;
import com.isw2.util.CsvHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ScraperController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;
    private final CommitDbDao commitDbDao;

    public ScraperController(String projectName, String projectAuthor) {
        this.project = new Project(projectName, projectAuthor);
        this.jiraDao = new JiraDao(project.getName());
        this.gitDao = new GitDao(project.getName(), project.getAuthor());
        this.commitDbDao = new CommitDbDao(project.getName());
    }


    public String getProjectName() {
        return project.getName();
    }

    public String getProjectAuthor() {
        return project.getAuthor();
    }

    public String getProjectCreationDate() {
        return project.getCreationDate();
    }

    public void setProjectCreationDate() {
        this.project.setCreationDate(gitDao.getProjectCreationDate());
    }

    public void setProjectCreationDate(String creationDate) {
        this.project.setCreationDate(creationDate);
    }

    public List<Commit> getProjectCommits() {
        return project.getCommits();
    }

    public void setProjectCommits(List<Commit> commits) {
        this.project.setCommits(commits);
    }

    public List<Ticket> getProjectFixedBugTickets() {
        return project.getFixedBugTickets();
    }

    public void setProjectFixedBugTickets(List<Ticket> tickets) {
        this.project.setFixedBugTickets(tickets);
    }

    public List<Ticket> getProjectFixedBugTicketsOfInterest() {
        return project.getFixedBugTicketsOfInterest();
    }

    public void setProjectFixedBugTicketsOfInterest(List<Ticket> tickets) {
        this.project.setFixedBugTicketsOfInterest(tickets);
    }

    public List<Release> getProjectReleases() {
        return project.getReleases();
    }

    public void setProjectReleases(List<Release> releases) {
        this.project.setReleases(releases);
    }

    public List<Release> getProjectReleasesOfInterest() {
        return project.getReleasesOfInterest();
    }

    public void setProjectReleasesOfInterest(List<Release> interestReleases) {
        this.project.setReleasesOfInterest(interestReleases);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JiraDao getJiraDao() {
        return jiraDao;
    }

    public void setJiraDao(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    public GitDao getGitDao() {
        return gitDao;
    }

    public void setGitDao(GitDao gitDao) {
        this.gitDao = gitDao;
    }

    public String getLastReleaseEndDateOfInterest() {
        return project.getReleasesOfInterest().get(project.getReleasesOfInterest().size() - 1).getEndDate();
    }

    public void setLastReleaseEndDateOfInterest(String date) {
        this.project.getReleasesOfInterest().get(this.project.getReleasesOfInterest().size() - 1).setEndDate(date);
    }

    public List<Release> getAllReleases() {
        return jiraDao.getAllReleases();
    }

    public List<Ticket> getAllTickets() {
        return jiraDao.getAllFixedBugTickets(0);
    }

    //Get all releases until the specified release, lastRel must be the name of the jira format e.g 4.4.0
    public List<Release> getReleasesOfInterest(String lastRel) {
        List<Release> ret = new ArrayList<>();
        for (Release release : project.getReleases()) {
            ret.add(release);
            if (release.getName().equals(lastRel)) {
                break;
            }
        }
        return ret;
    }

    //Get all tickets closed until the specified release so with the resolution date in the date range of the release
    public List<Ticket> getTicketsOfInterest(String relEndDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Ticket> ret = new ArrayList<>();
        for (Ticket ticket : project.getFixedBugTickets()) {
            String ticketResDate = ticket.getResolutionDate();
            if (sdf.parse(ticketResDate).before(sdf.parse(relEndDate))) {
                ret.add(ticket);
            }
        }
        return ret;
    }

    public void createAllCommitsJsonUntilDb(String relEndDate, String dbName) throws IOException, SQLException {
        Connection conn = commitDbDao.getConnection(dbName);
        commitDbDao.createCommitTable(conn);
        JSONArray commitList = gitDao.getAllCommitsJsonUntil(relEndDate);
        for (int i = 0; i < commitList.length(); i++) {
            commitDbDao.insertCommitJson(conn, commitList.getJSONObject(i).toString());
        }
    }

    private List<File> getFiles(JSONArray fileList) {
        List<File> ret = new ArrayList<>();
        for (int i = 0; i < fileList.length(); i++) {
            String fileName = fileList.getJSONObject(i).getString("filename");

            String rawUrl = fileList.getJSONObject(i).getString("raw_url");
            ret.add(new File(fileName, rawUrl));
        }
        return ret;
    }

    public List<Commit> getCommitsFromDb(String dbName) throws SQLException {
        List<Commit> ret = new ArrayList<>();
        Connection conn = commitDbDao.getConnection(dbName);
        JSONArray commitsJson = commitDbDao.getCommitsJson(conn);
        for (int i = 0; i < commitsJson.length(); i++) {
            String commitSha = commitsJson.getJSONObject(i).getString("sha");
            String commitUrl = commitsJson.getJSONObject(i).getString("url");
            List<File> files = getFiles(commitsJson.getJSONObject(i).getJSONArray("files"));
            JSONObject commitCamp = commitsJson.getJSONObject(i).getJSONObject("commit");
            String treeUrl = commitCamp.getJSONObject("tree").getString("url");
            String commitMessage = commitCamp.getString("message");
            String commitDate = commitCamp.getJSONObject("author").getString("date").substring(0, 10);
            String author = commitCamp.getJSONObject("author").getString("name");
            ret.add(new Commit(commitSha, commitMessage, commitDate, commitUrl, treeUrl, author, files));
        }
        return ret;
    }


    public void linkCommitsToReleases() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int j = 0; j < this.project.getReleasesOfInterest().size(); j++) {
            Release release = this.project.getReleasesOfInterest().get(j);
            for (int i = 0; i < this.project.getCommits().size(); i++) {
                Commit commit = this.project.getCommits().get(i);
                String commitDate = commit.getCommitDate();
                String releaseStartDate = release.getStartDate();
                String releaseEndDate = release.getEndDate();
                //ASSUNZIONE se la commit è antecedente alla data di inizio della prima release, la inglobo nella prima release
                if ((release.getNumber().equals("1")) && (sdf.parse(commitDate).before(sdf.parse(releaseStartDate)))) {
                    this.project.getReleasesOfInterest().get(j).addCommit(commit);
                }
                //Se la data della commit si trova all'interno del range di date di una release o combacia con la data di inizio della release la considero appartenente alla release
                if ((sdf.parse(commitDate).compareTo(sdf.parse(releaseStartDate)) == 0) || ((sdf.parse(commitDate).after(sdf.parse(releaseStartDate))) && (sdf.parse(commitDate).before(sdf.parse(releaseEndDate))))) {
                    this.project.getReleasesOfInterest().get(j).addCommit(commit);
                }
            }
        }
    }

    public void createWalkForwardDatasets() {
        List<List<String>> releaseFiles = new ArrayList<>();
        Commit lastCommit;
        List<Release> releases = this.project.getReleasesOfInterest();
        Release release;
        for (int i = 0; i < releases.size(); i++) {
            release = releases.get(i);
            //ASSUNZIONE se la release non ha commit associati, la lista di file è vuota, avrò due file dataset uguali, lo cancello manualmente
            if (release.getCommits().isEmpty()) {
                releaseFiles.add(new ArrayList<>());
            } else {
                lastCommit = release.getCommits().get(0);
                String commitTreeUrl = lastCommit.getTreeUrl();
                releaseFiles.add(gitDao.getRepoFileAtReleaseEnd(commitTreeUrl));
                System.out.println("Release " + release.getName() + " has " + releaseFiles.get(i).size() + " non test java files based on commit " + lastCommit.getCommitSha());

            }
            //TODO Create CSV Format
            CsvHandler.writeDataLineByLine(releaseFiles, i + 1);

        }
    }

}
