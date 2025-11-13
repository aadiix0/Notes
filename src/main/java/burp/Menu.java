package burp;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.Component;
import java.util.List;

public class Menu implements ContextMenuItemsProvider {
    private final MainPanel mainPanel;

    public Menu(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        JMenuItem menuItem = new JMenuItem("Link to Note");
        menuItem.addActionListener(e -> {
            List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
            if (!requestResponses.isEmpty()) {
                mainPanel.linkRequest(requestResponses.get(0));
            }
        });
        return List.of(menuItem);
    }
}
