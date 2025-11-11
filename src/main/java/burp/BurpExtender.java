package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;

import javax.swing.tree.DefaultTreeModel;
import java.io.*;

public class BurpExtender implements BurpExtension {
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
        api.userInterface().registerContextMenuItemsProvider(new ContextMenuProvider(api, mainPanel));

        api.extension().registerUnloadingHandler(new ExtensionUnloadingHandler() {
            @Override
            public void extensionUnloaded() {
                saveNotes();
            }
        });
    }

    private void saveNotes() {
        DefaultTreeModel treeModel = mainPanel.getTreeModel();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(treeModel);
            PersistedObject persistedObject = api.persistence().extensionData();
            persistedObject.setByteArray("notes", burp.api.montoya.core.ByteArray.byteArray(bos.toByteArray()));
        } catch (IOException e) {
            api.logging().logToError(e);
        }
    }

    private void loadNotes() {
        PersistedObject persistedObject = api.persistence().extensionData();
        burp.api.montoya.core.ByteArray notesBytes = persistedObject.getByteArray("notes");
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
