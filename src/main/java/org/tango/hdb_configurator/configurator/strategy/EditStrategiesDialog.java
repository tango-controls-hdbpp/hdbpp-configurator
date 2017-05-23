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
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class EditStrategiesDialog extends JDialog {
    private JFrame parent;
    private List<Subscriber> subscriberList;
    private Strategy defaultStrategy;
	private EditContextsPanel editPanel;
    private JTable table;
    private DataTableModel dataTableModel;
    private int tableWidth;
    private TablePopupMenu popupMenu = new TablePopupMenu();

    private int returnValue = JOptionPane.OK_OPTION;

    private static final String[] columnNames = { "Subscriber", "Strategy", "Default" };
    private static final int[] columnWidth = { 230, 450, 70 };
    private static final int maxHeight = 800;
    private static final int SUBSCRIBER = 0;
    private static final int STRATEGY  = 1;
    private static final int DEFAULT  = 2;
    //private static final int CLASS_PROPERTY = 0;
    private static final int SUBSCRIBER_STRATEGIES = 1;
	//===============================================================
    /**
     * Creates new form EditStrategyDialog from subscribe property
     *  (or class property if null)
     * @param parent parent frame instance
     * @param subscriberList  List of subscribers to manage
     */
	//===============================================================
	public EditStrategiesDialog(JFrame parent, List<Subscriber> subscriberList) throws DevFailed {
		super(parent, true);
        this.parent = parent;
        this.subscriberList = subscriberList;
		initComponents();

        //  Add a panel for default strategy
        title1Label.setText( "HDB contexts as class property");
        defaultStrategy = Strategy.getContextsFromDB();
        editPanel = new EditContextsPanel(this, defaultStrategy, null);
        classPropertyPanel.add(editPanel, BorderLayout.CENTER);

        //  Add a table for all subscribers with their strategies
        title2Label.setText("Subscriber strategies");
        buildSubscriberTable();

        pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
    //===============================================================
    //===============================================================
    private void buildSubscriberTable() {

        dataTableModel = new DataTableModel();

        // Create the table
        table = new JTable(dataTableModel);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setDragEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });

        //  Set column width
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
        subscriberPropertyPanel.add(scrollPane, BorderLayout.CENTER);
        setTableResize();
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent event) {

        //	get selected signal
        Point clickedPoint = new Point(event.getX(), event.getY());
        int row = table.rowAtPoint(clickedPoint);
        Subscriber subscriber = subscriberList.get(row);

        if (event.getClickCount() == 2) {
            editSubscriberStrategy(subscriber);
        }
        else
        if ((event.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            popupMenu.showMenu(event, subscriber);
        }
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
        javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();
        classPropertyPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        title1Label = new javax.swing.JLabel();
        subscriberPropertyPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        title2Label = new javax.swing.JLabel();

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

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        classPropertyPanel.setLayout(new java.awt.BorderLayout());

        title1Label.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        title1Label.setText("jLabel1");
        jPanel1.add(title1Label);

        classPropertyPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

        tabbedPane.addTab("Contexts Class Property", classPropertyPanel);

        subscriberPropertyPanel.setLayout(new java.awt.BorderLayout());

        title2Label.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        title2Label.setText("jLabel1");
        jPanel2.add(title2Label);

        subscriberPropertyPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

        tabbedPane.addTab("Subscriber Strategies", subscriberPropertyPanel);

        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        if (editPanel != null) {
            try {
                //  Check for default (class property)
                Strategy strategy = Strategy.getContextsFromDB();
                if (strategy.different(defaultStrategy)) {
                    //  OK after edit -> put property in database
                    Strategy.putStrategiesToDB(defaultStrategy, null);
                    System.out.println("Put new strategy as class : " + defaultStrategy);
                }
                //  Do same thing for subscribers if has changed
                for (Subscriber subscriber : subscriberList) {
                    saveStrategyIfChanged(subscriber);
                }
                returnValue = JOptionPane.OK_OPTION;
                doClose();
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, e.getMessage(), e);
            }
        }
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
    private void saveStrategyIfChanged(Subscriber subscriber) throws DevFailed {

        //  Check if strategy from class property
        if (subscriber.getStrategy().isClassProperty()) {
            System.out.println("Remove properties");
            subscriber.delete_property(new String[] { "ContextsList","DefaultStrategy" });
        }
        else {
            Strategy strategy = subscriber.getStrategy();
            if (strategy.different(Strategy.getContextsFromDB(subscriber))) {
                //  OK after edit -> put property in database
                Strategy.putStrategiesToDB(strategy, subscriber);
                System.out.println("Put new strategy for " + subscriber.getLabel()+" : " + strategy);
            }
        }
    }
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
    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        if (isVisible()) {
            JTabbedPane tabbedPane = (JTabbedPane) evt.getSource();
            int index = tabbedPane.getSelectedIndex();
            if (index == SUBSCRIBER_STRATEGIES) {
                //  Set subscriber strategy if default (Could have changed)
                for (Subscriber subscriber : subscriberList) {
                    if (subscriber.getStrategy().isClassProperty()) {
                        subscriber.setStrategy(defaultStrategy);
                    }
                }
            }
        }
    }//GEN-LAST:event_tabbedPaneStateChanged
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
    private void editSubscriberStrategy(Subscriber subscriber) {
        try {
            EditContextsDialog dialog = new EditContextsDialog(parent, subscriber);
            if (dialog.showDialog()==JOptionPane.OK_OPTION) {
                Strategy strategy = dialog.getStrategy();
                subscriber.setStrategy(strategy);
                dataTableModel.fireTableDataChanged();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }

    }
	//===============================================================
	//===============================================================


	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel classPropertyPanel;
    private javax.swing.JPanel subscriberPropertyPanel;
    private javax.swing.JLabel title1Label;
    private javax.swing.JLabel title2Label;
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
            Subscriber subscriber = subscriberMap.getSubscriberByLabel("SR 1");
            if (subscriber==null) {
                Except.throw_exception("", "Subscriber not found !");
            }
            new EditStrategiesDialog(null, subscriberMap.getSubscriberList()).setVisible(true);
		}
		catch(Exception e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
		}
		System.exit(0);
	}
	//=========================================================================
	//=========================================================================





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
            return subscriberList.size();
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
            switch (column) {
                case SUBSCRIBER:
                    return subscriberList.get(row).getLabel();
                case STRATEGY:
                    return subscriberList.get(row).getStrategy().toString();
                case DEFAULT:
                    return subscriberList.get(row).getStrategy().isClassProperty();
                default:
                    return "? ?";
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
                    case SUBSCRIBER:
                    case STRATEGY:
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
    //======================================================
    //======================================================




    //======================================================
    /**
     * Popup menu class
     */
    //======================================================
    private static final int EDIT = 0;
    private static final int SET_DEFAULT = 1;
    private static final int COPY_STRATEGY = 2;
    private static final int PASTE_STRATEGY = 3;
    private static final int OFFSET = 2;    //	Label And separator

    private Subscriber selectedSubscriber;
    private Subscriber copiedSubscriber = null;
    private static String[] menuLabels = {
            "Edit Strategy",
            "Set strategy from class property",
            "Copy Strategy",
            "Paste Strategy",
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
        private void showMenu(MouseEvent event, Subscriber subscriber) {
            selectedSubscriber = subscriber;
            title.setText(subscriber.getLabel());
            getComponent(OFFSET + SET_DEFAULT).setEnabled(!subscriber.getStrategy().isClassProperty());
            getComponent(OFFSET + PASTE_STRATEGY).setEnabled(copiedSubscriber!=null);
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
                case EDIT:
                    editSubscriberStrategy(selectedSubscriber);
                    break;
                case SET_DEFAULT:
                    selectedSubscriber.setStrategy(defaultStrategy);
                    selectedSubscriber.getStrategy().setClassProperty(true);
                    break;
                case COPY_STRATEGY:
                    copiedSubscriber = selectedSubscriber;
                    break;
                case PASTE_STRATEGY:
                    selectedSubscriber.setStrategy(copiedSubscriber.getStrategy());
                    selectedSubscriber.getStrategy().setClassProperty(false);
                    break;
            }
        }
    }
}
