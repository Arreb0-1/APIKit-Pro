/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.IScanIssue;
import burp.IScannerCheck;
import burp.IScannerInsertionPoint;
import burp.application.ApiScanner;
import burp.application.apitypes.ApiEndpoint;
import burp.application.apitypes.ApiType;
import burp.application.apitypes.actuator.ApiTypeActuator;
import burp.application.apitypes.graphql.ApiTypeGraphQL;
import burp.application.apitypes.rest.ApiTypeRest;
import burp.application.apitypes.soap.ApiTypeSoap;
import burp.application.apitypes.swagger.ApiTypeSwagger;
import burp.ui.ExtensionTab;
import burp.ui.apitable.ApiDetailEntity;
import burp.ui.apitable.ApiDocumentEntity;
import burp.utils.CommonUtils;
import burp.utils.HttpRequestResponse;
import burp.utils.UrlScanCount;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PassiveScanner
        implements IScannerCheck {
    private final UrlScanCount scanedUrl = new UrlScanCount();
    private final ApiScanner apiScanner;
    private final Object lock = new Object();
    private int scannedCount = 1;

    public PassiveScanner() {
        this.apiScanner = new ApiScanner();
    }

    public void clearUrlScanedCache() {
        this.scannedCount = 1;
        this.scanedUrl.clear();
        ApiTypeRest.scannedUrl.clear();
        ApiTypeActuator.scannedUrl.clear();
        ApiTypeGraphQL.scannedUrl.clear();
        ApiTypeSoap.scannedUrl.clear();
        ApiTypeSwagger.scannedUrl.clear();
        this.apiScanner.clearScanState();
    }

    public ApiScanner getApiScanner() {
        return this.apiScanner;
    }

    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse httpRequestResponse) {
        URL httpRequestURL = BurpExtender.getHelpers().analyzeRequest(httpRequestResponse).getUrl();
        String requestUrl = CommonUtils.getUrlWithoutFilename(httpRequestURL);
        if (this.scanedUrl.get(requestUrl) > 0) {
            return null;
        }
        this.scanedUrl.add(requestUrl);
        System.out.println("Scanning\t" + requestUrl);
        ArrayList<ApiType> apiTypes = this.apiScanner.detect(httpRequestResponse, true);
        return this.parseApiDocument(apiTypes, null);
    }

    public List<IScanIssue> parseApiDocument(final ArrayList<ApiType> apiTypes, final IHttpRequestResponse basePath) {
        ArrayList<IScanIssue> issues = new ArrayList<IScanIssue>();
        final ExtensionTab extensionTab = BurpExtender.getExtensionTab();
        for (final ApiType apiType : apiTypes) {
            Map apiDocuments = apiType.getApiDocuments();
            for (Object obj : apiDocuments.entrySet()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, IHttpRequestResponse> entry = (Map.Entry<String, IHttpRequestResponse>) obj;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int num;
                        Object lockObj = PassiveScanner.this.lock;
                        boolean isTargetScan = false;
                        if (apiTypes.size() == 1) {
                            isTargetScan = true;
                        }

                        List<ApiEndpoint> apiEndpoints = apiType.parseApiDocument(entry.getValue(), basePath, isTargetScan);
                        apiEndpoints.sort(Comparator.comparing(ApiEndpoint::getUrl));
                        ArrayList<ApiDetailEntity> apiDetails = new ArrayList<>();

                        for (ApiEndpoint endpoint : apiEndpoints) {
                            IHttpRequestResponse apiParseRequestResponse = endpoint.getHttpRequestResponse();
                            ApiDetailEntity currentDetail = createApiDetailEntity(endpoint, apiParseRequestResponse, apiType);
                            if (apiParseRequestResponse instanceof HttpRequestResponse) {
                                ((HttpRequestResponse) apiParseRequestResponse).setUpdateAcceptor(currentDetail);
                            }
                            apiDetails.add(currentDetail);
                        }

                        synchronized (lockObj) {
                            num = PassiveScanner.this.scannedCount++;
                        }
                        ApiDocumentEntity apiDocument = new ApiDocumentEntity(num, (String) entry.getKey(), BurpExtender.getHelpers().analyzeResponse(((IHttpRequestResponse) entry.getValue()).getResponse()).getStatusCode(), apiType.getApiTypeName(), "true", (IHttpRequestResponse) entry.getValue(), CommonUtils.getCurrentDateTime(), Integer.parseInt(CommonUtils.getContentLength((IHttpRequestResponse) entry.getValue())), apiDetails);
                        extensionTab.addApiDocument(apiDocument);
                    }
                }).start();
            }
            issues.addAll(apiType.exportIssues());
            BurpExtender.getStdout().print(apiType.exportConsole());
        }
        return issues;
    }

    private ApiDetailEntity createApiDetailEntity(ApiEndpoint endpoint, IHttpRequestResponse response, ApiType apiType) {
        if (response.getResponse() != null && response.getResponse().length != 0) {
            return new ApiDetailEntity(endpoint.getUrl(), BurpExtender.getHelpers().analyzeResponse(response.getResponse()).getStatusCode(), apiType.getApiTypeName(), String.valueOf(CommonUtils.isUnAuthResponse(response)), response, CommonUtils.getCurrentDateTime(), Integer.parseInt(CommonUtils.getContentLength(response)));
        }
        return new ApiDetailEntity(endpoint.getUrl(), 0, apiType.getApiTypeName(), "false", response, CommonUtils.getCurrentDateTime(), 0);
    }

    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse httpRequestResponse, IScannerInsertionPoint insertionPoint) {
        return null;
    }

    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
        if (existingIssue.getIssueName().equals(newIssue.getIssueName())) {
            return -1;
        }
        return 0;
    }
}

