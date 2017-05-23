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

package org.tango.hdb_configurator.diagnostics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.Subscriber;
import org.tango.hdb_configurator.common.SubscriberMap;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.tango.hdb_configurator.common.Utils.getTableColumnWidth;
import static org.tango.hdb_configurator.common.Utils.firstColumnBackground;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class ServerInfoTable extends JDialog {

	private JFrame	parent;
	private List <ServerInfo> serverInfoList = new ArrayList<>();
	private static final String[] columnNames = { "Device", "Server", "Host", "Uptime"};
	private int[] columnWidth;
	private static final int DEVICE_LABEL  = 0;
	private static final int SERVER_NAME   = 1;
	private static final int SERVER_HOST   = 2;
	private static final int SERVER_UPTIME = 3;
	//===============================================================
	/**
	 *	Creates new form UpTimeTable
	 */
	//===============================================================
	public ServerInfoTable(JFrame parent, SubscriberMap subscriberMap, boolean useManager) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		initComponents();
		List<Subscriber> subscribers = subscriberMap.getSubscriberList();
		for (Subscriber subscriber : subscribers) {
			serverInfoList.add(new ServerInfo(subscriber.getLabel(), subscriber, false));
		}
		if (useManager)
			serverInfoList.add(new ServerInfo("Configurator", Utils.getConfiguratorProxy(), true));
		Collections.sort(serverInfoList, new ServerComparator());

		computeColumnWidth();
		buildTable();
		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}

	//===============================================================
	//===============================================================
	private void computeColumnWidth() {
	    columnWidth = new int[columnNames.length];
	    List<String> labels = new ArrayList<>();
	    List<String> servers = new ArrayList<>();
	    List<String> hosts = new ArrayList<>();
	    List<String> uptime = new ArrayList<>();

        for (ServerInfo serverInfo : serverInfoList) {
            labels.add(serverInfo.name);
            servers.add(serverInfo.server);
            hosts.add(serverInfo.host);
            uptime.add(serverInfo.uptime);
        }

		//	get the label width
		columnWidth[DEVICE_LABEL]  = getTableColumnWidth(labels);
		columnWidth[SERVER_NAME]   = getTableColumnWidth(servers);
		columnWidth[SERVER_HOST]   = getTableColumnWidth(hosts);
		columnWidth[SERVER_UPTIME] = getTableColumnWidth(uptime);

		//	Remove label
		//getContentPane().remove(lbl);
	}
	//===============================================================
	//===============================================================
	private void buildTable() {
		DataTableModel model = new DataTableModel();

		// Create the table
		JTable table = new JTable(model);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);
		table.setDragEnabled(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
		table.setDefaultRenderer(String.class, new LabelCellRenderer());

		//	Put it in scrolled pane
		JScrollPane scrollPane = new JScrollPane(table);
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
		scrollPane.setPreferredSize(new Dimension(width, height+40));
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
        javax.swing.JLabel titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Information on HDB++ Servers");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

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


	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	//===============================================================





	//===============================================================
	//===============================================================
	private class ServerInfo {
		private String name;
		private String server = " - - - ";
		private String uptime = " - - - ";
		private String host   = " - - - ";
		private boolean configurator;
		//===========================================================
		private ServerInfo(String name, DeviceProxy proxy, boolean configurator) {
			this.name = name;
			this.configurator = configurator;
			try {
				String serverInfo = proxy.get_info().toString();
				server = getInfo(serverInfo, "Server:");
				uptime = getInfo(serverInfo, "last_exported:");
				host   = getInfo(serverInfo, "host:");
			}
			catch (DevFailed e) {
				System.err.println(e.toString());
			}
		}
		//===========================================================
		private String getInfo(String serverInfo, String target) {
			int start = serverInfo.indexOf(target);
			if (start > 0) {
				start += target.length();
				int end = serverInfo.indexOf("\n", start);
				if (end>0) {
					String str = serverInfo.substring(start, end).trim();
					if (target.equals("host:")) {
						//	Remove address
						int idx = str.indexOf("(");
						if (idx > 0) {
							str = str.substring(0, idx).trim();
						}
						//	remove fqdn
						idx = str.indexOf('.');
						if (idx>0) {
							str = str.substring(0, idx);
						}
					}
					return str;
				}
			}
			System.out.println(serverInfo);
			return target + ": not found";
		}
		//===========================================================
	}
	//===============================================================
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		try {
			//  Get subscriber labels if any
			SubscriberMap subscriberMap = new SubscriberMap(Utils.getConfiguratorProxy());

			new ServerInfoTable(null, subscriberMap, true).setVisible(true);
		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
	}
	//==============================================================
	//==============================================================







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
			return serverInfoList.size();
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
			setBackground(getBackground(row, column));
			switch (column) {
				case DEVICE_LABEL:
					setText(serverInfoList.get(row).name);
					break;
				case SERVER_NAME:
					setText(serverInfoList.get(row).server);
					break;
				case SERVER_HOST:
					setText(serverInfoList.get(row).host);
					break;
				case SERVER_UPTIME:
					setText(serverInfoList.get(row).uptime);
					break;
			}
			return this;
		}
		//==========================================================
		private Color getBackground(int row, int column) {
			if (serverInfoList.get(row).configurator)
				return firstColumnBackground;
			switch (column) {
				case 0:
					return firstColumnBackground;
				default:
					return Color.white;
			}
		}
		//==========================================================
	}
	//==============================================================
	//==============================================================





	//==========================================================
	/**
	 * Comparator to sort server list
	 */
	//==========================================================
	private class ServerComparator implements Comparator<ServerInfo> {
		//======================================================
		public int compare(ServerInfo serverInfo1, ServerInfo serverInfo2) {
			if (serverInfo1.configurator)
				return -1;
			if (serverInfo2.configurator)
				return 1;
			return alphabeticalSort(serverInfo1.name, serverInfo2.name);
		}
		//======================================================
		private int alphabeticalSort(String s1, String s2) {
			if (s1 == null) return 1;
			else if (s2 == null) return -1;
			else return s1.compareTo(s2);
		}
		//======================================================
	}
}
