/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes;

import burp.IHttpRequestResponse;

public class ApiEndpoint {
    private String url;
    private IHttpRequestResponse httpRequestResponse;
    private String summary;

    public ApiEndpoint(String url, IHttpRequestResponse httpRequestResponse) {
        this.url = url;
        this.httpRequestResponse = httpRequestResponse;
        this.summary = "";
    }

    public ApiEndpoint(String url, IHttpRequestResponse httpRequestResponse, String summary) {
        this.url = url;
        this.httpRequestResponse = httpRequestResponse;
        this.summary = summary != null ? summary : "";
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public IHttpRequestResponse getHttpRequestResponse() {
        return this.httpRequestResponse;
    }

    public void setHttpRequestResponse(IHttpRequestResponse httpRequestResponse) {
        this.httpRequestResponse = httpRequestResponse;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary != null ? summary : "";
    }
}

