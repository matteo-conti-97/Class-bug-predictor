package com.isw2.util;

import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.util.List;

public class Printer {

    public static void printTickets(List<Ticket> tickets){
        for (Ticket tmp : tickets) {
            String stringa=tmp.getKey() + " Resolution Date: " + tmp.getResolutionDate()+ " Fix Version: " + tmp.getFv().getName() +" Num: " +tmp.getFv().getNumber()+ " Creation Date: " + tmp.getCreationDate() + " Opening Version:" + tmp.getOv().getName() +" Num: " +tmp.getOv().getNumber();
            System.out.println(stringa);
            System.out.println("\tAffected Versions: ");
            for(Release av: tmp.getJiraAv()){
                System.out.println("\t\tVersion: "+av.getName());
            }
        }
    }
}
