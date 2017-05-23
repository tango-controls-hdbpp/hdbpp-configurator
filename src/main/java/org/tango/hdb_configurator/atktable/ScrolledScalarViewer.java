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

import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.attribute.MultiScalarTableViewer;
import fr.esrf.tangoatk.widget.util.ErrorHistory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;

//=======================================================

/**
 * Class Description: AtkTable Viewer Class inside a ScrolledPane.
 * It is necessary to put the table inside a JScrollPane. The JTable does not
 * display the column names if the JTable is not in a scrollPane!!!
 *
 * @author Pascal Verdier
 */
//=======================================================
@SuppressWarnings("WeakerAccess")
class ScrolledScalarViewer extends JScrollPane implements IErrorListener,
                                                        INumberScalarListener,
                                                        IStringScalarListener,
                                                        IBooleanScalarListener,
                                                        IEnumScalarListener {
    private TableScalarViewer parent;
    private TableConfig config;
    private AttributeList attlist;
    private MultiScalarTableViewer table;
    private ErrorHistory err_history;
    private boolean on_error = false;


    private ScrolledScalarViewer thisViewer;

    //===========================================================
    //===========================================================
    ScrolledScalarViewer(TableScalarViewer parent) {
        this.parent = parent;
        thisViewer = this;
        config = new TableConfig();
        err_history = new ErrorHistory();

        table = new MultiScalarTableViewer();
        table.setFont(new Font("Dialog", Font.BOLD, 12));
        table.setUnitVisible(false);
        table.setEnabled(false);

        //	Attach an error history
        attlist = new AttributeList();
        attlist.addErrorListener(err_history);
        attlist.addErrorListener(this);
    }
    //===========================================================
    //===========================================================
    JTable getJTable() {
        return table;
    }
    //===========================================================
    //===========================================================
    void setDimension(Dimension dimension) {
        table.setPreferredScrollableViewportSize(dimension);
    }
    //===========================================================
    //===========================================================
    TableConfig getTableConfig() {
        return config;
    }
    //===========================================================
    //===========================================================
    void showErrorHistory() {
        err_history.setVisible(true);
    }
    //===========================================================
    //===========================================================
    void setTableFont(Font font) {
        table.setFont(font);
    }
    //===========================================================
    //===========================================================
    void resetTable() {
        table.clearModel();
        config.resetConnection();
        attlist.removeAllElements();
        attlist.stopRefresher();
    }

    //===========================================================
    //===========================================================
    void initializeViewer(TableConfig config) {
        resetTable();

        //	Set titles
        this.config = config;
        table.setNbColumns(config.columnNames.length);
        table.setNbRows(config.rowNames.length);
        table.setColumnIdents(config.columnNames);
        table.setRowIdents(config.rowNames);

        //	Set refresher period is value OK
        if (config.period>1000)
            attlist.setRefreshInterval(config.period);
        else
            attlist.setRefreshInterval(1000);

        attlist.startRefresher();

        config.checkSize(table);
        Dimension dimension = new Dimension(config.width, config.height);
        table.setPreferredScrollableViewportSize(dimension);
        setViewportView(table);

        //	All attributes will be add later by a thread
        new AddAttr().start();
    }
    //===============================================================
    //===============================================================
    void setColWidth(int title_width, int[] col_width) {
        //	Manage column width
        final Enumeration enumeration = table.getColumnModel().getColumns();
        TableColumn tc;
        if (enumeration.hasMoreElements()) {
            tc = (TableColumn) enumeration.nextElement();
            tc.setPreferredWidth(title_width);
        }

        for (int i = 0 ; enumeration.hasMoreElements() && i<col_width.length ; i++) {
            tc = (TableColumn) enumeration.nextElement();
            if (col_width[i]==0)
                col_width[i] = 80;
            tc.setPreferredWidth(col_width[i]);
        }
    }


    //===============================================================
    /*
	 *	Error Management:
	 *		on_error is set on listener
	 *		and reset on polling with a thread
	 */
    //===============================================================
    public void errorChange(ErrorEvent evt) {
        config.setError(evt.getSource().toString(), true);
        if (!on_error) {
            on_error = true;
            //noinspection ConstantConditions
            parent.errorChanged(on_error);
            new EndErrorCheck().start();
        }
    }

    //===============================================================
    //===============================================================
    public void stringScalarChange(StringScalarEvent evt) {
        if (on_error)
            config.setError(evt.getSource().toString(), false);
    }

    //===============================================================
    //===============================================================
    public void numberScalarChange(NumberScalarEvent evt) {
        if (on_error)
            config.setError(evt.getSource().toString(), false);
    }

    //===============================================================
    //===============================================================
    public void booleanScalarChange(BooleanScalarEvent evt) {
        if (on_error)
            config.setError(evt.getSource().toString(), false);
    }

    //===============================================================
    //===============================================================
    public void enumScalarChange(EnumScalarEvent evt) {
        if (on_error)
            config.setError(evt.getSource().toString(), false);
    }

    //===============================================================
    //===============================================================
    public void stateChange(AttributeStateEvent evt) {
        if (on_error)
            config.setError(evt.getSource().toString(), false);
    }

    //===============================================================
    //===============================================================
    class EndErrorCheck extends Thread {
        public EndErrorCheck() {
        }

        public void run() {
            boolean end = false;
            while (!end) {

                if (!config.onError()) {
                    on_error = false;
                    parent.errorChanged(on_error);
                    end = true;
                } else
                    try {
                        sleep(2000);
                    } catch (Exception e) {/* */}
            }
        }
    }
    //===============================================================
    //===============================================================


    //===========================================================
    //===========================================================
    class AddAttr extends Thread {

        //===========================================================
        private boolean connectAttribute(TableConfig.Attribute attribute) {
            if (attribute.getName()==null) {
                attribute.setConnected(true);
                return false;
            }

            try {
                //	Add listener on attribute.
                IAttribute iAttribute = (IAttribute) attlist.add(attribute.getName());

                if (iAttribute instanceof INumberScalar)
                    ((INumberScalar) iAttribute).addNumberScalarListener(thisViewer);
                else if (iAttribute instanceof IStringScalar)
                    ((IStringScalar) iAttribute).addStringScalarListener(thisViewer);
                else if (iAttribute instanceof IEnumScalar)
                    ((IEnumScalar) iAttribute).addEnumScalarListener(thisViewer);
                else if (iAttribute instanceof IBooleanScalar)
                    ((IBooleanScalar) iAttribute).addBooleanScalarListener(thisViewer);
                else
                    System.out.println(attribute.getName() + "  Not Supported Type !!");

                //	Set all attribute positions
                table.setModelAt(iAttribute, attribute.getRow(), attribute.getCol());
                attribute.setConnected(true);
                return true;
            } catch (Exception e) {
                attribute.setConnected(false);
                return false;
            }
        }

        //===========================================================
        public void run() {
            //	Try to connect to all attributes
            int nbConnected = 0;
            while (nbConnected<config.size()) {
                nbConnected = 0;
                for (int i=0 ; i<config.size() ; i++) {
                    TableConfig.Attribute attribute = config.attributeAt(i);
                    if (!attribute.isConnected()) {
                        connectAttribute(attribute);
                    }
                    if (attribute.isConnected())
                        nbConnected++;
                }

                //	wait a bit
                try {
                    sleep(2000);
                } catch (Exception e) { /* Nothing */ }
            }
        }
        //===========================================================
    }
}
