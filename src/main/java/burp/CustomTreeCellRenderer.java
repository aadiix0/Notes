package burp;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof NoteEntry) {
            NoteEntry entry = (NoteEntry) userObject;
            if (entry.isFolder()) {
                // Set folder icon
                if (expanded) {
                    setIcon(UIManager.getIcon("Tree.expandedIcon"));
                } else {
                    setIcon(UIManager.getIcon("Tree.collapsedIcon"));
                }
            } else {
                // Set file icon
                setIcon(UIManager.getIcon("Tree.leafIcon"));
            }
        }

        // Set background and foreground colors
        if (sel) {
            setBackground(new Color(228, 231, 235));
            setForeground(UIManager.getColor("Tree.selectionForeground"));
        } else {
            setBackground(UIManager.getColor("Tree.background"));
            setForeground(UIManager.getColor("Tree.foreground"));
        }

        return this;
    }
}
