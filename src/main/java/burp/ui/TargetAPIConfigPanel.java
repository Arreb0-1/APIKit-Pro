/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui;

import burp.BurpExtender;
import burp.ui.TargetAPIScan;
import burp.utils.Constants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TargetAPIConfigPanel
        extends JPanel {
    private final JPanel mainPanel = new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private final JLabel APIType = new JLabel("API Technology Type", 0);
    private final JComboBox APITypes = new JComboBox();
    private final JLabel BasePath = new JLabel("Base Path URL", 0);
    private final JTextField BasePathURL = new JTextField("https://apisecurity.cn/api/");
    private final JLabel APIDocument = new JLabel("API Document URL", 0);
    private final JTextField APIDocumentURL = new JTextField("https://apisecurity.cn/api/mappings");
    private final JLabel Header = new JLabel("Header(\u901a\u8fc7\u6362\u884c\u53ef\u4ee5\u6dfb\u52a0\u591a\u4e2aheader)", 0);
    private final JTextArea Headervalue = new JTextArea("Cookie: APIKit=APISecurity; ");
    private final JLabel BypassSuffix = new JLabel("Bypass Suffix（支持Swagger和spring）", 0);
    private final JTextField BypassSuffixValue = new JTextField("");
    private final JButton btScan = new JButton("Scan");
    private final JButton btCancel = new JButton("Cancel");
    private JTextArea headerArea;

    public TargetAPIConfigPanel() {
        try {
            this.initGUI();
            this.initEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    public JComboBox getAPIType() {
        return this.APITypes;
    }

    public JTextField getBasePathURL() {
        return this.BasePathURL;
    }

    public JTextField getAPIDocumentURL() {
        return this.APIDocumentURL;
    }

    public JTextArea getHeadervalue() {
        return this.Headervalue;
    }

    public String getHeaders() {
        return this.headerArea.getText();
    }

    public void setHeaders(String headers) {
        this.headerArea.setText(headers);
    }

    private void initGUI() {
        this.centerPanel.setLayout(new GridLayout(5, 2, 1, 1));
        this.centerPanel.add(this.APIType);
        this.centerPanel.add(this.APITypes);
        for (String APITypeName : Constants.APITypeNames) {
            this.APITypes.addItem(APITypeName);
        }
        this.centerPanel.add(this.BasePath);
        this.centerPanel.add(this.BasePathURL);
        this.centerPanel.add(this.APIDocument);
        this.centerPanel.add(this.APIDocumentURL);
        this.Headervalue.setRows(5);
        this.Headervalue.setColumns(40);
        this.Headervalue.setLineWrap(true);
        this.Headervalue.setWrapStyleWord(true);
        JScrollPane headerScrollPane = new JScrollPane(this.Headervalue);
        headerScrollPane.setPreferredSize(new Dimension(100, 20));
        headerScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.centerPanel.add(this.Header);
        this.centerPanel.add(headerScrollPane);
        this.centerPanel.add(this.BypassSuffix);
        this.centerPanel.add(this.BypassSuffixValue);
        this.bottomPanel.setLayout(new FlowLayout(1));
        this.bottomPanel.add(this.btScan);
        this.bottomPanel.add(this.btCancel);
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add((Component) this.centerPanel, "Center");
        this.mainPanel.add((Component) this.bottomPanel, "South");
        this.setLayout(new BorderLayout());
        this.add(this.mainPanel, BorderLayout.CENTER);
    }

    private void initEvent() {
        this.btCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空所有输入字段
                TargetAPIConfigPanel.this.BasePathURL.setText("https://apisecurity.cn/api/");
                TargetAPIConfigPanel.this.APIDocumentURL.setText("https://apisecurity.cn/api/mappings");
                TargetAPIConfigPanel.this.Headervalue.setText("Cookie: APIKit=APISecurity; ");
                TargetAPIConfigPanel.this.BypassSuffixValue.setText("");
                TargetAPIConfigPanel.this.APITypes.setSelectedIndex(0);
            }
        });
        this.btScan.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                BurpExtender.TargetAPI.put("BasePathURL", TargetAPIConfigPanel.this.BasePathURL.getText().trim());
                BurpExtender.TargetAPI.put("APIDocumentURL", TargetAPIConfigPanel.this.APIDocumentURL.getText().trim());
                BurpExtender.TargetAPI.put("Header", TargetAPIConfigPanel.this.Headervalue.getText().trim());
                BurpExtender.TargetAPI.put("APITypename", (String) TargetAPIConfigPanel.this.APITypes.getSelectedItem());
                BurpExtender.TargetAPI.put("BypassSuffix", TargetAPIConfigPanel.this.BypassSuffixValue.getText().trim());
                TargetAPIScan targetAPIScan = new TargetAPIScan();
                targetAPIScan.start();
                try {
                    Thread.sleep(0L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return;
                }
                // 扫描完成后，结果会自动显示在主表格中
                BurpExtender.getStdout().println("Target API Scan started successfully!");
            }
        });
    }
}

