/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp;

import burp.BurpExtender;
import burp.IContextMenuFactory;
import burp.IContextMenuInvocation;
import burp.IHttpRequestResponse;
import burp.PassiveScanner;
import burp.application.apitypes.ApiType;
import burp.ui.TargetAPIConfigPanel;
import burp.utils.Executor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.JMenuItem;

public class ContextMenu
        implements IContextMenuFactory {
    private static final HashSet<Byte> availableToolFlag = new HashSet();

    static {
        availableToolFlag.add((byte) 6);
        availableToolFlag.add((byte) 0);
        availableToolFlag.add((byte) 2);
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        if (invocation != null && availableToolFlag.contains(invocation.getInvocationContext())) {
            ArrayList<JMenuItem> menuItemList = new ArrayList<JMenuItem>();
            JMenuItem AutoAPIScan = new JMenuItem("Do Auto API Scan");
            JMenuItem SendToPanel = new JMenuItem("Send URL to API Panel");
            
            AutoAPIScan.addActionListener(new ContextMenuActionListener(invocation));
            
            SendToPanel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        IHttpRequestResponse[] selectedMessages = invocation.getSelectedMessages();
                        if (selectedMessages != null && selectedMessages.length > 0) {
                            IHttpRequestResponse firstMessage = selectedMessages[0];
                            if (firstMessage != null && firstMessage.getHttpService() != null) {
                                String protocol = firstMessage.getHttpService().getProtocol();
                                String host = firstMessage.getHttpService().getHost();
                                int port = firstMessage.getHttpService().getPort();
                                
                                String url = protocol + "://" + host;
                                if ((protocol.equals("https") && port != 443) || (protocol.equals("http") && port != 80)) {
                                    url += ":" + port;
                                }
                                
                                // 获取请求路径
                                if (firstMessage.getRequest() != null) {
                                    String requestString = new String(firstMessage.getRequest());
                                    String[] lines = requestString.split("\n");
                                    if (lines.length > 0) {
                                        String firstLine = lines[0];
                                        String[] parts = firstLine.split(" ");
                                        if (parts.length >= 2) {
                                            String path = parts[1];
                                            url += path;
                                        }
                                    }
                                }
                                
                                // 将HTTP请求操作移到后台线程执行，避免在EDT中进行网络请求
                                final String finalUrl = url;
                                final IHttpRequestResponse finalFirstMessage = firstMessage;
                                
                                // 使用新线程执行HTTP请求
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // 发起HTTP请求获取API文档内容
                                            java.net.URL apiUrl = new java.net.URL(finalUrl);
                                            byte[] request = BurpExtender.getHelpers().buildHttpRequest(apiUrl);
                                            IHttpRequestResponse apiDocResponse = BurpExtender.getCallbacks().makeHttpRequest(
                                                BurpExtender.getHelpers().buildHttpService(apiUrl.getHost(), apiUrl.getPort(), apiUrl.getProtocol().equals("https"))
                                                , request);
                                            
                                            if (apiDocResponse != null && apiDocResponse.getResponse() != null) {
                                                // 使用ApiScanner检测和解析API文档
                                                burp.application.ApiScanner apiScanner = new burp.application.ApiScanner();
                                                // 清除之前的扫描状态，确保每次都能正常扫描
                                                apiScanner.clearScanState();
                                                java.util.ArrayList<burp.application.apitypes.ApiType> apiTypes = apiScanner.detect(apiDocResponse, false);
                                                
                                                if (!apiTypes.isEmpty()) {
                                                    // 为每个检测到的API类型设置文档内容
                                                    for (burp.application.apitypes.ApiType apiType : apiTypes) {
                                                        apiType.getApiDocuments().put(finalUrl, apiDocResponse);
                                                    }
                                                    
                                                    // 使用PassiveScanner解析API文档，这会自动添加到UI中
                                                    PassiveScanner passiveScanner = new PassiveScanner();
                                                    passiveScanner.parseApiDocument(apiTypes, null);
                                                } else {
                                                    // 如果没有检测到特定API类型，尝试基于内容类型判断
                                                    String apiTypeName = "Manual";
                                                    String responseBody = new String(apiDocResponse.getResponse());
                                                    
                                                    // 基于响应内容智能判断API类型
                                                    if (responseBody.toLowerCase().contains("swagger") || responseBody.toLowerCase().contains("openapi")) {
                                                        apiTypeName = "Swagger/OpenAPI";
                                                    } else if (responseBody.toLowerCase().contains("graphql") || responseBody.toLowerCase().contains("__schema")) {
                                                        apiTypeName = "GraphQL";
                                                    } else if (responseBody.toLowerCase().contains("wsdl") || responseBody.toLowerCase().contains("soap")) {
                                                        apiTypeName = "SOAP/WSDL";
                                                    } else if (responseBody.toLowerCase().contains("wadl")) {
                                                        apiTypeName = "WADL";
                                                    } else if (responseBody.toLowerCase().contains("actuator")) {
                                                        apiTypeName = "Spring Actuator";
                                                    } else if (responseBody.toLowerCase().contains("json") && (responseBody.contains("paths") || responseBody.contains("endpoints"))) {
                                                        apiTypeName = "REST API";
                                                    }
                                                    
                                                    // 在EDT中更新UI
                                                    final String finalApiTypeName = apiTypeName;
                                                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            burp.ui.apitable.ApiDocumentEntity apiDocumentEntity = new burp.ui.apitable.ApiDocumentEntity(
                                                                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                                                                finalUrl,
                                                                BurpExtender.getHelpers().analyzeResponse(apiDocResponse.getResponse()).getStatusCode(),
                                                                finalApiTypeName,
                                                                "Detected",
                                                                apiDocResponse,
                                                                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                                                                Integer.parseInt(burp.utils.CommonUtils.getContentLength(apiDocResponse)),
                                                                "API Document: " + finalApiTypeName,
                                                                new java.util.ArrayList<>()
                                                            );
                                                            BurpExtender.getExtensionTab().addApiDocument(apiDocumentEntity);
                                                        }
                                                    });
                                                }
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace(BurpExtender.getStderr());
                                            // 如果请求失败，基于URL进行智能判断
                                            String apiTypeName = "Manual";
                                            String urlLower = finalUrl.toLowerCase();
                                            
                                            // 基于URL路径智能判断可能的API类型
                                            if (urlLower.contains("swagger") || urlLower.contains("openapi") || urlLower.contains("api-docs")) {
                                                apiTypeName = "Swagger/OpenAPI";
                                            } else if (urlLower.contains("graphql")) {
                                                apiTypeName = "GraphQL";
                                            } else if (urlLower.contains("wsdl") || urlLower.contains("soap")) {
                                                apiTypeName = "SOAP/WSDL";
                                            } else if (urlLower.contains("wadl")) {
                                                apiTypeName = "WADL";
                                            } else if (urlLower.contains("actuator")) {
                                                apiTypeName = "Spring Actuator";
                                            } else if (urlLower.contains("api") || urlLower.contains("rest")) {
                                                apiTypeName = "REST API";
                                            }
                                            
                                            // 在EDT中更新UI
                                            final String finalApiTypeName = apiTypeName;
                                            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    burp.ui.apitable.ApiDocumentEntity apiDocumentEntity = new burp.ui.apitable.ApiDocumentEntity(
                                                        (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                                                        finalUrl,
                                                        0,
                                                        finalApiTypeName,
                                                        "Failed",
                                                        finalFirstMessage,
                                                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                                                        0,
                                                        "Failed to load: " + finalApiTypeName,
                                                        new java.util.ArrayList<>()
                                                    );
                                                    BurpExtender.getExtensionTab().addApiDocument(apiDocumentEntity);
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(BurpExtender.getStderr());
                    }
                }
            });
            
            menuItemList.add(AutoAPIScan);
            menuItemList.add(SendToPanel);
            return menuItemList;
        }
        return null;
    }

    static class ContextMenuActionListener
            implements ActionListener {
        IContextMenuInvocation invocation;

        public ContextMenuActionListener(IContextMenuInvocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            CompletableFuture.supplyAsync(() -> {
                IHttpRequestResponse[] httpRequestResponses;
                final PassiveScanner passiveScanner = BurpExtender.getPassiveScanner();
                if (this.invocation == null || this.invocation.getSelectedMessages() == null) {
                    return null;
                }
                for (final IHttpRequestResponse httpRequestResponse : httpRequestResponses = this.invocation.getSelectedMessages()) {
                    if (httpRequestResponse == null) {
                        continue;
                    }
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            ArrayList<ApiType> apiTypes = passiveScanner.getApiScanner().detect(httpRequestResponse, false);
                            passiveScanner.parseApiDocument(apiTypes, null);
                        }
                    }).start();
                }
                return null;
            }, Executor.getExecutor());
        }
    }
}

