package com.isw2.control;

import com.isw2.model.*;
import com.isw2.util.CsvHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MeasureController {
    private final Project project;
    private List<Project> coldStartProportionProjects;
    private double coldStartProportion;
    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureController.class);

    public MeasureController(Project project) {
        this.project = project;
        this.coldStartProportionProjects = new ArrayList<>();
    }

    public double getColdStartProportion() {
        return coldStartProportion;
    }

    public void setColdStartProportion(double coldStartProportion) {
        this.coldStartProportion = coldStartProportion;
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
        String projectName=this.project.getName();
        List<List<JavaFile>> releaseFiles = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        List<Ticket> tickets= this.project.getFixedBugTickets();
        List<Release> releases = this.project.getReleases();
        int releasesOfInterestSize = this.project.getReleasesOfInterest().size();
        //Qui mi genero in modo walkforward tutti i training set
        for (int i = 0; i < releases.size(); i++) {
            Release release = releases.get(i);
            int lastRelNum=release.getNumber();
            List<Commit> relCommits = release.getCommits();
            List<JavaFile> relFiles = release.getFileTreeAtReleaseEnd();
            measureInReleaseFeatures(relFiles, relCommits, tickets);
            commits.add(relCommits);
            releaseFiles.add(relFiles);
            measureFromStartFeatures(releaseFiles, i + 1);
            adjustMeasure(releaseFiles);
            List<Ticket> currRelFixTicket=getRelTickets(tickets,lastRelNum);
            List<Ticket> prevTickets=filterProportionTickets(tickets, lastRelNum);
            double proportion=computeIncrementalProportion(prevTickets, lastRelNum);
            computeTicketsIv(currRelFixTicket, proportion);
            measureBuggy(releaseFiles, commits, currRelFixTicket);
            if(i<releasesOfInterestSize) CsvHandler.createTrainingSet(releaseFiles, i + 1, projectName);
        }

        //Qui mi genero in modo walkforward tutti i testing set prendendo le informazioni dell'ultima release disponibile
        for(int i = 0; i < releasesOfInterestSize; i++){ //ASSUNZIONE 23
            CsvHandler.createTestingSet(releaseFiles.get(i), i + 1, projectName);
            CsvHandler.convertDataset(i + 1, projectName);
        }
    }

    public void affectPreviousVersion(JavaFile file, List<List<JavaFile>> releaseFiles, List<Ticket> tickets){
        for(Ticket ticket: tickets){
            int iv=ticket.getIv();
            setBuggy(file, releaseFiles, iv-1, releaseFiles.size()-1); //Setto buggy a 1 il file nelle release successive all'IV
            //setBuggy(file,releaseFiles,0,iv-1,"0"); //Setto buggy a 0 il file nelle release precedenti all'IV, iv-2
        }
    }

    private void setBuggy(JavaFile file, List<List<JavaFile>> releaseFiles, int start, int end){
        for(int i=start;i<end;i++){
            List<JavaFile> release=releaseFiles.get(i);
            for(JavaFile releaseFile:release){
                if(releaseFile.getName().equals(file.getName())){
                    releaseFile.setBuggy("1");
                }
            }
        }
    }

    private void computeFileBuggyness(JavaFile file, List<Commit> releaseCommit, List<Ticket> tickets, List<List<JavaFile>> releaseFiles){
        List<JavaFile> processedFiles=new ArrayList<>();
        for(Commit commit: releaseCommit){
            for(JavaFile touchedFile: commit.getTouchedFiles()){
                if((touchedFile.getName().equals(file.getName()))&&(Collections.frequency(processedFiles,file)<1) ){
                    processedFiles.add(file);
                    List<Ticket> commitLinkedTickets= getCommitFixTickets(commit,tickets);
                    file.setBuggy("0"); //Nella current release o non è buggy o è stato fixato
                    if (!commitLinkedTickets.isEmpty()) affectPreviousVersion(file, releaseFiles, commitLinkedTickets);
                }
            }
        }
    }

    private List<Integer> convertReleaseToNumber(List<Release> releases){
        List<Integer> avsNum=new ArrayList<>();
        for(Release av:releases){
            avsNum.add(av.getNumber()+1); //Perche quando popolo le AV le ho messe che partono da 0
        }
        return avsNum;
    }

    private List<Ticket> getRelTickets(List<Ticket> tickets, int relNum){
        List<Ticket> ret = new ArrayList<>();
        for(Ticket ticket:tickets){
            if(ticket.getFv().getNumber()==relNum){
                ret.add(ticket);
            }
        }
        return ret;
    }

    private void computeTicketsIv(List<Ticket> tickets, double proportion) {
        for(Ticket ticket: tickets){
            int fv=ticket.getFv().getNumber();
            int ov=ticket.getOv().getNumber();
                if(!ticket.getJiraAv().isEmpty()){
                    List<Integer> avsNum=convertReleaseToNumber(ticket.getJiraAv());
                    int iv= Collections.min(avsNum); //Perche la prima release deve essere 1 ma quando ho popolato ho omesso il +1
                    if(fv >= iv && ov >= iv) { //ASSUNZIONE 14
                        ticket.setIv(iv);
                        adjustIv(ticket); //Se iv è 0 lo setto a 1 perche le release le numero a partire da 1
                    }
                }
                else{
                    int iv;
                    if(fv<=ov) iv=(int) (fv-proportion);  //ASSUNZIONE 15
                    else  iv=(int) (fv-(proportion*(fv-ov))); //ASSUNZIONE 19
                    ticket.setIv(iv);
                    adjustIv(ticket); //Se iv è 0 lo setto a 1 perche le release le numero a partire da 1
                }
        }
    }

    private void adjustIv(Ticket ticket){
            if(ticket.getIv()<=0){
                ticket.setIv(1);
            }
    }


    private void measureBuggy(List<List<JavaFile>> releasesFiles, List<List<Commit>> commits, List<Ticket> tickets){
        int lastRelNum= releasesFiles.size();
        List<JavaFile> lastRelFiles= releasesFiles.get(lastRelNum-1);
        List<Commit> lastRelCommits= commits.get(lastRelNum-1);
        //SZZ per tutti i file della nuova release, le altre le ho gia processate in iterazioni precedenti
        for(JavaFile file:lastRelFiles){
            computeFileBuggyness(file,lastRelCommits, tickets, releasesFiles);
        }
    }

    private List<Ticket> filterProportionTickets(List<Ticket> tickets, int currRelNum){
        List<Ticket> ret=new ArrayList<>();
        for(Ticket ticket:tickets){
            if((ticket.getFv().getNumber()<currRelNum)&&(ticket.getFv().getNumber()!=1)){
                ret.add(ticket);
            }
        }
        return ret;
    }

    private double computeIncrementalProportion(List<Ticket> tickets , int currRelNum){
        int tot=0;
        int propSum=0;
        for(Ticket ticket: tickets){
            double prop;
            int fv=ticket.getFv().getNumber();
            int ov=ticket.getOv().getNumber();
            int iv=ticket.getIv();
            if((fv>ov)&&(fv>iv)&&(ov>=iv)){
                prop=((double)(fv - iv) /(fv-ov)); //ASSUNZIONE 14
                propSum+=prop;
                tot++; //Essenzialmente tot sono i ticket validi
            }
        }
        if((tickets.size()<5)||(currRelNum<3)||(tot<5)){
            LOGGER.info("Per release {} ho usato cold start proportion è {}", currRelNum, this.coldStartProportion);
            return this.coldStartProportion;//ASSUNZIONE 16/18
        }
        double ret=(double) propSum /tot;
        LOGGER.info("Incremental Proportion per release {} è {} propSum {} e tot {}", currRelNum, ret, propSum, tot);
        return ret;
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
                        fixCount+= getCommitFixTickets(commit,tickets).size();
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

    private List<Ticket> getCommitFixTickets(Commit commit, List<Ticket> tickets){
        List<Ticket> ret=new ArrayList<>();
        for(Ticket ticket: tickets){
            if(commit.getMessage().startsWith(ticket.getKey())){
                ret.add(ticket);
            }
        }
        return ret;
    }

    private double computeProjectColdStartProportion(Project project) {
        String projectName = project.getName();
        LOGGER.info("Computing proportion for {}", projectName);
        double projPropSum = 0;
        double projPropCnt = 0;

        for (Ticket ticket : project.getFixedBugTickets()) {
            if (!ticket.getJiraAv().isEmpty()) {
                int fv = ticket.getFv().getNumber();
                int ov = ticket.getOv().getNumber();
                List<Integer> avs = convertReleaseToNumber(ticket.getJiraAv());
                int iv = Collections.min(avs); //Perche la prima release deve essere 1 ma quando ho popolato ho omesso il +1
                if ((fv <= ov) || (fv <= iv) || (ov < iv)) { //ASSUNZIONE 14
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
