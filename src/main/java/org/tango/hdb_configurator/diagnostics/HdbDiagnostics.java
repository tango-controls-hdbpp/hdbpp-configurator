//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package org.tango.hdb_configurator.diagnostics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorHistory;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;
import org.tango.hdb_configurator.common.Strategy;
import org.tango.hdb_configurator.configurator.HdbConfigurator;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//=======================================================
/**
 *	JFrame Class to display info
 *
 * @author  Pascal Verdier
 */
//=======================================================
@SuppressWarnings("MagicConstant")
public class HdbDiagnostics extends JFrame {

    private DeviceProxy   configuratorProxy;
    private SubscriberMap subscriberMap;
    private SubscriberMenu subscriberMenu = new SubscriberMenu();
    private List<String> labels;
    private ScalarTableViewer subscriberTableViewer;
    private JFrame parent;
    private int  statisticsTimeWindow;
    private long statisticsResetTime;
    private ErrorHistory errorHistory = new ErrorHistory();
    private boolean buildSubscriberMap;
    private ServerInfoTable serverInfoTable = null;
    private HdbConfigurator hdbConfigurator = null;
    private boolean fromExternal = true;

    private static final String[] ATTRIBUTES = {
            "AttributeNokNumber",
            "AttributeStartedNumber",
            "AttributePausedNumber",
            "AttributeStoppedNumber",
            "AttributePendingNumber",
            "AttributeRecordFreq",
            "AttributeFailureFreq",
            "Context",
    };
    private static final String[] columnNames = {
            "Faulty", "Started", "Paused", "Stopped", "Pending", "Freq.", "Failures", "Context",
    };
    private static final int[] columnWiths = {
            60, 60, 60, 60, 60, 60, 60, 80
    };
	//=======================================================
    /**
	 *	Creates new form HdbDiagnostics
	 */
	//=======================================================
    public HdbDiagnostics(JFrame parent) throws DevFailed {
        this(parent, null);
        if (parent instanceof  HdbConfigurator)
            hdbConfigurator = (HdbConfigurator) parent;
        fromExternal = false;
    }
	//=======================================================
	//=======================================================
    public HdbDiagnostics(JFrame parent, SubscriberMap subscriberMap) throws DevFailed {
        this.parent = parent;
        this.subscriberMap = subscriberMap;
        SplashUtils.getInstance().startSplash();
        SplashUtils.getInstance().increaseSplashProgress(10, "Building GUI");
        setTitle(Utils.getInstance().getApplicationName());

        buildSubscriberMap = (subscriberMap==null);
        initComponents();
        initOwnComponents();

        //  Build Title
        if (buildSubscriberMap) {
            String title=titleLabel.getText();
            String archiveName=TangoUtils.getArchiveName(configuratorProxy);
            if (!archiveName.isEmpty()) {
                title+="  (" + archiveName + ")";
                titleLabel.setText(title);

                //  Check if extraction available
                String s=System.getenv("HdbExtraction");
                if (s != null && s.equals("true"))
                    System.setProperty("HDB_TYPE", archiveName);
            }
        }
        else
            titleLabel.setText("HDB++ Subscribers");
        ImageIcon icon = Utils.getInstance().getIcon("hdb++.gif", 0.75);
        titleLabel.setIcon(icon);
        setIconImage(icon.getImage());

        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
        SplashUtils.getInstance().stopSplash();
	}
    //=======================================================
    //=======================================================
    private void initOwnComponents() throws DevFailed {

        if (buildSubscriberMap) {
            configuratorProxy=Utils.getConfiguratorProxy();

            //  Get subscriber labels if any
            subscriberMap=Utils.getSubscriberMap(configuratorProxy.name(), true);
            if (subscriberMap.size() == 0) {
                SplashUtils.getInstance().stopSplash();
                ErrorPane.showErrorMessage(this, null,
                        new Exception("No subscriber registered for manager " + configuratorProxy.name()));
                System.exit(0);
            }
        }
        labels = subscriberMap.getLabelList();
        //  And get subscriber device names
        List<String> deviceNames = new ArrayList<>();
        for (String label : labels) {
            deviceNames.add( TangoUtils.getOnlyDeviceName(
                    subscriberMap.getSubscriberByLabel(label).name));
        }

        //  Set the frequency column label
        if (labels.size()>0) {
            //  Get the duration from first subscriber
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(labels.get(0));
            statisticsTimeWindow = subscriber.getStatisticsTimeWindow();
            columnNames[RECORD_FREQUENCY] = "ev/";
            columnNames[FAILURE_FREQUENCY] = "Fail./";
            if (statisticsTimeWindow==1) {
                columnNames[RECORD_FREQUENCY] += "sec";
                columnNames[FAILURE_FREQUENCY] += "sec";
            }
            else {
                columnNames[RECORD_FREQUENCY] += Utils.strPeriod(statisticsTimeWindow);
                columnNames[FAILURE_FREQUENCY] += Utils.strPeriod(statisticsTimeWindow);
            }

            statisticsResetTime = subscriber.getStatisticsResetTime();
        }

        //  A list of lines (a line per device)
        List<String[]> attributeNames = new ArrayList<>();
        for (String deviceName : deviceNames) {
            // Build an array with attribute names
            String[]    line = new String[ATTRIBUTES.length];
            int i=0;
            for (String attribute :ATTRIBUTES) {
                line[i++] = deviceName+'/'+attribute;
            }
            attributeNames.add(line);
        }

        //  Add an ATK table viewer
        subscriberTableViewer = new ScalarTableViewer(attributeNames, labels, columnNames, false, errorHistory);
        subscriberTableViewer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                subscriberTableActionPerformed(evt);
            }
        });
        getContentPane().add(subscriberTableViewer.getScrollPane(columnWiths), java.awt.BorderLayout.CENTER);

        //  If map has not been passed, could have several managers
        if (buildSubscriberMap) {
            //  Add a line for manager attributes is several subscribers
            if (subscriberMap.size()>1) {
                String[] line=new String[ATTRIBUTES.length];
                int i=0;
                for (String attribute : ATTRIBUTES) {
                    line[i++]=TangoUtils.getConfiguratorDeviceName() + '/' + attribute;
                }
                List<String[]> managerAttributes=new ArrayList<>();
                managerAttributes.add(line);
                List<String> rows=new ArrayList<>();
                rows.add("E.S.  Manager");

                ScalarTableViewer managerTableViewer=
                        new ScalarTableViewer(managerAttributes, rows, columnNames, false, errorHistory);
                managerTableViewer.setFont(new Font("Dialog", Font.BOLD, 14));
                managerTableViewer.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        managerTableActionPerformed(evt);
                    }
                });
                getContentPane().add(managerTableViewer.getScrollPane(columnWiths), BorderLayout.SOUTH);
            }
        }
    }
	//=======================================================
    //=======================================================
    @Override
    public void setTitle(String title) {
        if (titleLabel==null)
            super.setTitle(title);
        else
            titleLabel.setText(title);
        pack();
    }
	//=======================================================
	//=======================================================
    private void managerTableActionPerformed(java.awt.event.MouseEvent evt) {
        //int row = subscriberTableViewer.getJTable().rowAtPoint(new Point(evt.getX(), evt.getY()));
        int column = subscriberTableViewer.columnAtPoint(new Point(evt.getX(), evt.getY()));

        if (column==CONTEXT+1 && evt.getClickCount()==2) {
            try {
                manageConfiguratorContext();
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, e.getMessage(), e);
            }
        }
    }
	//=======================================================
	//=======================================================
    private void manageConfiguratorContext() throws DevFailed {
        //  Get configurator context
        String context = configuratorProxy.read_attribute("Context").extractString();

        //  Get List of contexts for subscriber
        Strategy strategy = Strategy.getContextsFromDB();
        String[] strategyNames = strategy.getNames();
        //  And propose to select one of them.
        context = (String) JOptionPane.showInputDialog(this,
                "Context ? ", "Selection",
                JOptionPane.QUESTION_MESSAGE, null,
                strategyNames, context);
        if (context != null) {
            DeviceAttribute deviceAttribute = new DeviceAttribute("Context");
            deviceAttribute.insert(context);
            configuratorProxy.write_attribute(deviceAttribute);
        }
    }
	//=======================================================
	//=======================================================
    private void subscriberTableActionPerformed(MouseEvent event) {
        int row = subscriberTableViewer.rowAtPoint(new Point(event.getX(), event.getY()));
        int column = subscriberTableViewer.columnAtPoint(new Point(event.getX(), event.getY()));
        String  label = labels.get(row);
        //  do any thing only if manager
        if (label.toLowerCase().contains("manager"))
            return;

        try {
            Subscriber subscriber =  subscriberMap.getSubscriberByLabel(label);

            if (event.getClickCount()==2 && event.getButton()==1) {
                if (column>0) { //  Not the line label
                    showAttributes(subscriber, column-1);
                }
            } else
            if (event.getButton()==3) {
                //	Display menu if on line label
                if (column==0) {
                    subscriberMenu.showMenu(event, label, subscriber);
                }
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //=======================================================
    //=======================================================

	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JPanel titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem statisticsItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem distributionItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem frequencyTrendItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem attributeErrorsItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem serverInformationItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewerAtkErrorItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewerAtkDiagnosticsItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("HDB++ Diagnostics");
        titlePanel.add(titleLabel);

        topPanel.add(titlePanel, java.awt.BorderLayout.PAGE_START);
        topPanel.add(jSeparator1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exitItem.setMnemonic('E');
        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");

        statisticsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        statisticsItem.setMnemonic('S');
        statisticsItem.setText("Statistics");
        statisticsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsItemActionPerformed(evt);
            }
        });
        viewMenu.add(statisticsItem);

        distributionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        distributionItem.setMnemonic('D');
        distributionItem.setText("Distribution");
        distributionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distributionItemActionPerformed(evt);
            }
        });
        viewMenu.add(distributionItem);

        frequencyTrendItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        frequencyTrendItem.setMnemonic('F');
        frequencyTrendItem.setText("Frequency Trend");
        frequencyTrendItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frequencyTrendItemActionPerformed(evt);
            }
        });
        viewMenu.add(frequencyTrendItem);

        attributeErrorsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        attributeErrorsItem.setMnemonic('A');
        attributeErrorsItem.setText("Attribute Errors");
        attributeErrorsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeErrorsItemActionPerformed(evt);
            }
        });
        viewMenu.add(attributeErrorsItem);

        serverInformationItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        serverInformationItem.setMnemonic('I');
        serverInformationItem.setText("Server information");
        serverInformationItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInformationItemActionPerformed(evt);
            }
        });
        viewMenu.add(serverInformationItem);

        viewerAtkErrorItem.setMnemonic('E');
        viewerAtkErrorItem.setText("ATK Viewer Errors");
        viewerAtkErrorItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewerAtkErrorItemActionPerformed(evt);
            }
        });
        viewMenu.add(viewerAtkErrorItem);

        viewerAtkDiagnosticsItem.setText("ATK Viewer Diagnostics");
        viewerAtkDiagnosticsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewerAtkDiagnosticsItemActionPerformed(evt);
            }
        });
        viewMenu.add(viewerAtkDiagnosticsItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("help");

        aboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutItem.setMnemonic('A');
        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        doClose();
    }//GEN-LAST:event_exitItemActionPerformed
	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        String  message = "This application is able to display information on HDB event subscribers\n" +
                "\nPascal Verdier - ESRF - Software Group";
        JOptionPane.showMessageDialog(this, message, "Help Window", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void attributeErrorsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeErrorsItemActionPerformed
        // TODO add your handling code here:
        try {
            new FaultyAttributesDialog(this, subscriberMap).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_attributeErrorsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void viewerAtkErrorItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewerAtkErrorItemActionPerformed
        errorHistory.setVisible(true);
    }//GEN-LAST:event_viewerAtkErrorItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void viewerAtkDiagnosticsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewerAtkDiagnosticsItemActionPerformed
        try {
            fr.esrf.tangoatk.widget.util.ATKDiagnostic.showDiagnostic();
        }
        catch (Exception e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_viewerAtkDiagnosticsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void distributionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distributionItemActionPerformed
        try {
            new DistributionDialog(this, subscriberMap).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_distributionItemActionPerformed

    //=======================================================
    //=======================================================
    private void statisticsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsItemActionPerformed
        try {
            new StatisticsDialog(this, subscriberMap, statisticsTimeWindow).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this,
                    ((JMenuItem)evt.getSource()).getText(), e);
        }
    }//GEN-LAST:event_statisticsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void frequencyTrendItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frequencyTrendItemActionPerformed
        try {
            /*
            List<String> attributeNames = new ArrayList<>();
            if (configuratorProxy!=null)
                attributeNames.add(configuratorProxy.name() + "/" + ATTRIBUTES[RECORD_FREQUENCY]);
            for (String label : labels) {
                attributeNames.add( TangoUtils.getOnlyDeviceName(
                        subscriberMap.getSubscriberByLabel(label).name) + "/" + ATTRIBUTES[RECORD_FREQUENCY]);
            }
             */
            List<String> labels = subscriberMap.getLabelList();
            labels.add(0, "Manager");
            List<String> selections =
                    new ItemSelectionDialog(new JFrame(), "Subscribers to monitor ?", labels, false).showDialog();
            if (!selections.isEmpty()) {
                List<String> attributeNames = new ArrayList<>();
                for (String label : selections) {
                    String deviceName;
                    if (label.equalsIgnoreCase("Manager"))
                        deviceName = Utils.getConfiguratorProxy().name();
                    else
                        deviceName = TangoUtils.getOnlyDeviceName(
                                subscriberMap.getSubscriberByLabel(label).getName());
                    attributeNames.add(deviceName + '/' + ATTRIBUTES[RECORD_FREQUENCY]);
                }
                new AtkMoniDialog(this, attributeNames, "HDB storage frequency").setVisible(true);
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_frequencyTrendItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void serverInformationItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInformationItemActionPerformed
        // TODO add your handling code here:
        try {
            if (serverInfoTable==null)
                serverInfoTable = new ServerInfoTable(this);
            serverInfoTable.setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_serverInformationItemActionPerformed

 	//=======================================================
	//=======================================================
    private void configureArchiver(Subscriber subscriber) {
        try {
            if (hdbConfigurator == null)
                hdbConfigurator = new HdbConfigurator(this, true);
            hdbConfigurator.selectArchiver(subscriber.getLabel());
            hdbConfigurator.setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
 	//=======================================================
	//=======================================================
    private void stopFaultyAttributes(Subscriber subscriber) {
        try {
            subscriber.command_inout("StopFaulty");
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }
	//=======================================================
	//=======================================================
    private void showAttributes(Subscriber subscriber, int type) {
        try {
            //  get the attribute list
            switch (type) {
                case STARTED_ATTRIBUTES:
                    if (subscriber.hasAttribute(Subscriber.ATTRIBUTE_STARTED)) {
                        new AttributesTableDialog(this,
                                subscriber, Subscriber.ATTRIBUTE_STARTED).setVisible(true);
                    }
                    return;
                case PAUSED_ATTRIBUTES:
                    if (subscriber.hasAttribute(Subscriber.ATTRIBUTE_PAUSED)) {
                        new AttributesTableDialog(this,
                                subscriber, Subscriber.ATTRIBUTE_PAUSED).setVisible(true);
                    }
                    return;
                case STOPPED_ATTRIBUTES:
                    if (subscriber.hasAttribute(Subscriber.ATTRIBUTE_STOPPED)) {
                        new AttributesTableDialog(this,
                                subscriber, Subscriber.ATTRIBUTE_STOPPED).setVisible(true);
                    }
                    return;
                case FAULTY_ATTRIBUTES:
                    String[] attributeList = ArchiverUtils.getAttributeList(subscriber, "Nok");
                    if (attributeList.length>0) {
                        new FaultyAttributesDialog(this, subscriber).setVisible(true);
                    }
                    return;
                case PENDING_ATTRIBUTES:
                    /*
                    attributeList = ArchiverUtils.getAttributeList(subscriber, "Pending");
                    if (attributeList.length>0) {
                        new AttributesTableDialog(this,
                                subscriber, ": Pending ", attributeList).setVisible(true);
                    }
                    */
                    break;
                case RECORD_FREQUENCY:
                case FAILURE_FREQUENCY:
                    new StatisticsDialog(this, subscriber).setVisible(true);
                    return;

                case CONTEXT:
                    //  Get List of contexts for subscriber
                    Strategy strategy = Strategy.getContextsFromDB(subscriber);
                    String[] contextNames = strategy.getNames();
                    if (contextNames.length>2) {
                        //  Remove ALWAYS and NEVER (not contexts) if any
                        List<String> tmp = new ArrayList<>();
                        for (String contextName : contextNames) {
                            if (!contextName.equalsIgnoreCase("always") && !contextName.equalsIgnoreCase("never"))
                                tmp.add(contextName);
                        }

                        //  And propose to select one of them.
                        String context = (String) JOptionPane.showInputDialog(this,
                                "Context ? ", "Selection",
                                JOptionPane.QUESTION_MESSAGE, null,
                                tmp.toArray(new String[0]), subscriber.getContext());
                        if (context!=null) {
                            subscriber.setContext(context);
                        }
                    }
                    return;

                default:
                    Except.throw_exception("BadOption", type + " not implemented");
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.toString(), e);
        }
    }
	//=======================================================
	//=======================================================
    private void testDevice(DeviceProxy deviceProxy) {
        try {
            TangoUtils.testDevice(this, deviceProxy.name());
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
	//=======================================================
	//=======================================================
    private void serverStatus(Subscriber subscriber) {
        try {
            if (serverInfoTable==null)
                serverInfoTable = new ServerInfoTable(this);
            serverInfoTable.setSelection( subscriber.getLabel());
            serverInfoTable.setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //=======================================================
    //=======================================================
    private void doClose() {
        //  if parent not null and still visible.
        if ((parent!=null && parent.isVisible()) ||
                (hdbConfigurator!=null && hdbConfigurator.isVisible())) {
            setVisible(false);
            dispose();
        }
        else
            System.exit(0);
    }
	//=======================================================
	//=======================================================







	//=======================================================
    /**
     * @param args the command line arguments
     */
	//=======================================================
    public static void main(String[] args) {
		try {
		    UIManager.put("ToolTip.foreground", new ColorUIResource(Color.black));
            UIManager.put("ToolTip.background", new ColorUIResource(Utils.toolTipBackground));
            new HdbDiagnostics(null).setVisible(true);
		}
		catch(DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//=======================================================




    //==============================================================================
    //==============================================================================
    private static final int FAULTY_ATTRIBUTES  = 0;
    private static final int STARTED_ATTRIBUTES = 1;
    private static final int PAUSED_ATTRIBUTES  = 2;
    private static final int STOPPED_ATTRIBUTES = 3;
    private static final int PENDING_ATTRIBUTES = 4;
    private static final int RECORD_FREQUENCY   = 5;
    //  Separator
    private static final int CONFIGURE_ARCHIVER = 7;
    private static final int SERVER_STATUS      = 8;
    private static final int STOP_FAULTY        = 9;
    private static final int COPY_DEVICE_NAME   = 10;

    private static final int TEST_ARCHIVER      = 11;
    private static final int TEST_CONFIGURATOR  = 12;
    private static final int OFFSET = 3;    //	Label And separator

    private static final int FAILURE_FREQUENCY  = 6; // not used for menu (Column number)
    private static final int CONTEXT            = 7; // not used for menu (Column number)

    private static String[] menuLabels = {
            "Faulty  Attributes",
            "Started Attributes",
            "Paused Attributes",
            "Stopped Attributes",
            "Pending Attributes",
            "Record Frequency",
            null,
            "Configure Archiver",
            "Server Status",
            "Stop Faulty Attributes",
            "Copy device name",

            "Test  Archiver",
            "Test  Configurator",
    };

    private class SubscriberMenu extends JPopupMenu {
        private JLabel subscriberLabel;
        private JLabel subscriberDeviceName;
        private Subscriber  selectedSubscriber;
        //======================================================
        private SubscriberMenu() {
            subscriberLabel = new JLabel();
            subscriberLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(subscriberLabel);
            subscriberDeviceName = new JLabel();
            add(subscriberDeviceName);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel==null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(this::hostActionPerformed);
                    add(btn);
                }
            }
        }
        //======================================================
        private void showMenu(MouseEvent event, String label, Subscriber subscriber) {

            subscriberLabel.setText(label);
            selectedSubscriber = subscriber;
            String deviceName = subscriber.getName();
            if (deviceName.equals(label))
                subscriberDeviceName.setVisible(false);
            else {
                subscriberDeviceName.setVisible(true);
                subscriberDeviceName.setText("(" + deviceName + ")");
            }
            boolean expert = (event.getModifiers() & MouseEvent.CTRL_MASK)!=0;

            getComponent(OFFSET + STOP_FAULTY).setVisible(subscriber.hasFaultyAttribute());

            getComponent(OFFSET + CONFIGURE_ARCHIVER).setVisible(!fromExternal);
            getComponent(OFFSET + TEST_ARCHIVER).setVisible(expert);
            getComponent(OFFSET + TEST_CONFIGURATOR).setVisible(expert);
            show(subscriberTableViewer, event.getX(), event.getY());
        }
        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = 0;
            for (int i = 0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i)==obj)
                    itemIndex = i;

            switch (itemIndex) {
                case STARTED_ATTRIBUTES:
                case PAUSED_ATTRIBUTES:
                case STOPPED_ATTRIBUTES:
                case FAULTY_ATTRIBUTES:
                case PENDING_ATTRIBUTES:
                case RECORD_FREQUENCY:
                    showAttributes(selectedSubscriber, itemIndex);
                    break;

                case CONFIGURE_ARCHIVER:
                    configureArchiver(selectedSubscriber);
                    break;
                case STOP_FAULTY:
                    stopFaultyAttributes(selectedSubscriber);
                    break;
                case COPY_DEVICE_NAME:
                    CopyUtils.copyToClipboard(selectedSubscriber.getName());
                    break;

                case SERVER_STATUS:
                    serverStatus(selectedSubscriber);
                    break;
                case TEST_ARCHIVER:
                    testDevice(selectedSubscriber);
                    break;
                case TEST_CONFIGURATOR:
                    testDevice(configuratorProxy);
                    break;
            }
        }
    }
}
