package com.isw2.dao;

import com.isw2.entity.Commit;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GitDao {
    private String projectName;
    private String author;
    private String repoUrl;

    public GitDao(String projectName, String author) {
        this.projectName = projectName.toLowerCase();
        this.author = author.toLowerCase();
        this.repoUrl = "https://api.github.com/repos/" + author + "/" + projectName;
    }

    public String getProjectCreationDate() {
        JsonParser jsonParser = new JsonParser();
        JSONObject projectJson = null;
        try {
            projectJson = jsonParser.readJsonFromUrl(repoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonParser.getJSONAttribute(projectJson, "created_at").substring(0, 10);
    }

    public List<Commit> getAllCommits() {
        JsonParser jsonParser = new JsonParser();
        List<Commit> commits = new ArrayList<>();
        JSONArray commitListJson = null;
        try {
            commitListJson = jsonParser.readJsonArrayFromUrl(repoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < Objects.requireNonNull(commitListJson).length(); i++) {
            String commitHash = jsonParser.getJSONAttribute(commitListJson.getJSONObject(i), "sha");
            //JSONObject commitJson=jsonParser.getJSONAttribute(commitListJson.getJSONObject(i),"commit");
        }
        System.out.println(commitListJson);
        return commits;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
