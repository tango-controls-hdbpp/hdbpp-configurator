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

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.Context;
import org.tango.hdb_configurator.common.Strategy;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static org.tango.hdb_configurator.common.Strategy.ALWAYS_INDEX;
import static org.tango.hdb_configurator.common.Utils.getTableColumnWidth;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class EditContextsPanel extends JPanel {
    private JDialog parent;
	private Strategy strategy;
    private int selectedRow = 0;
    private JButton removeButton;
    private JButton upButton;
    private JButton downButton;
    private StrategyTableModel model;
    private JTable table;

	private static final int[] columnWidth = { 180, 500, 70 };
	private static final String[] columnNames = { "Contexts", "Descriptions", "As Default", };
	private static final int CONTEXT = 0;
	private static final int DESCRIPTION = 1;
	private static final int DEFAULT_COL = 2;
	private static final String howTo = "\nDouble click to edit a context\n or use buttons to Add/Remove one";
    //===============================================================
	/**
	 *	Creates new form StrategyDialog
	 */
	//===============================================================
	public EditContextsPanel(JDialog parent, Strategy strategy, String title) {
	    this.parent = parent;
		this.strategy = strategy;
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
        if (title!=null && !title.isEmpty()) {
            JLabel label = new JLabel(title);
            label.setFont(new Font("Dialog", Font.BOLD, 14));
            panel.add(label);
        }
        addButtons(panel);
        add(panel, BorderLayout.NORTH);
		buildTable();
	}
	//===============================================================
	//===============================================================
    private void buttonActionPerformed(ActionEvent evt) {
        JButton btn = (JButton) evt.getSource();
        if (btn.getText().trim().equals("+")) {
            OneContextDialog contextDialog = new OneContextDialog(parent, strategy);
            if (contextDialog.showDialog()==JOptionPane.OK_OPTION)
                strategy.add(contextDialog.getContext());
        }
        else
        if (btn.getText().trim().equals("-")) {
            try {
                Context context = strategy.get(selectedRow);
                if (attributesUseContext(context))
                    return;
                if (JOptionPane.showConfirmDialog(this,
                        "remove context \'" + context.getName() + "\'  ?",
                        "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
                strategy.remove(selectedRow);
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
                return;
            }
        }
        else
        if (btn==upButton) {
            Context context = strategy.remove(selectedRow);
            strategy.add(--selectedRow, context);
        }
        else
        if (btn==downButton) {
            Context context = strategy.remove(selectedRow);
            strategy.add(++selectedRow, context);
        }
        model.fireTableDataChanged();
        table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        manageButtons();
    }
	//===============================================================
	//===============================================================
    private boolean attributesUseContext(Context context) throws DevFailed {
        //  Check if attributes use context
        StrategyMainPanel strategyMainPanel = new StrategyMainPanel(parent);
        String strategyStr = strategyMainPanel.hasAttributeUsingContext(context);
        if (strategyStr!=null) {
            ErrorPane.showErrorMessage(this, null,
                    new Exception("This context is used by attribute(s).\n"+
                    "Please, change attribute(s) strategy"));
            strategyMainPanel.setPane(strategyStr);
            strategyMainPanel.setVisible(true);
            return true;
        }
        return false;
    }
	//===============================================================
	//===============================================================
	private void buildTable() {
		model = new StrategyTableModel();
		//noinspection NullableProblems
        table = new JTable(model) {
			public String getToolTipText(MouseEvent event) {
				return manageTooltip(event);
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//table.setDefaultRenderer(String.class, new LabelCellRenderer());
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
		if (nameWidth>columnWidth[CONTEXT])
			columnWidth[CONTEXT] = nameWidth;
        table.getTableHeader().setReorderingAllowed(false);

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
				case CONTEXT:
					Context context = strategy.get(table.rowAtPoint(p));
					tip = Utils.buildTooltip(context.getHtmlDescription());
					break;
                case DEFAULT_COL:
                    tip = Utils.buildTooltip("Select for context as default");
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
		selectedRow = table.rowAtPoint(clickedPoint);
		int selectedColumn = table.columnAtPoint(clickedPoint);
		int mask = event.getModifiers();
        manageButtons();
        //  Check button clicked
		if (event.getClickCount()==1 && (mask & MouseEvent.BUTTON1_MASK)!=0) {
		    if (selectedColumn==DEFAULT_COL) {
                //  toggle as default
                strategy.get(selectedRow).setDefault(
                        !strategy.get(selectedRow).isDefault());
                boolean isDefault = strategy.get(selectedRow).isDefault();
                switch (selectedRow) {
                    case ALWAYS_INDEX:
                        // if always set -> reset others
                        if (isDefault) {
                            for (int i=ALWAYS_INDEX+1 ; i<strategy.size() ; i++)
                                strategy.get(i).setDefault(false);
                        }
                        break;
                    default:
                        //  if used --> reset always
                        if (isDefault)
                            strategy.get(ALWAYS_INDEX).setDefault(false);
                        else {
                            //  Check if any context is used ->set Always
                            isDefault = false;
                            for (int i=ALWAYS_INDEX+1 ; i<strategy.size() && !isDefault ; i++)
                                isDefault = strategy.get(i).isDefault();
                            if (!isDefault)
                                strategy.get(ALWAYS_INDEX).setDefault(true);
                        }
                        break;
                }
                model.fireTableDataChanged();
            }
        }
        else
		if (event.getClickCount()==2 && (mask & MouseEvent.BUTTON1_MASK)!=0) {
            //  Edit context
            new OneContextDialog(parent, strategy.get(selectedRow),
                     selectedRow!= ALWAYS_INDEX).setVisible(true);
            model.fireTableDataChanged();
		}
	}
	//===============================================================
	//===============================================================
	private int getNameColumnWidth() {
		List<String> names = new ArrayList<>();
		for (Context context : strategy)
			names.add(context.getName());
		return getTableColumnWidth(names);
	}
	//===============================================================
	//===============================================================
    private void manageButtons() {
        removeButton.setEnabled(selectedRow>0);
        upButton.setEnabled(selectedRow>1);
        downButton.setEnabled(selectedRow>0 && selectedRow < strategy.size()-1);
    }
	//===============================================================
	//===============================================================
    private void addButtons(JPanel panel) {

        //  Add buttons
        JButton addButton = new JButton(" + ");
        addButton.setToolTipText("Add new context");
        addButton.setForeground(new Color(0, 0xa0, 0));
        addButton.setFont(new Font("Dialog", 1, 18));
        addButton.setBorder(null);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        panel.add(new JLabel("       "));
        panel.add(addButton);

        removeButton = new JButton(" - ");
        removeButton.setToolTipText("Remove selection");
        removeButton.setForeground(Color.red);
        removeButton.setFont(new Font("Dialog", 1, 18));
        removeButton.setBorder(null);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        panel.add(removeButton);

        try {
            //  Add move buttons
            upButton = new JButton(Utils.getInstance().getIcon("up.gif"));
            upButton.setToolTipText("Add new context");
            upButton.setForeground(new Color(0, 0xa0, 0));
            upButton.setFont(new Font("Dialog", 1, 18));
            upButton.setBorder(null);
            upButton.setEnabled(false);
            upButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    buttonActionPerformed(evt);
                }
            });
            panel.add(new JLabel("    "));
            panel.add(upButton);

            downButton = new JButton(Utils.getInstance().getIcon("down.gif"));
            downButton.setToolTipText("Add new context");
            downButton.setForeground(new Color(0, 0xa0, 0));
            downButton.setFont(new Font("Dialog", 1, 18));
            downButton.setBorder(null);
            downButton.setEnabled(false);
            downButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    buttonActionPerformed(evt);
                }
            });
            panel.add(downButton);
        }
        catch (DevFailed e) {
            System.err.println(e.errors[0].desc);
        }
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
		    Context context = strategy.get(row);
			switch (column) {
                case CONTEXT:
                    return " " + context.getName(); // Row title
                case DESCRIPTION:
                    return " " + strategy.get(row).getDescription();
                default:
                    return context.isDefault();
            }
		}
		//==========================================================
		/**
		 * @param column the specified column number
		 * @return the cell class at first row for specified column.
		 */
		//==========================================================
		public Class getColumnClass(int column) {
			if (isVisible()) {
			    switch (column) {
                    case CONTEXT:
                    case DESCRIPTION:
                        return String.class;
                    default:
                        return Boolean.class;
                }
            } else
				return null;
		}
		//==========================================================
        @Override
        public boolean isCellEditable(int row, int column) {
            //return row!=DEFAULT_ROW;
            return false;
        }
		//==========================================================
	}
    //==========================================================
    //==========================================================
}
