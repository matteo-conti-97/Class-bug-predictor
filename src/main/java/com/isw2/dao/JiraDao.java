package com.isw2.dao;

import com.isw2.entity.Release;
import com.isw2.entity.Ticket;
import com.isw2.util.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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
    if an empty or non-existent name is specified, the method will return all releases
    */
    public List<Release> getReleases() {
        List<Release> ret = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        JSONObject jsonReleases = null;
        String query = String.format(ALL_RELEASE_QUERY, this.projectName);
        String lastReleaseEndDate;
        try {
            jsonReleases = jsonParser.readJsonFromUrl(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert jsonReleases != null;
        JSONArray releases = jsonReleases.getJSONArray("values");
        for (int i = 0; i < releases.length(); i++) {
            JSONObject releaseJson = releases.getJSONObject(i);
            String name = releaseJson.getString("name");
            String startDate = releaseJson.getString("releaseDate");
            Release release = new Release(name, i + 1, startDate, "Missing"); //+1 cause it start at 0, use the next release start date as end date of the current release, except for the last release which have missing
            if (i > 0) {
                lastReleaseEndDate = startDate;
                ret.get(i - 1).setEndDate(lastReleaseEndDate);
            }
            ret.add(release);
        }
        return ret;
    }


    //Retrieve specified ticket data from jira
    public Ticket getTicket(String key, String ticketId, String ticketUrl) {
        Ticket ret = new Ticket(key, ticketId, ticketUrl);
        JsonParser jsonParser = new JsonParser();
        JSONObject jsonTicket = null;
        try {
            jsonTicket = jsonParser.readJsonFromUrl(ticketUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert jsonTicket != null;
        JSONObject ticketFields = jsonTicket.getJSONObject("fields");
        String type = ticketFields.getJSONObject("issuetype").getString("name");
        String priority = ticketFields.getJSONObject("priority").getString("name");
        String status = ticketFields.getJSONObject("status").getString("name");
        String creator = ticketFields.getJSONObject("creator").getString("displayName");
        String creationDate = ticketFields.getString("created").substring(0, 10);
        String resolutionDate = ticketFields.getString("resolutiondate").substring(0, 10);
        ret.setType(type);
        ret.setPriority(priority);
        ret.setStatus(status);
        ret.setCreator(creator);
        ret.setCreationDate(creationDate);
        ret.setResolutionDate(resolutionDate);
        return ret;
    }

    //In jira the affected version is the field "name" of the version
    public List<Ticket> getFixedBugTickets(int start) {
        JsonParser jsonParser = new JsonParser();
        ArrayList<Ticket> ret = new ArrayList<>();
        int end = 0;
        int total = 1;
        int max = 1000;
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            end = start + max;
            String query = String.format(FIXED_BUG_QUERY, this.projectName, start, end);
            //System.out.println(query);
            JSONObject jsonTickets = null;
            try {
                jsonTickets = jsonParser.readJsonFromUrl(query);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert jsonTickets != null;
            total = jsonTickets.getInt("total");
            JSONArray issues = jsonTickets.getJSONArray("issues");
            for (; start < total && start < end; start++) {  //Iterate through each bug
                JSONObject ticketMetadata = issues.getJSONObject(start % 1000);
                String ticketId = ticketMetadata.get("id").toString();
                String key = ticketMetadata.get("key").toString();
                String ticketUrl = ticketMetadata.get("self").toString();
                ret.add(getTicket(key, ticketId, ticketUrl));
            }
        } while (start < total);

        return ret;
    }

}
