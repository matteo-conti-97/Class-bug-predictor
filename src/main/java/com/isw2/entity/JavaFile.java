package com.isw2.entity;

public class JavaFile {
    String name;
    String rawUrl;
    String add;
    String del;
    String content;
    String feature1;
    String feature2;
    String feature3;
    String feature4;
    String feature5;
    String feature6;
    String feature7;
    String feature8;
    String feature9;
    String feature10;


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


    public String getFeature1() {
        return feature1;
    }

    public void setFeature1(String feature1) {
        this.feature1 = feature1;
    }

    public String getFeature2() {
        return feature2;
    }

    public void setFeature2(String feature2) {
        this.feature2 = feature2;
    }

    public String getFeature3() {
        return feature3;
    }

    public void setFeature3(String feature3) {
        this.feature3 = feature3;
    }

    public String getFeature4() {
        return feature4;
    }

    public void setFeature4(String feature4) {
        this.feature4 = feature4;
    }

    public String getFeature5() {
        return feature5;
    }

    public void setFeature5(String feature5) {
        this.feature5 = feature5;
    }

    public String getFeature6() {
        return feature6;
    }

    public void setFeature6(String feature6) {
        this.feature6 = feature6;
    }

    public String getFeature7() {
        return feature7;
    }

    public void setFeature7(String feature7) {
        this.feature7 = feature7;
    }

    public String getFeature8() {
        return feature8;
    }

    public void setFeature8(String feature8) {
        this.feature8 = feature8;
    }

    public String getFeature9() {
        return feature9;
    }

    public void setFeature9(String feature9) {
        this.feature9 = feature9;
    }

    public String getFeature10() {
        return feature10;
    }

    public void setFeature10(String feature10) {
        this.feature10 = feature10;
    }
}
