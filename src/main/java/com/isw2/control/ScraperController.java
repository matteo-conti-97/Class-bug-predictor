package com.isw2.control;

import com.isw2.dao.CommitDbDao;
import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ScraperController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;
    private final CommitDbDao commitDbDao;
    private static final Logger myLogger = Logger.getLogger("logger");

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
        System.out.println("commit list size: " + commits.size());
        for (Commit commit : commits) {
            commitDbDao.insertCommit(commit.getSha(), commit.getId(), commit.getMessage(), commit.getAuthor(), commit.getDate(), commit.getTreeUrl(), this.project.getName());
            saveTouchedFilesDataOnDb(commit);
        }
    }

    private void saveTouchedFilesDataOnDb(Commit commit) {
        for (JavaFile file : commit.getTouchedFiles()) {
            commitDbDao.insertTouchedFile(file.getName(), commit.getSha(), commit.getId(), file.getAdd(), file.getDel(), file.getContent(), file.getStatus(), file.getPrevName(), this.project.getName());
        }
    }

    public void saveReleasesOnDb() {
        for (Release release : this.project.getReleasesOfInterest()) {
            commitDbDao.insertRelease(release.getName(), release.getNumber(), release.getStartDate(), release.getEndDate(), this.project.getName());
        }
    }

    public void saveFileTreeOnDb() {
        List<Release> releaseOfInterest = this.project.getReleasesOfInterest();
        int start = 0; //*****Mettere un numero più alto in caso si finiscano le chiamate all'api******
        for (int i = start; i < releaseOfInterest.size(); i++) {
            Release release = releaseOfInterest.get(i);
            List<Commit> commits = release.getCommits();
            System.out.println("Processing release " + release.getName() + " - " + release.getNumber() + " tree");
            if (!commits.isEmpty()) { //Questo check è necessario per evitare eccezioni in caso la release non abbia commit
                Commit lastCommit = commits.get(0);
                List<JavaFile> treeFiles = gitDao.getRepoFileAtReleaseEnd(lastCommit.getTreeUrl());
                for (JavaFile file : treeFiles) {
                    commitDbDao.insertRealeaseFileTree(file.getName(), file.getContent(), this.project.getName(), release.getNumber());
                }
            }

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

    public List<Release> getReleasesOfInterestFromDb() {
        List<Release> ret = null;
        try {
            ret = commitDbDao.getReleases(this.project.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Get file tree for each release
        assert ret != null;
        for (Release release : ret) {
            release.setFileTreeAtReleaseEnd(getReleaseFileTreeFromDb(release));
        }
        return ret;
    }

    public List<JavaFile> getReleaseFileTreeFromDb(Release release) {
        List<JavaFile> ret = null;
        try {
            ret = commitDbDao.getReleaseFileTree(this.project.getName(), release.getNumber());
        } catch (SQLException e) {
            e.printStackTrace();
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
                //Condizione 1 ASSUNZIONE se la commit è antecedente alla data di inizio della prima release, la inglobo nella prima release
                //Condizione 2 Se la data della commit combacia con la fine dell'ultima commit la assegno all'ultima release perche non posso metterla come prima commit della successiva
                //Condizione 3 Se la data della commit si trova all'interno del range di date di una release o combacia con la data di inizio della release la considero appartenente alla release
                if (((release.getNumber().equals("1")) && (sdf.parse(commitDate).before(sdf.parse(releaseStartDate)))) || ((release.getNumber().equals(Integer.toString(this.project.getReleasesOfInterest().size()))) && (sdf.parse(commitDate).compareTo(sdf.parse(releaseEndDate)) == 0)) || ((sdf.parse(commitDate).compareTo(sdf.parse(releaseStartDate)) == 0) || ((sdf.parse(commitDate).after(sdf.parse(releaseStartDate))) && (sdf.parse(commitDate).before(sdf.parse(releaseEndDate)))))) {
                    this.project.getReleasesOfInterest().get(j).addCommit(commit);
                }

            }
        }
    }

    public void removeEmptyReleases() {
        List<Release> releaseToRemove = new ArrayList<>();
        for (Release release : this.project.getReleasesOfInterest()) {
            if (release.getCommits().isEmpty()) {
                releaseToRemove.add(release);
            }
        }
        for (Release release : releaseToRemove) {
            this.project.getReleasesOfInterest().remove(release);
        }
    }

    public void getProjectDataFromDb() throws ParseException {
        setProjectCreationDate();
        List<Release> releasesOfInterest = getReleasesOfInterestFromDb();
        setProjectReleasesOfInterest(releasesOfInterest);
        String lastInterestReleaseEndDate = getLastReleaseEndDateOfInterest();
        //ASSUNZIONE la seguente istruzione è per troncare l'ultima release di interesse bookeeper alla data di migrazione a github issue
        //scraperController.setLastReleaseEndDateOfInterest("2017-06-16");
        List<Commit> commits = getCommitsFromDb();
        setProjectCommits(commits);
        linkCommitsToReleases();
        removeEmptyReleases(); //ASSUNZIONE le release senza commit le butto
        System.out.println("Project: " + getProjectName());
        System.out.println("Creation date: " + getProjectCreationDate());
        System.out.println("Last interest release end date: " + lastInterestReleaseEndDate);
        System.out.println("\nReleases of interest: ");
        for (Release release : releasesOfInterest) {
            System.out.println("Release: " + release.getName() + " number " + release.getNumber() + " has " + release.getCommits().size() + " commits and " + release.getFileTreeAtReleaseEnd().size() + " non test java files, starts at " + release.getStartDate() + " and ends at " + release.getEndDate());
        }

        List<Ticket> allTickets = getAllTickets();
        setProjectFixedBugTickets(allTickets);
        List<Ticket> ticketOfInterest =getTicketsOfInterest(lastInterestReleaseEndDate);
        setProjectFixedBugTicketsOfInterest(ticketOfInterest);
        linkTicketDatesToReleases();

        System.out.println("\n" + ticketOfInterest.size() + " tickets of interest:");
        for (Ticket tmp : ticketOfInterest) {
            String stringa=tmp.getKey() + " Resolution Date: " + tmp.getResolutionDate()+ " Fix Version: " + tmp.getFv() + " Creation Date: " + tmp.getCreationDate() + " Opening Version:" + tmp.getOv();
            System.out.println(stringa);
            System.out.println("\tAffected Versions: ");
            for(String av: tmp.getJiraAv()){
                System.out.println("\t\tVersion: "+av);
            }
        }
    }

    public void saveProjectDataOnDb() {
        setProjectCreationDate();
        List<Release> allReleases = getAllReleases();
        setProjectReleases(allReleases);
        List<Release> releasesOfInterest = getReleasesOfInterest("4.4.0");
        setProjectReleasesOfInterest(releasesOfInterest);

        String lastInterestReleaseEndDate = getLastReleaseEndDateOfInterest();
        /*ASSUNZIONE la seguente istruzione è per troncare l'ultima release di interesse bookeeper alla data di migrazione
        a github issue*/
        //scraperController.setLastReleaseEndDateOfInterest("2017-06-16");
        System.out.println("Project: " + getProjectName());
        System.out.println("Creation date: " + getProjectCreationDate());
        System.out.println("Last interest release end date: " + lastInterestReleaseEndDate);
        System.out.println("Releases of interest: ");

        saveProjectOnDb();
        saveCommitDataOnDb(lastInterestReleaseEndDate);
        saveReleasesOnDb();

        List<Commit> commits = getCommitsFromDb();
        setProjectCommits(commits);
        try {
            linkCommitsToReleases();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //for (Release release : releasesOfInterest) {
        //    System.out.println("Release: " + release.getName() + " number " + release.getNumber() + " has " + release.getCommits().size() + " commits and " + release.getFileTreeAtReleaseEnd().size() + " non test java files, starts at " + release.getStartDate() + " and ends at " + release.getEndDate());
        //}
        saveFileTreeOnDb();
    }

    public void linkTicketDatesToReleases() throws ParseException {
        List<Ticket> tickets=this.project.getFixedBugTicketsOfInterest();
        List<Release> releases=this.project.getReleasesOfInterest();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        for(Ticket ticket:tickets){
            for(Release release:releases){
                String ticketCreationDate=ticket.getCreationDate();
                String ticketResDate=ticket.getResolutionDate();
                String relCreationDate=release.getStartDate();
                String relEndDate=release.getEndDate();
                if((sdf.parse(ticketResDate).after(sdf.parse(relCreationDate)))&&(sdf.parse(ticketResDate).before(sdf.parse(relEndDate)))){
                    ticket.setFv(release.getName());
                    ticket.setFvNum(release.getNumber());
                }
                if((sdf.parse(ticketCreationDate).after(sdf.parse(relCreationDate)))&&(sdf.parse(ticketResDate).before(sdf.parse(relEndDate)))){
                    ticket.setOv(release.getName());
                    ticket.setOvNum(release.getNumber());
                }
            }
        }
    }

}
