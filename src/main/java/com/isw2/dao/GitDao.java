package com.isw2.dao;

import com.isw2.model.Commit;
import com.isw2.model.JavaFile;
import com.isw2.util.AuthJsonParser;
import com.isw2.util.CodeParser;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitDao {
    private String projectName;
    private String author;
    private String repoUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(GitDao.class);

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
            return null;
        }
        assert projectJson != null;
        return jsonParser.getJSONAttribute(projectJson, "created_at").substring(0, 10);
    }

    private Commit getCommit(String commitUrl, long commitId) {
        JsonParser jsonParser = new AuthJsonParser();
        Commit ret = null;
        try {
            JSONObject commitJson = jsonParser.readJsonFromUrl(commitUrl);
            String sha = commitJson.getString("sha");
            JSONObject commit = commitJson.getJSONObject("commit");
            String message = commit.getString("message");
            JSONObject authorJson = commit.getJSONObject("author");
            String date = authorJson.getString("date").substring(0, 10);
            String authorName = authorJson.getString("name");
            JSONArray touchedFilesJson = commitJson.getJSONArray("files");
            String treeUrl = commitJson.getJSONObject("commit").getJSONObject("tree").getString("url");
            List<JavaFile> touchedFiles = new ArrayList<>();
            for (int i = 0; i < touchedFilesJson.length(); i++) {
                JSONObject fileJson = touchedFilesJson.getJSONObject(i);
                String tmpName = fileJson.getString("filename");
                String filename = tmpName.substring(tmpName.lastIndexOf("/") + 1);
                if (filename.endsWith(".java")) {
                    String add = Integer.toString(fileJson.getInt("additions"));
                    String del = Integer.toString(fileJson.getInt("deletions"));
                    String status = fileJson.getString("status");
                    String prevName = "";
                    if (status.equals("renamed")) {
                        prevName = fileJson.getString("previous_filename");
                    }
                    String content = "";
                    JavaFile touchedFile = new JavaFile(filename, add, del, content, status, prevName);
                    touchedFiles.add(touchedFile);
                }
            }
            ret = new Commit(Long.toString(commitId), sha, message, date, authorName, treeUrl, touchedFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    // Get all commits until a given date, in the format YYYY-MM-DD for our purpose
    // the date is the end date of the interest release
    public List<Commit> getAllCommitsUntil(String relEndDate) {
        JsonParser jsonParser = new AuthJsonParser();
        List<Commit> ret = new ArrayList<>();
        int page = 0; // *****Mettere un numero di pagina più alto se si finiscono gli accessi
                      // dell'api*****
        long commitId = 0;
        while (true) {
            JSONArray tmp;
            String query = repoUrl + "/commits?until=" + relEndDate + "&per_page=100&page=" + page;
            LOGGER.info(query);
            try {
                tmp = jsonParser.readJsonArrayFromUrl(query);
                if (tmp.length() == 0)
                    break;
                for (int i = 0; i < tmp.length(); i++) {
                    String commitUrl = tmp.getJSONObject(i).getString("url");
                    ret.add(getCommit(commitUrl, commitId));
                    commitId++;
                }
                page++;

            } catch (IOException e) {
                e.printStackTrace();
                return ret;
            }
        }
        return ret;
    }

    // Prende una copia di tutti i file al termine della release
    public List<JavaFile> getRepoFileAtReleaseEnd(String treeUrl) throws IOException {
        List<JavaFile> ret = new ArrayList<>();
        AuthJsonParser jsonParser = new AuthJsonParser();
        JSONObject treeJson = null;
        treeJson = jsonParser.readJsonFromUrl(treeUrl + "?recursive=1");
        assert treeJson != null;
        JSONArray tree = treeJson.getJSONArray("tree");
        for (int i = 0; i < tree.length(); i++) {
            JSONObject treeElem = tree.getJSONObject(i);
            String filename = treeElem.getString("path");

            if ((filename.endsWith(".java")) && (!filename.contains("package-info")) && (!filename.contains("test"))
                    && (!filename.contains("Test"))) {
                String className = filename.substring(filename.lastIndexOf("/") + 1);
                String fileUrl = treeElem.getString("url");
                JSONObject fileJson = null;
                fileJson = jsonParser.readJsonFromUrl(fileUrl);
                assert fileJson != null;
                String fileContent = CodeParser.base64Decode(fileJson.getString("content"));
                ret.add(new JavaFile(className, fileContent));
            }
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
