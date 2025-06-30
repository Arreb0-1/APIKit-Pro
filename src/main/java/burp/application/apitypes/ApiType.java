/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes;

import burp.IHttpRequestResponse;
import burp.application.apitypes.ApiEndpoint;
import burp.application.apitypes.ApiTypeInterface;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ApiType
        implements ApiTypeInterface {
    private final HashMap<String, IHttpRequestResponse> ApiDocuments = new HashMap();
    protected Boolean isPassive;
    private String apiTypeName = "";

    @Override
    public String getApiTypeName() {
        return this.apiTypeName;
    }

    protected void setApiTypeName(String value) {
        this.apiTypeName = value;
    }

    public HashMap<String, IHttpRequestResponse> getApiDocuments() {
        return this.ApiDocuments;
    }

    public ArrayList<ApiEndpoint> parseApiDocument(IHttpRequestResponse apiDocument, IHttpRequestResponse basePathURL, boolean isTargetScan) {
        return null;
    }

    @Override
    public Boolean urlAddPath(String apiDocumentUrl) {
        return false;
    }

    @Override
    public Boolean isFingerprintMatch() {
        return false;
    }
}

