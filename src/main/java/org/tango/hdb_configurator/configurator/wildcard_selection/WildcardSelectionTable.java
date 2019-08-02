//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// Pascal Verdier: pascal_verdier $
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

package org.tango.hdb_configurator.configurator.wildcard_selection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This class is able to display data on a table
 *
 * @author verdier
 */

public class WildcardSelectionTable extends JTable {
    private List<Row> rowList = new ArrayList<>();
    private int tableWidth = 0;
    private TablePopupMenu popupMenu = new TablePopupMenu();

    private static final int[] COLUMN_WIDTH = {400, 50};
    private static final String[] COLUMN_HEADERS = {
            "Attributes", "Sel.",
    };
    //===============================================================
    //===============================================================
    private static class Row {
        private String attributeName;
        private boolean selected = true;
        private Row(String attributeName) {
            this.attributeName = attributeName;
        }
    }
    //===============================================================
    //===============================================================
    public WildcardSelectionTable(List<String> attributeList) {
        for (String attribute : attributeList)
            rowList.add(new Row(attribute));

        // Create the table
        DataTableModel tableModel = new DataTableModel();
        setModel(tableModel);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new LabelCellRenderer());
        getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableHeaderActionPerformed(event);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableActionPerformed(event);
            }
        });
        //  Column header management
        getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));

        //  Set column width
        final Enumeration columnEnum = getColumnModel().getColumns();
        int i = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            tableWidth += COLUMN_WIDTH[i];
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(COLUMN_WIDTH[i++]);
        }
        updateHeader();
    }
    //===============================================================
    //===============================================================
    private void tableHeaderActionPerformed(MouseEvent event) {
        JTableHeader tableHeader = (JTableHeader) event.getSource();
        int column = tableHeader.columnAtPoint(event.getPoint());

        if (column==1 && event.getButton()==3) {
            popupMenu.showMenu(event, "Selection");
        }
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(MouseEvent event) {
        int selectedRow = rowAtPoint(new Point(event.getX(), event.getY()));
        if (event.getButton() == 1) {
            rowList.get(selectedRow).selected = !rowList.get(selectedRow).selected;
            updateHeader();
            repaint();
        }
    }
    //===============================================================
    //===============================================================
    public int getTableWidth() {
        return tableWidth;
    }
    //===============================================================
    //===============================================================
    public List<String> getSelection() {
        List<String> list = new ArrayList<>();
        for (Row row : rowList) {
            if (row.selected)
                list.add(row.attributeName);
        }
        return list;
    }
    //===============================================================
    //===============================================================
    public int getNbAttributeSelected() {
        int nb = 0;
        for (Row row : rowList) {
            if (row.selected)
                nb++;
        }
        return nb;
    }
    //===============================================================
    //===============================================================
    private void setAttributeSelected(boolean b) {
        for (Row row : rowList) {
            row.selected = b;
        }
        updateHeader();
        repaint();
    }
    //===============================================================
    //===============================================================
    private void updateHeader() {
        JTableHeader tableHeader = getTableHeader();
        TableColumn column = tableHeader.getColumnModel().getColumn(0);
        column.setHeaderValue(rowList.size() + " Attributes (" + getNbAttributeSelected() + " selected)");
        //tableHeader.setBackground(Color.yellow);
        tableHeader.repaint();
    }
    //===============================================================
    //===============================================================


    //==============================================================
    /**
     * The Table model
     */
    //==============================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        @Override
        public int getColumnCount() {
            return COLUMN_HEADERS.length;
        }
        //==========================================================
        @Override
        public int getRowCount() {
            return rowList.size();
        }
        //==========================================================
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex >= getColumnCount())
                return COLUMN_HEADERS[getColumnCount() - 1];
            else
                return COLUMN_HEADERS[columnIndex];
        }
        //==========================================================
        @Override
        public Object getValueAt(int row, int column) {
            if (column == 1)
                return rowList.get(row).selected;
            return "";  //rowList.get(row)[column];
        }
        //==========================================================
        @Override
        public void setValueAt(Object value, int row, int column) {
        }
        //==========================================================
        /**
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         *
         * @param column the specified co;umn number
         * @return the cell class at first row for specified column.
         */
        //==========================================================
        @Override
        public Class getColumnClass(int column) {
            if (isVisible()) {
                if (column == 0)
                    return String.class;
                else
                    return Boolean.class;
            } else
                return null;
        }
        //==========================================================
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        //==========================================================
    }
    //==============================================================
    //==============================================================


    //==============================================================
    /**
     * Renderer to set cell color
     */
    //==============================================================
    public class LabelCellRenderer extends JLabel implements TableCellRenderer {
        //==========================================================
        public LabelCellRenderer() {
            setFont(new Font("Dialog", Font.BOLD, 12));
            setOpaque(true);
        }
        //==========================================================
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(" " + rowList.get(row).attributeName);
            return this;
        }
    }
    //==============================================================
    //==============================================================


    //===========================================================
    //===========================================================
    private static final int SELECT_ALL = 0;
    private static final int UN_SELECT_ALL = 1;
    private static final int OFFSET = 2;    //  Label + separator

    private static String[] menuLabels = {
            "Select All",
            "Un Select All",
    };

    private class TablePopupMenu extends JPopupMenu {
        //=======================================================
        //=======================================================
        private TablePopupMenu() {
            JLabel title = new JLabel("");
            title.setFont(new Font("Dialog", Font.BOLD, 14));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::menuActionPerformed);
                add(btn);
            }
        }
        //======================================================
        public void showMenu(MouseEvent event, String name) {
            ((JLabel) getComponent(0)).setText(name);
            show((Component) event.getSource(), event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent event) {
            //  Check component source
            Object obj = event.getSource();
            int commandIndex = 0;
            for (int i=0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i) == obj)
                    commandIndex = i;

            switch (commandIndex) {
                case SELECT_ALL:
                    setAttributeSelected(true);
                    break;
                case UN_SELECT_ALL:
                    setAttributeSelected(false);
                    break;
            }
        }
        //======================================================
    }
}
