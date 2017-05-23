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

package org.tango.hdb_configurator.atktable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//=======================================================
/**
 * Class Description: Basic JPanel Class to display
 * an ATK  MultiScalaViewer
 *
 * @author Pascal Verdier
 */
//=======================================================
public class TableScalarViewer extends JPanel {
    private ScrolledScalarViewer viewer = null;
    private JButton errorBtn;
    private JLabel titleLabel;

    //===============================================================
    /**
     * Creates new form TableScalarViewer
     *
     * @param    rowNameList        Row titles
     * @param    columnNames    Column titles
     * @param    attributeList  Attribute names (by line)
     * @param    columnWidths   column widths
     */
    //===============================================================
    public TableScalarViewer(List<String> rowNameList, String[] columnNames,
                             List<String[]> attributeList, int[] columnWidths) {
        String[] rowNames = new String[rowNameList.size()];
        for (int i=0 ; i<rowNameList.size() ; i++) rowNames[i] = rowNameList.get(i);
        String[][] attributeNames = new String[attributeList.size()][];
        for (int i=0 ; i<attributeList.size() ; i++) {
            attributeNames[i] = new String[attributeList.get(i).length];
            for (int j=0 ; j<attributeList.get(i).length ; j++) {
                attributeNames[i][j] = attributeList.get(i)[j];
            }
        }
        initComponents(rowNames, columnNames, attributeNames, columnWidths);
    }
    //===============================================================
    //===============================================================
    private void initComponents(String[] rowNames, String[] columnNames,
                                String[][] attributeNames, int[] columnWidths) {

        TableConfig config =  new TableConfig(rowNames, columnNames, attributeNames, columnWidths);
        setLayout(new BorderLayout());

        //	Create title label
        titleLabel = new JLabel(new TableConfig().title);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));

        //	Create Error btn
        errorBtn = new JButton("");
        errorBtn.setBorderPainted(false);
        errorBtn.setContentAreaFilled(false);
        errorBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        errorBtn.setVisible(false);
        //errorBtn.setIcon(Utils.getInstance().getIcon("redball.gif"));
        errorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorBtnActionPerformed();
            }
        });

        //	And put in in a panel on top
        JPanel panel = new JPanel();
        panel.add(titleLabel);
        panel.add(new JLabel("      "));
        panel.add(errorBtn);
        add(panel, BorderLayout.NORTH);

        //	Build the Table viewer
        viewer = new ScrolledScalarViewer(this);
        add(viewer, BorderLayout.CENTER);

        initScalarViewer(config);
        titleLabel.setText(config.title);
    }
    //===============================================================
    /**
     * Attributes global state management
     */
    //===============================================================
    public void errorChanged(boolean on_error) {
        errorBtn.setVisible(on_error);
    }
    //=======================================================
    /**
     * Set the table title
     */
    //=======================================================
    public void setPanelTitle(String title) {
        if (viewer!=null) {
            viewer.getTableConfig().title = title;
            titleLabel.setText(title);
        }
    }
    //=======================================================
    /**
     * Set the table title font
     */
    //=======================================================
    public void setPanelTitleFont(Font font) {
        titleLabel.setFont(font);
    }
    //=======================================================
    //=======================================================
    public void setPanelTitleVisible(boolean b) {
        titleLabel.getParent().setVisible(b);
    }

    //=======================================================
    /**
     * Set the table size
     */
    //=======================================================
    public void setDimension(Dimension dimension) {
        viewer.setDimension(dimension);
        getMainContainer(this).pack();
    }
    //=======================================================
    //=======================================================
    public Dimension getDimension() {
        return viewer.getSize();
    }
    //=======================================================
    //=======================================================
    private void initScalarViewer(TableConfig config) {
        viewer.initializeViewer(config);
        titleLabel.setText(config.title);
    }
    //=======================================================
    //=======================================================
    private void errorBtnActionPerformed() {
        showErrorHistory();
    }
    //=======================================================
    //=======================================================
    static Window getMainContainer(Component c) {
        Container parent = c.getParent();
        while (!(parent instanceof JFrame) &&
                !(parent instanceof JDialog) &&
                !(parent instanceof JWindow)) {
            parent = parent.getParent();
        }
        return (Window) parent;
    }
    //=======================================================
    //=======================================================
    void atkDiagnostic() {
        fr.esrf.tangoatk.widget.util.ATKDiagnostic.showDiagnostic();
    }
    //=======================================================
    //=======================================================
    public void showErrorHistory() {
        viewer.showErrorHistory();
    }
    //===============================================================
    //===============================================================
    public void setColWidth(int title_width, int[] col_width) {
        viewer.getTableConfig().setColWidth(title_width, col_width);
        viewer.setColWidth(title_width, col_width);
    }
    //===============================================================
    //===============================================================
    public void setColWidth(int[] columnWidth) {
        //	Take first as title width
        int title_width = columnWidth[0];
        int[] col_width = new int[columnWidth.length - 1];
        System.arraycopy(columnWidth, 1, col_width, 0, columnWidth.length - 1);

        setColWidth(title_width, col_width);
    }
    //===============================================================
    //===============================================================
    public JTable getJTable() {
        return viewer.getJTable();
    }
    //===============================================================
    //===============================================================
    public void setTableFont(Font font) {
        viewer.setTableFont(font);
    }
}
