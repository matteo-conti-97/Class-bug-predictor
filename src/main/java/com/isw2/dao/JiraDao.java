package com.isw2.dao;

import com.isw2.entity.Release;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class JiraDao {

    private final String projectName;
    private static final String FIXED_BUG_QUERY = "https://issues.apache.org/jira/rest/api/2/search?jql=project='%s'AND'issueType'='Bug'AND('status'='closed'OR'status'='resolved')AND'resolution'='fixed'&fields=key,resolutiondate,versions,created&startAt=%s&maxResults=%s";
    private static final String ALL_RELEASE_QUERY = "https://issues.apache.org/jira/rest/api/2/project/%s/version?maxResults=1000&orderBy=releaseDate&status=released";

    public JiraDao(String projectName) {
        this.projectName = projectName.toUpperCase();
    }

    /*
    Get all releases until the specified release, the releaseName must follow the name convention of jira (e.g. 1.0.0)
    if a non existent name is specified, the method will return all releases, the method is supposed to work only on
    past and terminated releases
     */
    public List<Release> getReleaseUntil(String lastReleaseName){
        List<Release> ret = new ArrayList<>();
        JsonParser jsonParser=new JsonParser();
        JSONObject json = null;
        String query = String.format(ALL_RELEASE_QUERY, this.projectName);
        String lastReleaseEndDate;
        try {
            json = jsonParser.readJsonFromUrl(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray releases = json.getJSONArray("values");
        for(int i=0; i<releases.length();i++){
            JSONObject releaseJson = releases.getJSONObject(i);
            String name = releaseJson.getString("name");
            String startDate=releaseJson.getString("releaseDate");
            Release release=new Release(name,i+1,startDate, "Missing"); //+1 cause it start at 0, missing cause if we get all realeases the last has no end date
            if(i>0){
                lastReleaseEndDate=startDate;
                ret.get(i-1).setEndDate(lastReleaseEndDate);
            }
            ret.add(release);

            if(Objects.equals(name, lastReleaseName)){ //Last release of interest
                break;
            }
        }
        return ret;
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
            String query = String.format(FIXED_BUG_QUERY, this.projectName, start, end);
            //System.out.println(query);
            JSONObject json = null;
            try {
                json = jsonParser.readJsonFromUrl(query);
            } catch (IOException e) {
                e.printStackTrace();
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
