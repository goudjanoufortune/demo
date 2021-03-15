package com.hello.demo;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class HttpBackendCaller {
    private final static String PATH_BACKEND = "/v1/rest/backends";
    OkHttpClient httpClient;
    String backedRegistryInfoListUrl;

    public HttpBackendCaller(String backedRegistryBaseUrl) {
        this(backedRegistryBaseUrl, new OkHttpClient());
    }

    public HttpBackendCaller(String backedRegistryBaseUrl, OkHttpClient httpClient) {
        this.httpClient = httpClient != null ? httpClient : new OkHttpClient();
        this.backedRegistryInfoListUrl = backedRegistryBaseUrl;
    }

    public HttpBackendCaller(){

    }

    public Call newCall(String backendName, String path, String method) throws BackendNotFoundException, BackendRegistryUnreachableFoundException, BadBackendDefinitionFoundException {
        return newCall(backendName, method, path, null, null, httpClient);
    }

    public Call newCall(String backendName, String path, String method, Headers headers) throws BackendNotFoundException, BackendRegistryUnreachableFoundException, BadBackendDefinitionFoundException {
        return newCall(backendName, method, path, null, headers, httpClient);
    }


    public Call newCall(String backendName, String path, String method, Headers headers, RequestBody requestBody) throws BackendNotFoundException, BackendRegistryUnreachableFoundException, BadBackendDefinitionFoundException {
        return newCall(backendName, method, path, requestBody, headers, httpClient);
    }

    public Call newCall(String backendName, String method, String path, RequestBody requestBody, Headers headers, OkHttpClient httpClient) throws BackendNotFoundException, BackendRegistryUnreachableFoundException, BadBackendDefinitionFoundException {
        Request.Builder requestBuilder = new Request.Builder();
        if (method != null || requestBody != null) {
            if (method == null) {
                method = "GET";
            }
            requestBuilder.method(method, requestBody);
        }
        if (headers != null) {
            requestBuilder.headers(headers);
        }
        return newCall(backendName, path, requestBuilder, httpClient);
    }

    public Call newCall(String backendName, String path, Request.Builder requestBuilder) throws BackendNotFoundException, BackendRegistryUnreachableFoundException {
        return newCall(backendName, path, requestBuilder, httpClient);
    }

    public Call newCall(String backendName, String path, Request.Builder requestBuilder, OkHttpClient httpClient) throws BackendNotFoundException, BackendRegistryUnreachableFoundException {
        String backendBaseUrl = resolveUrl(backendName, path);
        requestBuilder.url(backendBaseUrl);
        return httpClient.newCall(requestBuilder.build());
    }

    public String resolveUrl(String backendName, String path) throws BackendNotFoundException, BackendRegistryUnreachableFoundException {
        BackendInfo backendInfo;
        try {
            backendInfo = retrieveBackendInfo(backendName);
            if (backendInfo == null) {
                throw new BackendNotFoundException("No backend with name " + backendName + " was found", backendName);
            } else if (!backendInfo.enabled) {
                throw new BackendDisabledException(backendName, "the requested backed is disable: " + backendName);
            }
            if (backendInfo.scheme == null || backendInfo.scheme.isEmpty()) {
                throw new BadBackendDefinitionFoundException(backendName, "No 'scheme' specified for the backend named: " + backendName);
            }
            if (backendInfo.host == null || backendInfo.host.isEmpty()) {
                throw new BadBackendDefinitionFoundException(backendName, "No 'host' specified for the backend named: " + backendName);
            }
        } catch (IOException e) {
            throw new BackendRegistryUnreachableFoundException(backendName, "unable to reach the backend registry to found backendInfo for name: " + backendName, e);
        }
        return computeRequestUrl(backendInfo, path);
    }

    private String computeRequestUrl(BackendInfo backendInfo, String path) {
        String requestUrl = backendInfo.scheme + "://" + backendInfo.host;
        if (backendInfo.port > 0) {
            requestUrl += ":" + backendInfo.port;
        }
        if (requestUrl == null) {
            throw new IllegalArgumentException("the retrieved base url can't be NULL or empty");
        }
        requestUrl = regularizeUri(requestUrl, path);
        return requestUrl;
    }

    private String regularizeUri(String baseUrl, String path) {
        path = path.replaceFirst("^/", "");
        baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + path;
        return baseUrl;
    }

    private BackendInfo retrieveBackendInfo(String name) throws IOException {
        String backendRegistryUrl = regularizeUri(backedRegistryInfoListUrl, PATH_BACKEND);
        Request request = new Request.Builder()
                .url(backendRegistryUrl + "?name=" + name)
                .build();
        Response response = httpClient.newCall(request).execute();
        String jsonString = response.body().string();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray itemListJsonArray = jsonObject.optJSONArray("items");
        if (itemListJsonArray != null && itemListJsonArray.length() > 0) {
            JSONObject firstItemJsonObject = itemListJsonArray.optJSONObject(0);
            return BackendInfo.fromJson(firstItemJsonObject);
        }
        return null;
    }

    static class BackendResolutionException extends IllegalArgumentException {
        String backendName;

        public BackendResolutionException(String backendName, String message) {
            this(backendName, message, null);
        }

        public BackendResolutionException(String backendName, String message, Throwable cause) {
            super(message, cause);
            this.backendName = backendName;
        }

        public String getBackendName() {
            return backendName;
        }
    }


    public static class BackendNotFoundException extends BackendResolutionException {
        public BackendNotFoundException(String message, String backendName) {
            super(backendName, message);
        }

        public BackendNotFoundException(String message, String backendName, Throwable cause) {
            super(backendName, message, cause);
        }
    }

    public static class BackendDisabledException extends BackendResolutionException {
        public BackendDisabledException(String backendName, String message) {
            super(backendName, message);
        }

        public BackendDisabledException(String backendName, String message, Throwable cause) {
            super(backendName, message, cause);
        }
    }

    public static class BackendRegistryUnreachableFoundException extends BackendResolutionException {
        public BackendRegistryUnreachableFoundException(String backendName, String message) {
            super(backendName, message);
        }

        public BackendRegistryUnreachableFoundException(String backendName, String message, Throwable cause) {
            super(backendName, message, cause);
        }
    }

    public static class BadBackendDefinitionFoundException extends BackendResolutionException {

        public BadBackendDefinitionFoundException(String backendName, String message) {
            super(backendName, message);
        }

        public BadBackendDefinitionFoundException(String backendName, String message, Throwable cause) {
            super(backendName, message, cause);
        }
    }

}
