//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
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
// :  $
//
//-======================================================================

package org.tango.hdb_configurator.statistics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;
import org.tango.hdb_configurator.configurator.HdbConfigurator;
import org.tango.hdb_configurator.diagnostics.HdbDiagnostics;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.tango.hdb_configurator.common.Utils.firstColumnBackground;

/**
 * This class is a table to display statistics
 *
 * @author verdier
 */

public class StatisticsTable extends JTable {
    private JFrame parentFrame;
    private JDialog parentDialog;
    private DataTableModel tableModel;
    private List<StatAttribute> filteredStatAttributes;
    private List<StatAttribute> statAttributes;
    private TablePopupMenu popupMenu;
    private List<String> columnNames = new ArrayList<>();
    private boolean hasSeveralTangoHosts = false;
    private int selectedRow = -1;

    static final  String[] COLUMN_NAMES = {
            "Attribute Names", "Ev. since reset", "Average", };

    static final int ATTRIBUTE_NAME = 0;
    static final int EVENTS_NUMBER = 1;
    static final int AVERAGE        = 2;
	//===============================================================
 	//===============================================================
    StatisticsTable(JFrame parentFrame, JDialog parentDialog, List<StatAttribute> statAttributes) {
        this.parentFrame = parentFrame;
        this.parentDialog = parentDialog;
        this.statAttributes = statAttributes;
        filteredStatAttributes = new ArrayList<>(statAttributes);
        popupMenu = new TablePopupMenu(this);
        checkIfHasSeveralTangoHosts();

        // Create the table
        tableModel = new DataTableModel();
        setModel(tableModel);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setDragEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
        setDefaultRenderer(String.class, new LabelCellRenderer());
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });
        getTableHeader().setReorderingAllowed(false);
    }
	//===============================================================
	//===============================================================
    List<StatAttribute> getFilteredStatAttributes() {
        return filteredStatAttributes;
    }
    //===============================================================
	//===============================================================
    List<StatAttribute> applyFilter(String filter) {
        filteredStatAttributes = new ArrayList<>();
        for (StatAttribute statAttribute : statAttributes) {
            if (statAttribute.shortName.contains(filter)) {
                filteredStatAttributes.add(statAttribute);
            }
        }
        tableModel.fireTableDataChanged();
        return filteredStatAttributes;
    }
    //===============================================================
    //===============================================================
    List<StatAttribute> updateAttributes(List<StatAttribute> statAttributes, String filter) {
        this.statAttributes = statAttributes;
        return applyFilter(filter);
    }
    //===============================================================
    //===============================================================
    String getDataForFile() {
        StringBuilder   sb = new StringBuilder();
        for (String s : columnNames) {
            sb.append(s).append('\t');
        }
        sb.trimToSize();
        sb.append('\n');
        for (StatAttribute statAttribute : filteredStatAttributes) {
            sb.append(statAttribute.name).append('\t')
                    .append(statAttribute.nbStatistics).append('\t')
                    .append(statAttribute.nbEvents).append('\t')
                    .append(statAttribute.averageString).append('\n');
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private void checkIfHasSeveralTangoHosts() {
        Collections.addAll(columnNames, COLUMN_NAMES);

        //  Check if several tango hosts
        String tangoHost = statAttributes.get(0).tangoHost;
        for (StatAttribute attribute : statAttributes) {
            if (!tangoHost.equals(attribute.tangoHost)) {
                hasSeveralTangoHosts = true;
                break;
            }
        }
        //  If several tango hosts --> add a first column to display it
        if (hasSeveralTangoHosts) {
            columnNames.add(0, "");
        }
        else {
            // add tango host in first column header
            String s = columnNames.remove(0);
            s += "  (" + tangoHost + ")";
            columnNames.add(0, s);
        }
    }
	//===============================================================
	//===============================================================
	private int getColumnWidth(int column) {
		int width = 0;
		for (int row = 0 ; row<getRowCount() ; row++) {
			TableCellRenderer renderer = getCellRenderer(row, column);
			Component comp = prepareRenderer(renderer, row, column);
			width = Math.max(comp.getPreferredSize().width, width);
		}
        if (width<80) width = 80;
        //System.out.println("Column " + columnNames.get(column) + " width: " + width);
		return width;

	}
	//===============================================================
	//===============================================================
	private int getTableHeight() {
		Component cell = getCellRenderer(0, 0).
				getTableCellRendererComponent(this, getValueAt(0, 0), false, false, 0, 0);
		Font font = cell.getFont();
		FontMetrics metrics = cell.getFontMetrics(font);
		int cellHeight = metrics.getHeight() + 1;

		JTableHeader header = getTableHeader();
		font = header.getFont();
		int headerHeight = header.getFontMetrics(font).getHeight() + 10; // + border
		return statAttributes.size() * cellHeight + headerHeight;
	}
    //===============================================================
    //===============================================================
    Dimension getTableDimension() {
        int tableWidth = 0;
        //  Set column width
        final Enumeration columnEnum = getColumnModel().getColumns();
        int i = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            int width = getColumnWidth(i++);
            tableWidth += width;
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(width);
        }
        return new Dimension(tableWidth, getTableHeight());
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent event) {
        //	get selected signal
        Point clickedPoint = new Point(event.getX(), event.getY());
        int row = rowAtPoint(clickedPoint);
        selectedRow = row;
        repaint();

        if (event.getClickCount() == 2) {
            popupAttributeStatus(filteredStatAttributes.get(row));
        }
        else {
            int mask = event.getModifiers();

            //  Check button clicked
            if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
                popupMenu.showMenu(event, filteredStatAttributes.get(row));
            }
        }
    }
    //===============================================================
    //===============================================================
    private void testDevice(StatAttribute statAttribute) {
        try {
            TangoUtils.testDevice(parentDialog,
                    statAttribute.name.substring(0, statAttribute.name.lastIndexOf('/')));
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //===============================================================
    //===============================================================
    private void popupAttributeStatus(StatAttribute statAttribute) {
        new PopupHtml(parentDialog, true).show(statAttribute.getInfo(),
                "Attribute: "+ statAttribute.name, 600, 650);
    }
    //=======================================================
    //=======================================================
    private void configureArchiver(Subscriber subscriber) {
            try {
                HdbConfigurator hdbConfigurator;
                if (parentFrame instanceof HdbDiagnostics) {
                    hdbConfigurator = ((HdbDiagnostics) parentFrame).getHdbConfigurator();
                    hdbConfigurator.selectArchiver(subscriber.getLabel());
                }
                else {
                    // From external, start configurator
                    // with event tango host if unique
                    List<String> tangoHostList = subscriber.getTangoHostList();
                    hdbConfigurator = new HdbConfigurator(
                            parentFrame, subscriber.getConfiguratorProxy(),
                            tangoHostList.size()==1? tangoHostList.get(0) : null);
                }
                hdbConfigurator.setVisible(true);
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
    }
    //===============================================================
    //===============================================================
    private void readOnAttributeFromHdb() {
        if (selectedRow<0) {
            Utils.popupError(this, "No attribute selected !");
            return;
        }
        StatAttribute attribute = filteredStatAttributes.get(selectedRow);
        System.out.println("Display " + attribute.name);
        try {
            //  Use new HDB API
            Utils.startHdbViewer(attribute.name);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.toString(), e);
        }
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
            // If one subscriber --> one reset time
            return columnNames.size();
        }
        //==========================================================
        public int getRowCount() {
            return filteredStatAttributes.size();
        }
        //==========================================================
        public String getColumnName(int columnIndex) {
            String title;
            title = columnNames.get(columnIndex);

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
            setToolTipText("");
            StatAttribute attribute = filteredStatAttributes.get(row);
            if (hasSeveralTangoHosts) column--;
            setBackground(getBackground(row, column));
            switch (column) {
                case -1: // TANGO_HOST only if several
                    setText(attribute.tangoHost);
                    setToolTipText(attribute.nameTooltip);
                    break;
                case ATTRIBUTE_NAME:
                    setText(attribute.shortName);
                    setToolTipText(attribute.nameTooltip);
                    break;
                case EVENTS_NUMBER:
                    setText("  " + attribute.nbEvents + "  ");
                    setToolTipText(attribute.resetTooltip);
                    break;
                case AVERAGE:
                    setText("  " + attribute.averageString + "  ");
                    setToolTipText(attribute.resetTooltip);
                    break;
            }
            return this;
        }
        //==========================================================
        private Color getBackground(int row, int column) {
            if (column <= 0) {
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
    private static final int STATUS             = 0;
    private static final int READ_HDB           = 1;
    private static final int TEST_DEVICE        = 2;
    private static final int CONFIGURE_POLLING  = 3;
    private static final int CONFIGURE_ARCHIVER = 4;
    private static final int TEST_EVENT         = 5;
    private static final int COPY_ATTR          = 6;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Status",
            "Read attribute from HDB",
            "Test Device",
            "Configure Polling/Events",
            "Configure Archiver",
            "Test Event",
            "Copy attribute name",
    };
    //=======================================================
    //=======================================================
    private class TablePopupMenu extends JPopupMenu {
        private JTable table;
        private JLabel title;
        private StatAttribute selectedAttribute;
        private boolean configuratorAllowed;
        //======================================================
        private TablePopupMenu(JTable table) {
            this.table = table;
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

            checkIfConfiguratorAllowed();
        }
        //======================================================
        private void checkIfConfiguratorAllowed() {
            configuratorAllowed = true;
            // If from HdbDiagnostics allowed
            // only if all subscribers are registered in one configurator
            if (parentFrame instanceof HdbDiagnostics) {
                DeviceProxy configuratorProxy = statAttributes.get(0).subscriber.getConfiguratorProxy();
                for (StatAttribute attribute : statAttributes) {
                    if (attribute.subscriber.getConfiguratorProxy()!=configuratorProxy) {
                        configuratorAllowed = false;
                        break;
                    }
                }
            }
        }
        //======================================================
        private void showMenu(MouseEvent event, StatAttribute statAttribute) {
            title.setText(statAttribute.shortName);
            selectedAttribute = statAttribute;
            // Too much subscriptions already done -> does not work
            getComponent(OFFSET + TEST_EVENT).setVisible(false/*Utils.getTestEvents()!=null*/);
            getComponent(OFFSET + CONFIGURE_POLLING).setEnabled(statAttribute.useDefaultTangoHost);
            getComponent(OFFSET + CONFIGURE_ARCHIVER).setVisible(configuratorAllowed);

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
                    popupAttributeStatus(selectedAttribute);
                    break;
                case CONFIGURE_POLLING:
                    selectedAttribute.configureEvent();
                    break;
                case CONFIGURE_ARCHIVER:
                    configureArchiver(selectedAttribute.subscriber);
                    break;
                case READ_HDB:
                    readOnAttributeFromHdb();
                    break;
                case TEST_DEVICE:
                    testDevice(selectedAttribute);
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
    //=========================================================================
}

