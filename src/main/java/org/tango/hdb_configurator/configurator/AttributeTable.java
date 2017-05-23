//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015
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

package org.tango.hdb_configurator.configurator;

import org.tango.hdb_configurator.common.HdbAttribute;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


//===============================================================
/**
 *	JTable to display HdbAttributes
 *
 *	@author  Pascal Verdier
 */
//===============================================================

public class AttributeTable extends JTable {
    private List<HdbAttribute> attributeList = new ArrayList<>();
    private int tableWidth = 0;
    private DataTableModel model;

	private static final String[] columnNames = { "Attribute", "Strategy", };
	private static final int[] columnWidth = { 500, 100 };
	private static final int ATTRIBUTE_NAME     = 0;
	private static final int ATTRIBUTE_STRATEGY = 1;
	//===============================================================
	/**
	 *	Creates new form UpTimeTable
	 */
	//===============================================================
	public AttributeTable() {

		// Create the table
        model = new DataTableModel();
        setModel(model);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(true);
		setDragEnabled(false);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));

		//  Set column width
		final Enumeration columnEnum = getColumnModel().getColumns();
		int i = 0;
		TableColumn tableColumn;
		while (columnEnum.hasMoreElements()) {
			tableWidth += columnWidth[i];
			tableColumn = (TableColumn) columnEnum.nextElement();
			tableColumn.setPreferredWidth(columnWidth[i++]);
		}
    }
    //===============================================================
    //===============================================================
    void setSelected(int row) {
        //  Seems that does not work. (?)
        getSelectionModel().setSelectionInterval(row, row);
        //this.setRowSelectionInterval(row, row);
        //model.fireTableDataChanged();
    }
    //===============================================================
    //===============================================================
    List<HdbAttribute> getSelectedAttributes() {
        int[] rows = getSelectedRows();
        List<HdbAttribute> attributes = new ArrayList<>(rows.length);
        for (int row : rows) {
            attributes.add(attributeList.get(row));
        }
        return attributes;
    }
    //===============================================================
    //===============================================================
    HdbAttribute getAttribute(int index) {
        return attributeList.get(index);
    }
    //===============================================================
    //===============================================================
    List<HdbAttribute> getAttributeList() {
        return attributeList;
    }
    //===============================================================
    //===============================================================
    int getTableWidth() {
        return tableWidth;
    }
    //===============================================================
    //===============================================================
    void fireTableDataChanged() {
        model.fireTableDataChanged();
    }
    //===============================================================
    //===============================================================
    void updateAttributeList(List<HdbAttribute> attributeList) {
        this.attributeList = attributeList;
        model.fireTableDataChanged();
    }
    //===============================================================
    //===============================================================


	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//==============================================================
	/**
	 * The Table model
	 */
	//==============================================================
	public class DataTableModel extends AbstractTableModel {
		//==========================================================
		public int getColumnCount() {
			return columnNames.length;
		}

		//==========================================================
		public int getRowCount() {
			return attributeList.size();
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
			//return "";
            HdbAttribute hdbAttribute = attributeList.get(row);
            switch (column) {
                case ATTRIBUTE_NAME:
                    return hdbAttribute.getName();
                default:
                    return hdbAttribute.strategyToString();
            }
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
			if (isVisible()) {
				return getValueAt(0, column).getClass();
			}
			else
				return null;
		}
		//==========================================================
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
			//setFont(new Font("Dialog", Font.BOLD, 11));
			setOpaque(true); //MUST do this for background to show up.
		}

		//==========================================================
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {
			//setBackground(getBackground(row, column));
			switch (column) {
				case ATTRIBUTE_NAME:
					setText(attributeList.get(row).getName());
					break;
				case ATTRIBUTE_STRATEGY:
					setText(attributeList.get(row).strategyToString());
					break;
			}
			return this;
		}
		//==========================================================
	}
	//==============================================================
	//==============================================================
}
