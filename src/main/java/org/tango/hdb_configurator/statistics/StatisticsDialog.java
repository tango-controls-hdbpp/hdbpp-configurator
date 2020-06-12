//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
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

package org.tango.hdb_configurator.statistics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//===============================================================
/**
 *	JDialog Class to display statistics on attributes
 *
 *	@author  Pascal Verdier
 */
//===============================================================

@SuppressWarnings("MagicConstant")
public class StatisticsDialog extends JDialog {
    private JFrame parent;
    private List<StatAttribute> statAttributes;
    private StatisticsTable table;
    private List<Subscriber> subscribers;
    private String subscriberName = null;
    private long readTime;
    private StatisticsChart statisticsChart;

    private static List<String> defaultTangoHosts;
    private static JFileChooser fileChooser = null;
    //===============================================================
	/**
	 *	Creates new form StatisticsDialog for several subscribers
	 */
	//===============================================================
    public StatisticsDialog(JFrame parent, List<Subscriber> subscriberList) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        readTime = System.currentTimeMillis();
        SplashUtils.getInstance().startSplash();
        try {
            defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
            initComponents();
            subscribers = subscriberList;
            finalizeConstruction("All Subscribers ", true);
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            throw e;
        }
        SplashUtils.getInstance().stopSplash();
    }
    //===============================================================
	/**
	 *	Creates new form StatisticsDialog for one subscriber
	 */
	//===============================================================
	public StatisticsDialog(JFrame parent, String subscriberDeviceName) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        readTime = System.currentTimeMillis();

        //  Search subscriber label
        List<String[]> labels = TangoUtils.getSubscriberLabels();
        String label = subscriberDeviceName;
        for (String[] tuple : labels) {
            if (tuple[0].equalsIgnoreCase(subscriberDeviceName))
                label = tuple[1];
        }
        //  Build the subscriber and build the form
        DeviceProxy managerProxy = Utils.getConfiguratorProxy();
        Subscriber subscriber = new Subscriber(subscriberDeviceName, label, managerProxy);
        buildForOneSubscriber(subscriber);
    }
    //===============================================================
	/**
	 *	Creates new form StatisticsDialog for one subscriber
	 */
	//===============================================================
	public StatisticsDialog(JFrame parent, Subscriber subscriber) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        readTime = System.currentTimeMillis();
        buildForOneSubscriber(subscriber);
    }
    //===============================================================
    //===============================================================
    private void buildForOneSubscriber(Subscriber subscriber) throws DevFailed {
        SplashUtils.getInstance().startSplash();
        try {
            defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
            initComponents();
            subscriberName = subscriber.getLabel();
            subscribers = new ArrayList<>(1);
            subscribers.add(subscriber);
            finalizeConstruction("Subscriber " + subscriber.getLabel(), false);
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            throw e;
        }
        SplashUtils.getInstance().stopSplash();
	}
    //===============================================================
    //===============================================================
    private void finalizeConstruction(String title, boolean allowReset) throws DevFailed {
        try {
            buildRecords();
        }
        catch (DevFailed e) {
            if (subscribers.size()==1)  //  Only one (failed)
                throw e;
            else {
                SplashUtils.getInstance().stopSplash();
                ErrorPane.showErrorMessage(this, "Read Subscribe Failed", e);
            }
        }
        // Build a Table
        table = new StatisticsTable(parent, this, statAttributes);

        //	Put it in scrolled pane
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        Dimension tableDimension = table.getTableDimension();
        if (tableDimension.height>800) tableDimension.height = 800;
        scrollPane.setPreferredSize(tableDimension);

        setTitle(title);
        displayTitle();

        //  Build chart and adapt size to table size
        statisticsChart = new StatisticsChart(table.getFilteredStatAttributes());
        Dimension chartDimension = new Dimension(700, tableDimension.height);
        statisticsChart.setPreferredSize(chartDimension);
        getContentPane().add(statisticsChart, BorderLayout.EAST);
        statisticsChart.setVisible(false);

        //  Do not allow for one subscriber
        resetItem.setVisible(allowReset);

        //  Check if extraction available
        String s = System.getenv("HDB_TYPE");
        readHdbItem.setVisible(s!=null && !s.isEmpty());

        pack();
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    //===============================================================
    private void displayTitle() {
        String  title;
        if (subscriberName==null)
            title = "Statistics on " + subscribers.size() + " subscribers  -  ";
        else
            title = subscriberName + " - ";
        if (table.getFilteredStatAttributes().size()!= statAttributes.size()) {
            title += table.getFilteredStatAttributes().size() + " filtered / ";
        }
        title += statAttributes.size() + " Attributes";
        titleLabel1.setText(title);

        if (subscribers.size()==1) {
            long resetTime = subscribers.get(0).getStatisticsResetTime();
            if (resetTime > 0) {
                titleLabel2.setText("Reset: " + Utils.formatDateTime(resetTime));
            }

            long sinceReset = readTime - resetTime;
            if (sinceReset > 0) {
                String text = "<table>\n" +
                        "<tr><td> Reset done </td><td> " + Utils.formatDateTime(resetTime) + " </td></tr>\n" +
                        "<tr><td>Statistics read </td><td> " + Utils.formatDateTime(readTime) + " </td></tr>\n" +
                        "<tr><td>Duration </td><td> " + Utils.strPeriod((double) sinceReset / 1000) + " </td></tr>\n" +
                        "</table>";

                titleLabel1.setToolTipText(Utils.buildTooltip(text));
                table.getTableHeader().setToolTipText(Utils.buildTooltip(text));
            }
        }
        else
            titleLabel2.setText("");
    }
    //===============================================================
    //===============================================================
    private void buildRecords() throws DevFailed {
        String[] statAttributeNames = {
                "AttributeList", "AttributeRecordFreqList", "AttributeEventNumberList" };
        StringBuilder errorMessage = new StringBuilder();
        statAttributes = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            try {
                //  Read statistic attributes
                SplashUtils.getInstance().increaseSplashProgressForLoop(
                        subscribers.size(), "Reading " + subscriber.getLabel());
                DeviceAttribute[] deviceAttributes = subscriber.read_attribute(statAttributeNames);

                //  Extract values
                int i=0;
                String[] hdbAttributeNames = new String[0];
                if (!deviceAttributes[i].hasFailed())
                    hdbAttributeNames = deviceAttributes[i].extractStringArray();
                i++;
                double[] frequencies = new double[0];
                if (!deviceAttributes[i].hasFailed())
                    frequencies = deviceAttributes[i].extractDoubleArray();
                i++;
                int[] eventNumbers = new int[0];
                if (!deviceAttributes[i].hasFailed())
                    eventNumbers = deviceAttributes[i].extractLongArray();

                //  Build filteredHdbAttributes and store in a list
                for (int x=0 ; x<hdbAttributeNames.length &&
                               x<frequencies.length && x<eventNumbers.length ; x++) {
                    statAttributes.add(new StatAttribute(
                            hdbAttributeNames[x], readTime, defaultTangoHosts,
                            frequencies[x], eventNumbers[x], subscriber));
                }
            }
            catch (DevFailed e) {
                errorMessage.append(e.errors[0].desc).append("\n");
            }
        }
        statAttributes.sort(new AttributeComparator());
        if (errorMessage.length()>0)
            Except.throw_exception("ReadSubscriberFailed", errorMessage.toString());
    }
	//===============================================================
	//===============================================================

	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        javax.swing.JButton applyButton = new javax.swing.JButton();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JPanel topTopPanel = new javax.swing.JPanel();
        titleLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel topBottomPanel = new javax.swing.JPanel();
        titleLabel2 = new javax.swing.JLabel();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem saveItem = new javax.swing.JMenuItem();
        resetItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem dismissItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem updateItem = new javax.swing.JMenuItem();
        javax.swing.JCheckBoxMenuItem chartItem = new javax.swing.JCheckBoxMenuItem();
        readHdbItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Filter:   ");
        bottomPanel.add(jLabel1);

        filterTextField.setColumns(25);
        filterTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTextFieldActionPerformed(evt);
            }
        });
        bottomPanel.add(filterTextField);

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(applyButton);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel1.setText("Dialog Title 1");
        topTopPanel.add(titleLabel1);

        topPanel.add(topTopPanel, java.awt.BorderLayout.NORTH);

        titleLabel2.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel2.setText("Dialog Title 2");
        topBottomPanel.add(titleLabel2);

        topPanel.add(topBottomPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        fileMenu.setText("File");

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setText("Save in text file");
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        resetItem.setText("Reset Event Counters");
        resetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetItemActionPerformed(evt);
            }
        });
        fileMenu.add(resetItem);

        dismissItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        dismissItem.setText("Dismiss");
        dismissItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissItemActionPerformed(evt);
            }
        });
        fileMenu.add(dismissItem);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        updateItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        updateItem.setText("Update");
        updateItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateItemActionPerformed(evt);
            }
        });
        viewMenu.add(updateItem);

        chartItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        chartItem.setMnemonic('G');
        chartItem.setText("Graphical display");
        chartItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartItemActionPerformed(evt);
            }
        });
        viewMenu.add(chartItem);

        readHdbItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        readHdbItem.setText("Read attributeList from HDB");
        readHdbItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readHdbItemActionPerformed(evt);
            }
        });
        viewMenu.add(readHdbItem);

        jMenuBar1.add(viewMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
    private void dismissItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissItemActionPerformed
        doClose();
    }//GEN-LAST:event_dismissItemActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        applyFilter();
    }//GEN-LAST:event_applyButtonActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void filterTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTextFieldActionPerformed
        applyFilter();
    }//GEN-LAST:event_filterTextFieldActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void resetItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetItemActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Reset event counter on all archivers ?",
                "Reset Counters", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            StringBuilder errorMessage = new StringBuilder();
            for (Subscriber subscriber : subscribers) {
                try {
                    subscriber.command_inout("ResetStatistics");
                }
                catch (DevFailed e) {
                    errorMessage.append(subscriber.getLabel()).append(": ").append(e.errors[0].desc).append("\n");
                }
            }

            //  If at least one failed, display message
            if (errorMessage.length()>0) {
                JOptionPane.showMessageDialog(this, errorMessage.toString(), "Reset Failed", JOptionPane.ERROR_MESSAGE);
            }
            updateItemActionPerformed(null);
        }
    }//GEN-LAST:event_resetItemActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void updateItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateItemActionPerformed
        try {
            readTime = System.currentTimeMillis();
            buildRecords();
            List<StatAttribute> filtered = table.updateAttributes(
                    statAttributes, filterTextField.getText().trim());
            statisticsChart.updateValues(filtered);
            displayTitle();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "Update", e);
        }
    }//GEN-LAST:event_updateItemActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed

        //  Initialize if not already done
        if (fileChooser==null) {
            fileChooser = new JFileChooser(new File("").getAbsolutePath());
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setDialogTitle("Save as text");
        }

        //  Start file chooser to select file
        if (fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file!=null) {
                String	filename = file.getAbsolutePath();
                saveDataToFile(filename);
            }
        }
    }//GEN-LAST:event_saveItemActionPerformed
    //===============================================================
    //===============================================================
    private void saveDataToFile(String fileName) {
        try {
            Utils.writeFile(fileName, table.getDataForFile());
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "Saving "+fileName, e);
        }
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void readHdbItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readHdbItemActionPerformed
        try {
            List<String> attributeNames = new ArrayList<>();
            for (StatAttribute attribute : table.getFilteredStatAttributes())
                attributeNames.add(attribute.name);
            List<String> selectionList = new ItemSelectionDialog(
                    this, "Attribute Selection", attributeNames, false).showDialog();
            if (!selectionList.isEmpty()) {
                if (selectionList.size()>16)
                    JOptionPane.showMessageDialog(this, "Too many selected attributes !");
                else
                    Utils.startHdbViewer(selectionList);
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.toString(), e);
        }
    }//GEN-LAST:event_readHdbItemActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void chartItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartItemActionPerformed
        JCheckBoxMenuItem  button = (JCheckBoxMenuItem) evt.getSource();
        statisticsChart.setVisible(button.isSelected());
        pack();
    }//GEN-LAST:event_chartItemActionPerformed
	//===============================================================
	//===============================================================
    private void applyFilter() {
        String  filter = filterTextField.getText();
        statisticsChart.updateValues(table.applyFilter(filter));
        displayTitle();
    }
	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
	    if (parent==null)
	        System.exit(0);
	    else {
            setVisible(false);
            dispose();
        }
	}
    //===============================================================
    //===============================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterTextField;
    private javax.swing.JMenuItem readHdbItem;
    private javax.swing.JMenuItem resetItem;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JLabel titleLabel2;
    // End of variables declaration//GEN-END:variables
	//===============================================================



	//===============================================================
    private static final boolean oneSubscriber = false;
	//===============================================================
	public static void main(String[] args) {
	    try {
	        if (oneSubscriber) {
                String subscriberDeviceName = "sys/hdb-es/srvac-press";
                if (args.length > 0) subscriberDeviceName = args[0];
                new StatisticsDialog(null, subscriberDeviceName).setVisible(true);
            }
	        else {
                SubscriberMap subscriberMap = Utils.getSubscriberMap(TangoUtils.getConfiguratorDeviceName());
                List<Subscriber> subscriberList = new ArrayList<>();
                subscriberList.add(subscriberMap.getSubscriberByLabel("FE Pressures"));
                subscriberList.add(subscriberMap.getSubscriberByLabel("SY Pressures"));
                new StatisticsDialog(null, subscriberList).setVisible(true);
                //new StatisticsDialog(null, subscriberMap.getSubscriberList()).setVisible(true);
            }
        }
	    catch (DevFailed e) {
	        ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }
    //===============================================================
	//===============================================================







    //=========================================================================
    /**
     * Comparator to sort attribute list
     */
    //=========================================================================
    private static class AttributeComparator implements Comparator<StatAttribute> {
        //======================================================
        public int compare(StatAttribute statAttribute1, StatAttribute statAttribute2) {
            return valueSort(statAttribute2.nbEvents, statAttribute1.nbEvents);
        }
        //======================================================
        private int valueSort(double d1, double d2) {
            if (d1==d2)    return 0;
            else if (d1<0) return  1;   // Not initialized
            else if (d2<0) return -1;   // Not initialized
            else return ((d1 > d2)? 1 : -1);
        }
        //======================================================
    }
    //===============================================================
    //===============================================================
}
