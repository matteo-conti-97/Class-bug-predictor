package com.isw2.control;

import com.isw2.dao.JiraDao;
import com.isw2.entity.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketController {
    private JiraDao jiraDao;

    public TicketController(JiraDao jiraDao){
        this.jiraDao = jiraDao;
    }

    public JiraDao getJiraDao() {
        return jiraDao;
    }

    public void setJiraDao(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    //Get all tickets closed until the specified release TODO finire
    public List<Ticket> getAllTicketsUntilRelease(){
        List<Ticket> ret = new ArrayList<>();
        List<Ticket> tickets = jiraDao.getFixedBugTickets(0);
        return ret;
    }
}
