package com.isw2.model;

import java.util.List;

public class Commit {
    private String id;
    private String sha;
    private String message;
    private String date;
    private String projectName;
    private String commitUrl;
    private String treeUrl;
    private String ticketId;
    private String author;
    private List<JavaFile> touchedFiles;

    // Used for db creation
    public Commit(String id, String sha, String message, String date, String author, String treeUrl,
            List<JavaFile> touchedFiles) {
        this.id = id;
        this.sha = sha;
        this.message = message;
        this.date = date;
        this.author = author;
        this.touchedFiles = touchedFiles;
        this.treeUrl = treeUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public List<JavaFile> getTouchedFiles() {
        return touchedFiles;
    }

    public void setTouchedFiles(List<JavaFile> touchedFiles) {
        this.touchedFiles = touchedFiles;
    }

    public String getTreeUrl() {
        return treeUrl;
    }

    public void setTreeUrl(String treeUrl) {
        this.treeUrl = treeUrl;
    }
}
