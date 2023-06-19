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
    private double coldStartProportion;

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
            CsvHandler.convertDataset(i+1);
        }
    }

    /*TODO risolvere bug, ok che setto buggy a 1 le release affected in base a proportion ma devo anche settare
    *  buggy a 0 per le release che prima erano buggy ma ora non sono piu tra le AV*/
    public void affectPreviousVersion(JavaFile file, List<List<JavaFile>> releaseFiles, List<Ticket> tickets, Commit commit){
        int iv=-1;
        for(Ticket ticket: tickets){
            if(commit.getMessage().startsWith(ticket.getKey())){
               iv=ticket.getIv();
            }
        }
        if(iv==-1) return;
        setBuggy(file, releaseFiles, iv-1, releaseFiles.size()-1, "1"); //Setto buggy a 1 il file nelle release successive all'IV
        setBuggy(file,releaseFiles,0,iv-1,"0"); //Setto buggy a 0 il file nelle release precedenti all'IV
    }

    private void setBuggy(JavaFile file, List<List<JavaFile>> releaseFiles, int start, int end, String value){
        for(int i=start;i<end;i++){
            List<JavaFile> release=releaseFiles.get(i);
            for(JavaFile releaseFile:release){
                if(releaseFile.getName().equals(file.getName())){
                    releaseFile.setBuggy(value);
                }
            }
        }
    }

    private void computeFileBuggyness(JavaFile file, List<Commit> releaseCommit, List<Ticket> tickets, List<List<JavaFile>> releaseFiles, double proportion){
        List<JavaFile> processedFiles=new ArrayList<>();
        computeTicketsIv(tickets, proportion); //Calcolo le IV per i ticket
        for(Commit commit:releaseCommit){
            for(JavaFile touchedFile:commit.getTouchedFiles()){
                if((touchedFile.getName().equals(file.getName()))&&(Collections.frequency(processedFiles,file)<1) ){
                    processedFiles.add(file);
                    int fixCount=countFixCommit(commit,tickets);
                    if (fixCount > 0) {
                        file.setBuggy("1");
                        affectPreviousVersion(file, releaseFiles, tickets, commit); //Setto buggy quelle release il cui ticket ha iv vecchie
                    } else {
                        file.setBuggy("0");
                    }

                }
            }
        }
    }

    private List<Integer> convertReleaseToNumber(List<Release> releases){
        List<Integer> avsNum=new ArrayList<>();
        for(Release av:releases){
            avsNum.add(av.getNumber());
        }
        return avsNum;
    }


    private void computeTicketsIv(List<Ticket> tickets, double proportion) {
        for(Ticket ticket:tickets){
            int fv=ticket.getFv().getNumber();
            int ov=ticket.getOv().getNumber();
            if(fv<=ov){ //ASSUNZIONE 15
                ticket.setIv(fv);
                adjustIv(ticket);
            }else{
                if(!ticket.getJiraAv().isEmpty()){
                    List<Integer> avsNum=convertReleaseToNumber(ticket.getJiraAv());
                    int iv= Collections.min(avsNum)+1; //Perche la prima release deve essere 1 ma quando ho popolato ho omesso il +1
                    if(fv >= iv && ov >= iv) { //ASSUNZIONE 14
                        ticket.setIv(iv);
                        adjustIv(ticket);
                    }
                }
                else{
                    int iv=(int) (fv-(proportion*(fv-ov)));
                    ticket.setIv(iv);
                    adjustIv(ticket);
                    //System.out.println("Ticket aveva jira AV vuote " + ticket.getKey() + " ha IV " + ticket.getIv() + " perche fv è " + ticket.getFv().getName()+" num "+fv + " e ov è " + ticket.getOv().getName()+" num "+ ov + " con proportion "+proportion);
                }
            }
        }
    }

    private void adjustIv(Ticket ticket){
            if(ticket.getIv()==0){
                ticket.setIv(1);
            }
    }


    private void measureBuggy(List<List<JavaFile>> releasesFiles, List<List<Commit>> commits, List<Ticket> tickets){
        int lastRelNum= releasesFiles.size();
        List<JavaFile> lastRelFiles= releasesFiles.get(lastRelNum-1);
        List<Commit> lastRelCommits= commits.get(lastRelNum-1);
        //SZZ per tutti i file della nuova release, le altre le ho gia processate in iterazioni precedenti
        for(JavaFile file:lastRelFiles){//ASSUNZIONE 16
            if((lastRelNum>1)&&(lastRelNum<5)){ //Cold Start Proportion
                computeFileBuggyness(file,lastRelCommits, tickets, releasesFiles, this.coldStartProportion);
            }
            else if(lastRelNum>=5){ //Incremental Proportion
                double incrProp=computeIncrementalProportion(tickets,lastRelNum);
                computeFileBuggyness(file,lastRelCommits,tickets, releasesFiles, incrProp);
            }
        }
    }

    private List<Ticket> filterProportionTickets(List<Ticket> tickets, int currRelNum){
        List<Ticket> ret=new ArrayList<>();
        for(Ticket ticket:tickets){
            if(ticket.getFv().getNumber()<currRelNum){
                ret.add(ticket);
            }
        }
        return ret;
    }


    /*TODO Controllare che sia corretta, no non lo è manca il pezzo in cui cambio le iv dei ticket, in measureFileBuggy
    *  devo andare a differenziare quando faccio compute ticketIV con coldstart e con incremental perche con incremental
    * devo calcolare l'iv solo dei ticket della release n-esima usando proportion calcolata dalle n-1 precedenti*/
    private double computeIncrementalProportion(List<Ticket> tickets , int currRelNum){
        int tot=0;
        int propSum=0;
        List<Ticket> prevTickets=filterProportionTickets(tickets, currRelNum);
        for(Ticket ticket:prevTickets){
            double prop;
            int fv=ticket.getFv().getNumber();
            int ov=ticket.getOv().getNumber();
            int iv=ticket.getIv();
            //System.out.println("Incremental proportion - Ticket "+ticket.getKey()+" ha IV "+iv+" perche fv è "+ticket.getFv().getName()+" num "+fv+" e ov è "+ticket.getOv().getName()+" num "+ ov);
            if((fv>=iv)&&(fv>ov)){
                prop=((double) (fv - iv) /(fv-ov));
                propSum+=prop;
                tot++;
            }


        }
        double ret=(double) propSum /tot;
        System.out.println("Incremental Proportion per " + currRelNum+" release è "+ret+" utilizzati "+prevTickets.size()+" ticket su "+tickets.size()+" propSum "+propSum+" tot "+tot);
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
                List<Integer> avs=convertReleaseToNumber(ticket.getJiraAv());
                int iv = Collections.min(avs)+1; //Perche la prima release deve essere 1 ma quando ho popolato ho omesso il +1
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
