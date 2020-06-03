//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,......,2018,2019
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
//-======================================================================

package org.tango.hdb_configurator.configurator.wildcard_selection;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.SplashUtils;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *	JDialog Class to select attribute from a wildcard
 *
 *	@author  Pascal Verdier
 */
public class WildcardSelectionDialog extends JDialog {
	private JFrame	parent;
	private WildcardSelectionTable selectionTable;
	private JScrollPane scrollPane = null;
	private boolean available;
	private int returnValue = JOptionPane.CANCEL_OPTION;
	private static final int MAX_DEVICES = 200;
	//===============================================================
	/**
	 *	Creates new form WildcardSelectionDialog
	 */
	//===============================================================
	public WildcardSelectionDialog(JFrame parent) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		initComponents();

		//Set title and add a button to get wildcard
		titleLabel.setText("Attribute Selection");
		titleLabel.getParent().add(new JLabel("     "));
		JButton button = new JButton(Utils.getInstance().getIcon("search.gif"));
		button.setToolTipText("Get new wildcard");
		button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		button.addActionListener(evt -> searchActionPerformed());
		titleLabel.getParent().add(button);

		available = buildSelectionTable();
	}
	//===============================================================
	//===============================================================
	private boolean buildSelectionTable() throws DevFailed {
		//	Get wildcard inputs
		String[] inputs = getWildcard();
		if (inputs==null) {
			return false;
		}
		String deviceWildcard = inputs[0];
		String attributeName  = inputs[1];

		try {
			SplashUtils.getInstance().startSplash();
			SplashUtils.getInstance().setSplashProgress(5, "Browsing Database....");
			SplashUtils.getInstance().startAutoUpdate();

			//	Get device list to build attribute list
			String eventTgHost = System.getenv("EVENT_TANGO_HOST");
			String[] deviceNames;
			if (eventTgHost==null)
				deviceNames = ApiUtil.get_db_obj().get_device_list(deviceWildcard);
			else
				deviceNames = ApiUtil.get_db_obj(eventTgHost).get_device_list(deviceWildcard);

			if (checkDeviceNumber(deviceNames.length)) {
				List<String> attributeList = new ArrayList<>();
				for (String deviceName : deviceNames) {
					attributeList.add(deviceName + '/' + attributeName);
				}

				//	Remove table if already created
				if (scrollPane!=null) {
					getContentPane().remove(scrollPane);
				}
				//	Add a table to select attributes
				selectionTable = new WildcardSelectionTable(attributeList);
				scrollPane = new JScrollPane(selectionTable);
				getContentPane().add(scrollPane, BorderLayout.CENTER);
				pack();

				//	Check table size
				int height = selectionTable.getHeight();
				if (height>800) height = 800;
				scrollPane.setPreferredSize(new Dimension(selectionTable.getTableWidth(), height + 30));

				pack();
				ATKGraphicsUtils.centerDialog(this);
				SplashUtils.getInstance().stopSplash();
			}
			else
				return false;
		}
		catch (DevFailed e) {
			SplashUtils.getInstance().stopSplash();
			e.printStackTrace();
			throw e;
		}
		return true;
	}
	//===============================================================
	//===============================================================
	private boolean checkDeviceNumber(int nbDevices) {
		if (nbDevices<=MAX_DEVICES)
			return true;
		SplashUtils.getInstance().stopSplash();
		return  (JOptionPane.showConfirmDialog(this,
				nbDevices+" devices found !\n Continue ?", "Confirm",
				JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION);
	}
	//===============================================================
	//===============================================================
	private String[] getWildcard() {
		GetWildcardDialog getWildcardDialog = new GetWildcardDialog(this);
		if (getWildcardDialog.showDialog()==JOptionPane.OK_OPTION) {
			return getWildcardDialog.getInputs();
		}
		else {
			return null;
		}
	}
	//===============================================================
	//===============================================================
	private void searchActionPerformed() {
		try {
			buildSelectionTable();
		}
		catch (DevFailed e) {
			ErrorPane.showErrorMessage(this, null, e);
		}
	}
	//===============================================================
	//===============================================================
	public boolean isAvailable() {
		if (!available)
			doClose();
		return available;
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

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
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
    	if (getSelection().isEmpty()) {
    		ErrorPane.showErrorMessage(this, null, new Exception("No attribute selected"));
		}
    	else {
			returnValue = JOptionPane.OK_OPTION;
			doClose();
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
	//===============================================================
	public List<String> getSelection() {
    	return selectionTable.getSelection();
	}
	//===============================================================
	//===============================================================



	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String[] args) {
		try {
			new WildcardSelectionDialog(null).setVisible(true);
		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
	}

}
