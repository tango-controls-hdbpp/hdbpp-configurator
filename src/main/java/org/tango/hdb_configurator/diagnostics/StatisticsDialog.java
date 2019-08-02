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

package org.tango.hdb_configurator.diagnostics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

import static org.tango.hdb_configurator.common.Utils.firstColumnBackground;
import static org.tango.hdb_configurator.common.Utils.selectionBackground;


//===============================================================
/**
 *	JDialog Class to display statistics on attributes
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class StatisticsDialog extends JDialog {

    private List<HdbAttribute> filteredHdbAttributes;
    private List<HdbAttribute> hdbAttributes;
    private JTable table;
    private DataTableModel model;
    private TablePopupMenu popupMenu = new TablePopupMenu();
    private ArrayList<Subscriber> subscribers;
    private int selectedRow    = -1;
    private int selectedColumn = EVENTS_RESET;
    private String subscriberName = null;
    private long resetTime = 0;
    private long readTime = 0;
    private long sinceReset = 0;

    private static List<String> defaultTangoHosts;
    private static JFileChooser fileChooser = null;

    private static final int[] columnWidth = { 300, 100, 80, 100, 80 };
    private static final  String[] columnNames = {
            "Attribute Names", "Ev. since reset", "Av.Period.", "Events", "Av.Period." };

    private static final int ATTRIBUTE_NAME = 0;
    private static final int EVENTS_RESET   = 1;
    private static final int PERIOD_RESET   = 2;
    private static final int EVENTS_STAT    = 3;
    private static final int PERIOD_STAT    = 4;
	//===============================================================
	/**
	 *	Creates new form StatisticsDialog for several subscribers
	 */
	//===============================================================
    public StatisticsDialog(JFrame parent, SubscriberMap subscriberMap, int statisticsTimeWindow) throws DevFailed {
        super(parent, false);
        SplashUtils.getInstance().startSplash();
        try {
            defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
            initComponents();
            subscribers = (ArrayList<Subscriber>) subscriberMap.getSubscriberList();
            finalizeConstruction(statisticsTimeWindow, "All Subscribers ", true);
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
	public StatisticsDialog(JFrame parent, Subscriber subscriber,
                            int statisticsTimeWindow, long resetTime) throws DevFailed {
		super(parent, false);
        this.resetTime = resetTime;
        if (resetTime>0) {
            sinceReset = System.currentTimeMillis() - resetTime;
        }
        SplashUtils.getInstance().startSplash();
        try {
            defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
            initComponents();
            subscriberName = subscriber.getLabel();
            subscribers = new ArrayList<>(1);
            subscribers.add(subscriber);
            finalizeConstruction(statisticsTimeWindow, "Subscriber " + subscriber.getLabel(), false);
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            throw e;
        }
        SplashUtils.getInstance().stopSplash();
	}
    //===============================================================
    //===============================================================
    private void finalizeConstruction(int statisticsTimeWindow,
                                      String title, boolean allowReset) throws DevFailed {
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
        updateColumnNames(statisticsTimeWindow);
        buildTableComponent();
        setTitle(title);
        displayTitle();

        //  Do not allow for one subscriber
        resetItem.setVisible(allowReset);

        //  Check if extraction available
        String s = System.getProperty("HDB_TYPE");
        readHdbItem.setVisible(s!=null && !s.isEmpty());

        pack();
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    //===============================================================
    private void updateColumnNames(int statisticsTimeWindow) {
        columnNames[EVENTS_STAT] = "Ev./" + Utils.strPeriod(statisticsTimeWindow);
    }
    //===============================================================
    //===============================================================
    public static String formatResetTime(long ms)
    {
        StringTokenizer st = new StringTokenizer(new Date(ms).toString());
        List<String> list = new ArrayList<>();
        while (st.hasMoreTokens())
            list.add(st.nextToken());

        String  month = list.get(1);
        String  day   = list.get(2);
        String  time  = list.get(3);
        //String  year  = list.get(list.size()-1);
        //if (year.indexOf(')')>0)    year = year.substring(0, year.indexOf(')'));

        return day+' '+month + ' ' + time;
    }

    //===============================================================
    //===============================================================
    private void displayTitle() {
        String  title;
        if (subscriberName==null)
            title = "Statistics on " + subscribers.size() + " subscribers  -  ";
        else
            title = subscriberName + " - ";
        if (filteredHdbAttributes.size()!=hdbAttributes.size()) {
            title += filteredHdbAttributes.size() + " filtered / ";
        }
        title += hdbAttributes.size() + " Attributes";

        if (resetTime>0) {
            title += " - Reset: " + formatResetTime(resetTime);
        }
        titleLabel.setText(title);

        if (sinceReset>0) {
            String text = "<table>\n" +
                    "<tr><td> Reset done </td><td> "     + formatResetTime(resetTime) + " </td></tr>\n"+
                    "<tr><td>Statistics read </td><td> " + formatResetTime(readTime)  + " </td></tr>\n"+
                    "<tr><td>Duration </td><td> " + Utils.strPeriod((double) sinceReset/1000)  + " </td></tr>\n"+
                    "</table>";

            titleLabel.setToolTipText(Utils.buildTooltip(text));
            table.getTableHeader().setToolTipText(Utils.buildTooltip(text));
        }

    }
    //===============================================================
    //===============================================================
    private void buildRecords() throws DevFailed {
        String[] statAttributeNames = {
                "AttributeList", "AttributeRecordFreqList", "AttributeEventNumberList" };
        StringBuilder errorMessage = new StringBuilder();
        hdbAttributes = new ArrayList<>();
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

                //  Duration is passed to have just one DB read per subscriber
                int duration = subscriber.getStatisticsTimeWindow();

                //  Build filteredHdbAttributes and store in a list
                for (int x=0 ; x<hdbAttributeNames.length &&
                               x<frequencies.length && x<eventNumbers.length ; x++) {
                    hdbAttributes.add(new HdbAttribute(hdbAttributeNames[x],
                            frequencies[x], eventNumbers[x], subscriber, duration));
                }
            }
            catch (DevFailed e) {
                errorMessage.append(e.errors[0].desc).append("\n");
            }
        }
        readTime = System.currentTimeMillis();
        hdbAttributes.sort(new AttributeComparator());

        //  Copy to filtered (no filter at start up)
        filteredHdbAttributes = new ArrayList<>();
        filteredHdbAttributes.addAll(hdbAttributes);
        if (errorMessage.length()>0)
            Except.throw_exception("ReadSubscriberFailed", errorMessage.toString());
    }
    //===============================================================
    //===============================================================
    private void buildTableComponent() throws DevFailed {
        try {
            model = new DataTableModel();

            // Create the table
            table = new JTable(model) {
                //	Implements table cell tool tip
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    if (isVisible()) {
                        Point p = e.getPoint();
                        int column = columnAtPoint(p);
                        int row = rowAtPoint(p);
                        if (column==ATTRIBUTE_NAME) {
                            HdbAttribute    attribute = filteredHdbAttributes.get(row);
                            tip = Utils.buildTooltip(attribute.name);
                        }
                    }
                    return tip;
                }
            };

            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(true);
            table.setDragEnabled(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            table.setDefaultRenderer(String.class, new LabelCellRenderer());
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });
            table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableHeaderActionPerformed(evt);
                }
            });

            //	Put it in scrolled pane
            JScrollPane scrollPane = new JScrollPane(table);
            model.fireTableDataChanged();
            getContentPane().add(scrollPane, BorderLayout.CENTER);

            //  Set column width
            final Enumeration columnEnum = table.getColumnModel().getColumns();
            int i = 0;
            int width = 0;
            TableColumn tableColumn;
            while (columnEnum.hasMoreElements()) {
                width += columnWidth[i];
                tableColumn = (TableColumn) columnEnum.nextElement();
                tableColumn.setPreferredWidth(columnWidth[i++]);
            }

            //  Compute size to display
            pack();
            int height = table.getHeight();
            if (height>800) height = 800;
            scrollPane.setPreferredSize(new Dimension(width, height+20));
        }
        catch (Exception e) {
            e.printStackTrace();
            Except.throw_exception("INIT_ERROR", e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void tableHeaderActionPerformed(java.awt.event.MouseEvent event) {
        //	Get specified column
        selectedColumn = table.getTableHeader().columnAtPoint(new Point(event.getX(), event.getY()));
        filteredHdbAttributes.sort(new AttributeComparator());
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent event) {

        //	get selected signal
        Point clickedPoint = new Point(event.getX(), event.getY());
        int row = table.rowAtPoint(clickedPoint);
        selectedRow = row;
        table.repaint();

        if (event.getClickCount() == 2) {
            JOptionPane.showMessageDialog(this, filteredHdbAttributes.get(row).getInfo());
        }
        else {
            int mask = event.getModifiers();

            //  Check button clicked
            if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
                popupMenu.showMenu(event, filteredHdbAttributes.get(row));
            }
        }
    }
	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        javax.swing.JButton applyButton = new javax.swing.JButton();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem saveItem = new javax.swing.JMenuItem();
        resetItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem dismissItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem updateItem = new javax.swing.JMenuItem();
        readHdbItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
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

        readHdbItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        readHdbItem.setText("Read attribute from HDB");
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
            //  Check reset time
            resetTime = subscribers.get(0).getStatisticsResetTime();
            if (resetTime>0)
                sinceReset = System.currentTimeMillis() - resetTime;
            buildRecords();
            model.fireTableDataChanged();
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
        StringBuilder   sb = new StringBuilder();
        for (String s : columnNames) {
            sb.append(s).append('\t');
        }
        sb.trimToSize();
        sb.append('\n');
        for (HdbAttribute hdbAttribute : filteredHdbAttributes) {
            sb.append(hdbAttribute.shortName).append('\t')
                    .append(hdbAttribute.nbStatistics).append('\t')
                    .append(hdbAttribute.averagePeriodString).append('\t')
                    .append(hdbAttribute.nbEvents).append('\n');
        }

        //System.out.println(sb);
        try {
            Utils.writeFile(fileName, sb.toString());
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "Saving "+fileName, e);
        }

    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void readHdbItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readHdbItemActionPerformed
        if (selectedRow<0) {
            Utils.popupError(this, "No attribute selected !");
            return;
        }
        HdbAttribute    attribute = filteredHdbAttributes.get(selectedRow);
        System.out.println("Display " + attribute.name);
        try {
            //  Use new HDB API
            Utils.startHdbViewer(attribute.name);
            /*
            Component extractor = Utils.getInstance().startExternalApplication(
                    new JFrame(), extractionClass, new String[] {attribute.name});
            extractor.setVisible(false);
            */
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.toString(), e);
        }
    }//GEN-LAST:event_readHdbItemActionPerformed

	//===============================================================
	//===============================================================
    private void applyFilter() {
        String  filter = filterTextField.getText();
        filteredHdbAttributes = new ArrayList<>();
        for (HdbAttribute hdbAttribute : hdbAttributes) {
            if (hdbAttribute.shortName.contains(filter)) {
                filteredHdbAttributes.add(hdbAttribute);
            }
        }
        model.fireTableDataChanged();
        displayTitle();
    }
	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
        setVisible(false);
        dispose();
	}
    //===============================================================
    //===============================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterTextField;
    private javax.swing.JMenuItem readHdbItem;
    private javax.swing.JMenuItem resetItem;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




    //===============================================================
    /**
     * Attribute read from HDB object.
     */
    //===============================================================
    private class HdbAttribute {
        private String name;
        private String shortName;
        private int nbStatistics;
        private int nbEvents;
        private int duration;
        private String averagePeriodString;
        private double averagePeriod = -1;
        private Subscriber subscriber;
        private String deviceName;
        private double resetPeriod;
        private String resetPeriodString;

        /** if use another TANGO_HOST, cannot configure with Jive */
        private boolean useDefaultTangoHost = false;
        //===========================================================
        private HdbAttribute(String name, double frequency, int nbEvents, Subscriber subscriber, int duration) {
            this.name = name;
            this.shortName = TangoUtils.getOnlyDeviceName(name);
            this.nbStatistics = (int)frequency;
            this.nbEvents  = nbEvents;
            this.subscriber = subscriber;
            this.duration = duration;
            //  Average period
            if (frequency>0.0) {
                averagePeriod = (double)duration/frequency;
                averagePeriodString = Utils.strPeriod(averagePeriod);
            }
            else {
                averagePeriodString = "---";
            }

            //  Average period since reset
            if (resetTime>0 && sinceReset>0 && nbEvents>0) {
                resetPeriod = (double) sinceReset/nbEvents;
                resetPeriodString = Utils.strPeriod(resetPeriod/1000);
            }
            else {
                resetPeriodString = "---";
            }

            deviceName = shortName.substring(0, shortName.lastIndexOf('/'));
            String tangoHost = TangoUtils.getOnlyTangoHost(name);
            for (String defaultTangoHost : defaultTangoHosts) {
                if (tangoHost.equals(defaultTangoHost)) {
                    useDefaultTangoHost = true;
                    break;
                }
            }
        }
        //===========================================================
        private String getInfo() {
            String host = null;
            String server = null;
            String attributeStatus = null;
            try {
                String deviceName = name.substring(0, name.lastIndexOf('/'));
                DeviceProxy deviceProxy = new DeviceProxy(deviceName);
                DeviceInfo info = deviceProxy.get_info();
                host = info.hostname;
                server = info.server;
                attributeStatus = subscriber.getAttributeStatus(name);
            }
            catch (DevFailed e) {
                Except.print_exception(e);
            }
            catch (Exception e) {
                // cannot find host
            }

            StringBuilder sb = new StringBuilder(name+"\n");
            if (server!=null)
                sb.append("    - Server   ").append(server).append("\n");
            if (host!=null)
                sb.append("    - registered on   ").append(host).append("\n");
            sb.append("\nArchived by ").append(subscriber.getLabel());
            sb.append("    (").append(subscriber.getName()).append(")\n");
            sb.append(nbStatistics).append(" events during ").append(Utils.strPeriod(duration)).append("\n");
            sb.append(nbEvents).append(" Since last reset : ").append(formatResetTime(resetTime)).append("\n");
            sb.append("    - during ").append(Utils.strPeriod((double) sinceReset/1000));

            if (attributeStatus!=null) {
                sb.append("\n\n----------------------- Attribute Status --------------------------\n");
                sb.append(attributeStatus);
            }
            return sb.toString();
        }
        //===========================================================
        private void configureEvent() {
            Utils.startJiveForDevice(deviceName);
        }
        //===========================================================
        public String toString() {
            return shortName + ":\t" + nbEvents;
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================


    //=========================================================================
    /**
     * The Table model
     */
    //=========================================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        public int getColumnCount() {
            return columnNames.length;
        }

        //==========================================================
        public int getRowCount() {
            return filteredHdbAttributes.size();
        }

        //==========================================================
        public String getColumnName(int columnIndex) {
            String title;
            if (columnIndex >= getColumnCount())
                title = columnNames[getColumnCount()-1];
            else
                title = columnNames[columnIndex];

            // remove tango host if any
            if (title.startsWith("tango://")) {
                int index = title.indexOf('/', "tango://".length());
                title = title.substring(index+1);
            }

            return title;
        }

        //==========================================================
        public Object getValueAt(int row, int column) {
            //  Value to display is returned by
            // LabelCellRenderer.getTableCellRendererComponent()
            return "";
        }
        //==========================================================
        /**
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         *
         * @param  column the specified co;umn number
         * @return the cell class at first row for specified column.
         */
        //==========================================================
        public Class getColumnClass(int column) {
            if (isVisible())
                return getValueAt(0, column).getClass();
            else
                return null;
        }
        //==========================================================
        //==========================================================
    }
    //=========================================================================
    //=========================================================================



    //=========================================================================
    /**
     * Renderer to set cell color
     */
    //=========================================================================
    public class LabelCellRenderer extends JLabel implements TableCellRenderer {

        //==========================================================
        public LabelCellRenderer() {
            //setFont(new Font("Dialog", Font.BOLD, 11));
            setOpaque(true); //MUST do this for background to show up.
        }

        //==========================================================
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setBackground(getBackground(row, column));
            switch (column) {
                case ATTRIBUTE_NAME:
                    setText(filteredHdbAttributes.get(row).shortName);
                    break;
                case EVENTS_STAT:
                    setText(Integer.toString(filteredHdbAttributes.get(row).nbStatistics));
                    break;
                case PERIOD_STAT:
                    setText(filteredHdbAttributes.get(row).averagePeriodString);
                    break;
                case EVENTS_RESET:
                    setText(Integer.toString(filteredHdbAttributes.get(row).nbEvents));
                    break;
                case PERIOD_RESET:
                    setText(filteredHdbAttributes.get(row).resetPeriodString);
                    break;
            }
            return this;
        }
        //==========================================================
        private Color getBackground(int row, int column) {
            if (column == 0) {
                return firstColumnBackground;
            }
            if (selectedRow >= 0) {
                if (row == selectedRow)
                    return selectionBackground;
            }
            return Color.white;
        }
        //==========================================================
    }
    //=========================================================================
    //=========================================================================


    //======================================================
    /**
     * Popup menu class
     */
    //======================================================
    private static final int STATUS     = 0;
    private static final int READ_HDB   = 1;
    private static final int CONFIGURE  = 2;
    private static final int TEST_EVENT = 3;
    private static final int COPY_ATTR  = 4;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Status",
            "Read attribute from HDB",
            "Configure Polling/Events",
            "Test Event",
            "Copy attribute name",
    };
    //=======================================================
    //=======================================================
    private class TablePopupMenu extends JPopupMenu {
        private JLabel title;
        private HdbAttribute selectedAttribute;
        //======================================================
        private TablePopupMenu() {
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::menuActionPerformed);
                add(btn);
            }
            //  Check if extraction available
            String s = System.getenv("HDB_TYPE");
            getComponent(OFFSET + READ_HDB).setVisible(s!=null && !s.isEmpty());
        }
        //======================================================
        //======================================================
        private void showMenu(MouseEvent event, HdbAttribute hdbAttribute) {
            title.setText(hdbAttribute.shortName);
            selectedAttribute = hdbAttribute;
            // Too much subscriptions already done -> does not work
            getComponent(OFFSET + TEST_EVENT).setVisible(false/*Utils.getTestEvents()!=null*/);
            getComponent(OFFSET + CONFIGURE).setEnabled(hdbAttribute.useDefaultTangoHost);
            show(table, event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = -1;
            for (int i=0; i<menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    itemIndex = i;
            switch (itemIndex){
                case STATUS:
                    JOptionPane.showMessageDialog(this, selectedAttribute.getInfo());
                    break;
                case CONFIGURE:
                    selectedAttribute.configureEvent();
                    break;
                case READ_HDB:
                    readHdbItemActionPerformed(null);
                    break;
                case TEST_EVENT:
                    Utils.getTestEvents().add(selectedAttribute.name);
                    break;
                case COPY_ATTR:
                    if (selectedAttribute!=null) {
                        CopyUtils.copyToClipboard(selectedAttribute.name);
                    }
                    break;
            }
        }
    }

    //=========================================================================
    /**
     * Comparator to sort attribute list
     */
    //=========================================================================
    private class AttributeComparator implements Comparator<HdbAttribute> {

        //======================================================
        public int compare(HdbAttribute hdbAttribute1, HdbAttribute hdbAttribute2) {
            switch (selectedColumn) {
                case EVENTS_STAT:
                    return valueSort(hdbAttribute2.nbStatistics, hdbAttribute1.nbStatistics);
                case PERIOD_STAT:
                    return valueSort(hdbAttribute1.averagePeriod, hdbAttribute2.averagePeriod);
                case EVENTS_RESET:
                    return valueSort(hdbAttribute2.nbEvents, hdbAttribute1.nbEvents);
                case PERIOD_RESET:
                    return valueSort(hdbAttribute1.resetPeriod, hdbAttribute2.resetPeriod);
                default:
                    return alphabeticalSort(hdbAttribute1.shortName, hdbAttribute2.shortName);
            }
        }
        //======================================================
        private int alphabeticalSort(String s1, String s2) {
            if (s1==null)      return 1;
            else if (s2==null) return -1;
            else return s1.compareTo(s2);
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
