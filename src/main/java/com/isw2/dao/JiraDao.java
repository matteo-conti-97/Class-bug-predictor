package com.isw2.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JiraDao {

    private final String projectName;
    private final String fixed_bug_query="https://issues.apache.org/jira/rest/api/2/search?jql=project='%s'AND'issueType'='Bug'AND('status'='closed'OR'status'='resolved')AND'resolution'='fixed'&fields=key,resolutiondate,versions,created&startAt=%s&maxResults=%s";
    private final String all_release_query="https://issues.apache.org/jira/rest/api/2/project/%s/version?orderBy=releaseDate&status=released";

    public JiraDao(String projectName){
        this.projectName=projectName;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    //In jira the affected version is the field "name" of the version
    public List<String> getFixedBugTickets(int start)  {
        ArrayList<String> ticketList = new ArrayList<>();
        int end = 0;
        int total = 1;
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            end = start + 1000;
            String query = String.format(fixed_bug_query, this.projectName, Integer.toString(start), Integer.toString(end));
            System.out.println(query);
            JSONObject json = null;
            try {
                json = readJsonFromUrl(query);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; start < total && start < end; start++) {
                //Iterate through each bug
                String key = issues.getJSONObject(start % 1000).get("key").toString();
                ticketList.add(key);
                System.out.println(key);
            }
        } while (start < total);

        return ticketList;
    }

}
