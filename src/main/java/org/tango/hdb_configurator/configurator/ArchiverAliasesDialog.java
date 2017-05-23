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

package org.tango.hdb_configurator.configurator;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.SubscriberMap;
import org.tango.hdb_configurator.common.TangoUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import static org.tango.hdb_configurator.common.Utils.getTableColumnWidth;


//===============================================================
/**
 *	JDialog Class to manage subscriber aliases
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class ArchiverAliasesDialog extends JDialog {

	private JFrame	parent;
	private ArrayList<Archiver> archivers = new ArrayList<>();
	private JTable	table;
	private TablePopupMenu	popupMenu = new TablePopupMenu();
	private DataTableModel dataTableModel;
    private int tableWidth;

	private int returnValue = JOptionPane.OK_OPTION;
	private static final String[] columnNames = { "Subscriber", "Aliases" };
    private static final int maxHeight = 800;
    private static final int DEVICE_NAME = 0;
    private static final int ALIAS_NAME  = 1;
	//===============================================================
	/*
	 *	A class to temporarily define archiver alias
	 */
	//===============================================================
	private class Archiver {
		String deviceName, alias;
		private Archiver(String deviceName, String alias) {
			this.deviceName  = TangoUtils.getOnlyDeviceName(deviceName);
			this.alias = alias;
		}
	}
	//===============================================================
	/**
	 *	Creates new form ArchiverAliasesDialog
	 */
	//===============================================================
	public ArchiverAliasesDialog(JFrame parent, SubscriberMap subscriberMap) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		initComponents();

		//	Get subscribers in a list
		List<String> labels = subscriberMap.getLabelList();
		for (String label : labels) {
			archivers.add(new Archiver(subscriberMap.getSubscriberByLabel(label).name, label));
		}
		buildTable();

		titleLabel.setText(archivers.size() + " Subscriber Aliases");
		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	private void buildTable() {

		dataTableModel = new DataTableModel();

		// Create the table
		table = new JTable(dataTableModel);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);
		table.setDragEnabled(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				tableActionPerformed(evt);
			}
		});

		//  Set column width
        int[] columnWidth = getColumnWidths();
        final Enumeration columnEnum = table.getColumnModel().getColumns();
        int i = 0;
        tableWidth = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            tableWidth += columnWidth[i];
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(columnWidth[i++]);
        }
        //	Put it in scrolled pane
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        setTableResize();
	}
    //===============================================================
    //===============================================================
    private int[] getColumnWidths() {
        int[] widths = new int[columnNames.length];
        //  build a list of string for each column
        List<String> devices = new ArrayList<>();
        List<String> aliases = new ArrayList<>();
        for (Archiver subscriber : archivers) {
            devices.add(subscriber.deviceName);
            aliases.add(subscriber.alias);
        }
        // Compute for each column
        widths[DEVICE_NAME] = getTableColumnWidth(devices);
        widths[ALIAS_NAME]  = getTableColumnWidth(aliases);
        return widths;
    }
    //===============================================================
    //===============================================================
    private void setTableResize() {
        pack();
        JScrollPane scrollPane = (JScrollPane) table.getParent().getParent();
        int height = table.getHeight()+table.getTableHeader().getHeight()+10;
        if (height>maxHeight)  {
            height = maxHeight;
        }
        scrollPane.setPreferredSize(new Dimension(tableWidth+20, height));
    }
	//===============================================================
	//===============================================================
	private Archiver selectedArchiver;
	private void tableActionPerformed(java.awt.event.MouseEvent event) {

		//	get selected signal
		Point clickedPoint = new Point(event.getX(), event.getY());
		int row = table.rowAtPoint(clickedPoint);
		selectedArchiver = archivers.get(row);
		table.repaint();

		if (event.getClickCount() == 2) {
			changeAlias();
		}
		else {
			int mask = event.getModifiers();

			//  Check button clicked
			if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
				popupMenu.showMenu(event, selectedArchiver);
			}
		}
	}
	//===============================================================
	//===============================================================
	private void changeAlias() {
		boolean done = false;
		while (!done) {
			String newAlias = JOptionPane.showInputDialog(this,
					"New alias for " + selectedArchiver.deviceName, selectedArchiver.alias);
			if (newAlias!=null) {
				try {
					checkNewAliasName(newAlias);
					selectedArchiver.alias = newAlias;
					dataTableModel.fireTableDataChanged();
					done = true;
				} catch (Exception e) {
					ErrorPane.showErrorMessage(this, null, e);
				}
			}
			else
				done = true;
		}
	}
	//===============================================================
	//===============================================================
	private void checkNewAliasName(String aliasName) throws Exception {
		for (Archiver archiver : archivers) {
			if (archiver!=selectedArchiver) {
				if (aliasName.equalsIgnoreCase(archiver.alias)) {
					throw new Exception("\"" + aliasName +
							"\"  is already an alias for  "+archiver.deviceName);
				}
			}
		}
	}
	//===============================================================
	//===============================================================
	private void updateDatabase() throws DevFailed {
		//	Get alias list
		List<String[]> labels = TangoUtils.getSubscriberLabels();
		if (labels.isEmpty()) {
			//	Create list
			for (Archiver archiver : archivers) {
				labels.add(new String[] { archiver.deviceName, archiver.alias});
			}
		}
		else {
			// 	and modify to do not overwrite labels for others (test, second HDB system, ...)
			for (String[] label : labels) {
				String deviceName = label[0];
				String alias = label[1];
				for (Archiver archiver : archivers) {
					if (deviceName.equalsIgnoreCase(archiver.deviceName) &&
							!alias.equals(archiver.alias)) {
						label[1] = archiver.alias;
						System.out.println(deviceName + " --> " + archiver.alias);
					}
				}
			}
		}
		// And update database
		TangoUtils.setSubscriberLabels(labels);
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
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(okBtn);

        cancelBtn.setText("Cancel");
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
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
		try {
			updateDatabase();
			returnValue = JOptionPane.OK_OPTION;
			doClose();
		}
		catch (DevFailed e) {
			ErrorPane.showErrorMessage(this, e.getMessage(), e);
		}
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		returnValue = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		returnValue = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_closeDialog

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
	public int showDialog() {
		setVisible(true);
		return returnValue;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================






	//=========================================================================
	/**
	 * The Table dataTableModel
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
			return title;
		}

		//==========================================================
		public Object getValueAt(int row, int column) {
			if (column==0)
				return archivers.get(row).deviceName;
			else
				return archivers.get(row).alias;
		}
		//==========================================================
	}
	//======================================================
	//======================================================




	//======================================================
	/**
	 * Popup menu class
	 */
	//======================================================
	private static final int CHANGE_ALIAS = 0;
	private static final int OFFSET = 2;    //	Label And separator

	private static String[] menuLabels = {
			"Change alias",
	};
	//=======================================================
	//=======================================================
	private class TablePopupMenu extends JPopupMenu {
		private JLabel title;
		//======================================================
		private TablePopupMenu() {
			title = new JLabel();
			title.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
			add(title);
			add(new JPopupMenu.Separator());

			for (String menuLabel : menuLabels) {
				if (menuLabel == null)
					add(new Separator());
				else {
					JMenuItem btn = new JMenuItem(menuLabel);
					btn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							menuActionPerformed(evt);
						}
					});
					add(btn);
				}
			}
		}
		//======================================================
		//======================================================
		private void showMenu(MouseEvent event, Archiver archiver) {
			title.setText(archiver.deviceName);
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
			switch (itemIndex) {
				case CHANGE_ALIAS:
					changeAlias();
					break;
			}
		}
	}
}
