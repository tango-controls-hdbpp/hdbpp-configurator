//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,...................,2018,2019
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

package org.tango.hdb_configurator.common;

import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.attribute.MultiScalarTableViewer;
import fr.esrf.tangoatk.widget.util.ErrorHistory;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;


/**
 * This class display a set of attributes in a table
 *
 * @author verdier
 */

public class ScalarTableViewer extends MultiScalarTableViewer
        implements IErrorListener,  INumberScalarListener, IStringScalarListener, IBooleanScalarListener, IEnumScalarListener {

    private List<ControlledAttribute> controlledAttributes = new ArrayList<>();
    private AttributeList atkAttributeList = new AttributeList();
    private ScalarTableViewer thisViewer;
    private int tableWidth;
    private static final int ROW_TITLE = 0;   // First column
    private static final int DEFAULT_COL_WIDTH = 80;
    //===============================================================
    /**
     * Build a ScalarTableViewer for a set of attributes
     *
     * @param attributeNames attribute names to be controlled
     * @param rowHeaders     table row titles
     * @param columnHeader  table column title
     * @param unitVisible    display attribute unit
     * @param errorHistory   an ErrorHistory instance
     */
    //===============================================================
    public ScalarTableViewer(List<String> attributeNames,
                             List<String> rowHeaders, String columnHeader, boolean unitVisible, ErrorHistory errorHistory) {
        List<String[]> rowAttributeNames = new ArrayList<>();
        for (String attributeName : attributeNames)
            rowAttributeNames.add(new String[] { attributeName });
        buildViewer(rowAttributeNames, rowHeaders, new String[] { columnHeader }, unitVisible, errorHistory);
    }
    //===============================================================
    /**
     * Build a ScalarTableViewer for a set of attributes
     *
     * @param rowAttributeNames attribute names to be controlled ordered by rows
     * @param rowHeaders        table row titles
     * @param columnHeaders     table column titles
     * @param unitVisible       display attribute unit
     * @param errorHistory      an ErrorHistory instance
     */
    //===============================================================
    public ScalarTableViewer(List<String[]> rowAttributeNames,
                             List<String> rowHeaders, String[] columnHeaders, boolean unitVisible, ErrorHistory errorHistory) {
        buildViewer(rowAttributeNames, rowHeaders, columnHeaders, unitVisible, errorHistory);
    }
    //===============================================================
    //===============================================================
    private void buildViewer(List<String[]> rowAttributeNames,
                             List<String> rowHeaders, String[] columnHeaders, boolean unitVisible, ErrorHistory errorHistory) {
        thisViewer = this;

        //  Set columns
        setNbColumns(columnHeaders.length);
        setColumnIdents(columnHeaders);
        // Set rows
        setNbRows(rowAttributeNames.size());
        setRowIdents(rowHeaders.toArray(new String[0]));
        int row=0;
        for (String[] attributeNames : rowAttributeNames) {
            int column = 0;
            for (String attributeName : attributeNames)
                controlledAttributes.add(new ControlledAttribute(attributeName, row, column++));
            row++;
        }
        new AddAttributeThread().start();

        setFont(new Font("Dialog", Font.BOLD, 12));
        getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
        setUnitVisible(unitVisible);
        setEnabled(false);

        //  Set a default column width
        //  Set column width
        final Enumeration columnEnum = getColumnModel().getColumns();
        int i = 0;
        tableWidth = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            int width;
            if (i==0)  // Row titles
                width = getRowTitleWidth();
            else
                width = DEFAULT_COL_WIDTH;
            tableWidth += width;
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(width);
            i++;
        }
        setRowSelectionAllowed(true);

        //	Attach an error history
        if (errorHistory!=null) {
            atkAttributeList.addErrorListener(errorHistory);
            atkAttributeList.addErrorListener(this);
        }
        atkAttributeList.setRefreshInterval(1000);
        atkAttributeList.startRefresher();
    }
    //===============================================================
    //===============================================================
    @Override
    public void booleanScalarChange(BooleanScalarEvent event) {
        setStatus(event.getSource().toString(), false);
    }
    //===============================================================
    //===============================================================
    @Override
    public void enumScalarChange(EnumScalarEvent event) {
        setStatus(event.getSource().toString(), false);
    }
    //===============================================================
    //===============================================================
    @Override
    public void numberScalarChange(NumberScalarEvent event) {
        setStatus(event.getSource().toString(), false);
    }
    //===============================================================
    //===============================================================
    @Override
    public void stringScalarChange(StringScalarEvent event) {
        setStatus(event.getSource().toString(), false);
    }
    //===============================================================
    //===============================================================
    @Override
    public void stateChange(AttributeStateEvent event) {
        setStatus(event.getSource().toString(), false);
    }
    //===============================================================
    //===============================================================
    @Override
    public void errorChange(ErrorEvent event) {
        setStatus(event.getSource().toString(), true);
    }
    //===============================================================
    //===============================================================
    private void setStatus(String attributeName, boolean b) {
        ControlledAttribute controlledAttribute;
        if (attributeName!=null) {
            controlledAttribute = getAttribute(attributeName);
            if (controlledAttribute !=null)
                controlledAttribute.on_error = b;
        }
    }
    //===============================================================
    //===============================================================
    private ControlledAttribute getAttribute(String attributeName) {
        for (ControlledAttribute controlledAttribute : controlledAttributes) {
            if (attributeName.equals(controlledAttribute.name))
                return controlledAttribute;
        }
        return null;
    }
    //===============================================================
    //===============================================================
    private int getRowTitleWidth() {
        int width = 0;
        for (int row = 0; row < getRowCount(); row++) {
            TableCellRenderer renderer = getCellRenderer(row, ROW_TITLE);
            Component comp = prepareRenderer(renderer, row, ROW_TITLE);
            width = Math.max (comp.getPreferredSize().width, width);
        }
        if (width<30)	width = 30;
        return width;
    }
    //===============================================================
    //===============================================================
    private int getTableHeight() {
        //  Rows as cell in 0,0
        Component cell = getCellRenderer(0,0).
                getTableCellRendererComponent(this, getValueAt(0, 0), false, false, 0, 0);
        Font font = cell.getFont();
        FontMetrics metrics = cell.getFontMetrics(font);
        int cellHeight = metrics.getHeight()+1;

        //  Add header
        JTableHeader header = getTableHeader();
        font = header.getFont();
        int headerHeight = header.getFontMetrics(font).getHeight() + 10; // + border
        return rowIdents.length*cellHeight + headerHeight;
    }
    //===============================================================
    //===============================================================
    public Dimension getTableDimension() {
        // Add a margin in case of scroll bar
        return new Dimension(tableWidth + 40, getTableHeight());
    }
    //===============================================================
    /**
     * Set the attribute value columns widths
     * The row title column width is computed
     * This computation cannot be done for values (because unknown before display).
     *
     * @param widths specified widths
     */
    //===============================================================
    public void setValueColumnWidth(int[] widths) {
        //  Set column width
        final Enumeration columnEnum = getColumnModel().getColumns();
        int i = 0;
        tableWidth = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            if (i<widths.length+1) {
                int width;
                if (i==0)  // Row titles
                    width = getRowTitleWidth();
                else
                    width = widths[i-1];
                tableWidth += width;
                tableColumn = (TableColumn) columnEnum.nextElement();
                tableColumn.setPreferredWidth(width);
            }
            i++;
        }
    }
    //===============================================================
    /**
     * Build a JScrollPane containing this table and compute
     * size for specified column width.
     *
     * @param valueColumnWidth column width for value columns
     * @return the JScrollPane
     */
    //===============================================================
    public JScrollPane getScrollPane(int valueColumnWidth) {
        // Set all value columns with same width
        int[] valueColumnWidths = new int[getNbColumns()];
        Arrays.fill(valueColumnWidths, valueColumnWidth);

        setValueColumnWidth(valueColumnWidths);
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setPreferredSize(getTableDimension());
        return scrollPane;
    }
    //===============================================================
    /**
     * Build a JScrollPane containing this table and compute
     * size for specified column width.
     *
     * @param valueColumnWidths column width for value columns
     * @return the JScrollPane
     */
    //===============================================================
    public JScrollPane getScrollPane(int[] valueColumnWidths) {
        setValueColumnWidth(valueColumnWidths);
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setPreferredSize(getTableDimension());
        return scrollPane;
    }
    //===============================================================
    //===============================================================


    //===========================================================
    //===========================================================
    public static class ControlledAttribute {
        String name;
        int row;
        int col;
        boolean connected = false;
        boolean on_error = true;
        //=======================================================
        public ControlledAttribute(String name, int row, int col) {
            this.name = name;
            this.row = row;
            this.col = col;
        }
        //=======================================================
        public String toString() {
            return name;
        }
    }
    //===========================================================
    //===========================================================



    //===========================================================
    //===========================================================
    class AddAttributeThread extends Thread {
        //=======================================================
        private void connectAttribute(ControlledAttribute controlledAttribute) {
            if (controlledAttribute.name==null) {
                controlledAttribute.connected = true;
                return;
            }
            try {
                //	Add listener on attribute.
                IAttribute iAttribute = (IAttribute) atkAttributeList.add(controlledAttribute.name);

                if (iAttribute instanceof INumberScalar)
                    ((INumberScalar) iAttribute).addNumberScalarListener(thisViewer);
                else if (iAttribute instanceof IStringScalar)
                    ((IStringScalar) iAttribute).addStringScalarListener(thisViewer);
                else if (iAttribute instanceof IEnumScalar)
                    ((IEnumScalar) iAttribute).addEnumScalarListener(thisViewer);
                else if (iAttribute instanceof IBooleanScalar)
                    ((IBooleanScalar) iAttribute).addBooleanScalarListener(thisViewer);
                else
                    System.out.println(controlledAttribute.name + "  Not Supported Type !!");

                //	Set all attribute positions
                setModelAt(iAttribute, controlledAttribute.row, controlledAttribute.col);
                controlledAttribute.connected = true;
            } catch (Exception e) {
                controlledAttribute.connected = false;
            }
        }
        //=======================================================
        public void run() {
            //	Try to connect to all attributes
            int nbConnected = 0;
            while (nbConnected<controlledAttributes.size()) {
                nbConnected = 0;
                for (ControlledAttribute controlledAttribute : controlledAttributes) {
                    if (!controlledAttribute.connected) {
                        connectAttribute(controlledAttribute);
                    }
                    if (controlledAttribute.connected)
                        nbConnected++;
                }
                try { sleep(2000); } catch (Exception e) { /*  */ }
            }
        }
        //=======================================================
    }
}
