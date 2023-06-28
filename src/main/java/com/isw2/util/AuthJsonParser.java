package com.isw2.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthJsonParser extends JsonParser {
    private static final String USERNAME = "matteo-conti-97";
    private static final String USERNAME2 = "OniBaku972";
    private static final String USERNAME3 = "mcuni97";
    private static final String USERNAME4 = "sdgasg82";
    private static final String USERNAME5 = "sdfas6695";
    private static final String USERNAME6 = "rioeuto";
    private static int flagToken = 4;

    public static void setFlagToken(int flagToken) {
        AuthJsonParser.flagToken = flagToken;
    }

    public static int getFlagToken(){
        return flagToken;
    }

    private InputStreamReader getAuthStream(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        uc.setRequestProperty("X-Requested-With", "Curl");
        String token;
        String user;
        if (flagToken ==1) {
            user = USERNAME2;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_onibaku"))) {
                token = oauthReader.readLine();
            }
        }else if(flagToken ==2){
            user = USERNAME3;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_mcuni"))) {
                token = oauthReader.readLine();
            }
        }else if(flagToken ==3){
            user = USERNAME4;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_sdgasg82"))) {
                token = oauthReader.readLine();
            }
        }else if(flagToken ==4) {
            user = USERNAME5;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_sdfas6695"))) {
                token = oauthReader.readLine();
            }
        }else if(flagToken ==5) {
            user = USERNAME6;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_rioeuto"))) {
                token = oauthReader.readLine();
            }
        } else {
            user = USERNAME;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_matteo"))) {
                token = oauthReader.readLine();
            }
        }
        String userPass = user + ":" + token;
        byte[] encodedBytes = Base64.getEncoder().encode(userPass.getBytes());
        String basicAuth = "Basic " + new String(encodedBytes);
        uc.setRequestProperty("Authorization", basicAuth);

        return new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8);
    }

    @Override
    public JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        BufferedReader rd = new BufferedReader(getAuthStream(new URL(url)));
        String jsonText = readAll(rd);
        return new JSONArray(jsonText);
    }

    @Override
    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        BufferedReader rd = new BufferedReader(getAuthStream(new URL(url)));
        String jsonText = readAll(rd);
        return new JSONObject(jsonText);
    }
}
