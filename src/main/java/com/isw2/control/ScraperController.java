package com.isw2.control;

import com.isw2.dao.CommitDbDao;
import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.model.*;
import com.isw2.util.AuthJsonParser;
import com.isw2.util.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScraperController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;
    private final CommitDbDao commitDbDao;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperController.class);

    public ScraperController(String projectName, String projectAuthor, String projectCreationDate) {
        this.project = new Project(projectName, projectAuthor, projectCreationDate);
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
        return jiraDao.getAllReleases(this.project.getCreationDate());
    }

    public List<Ticket> getAllTickets() {
        return jiraDao.getAllFixedBugTickets(0);
    }

    // Get all releases until the specified release, lastRel must be the name of the
    // jira format e.g 4.4.0
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

    // Get all tickets closed until the specified release so with the resolution
    // date in the date range of the release
    public List<Ticket> getTicketsOfInterest(String relEndDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
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
        int commitSize = commits.size();
        LOGGER.info("commit list size: {}", commitSize);
        for (Commit commit : commits) {
            commitDbDao.insertCommit(commit.getSha(), commit.getId(), commit.getMessage(), commit.getAuthor(),
                    commit.getDate(), commit.getTreeUrl(), this.project.getName());
            saveTouchedFilesDataOnDb(commit);
        }
    }

    private void saveTouchedFilesDataOnDb(Commit commit) {
        for (JavaFile file : commit.getTouchedFiles()) {
            commitDbDao.insertTouchedFile(file.getName(), commit.getSha(), commit.getId(), file.getAdd(), file.getDel(),
                    file.getContent(), this.project.getName());
        }
    }

    public void saveReleasesOfInterestOnDb() {
        for (Release release : this.project.getReleasesOfInterest()) {
            commitDbDao.insertRelease(release.getName(), release.getNumberStr(), release.getStartDate(),
                    release.getEndDate(), this.project.getName());
        }
    }

    public void saveReleases() {
        for (Release release : this.project.getReleases()) {
            commitDbDao.insertRelease(release.getName(), release.getNumberStr(), release.getStartDate(),
                    release.getEndDate(), this.project.getName());
        }
    }

    public void saveReleasesOnDb() {
        for (Release release : this.project.getReleasesOfInterest()) {
            commitDbDao.insertRelease(release.getName(), release.getNumberStr(), release.getStartDate(),
                    release.getEndDate(), this.project.getName());
        }
    }

    public void saveTicketsOnDb(List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            if (!ticket.getJiraAv().isEmpty()) {
                for (Release av : ticket.getJiraAv()) {
                    commitDbDao.insertTicket(ticket.getKey(), ticket.getResolutionDate(), ticket.getCreationDate(),
                            this.project.getName(), av.getName());
                }
            } else {
                commitDbDao.insertTicket(ticket.getKey(), ticket.getResolutionDate(), ticket.getCreationDate(),
                        this.project.getName(), "");
            }
        }
    }

    public void saveFileTreeOnDb(List<Release> releases) {
        int start = 0; // *****Mettere un numero più alto in caso si finiscano le chiamate
                       // all'api******
        for (int i = start; i < releases.size(); i++) {
            Release release = releases.get(i);
            List<Commit> commits = release.getCommits();
            String releaseName = release.getName();
            String releaseNumber = release.getNumberStr();
            LOGGER.info("Processing release {} - {} tree", releaseName, releaseNumber);
            if (!commits.isEmpty()) { // Questo check è necessario per evitare eccezioni in caso la release non abbia
                                      // commit
                Commit lastCommit = commits.get(0);
                List<JavaFile> treeFiles = null;
                try {
                    treeFiles = gitDao.getRepoFileAtReleaseEnd(lastCommit.getTreeUrl());
                } catch (IOException e) {
                    int token = AuthJsonParser.getFlagToken();
                    if (token <= 5)
                        LOGGER.info("Token {} scaduto, swappo con token {}", token, token + 1);
                    else
                        LOGGER.info("Token finiti");
                    AuthJsonParser.setFlagToken(token + 1);
                    // Mettere un i-- per ripetere la chiamata con il nuovo token
                    continue;
                }
                for (JavaFile file : treeFiles) {
                    commitDbDao.insertRealeaseFileTree(file.getName(), file.getContent(), this.project.getName(),
                            release.getNumberStr());
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

    public List<Release> getReleasesFromDb() {
        List<Release> ret = null;
        try {
            ret = commitDbDao.getReleases(this.project.getName());
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
        // Get file tree for each release
        assert ret != null;
        for (Release release : ret) {
            release.setFileTreeAtReleaseEnd(getReleaseFileTreeFromDb(release));
        }
        return ret;
    }

    public List<JavaFile> getReleaseFileTreeFromDb(Release release) {
        List<JavaFile> ret = null;
        try {
            ret = commitDbDao.getReleaseFileTree(this.project.getName(), release.getNumberStr());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void linkCommitsToReleases(List<Commit> commits, List<Release> releases) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        for (int j = 0; j < releases.size(); j++) {
            Release release = releases.get(j);
            for (int i = 0; i < commits.size(); i++) {
                Commit commit = commits.get(i);
                String commitDate = commit.getDate();
                String releaseStartDate = release.getStartDate();
                String releaseEndDate = release.getEndDate();
                // Condizione 1 ASSUNZIONE se la commit è antecedente alla data di inizio della
                // prima release, la inglobo nella prima release
                // Condizione 2 Se la data della commit combacia con la fine dell'ultima commit
                // la assegno all'ultima release perche non posso metterla come prima commit
                // della successiva
                // Condizione 3 Se la data della commit si trova all'interno del range di date
                // di una release o combacia con la data di inizio della release la considero
                // appartenente alla release
                if (((release.getNumberStr().equals("1"))
                        && (sdf.parse(commitDate).before(sdf.parse(releaseStartDate))))
                        || ((release.getNumberStr().equals(Integer.toString(releases.size())))
                                && (sdf.parse(commitDate).compareTo(sdf.parse(releaseEndDate)) == 0))
                        || ((sdf.parse(commitDate).compareTo(sdf.parse(releaseStartDate)) == 0)
                                || ((sdf.parse(commitDate).after(sdf.parse(releaseStartDate)))
                                        && (sdf.parse(commitDate).before(sdf.parse(releaseEndDate)))))) {
                    releases.get(j).addCommit(commit);
                }

            }
        }
    }

    public void removeEmptyReleases(List<Release> releases) {
        List<Release> releaseToRemove = new ArrayList<>();
        for (Release release : releases) {
            if (release.getCommits().isEmpty()) {
                releaseToRemove.add(release);
            }
        }
        for (Release release : releaseToRemove) {
            releases.remove(release);
        }
    }

    public void adjustReleaseIndexes(List<Release> releases) {
        for (int i = 0; i < releases.size(); i++) {
            releases.get(i).setNumber(i + 1);
        }
    }

    public void getProjectDataFromDb(String lastRelease) throws ParseException {
        List<Release> allReleases = getReleasesOfInterestFromDb(); // In realta ora le prende tutte
        setProjectReleases(allReleases);
        List<Commit> commits = getCommitsFromDb();
        setProjectCommits(commits);
        linkCommitsToReleases(commits, allReleases);
        Printer.printProjectInfo(this.project);
        Printer.printReleasesDetailed(allReleases, false);
        removeEmptyReleases(allReleases); // ASSUNZIONE 2
        adjustReleaseIndexes(allReleases); // Se alcune release le ho tolte rimuovo il "buco" lasciato negli indici

        List<Ticket> allTickets = getAllTickets();
        setProjectFixedBugTickets(allTickets);
        linkTicketDatesToReleases(allTickets, allReleases);
        purgeTicketWithNoOvOrFv(allTickets); // ASSUNZIONE 21
        Printer.printTicketsBasic(allTickets, false);

        // Release e ticket di interesse, in teoria forse posso anche non usarli più e
        // prendere AllRelease/2
        List<Release> releasesOfInterest = getReleasesOfInterest(lastRelease);
        setProjectReleasesOfInterest(releasesOfInterest);
        String lastInterestReleaseEndDate = getLastReleaseEndDateOfInterest();
        List<Ticket> ticketOfInterest = getTicketsOfInterest(lastInterestReleaseEndDate);
        setProjectFixedBugTicketsOfInterest(ticketOfInterest);

        Printer.printReleasesDetailed(releasesOfInterest, true);
        Printer.printTicketsBasic(ticketOfInterest, true);
    }

    public void saveProjectDataOnDb() {
        List<Release> allReleases = getAllReleases();
        setProjectReleases(allReleases);
        Printer.printProjectInfo(this.project);

        saveProjectOnDb();
        String lastReleaseDate = allReleases.get(allReleases.size() - 1).getEndDate();
        saveCommitDataOnDb(lastReleaseDate);
        saveReleases();
        LOGGER.info("Saved releases on db");

        List<Commit> commits = getCommitsFromDb();
        setProjectCommits(commits);
        try {
            linkCommitsToReleases(commits, allReleases);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        saveFileTreeOnDb(allReleases);

        List<Ticket> allTickets = getAllTickets();
        setProjectFixedBugTickets(allTickets);
        saveTicketsOnDb(allTickets);
        LOGGER.info("Saved tickets on db");

    }

    public void linkTicketDatesToReleases(List<Ticket> tickets, List<Release> releases) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        for (Ticket ticket : tickets) {
            String ticketCreationDate = ticket.getCreationDate();
            String ticketResDate = ticket.getResolutionDate();
            for (Release release : releases) {
                String releaseNumber = release.getNumberStr();
                String relCreationDate = release.getStartDate();
                String relEndDate = release.getEndDate();
                // ASSUNZIONE 12 condizione 2, ASSUNZIONE 13 condizione 1
                if (((Objects.equals(releaseNumber, "1"))
                        && (sdf.parse(ticketResDate).before(sdf.parse(relCreationDate))))
                        || (sdf.parse(ticketResDate).compareTo(sdf.parse(relCreationDate)) == 0)
                        || ((sdf.parse(ticketResDate).after(sdf.parse(relCreationDate)))
                                && (sdf.parse(ticketResDate).before(sdf.parse(relEndDate))))) {
                    ticket.setFv(release);
                }
                if (((Objects.equals(release.getNumberStr(), "1"))
                        && (sdf.parse(ticketCreationDate).before(sdf.parse(relCreationDate))))
                        || (sdf.parse(ticketCreationDate).compareTo(sdf.parse(relCreationDate)) == 0)
                        || ((sdf.parse(ticketCreationDate).after(sdf.parse(relCreationDate)))
                                && (sdf.parse(ticketCreationDate).before(sdf.parse(relEndDate))))) {
                    ticket.setOv(release);
                }
            }
        }
    }

    public void saveColdStartDataOnDb(String lastReleaseOfInterest) throws ParseException {
        List<Release> allReleases = getAllReleases();
        setProjectReleases(allReleases);
        List<Release> releasesOfInterest = getReleasesOfInterest(lastReleaseOfInterest);
        setProjectReleasesOfInterest(releasesOfInterest);
        String lastInterestReleaseEndDate = getLastReleaseEndDateOfInterest();
        setLastReleaseEndDateOfInterest(lastInterestReleaseEndDate);
        Printer.printProjectInfo(this.project);

        List<Ticket> allTickets = getAllTickets();
        setProjectFixedBugTickets(allTickets);
        List<Ticket> ticketOfInterest = getTicketsOfInterest(lastInterestReleaseEndDate);
        setProjectFixedBugTicketsOfInterest(ticketOfInterest);

        Printer.printProjectInfo(this.project);
        Printer.printReleasesBasic(releasesOfInterest);
        Printer.printTicketsBasic(ticketOfInterest, false);

        saveProjectOnDb();
        saveReleasesOnDb();
        saveTicketsOnDb(ticketOfInterest);

    }

    public void purgeTicketWithNoOvOrFv(List<Ticket> tickets) {
        List<Ticket> ticketToPurge = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if ((ticket.getOv() == null) || (ticket.getFv() == null)) {
                ticketToPurge.add(ticket);
            }
        }
        for (Ticket ticket : ticketToPurge) {
            tickets.remove(ticket);
        }
    }

    public void getColdStartDataFromDb() throws ParseException, SQLException {
        List<Release> releases = getReleasesFromDb();
        String projectName = this.project.getName();
        Printer.printProjectInfo(this.project);
        Printer.printReleasesBasic(releases);

        List<Ticket> allTickets = commitDbDao.getTickets(projectName);

        linkTicketDatesToReleases(allTickets, releases);
        purgeTicketWithNoOvOrFv(allTickets);
        setProjectFixedBugTickets(allTickets);
        setProjectReleases(releases);

        Printer.printTicketsDetailed(allTickets);
    }

}
