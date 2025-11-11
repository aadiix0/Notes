package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.util.List;

public class ContextMenuProvider implements ContextMenuItemsProvider {
    private final MainPanel mainPanel;

    public ContextMenuProvider(MontoyaApi api, MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        JMenuItem menuItem = new JMenuItem("Link to Note");
        menuItem.addActionListener(e -> {
            mainPanel.linkRequest(event.messageEditorRequestResponse().get().requestResponse());
        });

        if (event.messageEditorRequestResponse().isEmpty()) {
            return null;
        }

        return List.of(menuItem);
    }
}
