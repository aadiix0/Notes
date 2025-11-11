package burp;

import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.ui.UserInterface;

import javax.swing.tree.DefaultTreeModel;
import java.io.*;

public class BurpExtender implements BurpExtension, burp.api.montoya.extension.ExtensionStateListener {
    private MontoyaApi api;
    private MainPanel mainPanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("Burp Notion");

        Logging logging = api.logging();
        logging.logToOutput("Burp Notion Extension loaded.");

        mainPanel = new MainPanel();
        loadNotes();
        api.userInterface().registerSuiteTab("Burp Notion", mainPanel);
        api.userInterface().registerContextMenuItemsProvider(new Menu(mainPanel));

        api.extension().registerExtensionStateListener(this);
    }

    @Override
    public void extensionUnloaded() {
        saveNotes();
    }

    private void saveNotes() {
        DefaultTreeModel treeModel = mainPanel.getTreeModel();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(treeModel);
            PersistedObject persistedObject = PersistedObject.persistedObject();
            persistedObject.setByteArray("notes", bos.toByteArray());
        } catch (IOException e) {
            api.logging().logToError(e);
        }
    }

    private void loadNotes() {
        PersistedObject persistedObject = PersistedObject.persistedObject();
        byte[] notesBytes = persistedObject.getByteArray("notes");
        if (notesBytes != null) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(notesBytes);
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                DefaultTreeModel treeModel = (DefaultTreeModel) in.readObject();
                mainPanel.setTreeModel(treeModel);
            } catch (IOException | ClassNotFoundException e) {
                api.logging().logToError(e);
            }
        }
    }
}
