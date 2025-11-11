package burp;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.menu.BasicMenuItem;
import burp.api.montoya.ui.menu.Menu;

import javax.swing.*;
import java.util.List;

public class Menu implements burp.api.montoya.ui.menu.ContextMenuItemsProvider {
    private final MainPanel mainPanel;

    public Menu(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public List<Component> provideMenuItems(HttpRequestResponse... httpRequestResponses) {
        BasicMenuItem menuItem = new BasicMenuItem("Link to Note");
        menuItem.addActionListener(e -> {
            // Link the request to the currently selected note
            mainPanel.linkRequest(httpRequestResponses[0]);
        });
        return List.of(menuItem);
    }
}
