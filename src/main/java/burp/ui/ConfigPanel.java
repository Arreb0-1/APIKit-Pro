/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToolBar;

public class ConfigPanel
        extends JToolBar {
    JCheckBox autoSendRequestCheckBox = new JCheckBox("Auto request sending");
    JCheckBox includeCookieCheckBox = new JCheckBox("Send with cookie");
    JButton clearHistoryButton = new JButton("Clear history");
    JCheckBox scannerEnabledCheckBox = new JCheckBox("Scanner Enabled");

    public ConfigPanel() {
        this.autoSendRequestCheckBox.setSelected(false);
        this.includeCookieCheckBox.setSelected(false);
        this.scannerEnabledCheckBox.setSelected(false);
        this.setFloatable(false);
        this.add(this.scannerEnabledCheckBox);
        this.add(this.autoSendRequestCheckBox);
        this.add(this.includeCookieCheckBox);
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

    public void addClearHistoryCallback(Runnable callback) {
        this.clearHistoryButton.addActionListener(actionEvent -> callback.run());
    }
}

