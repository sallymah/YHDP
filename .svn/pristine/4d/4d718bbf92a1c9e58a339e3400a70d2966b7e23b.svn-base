/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MonPanel.java
 *
 * Created on 2010/6/8, 10:45:47
 */

package tw.com.hyweb.gui;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import tw.com.hyweb.admin.JmxClient;
import tw.com.hyweb.online.mbean.AgentMBean;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.ISODate;

/**
 *
 * @author SamSon
 */
public class MonPanel extends javax.swing.JPanel {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MonPanel.class);

    private DefMainGui defMainGui;
    private JmxClient jmxClient;
    private Timer timer;
    private MonTask dayMonTask;
    private MonTask nightMonTask;

    /**
     * Creates new form MonPanel.
     *
     * @param defMainGui the def main gui
     */
    public MonPanel(DefMainGui defMainGui) {
        this.defMainGui = defMainGui;
        jmxClient = defMainGui.getJmxClient();
        initComponents();
        timer = new Timer();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelAp = new javax.swing.JLabel();
        labelDb = new javax.swing.JLabel();
        labelHsm = new javax.swing.JLabel();
        labelApStatus = new javax.swing.JLabel();
        labelDbStatus = new javax.swing.JLabel();
        labelHsmStatus = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        taMsg = new javax.swing.JTextArea();
        labelLastTxnTime = new javax.swing.JLabel();
        labelLastChkTime = new javax.swing.JLabel();
        labelLastNotifyTime = new javax.swing.JLabel();
        btnToggle = new javax.swing.JToggleButton();

        labelAp.setText("Application Server Status");

        labelDb.setText("Database Connection Status");

        labelHsm.setText("HSM Connection Status");

        labelApStatus.setText("OK");

        labelDbStatus.setText("OK");

        labelHsmStatus.setText("OK");

        taMsg.setColumns(20);
        taMsg.setRows(5);
        jScrollPane1.setViewportView(taMsg);

        labelLastTxnTime.setText("MM-dd HH:mm:ss");

        labelLastChkTime.setText("MMdd HH:mm:ss");
        labelLastChkTime.setMaximumSize(new java.awt.Dimension(87, 15));
        labelLastChkTime.setMinimumSize(new java.awt.Dimension(87, 15));
        labelLastChkTime.setPreferredSize(new java.awt.Dimension(87, 15));

        labelLastNotifyTime.setText("MM-dd HH:mm:ss");

        btnToggle.setText("Begin");
        btnToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToggleActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, labelAp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, labelDb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, labelHsm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 187, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(labelApStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labelDbStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labelHsmStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(labelLastChkTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(labelLastNotifyTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                                .add(17, 17, 17)
                                .add(labelLastTxnTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(25, 25, 25)
                                .add(btnToggle)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelAp)
                    .add(labelApStatus))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelDb)
                    .add(labelDbStatus))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelHsm)
                    .add(labelHsmStatus))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnToggle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 9, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelLastChkTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(labelLastTxnTime)
                    .add(labelLastNotifyTime))
                .addContainerGap())
        );

        labelLastTxnTime.getAccessibleContext().setAccessibleName("MMdd HH:mm:ss");
        labelLastNotifyTime.getAccessibleContext().setAccessibleName("MMdd HH:mm:ss");
    }// </editor-fold>//GEN-END:initComponents


    private String[] successRcode=new String[]{"0000"};
    
    private void doMonitor()
    {
        CfgInfo cfgInfo = this.defMainGui.getCfgInfo();
        long lastTxnTime=Long.parseLong(cfgInfo.getProperty(CfgInfo.LAST_TXN_TIME));
        long lastNotifyTime=Long.parseLong(cfgInfo.getProperty(CfgInfo.LAST_NOTIFY_TIME));
        long lastChkTime=Long.parseLong(cfgInfo.getProperty(CfgInfo.LAST_CHK_TIME));
        
        int notifyPeriod=cfgInfo.getInt(CfgInfo.NOTIFY_PERIOD);
        
        int txnTimeoutValDef = cfgInfo.getInt(CfgInfo.TXN_TIMEOUT_VAL_DEF);
        int txnLongTimeIdleValDef = cfgInfo.getInt(CfgInfo.TXN_LONGTIMEIDLE_VAL_DEF);
        
        int apErrCntDef = cfgInfo.getInt(CfgInfo.AP_ERR_CNT_DEF);
        int txnErrCntDef = cfgInfo.getInt(CfgInfo.TXN_ERR_CNT_DEF);
        int txnTimeoutCntDef = cfgInfo.getInt(CfgInfo.TXN_TIMEOUT_CNT_DEF);
        int dbErrCntDef = cfgInfo.getInt(CfgInfo.DB_ERR_CNT_DEF);
        int hsmErrCntDef = cfgInfo.getInt(CfgInfo.HSM_ERR_CNT_DEF);

        int apErrCnt = cfgInfo.getInt(CfgInfo.AP_ERR_CNT,"0");
        int txnErrCnt = cfgInfo.getInt(CfgInfo.TXN_ERR_CNT,"0");
        int txnTimeoutCnt = cfgInfo.getInt(CfgInfo.TXN_TIMEOUT_CNT,"0");
        int dbErrCnt = cfgInfo.getInt(CfgInfo.DB_ERR_CNT,"0");
        int hsmErrCnt = cfgInfo.getInt(CfgInfo.HSM_ERR_CNT,"0");
         
        
        if (cfgInfo.containsKey(CfgInfo.SUCCESS_RCODE))
        {
            String rcodeList = cfgInfo.getProperty(CfgInfo.SUCCESS_RCODE);
            if (rcodeList!=null)
            {
                successRcode=ArraysUtil.toStrArray(rcodeList);
            }
        }
        
        String apErrMsgDef=cfgInfo.getProperty(CfgInfo.AP_ERR_MSG_DEF);//"Online程式最後%s筆超過%s筆逾時無回應.";
        String dbErrMsgDef=cfgInfo.getProperty(CfgInfo.DB_ERR_MSG_DEF);//"DB最後%s筆超過%s筆逾時無回應.";
        String hsmErrMsgDef=cfgInfo.getProperty(CfgInfo.HSM_ERR_MSG_DEF);//"HSM最後%s筆超過%s筆逾時無回應.";
        String txnErrMsgDef=cfgInfo.getProperty(CfgInfo.TXN_ERR_MSG_DEF);//"最後%s筆超過%s筆錯誤交易.";
        String timeoutMsgDef = cfgInfo.getProperty(CfgInfo.TIMEOUT_ERR_MSG_DEF);//"最後%s筆交易Timeout超過%s筆.";
        String idleMsgDef = cfgInfo.getProperty(CfgInfo.IDLE_ERR_MSG_DEF);//"長時間沒有交易訊息:%s.";
        
        int cacheSize=10;
        
        String inqBeanName = "bean:name=AgentMBean";
        String inqBeanMethod = "queryStatus";
        String[] jmxCmd = new String[]{jmxClient.getHostport(),inqBeanName,inqBeanMethod};
        StringBuffer notifyMsg = new StringBuffer();
        try
        {
            String data="{}";
            try
            {
                if (!jmxClient.isConnected())
                {
                    jmxClient.startService();
                }
                Object obj = jmxClient.execute(jmxCmd);
                if (obj==null)
                {
                    taMsg.setText("no response return.");
                    data = "{"+AgentMBean.AP_ERR+":true}";
                }
                else
                {
                    data = (String)obj;
                }
            }
            catch(IOException ioe)
            {
                taMsg.setText("no response return.");
                data = "{"+AgentMBean.AP_ERR+":true}";
                logger.error(ioe);
                jmxClient.close();
            }
            //taMsg.setText(data);
            JSONObject json = JSONObject.fromObject(data);

            boolean isApErr = json.containsKey(AgentMBean.AP_ERR)?json.getBoolean(AgentMBean.AP_ERR):false;
            boolean isDbErr = json.containsKey(AgentMBean.DB_ERR)?json.getBoolean(AgentMBean.DB_ERR):false;;
            boolean isHsmErr = json.containsKey(AgentMBean.HSM_ERR)?json.getBoolean(AgentMBean.HSM_ERR):false;;
            labelApStatus.setText(isApErr?"Fail":"OK");
            labelDbStatus.setText(isDbErr?"Fail":"OK");
            labelHsmStatus.setText(isHsmErr?"Fail":"OK");
            
            String apErrMsg=null;
            String dbErrMsg=null;
            String hsmErrMsg=null;
            String txnErrMsg=null;
            String timeoutMsg=null;
            String idleMsg=null;
            if (isApErr)
            {
                apErrCnt++;
                if (apErrCntDef>=0 && apErrCnt>=apErrCntDef)
                {
                    //notify
                    apErrMsg = String.format(apErrMsgDef, cacheSize,apErrCntDef);
                }
            }
            else
            {
                apErrCnt=0;
            }

            if (isDbErr)
            {
                dbErrCnt++;
                if (dbErrCntDef>=0 && dbErrCnt>=dbErrCntDef)
                {
                    //notify
                    dbErrMsg=String.format(dbErrMsgDef,cacheSize,dbErrCntDef);
                }
            }
            else
            {
                dbErrCnt=0;
            }
            
            if (isHsmErr)
            {
                hsmErrCnt++;
                if (hsmErrCntDef>=0 && hsmErrCnt>=hsmErrCntDef)
                {
                    //notify
                    hsmErrMsg=String.format(hsmErrMsgDef, cacheSize,hsmErrCntDef);
                }
            }
            else
            {
                hsmErrCnt=0;
            }

            Date curDate = new Date();
            long curTime = curDate.getTime();
            lastChkTime = curTime;
            
            StringBuffer strb = new StringBuffer();
            JSONArray jsonArr = json.containsKey(AgentMBean.TX_DATA)?json.getJSONArray(AgentMBean.TX_DATA):null;
            if (jsonArr!=null)
            {
                int size = jsonArr.size();
                cacheSize = size;
                for (int i =size-1;i>=0;i--)
                {
                    HashMap map = (HashMap)JSONObject.toBean(jsonArr.getJSONObject(i),HashMap.class);
                    long txTime = (Long) map.get(AgentMBean.TX_TIME);
                    int resptime = (Integer) map.get(AgentMBean.TX_RESPTIME);
                    String rcode = (String)map.get(AgentMBean.TX_RCODE);
                    strb.append(" Time:").append(txTime);
                    strb.append(" RespTime:").append(resptime);
                    strb.append(" rcode:").append(rcode);
                    strb.append("\n");
                    if (rcode.equals("0000") || (ArraysUtil.find(successRcode, rcode)>=0))
                    {
                        txnErrCnt=0;
                    }
                    else
                    {
                        txnErrCnt++;
                        if (txnErrCntDef>=0 && txnErrCnt>=txnErrCntDef && txnErrMsg==null)
                        {
                            //notify
                            txnErrMsg=String.format(txnErrMsgDef,cacheSize,txnErrCntDef);
                        }
                    }
    
                    if (resptime>=0 && resptime < txnTimeoutValDef*1000)
                    {//value < 0 means timeout
                        txnTimeoutCnt=0;
                    }
                    else
                    {
                        txnTimeoutCnt++;
                        if (txnTimeoutCntDef>=0 && txnTimeoutCnt>=txnTimeoutCntDef && timeoutMsg==null)
                        {
                            //notify
                            timeoutMsg=String.format(timeoutMsgDef, cacheSize,txnTimeoutCntDef);
                        }
                    }
    
                    if (i ==size-1)
                    {
                        lastTxnTime = txTime;
                        if (txnLongTimeIdleValDef >=0 && curTime-lastTxnTime > txnLongTimeIdleValDef*1000)
                        {
                            //notify
                            idleMsg = String.format(idleMsgDef, ISODate.getDateTime(new Date(lastTxnTime)));
                        }
                    }
                }
            }

            if (apErrMsg!=null)
            {
                notifyMsg.append(apErrMsg);
            }
            if (dbErrMsg!=null)
            {
                notifyMsg.append(dbErrMsg);
            }
            if (hsmErrMsg!=null)
            {
                notifyMsg.append(hsmErrMsg);
            }
            if (txnErrMsg!=null)
            {
                notifyMsg.append(txnErrMsg);
            }
            if (timeoutMsg!=null)
            {
                notifyMsg.append(timeoutMsg);
            }
            if (idleMsg!=null)
            {
                notifyMsg.append(idleMsg);
            }
            taMsg.setText(taMsg.getText()+String.format("%n")+notifyMsg.toString());
            cfgInfo.put(CfgInfo.LAST_CHK_TIME, ""+lastChkTime);
            cfgInfo.put(CfgInfo.LAST_TXN_TIME, ""+lastTxnTime);
            cfgInfo.put(CfgInfo.LAST_NOTIFY_TIME, ""+lastNotifyTime);           
            cfgInfo.put(CfgInfo.AP_ERR_CNT, ""+apErrCnt);
            cfgInfo.put(CfgInfo.TXN_ERR_CNT, ""+txnErrCnt);
            cfgInfo.put(CfgInfo.TXN_TIMEOUT_CNT, ""+txnTimeoutCnt);
            cfgInfo.put(CfgInfo.DB_ERR_CNT, ""+dbErrCnt);
            cfgInfo.put(CfgInfo.HSM_ERR_CNT, ""+hsmErrCnt);
            
            labelLastTxnTime.setText("lastTxn:"+ISODate.formatDate(new Date(lastTxnTime),"MM/dd HH:mm:SS"));
            labelLastChkTime.setText("lastChk:"+ISODate.formatDate(curDate,"MM/dd HH:mm:SS"));
            //labelLastNotifyTime.setText("lastNotify:"+ISODate.formatDate(new Date(lastNotifyTime),"MM/dd HH:mm:SS"));
            
            if (notifyMsg!=null && notifyMsg.length()>0)
            {//reset after notify
                long now = System.currentTimeMillis();
                if (now-lastNotifyTime<notifyPeriod*1000)
                {
                    taMsg.setText("already notified in past "+notifyPeriod+" second.");
                    return;
                }
                lastNotifyTime=now;
                labelLastNotifyTime.setText("lastNty:"+ISODate.formatDate(new Date(lastNotifyTime),"MM/dd HH:mm:SS"));

                String mailto = cfgInfo.getProperty(CfgInfo.EMAIL);
                defMainGui.getNotifyMail().mailto(mailto, notifyMsg.toString());
                defMainGui.getNotifySms().doNotify(notifyMsg.toString());
                
                apErrMsg=null;
                dbErrMsg=null;
                hsmErrMsg=null;
                txnErrMsg=null;
                timeoutMsg=null;
                idleMsg=null;
                apErrCnt=0;
                txnErrCnt=0;
                txnTimeoutCnt=0;
                dbErrCnt=0;
                hsmErrCnt=0; 
                notifyMsg.delete(0, notifyMsg.length());
                cfgInfo.put(CfgInfo.LAST_CHK_TIME, "0");
                cfgInfo.put(CfgInfo.LAST_TXN_TIME, "0");
                cfgInfo.put(CfgInfo.LAST_NOTIFY_TIME, "0");
                cfgInfo.put(CfgInfo.AP_ERR_CNT, "0");
                cfgInfo.put(CfgInfo.TXN_ERR_CNT, "0");
                cfgInfo.put(CfgInfo.TXN_TIMEOUT_CNT, "0");
                cfgInfo.put(CfgInfo.DB_ERR_CNT, "0");
                cfgInfo.put(CfgInfo.HSM_ERR_CNT, "0");
            }
             
        }
        catch (Exception e)
        {
            logger.error(e);
            taMsg.setText(e.getMessage());
            //jmxClient.stopService();
        }        
    }

    class MonTask extends TimerTask
    {
        int istart;
        int iend;
        public MonTask(String start,String end)
        {
            istart = Integer.parseInt(start);
            iend = Integer.parseInt(end);
        }

        /* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            Date curDate = new Date();
            int icur = Integer.parseInt(ISODate.formatDate(curDate, "HHmmss"));
            if (iend>=istart)
            {//day
                if (icur>=istart && icur<=iend)
                {//do it
                    logger.debug("day task doing...");
                    doMonitor();
                }
                else
                {//return
                    logger.debug("day task return");
                }
            }
            else
            {//night
                if (icur>=iend && icur<=istart)
                {//return
                    logger.debug("night task return");
                }
                else
                {//do it
                    logger.debug("night task doing...");
                    doMonitor();
                }
            }
        }
    }

    private void btnToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToggleActionPerformed
        if (btnToggle.isSelected())
        {
            startMonitor();
            btnToggle.setText("End");
        }
        else
        {
            stopMonitor();
            btnToggle.setText("Start");
            taMsg.setText("");
            labelApStatus.setText("N/A");
            labelDbStatus.setText("N/A");
            labelHsmStatus.setText("N/A");
        }
    }//GEN-LAST:event_btnToggleActionPerformed

    /**
     * Start monitor.
     */
    public void startMonitor()
    {
        String dayChkTime = defMainGui.getCfgInfo().getProperty(CfgInfo.DAY_CHK_TIME);
        String nightChkTime = defMainGui.getCfgInfo().getProperty(CfgInfo.NIGHT_CHK_TIME);
        dayMonTask = new MonTask(dayChkTime, nightChkTime);
        nightMonTask = new MonTask(nightChkTime, dayChkTime);
        int dayChkPeriod = defMainGui.getCfgInfo().getInt(CfgInfo.DAY_CHK_PERIOD);
        int nightChkPeriod = defMainGui.getCfgInfo().getInt(CfgInfo.NIGHT_CHK_PERIOD);
        timer.schedule(dayMonTask, 0, dayChkPeriod * 1000);
        timer.schedule(nightMonTask, 0, nightChkPeriod * 1000);
    }
    
    /**
     * Stop monitor.
     */
    public void stopMonitor()
    {
        if (dayMonTask != null)
        {
            dayMonTask.cancel();
            dayMonTask = null;
        }
        if (nightMonTask != null)
        {
            nightMonTask.cancel();
            nightMonTask = null;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnToggle;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelAp;
    private javax.swing.JLabel labelApStatus;
    private javax.swing.JLabel labelDb;
    private javax.swing.JLabel labelDbStatus;
    private javax.swing.JLabel labelHsm;
    private javax.swing.JLabel labelHsmStatus;
    private javax.swing.JLabel labelLastChkTime;
    private javax.swing.JLabel labelLastNotifyTime;
    private javax.swing.JLabel labelLastTxnTime;
    private javax.swing.JTextArea taMsg;
    // End of variables declaration//GEN-END:variables

}
