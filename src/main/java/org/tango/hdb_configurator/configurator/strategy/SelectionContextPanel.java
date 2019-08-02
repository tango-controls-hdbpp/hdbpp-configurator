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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.tango.hdb_configurator.common.Strategy.ALWAYS_INDEX;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class SelectionContextPanel extends JPanel {

	private Strategy strategy;
	private int[] columnWidth = { 100, 50 };
	private static final String[] columnNames = { "Strategy", "Use It" };
	private static final int NAME = 0;
	private static final String howTo = "\nSelect strategies to be used for HDB storage";
	//===============================================================
	/**
	 *	Creates new form StrategyDialog
	 */
	//===============================================================
	public SelectionContextPanel(Strategy strategy) {
		this.strategy = strategy;
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		JLabel label = new JLabel("HDB Strategy Selection");
		label.setFont(new Font("Dialog", Font.BOLD, 14));
		panel.add(label);
		add(panel, BorderLayout.NORTH);
		buildTable();
	}
	//===============================================================
	//===============================================================
	private void buildTable() {
		StrategyTableModel model = new StrategyTableModel();
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

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		//	Check name column width
		int nameWidth = getNameColumnWidth();
		if (nameWidth>columnWidth[NAME])
			columnWidth[NAME] = nameWidth;

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
		scrollPane.setPreferredSize(new Dimension(width, 40+ strategy.size()*16));
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
					Context context = strategy.get(table.rowAtPoint(p));
					tip = Utils.buildTooltip(context.getHtmlDescription());
					break;
				default:
					tip = Utils.buildTooltip(howTo);
					break;
			}
		}
		return tip;
	}
	//===============================================================
	//===============================================================
	public Strategy getStrategy() {
		return strategy;
	}
	//===============================================================
	//===============================================================
	private void tableActionPerformed(MouseEvent event) {
		JTable table = (JTable) event.getSource();
		Point clickedPoint = new Point(event.getX(), event.getY());
		int selectedRow = table.rowAtPoint(clickedPoint);
		int column = table.columnAtPoint(clickedPoint);
		int mask = event.getModifiers();
		//  Check button clicked
		if ((mask & MouseEvent.BUTTON1_MASK)!=0) {
			if (column>0) {
				Context context = strategy.get(selectedRow);
				context.toggleUsed();
				switch (selectedRow) {
					case ALWAYS_INDEX:
                        // if always set -> reset others
                        if (context.isUsed()) {
                            for (int i=ALWAYS_INDEX+1 ; i<strategy.size() ; i++)
                                strategy.get(i).setUsed(false);
                        }
                        break;
                    default:
                        //  if used --> reset always
                        if (context.isUsed())
                            strategy.get(ALWAYS_INDEX).setUsed(false);
                        else {
                            //  Check if any context is used ->set Always
                            boolean used = false;
                            for (int i=ALWAYS_INDEX+1 ; i<strategy.size() && !used ; i++)
                                used = strategy.get(i).isUsed();
                            if (!used)
                                strategy.get(ALWAYS_INDEX).setUsed(true);
                        }
                        break;
                }
				repaint();
			}
		}
	}
	//===============================================================
	//===============================================================
	private int getNameColumnWidth() {
		List<String> names = new ArrayList<>();
		for (Context context : strategy)
			names.add(context.getName());
		return Utils.getTableColumnWidth(names);
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
			return columnNames.length;
		}

		//==========================================================
		public int getRowCount() {
			return strategy.size();
		}

		//==========================================================
		public String getColumnName(int columnIndex) {
			if (columnIndex>=getColumnCount())
				return columnNames[getColumnCount() - 1];
			else
				return columnNames[columnIndex];
		}
		//==========================================================
		public Object getValueAt(int row, int column) {
			if (column==0)
				return strategy.get(row).getName(); // Row title
			return strategy.get(row).isUsed();
		}
		//==========================================================
		/**
		 * @param column the specified column number
		 * @return the cell class at first row for specified column.
		 */
		//==========================================================
		public Class getColumnClass(int column) {
			if (isVisible()) {
				if (column==0)
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
			setText(strategy.get(row).getName());
			return this;
		}
		//==========================================================
	}
	//=========================================================================
	//=========================================================================
}
