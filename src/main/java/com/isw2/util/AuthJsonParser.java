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
    private static boolean flag_token_onibaku = false;

    public static void setFlag_token_onibaku(boolean flag_token_onibaku) {
        AuthJsonParser.flag_token_onibaku = flag_token_onibaku;
    }

    private InputStreamReader getAuthStream(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        uc.setRequestProperty("X-Requested-With", "Curl");
        String token;
        String user;
        if (flag_token_onibaku) {
            user = USERNAME2;
            try (BufferedReader oauthReader = new BufferedReader(new FileReader("src/main/java/resource/token/git_token_onibaku"))) {
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
