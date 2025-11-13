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
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            } else {
                // Set file icon
                setIcon(UIManager.getIcon("Tree.leafIcon"));
            }
        }

        // Set background and foreground colors
        if (sel) {
            setBackground(new Color(69, 69, 69));
            setForeground(Color.WHITE);
        } else {
            setBackground(tree.getBackground());
            setForeground(UIManager.getColor("Tree.foreground"));
        }
        setOpaque(true);

        return this;
    }
}
