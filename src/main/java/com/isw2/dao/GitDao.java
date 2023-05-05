package com.isw2.dao;

import com.isw2.util.AuthJsonParser;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

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
        JsonParser jsonParser = new AuthJsonParser();
        JSONObject projectJson = null;
        try {
            projectJson = jsonParser.readJsonFromUrl(repoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonParser.getJSONAttribute(projectJson, "created_at").substring(0, 10);
    }

    private JSONObject getCommitJson(String commitUrl) {
        JsonParser jsonParser = new AuthJsonParser();
        JSONObject ret = null;
        try {
            ret = jsonParser.readJsonFromUrl(commitUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    //Get all commits until a given date, in the format YYYY-MM-DD for our purpose the date is the end date of the interest release
    public JSONArray getAllCommitsJsonUntil(String relEndDate) throws IOException {
        JsonParser jsonParser = new AuthJsonParser();
        JSONArray ret = new JSONArray();
        int page = 1;
        while (true) {
            JSONArray tmp;
            System.out.println("page: " + page);
            String query = repoUrl + "/commits?until=" + relEndDate + "&per_page=100&page=" + page;
            System.out.println(query);
            tmp = jsonParser.readJsonArrayFromUrl(query);
            if (tmp.length() == 0) break;
            for (int i = 0; i < tmp.length(); i++) {
                ret.put(getCommitJson(tmp.getJSONObject(i).getString("url")));
            }
            page++;
        }
        return ret;
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
