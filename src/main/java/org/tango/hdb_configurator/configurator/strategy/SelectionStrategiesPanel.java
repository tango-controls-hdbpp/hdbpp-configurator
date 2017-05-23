//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015,2016
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

package org.tango.hdb_configurator.configurator.strategy;

import org.tango.hdb_configurator.common.Context;
import org.tango.hdb_configurator.common.Strategy;
import org.tango.hdb_configurator.common.Utils;
import org.tango.hdb_configurator.common.HdbAttribute;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.tango.hdb_configurator.common.Strategy.ALWAYS_INDEX;
import static org.tango.hdb_configurator.common.Utils.getTableColumnWidth;


//===============================================================
/**
 *	JDialog Class to select strategies for attributes
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class SelectionStrategiesPanel extends JPanel {
    private List<HdbAttribute> hdbAttributeList;
    private List<String> columnNames = new ArrayList<>();
	private Strategy strategy;
	private static final int NAME = 0;
	private static final int ALWAYS_COLUMN = 1;
	private static final String howTo = "\nSelect strategies to be used for each attribute";
	//===============================================================
	/**
	 *	Creates new form StrategyDialog
	 */
	//===============================================================
	public SelectionStrategiesPanel(Strategy strategy, List<HdbAttribute> attributeList) {
	    this.hdbAttributeList = attributeList;
		this.strategy = strategy;

		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		JLabel label = new JLabel("HDB Strategies Selection");
		label.setFont(new Font("Dialog", Font.BOLD, 14));
		panel.add(label);
		add(panel, BorderLayout.NORTH);
		columnNames.add("Attributes");
		for (Context context : strategy) {
            columnNames.add(context.getName());
        }
		buildTable();
	}
	//===============================================================
	//===============================================================
	private void buildTable() {
		StrategyTableModel model = new StrategyTableModel();
		//noinspection NullableProblems
		JTable table = new JTable(model) {
			public String getToolTipText(MouseEvent event) {
				return manageTooltip(event);
			}
		};
		table.setDefaultRenderer(String.class, new LabelCellRenderer());
		table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
		table.getTableHeader().setToolTipText(Utils.buildTooltip(howTo));
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				tableActionPerformed(evt);
			}
		});
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				headerActionPerformed(evt);
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		//	Check name column width
		int nameWidth = getNameColumnWidth();

		//  Set column width
		final Enumeration columnEnum = table.getColumnModel().getColumns();
        int i = 0;
		int width = 0;
		TableColumn tableColumn;
		while (columnEnum.hasMoreElements()) {
			width += (i==0)? nameWidth : getColumnWidth(columnNames.get(i));
			tableColumn = (TableColumn) columnEnum.nextElement();
			tableColumn.setPreferredWidth((i==0)? nameWidth : getColumnWidth(columnNames.get(i)));
            i++;
		}
		scrollPane.setPreferredSize(new Dimension(width, 40+ hdbAttributeList.size()*16));
	}
	//===============================================================
	//===============================================================
	private String manageTooltip(MouseEvent event) {
		JTable table = (JTable) event.getSource();
		String tip = null;
		if (isVisible()) {
			Point p = event.getPoint();
			switch (table.columnAtPoint(p)) {
				case NAME:
					tip = Utils.buildTooltip(howTo);
					break;
				default:
					Context context = strategy.get(table.columnAtPoint(p)-1);
					tip = Utils.buildTooltip(context.getHtmlDescription());
					break;
			}
		}
		return tip;
	}
	//===============================================================
	//===============================================================
	public List<HdbAttribute> getHdbAttributeList() {
        return hdbAttributeList;
	}
	//===============================================================
	//===============================================================
	private void headerActionPerformed(MouseEvent event) {
        JTableHeader tableHeader = (JTableHeader) event.getSource();
        int column = tableHeader.columnAtPoint(event.getPoint());
        //  Check button clicked
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK)!=0) {
            for (int row=0 ; row<hdbAttributeList.size() ; row++)
                manageCellClicked(row, column);
        }
    }
	//===============================================================
	//===============================================================
	private void tableActionPerformed(MouseEvent event) {
		JTable table = (JTable) event.getSource();
		Point clickedPoint = new Point(event.getX(), event.getY());
		int selectedRow = table.rowAtPoint(clickedPoint);
		int column = table.columnAtPoint(clickedPoint);
        //  Check button clicked
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK)!=0) {
            manageCellClicked(selectedRow, column);
        }
	}
	//===============================================================
	//===============================================================
    private void manageCellClicked(int row, int column) {
        if (column!=NAME) {
            //  Toggle this context
            HdbAttribute attribute = hdbAttributeList.get(row);
            switch (column) {
                case ALWAYS_COLUMN:
                    //  Set ALWAYS
                    attribute.get(ALWAYS_INDEX).setUsed(true);
                    // if always set -> reset others
                    for (int i=ALWAYS_INDEX+1 ; i<attribute.size() ; i++)
                        attribute.get(i).setUsed(false);
                    break;
                default:
                    //  Reset ALWAYS
                    attribute.get(ALWAYS_INDEX).setUsed(false);
                    //  Toggle specified one
                    attribute.get(column-1).toggleUsed();
                    //  if used --> reset always
                    boolean used = attribute.get(column-1).isUsed();
                    if (used)
                        attribute.get(ALWAYS_INDEX).setUsed(false);
                    else {
                        //  Check if any context is used -> set Always
                        used = false;
                        for (int i=ALWAYS_INDEX+1 ; i<attribute.size() && !used ; i++)
                            used = attribute.get(i).isUsed();
                        if (!used)
                            attribute.get(ALWAYS_INDEX).setUsed(true);
                    }
                    break;
            }
        }
        repaint();
    }
	//===============================================================
	//===============================================================
	private int getNameColumnWidth() {
		List<String> names = new ArrayList<>();
		for (HdbAttribute attribute : hdbAttributeList)
			names.add(attribute.getName());
		return getTableColumnWidth(names);
	}
	//===============================================================
	//===============================================================
    public static int getColumnWidth(String str) {
        List<String> list = new ArrayList<>();
        list.add(str);
        return getTableColumnWidth(list);
    }
	//===============================================================
	//===============================================================








	//=========================================================================
	/**
	 * The Table model
	 */
	//=========================================================================
	public class StrategyTableModel extends DefaultTableModel {
		//==========================================================
		public int getColumnCount() {
			return columnNames.size();
		}
		//==========================================================
		public int getRowCount() {
			return hdbAttributeList.size();
		}
		//==========================================================
		public String getColumnName(int columnIndex) {
			if (columnIndex>=getColumnCount())
				return columnNames.get(getColumnCount() - 1);
			else
				return columnNames.get(columnIndex);
		}
		//==========================================================
		public Object getValueAt(int row, int column) {
			if (column==NAME)
				return hdbAttributeList.get(row).getName(); // Row title
			return hdbAttributeList.get(row).get(column-1).isUsed();
		}
		//==========================================================
		/**
		 * @param column the specified co;umn number
		 * @return the cell class at first row for specified column.
		 */
		//==========================================================
		public Class getColumnClass(int column) {
			if (isVisible()) {
				if (column==NAME)
					return String.class;
				else
					return Boolean.class;
			} else
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
			setFont(new Font("Dialog", Font.BOLD, 12));
			setOpaque(true);
		}
		//==========================================================
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			setText(hdbAttributeList.get(row).getName());
			return this;
		}
		//==========================================================
	}
	//=========================================================================
	//=========================================================================
}
