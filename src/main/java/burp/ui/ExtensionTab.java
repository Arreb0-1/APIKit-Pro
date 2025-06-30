/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.ITab;
import burp.ui.ConfigPanel;
import burp.ui.HttpRequestResponsePanel;
import burp.ui.TargetAPIConfigPanel;
import burp.ui.apitable.ApiDetailTable;
import burp.ui.apitable.ApiDocumentEntity;
import burp.ui.apitable.ApiDocumentTable;

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class ExtensionTab
        implements ITab {
    private final String tagName;
    private final ConfigPanel configPanel;
    private JTabbedPane mainPanel;
    private ApiDocumentTable apiDocumentTable;
    private TargetAPIConfigPanel targetAPIConfigPanel;

    public ExtensionTab(String name) {
        final IBurpExtenderCallbacks callbacks = BurpExtender.getCallbacks();
        this.tagName = name;
        this.configPanel = new ConfigPanel();
        this.targetAPIConfigPanel = new TargetAPIConfigPanel();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // 创建主标签页面板
                ExtensionTab.this.mainPanel = new JTabbedPane();
                
                // 创建API扫描主界面
                JSplitPane apiScanPanel = new JSplitPane(0);
                HttpRequestResponsePanel httpRequestResponsePanel = new HttpRequestResponsePanel(callbacks);
                JSplitPane apiTableAndConfigPanel = new JSplitPane(0);
                apiTableAndConfigPanel.setEnabled(false);
                BurpExtender.setConfigPanel(ExtensionTab.this.configPanel);
                ApiDetailTable apiDetailTable = new ApiDetailTable(entity -> httpRequestResponsePanel.setHttpRequestResponse(entity.requestResponse));
                JSplitPane apiTablePanel = new JSplitPane(0);
                ExtensionTab.this.apiDocumentTable = new ApiDocumentTable(entity -> {
                    apiDetailTable.setApiDetail((ApiDocumentEntity) entity);
                    httpRequestResponsePanel.setHttpRequestResponse(entity.requestResponse);
                });
                ExtensionTab.this.configPanel.addClearHistoryCallback(() -> {
                    apiDetailTable.clear();
                    ExtensionTab.this.apiDocumentTable.clear();
                    httpRequestResponsePanel.clear();
                    BurpExtender.clearPassiveScannerCache();
                });
                apiTablePanel.add((Component) new JScrollPane(ExtensionTab.this.apiDocumentTable), "left");
                apiTablePanel.add((Component) new JScrollPane(apiDetailTable), "right");
                apiTablePanel.setResizeWeight(0.5);
                apiTableAndConfigPanel.add((Component) ExtensionTab.this.configPanel, "left");
                apiTableAndConfigPanel.add((Component) apiTablePanel, "right");
                apiScanPanel.add((Component) apiTableAndConfigPanel, "left");
                apiScanPanel.add((Component) httpRequestResponsePanel, "right");
                apiScanPanel.setResizeWeight(0.5);
                
                // 添加标签页
                ExtensionTab.this.mainPanel.addTab("API Scanner", apiScanPanel);
                ExtensionTab.this.mainPanel.addTab("Target API Scan", ExtensionTab.this.targetAPIConfigPanel.getMainPanel());
                
                callbacks.customizeUiComponent(ExtensionTab.this.mainPanel);
                callbacks.addSuiteTab(ExtensionTab.this);
            }
        });
    }

    @Override
    public String getTabCaption() {
        return this.tagName;
    }

    @Override
    public Component getUiComponent() {
        return this.mainPanel;
    }

    public void addApiDocument(ApiDocumentEntity apiDocumentEntity) {
        this.apiDocumentTable.append(apiDocumentEntity);
    }

    public ConfigPanel getConfigPanel() {
        return this.configPanel;
    }
}

