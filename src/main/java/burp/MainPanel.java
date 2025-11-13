package burp;

import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


public class MainPanel extends JPanel {
    private JTree noteTree;
    private DefaultTreeModel treeModel;
    private DefaultTreeModel originalTreeModel;
    private RSyntaxTextArea editor;
    private JSplitPane splitPane;
    private int lastDividerLocation = 250;
    private int defaultDividerSize;


    public MainPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // --- Left Panel ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // Create a new top panel to hold search and filters
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        // Create the search bar
        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search...");
        searchField.putClientProperty("JTextField.leadingIcon", UIManager.getIcon("Actions.find"));
        searchField.putClientProperty("JTextField.trailingComponent", new JLabel(UIManager.getIcon("Component.helpIcon")));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterTreeByText(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterTreeByText(searchField.getText()); }
            public void insertUpdate(DocumentEvent e) { filterTreeByText(searchField.getText()); }
        });
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton toggleButton = new JButton("<<");
        toggleButton.setToolTipText("Toggle folder structure");
        searchPanel.add(toggleButton, BorderLayout.EAST);


        // Create the filter tabs panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JToggleButton allButton = new JToggleButton("All");
        JToggleButton doneButton = new JToggleButton("Done");
        JToggleButton todoButton = new JToggleButton("To do");

        allButton.addActionListener(e -> filterTreeByStatus(null));
        doneButton.addActionListener(e -> filterTreeByStatus(NoteEntry.NoteStatus.DONE));
        todoButton.addActionListener(e -> filterTreeByStatus(NoteEntry.NoteStatus.TODO));


        // Style the buttons
        allButton.putClientProperty("JButton.buttonType", "roundRect");
        doneButton.putClientProperty("JButton.buttonType", "roundRect");
        todoButton.putClientProperty("JButton.buttonType", "roundRect");
        allButton.setSelected(true);

        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(allButton);
        filterGroup.add(doneButton);
        filterGroup.add(todoButton);

        filterPanel.add(allButton);
        filterPanel.add(Box.createHorizontalStrut(5));
        filterPanel.add(doneButton);
        filterPanel.add(Box.createHorizontalStrut(5));
        filterPanel.add(todoButton);

        topPanel.add(searchPanel);
        topPanel.add(filterPanel);

        leftPanel.add(topPanel, BorderLayout.NORTH);


        // Create the tree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new NoteEntry("Notes", "", true));
        treeModel = new DefaultTreeModel(rootNode);
        originalTreeModel = treeModel;
        noteTree = new JTree(treeModel);
        noteTree.setCellRenderer(new CustomTreeCellRenderer());
        noteTree.setEditable(true);
        noteTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        noteTree.setShowsRootHandles(true);
        noteTree.setDragEnabled(true);
        noteTree.setDropMode(DropMode.ON_OR_INSERT);
        noteTree.setTransferHandler(new TreeTransferHandler());
        JScrollPane treeScrollPane = new JScrollPane(noteTree);
        treeScrollPane.setOpaque(false);
        treeScrollPane.getViewport().setOpaque(false);
        treeScrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);

        // Add context menu
        noteTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) { showContextMenu(e); } }
            public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) { showContextMenu(e); } }
        });

        // Add "Create new group" button
        JButton createGroupButton = new JButton("Create new group");
        createGroupButton.putClientProperty("JButton.buttonType", "roundRect");
        createGroupButton.setIcon(UIManager.getIcon("Tree.plusIcon"));
        createGroupButton.addActionListener(e -> addFolder((DefaultMutableTreeNode) treeModel.getRoot()));
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(createGroupButton, BorderLayout.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Right Panel (Editor) ---
        editor = new RSyntaxTextArea(20, 60);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        editor.setCodeFoldingEnabled(true);
        editor.setAntiAliasingEnabled(true);
        editor.setBackground(new Color(43, 43, 43));
        editor.setForeground(new Color(204, 204, 204));
        editor.setCurrentLineHighlightColor(new Color(53, 53, 53));


        RTextScrollPane editorScrollPane = new RTextScrollPane(editor);
        editorScrollPane.getGutter().setBackground(new Color(43, 43, 43));
        editorScrollPane.setOpaque(false);
        editorScrollPane.getViewport().setOpaque(false);
        editorScrollPane.setBorder(BorderFactory.createEmptyBorder());


        // Add tree selection listener to update editor
        noteTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) noteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();
                if (!selectedEntry.isFolder()) {
                    editor.setText(selectedEntry.getContent());
                }
            }
        });


        // Create a split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editorScrollPane);
        splitPane.setDividerLocation(lastDividerLocation);
        defaultDividerSize = splitPane.getDividerSize();


        // Add action listener to the toggle button
        toggleButton.addActionListener(e -> toggleLeftPanel());

        // Add keyboard shortcut for toggling
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("control B"), "toggleLeftPanel");
        getActionMap().put("toggleLeftPanel", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleLeftPanel();
            }
        });


        add(splitPane, BorderLayout.CENTER);
    }

    private void toggleLeftPanel() {
        if (splitPane.getLeftComponent().isVisible()) {
            // Hide left panel
            lastDividerLocation = splitPane.getDividerLocation();
            splitPane.getLeftComponent().setVisible(false);
            splitPane.setDividerSize(0);
        } else {
            // Show left panel
            splitPane.getLeftComponent().setVisible(true);
            splitPane.setDividerLocation(lastDividerLocation);
            splitPane.setDividerSize(defaultDividerSize);
        }
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
        this.originalTreeModel = treeModel;
        noteTree.setModel(treeModel);
    }

    public void linkRequest(HttpRequestResponse requestResponse) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) noteTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            NoteEntry selectedEntry = (NoteEntry) selectedNode.getUserObject();
            if (!selectedEntry.isFolder()) {
                String link = String.format("<a href=\"%s\">%s</a>", requestResponse.url(), requestResponse.url());
                selectedEntry.setContent(selectedEntry.getContent() + "\n" + link);
                editor.setText(selectedEntry.getContent());
            }
        }
    }

    private void filterTreeByText(String searchText) {
        if (searchText.isEmpty() || searchText.equals("Search...")) {
            noteTree.setModel(originalTreeModel);
            treeModel = originalTreeModel;
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) originalTreeModel.getRoot();
        DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(root.getUserObject());
        filterByText(root, filteredRoot, searchText.toLowerCase());
        treeModel = new DefaultTreeModel(filteredRoot);
        noteTree.setModel(treeModel);
    }

    private boolean filterByText(DefaultMutableTreeNode parent, DefaultMutableTreeNode filteredParent, String searchText) {
        boolean parentMatches = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            NoteEntry entry = (NoteEntry) child.getUserObject();
            DefaultMutableTreeNode filteredChild = new DefaultMutableTreeNode(entry);

            if (filterByText(child, filteredChild, searchText) || entry.getTitle().toLowerCase().contains(searchText)) {
                filteredParent.add(filteredChild);
                parentMatches = true;
            }
        }
        return parentMatches;
    }

    private void filterTreeByStatus(NoteEntry.NoteStatus status) {
        if (status == null) {
            noteTree.setModel(originalTreeModel);
            treeModel = originalTreeModel;
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) originalTreeModel.getRoot();
        DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(root.getUserObject());
        filterByStatus(root, filteredRoot, status);
        treeModel = new DefaultTreeModel(filteredRoot);
        noteTree.setModel(treeModel);
    }

    private boolean filterByStatus(DefaultMutableTreeNode parent, DefaultMutableTreeNode filteredParent, NoteEntry.NoteStatus status) {
        boolean parentMatches = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            NoteEntry entry = (NoteEntry) child.getUserObject();
            DefaultMutableTreeNode filteredChild = new DefaultMutableTreeNode(entry);

            if (entry.isFolder()) {
                if (filterByStatus(child, filteredChild, status)) {
                    filteredParent.add(filteredChild);
                    parentMatches = true;
                }
            } else if (entry.getStatus() == status) {
                filteredParent.add(filteredChild);
                parentMatches = true;
            }
        }
        return parentMatches;
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
                                  DefaultMutableTreeNode[].class.getName() +
                                  "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }

        public boolean canImport(TransferSupport support) {
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

        public boolean importData(TransferSupport support) {
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
            for(int i = 0; i < nodes.length; i++) {
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
