/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tw.com.hyweb.gui;

import tw.com.hyweb.admin.JmxClient;

/**
 * The Class ConfigPanel.
 *
 * @author SamSon
 */
public class ConfigPanel extends javax.swing.JPanel {
    private JmxClient jmxClient;
    private DefMainGui defMainGui;
    
    /**
     * Creates new form ConfigPanel.
     *
     * @param defMainGui the def main gui
     */
    public ConfigPanel(DefMainGui defMainGui) {
        this.defMainGui = defMainGui;
        jmxClient = defMainGui.getJmxClient();
        initComponents();
        
        CfgInfo cfgInfo = defMainGui.getCfgInfo();
        String hostport = cfgInfo.getProperty(CfgInfo.IP)+":"+cfgInfo.get(CfgInfo.PORT);
        tfHostPort.setText(hostport);
        tfEMail.setText(cfgInfo.getProperty(CfgInfo.EMAIL));
        tfTel.setText(cfgInfo.getProperty(CfgInfo.TEL));
        tfNotifyPeriod.setText(cfgInfo.getProperty(CfgInfo.NOTIFY_PERIOD));
        tfDayChkPeriod.setText(cfgInfo.getProperty(CfgInfo.DAY_CHK_PERIOD));
        tfNightChkPeriod.setText(cfgInfo.getProperty(CfgInfo.NIGHT_CHK_PERIOD));
        tfDayChkTime.setText(cfgInfo.getProperty(CfgInfo.DAY_CHK_TIME));
        tfNightChkTime.setText(cfgInfo.getProperty(CfgInfo.NIGHT_CHK_TIME));
        tfDbErrDef.setText(cfgInfo.getProperty(CfgInfo.DB_ERR_CNT_DEF));
        tfHsmErrDef.setText(cfgInfo.getProperty(CfgInfo.HSM_ERR_CNT_DEF));
        tfApErrDef.setText(cfgInfo.getProperty(CfgInfo.AP_ERR_CNT_DEF));
        tfTxTimeoutCntDef.setText(cfgInfo.getProperty(CfgInfo.TXN_TIMEOUT_CNT_DEF));
        tfTxErrCntDef.setText(cfgInfo.getProperty(CfgInfo.TXN_ERR_CNT_DEF));
        tfMailServer.setText(cfgInfo.getProperty(CfgInfo.MAIL_SERVER));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelHostPort = new javax.swing.JLabel();
        tfHostPort = new javax.swing.JFormattedTextField();
        labelEMail = new javax.swing.JLabel();
        tfEMail = new javax.swing.JFormattedTextField();
        labelTel = new javax.swing.JLabel();
        tfTel = new javax.swing.JFormattedTextField();
        labelNotifyPeriod = new javax.swing.JLabel();
        labelDayChkPeriod = new javax.swing.JLabel();
        tfDayChkPeriod = new javax.swing.JFormattedTextField();
        tfDayChkTime = new javax.swing.JFormattedTextField();
        tfNotifyPeriod = new javax.swing.JFormattedTextField();
        labelDbErrDef = new javax.swing.JLabel();
        tfDbErrDef = new javax.swing.JFormattedTextField();
        labelHsmErrDef = new javax.swing.JLabel();
        tfHsmErrDef = new javax.swing.JFormattedTextField();
        tfApErrDef = new javax.swing.JFormattedTextField();
        labelApErrDef = new javax.swing.JLabel();
        labelTxTimeoutCntDef = new javax.swing.JLabel();
        tfTxTimeoutCntDef = new javax.swing.JFormattedTextField();
        labelTxErrCntDef = new javax.swing.JLabel();
        tfTxErrCntDef = new javax.swing.JFormattedTextField();
        labelDisplay = new javax.swing.JLabel();
        btnConnect = new javax.swing.JToggleButton();
        labelMailServer = new javax.swing.JLabel();
        tfMailServer = new javax.swing.JFormattedTextField();
        labelNightPeriod = new javax.swing.JLabel();
        tfNightChkPeriod = new javax.swing.JFormattedTextField();
        tfNightChkTime = new javax.swing.JFormattedTextField();
        lblTimeS = new javax.swing.JLabel();
        lblTimeE = new javax.swing.JLabel();

        labelHostPort.setText("IP:PORT");

        tfHostPort.setText("localhost:5678");

        labelEMail.setText("EMail");

        labelTel.setText("Mobile");
        labelTel.setPreferredSize(new java.awt.Dimension(42, 15));

        labelNotifyPeriod.setText("Notify Period");
        labelNotifyPeriod.setAlignmentY(0.0F);
        labelNotifyPeriod.setIconTextGap(0);

        labelDayChkPeriod.setText("Check Period (Day)");

        tfDayChkPeriod.setText("300");
        tfDayChkPeriod.setToolTipText("input second value");

        tfDayChkTime.setText("070000");

        tfNotifyPeriod.setText("3600");
        tfNotifyPeriod.setToolTipText("input second value");

        labelDbErrDef.setText("DB Alert Limit");
        labelDbErrDef.setPreferredSize(new java.awt.Dimension(85, 15));

        tfDbErrDef.setText("1");

        labelHsmErrDef.setText("HSM Alert Limit");
        labelHsmErrDef.setPreferredSize(new java.awt.Dimension(85, 15));

        tfHsmErrDef.setText("1");

        tfApErrDef.setText("1");

        labelApErrDef.setText("AP Alert Limit");
        labelApErrDef.setPreferredSize(new java.awt.Dimension(85, 15));

        labelTxTimeoutCntDef.setText("LNG Alert Limit");
        labelTxTimeoutCntDef.setPreferredSize(new java.awt.Dimension(110, 15));

        tfTxTimeoutCntDef.setText("1");

        labelTxErrCntDef.setText("Tx Err Alert Limit");

        tfTxErrCntDef.setText("1");

        labelDisplay.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelDisplay.setText("Display Message Here");

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        labelMailServer.setText("MailServer");

        tfMailServer.setText("tpemail.hyweb.com.tw");

        labelNightPeriod.setText("Check Period (Night)");

        tfNightChkPeriod.setText("3600");
        tfNightChkPeriod.setToolTipText("input second value");

        tfNightChkTime.setText("220000");

        lblTimeS.setText("Time");

        lblTimeE.setText("Time");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelTxErrCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelApErrDef, 0, 0, Short.MAX_VALUE)
                            .add(labelDbErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelNotifyPeriod)
                            .add(labelNightPeriod)
                            .add(labelDayChkPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelTel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelMailServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelEMail, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .add(labelHostPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, tfHostPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, tfTel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, tfMailServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, tfEMail, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(tfNightChkPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .add(tfTxErrCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tfNotifyPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .add(tfDayChkPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tfApErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                    .add(tfDbErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                                .add(111, 111, 111)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                            .add(labelTxTimeoutCntDef, 0, 0, Short.MAX_VALUE)
                                            .add(labelHsmErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(tfTxTimeoutCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, tfHsmErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(lblTimeE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(tfNightChkTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(lblTimeS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(tfDayChkTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(labelDisplay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(149, 149, 149)
                        .add(btnConnect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                        .add(135, 135, 135)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tfHostPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(labelHostPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(labelEMail, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfEMail))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(tfMailServer))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelMailServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(tfTel)
                    .add(labelTel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblTimeS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfDayChkPeriod)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, tfDayChkTime)
                    .add(labelDayChkPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, tfNightChkTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, labelNightPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(lblTimeE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfNightChkPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(tfNotifyPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(labelNotifyPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tfDbErrDef)
                    .add(labelDbErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tfHsmErrDef)
                    .add(labelHsmErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(labelTxTimeoutCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(tfTxTimeoutCntDef)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, labelApErrDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tfApErrDef))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelTxErrCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .add(tfTxErrCntDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                .add(11, 11, 11)
                .add(labelDisplay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnConnect))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        if (jmxClient.getServiceState()==jmxClient.START || jmxClient.getServiceState()==jmxClient.INIT)
        {
            labelDisplay.setText("disconnect...");
            jmxClient.shutdownService();
            btnConnect.setText("connect");
        }
        else
        {
            labelDisplay.setText("connect...");
            CfgInfo cfgInfo = defMainGui.getCfgInfo();
            
            String[] hostport = tfHostPort.getText().split(":");
            cfgInfo.put(CfgInfo.IP, hostport[0]);
            cfgInfo.put(CfgInfo.PORT, hostport[1]);
            
            String email = tfEMail.getText();
            cfgInfo.put(CfgInfo.EMAIL, email);
            
            String tel = tfTel.getText();
            cfgInfo.put(CfgInfo.TEL, tel);
            
            String notifyPeriod = tfNotifyPeriod.getText();
            cfgInfo.put(CfgInfo.NOTIFY_PERIOD, notifyPeriod);
            
            String dayChkPeriod = tfDayChkPeriod.getText();
            cfgInfo.put(CfgInfo.DAY_CHK_PERIOD, dayChkPeriod);

            String nightChkPeriod = tfNightChkPeriod.getText();
            cfgInfo.put(CfgInfo.DAY_CHK_PERIOD, nightChkPeriod);

            String dayChkTime = tfDayChkTime.getText();
            cfgInfo.put(CfgInfo.DAY_CHK_TIME, dayChkTime);
            
            String nightChkTime = tfNightChkTime.getText();
            cfgInfo.put(CfgInfo.NIGHT_CHK_TIME, nightChkTime);
            
            String dbErrCntDef = tfDbErrDef.getText();
            cfgInfo.put(CfgInfo.DB_ERR_CNT_DEF, dbErrCntDef);
            
            String hsmErrCntDef = tfHsmErrDef.getText();
            cfgInfo.put(CfgInfo.HSM_ERR_CNT_DEF, hsmErrCntDef);
            
            String apErrCntDef = tfApErrDef.getText();
            cfgInfo.put(CfgInfo.AP_ERR_CNT_DEF, apErrCntDef);
            
            String txTimeoutCntDef = tfTxTimeoutCntDef.getText();
            cfgInfo.put(CfgInfo.TXN_TIMEOUT_CNT_DEF, txTimeoutCntDef);
            
            String txErrCntDef = tfTxErrCntDef.getText();
            cfgInfo.put(CfgInfo.TXN_ERR_CNT_DEF, txErrCntDef);
            cfgInfo.store();
            jmxClient.setHostport(tfHostPort.getText());
            jmxClient.initService();
            jmxClient.startService();
            labelDisplay.setText("connect ok!");
            btnConnect.setText("disconnect");
        }
        
    }//GEN-LAST:event_btnConnectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JLabel labelApErrDef;
    private javax.swing.JLabel labelDayChkPeriod;
    private javax.swing.JLabel labelDbErrDef;
    private javax.swing.JLabel labelDisplay;
    private javax.swing.JLabel labelEMail;
    private javax.swing.JLabel labelHostPort;
    private javax.swing.JLabel labelHsmErrDef;
    private javax.swing.JLabel labelMailServer;
    private javax.swing.JLabel labelNightPeriod;
    private javax.swing.JLabel labelNotifyPeriod;
    private javax.swing.JLabel labelTel;
    private javax.swing.JLabel labelTxErrCntDef;
    private javax.swing.JLabel labelTxTimeoutCntDef;
    private javax.swing.JLabel lblTimeE;
    private javax.swing.JLabel lblTimeS;
    private javax.swing.JFormattedTextField tfApErrDef;
    private javax.swing.JFormattedTextField tfDayChkPeriod;
    private javax.swing.JFormattedTextField tfDayChkTime;
    private javax.swing.JFormattedTextField tfDbErrDef;
    private javax.swing.JFormattedTextField tfEMail;
    private javax.swing.JFormattedTextField tfHostPort;
    private javax.swing.JFormattedTextField tfHsmErrDef;
    private javax.swing.JFormattedTextField tfMailServer;
    private javax.swing.JFormattedTextField tfNightChkPeriod;
    private javax.swing.JFormattedTextField tfNightChkTime;
    private javax.swing.JFormattedTextField tfNotifyPeriod;
    private javax.swing.JFormattedTextField tfTel;
    private javax.swing.JFormattedTextField tfTxErrCntDef;
    private javax.swing.JFormattedTextField tfTxTimeoutCntDef;
    // End of variables declaration//GEN-END:variables

}
