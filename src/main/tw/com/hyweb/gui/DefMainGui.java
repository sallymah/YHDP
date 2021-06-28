/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.hyweb.gui;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.apache.log4j.xml.DOMConfigurator;
import org.jpos.iso.ISOUtil;

import tw.com.hyweb.admin.JmxClient;
import tw.com.hyweb.util.SystemUtil;

/**
 * The Class DefMainGui.
 *
 * @author user
 */
public class DefMainGui extends JFrame
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(DefMainGui.class);

    private JTabbedPane jTabbedPane1;
    private ConfigPanel configPanel;
    private MonPanel monPanel;
    private NetPanel networkPanel;
    private SimulatorPanel simPanel;

    private JmxClient jmxClient = new JmxClient();
    private CfgInfo cfgInfo = new CfgInfo(); 
    private NotifyMail notifyMail = new NotifyMail(cfgInfo);
    private NotifySms notifySms = new NotifySms(cfgInfo, cfgInfo.getProperty(CfgInfo.TEL));

    private static String apName = "mon";
    private static String configDir = "config";
    private static String apDirPath = configDir + File.separator + apName + File.separator;
    
    /** Creates new form NewApplication */
    public DefMainGui()
    {
        initResource();
        initComponents();
    }

    private void initComponents()
    {
//        jmxClient.initService();
//        jmxClient.startService();
        
        jTabbedPane1 = new JTabbedPane();
        configPanel = new ConfigPanel(this);
        monPanel = new MonPanel(this);
        networkPanel = new NetPanel(this);
        simPanel = new SimulatorPanel(this);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosed(WindowEvent we)
            {
                shutdown();
            }
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
        jTabbedPane1.addTab("monitor config", configPanel);
        jTabbedPane1.addTab("monitor", monPanel);
        jTabbedPane1.addTab("network", networkPanel);
        jTabbedPane1.addTab("simulator", simPanel);
        pack();
    }
    
    protected void shutdown()
    {
        System.out.println("shutdown win.");
        monPanel.stopMonitor();
        jmxClient.shutdownService();
    }
    
    /**
     * shutdown service while Ctrl + C is press
     */
    private void shoudownHook()
    {
        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
    }

    /**
     * ShutdownHookThread:
     *
     * @author user
     */
    private class ShutdownHookThread extends Thread
    {
        /**
         * Logger for this class
         */
        private final Logger logger = Logger.getLogger(ShutdownHookThread.class);

        /**
         * default constructor
         */
        public ShutdownHookThread()
        {
            super("ShutdownHookThread");
        }

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            shutdown();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        if (args!=null && args.length>0 && args[0].equalsIgnoreCase("-noui"))
        {
            DefMainGui gui = new DefMainGui();
            String hostport = gui.cfgInfo.getProperty(CfgInfo.IP, "localhost")+":"+gui.cfgInfo.getProperty(CfgInfo.PORT, "5678");
            gui.jmxClient.setHostport(hostport);
            gui.shoudownHook();
            gui.jmxClient.initService();
            for (int i=0;i>=0;i++)
            { 
                gui.jmxClient.startService();
                if (gui.jmxClient.isConnected())
                {
                    gui.monPanel.startMonitor();
                    break;
                }
                else
                {
                    logger.debug("cannot connect to "+hostport);
                    gui.jmxClient.close();
                }
                ISOUtil.sleep(60000);
            }
        }
        else
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    DefMainGui gui = new DefMainGui();
                    gui.setSize(555, 500);
                    gui.setVisible(true);
                }
            });
        }
    }
    
    /**
     * create dirs , config log and load properties from application dir
     */
    protected static void initResource()
    {

        // 設定log4j設定
        String logCfgFile = apDirPath + "log4j.xml";
        initLog4j(logCfgFile);
    }

    /**
     * config log4j and monitor it's config file
     *
     * @param file log4j configuration file
     */
    protected static void initLog4j(String file)
    {
        boolean isRun = SystemUtil.getBoolean("log4j.init", true);
        if (!isRun)
        {//fot batch mediator call online時避免initialize log4j兩次而蓋過batch設定
            return;
        }

        String cfgstr = System.getProperty("log4j.configuration");
        if (cfgstr == null || !cfgstr.equalsIgnoreCase(file))
        {
            // 設定log4j固定時間監控設定檔是否變更
            DOMConfigurator.configureAndWatch(file, 60000);
            System.setProperty("log4j.configuration", file);
        }
    }
    /**
     * @return 傳回 apName。
     */
    public static final String getApName()
    {
        return apName;
    }

    /**
     * @return 傳回 apDirPath。
     */
    public static final String getApDirPath()
    {
        return apDirPath;
    }
    /**
     * Mailto.
     *
     * @return the string
     */
    public String mailto()
    {
        String ret = "";
        
        String mailServer = cfgInfo.getProperty(CfgInfo.MAIL_SERVER);//"tpemail.hyweb.com.tw";
        String from = cfgInfo.getProperty(CfgInfo.EMAIL);//"samson@tpe.hyweb.com.tw";
        String to = cfgInfo.getProperty(CfgInfo.EMAIL);//"samson@tpe.hyweb.com.tw";
        String subject = "heart beat stop...";
        String messageText = "<h1>please check the server status...</h1>";
        Properties prop = new Properties();
        prop.put("mail.host",mailServer);
        prop.put("mail.transport.protocol","smtp");   
        Session mailSession = Session.getDefaultInstance(prop,null);
        Message msg = new MimeMessage(mailSession);
        try
        {
            if (from!=null)
            {
                msg.setFrom(new InternetAddress(from));
            }

            InternetAddress[] address = InternetAddress.parse(to,false);
            msg.setRecipients(Message.RecipientType.TO, address);

            msg.setSubject(subject);

            msg.setSentDate(new Date());

            //msg.setText(messageText);
            msg.setContent(messageText,"text/html" + ";charset=big5");

            Transport.send(msg);
            ret = "send mail successful.";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            ret = "send mail fail";
        }
        return ret;
    }
    
    /**
     * @return the configPanel
     */
    public JPanel getConfigPanel()
    {
        return this.configPanel;
    }

    /**
     * @param configPanel the configPanel to set
     */
    public void setConfigPanel(ConfigPanel configPanel)
    {
        this.configPanel = configPanel;
    }

    /**
     * @return the jmxClient
     */
    public JmxClient getJmxClient()
    {
        return this.jmxClient;
    }

    /**
     * @param jmxClient the jmxClient to set
     */
    public void setJmxClient(JmxClient jmxClient)
    {
        this.jmxClient = jmxClient;
    }

    /**
     * @return the tbMonTxnInfo
     */
    public CfgInfo getCfgInfo()
    {
        return this.cfgInfo;
    }

    /**
     * @param cfgInfo the cfgInfo to set
     */
    public void setCfgInfo(CfgInfo cfgInfo)
    {
        this.cfgInfo = cfgInfo;
    }

    /**
     * @return the notifyMail
     */
    public NotifyMail getNotifyMail()
    {
        return this.notifyMail;
    }

    /**
     * @param notifyMail the notifyMail to set
     */
    public void setNotifyMail(NotifyMail notifyMail)
    {
        this.notifyMail = notifyMail;
    }

    /**
     * @return the notifySms
     */
    public NotifySms getNotifySms()
    {
        return this.notifySms;
    }

    /**
     * @param notifySms the notifySms to set
     */
    public void setNotifySms(NotifySms notifySms)
    {
        this.notifySms = notifySms;
    }
}
