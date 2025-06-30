/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes;

import burp.IHttpRequestResponse;
import burp.IScanIssue;
import burp.application.apitypes.ApiEndpoint;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

public interface ApiTypeInterface {
    public String getApiTypeName();

    public Boolean isFingerprintMatch();

    public Map<String, IHttpRequestResponse> getApiDocuments();

    public List<ApiEndpoint> parseApiDocument(IHttpRequestResponse var1, @Nullable IHttpRequestResponse var2, boolean var3);

    public List<IScanIssue> exportIssues();

    public String exportConsole();

    public Boolean urlAddPath(String var1);
}

