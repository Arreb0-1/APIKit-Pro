/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.utils;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.ui.apitable.ResponseDynamicUpdatable;
import burp.utils.CommonUtils;
import burp.utils.Executor;
import burp.utils.DangerousApiFilter;

import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.HashSet;

public class HttpRequestResponse
        implements IHttpRequestResponse {
    byte[] request;
    byte[] response;
    String comment;
    IHttpService httpService;
    ResponseDynamicUpdatable updateAcceptor;

    public HttpRequestResponse() {
    }

    public HttpRequestResponse(ResponseDynamicUpdatable updateAcceptor) {
        this.updateAcceptor = updateAcceptor;
    }

    public void setUpdateAcceptor(ResponseDynamicUpdatable updateAcceptor) {
        this.updateAcceptor = updateAcceptor;
    }

    @Override
    public byte[] getRequest() {
        return this.request;
    }

    @Override
    public void setRequest(byte[] request) {
        this.request = request;
    }

    public void sendRequest() {
        if (BurpExtender.getConfigPanel().getAutoSendRequest().booleanValue()) {
            // 检查危险接口过滤
            if (BurpExtender.getConfigPanel().getDangerousApiFilterEnabled().booleanValue()) {
                String requestUrl = getRequestUrl();
                if (requestUrl != null) {
                    DangerousApiFilter filter = createDangerousApiFilter();
                    DangerousApiFilter.DangerousApiResult result = filter.checkDangerousApi(requestUrl);
                    if (result.isDangerous()) {
                        String dangerousMessage = "Dangerous API detected! Matched keywords: " + result.getMatchedKeywordsString() + "\nURL: " + requestUrl;
                        this.setResponse(dangerousMessage.getBytes());
                        BurpExtender.getStdout().println("[Dangerous API Filter] Blocked request to: " + requestUrl + " (Keywords: " + result.getMatchedKeywordsString() + ")");
                        return;
                    }
                }
            }
            
            this.setRequest(this.request);
            this.setResponse("Loading...".getBytes());
            CompletableFuture.supplyAsync(() -> {
                try {
                    IHttpRequestResponse newHttpRequestResponse = BurpExtender.getCallbacks().makeHttpRequest(this.httpService, this.request);
                    this.setResponse(newHttpRequestResponse.getResponse());
                    if (this.updateAcceptor != null) {
                        if (newHttpRequestResponse.getResponse() == null) {
                            this.updateAcceptor.setStatusCode(0);
                            this.updateAcceptor.setUnAuth("false");
                            this.updateAcceptor.setContentLength(0);
                            this.updateAcceptor.setScanTime(CommonUtils.getCurrentDateTime());
                        } else {
                            this.updateAcceptor.setStatusCode(BurpExtender.getHelpers().analyzeResponse(newHttpRequestResponse.getResponse()).getStatusCode());
                            this.updateAcceptor.setUnAuth(String.valueOf(CommonUtils.isUnAuthResponse(newHttpRequestResponse)));
                            this.updateAcceptor.setContentLength(Integer.parseInt(CommonUtils.getContentLength(newHttpRequestResponse)));
                            this.updateAcceptor.setScanTime(CommonUtils.getCurrentDateTime());
                        }
                    }
                } catch (Exception e) {
                    BurpExtender.getStderr().println(CommonUtils.exceptionToString(e));
                    this.setResponse(CommonUtils.exceptionToString(e).getBytes());
                }
                return null;
            }, Executor.getExecutor());
        } else {
            this.setResponse("Auto request sending disabled".getBytes());
        }
    }

    @Override
    public byte[] getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(byte[] response) {
        this.response = response;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getHighlight() {
        return "";
    }

    @Override
    public void setHighlight(String s) {
    }

    @Override
    public IHttpService getHttpService() {
        return this.httpService;
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
    }
    
    /**
     * 获取请求URL
     * @return 请求URL字符串
     */
    private String getRequestUrl() {
        try {
            if (this.request != null && this.httpService != null) {
                String requestString = new String(this.request);
                String[] lines = requestString.split("\r?\n");
                if (lines.length > 0) {
                    String requestLine = lines[0];
                    String[] parts = requestLine.split(" ");
                    if (parts.length >= 2) {
                        String path = parts[1];
                        String protocol = this.httpService.getProtocol();
                        String host = this.httpService.getHost();
                        int port = this.httpService.getPort();
                        
                        StringBuilder url = new StringBuilder();
                        url.append(protocol).append("://").append(host);
                        if (("http".equals(protocol) && port != 80) || ("https".equals(protocol) && port != 443)) {
                            url.append(":").append(port);
                        }
                        url.append(path);
                        return url.toString();
                    }
                }
            }
        } catch (Exception e) {
            BurpExtender.getStderr().println("Error extracting URL from request: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 创建危险接口过滤器
     * @return DangerousApiFilter实例
     */
    private DangerousApiFilter createDangerousApiFilter() {
        String keywordsString = BurpExtender.getConfigPanel().getDangerousKeywords();
        if (keywordsString != null && !keywordsString.trim().isEmpty()) {
            String[] keywords = keywordsString.split(",");
            HashSet<String> keywordSet = new HashSet<>();
            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    keywordSet.add(keyword.trim());
                }
            }
            return new DangerousApiFilter(keywordSet);
        }
        return new DangerousApiFilter();
    }
}

