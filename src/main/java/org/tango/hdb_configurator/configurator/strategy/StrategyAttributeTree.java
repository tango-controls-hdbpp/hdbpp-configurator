//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:	java source code for display JTree
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015
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
// $Revision: 1.2 $
//
// $Log:  $
//
//-======================================================================

package org.tango.hdb_configurator.configurator.strategy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.HdbAttribute;
import org.tango.hdb_configurator.common.SplashUtils;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class StrategyAttributeTree extends JTree implements TangoConst {
    private DefaultMutableTreeNode root;
    private List<HdbAttribute> attributeList;
    private Component parent;

    private static final int TG_HOST   = 0;
    private static final int DOMAIN    = 1;
    private static final int FAMILY    = 2;
    private static final int MEMBER    = 3;
    private static final int ATTRIBUTE = 4;
    //===============================================================
    //===============================================================
    StrategyAttributeTree(Component parent, String strategyName, List<HdbAttribute> attributeList) {
        super();
        this.parent = parent;
        this.attributeList = attributeList;
        setBackground(Color.white);

        buildTree(strategyName);
        expandChildren(root, DOMAIN);
        setSelectionPath(null);
    }
    //===============================================================
    //===============================================================
    private void buildTree(String root_name) {
        //  Create the nodes.
        root = new DefaultMutableTreeNode(root_name);
        createNodes();

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        //	Create Tree and Tree model
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //  Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        TangoRenderer renderer = new TangoRenderer();
        setCellRenderer(renderer);
    }
    //===============================================================
    //===============================================================
    private void createNodes() {
        addNodes(root, attributeList, TG_HOST);
    }
    //===============================================================
    //===============================================================
    private List<NameComponent> getNodeNames(List<HdbAttribute> attributes, int level) {
        //  Distribute HdbAttributes for node under specified level
        Hashtable<String, List<HdbAttribute>> hashTable = new Hashtable<>();
        for (HdbAttribute attribute : attributes) {
            String key = attribute.getNameComponents().get(level);
            List<HdbAttribute> list = hashTable.get(key);
            if (list==null) {
                list = new ArrayList<>();
                hashTable.put(key, list);
            }
            list.add(attribute);
        }
        //  Build list of NameComponent and sort
        List<NameComponent> nameComponents = new ArrayList<>();
        Collection<String> strategies = hashTable.keySet();
        for (String strategy : strategies) {
            nameComponents.add(new NameComponent(level, strategy, hashTable.get(strategy)));
        }
        Collections.sort(nameComponents, new ComponentComparator());
        return nameComponents;
    }
    //======================================================
    //======================================================
    private void addNodes(DefaultMutableTreeNode srcNode, List<HdbAttribute> attributes, int level) {
        List<NameComponent> nameComponents = getNodeNames(attributes, level);
        // Add node for each level(domain, family, ....)
        for (NameComponent nameComponent : nameComponents) {
            List<HdbAttribute> hdbAttributes = nameComponent.attributeList;
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(nameComponent);
            srcNode.add(node);

            //  Add nodes for next level if exists
            if (level<ATTRIBUTE) {
                addNodes(node, hdbAttributes, level+1);
            }
        }
    }
    //======================================================
    //======================================================
    List<HdbAttribute> getSelectedAttributes() {
        List<HdbAttribute>   attributes = new ArrayList<>();
        TreePath[]  paths = getSelectionPaths();
        if (paths!=null) {
            for (TreePath path : paths) {
                //  Check if attribute
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node!=root) {
                    if (node.getUserObject() instanceof NameComponent) {
                        NameComponent component = (NameComponent) node.getUserObject();
                        if (component.level>DOMAIN)
                            attributes.addAll(component.attributeList);
                    }
                }
            }
        }
        return attributes;
    }
    //===============================================================
    //===============================================================
    void expandChildren(final int level) {
        if (level<TG_HOST || level>ATTRIBUTE)
                ErrorPane.showErrorMessage(this, "Bad Value", new Exception("Bad level to expand !"));

        collapseChildren();
        expandChildren(root, level);
    }
    //===============================================================
    //===============================================================
    private void collapseChildren() {
        int row = getRowCount() - 1;
        while (row >= 0) {
            collapseRow(row);
            row--;
        }
    }
    //===============================================================
    //===============================================================
    private void expandChildren(DefaultMutableTreeNode node, int maxLevel) {
        for (int i=0 ; i<node.getChildCount() ; i++) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            NameComponent nameComponent = (NameComponent) (child.getUserObject());
            if (nameComponent.level<maxLevel) {
                expandChildren(child, maxLevel);
            }
            else {
                expandNode(child);
            }
        }
    }

    //===============================================================
    //===============================================================
    private void expandNode(DefaultMutableTreeNode node) {
        ArrayList<DefaultMutableTreeNode> nodeList = new ArrayList<>();
        nodeList.add(node);
        while (node!=root) {
            node = (DefaultMutableTreeNode) node.getParent();
            nodeList.add(0, node);
        }
        TreeNode[] tn = new DefaultMutableTreeNode[nodeList.size()];
        for (int i=0 ; i<nodeList.size() ; i++)
            tn[i] = nodeList.get(i);
        TreePath tp = new TreePath(tn);
        setSelectionPath(tp);
        scrollPathToVisible(tp);
    }
    //===============================================================
    //===============================================================




    //===============================================================
    //===============================================================
    private class NameComponent {
        private int level;
        private String name;
        private List<HdbAttribute> attributeList = null;
        private NameComponent(int level, String name, List<HdbAttribute> attributeList) {
            this.level = level;
            this.name  = name;
            this.attributeList = attributeList;
        }
        public String toString() {
            if (level==FAMILY || level==MEMBER)
                return name + " (" + attributeList.size() + ")";
            else
                return name;
        }
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private static ImageIcon hdbIcon;
    private static ImageIcon deviceIcon;
    private static ImageIcon attributeIcon;
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font rootFont = new Font("Dialog", Font.BOLD, 18);
        private Font nodeFont = new Font("Dialog", Font.BOLD, 12);
        private Font attributeFont = new Font("Dialog", Font.PLAIN, 12);

        //===============================================================
        //===============================================================
        private TangoRenderer() {
            try {
                Utils utils = Utils.getInstance();
                hdbIcon= utils.getIcon("hdb++.gif", 0.4);
                deviceIcon    = utils.getIcon("device.gif");
                attributeIcon = utils.getIcon("attribute.gif");
            }
            catch (DevFailed e) {
                SplashUtils.getInstance().stopSplash();
                ErrorPane.showErrorMessage(parent, null, e);
            }
        }
        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object obj,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, sel,
                    expanded, leaf, row,
                    hasFocus);

            setBackgroundNonSelectionColor(Color.white);
            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            if (row==0) {
                //	ROOT
                setFont(rootFont);
                setIcon(hdbIcon);
            } else {
                //DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
                if (leaf) {
                    setIcon(attributeIcon);
                    setFont(attributeFont);
                }
                else {
                    setIcon(deviceIcon);
                    setFont(nodeFont);
                }
            }
            return this;
        }
    }//	End of Renderer Class
    //==============================================================================
    //==============================================================================



    //==========================================================
    //==========================================================
    private class ComponentComparator implements Comparator<NameComponent> {
        public int compare(NameComponent component1, NameComponent component2) {
            if (component1 == null)
                return 1;
            else if (component2 == null)
                return -1;
            else
                return component1.name.compareTo(component2.name);
        }
    }
    //===============================================================
    //===============================================================
}
