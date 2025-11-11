package burp;

import javax.swing.*;
import java.awt.*;

import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MainPanel extends JPanel {
    private JTree noteTree;
    private DefaultTreeModel treeModel;
    private DefaultTreeModel originalTreeModel;
    private RichTextEditor editor;

    public MainPanel() {
        setLayout(new BorderLayout());

        // Create the root node and the tree model
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new NoteEntry("Notes", "", true));
        treeModel = new DefaultTreeModel(rootNode);
        originalTreeModel = treeModel;

        // Create the tree
        noteTree = new JTree(treeModel);
        noteTree.setEditable(true);
        noteTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        noteTree.setShowsRootHandles(true);
        noteTree.setDragEnabled(true);
        noteTree.setDropMode(DropMode.ON_OR_INSERT);
        noteTree.setTransferHandler(new TreeTransferHandler());
        JScrollPane treeScrollPane = new JScrollPane(noteTree);

        // Add a mouse listener for the context menu
        noteTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        // Create the rich text editor
        editor = new RichTextEditor();

        // Add a tree selection listener
        noteTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) noteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();
                if (!selectedEntry.isFolder()) {
                    editor.setText(selectedEntry.getContent());
                }
            }
        });

        // Create a search bar
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTree(searchField.getText());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTree(searchField.getText());
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTree(searchField.getText());
            }
        });

        // Create a tag field
        JTextField tagField = new JTextField();
        tagField.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) noteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();
                if (!selectedEntry.isFolder()) {
                    selectedEntry.addTag(tagField.getText());
                    tagField.setText("");
                }
            }
        });

        // Add the search and tag fields to a panel
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JPanel tagPanel = new JPanel(new BorderLayout());
        tagPanel.add(new JLabel("Tags: "), BorderLayout.WEST);
        tagPanel.add(tagField, BorderLayout.CENTER);
        topPanel.add(searchPanel);
        topPanel.add(tagPanel);

        // Create a split pane to hold the tree and the editor
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, editor);
        splitPane.setDividerLocation(200);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void showContextMenu(MouseEvent e) {
        TreePath path = noteTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        noteTree.setSelectionPath(path);
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();

        JPopupMenu contextMenu = new JPopupMenu();

        if (selectedEntry.isFolder()) {
            JMenuItem addNoteItem = new JMenuItem("Add Note");
            addNoteItem.addActionListener(ae -> addNote(selectedNode));
            contextMenu.add(addNoteItem);

            JMenuItem addFolderItem = new JMenuItem("Add Folder");
            addFolderItem.addActionListener(ae -> addFolder(selectedNode));
            contextMenu.add(addFolderItem);
        }

        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(ae -> noteTree.startEditingAtPath(path));
        contextMenu.add(renameItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(ae -> deleteNode(selectedNode));
        contextMenu.add(deleteItem);

        contextMenu.show(noteTree, e.getX(), e.getY());
    }

    private void addNote(DefaultMutableTreeNode parent) {
        String noteName = JOptionPane.showInputDialog(this, "Enter note name:", "New Note", JOptionPane.PLAIN_MESSAGE);
        if (noteName != null && !noteName.trim().isEmpty()) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new NoteEntry(noteName, "", false));
            treeModel.insertNodeInto(newNode, parent, parent.getChildCount());
            noteTree.scrollPathToVisible(new TreePath(newNode.getPath()));
        }
    }

    private void addFolder(DefaultMutableTreeNode parent) {
        String folderName = JOptionPane.showInputDialog(this, "Enter folder name:", "New Folder", JOptionPane.PLAIN_MESSAGE);
        if (folderName != null && !folderName.trim().isEmpty()) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new NoteEntry(folderName, "", true));
            treeModel.insertNodeInto(newNode, parent, parent.getChildCount());
            noteTree.scrollPathToVisible(new TreePath(newNode.getPath()));
        }
    }

    private void deleteNode(DefaultMutableTreeNode node) {
        if (node.getParent() != null) {
            treeModel.removeNodeFromParent(node);
        }
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public void setTreeModel(DefaultTreeModel treeModel) {
        this.treeModel = treeModel;
        noteTree.setModel(treeModel);
    }

    public void linkRequest(HttpRequestResponse requestResponse) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) noteTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();
            if (!selectedEntry.isFolder()) {
                String link = String.format("<a href=\"%s\">%s</a>", requestResponse.request().url(), requestResponse.request().url());
                selectedEntry.setContent(selectedEntry.getContent() + "\n" + link);
                editor.setText(selectedEntry.getContent());
            }
        }
    }

    private void filterTree(String searchText) {
        if (searchText.isEmpty()) {
            noteTree.setModel(originalTreeModel);
            treeModel = originalTreeModel;
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) originalTreeModel.getRoot();
        DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(root.getUserObject());
        filter(root, filteredRoot, searchText.toLowerCase());
        treeModel = new DefaultTreeModel(filteredRoot);
        noteTree.setModel(treeModel);
    }

    private void filter(DefaultMutableTreeNode parent, DefaultMutableTreeNode filteredParent, String searchText) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            NoteEntry entry = (NoteEntry) child.getUserObject();
            if (entry.getTitle().toLowerCase().contains(searchText) ||
                entry.getContent().toLowerCase().contains(searchText) ||
                entry.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchText))) {
                DefaultMutableTreeNode filteredChild = new DefaultMutableTreeNode(entry);
                filteredParent.add(filteredChild);
                filter(child, filteredChild, searchText);
            }
        }
    }

    // Inner class for drag and drop functionality
    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        DefaultMutableTreeNode[] nodesToRemove;

        public TreeTransferHandler() {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                                  ";class=\"" +
                                  javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                                  "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            support.setShowDropLocation(true);
            if (!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            JTree tree = (JTree) support.getComponent();
            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();
            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] == dropRow) {
                    return false;
                }
            }
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();
            NoteEntry targetEntry = (NoteEntry) target.getUserObject();
            if (!targetEntry.isFolder()) {
                return false;
            }
            return true;
        }

        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] paths = tree.getSelectionPaths();
            if (paths != null) {
                DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    nodes[i] = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                }
                return new NodesTransferable(nodes);
            }
            return null;
        }

        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            DefaultMutableTreeNode[] nodes = null;
            try {
                Transferable t = support.getTransferable();
                nodes = (DefaultMutableTreeNode[]) t.getTransferData(nodesFlavor);
            } catch (UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch (java.io.IOException ioe) {
                System.out.println("I/O error: " + ioe.getMessage());
            }
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
            JTree tree = (JTree) support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            int index = childIndex;
            if (childIndex == -1) {
                index = parent.getChildCount();
            }
            nodesToRemove = new DefaultMutableTreeNode[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                nodesToRemove[i] = nodes[i];
            }
            for (int i = 0; i < nodes.length; i++) {
                model.insertNodeInto(nodes[i], parent, index++);
            }
            return true;
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if ((action & MOVE) == MOVE) {
                JTree tree = (JTree) source;
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                for (int i = 0; i < nodesToRemove.length; i++) {
                    model.removeNodeFromParent(nodesToRemove[i]);
                }
            }
        }

        public class NodesTransferable implements Transferable {
            DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return nodes;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
}
