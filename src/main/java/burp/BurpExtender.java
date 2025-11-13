package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.tree.DefaultTreeModel;
import java.io.*;

public class BurpExtender implements BurpExtension {
    private MontoyaApi api;
    private MainPanel mainPanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("Burp Notion");

        // Apply FlatLaf Darcula theme
        FlatDarculaLaf.setup();

        // Apply custom dark theme properties
        javax.swing.UIManager.put("Panel.background", new java.awt.Color(43, 43, 43));
        javax.swing.UIManager.put("Component.background", new java.awt.Color(43, 43, 43));
        javax.swing.UIManager.put("Window.background", new java.awt.Color(43, 43, 43));
        javax.swing.UIManager.put("TextArea.background", new java.awt.Color(43, 43, 43));
        javax.swing.UIManager.put("Tree.background", new java.awt.Color(43, 43, 43));
        javax.swing.UIManager.put("TextField.background", new java.awt.Color(43, 43, 43));

        // Set tree icons
        javax.swing.UIManager.put("Tree.collapsedIcon", javax.swing.UIManager.getIcon("Component.arrow.right"));
        javax.swing.UIManager.put("Tree.expandedIcon", javax.swing.UIManager.getIcon("Component.arrow.down"));
        javax.swing.UIManager.put("Tree.paintLines", false);


        javax.swing.UIManager.put("Component.foreground", new java.awt.Color(204, 204, 204));
        javax.swing.UIManager.put("Label.foreground", new java.awt.Color(204, 204, 204));
        javax.swing.UIManager.put("TextArea.foreground", new java.awt.Color(204, 204, 204));
        javax.swing.UIManager.put("Tree.foreground", new java.awt.Color(204, 204, 204));
        javax.swing.UIManager.put("TextField.foreground", new java.awt.Color(204, 204, 204));


        Logging logging = api.logging();
        logging.logToOutput("Burp Notion Extension loaded.");

        mainPanel = new MainPanel();
        loadNotes();
        api.userInterface().registerSuiteTab("Burp Notion", mainPanel);
        api.userInterface().registerContextMenuItemsProvider(new Menu(mainPanel));

        api.extension().registerUnloadingHandler(this::saveNotes);
    }

    private void saveNotes() {
        DefaultTreeModel treeModel = mainPanel.getTreeModel();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(treeModel);
            PersistedObject persistedObject = api.persistence().extensionData();
            persistedObject.setByteArray("notes", ByteArray.byteArray(bos.toByteArray()));
        } catch (IOException e) {
            api.logging().logToError(e);
        }
    }

    private void loadNotes() {
        PersistedObject persistedObject = api.persistence().extensionData();
        ByteArray notesBytes = persistedObject.getByteArray("notes");
        if (notesBytes != null) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(notesBytes.getBytes());
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                DefaultTreeModel treeModel = (DefaultTreeModel) in.readObject();
                mainPanel.setTreeModel(treeModel);
            } catch (IOException | ClassNotFoundException e) {
                api.logging().logToError(e);
            }
        }
    }
}
