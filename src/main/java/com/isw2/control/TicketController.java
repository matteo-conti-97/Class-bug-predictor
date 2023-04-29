package com.isw2.control;

import com.isw2.dao.JiraDao;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TicketController {
    private JiraDao jiraDao;

    public TicketController(String projectName) {
        this.jiraDao = new JiraDao(projectName);
    }

    public JiraDao getJiraDao() {
        return jiraDao;
    }

    public void setJiraDao(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    //Get all tickets closed until the specified release so with the resolution date in the date range of the release
    public List<Ticket> getAllTicketsUntilRelEndDate(String relEndDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Ticket> ret = new ArrayList<>();
        List<Ticket> tickets = jiraDao.getFixedBugTickets(0);
        for (Ticket ticket : tickets) {
            String ticketResDate = ticket.getResolutionDate();
            if (sdf.parse(ticketResDate).before(sdf.parse(relEndDate))) {
                ret.add(ticket);
            }
        }
        return ret;
    }
}
