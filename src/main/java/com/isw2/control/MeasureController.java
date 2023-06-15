package com.isw2.control;

import com.isw2.entity.*;
import com.isw2.util.CsvHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MeasureController {
    private final Project project;
    private List<Project> coldStartProportionProjects;

    public MeasureController(Project project) {
        this.project = project;
        this.coldStartProportionProjects = new ArrayList<>();
    }

    public List<Project> getColdStartProportionProjects() {
        return coldStartProportionProjects;
    }

    public void setColdStartProportionProjects(List<Project> coldStartProportionProjects) {
        this.coldStartProportionProjects = coldStartProportionProjects;
    }

    public void addColdStartProportionProject(Project project) {
        this.coldStartProportionProjects.add(project);
    }

    public void createWalkForwardDatasets() {
        List<List<JavaFile>> releaseFiles = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        List<Ticket> tickets= this.project.getFixedBugTicketsOfInterest();
        List<Release> releases = this.project.getReleasesOfInterest();
        for (int i = 0; i < releases.size(); i++) {
            Release release = releases.get(i);
            List<Commit> relCommits = release.getCommits();
            List<JavaFile> relFiles = release.getFileTreeAtReleaseEnd();
            measureInReleaseFeatures(relFiles, relCommits, tickets);
            commits.add(relCommits);
            releaseFiles.add(relFiles);
            measureFromStartFeatures(releaseFiles, i + 1);
            adjustMeasure(releaseFiles);
            measureBuggy(releaseFiles, commits, tickets);
            CsvHandler.writeDataLineByLine(releaseFiles, i + 1);
        }
    }

    private void szz(List<JavaFile> releaseFiles, List<Commit> releaseCommit, List<Ticket> tickets){
        List<JavaFile> processedFiles=new ArrayList<>();
        for(JavaFile file:releaseFiles){
            for(Commit commit:releaseCommit){
                for(JavaFile touchedFile:commit.getTouchedFiles()){
                    if(touchedFile.getName().equals(file.getName())){
                        if(Collections.frequency(processedFiles,file)<1) {
                            processedFiles.add(file);
                            int fixCount=countFixCommit(commit,tickets);
                            if (fixCount > 0) {
                                file.setBuggy("1");
                            } else {
                                file.setBuggy("0");
                            }
                        }
                    }
                }
            }
        }
    }
    //TODO: da implementare
    private void measureBuggy(List<List<JavaFile>> releasesFiles, List<List<Commit>> commits, List<Ticket> tickets){
        int relNum= releasesFiles.size();
        List<JavaFile> lastRelFiles= releasesFiles.get(relNum-1);
        List<Commit> lastRelCommits= commits.get(relNum-1);
        szz(lastRelFiles, lastRelCommits, tickets);


        /*TODO ad ogni giro mi faccio szz sull'ultima release e proportion sui ticket dell'ultima release
        * nelle passate successive in cui faccio solo proportion?
        */
        /*if((relNum>1)&&(relNum<5)){ //SZZ + Cold Start Proportion
            return;//TODO
        }
        else if(relNum>=5){ //SZZ + Incremental Proportion
            return;//TODO
        }*/
    }

    private void adjustMeasure(List<List<JavaFile>> releaseFiles){
        for(List<JavaFile> files : releaseFiles){
            for(JavaFile file : files) {
                String nRevFromStart = file.getnRevFromStart();
                String nRevInRelease = file.getnRevInRelease();
                String locAtEndRelease = file.getLocAtEndRelease();
                String avgLocAddedInRelease = file.getAvgLocAddedInRelease();
                if ((Objects.equals(nRevFromStart, "1")) && (Objects.equals(nRevInRelease, "1")) && (!Objects.equals(locAtEndRelease, avgLocAddedInRelease))) {
                    file.setAvgLocAddedInRelease(locAtEndRelease);
                    file.setAvgChurnInRelease(locAtEndRelease);
                    file.setAvgChurnFromStart(locAtEndRelease);
                    file.setAvgLocAddedFromStart(locAtEndRelease);
                }
            }
        }
    }

    private void measureInReleaseFeatures(List<JavaFile> releaseFiles, List<Commit> commits, List<Ticket> tickets) {
        for (JavaFile srcFile : releaseFiles) {
            int cnt = 0;
            int adds = 0;
            int dels = 0;
            int filteredAdds;
            int filteredDels;
            int realAdds = 0;
            int realDels = 0;
            int fixCount = 0;
            List<String> authors = new ArrayList<>();

            String[] lines = srcFile.getContent().split("\r\n|\r|\n");
            int nLines = lines.length;
            int filteredLines = (int) Math.floor((double) (nLines * 10) / 100);

            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        String author = commit.getAuthor();
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                        fixCount+=countFixCommit(commit,tickets);
                        cnt++;
                        adds = Integer.parseInt(file.getAdd());
                        filteredAdds = (int) Math.floor((double) (adds * 10) / 100); //ASSUNZIONE 9-10
                        realAdds += adds - filteredAdds;
                        dels = Integer.parseInt(file.getDel());
                        filteredDels = (int) Math.floor((double) (dels * 10) / 100); //ASSUNZIONE 9-10
                        realDels += dels - filteredDels;

                    }
                }
            }

            int nAuthors = authors.size();       //Per ogni commit nella release che ha toccato il file si guarda l'autore e si aggiunge ad una lista senza duplicati
            srcFile.setnAuthorInRelease(Integer.toString(nAuthors));
            int loc = nLines - filteredLines;     //Per ogni commit nella release che ha toccato il file si guarda loc e si fa la media
            srcFile.setLocAtEndRelease(Integer.toString(loc));
            srcFile.setnRevInRelease(Integer.toString(cnt));        //Per ogni file si contano le commit nella release che lo hanno toccato
            assert cnt != 0;
            int avgAdds = (int) Math.floor((double) realAdds / cnt);     //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
            srcFile.setAvgLocAddedInRelease(Integer.toString(avgAdds));
            int churn = (int) Math.floor((double) (realAdds - realDels) / cnt);      //Per ogni commit nella release che ha toccato il file si fa locAdded - locDeleted e si fa la media
            srcFile.setAvgChurnInRelease(Integer.toString(churn));
            srcFile.setRelDels(Integer.toString(realDels)); //Serve per le misure from start
            srcFile.setRelAdds(Integer.toString(realAdds)); //Serve per le misure from start
            srcFile.setnFixCommitInRelease(Integer.toString(fixCount));     //Per ogni commit nella release che ha toccato il file si conta quante sono afferenti ad un bugFix

        }
    }

    private void measureFromStartFeatures(List<List<JavaFile>> releaseFiles, int numReleases) {
        //I valori dalla release corrente (cioè l'ultima nella lista attuale) me li calcolo come somma/media dei singoli valori della release fino alla corrente
        List<JavaFile> lastRelFiles = releaseFiles.get(numReleases - 1);
        for (JavaFile file : lastRelFiles) {
            int totBugFix = Integer.parseInt(file.getnFixCommitInRelease());
            int totCnt = Integer.parseInt(file.getnRevInRelease());
            int totAdds = Integer.parseInt(file.getRelAdds());
            int totDels = Integer.parseInt(file.getRelDels());
            for (int i = numReleases - 2; i >= 0; i--) {
                List<JavaFile> relFiles = releaseFiles.get(i);
                for (JavaFile relFile : relFiles) {
                    if (relFile.getName().equals(file.getName())) {
                        totBugFix += Integer.parseInt(relFile.getnFixCommitInRelease());
                        totCnt += Integer.parseInt(relFile.getnRevInRelease());
                        totAdds += Integer.parseInt(relFile.getRelAdds()); //Nota qui non faccio ASSUNZIONE 9-10 perchè questi valori li setto in measureInReleaseFeatures e sono gia 'corretti'
                        totDels += Integer.parseInt(relFile.getRelDels());
                    }
                }
            }
            file.setnRevFromStart(Integer.toString(totCnt));        //Per ogni file si contano le commit nella release che lo hanno toccato
            int avgLocAdded = (int) Math.floor((double) totAdds / totCnt);       //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
            file.setAvgLocAddedFromStart(Integer.toString(avgLocAdded));
            int avgChurn = (int) Math.floor((double) (totAdds - totDels) / totCnt);     //Per ogni commit che ha toccato il file si fa locAdded - locDeleted e si fa la media
            file.setAvgChurnFromStart(Integer.toString(avgChurn));
            file.setnFixCommitFromStart(Integer.toString(totBugFix));     //Per ogni commit che ha toccato il file si conta quante sono afferenti ad un bugFix
        }
    }

    private int countFixCommit(Commit commit,List<Ticket> tickets){
        int ret=0;
        for(Ticket ticket: tickets){
            if(commit.getMessage().startsWith(ticket.getKey())){
                ret++;
            }
        }
        return ret;
    }

    private double computeProjectColdStartProportion(Project project){
        System.out.println("Computing proportion for "+project.getName());
        double projPropSum=0;
        double projPropCnt=0;

        for(Ticket ticket: project.getFixedBugTickets()){
            if(!ticket.getJiraAv().isEmpty()){
                int fv=ticket.getFv().getNumber();
                int ov=ticket.getOv().getNumber();
                List<Integer> avs=new ArrayList<>();
                for(Release rel: ticket.getJiraAv()){
                    avs.add(rel.getNumber());
                }
                int iv = Collections.min(avs);
                if((fv==ov)||(fv==iv)||(fv<ov)||(fv<iv)||(ov<iv)){ //ASSUNZIONE 14
                    continue;
                }
                projPropCnt++;
                double prop = (double) (fv - iv) /(fv-ov);
                projPropSum+= prop;
            }
        }
        assert projPropCnt!=0;
        return projPropSum/projPropCnt;
    }

    public double computeColdStartProportion(){
        double allPropSum=0;
        int allPropCnt=this.coldStartProportionProjects.size();
        for(Project proj: this.coldStartProportionProjects){
            double projProp=computeProjectColdStartProportion(proj);
            allPropSum+=projProp;
        }

        return allPropSum/allPropCnt;
    }

}
