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
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.SplashUtils;
import org.tango.hdb_configurator.common.TangoUtils;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static org.tango.hdb_configurator.common.Utils.getTableColumnWidth;
import static org.tango.hdb_configurator.common.Utils.firstColumnBackground;
import static org.tango.hdb_configurator.common.Utils.selectionBackground;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class ServerInfoTable extends JDialog {
	private JFrame	parent;
	private JTable table;
	private List <ServerInfo> serverInfoList = new ArrayList<>();
	private static final String[] columnNames = { "Device", "Server", "Host", "Uptime"};
	private int[] columnWidth;
	private int selectedRow = -1;
	private ServerPopupMenu popupMenu = new ServerPopupMenu();
    private HashMap<String, String> subscriberLabels = null;

	private static final int DEVICE_LABEL  = 0;
	private static final int SERVER_NAME   = 1;
	private static final int SERVER_HOST   = 2;
	private static final int SERVER_UPTIME = 3;
	//===============================================================
	/**
	 *	Creates new form UpTimeTable
	 */
	//===============================================================
	public ServerInfoTable(JFrame parent) throws DevFailed {
		super(parent, true);
		try {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().setSplashProgress(10, "Building GUI");
            this.parent = parent;
            initComponents();

            SplashUtils.getInstance().setSplashProgress(10, "Building Archiver objects");
            List<String> subscriberDeviceNames = getSubscriberDeviceNames();
            for (String subscriberDeviceName : subscriberDeviceNames) {
                ServerInfo serverInfo =
                        new ServerInfo(getLabel(subscriberDeviceName), subscriberDeviceName);
                if (!isInList(serverInfo)) {
                    serverInfoList.add(serverInfo);
                    System.out.println(serverInfo.deviceName);
                }
            }
            serverInfoList.sort(new ServerComparator());

            computeColumnWidth();
            buildTable();
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
    private boolean isInList(ServerInfo server) {
	    for (ServerInfo serverInfo : serverInfoList)
    	    if (server.serverName.equals(serverInfo.serverName))
    	        return true;
        return false;
    }
	//===============================================================
	//===============================================================
    private List<String> getSubscriberDeviceNames() throws DevFailed {
        //  Get it form manager
        DeviceProxy configuratorProxy = Utils.getConfiguratorProxy();
        DeviceAttribute attribute = configuratorProxy.read_attribute("ArchiverList");
        String[] archivers = attribute.extractStringArray();
        List<String> list = new ArrayList<>();
        list.add(configuratorProxy.name());
        list.addAll(Arrays.asList(archivers));
        return list;
    }
	//===============================================================
	//===============================================================
    private String getLabel(String subscriberDeviceName) {
        if (subscriberLabels==null) {
            initializeSubscriberLabels();
        }
        String deviceName = TangoUtils.getOnlyDeviceName(subscriberDeviceName);
        String label = subscriberLabels.get(deviceName);
        if (label==null)
    	    return subscriberDeviceName.substring(deviceName.lastIndexOf('/')+1);
        else
            return label;
    }
	//===============================================================
	//===============================================================
    private void initializeSubscriberLabels() {
        subscriberLabels = new HashMap<>();
        String[] properties = new String[0];
        try {
            DbDatum datum = ApiUtil.get_db_obj().get_property(
                    "HdbConfigurator", "ArchiverLabels");
            if (!datum.is_empty())
                properties = datum.extractStringArray();
        } catch (DevFailed e) {
            System.err.println(e.toString());
        }
        for (String property : properties) {
            StringTokenizer stk = new StringTokenizer(property, ":");
            if (stk.countTokens()==2) {
                String deviceName = stk.nextToken().trim();
                String label = stk.nextToken().trim();
                subscriberLabels.put(deviceName, label);
            }
        }
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
            labels.add(serverInfo.label);
            servers.add(serverInfo.serverName);
            hosts.add(serverInfo.host);
            uptime.add(serverInfo.uptime);
        }

		//	get the label width
		columnWidth[DEVICE_LABEL]  = getTableColumnWidth(labels);
		columnWidth[SERVER_NAME]   = getTableColumnWidth(servers) + 10; // for Icon
		columnWidth[SERVER_HOST]   = getTableColumnWidth(hosts);
		columnWidth[SERVER_UPTIME] = getTableColumnWidth(uptime);
	}
	//===============================================================
	//===============================================================
	private void buildTable() {
		DataTableModel model = new DataTableModel();

		// Create the table
		table = new JTable(model);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);
		table.setDragEnabled(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
		table.setDefaultRenderer(String.class, new LabelCellRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableActionPerformed(event);
            }
        });

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
    //===============================================================
    private void tableActionPerformed(MouseEvent event) {
        selectedRow = table.rowAtPoint(new Point(event.getX(), event.getY()));
        if ((event.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            popupMenu.showMenu(event);
        }
        table.repaint();
    }
	//===============================================================
	//===============================================================
	public void setSelection(String selection) {
        int row=0;
        System.out.println("setSelection(" + selection + ")");
        for (ServerInfo serverInfo : serverInfoList) {
            if (serverInfo.label.equals(selection) ||
                serverInfo.serverName.equals(selection))
                selectedRow = row;
            row++;
        }
        repaint();
    }
    //=======================================================
    //=======================================================
    private void testSelectedDevice() {
        try {
            TangoUtils.testDevice(this, serverInfoList.get(selectedRow).deviceName);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //===============================================================
	//===============================================================



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

        titleLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 18));
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

		if (parent==null) {
            for (ServerInfo serverInfo : serverInfoList)
                serverInfo.runThread = false;
            System.exit(0);
        }
		else {
			setVisible(false);
			dispose();
		}
	}
	//===============================================================
	//===============================================================




    //===============================================================
    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String[] args) {
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.black));
        UIManager.put("ToolTip.background", new ColorUIResource(Utils.toolTipBackground));
        try {
            new ServerInfoTable(null).setVisible(true);
        }
        catch(DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
            System.exit(0);
        }
    }
    //==============================================================
    //==============================================================



    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	//===============================================================





	//===============================================================
	//===============================================================
	private class ServerInfo {
		private String label;
		private String deviceName;
		private DeviceProxy adminProxy;
		private String serverName = " - - - ";
		private String uptime = " - - - ";
		private String host   = " - - - ";
		private boolean configurator = false;
		private boolean alive =  false;
		private boolean runThread = true;
		//===========================================================
		private ServerInfo(String name, String deviceName) {
            this.deviceName = deviceName;
			try {
			    DeviceProxy deviceProxy = new DeviceProxy(deviceName);
                configurator = deviceProxy.get_class().contains("Config");
                if (configurator) {
                    if (name.equalsIgnoreCase("manager"))
                        label = "ES Manager";
                    else
                        label = name + " manager";
                }
                else
                    label = name;

				String serverInfo = deviceProxy.get_info().toString();
				serverName = getInfo(serverInfo, "Server:");
				uptime = getInfo(serverInfo, "last_exported:");
				host   = getInfo(serverInfo, "host:");
			}
			catch (DevFailed e) {
				System.err.println(e.toString());
			}
            new StateThread().start();
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
        private void startServer() throws DevFailed {
		    DeviceData argIn = new DeviceData();
		    argIn.insert(serverName);
		    new DeviceProxy("tango/admin/"+host).command_inout("DevStart", argIn);
        }
		//===========================================================
        private void stopServer() throws DevFailed {
		    adminProxy.command_inout("kill");
        }
		//===========================================================
        private ImageIcon getStateIcon() {
		    try {
                if (alive)
                    return Utils.getGreenBall();
                else
                    return Utils.getRedBall();
            } catch (DevFailed e) {
		        return null;
            }
        }
		//===========================================================
        private class StateThread extends Thread {
            @Override
            public void run() {
                while (runThread) {
                    boolean running;
                    try {
                        if (adminProxy==null)
                            adminProxy = new DeviceProxy("dserver/"+serverName);
                        adminProxy.ping();
                        running = true;
                    } catch (DevFailed e) {
                        running = false;
                    }
                    if (table==null) {
                        alive = running;
                    }
                    else
                    if (running!=alive) {
                        alive = running;
                        table.repaint();
                    }
                    try { sleep(3000); } catch (InterruptedException e) { /* */ }
                }
            }
        }
		//===========================================================
	}
	//===============================================================
	//===============================================================



    //======================================================
    /**
     * Popup menu class
     */
    //======================================================
    private static final int START_SERVER  = 0;
    private static final int STOP_SERVER   = 1;
    private static final int TEST_DEVICE   = 2;

    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Start Server",
            "Stop Server",
            "Test Subscriber device",
    };
    //=======================================================
    //=======================================================
    private class ServerPopupMenu extends JPopupMenu {
        private JLabel title;
        //======================================================
        private ServerPopupMenu() {
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            add(title);
            add(new JPopupMenu.Separator());
            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::menuActionPerformed);
                add(btn);
            }
        }
        //======================================================
        private void showMenu(MouseEvent event) {
            ServerInfo serverInfo = serverInfoList.get(selectedRow);
            title.setText("  " + serverInfo.label);
            getComponent(OFFSET + START_SERVER).setVisible(!serverInfo.alive);
            getComponent(OFFSET + STOP_SERVER).setVisible(serverInfo.alive);

            show(table, event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = -1;
            for (int i = 0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i) == obj)
                    itemIndex = i;
            ServerInfo serverInfo = serverInfoList.get(selectedRow);

            try {
                switch (itemIndex) {
                    case START_SERVER:
                        serverInfo.startServer();
                        break;
                    case STOP_SERVER:
                        if (JOptionPane.showConfirmDialog(this,
                                "Stop  " + serverInfo.label + " ?", "Confirm",
                                JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)
                            serverInfo.stopServer();
                        break;
                    case TEST_DEVICE:
                        testSelectedDevice();
                        break;
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
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
			return columnNames.length;
		}
		//==========================================================
        @Override
		public int getRowCount() {
			return serverInfoList.size();
		}
		//==========================================================
        @Override
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
        @Override
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
        @Override
		public Class getColumnClass(int column) {
			if (isVisible()) {
				return getValueAt(0, column).getClass();
			}
			else
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
			setOpaque(true); //MUST do this for background to show up.
		}
		//==========================================================
        @Override
		public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {
			setBackground(getBackground(row, column));
			ServerInfo serverInfo = serverInfoList.get(row);
			setIcon(null);
			setToolTipText(null);
			switch (column) {
				case DEVICE_LABEL:
				    setToolTipText(serverInfo.deviceName);
					setText(serverInfo.label);
					break;
				case SERVER_NAME:
				    setIcon(serverInfo.getStateIcon());
					setText(serverInfo.serverName);
					break;
				case SERVER_HOST:
					setText(serverInfo.host);
					break;
				case SERVER_UPTIME:
					setText(serverInfo.uptime);
					break;
			}
			if (serverInfo.configurator)
				setFont(new Font("Dialog", Font.BOLD, 14));
			else
				setFont(new Font("Dialog", Font.PLAIN, 12));
			return this;
		}
		//==========================================================
		private Color getBackground(int row, int column) {
            if (row==selectedRow)
                return selectionBackground;
			if (serverInfoList.get(row).configurator)
				return firstColumnBackground;
            if (column == 0)
                return firstColumnBackground;
            else
                return Color.white;
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
			return alphabeticalSort(serverInfo1.label, serverInfo2.label);
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
