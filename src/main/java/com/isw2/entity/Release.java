package com.isw2.entity;

import java.util.List;

public class Release {
    private String name;
    private int number;
    private String creationDate;
    private List<Commit> commits;
    private List<Ticket> tickets;
}
