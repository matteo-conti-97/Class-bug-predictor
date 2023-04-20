package com.isw2.dao;

import com.isw2.entity.Commit;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitDao {
    private String projectName;
    private String repoUrl;

    public GitDao(String projectName, String author) {
        this.projectName = projectName;
        this.repoUrl = "https://api.github.com/repos/"+author.toLowerCase()+"/"+projectName.toLowerCase()+"/commits";
    }

    public List<Commit> getAllCommits(){
        JsonParser jsonParser=new JsonParser();
        List<Commit> commits=new ArrayList<>();
        JSONArray commitListJson=null;
        try {
            commitListJson=jsonParser.readJsonArrayFromUrl(repoUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(int i=0; i<commitListJson.length(); i++){
            String commitHash=jsonParser.getJSONAttribute(commitListJson.getJSONObject(i),"sha");
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
}
