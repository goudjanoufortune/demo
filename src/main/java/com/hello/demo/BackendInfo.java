package com.hello.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class BackendInfo {
    String scheme;
    Date createdAt;
    Date updatedAt;
    UUID id;
    String name;
    String host;
    int port;
    boolean enabled = false;
    Credential credential;

    BackendInfo() {

    }

    public static BackendInfo fromJson(JSONObject firstItemJsonObject) {
        return fromJsonString(firstItemJsonObject.toString());
    }

    public static class Credential {
        String type;
        String value;

        Credential() {

        }

    }

    public static BackendInfo fromJsonString(String jsonString) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00").create();
        return gson.fromJson(jsonString, BackendInfo.class);
    }
}
