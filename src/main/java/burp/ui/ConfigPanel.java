/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToolBar;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.FlowLayout;
import java.awt.Dimension;

public class ConfigPanel
        extends JToolBar {
    JCheckBox autoSendRequestCheckBox = new JCheckBox("Auto request sending");
    JCheckBox includeCookieCheckBox = new JCheckBox("Send with cookie");
    JButton clearHistoryButton = new JButton("Clear history");
    JCheckBox scannerEnabledCheckBox = new JCheckBox("Scanner Enabled");
    JCheckBox dangerousApiFilterCheckBox = new JCheckBox("Dangerous API Filter");
    JTextField dangerousKeywordsField = new JTextField("delete,del,remove,drop,destroy,admin,exec", 30);

    public ConfigPanel() {
        this.autoSendRequestCheckBox.setSelected(false);
        this.includeCookieCheckBox.setSelected(false);
        this.scannerEnabledCheckBox.setSelected(false);
        this.dangerousApiFilterCheckBox.setSelected(true);
        this.setFloatable(false);
        
        this.add(this.scannerEnabledCheckBox);
        this.add(this.autoSendRequestCheckBox);
        this.add(this.includeCookieCheckBox);
        this.add(this.dangerousApiFilterCheckBox);
        
        // 添加危险关键词配置面板
        JPanel keywordsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        keywordsPanel.add(new JLabel("Keywords:"));
        keywordsPanel.add(this.dangerousKeywordsField);
        keywordsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, keywordsPanel.getPreferredSize().height));
        this.add(keywordsPanel);
        
        this.add(Box.createHorizontalGlue());
        this.add(this.clearHistoryButton);
    }

    public Boolean getAutoSendRequest() {
        return this.autoSendRequestCheckBox.isSelected();
    }

    public Boolean getIncludeCookie() {
        return this.includeCookieCheckBox.isSelected();
    }

    public Boolean getScannerEnabled() {
        return this.scannerEnabledCheckBox.isSelected();
    }
    
    public Boolean getDangerousApiFilterEnabled() {
        return this.dangerousApiFilterCheckBox.isSelected();
    }
    
    public String getDangerousKeywords() {
        return this.dangerousKeywordsField.getText();
    }
    
    public void setDangerousKeywords(String keywords) {
        this.dangerousKeywordsField.setText(keywords);
    }

    public void addClearHistoryCallback(Runnable callback) {
        this.clearHistoryButton.addActionListener(actionEvent -> callback.run());
    }
}

