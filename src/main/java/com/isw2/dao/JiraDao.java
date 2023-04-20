package com.isw2.dao;

import com.isw2.entity.Ticket;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class JiraDao {

    private final String projectName;
    private final String fixed_bug_query = "https://issues.apache.org/jira/rest/api/2/search?jql=project='%s'AND'issueType'='Bug'AND('status'='closed'OR'status'='resolved')AND'resolution'='fixed'&fields=key,resolutiondate,versions,created&startAt=%s&maxResults=%s";
    private final String all_release_query = "https://issues.apache.org/jira/rest/api/2/project/%s/version?orderBy=releaseDate&status=released";

    public JiraDao(String projectName) {
        this.projectName = projectName;
    }


    //In jira the affected version is the field "name" of the version
    public List<String> getFixedBugTickets(int start) {
        JsonParser jsonParser=new JsonParser();
        ArrayList<String> ticketList = new ArrayList<>();
        int end = 0;
        int total = 1;
        int max = 1000;
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            end = start + max;
            String query = String.format(fixed_bug_query, this.projectName, start, end);
            //System.out.println(query);
            JSONObject json = null;
            try {
                json = jsonParser.readJsonFromUrl(query);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            total = json.getInt("total");
            JSONArray issues = json.getJSONArray("issues");
            for (; start < total && start < end; start++) {
                //Iterate through each bug
                String key = issues.getJSONObject(start%1000).get("key").toString();
                ticketList.add(key);
                //ticketList.add(String.valueOf(new Ticket()));
                System.out.println(key);
            }
        } while (start < total);

        return ticketList;
    }

}
