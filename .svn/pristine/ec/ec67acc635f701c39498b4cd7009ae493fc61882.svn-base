/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * *********************************************** 
 */
package tw.com.hyweb.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import tw.com.hyweb.admin.JmxClient;
import tw.com.hyweb.util.ISOUtil;

/**
 * NetworkPanel
 */
public class NetworkPanel extends JPanel
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(NetworkPanel.class);

    private JButton heartBeatButton;
    private JButton echoButton;
    private JButton sigOffButton;
    private JButton sigOnButton;
    private JLabel msgLabel;
    
    private DefMainGui defMainGui;
    private JmxClient jmxClient;
    private String netBeanName = "bean:name=NetEventHandler";
    private String netBeanMethod = "sendNetworkMgmt=";
    String[] jmxCmd = new String[]{jmxClient.getHostport(),netBeanName,netBeanMethod+"301"};
        
    /**
     * Constructor
     * @param jmxClient
     */
    public NetworkPanel(DefMainGui defMainGui)
    {
        super();
        jmxClient = defMainGui.getJmxClient();
        setLayout(new BorderLayout());
        initComponents();
    }
    
    private void initComponents()
    {
        heartBeatButton = new JButton();
        echoButton = new JButton();
        sigOnButton = new JButton();
        sigOffButton = new JButton();

        msgLabel = new JLabel();
        
        heartBeatButton.setText("heart beat");
        heartBeatButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                heartBeatButtonActionPerformed(evt);
            }
        });
        
        echoButton.setText("echo");
        echoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                echoButtonActionPerformed(evt);
            }
        });

        sigOnButton.setText("sign on");
        sigOnButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                sigOnButtonActionPerformed(evt);
            }
        });

        sigOffButton.setText("sign off");
        sigOffButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                sigOffButtonActionPerformed(evt);
            }
        });

        JPanel flowBtnPanel = new JPanel(new FlowLayout());
        flowBtnPanel.add(echoButton);
        flowBtnPanel.add(sigOnButton);
        flowBtnPanel.add(sigOffButton);
        flowBtnPanel.add(heartBeatButton);

        JPanel txtPanel = new JPanel();
        txtPanel.add(msgLabel);

        add(flowBtnPanel, BorderLayout.NORTH);
        add(txtPanel, BorderLayout.CENTER);

    }
    private void heartBeatButtonActionPerformed(ActionEvent evt)
    {
        msgLabel.setText("send echo message.");
        jmxCmd[2]=netBeanMethod+"301";
        new Thread()
        {
            public void run()
            {
                for(int i=0,failCnt=0;;i++)
                {
                    msgLabel.setText("send..."+System.currentTimeMillis());
                    try
                    {
                        Object restult = jmxClient.execute(jmxCmd);
                        msgLabel.setText("receive..."+restult+"..."+System.currentTimeMillis());
                    }
                    catch(Exception e)
                    {
                        failCnt++;
                        msgLabel.setText("fail cnt="+failCnt);
                        logger.warn("heart beat fail count:"+failCnt);
                    }
                    if (failCnt==3)
                    {
                        break;
                    }
                    
                    ISOUtil.sleep(10000);
                }
                msgLabel.setText("heart beat is stop...");
                jmxClient.stopService();            
                String ret = defMainGui.mailto();
                msgLabel.setText(ret);
            }
        }.start();
    }
       
    private void echoButtonActionPerformed(ActionEvent evt)
    {
        msgLabel.setText("send echo message.");
        jmxCmd[2]=netBeanMethod+"301";
        try
        {
            Object obj = jmxClient.execute(jmxCmd);
            if (obj==null)
            {
                msgLabel.setText("no response return.");    
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgLabel.setText(e.getMessage());
            jmxClient.stopService();
        }
    }

    private void sigOnButtonActionPerformed(ActionEvent evt)
    {
        msgLabel.setText("send sign on message.");
        jmxCmd[2]=netBeanMethod+"001";
        try
        {
            Object obj = jmxClient.execute(jmxCmd);
            if (obj==null)
            {
                msgLabel.setText("no response return.");    
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgLabel.setText(e.getMessage());
            jmxClient.stopService();
        }
    }

    private void sigOffButtonActionPerformed(ActionEvent evt)
    {
        msgLabel.setText("send sign off message.");
        jmxCmd[2]=netBeanMethod+"002";
        try
        {
            Object obj = jmxClient.execute(jmxCmd);
            if (obj==null)
            {
                msgLabel.setText("no response return.");    
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgLabel.setText(e.getMessage());
            jmxClient.stopService();
        }
    }
}
