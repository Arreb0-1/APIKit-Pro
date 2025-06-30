/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes;

import burp.IHttpRequestResponse;
import burp.utils.DangerousApiFilter;

public class ApiEndpoint {
    private String url;
    private IHttpRequestResponse httpRequestResponse;
    private String summary;
    private boolean isDangerous;
    private String dangerousReason;

    public ApiEndpoint(String url, IHttpRequestResponse httpRequestResponse) {
        this.url = url;
        this.httpRequestResponse = httpRequestResponse;
        this.summary = "";
        this.isDangerous = false;
        this.dangerousReason = "";
    }

    public ApiEndpoint(String url, IHttpRequestResponse httpRequestResponse, String summary) {
        this.url = url;
        this.httpRequestResponse = httpRequestResponse;
        this.summary = summary != null ? summary : "";
        this.isDangerous = false;
        this.dangerousReason = "";
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
    
    public boolean isDangerous() {
        return this.isDangerous;
    }
    
    public void setDangerous(boolean dangerous) {
        this.isDangerous = dangerous;
    }
    
    public String getDangerousReason() {
        return this.dangerousReason;
    }
    
    public void setDangerousReason(String dangerousReason) {
        this.dangerousReason = dangerousReason != null ? dangerousReason : "";
    }
    
    /**
     * 检查并标记危险接口
     * @param filter 危险接口过滤器
     */
    public void checkAndMarkDangerous(DangerousApiFilter filter) {
        if (filter != null && filter.isEnabled()) {
            DangerousApiFilter.DangerousApiResult result = filter.checkDangerousApi(this.url);
            this.isDangerous = result.isDangerous();
            if (result.isDangerous()) {
                this.dangerousReason = "Dangerous keywords detected: " + result.getMatchedKeywordsString();
            } else {
                this.dangerousReason = "";
            }
        }
    }
}

