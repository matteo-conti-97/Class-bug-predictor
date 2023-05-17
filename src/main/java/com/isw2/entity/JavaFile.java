package com.isw2.entity;

public class JavaFile {
    String name;
    String rawUrl;
    String add;
    String del;
    String content;
    String nAuthorInRelease;
    String locAtEndRelease;
    String avgChurnInRelease;
    String avgChurnFromStart;
    String nRevInRelease;
    String nRevFromStart;
    String avgLocAddedInRelease;
    String avgLocAddedFromStart;
    String nFixCommitInRelease;
    String nFixCommitFromStart;
    String buggy;


    //Usato per riempire la tabella del file tree nel db
    public JavaFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    //Usato per riempire la tabella delle commit nel db
    public JavaFile(String name, String add, String del, String content) {
        this.name = name;
        this.add = add;
        this.del = del;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public String getDel() {
        return del;
    }

    public void setDel(String del) {
        this.del = del;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }


    public String getnAuthorInRelease() {
        return nAuthorInRelease;
    }

    public void setnAuthorInRelease(String nAuthorInRelease) {
        this.nAuthorInRelease = nAuthorInRelease;
    }

    public String getLocAtEndRelease() {
        return locAtEndRelease;
    }

    public void setLocAtEndRelease(String locAtEndRelease) {
        this.locAtEndRelease = locAtEndRelease;
    }

    public String getAvgChurnInRelease() {
        return avgChurnInRelease;
    }

    public void setAvgChurnInRelease(String avgChurnInRelease) {
        this.avgChurnInRelease = avgChurnInRelease;
    }

    public String getAvgChurnFromStart() {
        return avgChurnFromStart;
    }

    public void setAvgChurnFromStart(String avgChurnFromStart) {
        this.avgChurnFromStart = avgChurnFromStart;
    }

    public String getnRevInRelease() {
        return nRevInRelease;
    }

    public void setnRevInRelease(String nRevInRelease) {
        this.nRevInRelease = nRevInRelease;
    }

    public String getnRevFromStart() {
        return nRevFromStart;
    }

    public void setnRevFromStart(String nRevFromStart) {
        this.nRevFromStart = nRevFromStart;
    }

    public String getAvgLocAddedInRelease() {
        return avgLocAddedInRelease;
    }

    public void setAvgLocAddedInRelease(String avgLocAddedInRelease) {
        this.avgLocAddedInRelease = avgLocAddedInRelease;
    }

    public String getAvgLocAddedFromStart() {
        return avgLocAddedFromStart;
    }

    public void setAvgLocAddedFromStart(String avgLocAddedFromStart) {
        this.avgLocAddedFromStart = avgLocAddedFromStart;
    }

    public String getnFixCommitInRelease() {
        return nFixCommitInRelease;
    }

    public void setnFixCommitInRelease(String nFixCommitInRelease) {
        this.nFixCommitInRelease = nFixCommitInRelease;
    }

    public String getnFixCommitFromStart() {
        return nFixCommitFromStart;
    }

    public void setnFixCommitFromStart(String nFixCommitFromStart) {
        this.nFixCommitFromStart = nFixCommitFromStart;
    }

    public String getBuggy() {
        return buggy;
    }

    public void setBuggy(String buggy) {
        this.buggy = buggy;
    }
}
