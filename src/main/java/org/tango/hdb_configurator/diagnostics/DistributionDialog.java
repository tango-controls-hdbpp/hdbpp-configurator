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
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.chart.*;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static org.tango.hdb_configurator.common.Utils.firstColumnBackground;


//===============================================================
/**
 *	JDialog Class to display attributes distribution as a bar chart
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class DistributionDialog extends JDialog {

	private JFrame	parent;
	private JDialog	thisDialog;
    private SubscriberMap subscriberMap;
    private DeviceProxy configuratorProxy;
    private AttributeChart attributeChartChart;
    private PerformancesChart performancesChart;
    private List<Archiver> archivers = new ArrayList<>();
    private JTable table;
    private int selectedColumn = SUBSCRIBER_NAME;

    private static final int SUBSCRIBER_NAME  = 0;
    private static final int ATTRIBUTE_NUMBER = 1;
    private static final int EVENT_NUMBER     = 2;
    private static final int STORE_TIME       = 3;
    private static final int MAX_PENDING      = 4;
    private static final int RESET_TIME       = 5;
    private static final int RESET_DURATION   = 6;
    private static final String[] columnNames = {
            "Subscriber",
            "Attributes", "Nb Events",
            "Max Store",  "Max Pending",
            "Reset Time", "Duration"
    };
    private static final int[] columnWidth = { 250, 60, 60, 60, 60, 100, 120 };
    private static final Dimension chartDimension = new Dimension(800, 650);
	//===============================================================
	/**
	 *	Creates new form DistributionDialog
	 */
	//===============================================================
	public DistributionDialog(JFrame parent, SubscriberMap subscriberMap) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		thisDialog  = this;
        SplashUtils.getInstance().startSplash();
        initComponents();

        try {
            this.subscriberMap = subscriberMap;
            configuratorProxy = subscriberMap.getConfiguratorProxy();
            attributeChartChart = new AttributeChart();
            attributesPanel.add(attributeChartChart, BorderLayout.CENTER);
            performancesChart = new PerformancesChart();
            performancesPanel.add(performancesChart, BorderLayout.CENTER);
            buildTable();
            if (resetsAtSameTime()) {
                //  If resets could be considered at same time
                //  Display statistics on events for all subscribers
                displayGlobalStatistics();
            }
            else
                globalPanel.setVisible(false);

            titleLabel.setText(attributeChartChart.attributeCount + " Attributes   distributed   in " +
                    subscriberMap.getLabelList().size() + " Subscribers");

            pack();
            ATKGraphicsUtils.centerDialog(this);
            SplashUtils.getInstance().stopSplash();
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            throw e;
        }
	}

	//===============================================================
	//===============================================================
    private static final String Space = "&nbsp;"; // used for toolTips
    private void buildTable() {

        DataTableModel model = new DataTableModel();

        // Create the table
        table = new JTable(model) {
            //	Implements table cell tool tip
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                if (isVisible()) {
                    Point p = e.getPoint();
                    int column = columnAtPoint(p);
                    int row = rowAtPoint(p);
                    if (column==SUBSCRIBER_NAME) {
                        Archiver archiver = archivers.get(row);
                        /*
                        String text = "<b>"+archiver.subscriber.getLabel() + "</b>( " +
                                archiver.subscriber.name() +" )";
                        */
                        String text = "<b>"+archiver.subscriber.getLabel() + "</b>" +
                                Space+ Space + Space + "( " + archiver.subscriber.name() +")";
                        tip = Utils.buildTooltip(text);
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
        /*
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });
        */
        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableHeaderActionPerformed(evt);
            }
        });
        //	Put it in scrolled pane
        JScrollPane scrollPane = new JScrollPane(table);
        model.fireTableDataChanged();
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        //  Set column width
        final Enumeration columnEnum = table.getColumnModel().getColumns();
        int i = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(columnWidth[i++]);
        }
        table.getTableHeader().setReorderingAllowed(false);
        archivers.sort(new ArchiverComparator());
    }
    //===============================================================
    //===============================================================
    private void tableHeaderActionPerformed(java.awt.event.MouseEvent event) {
        //	Get specified column
        selectedColumn = table.getTableHeader().columnAtPoint(new Point(event.getX(), event.getY()));
        archivers.sort(new ArchiverComparator());
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
        javax.swing.JPanel topTopPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        globalPanel = new javax.swing.JPanel();
        globalLabel = new javax.swing.JLabel();
        javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
        attributesPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JRadioButton notOkButton = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JRadioButton OkButton = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JRadioButton eventsButton = new javax.swing.JRadioButton();
        performancesPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JRadioButton processButton = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JRadioButton storeButton = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JRadioButton pendingButton = new javax.swing.JRadioButton();
        tablePanel = new javax.swing.JPanel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Attributes Distribution");
        topTopPanel.add(titleLabel);

        topPanel.add(topTopPanel, java.awt.BorderLayout.PAGE_START);

        globalLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        globalLabel.setText("jLabel5");
        globalPanel.add(globalLabel);

        topPanel.add(globalPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        attributesPanel.setLayout(new java.awt.BorderLayout());

        notOkButton.setSelected(true);
        notOkButton.setText("Attributes Not OK");
        notOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notOkButtonActionPerformed(evt);
            }
        });
        jPanel1.add(notOkButton);

        jLabel1.setText("          ");
        jPanel1.add(jLabel1);

        OkButton.setSelected(true);
        OkButton.setText("Attributes OK");
        OkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkButtonActionPerformed(evt);
            }
        });
        jPanel1.add(OkButton);

        jLabel2.setText("          ");
        jPanel1.add(jLabel2);

        eventsButton.setSelected(true);
        eventsButton.setText("Events Received");
        eventsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventsButtonActionPerformed(evt);
            }
        });
        jPanel1.add(eventsButton);

        attributesPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

        jTabbedPane1.addTab("Attributes", attributesPanel);

        performancesPanel.setLayout(new java.awt.BorderLayout());

        processButton.setSelected(true);
        processButton.setText("Process Time");
        processButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processButtonActionPerformed(evt);
            }
        });
        jPanel2.add(processButton);

        jLabel3.setText("          ");
        jPanel2.add(jLabel3);

        storeButton.setSelected(true);
        storeButton.setText("Store Time");
        storeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeButtonActionPerformed(evt);
            }
        });
        jPanel2.add(storeButton);

        jLabel4.setText("          ");
        jPanel2.add(jLabel4);

        pendingButton.setSelected(true);
        pendingButton.setText("Pending");
        pendingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pendingButtonActionPerformed(evt);
            }
        });
        jPanel2.add(pendingButton);

        performancesPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

        jTabbedPane1.addTab("Performances", performancesPanel);

        tablePanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Table", tablePanel);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose();
	}//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void notOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notOkButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        attributeChartChart.setVisibleCurve(btn.isSelected(),  attributeChartChart.failedDataView);
    }//GEN-LAST:event_notOkButtonActionPerformed

    //===============================================================
    //===============================================================
    private void OkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        attributeChartChart.setVisibleCurve(btn.isSelected(), attributeChartChart.okDataView);
    }//GEN-LAST:event_OkButtonActionPerformed

    //===============================================================
    //===============================================================
    private void eventsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventsButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        attributeChartChart.setVisibleCurve(btn.isSelected(), attributeChartChart.eventDataView);
    }//GEN-LAST:event_eventsButtonActionPerformed

    //===============================================================
    //===============================================================
    private void processButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        performancesChart.setVisibleCurve(btn.isSelected(), performancesChart.processDataView);
    }//GEN-LAST:event_processButtonActionPerformed

    //===============================================================
    //===============================================================
    private void storeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        performancesChart.setVisibleCurve(btn.isSelected(), performancesChart.storeDataView);
    }//GEN-LAST:event_storeButtonActionPerformed

    //===============================================================
    //===============================================================
    private void pendingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pendingButtonActionPerformed
        JRadioButton btn = (JRadioButton) evt.getSource();
        performancesChart.setVisibleCurve(btn.isSelected(), performancesChart.pendingDataView);
    }//GEN-LAST:event_pendingButtonActionPerformed
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
    private void displayGlobalStatistics() {
        long nbEvents = 0;
        for (Archiver archiver : archivers) {
            nbEvents += archiver.totalEvents;
        }
        Archiver archiver = archivers.get(0);
        double average = (double)nbEvents / ((double) archiver.sinceReset/1000);
        String statistics = nbEvents +
                "  events received during " + archiver.getResetDuration() +
                "  ->  average = " + String.format("%.3f", average) + " ev/sec.";
        globalLabel.setText(statistics);
    }
	//===============================================================
	//===============================================================
    private boolean resetsAtSameTime() {

        //  Check delta time on reset
        long maxTime = 0;
        long minTime = System.currentTimeMillis();
        for (Archiver archiver : archivers) {
            if (archiver.resetTime>maxTime) maxTime = archiver.resetTime;
            if (archiver.resetTime<minTime) minTime = archiver.resetTime;
        }
        long delta = maxTime - minTime;
        System.out.println("Delta reset time = " + delta + " ms");
        return delta<5000; //  < 5sec -> Could be considered as same time
    }
	//===============================================================
	//===============================================================



	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributesPanel;
    private javax.swing.JLabel globalLabel;
    private javax.swing.JPanel globalPanel;
    private javax.swing.JPanel performancesPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




    //===============================================================
    /**
     * Generic chart to display distribution
     */
    //===============================================================
    private class DistributionChart extends JLChart{
        protected List<JLDataView> dataViews = new ArrayList<>();
        protected Archiver selectedArchiver = null;
        protected final String[] labels = {
                "========================",
                "Update from archivers",
        };
        protected static final int SEPARATOR = 0;
        protected static final int UPDATE_DATA = 1;
        //===============================================================
        protected DistributionChart() {
            setPreferredSize(chartDimension);

            //  Add JMenuItem to popup menu
            this.addMenuItem(new JMenuItem(labels[SEPARATOR]));
            JMenuItem updateItem = new JMenuItem(labels[UPDATE_DATA]);
            updateItem.setSelected(true);
            updateItem.addActionListener(this::menuActionPerformed);
            this.addMenuItem(updateItem);
        }
        //===============================================================
        protected JLDataView buildCurve(String name, Color color, JLAxis axis) {
            JLDataView  dataView = new JLDataView();
            dataView.setColor(color);
            dataView.setFillColor(color);
            dataView.setName(name);
            dataView.setFill(false);
            dataView.setLabelVisible(true);
            dataView.setViewType(JLDataView.TYPE_BAR);
            dataView.setBarWidth(6);
            dataView.setFillStyle(JLDataView.FILL_STYLE_SOLID);
            axis.addDataView(dataView);

            dataViews.add(dataView);
            return dataView;
        }
        //===============================================================
        protected void buildAxises(String[] names) {
            //  Create X axis.
            int i=0;
            getXAxis().setName(names[i++]);
            getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
            getXAxis().setGridVisible(true);
            getXAxis().setSubGridVisible(true);
            getXAxis().setAutoScale(false);
            getXAxis().setMinimum(0);
            //  Maxi will be set after to know archivers number

            //  Create Y1
            getY1Axis().setName(names[i++]);
            getY1Axis().setAutoScale(true);
            getY1Axis().setScale(JLAxis.LINEAR_SCALE);
            getY1Axis().setGridVisible(true);
            getY1Axis().setSubGridVisible(true);

            //  Create Y2
            getY2Axis().setName(names[i]);
            getY2Axis().setAutoScale(true);
            getY2Axis().setScale(JLAxis.LINEAR_SCALE);
            getY2Axis().setGridVisible(true);
            getY2Axis().setSubGridVisible(true);
        }
        //===============================================================
        private void menuActionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();
            if (cmd.equals(labels[UPDATE_DATA])) {
                try {
                    SplashUtils.getInstance().startSplash();
                    subscriberMap = new SubscriberMap(configuratorProxy);
                    attributeChartChart.updateValues();
                    performancesChart.updateValues();
                    SplashUtils.getInstance().stopSplash();
                }
                catch (DevFailed e) {
                    SplashUtils.getInstance().stopSplash();
                    ErrorPane.showErrorMessage(this, null, e);
                }
            }
            repaint();
        }
        //===============================================================
        protected void setVisibleCurve(boolean b, JLDataView dataView) {
            int i = 0;
            for (JLDataView dv : dataViews) {
                if (dv == dataView) {
                    if (!b) {
                        dataView.getAxis().removeDataView(dataView);
                    }
                    else {
                        if (i==0 || i==1)
                            getY1Axis().addDataView(dataView);
                        else
                            getY2Axis().addDataView(dataView);
                    }
                }
                i++;
            }
            repaint();
        }
        //===============================================================
        protected void resetDataViews() {
            for (JLDataView dataView : dataViews) {
                dataView.reset();
            }
        }
        //===============================================================
    }
    //===============================================================
    //===============================================================





    //===============================================================
    /**
     * JLChart class to display attribute distribution
     */
    //===============================================================
    private class AttributeChart extends DistributionChart  implements IJLChartListener {
        private JLDataView failedDataView;
        private JLDataView okDataView;
        private JLDataView eventDataView;
        private int attributeCount;
        //===============================================================
        private AttributeChart() throws DevFailed {
            String[] axisNames = { "Archivers", "Attributes", "Events Number" };
            String[] curveNames = { "Attributes Not OK", "Attributes OK", "Events Received" };

            setJLChartListener(this);
            buildAxises(axisNames);
            int i =0;
            failedDataView = buildCurve(curveNames[i++], Color.red, getY1Axis());
            okDataView     = buildCurve(curveNames[i++], new Color(0x00aa00), getY1Axis());
            eventDataView  = buildCurve(curveNames[i],   new Color(0x0000aa), getY2Axis());

            updateValues();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    chartMouseClicked(event);
                }
            });
            getXAxis().setMaximum(archivers.size()+0.6);
            getXAxis().setMinimum(-0.2);
        }
        //===============================================================
        private void updateValues() throws DevFailed {
            resetDataViews();
            archivers.clear();
            List<String> labels = subscriberMap.getLabelList();
            SplashUtils.getInstance().reset();
            attributeCount = 0;
            int x = 0;
            for (String label : labels) {
                SplashUtils.getInstance().increaseSplashProgressForLoop(labels.size(), "Reading " + label);
                Archiver archiver = new Archiver(subscriberMap.getSubscriberByLabel(label));

                okDataView.add(x, archiver.attributeOk.length);
                failedDataView.add(x + 0.2, archiver.attributeFailed.length);
                eventDataView.add(x+0.4, archiver.totalEvents);
                attributeCount += archiver.attributeCount();
                archivers.add(archiver);
                x++;
            }
        }
        //===============================================================
        /**
         *  Called after implementation of clickOnChart method
         *  to add another feature (display attributes on error)
         */
        //===============================================================
        private void chartMouseClicked(MouseEvent event) {
            int mask = event.getModifiers();
            if (event.getClickCount()==2 && (mask & MouseEvent.BUTTON1_MASK)!=0) {
                if (selectedArchiver!=null) {
                    try {
                        String[] attributeList =
                                ArchiverUtils.getAttributeList(selectedArchiver.subscriber, "Nok");
                        if (attributeList.length>0) {
                            new FaultyAttributesDialog(
                                    thisDialog, selectedArchiver.subscriber).setVisible(true);
                        }
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, e.getMessage(), e);
                    }
                }
            }
        }
        //===============================================================
        @Override
        public String[] clickOnChart(JLChartEvent event) {
            int index = event.getDataViewIndex();
            Archiver archiver = archivers.get(index);
            selectedArchiver = archiver;

            if (archiver!=null) {
                //  Check mouse modifier
                MouseEvent mouseEvent = event.getMouseEvent();
                if (archiver.attributeFailed.length>0 && mouseEvent!=null &&
                        (mouseEvent.getModifiers() & MouseEvent.SHIFT_MASK)!=0)
                    return null;

                //  Display archiver info
                List<String>   lines = new ArrayList<>();
                lines.add(archiver.title);
                if (archiver.resetTime>0) {
                    lines.add("Since " + archiver.getResetTime() +
                            "  (" + archiver.getResetDuration() + ")");
                }
                lines.add(archiver.totalEvents + " Events Received");
                if ( archiver.attributeOk.length==0)
                    lines.add(" - NO  Attribute OK");
                else
                    lines.add(" - " + archiver.attributeOk.length + " attributes are OK");

                if ( archiver.attributeFailed.length==0)
                    lines.add(" - NO  Attribute Failed");
                else
                    lines.add(" - " + archiver.attributeFailed.length + " attributes Failed");

                int cnt = 0;
                for (String attribute : archiver.attributeFailed) {
                    lines.add("    - " + attribute);
                    if (cnt++>15) {
                        lines.add("       - - - - -");
                        lines.add("       Use double click for Full List");
                        break;
                    }
                }

                String[]    array = new String[lines.size()];
                for (int i=0 ; i<lines.size() ; i++)
                    array[i] = lines.get(i);
                return array;
            }
            else
                return new String[0];
        }

        //===============================================================
    }
    //===============================================================
    //===============================================================



    //===============================================================
    /**
     * JLChart class to display performances distribution
     */
    //===============================================================
    private class PerformancesChart extends DistributionChart implements IJLChartListener {
        private JLDataView processDataView;
        private JLDataView storeDataView;
        private JLDataView pendingDataView;
        //===============================================================
        private PerformancesChart() throws DevFailed {
            String[] axisNames = { "Archivers", "Time (millis)", "Attributes" };
            String[] curveNames = { "Process Time", "Store Time", "Max Pending" };
            setJLChartListener(this);
            buildAxises(axisNames);
            int i=0;
            processDataView = buildCurve(curveNames[i++], Color.red, getY1Axis());
            storeDataView   = buildCurve(curveNames[i++], new Color(0x00aa00), getY1Axis());
            pendingDataView = buildCurve(curveNames[i],   new Color(0x0000aa), getY2Axis());

            updateValues();
            getXAxis().setMaximum(archivers.size()+0.6);
        }
        //===============================================================
        private void updateValues() throws DevFailed {
            resetDataViews();
            archivers.clear();
            List<String> labels = subscriberMap.getLabelList();
            SplashUtils.getInstance().reset();
            int x = 0;
            for (String label : labels) {
                SplashUtils.getInstance().increaseSplashProgressForLoop(labels.size(), "Reading " + label);
                Archiver archiver = new Archiver(subscriberMap.getSubscriberByLabel(label));

                processDataView.add(x, archiver.maxProcess*1000.0);
                storeDataView.add(x + 0.2, archiver.maxStore*1000.0);
                pendingDataView.add(x + 0.4, archiver.pending);
                archivers.add(archiver);
                x++;
            }
        }
        //===============================================================
        @Override
        public String[] clickOnChart(JLChartEvent event) {
            int index = event.getDataViewIndex();
            Archiver archiver = archivers.get(index);

            if (archiver!=null) {
                //  Check mouse modifier
                MouseEvent mouseEvent = event.getMouseEvent();
                if (archiver.attributeFailed.length>0 && mouseEvent!=null &&
                        (mouseEvent.getModifiers() & MouseEvent.SHIFT_MASK)!=0)
                    return null;

                //  Display archiver info
                List<String>   lines = new ArrayList<>();
                lines.add(archiver.title);
                if (archiver.resetTime>0) {
                    lines.add("Since " + archiver.getResetTime() +
                            "  (" + archiver.getResetDuration() + ")");
                }
                lines.add(archiver.totalEvents + " Events Received");
                lines.add(" - Max process time: " + Utils.strPeriod(archiver.maxProcess));
                lines.add(" - Max store time: "   + Utils.strPeriod(archiver.maxStore));
                lines.add(" - Min store time: "   + Utils.strPeriod(archiver.minStore));
                lines.add(" - Max pending:    "   + archiver.pending   + " att.");

                String[]    array = new String[lines.size()];
                for (int i=0 ; i<lines.size() ; i++)
                    array[i] = lines.get(i);
                return array;
            }
            else
                return new String[0];
        }

        //===============================================================
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    private static class Archiver {
        private Subscriber  subscriber;
        private String      title;
        private String[]    attributeOk;
        private String[]    attributeFailed;
        private int         totalEvents = 0;
        private int         pending;
        private double      maxProcess;
        private double      minProcess;
        private double      maxStore;
        private double      minStore;
        private long        resetTime=-1;
        private long        sinceReset=0;
        //===========================================================
        private Archiver(Subscriber subscriber) {
            this.subscriber = subscriber;
            title = subscriber.getLabel() + "  (" +
                    TangoUtils.getOnlyDeviceName(subscriber.getName()) + ") :";
            update();
        }
        //===========================================================
        private void update() {
            try {
                DeviceAttribute[] attributes = subscriber.read_attribute(
                        new String[]{
                                "AttributeOkList",
                                "AttributeNOkList",
                                "AttributeEventNumberList",
                                "AttributeMinProcessingTime",
                                "AttributeMaxProcessingTime",
                                "AttributeMaxStoreTime",
                                "AttributeMinStoreTime",
                                "AttributeMaxPendingNumber"
                        });
                int i = 0;
                DeviceAttribute attribute = attributes[i++];
                if (attribute.hasFailed())
                    attributeOk = new String[0];
                else
                    attributeOk = attribute.extractStringArray();

                attribute = attributes[i++];
                if (attribute.hasFailed())
                    attributeFailed = new String[0];
                else
                    attributeFailed = attribute.extractStringArray();

                attribute = attributes[i++];
                if (!attribute.hasFailed()) {
                    int[] nbEvents = attribute.extractLongArray();
                    totalEvents = 0;
                    for (int nb : nbEvents)
                        totalEvents += nb;
                }
                resetTime = subscriber.getStatisticsResetTime();
                if (resetTime>0) {
                    sinceReset = System.currentTimeMillis() - resetTime;
                }

                //  extraction for performances
                attribute = attributes[i++];
                if (attribute.hasFailed())  maxProcess = 0;
                else maxProcess = attribute.extractDouble();
                if (maxProcess<0)   maxProcess = 0.0;
                attribute = attributes[i++];
                if (attribute.hasFailed())  minProcess = 0;
                else minProcess = attribute.extractDouble();
                if (minProcess<0)   minProcess = 0.0;

                attribute = attributes[i++];
                if (attribute.hasFailed())  maxStore= 0;
                else maxStore = attribute.extractDouble();
                if (maxStore<0)   maxStore = 0.0;
                attribute = attributes[i++];
                if (attribute.hasFailed())  minStore = 0;
                else minStore = attribute.extractDouble();
                if (minStore<0)   minStore = 0.0;

                attribute = attributes[i];
                if (attribute.hasFailed())  pending = 0;
                else pending = attribute.extractLong();
            }
            catch (DevFailed e) {
                attributeOk = new String[0];
                attributeFailed = new String[0];
            }
        }
        //===========================================================
        private int attributeCount() {
            return attributeOk.length + attributeFailed.length;
        }
        //===========================================================
        public String getResetTime() {
            return Utils.formatDateTime(resetTime);
        }
        //===========================================================
        public String getResetDuration() {
            return  Utils.strPeriod((double) sinceReset / 1000);

        }
        //===========================================================
        public String toString() {
            return subscriber.getLabel();
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
            return archivers.size();
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
            setBackground(getBackground(column));
            Archiver archiver = archivers.get(row);
            switch (column) {
                case SUBSCRIBER_NAME:
                    setText(archiver.subscriber.getLabel());
                    break;
                case ATTRIBUTE_NUMBER:
                    setText(Integer.toString(archiver.attributeCount()));
                    break;
                case EVENT_NUMBER:
                    setText(Integer.toString(archiver.totalEvents));
                    break;
                case STORE_TIME:
                    setText(Utils.strPeriod(archiver.maxStore));
                    break;
                case MAX_PENDING:
                    setText(Integer.toString(archiver.pending));
                    break;
                case RESET_TIME:
                    if (archiver.resetTime>0)
                        setText(archiver.getResetTime());
                    else
                        setText("Not available");
                    break;
                case RESET_DURATION:
                    if (archiver.resetTime>0)
                        setText(archiver.getResetDuration());
                    else
                        setText("Not available");
                    break;
            }
            return this;
        }
        //==========================================================
        private Color getBackground(int column) {
            if (column == SUBSCRIBER_NAME) {
                return firstColumnBackground;
            }
            return Color.white;
        }
        //==========================================================
    }
    //=========================================================================
    //=========================================================================




    //=========================================================================
    /**
     * Comparator to sort attribute list
     */
    //=========================================================================
    private class ArchiverComparator implements Comparator<Archiver> {

        //======================================================
        public int compare(Archiver archiver1, Archiver archiver2) {
            switch (selectedColumn) {
                case ATTRIBUTE_NUMBER:
                    return valueSort(archiver2.attributeCount(), archiver1.attributeCount());
                case EVENT_NUMBER:
                    return valueSort(archiver2.totalEvents, archiver1.totalEvents);
                case STORE_TIME:
                    return valueSort(archiver2.maxStore, archiver1.maxStore);
               case MAX_PENDING:
                    return valueSort(archiver2.pending, archiver1.pending);
                case RESET_TIME:
                    return valueSort(archiver1.resetTime, archiver2.resetTime);
                case RESET_DURATION:
                    return valueSort(archiver1.sinceReset, archiver2.sinceReset);
                default:
                    return alphabeticalSort(
                            archiver1.subscriber.getLabel(), archiver2.subscriber.getLabel());
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
