package burp;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Color selectionColor = new Color(13, 126, 255); // #0D7EFF

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        setOpaque(true);
        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));

        // Set the background color
        if (sel) {
            setBackground(selectionColor);
        } else {
            setBackground(tree.getBackground());
        }


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


        return this;
    }
}
