/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.ui.apitable;

import burp.ui.apitable.ApiDocumentEntity;
import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.PassiveScanner;
import burp.application.ApiScanner;
import burp.application.apitypes.ApiType;
import burp.utils.CommonUtils;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class ApiDocumentTable
        extends JTable {
    private final ApiDocumentTableModel model;
    private final Consumer<ApiDocumentEntity> onSelectedCallback;

    public ApiDocumentTable(Consumer<ApiDocumentEntity> onSelectedCallback) {
        this.onSelectedCallback = onSelectedCallback;
        this.model = new ApiDocumentTableModel();
        this.setModel(this.model);
        this.setSelectionMode(2);
        this.setEnabled(true);
        this.setAutoCreateRowSorter(true);
        this.getColumnModel().getColumn(0).setMaxWidth(30);
        this.getSelectionModel().addListSelectionListener(e -> {
            ApiDocumentEntity selected;
            int selectedRow;
            if (!e.getValueIsAdjusting() && (selectedRow = this.getSelectedRow()) != -1 && (selected = this.model.getEntityAt(this.convertRowIndexToModel(selectedRow))) != null && this.onSelectedCallback != null) {
                this.onSelectedCallback.accept(selected);
            }
        });
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 67 && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    ApiDocumentTable.this.copySelectedRows();
                }
            }
        });
        
        // 添加右键菜单
        this.setupContextMenu();
    }
    
    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem requestApiDocItem = new JMenuItem("Request API Document");
        requestApiDocItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();
                if (selectedRow != -1) {
                    ApiDocumentEntity entity = model.getEntityAt(convertRowIndexToModel(selectedRow));
                    requestApiDocument(entity);
                }
            }
        });
        
        JMenuItem copyUrlItem = new JMenuItem("Copy URL");
        copyUrlItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();
                if (selectedRow != -1) {
                    ApiDocumentEntity entity = model.getEntityAt(convertRowIndexToModel(selectedRow));
                    StringSelection selection = new StringSelection(entity.url);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
            }
        });
        
        JMenuItem deleteItemItem = new JMenuItem("Delete Item");
        deleteItemItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = convertRowIndexToModel(selectedRow);
                    model.removeRow(modelRow);
                }
            }
        });
        
        JMenuItem clearAllItem = new JMenuItem("Clear All Records");
        clearAllItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.clear();
            }
        });
        
        contextMenu.add(requestApiDocItem);
        contextMenu.add(copyUrlItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItemItem);
        contextMenu.add(clearAllItem);
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            private void showContextMenu(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row >= 0 && row < getRowCount()) {
                    setRowSelectionInterval(row, row);
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private void requestApiDocument(final ApiDocumentEntity entity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 发起HTTP请求获取API文档内容
                    java.net.URL apiUrl = new java.net.URL(entity.url);
                    byte[] request = BurpExtender.getHelpers().buildHttpRequest(apiUrl);
                    IHttpRequestResponse apiDocResponse = BurpExtender.getCallbacks().makeHttpRequest(
                        BurpExtender.getHelpers().buildHttpService(apiUrl.getHost(), apiUrl.getPort(), apiUrl.getProtocol().equals("https")),
                        request);
                    
                    if (apiDocResponse != null && apiDocResponse.getResponse() != null) {
                        // 使用ApiScanner检测API文档类型
                        ApiScanner apiScanner = new ApiScanner();
                        // 清除之前的扫描状态，确保每次都能正常扫描
                        apiScanner.clearScanState();
                        ArrayList<ApiType> apiTypes = apiScanner.detect(apiDocResponse, false);
                        
                        if (!apiTypes.isEmpty()) {
                            // 为每个检测到的API类型设置文档内容
                            for (ApiType apiType : apiTypes) {
                                apiType.getApiDocuments().put(entity.url, apiDocResponse);
                            }
                            
                            // 使用PassiveScanner解析API文档，这会自动添加到UI中
                            PassiveScanner passiveScanner = new PassiveScanner();
                            passiveScanner.parseApiDocument(apiTypes, null);
                            
                            // 在UI线程中更新当前实体的状态
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    entity.statusCode = BurpExtender.getHelpers().analyzeResponse(apiDocResponse.getResponse()).getStatusCode();
                                    entity.contentLength = Integer.parseInt(CommonUtils.getContentLength(apiDocResponse));
                                    entity.scanTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                                    
                                    // 刷新表格显示
                                    model.fireTableDataChanged();
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(BurpExtender.getStderr());
                }
            }
        }).start();
    }

    public void append(ApiDocumentEntity apiDocument) {
        this.model.append(apiDocument);
    }

    public void clear() {
        this.model.clear();
        this.setEnabled(true);
    }

    private void copySelectedRows() {
        int[] rows = this.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < this.getColumnCount(); ++col) {
            sb.append(this.getColumnName(col));
            if (col >= this.getColumnCount() - 1) continue;
            sb.append("\t");
        }
        sb.append("\n");
        for (int row : rows) {
            for (int col = 0; col < this.getColumnCount(); ++col) {
                Object value = this.getValueAt(row, col);
                if (value != null) {
                    sb.append(value.toString());
                }
                if (col >= this.getColumnCount() - 1) continue;
                sb.append("\t");
            }
            sb.append("\n");
        }
        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    public static class ApiDocumentTableModel
            extends AbstractTableModel {
        private final List<ApiDocumentEntity> tableData = new ArrayList<ApiDocumentEntity>();

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void append(ApiDocumentEntity entity) {
            ApiDocumentTableModel apiDocumentTableModel;
            ApiDocumentTableModel apiDocumentTableModel2 = apiDocumentTableModel = this;
            synchronized (apiDocumentTableModel2) {
                this.tableData.add(entity);
                int _id = this.tableData.size();
                this.fireTableRowsInserted(_id - 1, _id - 1);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public synchronized void clear() {
            ApiDocumentTableModel apiDocumentTableModel;
            ApiDocumentTableModel apiDocumentTableModel2 = apiDocumentTableModel = this;
            synchronized (apiDocumentTableModel2) {
                int size = this.tableData.size();
                if (size > 0) {
                    this.tableData.clear();
                    this.fireTableRowsDeleted(0, size - 1);
                }
            }
        }

        public ApiDocumentEntity getEntityAt(int rowIndex) {
            return this.tableData.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return this.tableData.size();
        }

        @Override
        public int getColumnCount() {
            return 8;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return Integer.class;
                }
                case 1: {
                    return String.class;
                }
                case 2: {
                    return Integer.class;
                }
                case 3: {
                    return Integer.class;
                }
                case 4: {
                    return String.class;
                }
                case 5: {
                    return String.class;
                }
                case 6: {
                    return String.class;
                }
                case 7: {
                    return String.class;
                }
            }
            return null;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    return "#";
                }
                case 1: {
                    return "URL";
                }
                case 2: {
                    return "Status Code";
                }
                case 3: {
                    return "Content Length";
                }
                case 4: {
                    return "Unauth";
                }
                case 5: {
                    return "API Type";
                }
                case 6: {
                    return "Scan Time";
                }
                case 7: {
                    return "Summary";
                }
            }
            return null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ApiDocumentEntity entity = this.tableData.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return entity.id;
                }
                case 1: {
                    return entity.url;
                }
                case 2: {
                    return entity.statusCode;
                }
                case 3: {
                    return entity.contentLength;
                }
                case 4: {
                    return entity.unAuth;
                }
                case 5: {
                    return entity.apiType;
                }
                case 6: {
                    return entity.scanTime;
                }
                case 7: {
                    return entity.summary != null ? entity.summary : "";
                }
            }
            return null;
        }

        public ApiDocumentEntity getApiDocument(int rowIndex) {
            return this.getEntityAt(rowIndex);
        }
        
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public synchronized void removeRow(int rowIndex) {
            ApiDocumentTableModel apiDocumentTableModel;
            ApiDocumentTableModel apiDocumentTableModel2 = apiDocumentTableModel = this;
            synchronized (apiDocumentTableModel2) {
                if (rowIndex >= 0 && rowIndex < this.tableData.size()) {
                    this.tableData.remove(rowIndex);
                    this.fireTableRowsDeleted(rowIndex, rowIndex);
                }
            }
        }
    }
}

