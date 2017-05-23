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
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.Strategy;
import org.tango.hdb_configurator.common.Subscriber;
import org.tango.hdb_configurator.common.SubscriberMap;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import java.awt.*;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class EditContextsDialog extends JDialog {
	private EditContextsPanel editPanel;
	private int	returnValue = JOptionPane.OK_OPTION;
	//===============================================================
    /**
     * Creates new form EditStrategyDialog from class property
     * @param parent prent instance
     */
	//===============================================================
	public EditContextsDialog(JFrame parent) throws DevFailed {
	    this(parent, null);
    }
	//===============================================================
    /**
     * Creates new form EditStrategyDialog from subscribe property
     *  (or class property if null)
     * @param parent prent instance
     * @param subscriber  Subscriber to get list of strategy to be managed
     */
	//===============================================================
	public EditContextsDialog(JFrame parent, Subscriber subscriber) throws DevFailed {
		super(parent, true);
		initComponents();
        Strategy strategy = subscriber.getStrategy();

        editPanel = new EditContextsPanel(this, strategy, "HDB Strategy for " + subscriber.getLabel());
        getContentPane().add(editPanel, BorderLayout.CENTER);
        pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	@SuppressWarnings("unused")
    public Strategy getStrategy() {
	    return editPanel.getStrategy();
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

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

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
        /*
        if (editPanel != null) {
            try {
                //  OK after edit -> put property in database
                Strategy.putStrategiesToDB(editPanel.getStrategyCopy(), subscriber);
                returnValue = JOptionPane.OK_OPTION;
                doClose();
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, e.getMessage(), e);
            }
         */
        returnValue = JOptionPane.OK_OPTION;
        doClose();
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
		setVisible(false);
		dispose();
	}
	//===============================================================
	//===============================================================
	public int showDialog() {
		setVisible(true);
		return returnValue;
	}
	//===============================================================
	//===============================================================


	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	//===============================================================



	//===============================================================
	/**
	 * @param args the command line arguments
	 */
	//===============================================================
	public static void main(String args[]) {
	    try {
            //  Build subscriber map
            SubscriberMap subscriberMap = new SubscriberMap(Utils.getConfiguratorProxy());
            Subscriber subscriber = subscriberMap.getSubscriberByLabel("Insertion Devices 2");
            if (subscriber==null) {
                Except.throw_exception("", "Subscriber not found !");
            }
            new EditContextsDialog(null, subscriber).setVisible(true);
		}
		catch(Exception e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
		}
		System.exit(0);
	}
	//=========================================================================
	//=========================================================================
}
