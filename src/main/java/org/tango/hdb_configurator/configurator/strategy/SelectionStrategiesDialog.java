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
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import org.tango.hdb_configurator.common.Strategy;
import org.tango.hdb_configurator.common.HdbAttribute;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//===============================================================
/**
 *	JDialog Class select attribute strategies.
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class SelectionStrategiesDialog extends JDialog {
	private SelectionStrategiesPanel selectionPanel;
    private DeviceProxy configuratorProxy;
    private int returnedValue = JOptionPane.CANCEL_OPTION;

    private static final int maxHeight = 400;
	//===============================================================
    /**
     * Creates new form SelectionStrategyDialog
     * @param parent prent instance
     * @param strategy List of context to be proposed
     * @param attributeList lit of attributes to select strategies
     */
	//===============================================================
	public SelectionStrategiesDialog(JFrame parent,
                                     List<HdbAttribute> attributeList,
                                     Strategy strategy) throws DevFailed {
	    this(parent, attributeList, strategy, null);
    }
	//===============================================================
    /**
     * Creates new form SelectionStrategyDialog
     * @param parent prent instance
     * @param attributeList lit of attributes to select strategies
     * @param strategy List of context to be proposed
     * @param configuratorProxy proxy on manager
     */
	//===============================================================
	public SelectionStrategiesDialog(JFrame parent,
                                     List<HdbAttribute> attributeList,
                                     Strategy strategy,
                                     DeviceProxy configuratorProxy) throws DevFailed {
        super(parent, true);
        initForm(attributeList, strategy, configuratorProxy);
    }
	//===============================================================
    /**
     * Creates new form SelectionStrategyDialog
     * @param parent prent instance
     * @param attributeList lit of attributes to select strategies
     * @param strategy List of context to be proposed
     * @param configuratorProxy proxy on manager
     */
	//===============================================================
	public SelectionStrategiesDialog(JDialog parent,
                                     List<HdbAttribute> attributeList,
                                     Strategy strategy,
                                     DeviceProxy configuratorProxy) throws DevFailed {
        super(parent, true);
        initForm(attributeList, strategy, configuratorProxy);
    }
    //===============================================================
    //===============================================================
    private void initForm(List<HdbAttribute> attributeList,
                          Strategy strategy,
                          DeviceProxy configuratorProxy) throws DevFailed {
        this.configuratorProxy = configuratorProxy;
		initComponents();
        if (strategy==null) {
            strategy = Strategy.getContextsFromDB();
        }

        selectionPanel = new SelectionStrategiesPanel(strategy, attributeList);
        getContentPane().add(selectionPanel, BorderLayout.CENTER);
        pack();

        int height = selectionPanel.getHeight();
        if (height>maxHeight)  {
            selectionPanel.setPreferredSize(
                    new Dimension(selectionPanel.getWidth()+20, maxHeight));
        }

        pack();
 		ATKGraphicsUtils.centerDialog(this);
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

        JPanel bottomPanel = new JPanel();
        JButton okBtn = new JButton();
        JButton cancelBtn = new JButton();

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

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        returnedValue = JOptionPane.OK_OPTION;
        doClose();
	}//GEN-LAST:event_okBtnActionPerformed

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
		setVisible(false);
		dispose();
	}
	//===============================================================
	//===============================================================
    public int showDialog() {
        setVisible(true);
        return returnedValue;
    }
	//===============================================================
	//===============================================================
    public List<HdbAttribute> getHdbAttributeList() {
        return selectionPanel.getHdbAttributeList();
    }
	//===============================================================
	//===============================================================
    public void setAttributeStrategy() throws DevFailed {
        if (configuratorProxy==null)
            Except.throw_exception("NotInitialized", "configuratorProxy has not been initialized");

        List<HdbAttribute> hdbAttributes = selectionPanel.getHdbAttributeList();
        for (HdbAttribute attribute : hdbAttributes) {
            //  set strategies for attribute
            String[] inArray = new String[2];
            inArray[0] = attribute.getName();
            inArray[1] = attribute.strategyToString();
            DeviceData argIn = new DeviceData();
            argIn.insert(inArray);
            configuratorProxy.command_inout("SetAttributeStrategy", argIn);
        }
    }

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	//===============================================================


}
