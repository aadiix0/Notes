package burp;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;
import java.net.URISyntaxException;

public class RichTextEditor extends JPanel {
    private JTextPane textPane;

    public RichTextEditor() {
        setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setEditorKit(new HTMLEditorKit());
        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(textPane);

        JToolBar toolBar = new JToolBar();
        addFormattingButtons(toolBar);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addFormattingButtons(JToolBar toolBar) {
        JButton boldButton = new JButton(new StyledEditorKit.BoldAction());
        boldButton.setText("Bold");
        toolBar.add(boldButton);

        JButton italicButton = new JButton(new StyledEditorKit.ItalicAction());
        italicButton.setText("Italic");
        toolBar.add(italicButton);

        JButton underlineButton = new JButton(new StyledEditorKit.UnderlineAction());
        underlineButton.setText("Underline");
        toolBar.add(underlineButton);

        JButton strikeThroughButton = new JButton(new StrikeThroughAction());
        strikeThroughButton.setText("Strikethrough");
        toolBar.add(strikeThroughButton);

        toolBar.addSeparator();

        JButton bulletButton = new JButton("Bullet List");
        bulletButton.addActionListener(e -> {
            try {
                String selectedText = textPane.getSelectedText();
                if (selectedText != null) {
                    String[] lines = selectedText.split("\n");
                    StringBuilder list = new StringBuilder("<ul>");
                    for (String line : lines) {
                        list.append("<li>").append(line).append("</li>");
                    }
                    list.append("</ul>");
                    textPane.getDocument().insertString(textPane.getSelectionStart(), list.toString(), null);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        toolBar.add(bulletButton);

        JButton quoteButton = new JButton("Blockquote");
        quoteButton.addActionListener(e -> {
            try {
                String selectedText = textPane.getSelectedText();
                if (selectedText != null) {
                    textPane.getDocument().insertString(textPane.getSelectionStart(), "<blockquote>" + selectedText + "</blockquote>", null);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        toolBar.add(quoteButton);

        JButton codeButton = new JButton("Code Block");
        codeButton.addActionListener(e -> {
            try {
                String selectedText = textPane.getSelectedText();
                if (selectedText != null) {
                    textPane.getDocument().insertString(textPane.getSelectionStart(), "<pre>" + selectedText + "</pre>", null);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        toolBar.add(codeButton);

        toolBar.addSeparator();

        JButton linkButton = new JButton("Link");
        linkButton.addActionListener(e -> {
            String url = JOptionPane.showInputDialog(this, "Enter URL:", "Insert Hyperlink", JOptionPane.PLAIN_MESSAGE);
            if (url != null && !url.trim().isEmpty()) {
                try {
                    String selectedText = textPane.getSelectedText();
                    if (selectedText == null) {
                        selectedText = url;
                    }
                    textPane.getDocument().insertString(textPane.getSelectionStart(), "<a href=\"" + url + "\">" + selectedText + "</a>", null);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolBar.add(linkButton);
    }

    public String getText() {
        return textPane.getText();
    }

    public void setText(String text) {
        textPane.setText(text);
    }

    // Custom action for strikethrough
    class StrikeThroughAction extends StyledEditorKit.StyledTextAction {
        public StrikeThroughAction() {
            super("font-strikethrough");
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            JEditorPane editor = getEditor(e);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean strikeThrough = !StyleConstants.isStrikeThrough(attr);
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setStrikeThrough(sas, strikeThrough);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }
}
