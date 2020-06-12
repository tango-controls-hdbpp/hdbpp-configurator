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
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class CreateSubscriberPanel extends JDialog {

	private JFrame	parent;
    private DeviceProxy configuratorProxy;
    private List<String>   instances = new ArrayList<>();
    private List<String>   archivers = new ArrayList<>();
    private List<String>   labels;
    private SubscriberMap  subscriberMap;   //  Used only to remove
    private int action;

    private int option = JOptionPane.OK_OPTION;
    private String managerName;
    private String subscriberName;
    private static final String CLASS_NAME = "HdbEventSubscriber";
    public static final int CREATE = 0;
    public static final int REMOVE = 1;

	//===============================================================
	/**
	 *	Creates new form CreateSubscriberPanel
	 */
	//===============================================================
	public CreateSubscriberPanel(JFrame parent, DeviceProxy configuratorProxy, int action) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        this.configuratorProxy = configuratorProxy;
        this.action = action;
		initComponents();
        classPanel.setVisible(false);

        //  Get manager device and server name and build subscriber map
        managerName = configuratorProxy.get_server_name();
        managerName = managerName.substring(0, managerName.indexOf('/'));
        subscriberName= TangoUtils.getServerNameForClass(CLASS_NAME);
        subscriberMap = new SubscriberMap(configuratorProxy);

        //  Customize dialog (Create or Remove)
        if (action==CREATE) {
            titleLabel.setText("Create a Subscriber on " + configuratorProxy.name());
            String[] archiverDevices = ArchiverUtils.getSubscriberList(configuratorProxy);
            deviceComboBox.addItem("");
            instanceComboBox.addItem("");
            labelComboBox.addItem("");
            for (String archiver : archiverDevices) {
                archivers.add(archiver);
                deviceComboBox.addItem(TangoUtils.getOnlyDeviceName(archiver));
                String instance = getServerInstance(archiver);
                if (!instances.contains(instance)) {
                    instances.add(instance);
                    instanceComboBox.addItem(instance);
                }
            }

            //  Initialize exe file name if first
            if (archiverDevices.length==0) {
                // Get class properties if first subscriber
                classPanel.setVisible(true);
                classPropertyTextArea.setToolTipText(
                        Utils.buildTooltip("Fill LibConfiguration class property\n" +
                                "with information about your history database.\n" +
                                "See HDB++ documentation."));

                //  Check if a previous property is available (if several manager).
                DbDatum dbDatum = new DbClass(CLASS_NAME).get_property("LibConfiguration");
                if (!dbDatum.is_empty())
                    classPropertyTextArea.setText(arrayToString(dbDatum.extractStringArray()));
            }
        }
        else {
            //  action is REMOVE
            titleLabel.setText("Remove a Subscriber from " + configuratorProxy.name());
            instanceComboBox.setVisible(false);
            instanceLabel.setVisible(false);
            deviceComboBox.setVisible(false);
            deviceLabel.setVisible(false);
            labelComboBox.setEditable(false);
            inManagerButton.setVisible(false);
        }

        //  Put in a list to sort before
        labels = subscriberMap.getLabelList();
        for (String label : labels) {
            labelComboBox.addItem(label);
        }
        pack();
 		ATKGraphicsUtils.centerDialog(this);
	}

	//===============================================================
	//===============================================================
    private String getServerInstance(String deviceName) throws DevFailed {
        String serverName = new DeviceProxy(deviceName).get_server_name();
        int idx = serverName.indexOf('/');
        if (idx>0) {
            subscriberName= serverName.substring(0, idx);
            return serverName.substring(idx+1);
        }
        else {
            subscriberName= deviceName;
            return serverName;
        }
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
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        instanceLabel = new javax.swing.JLabel();
        deviceLabel = new javax.swing.JLabel();
        javax.swing.JLabel aliasLabel = new javax.swing.JLabel();
        instanceComboBox = new javax.swing.JComboBox<>();
        deviceComboBox = new javax.swing.JComboBox<>();
        labelComboBox = new javax.swing.JComboBox<>();
        inManagerButton = new javax.swing.JRadioButton();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        classPanel = new javax.swing.JPanel();
        javax.swing.JLabel classLabel = new javax.swing.JLabel();
        javax.swing.JLabel classLabel1 = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        classPropertyTextArea = new javax.swing.JTextArea();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Create a new Subscriber");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.GridBagLayout());

        instanceLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instanceLabel.setText("Server instance:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        centerPanel.add(instanceLabel, gridBagConstraints);

        deviceLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        deviceLabel.setText("Device name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 8, 12);
        centerPanel.add(deviceLabel, gridBagConstraints);

        aliasLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        aliasLabel.setText("Label:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        centerPanel.add(aliasLabel, gridBagConstraints);

        instanceComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        centerPanel.add(instanceComboBox, gridBagConstraints);

        deviceComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 8, 12);
        centerPanel.add(deviceComboBox, gridBagConstraints);

        labelComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        centerPanel.add(labelComboBox, gridBagConstraints);

        inManagerButton.setText("Inside manager server");
        inManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inManagerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        centerPanel.add(inManagerButton, gridBagConstraints);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

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

        classPanel.setLayout(new java.awt.GridBagLayout());

        classLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        classLabel.setText("Class Properties:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 12);
        classPanel.add(classLabel, gridBagConstraints);

        classLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        classLabel1.setText("LibConfiguration: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 5);
        classPanel.add(classLabel1, gridBagConstraints);

        classPropertyTextArea.setColumns(25);
        classPropertyTextArea.setRows(8);
        jScrollPane1.setViewportView(classPropertyTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        classPanel.add(jScrollPane1, gridBagConstraints);

        getContentPane().add(classPanel, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        try {
            if (action==CREATE) {
                if (addSubscriber())
                    doClose();
            }
            else {
                if (removeSubscriber())
                    doClose();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
    private boolean removeSubscriber() throws DevFailed {
        //  ToDo
        String label = ((String) labelComboBox.getSelectedItem()).trim();
        //  Check if field filled.
        if (label.isEmpty())
            Except.throw_exception("SyntaxError", "Label is not defined");

        //  Get confirmation
        Subscriber subscriber  = subscriberMap.getSubscriberByLabel(label);
        checkManagedAttributes(subscriber);

        String message = "Remove subscriber  " + label + "  (" + subscriber.name + ") ?";
        if (JOptionPane.showConfirmDialog(this,
                message, "Confirmation", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            DeviceData argIn = new DeviceData();
            argIn.insert(subscriber.name);
            configuratorProxy.command_inout("ArchiverRemove", argIn);
            manageArchiveLabels(TangoUtils.getOnlyDeviceName(subscriber.name), label);
            message = "Subscriber  " + label + "  (" + subscriber.name + " has been removed.\n\n" +
                    "WARNING: device " + subscriber.name + "  still exists in TANGO database";
            JOptionPane.showMessageDialog(this, message);
            return true;
        }
        else
            return false;

    }
	//===============================================================
	//===============================================================
    private void checkManagedAttributes(Subscriber subscriber) throws DevFailed {
        DbDatum datum = subscriber.get_property("AttributeList");
        if (!datum.is_empty())
            Except.throw_exception("NotEmpty",
                    subscriber.getLabel() + " still manage attribute(s).\nRemove attribute(s) before");
    }
	//===============================================================
	//===============================================================
    private boolean checkExeFile() {
        if (inManagerButton.isSelected())
            managerName = JOptionPane.showInputDialog(this, "Manager executable name ?", managerName);
        else {
            List<String> exeFiles = subscriberMap.getSubscriberExeFiles();
            Selector selector = new Selector(this,
                    "Subscriber executable name ?", "",
                    exeFiles, "");//exeFiles.get(0));
            subscriberName = selector.showDialog();
        }
        return subscriberName !=null;
    }
	//===============================================================
	//===============================================================
    private String[] stringToArray(String str) {
        StringTokenizer stk = new StringTokenizer(str, "\n");
        String[] array = new String[stk.countTokens()];
        for (int i=0 ; stk.hasMoreTokens() ; i++)
            array[i] = stk.nextToken();
        return array;
    }
	//===============================================================
	//===============================================================
    private String arrayToString(String[] lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
            sb.append(line).append('\n');
        return sb.toString().trim();
    }
	//===============================================================
	//===============================================================
    private void putClassProperties() throws DevFailed {
        String[] libConfiguration = stringToArray(classPropertyTextArea.getText().trim());
        DbDatum  dbDatum = new DbDatum("LibConfiguration", libConfiguration);

        DbClass dbClass = new DbClass(Subscriber.CLASS_NAME);
        dbClass.put_property(new DbDatum[] { dbDatum });
    }
	//===============================================================
	//===============================================================
    private boolean addSubscriber() throws DevFailed {
        //  ToDo
        if (classPanel.isVisible()) {
            putClassProperties();
        }

        //  Check inputs
        String deviceName = ((String) deviceComboBox.getSelectedItem()).trim();
        String label      = ((String) labelComboBox.getSelectedItem()).trim();
        //  Check if all fields filled.
        if (deviceName.isEmpty())
            Except.throw_exception("SyntaxError", "Device is not defined");
        if (label.isEmpty())
          Except.throw_exception("SyntaxError", "Label is not defined");

        //  Check for server file name
        if (!checkExeFile()) {
            return false;
        }

        //  Check device name (with tango host !!!)
        for (String archiver : archivers) {
            if (TangoUtils.getOnlyDeviceName(archiver).equals(deviceName))
                Except.throw_exception("AlreadyExists", "Device " + deviceName + " already defined as subscriber");
        }

        //  Check label
        if (labels.contains(label))
            Except.throw_exception("AlreadyExists", "Label " + label+ " already defined");

        //  Manage subscriber device in Manager server
        if (inManagerButton.isSelected()) {
            return createSubscriberDeviceToManager(deviceName, label);
        }
        else {
            return createSubscriberDeviceAndServer(deviceName, label);
        }
    }
	//===============================================================
	//===============================================================
    private boolean createSubscriberDeviceToManager(String deviceName, String label) throws DevFailed {
        //  Build message to be displayed
        String message = "Device  " + deviceName + " will be added to  ";
        message += configuratorProxy.name() + "\n";
        message += "This device will be used with " + label + " label.";

        String configuratorName = configuratorProxy.get_server_name();
        //  Get confirmation.
        if (JOptionPane.showConfirmDialog(this,
                message, "Confirmation", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            createArchiverDevice(configuratorName, deviceName, true);
            manageArchiveLabels(deviceName, label);
            addToManager(TangoUtils.fullName(deviceName));

            //  Re start server
            new DeviceProxy("dserver/"+configuratorName).command_inout("RestartServer");

            message = configuratorName + "  has been updated.\n     And restarted.";
            JOptionPane.showMessageDialog(this, message);
            return true;
        }
        else
            return false;
    }
	//===============================================================
	//===============================================================
    private boolean createSubscriberDeviceAndServer(String deviceName, String label) throws DevFailed {
        String instance   = ((String) instanceComboBox.getSelectedItem()).trim();
        if (instance.isEmpty())
            Except.throw_exception("SyntaxError", "Instance is not defined");

        //  Check instance
        boolean serverExists = instances.contains(instance);
        System.out.println(instance);

        //  Build message to be displayed
        String message = "Device " + deviceName + " will be created ";
        if (serverExists)
            message += "in existing ";
        else
            message += "in new ";
        message += subscriberName + '/' + instance + " server as " + CLASS_NAME + " class\n";
        message += "This device will be used with " + label + " label.";

        //  Get confirmation.
        if (JOptionPane.showConfirmDialog(this,
                message, "Confirmation", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {

            createArchiverDevice(subscriberName+'/'+instance, deviceName, serverExists);
            manageArchiveLabels(deviceName, label);
            addToManager(TangoUtils.fullName(deviceName));
            if (serverExists)
                message = subscriberName +'/'+instance + "  has been updated.\nYou can re-start it.";
            else
                message = subscriberName +'/'+instance + "  has been created.\nYou can start it.";
            JOptionPane.showMessageDialog(this, message);
            return true;
        }
        else
            return false;
    }
	//===============================================================
	//===============================================================
    private void addToManager(String deviceName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(deviceName);
        configuratorProxy.command_inout("ArchiverAdd", argIn);
    }
	//===============================================================
	//===============================================================
    private void manageArchiveLabels(String deviceName, String archiverLabel) throws DevFailed {
        List<String[]> labelList = TangoUtils.getSubscriberLabels();
        //  if REMOVE remove it
        //  if CREATE remove if already exists
        for (String[] line : labelList) {
            if (line[0].equalsIgnoreCase(deviceName)) {
                labelList.remove(line);
                break;
            }
        }
        // Then if CREATE add iot and sort
        if (action==CREATE) {
            labelList.add(new String[]{deviceName, archiverLabel});
            Collections.sort(labelList, new StringArrayComparator());
        }

        // And finally update database
        TangoUtils.setSubscriberLabels(labelList);
    }
	//===============================================================
	//===============================================================
    private void createArchiverDevice(String serverName, String deviceName, boolean severExists) throws DevFailed {
        //  Check if device already exists and is alive
        try{
            new DeviceProxy(deviceName).ping();
            //  if alive, do not create
            return;
        } catch (DevFailed e) {
            //
        }

        if (!severExists) {
            String adminName = "dserver/"+ serverName.toLowerCase();
            ApiUtil.get_db_obj().add_device(new DbDevInfo(adminName,  "DServer", serverName));
        }
        ApiUtil.get_db_obj().add_device(new DbDevInfo(deviceName, CLASS_NAME, serverName));

        //  Copy properties from another archiver device
        DeviceProxy archiverProxy = new DeviceProxy(deviceName);
        if (archivers.size()>0) {
            DeviceProxy anotherDevice = new DeviceProxy(archivers.get(0));
            String[]    propertyNames = anotherDevice.get_property_list("*");
            for (String propertyName : propertyNames) {
                if (!propertyName.equalsIgnoreCase("AttributeList") &&
                    !propertyName.startsWith("__")) {
                    archiverProxy.put_property(anotherDevice.get_property(propertyName));
                }
            }
        }
        System.out.println(serverName + " Created");
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        option = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        option = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void inManagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inManagerButtonActionPerformed
         // Set instance visible only if not in manager server
        JRadioButton button = (JRadioButton) evt.getSource();
        instanceLabel.setVisible(!button.isSelected());
        instanceComboBox.setVisible(!button.isSelected());
    }//GEN-LAST:event_inManagerButtonActionPerformed
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
    public int showDialog() {
        setVisible(true);
        return option;
    }
	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel classPanel;
    private javax.swing.JTextArea classPropertyTextArea;
    private javax.swing.JComboBox<String> deviceComboBox;
    private javax.swing.JLabel deviceLabel;
    private javax.swing.JRadioButton inManagerButton;
    private javax.swing.JComboBox<String> instanceComboBox;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JComboBox<String> labelComboBox;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		try {
            String deviceName = System.getenv("HdbManager");
            if (deviceName==null)
                Except.throw_exception("DeviceNotDefined",
                        "HDB manager device not defined");
            new CreateSubscriberPanel(null,
                    new DeviceProxy(deviceName), CreateSubscriberPanel.REMOVE).showDialog();
		}
		catch(DevFailed e) {
		    e.printStackTrace();
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
	}
    //=======================================================
    //=======================================================




   //======================================================
    /**
     * Comparator class to sort by Strings
     */
    //======================================================
    private class StringArrayComparator implements Comparator<String[]> {
        public int compare(String[] a1, String[] a2) {

            if (a1 == null)      return 1;
            else if (a2 == null) return -1;
            else {
                return a1[0].compareTo(a2[0]);
            }
        }
    }
    //=======================================================
    //=======================================================
}
