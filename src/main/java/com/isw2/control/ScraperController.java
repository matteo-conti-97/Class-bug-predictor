package com.isw2.control;

import com.isw2.dao.CommitDbDao;
import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.*;
import com.isw2.util.CsvHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScraperController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;
    private final CommitDbDao commitDbDao;

    public ScraperController(String projectName, String projectAuthor) {
        this.project = new Project(projectName, projectAuthor);
        this.jiraDao = new JiraDao(project.getName());
        this.gitDao = new GitDao(project.getName(), project.getAuthor());
        this.commitDbDao = new CommitDbDao();
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

    public void saveProjectOnDb() {
        commitDbDao.insertProject(this.project.getName(), this.project.getAuthor());
    }

    public void saveCommitDataOnDb(String lastRelOfInterestEndDate) {
        List<Commit> commits;
        commits = gitDao.getAllCommitsUntil(lastRelOfInterestEndDate);
        assert commits != null;
        System.out.println("commit list size: "+commits.size());
        for (Commit commit : commits) {
            commitDbDao.insertCommit(commit.getSha(), commit.getId(), commit.getMessage(), commit.getAuthor(), commit.getDate(), commit.getTreeUrl(), this.project.getName());
            saveTouchedFilesDataOnDb(commit);
        }
    }

    private void saveTouchedFilesDataOnDb(Commit commit) {
        for (JavaFile file : commit.getTouchedFiles()) {
            commitDbDao.insertTouchedFile(file.getName(), commit.getSha(), commit.getId(), file.getAdd(), file.getDel(), file.getContent(), this.project.getName());
        }
    }

    public void saveReleasesOnDb() {
        for (Release release : this.project.getReleases()) {
            commitDbDao.insertRelease(release.getName(), release.getNumber(), release.getStartDate(), release.getEndDate(), this.project.getName());
        }
    }

    public List<Commit> getCommitsFromDb() {
        List<Commit> ret = null;
        try {
            ret = commitDbDao.getCommits(this.project.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    //TODO
    /*public void saveReleaseTreeFilesOnDb(){
        for (Release release: this.project.getReleases()) {
            insertFile(String filename, String commitSha, String project, String release)
            commitDbDao.insertReleaseTree(release.getName(), release.getNumber(), release.getStartDate(), release.getEndDate(), this.project.getName());
        }
    }*/



    /*public void createAllCommitsJsonUntilDb(String relEndDate, String dbName) throws IOException, SQLException {
        Connection conn = commitDbDao.getConnection(dbName);
        commitDbDao.createCommitTable(conn);
        JSONArray commitList = gitDao.getAllCommitsJsonUntil(relEndDate);
        for (int i = 0; i < commitList.length(); i++) {
            commitDbDao.insertCommitJson(conn, commitList.getJSONObject(i).toString());
        }
    }*/

    private List<JavaFile> getFiles(JSONArray fileList) {
        List<JavaFile> ret = new ArrayList<>();
        for (int i = 0; i < fileList.length(); i++) {
            String fileName = fileList.getJSONObject(i).getString("filename");

            String rawUrl = fileList.getJSONObject(i).getString("raw_url");
            ret.add(new JavaFile(fileName, rawUrl));
        }
        return ret;
    }


    public void linkCommitsToReleases() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int j = 0; j < this.project.getReleasesOfInterest().size(); j++) {
            Release release = this.project.getReleasesOfInterest().get(j);
            for (int i = 0; i < this.project.getCommits().size(); i++) {
                Commit commit = this.project.getCommits().get(i);
                String commitDate = commit.getDate();
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
        List<List<String>> features = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        Commit lastCommit;
        List<Release> releases = this.project.getReleasesOfInterest();
        Release release;
        for (int i = 0; i < releases.size(); i++) {
            release = releases.get(i);
            //ASSUNZIONE se la release non ha commit associati, la lista di file è vuota e la lista di commit anche, avrò due file dataset uguali, lo cancello manualmente
            if (release.getCommits().isEmpty()) {
                releaseFiles.add(new ArrayList<>());
                commits.add(new ArrayList<>());
                features.add(new ArrayList<>());
            } else {
                System.out.println(i);
                commits.add(release.getCommits()); //Lista i cui elementi sono la lista di commits della release i-esima
                lastCommit = commits.get(i).get(0); //Ultima commit della release i-esima
                String commitTreeUrl = lastCommit.getTreeUrl();
                releaseFiles.add(gitDao.getRepoFileAtReleaseEnd(commitTreeUrl));
                System.out.println("Release " + release.getName() + " has " + releaseFiles.get(i).size() + " non test java files based on commit " + lastCommit.getSha());
                //TODO Ora ho la lista dei file per ogni release, per ogni lista dei file devo girarmela e calcolarmi tutte le feature per la release associata alla lista
                features.add(measureAuthorsInRelease(releaseFiles.get(i), commits.get(i)));
                //TODO Probabilmente mi conviene fare un array feature per ogni feature
                System.out.println(features);
            }
            //TODO Create CSV Format modificarlo per prendere anche le feature
            CsvHandler.writeDataLineByLine(releaseFiles, features, i + 1);

        }
    }

    //Per ogni commit nella release che ha toccato il file si guarda l'autore e si aggiunge ad una lista senza duplicati
    private List<String> measureAuthorsInRelease(List<String> releaseFiles, List<Commit> commits) {
        List<String> ret = new ArrayList<>();
        for (String filename : releaseFiles) {
            List<String> authors = new ArrayList<>();
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(filename)) {
                        String author = commit.getAuthor();
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                    }
                }
            }
            ret.add(Integer.toString(authors.size()));
        }
        System.out.println((ret.size()));
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file si guarda loc e si fa la media
    private String measureLocAtEndRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private String measureAvgChurnInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private String measureAvgChurnFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private String measureRevisionNumInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private String measureRevisionNumFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //
    private String measureRevisionAge(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
    private String measureAvgLocAddedInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }


    //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
    private String measureAvgLocAddedFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file si conta quante sono afferenti ad un bugFix
    private String measureFixCommitsInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit che ha toccato il file si conta quante sono afferenti ad un bugFix
    private String measureFixCommitsFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

}
