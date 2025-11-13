package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;

public class BurpExtender implements BurpExtension {
    private MontoyaApi api;
    private MainPanel mainPanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("Burp Notion");
        FlatDarculaLaf.setup();

        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("Tree.paint.lines", false);
        UIManager.put("Tree.rowHeight", 24);
        UIManager.put("Tree.leftChildIndent", 10);
        UIManager.put("Tree.rightChildIndent", 10);

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
