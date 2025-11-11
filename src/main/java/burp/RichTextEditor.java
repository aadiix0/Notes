package burp;

import javax.swing.*;
import java.awt.*;

import javax.swing.text.*;

import javax.swing.event.HyperlinkEvent;
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

        JButton bulletButton = new JButton("Bullet");
        bulletButton.addActionListener(e -> toggleBulletPoints());
        toolBar.add(bulletButton);

        JButton checklistButton = new JButton("Checklist");
        checklistButton.addActionListener(e -> insertChecklist());
        toolBar.add(checklistButton);

        JButton quoteButton = new JButton("Quote");
        quoteButton.addActionListener(e -> setBlockStyle(true, false));
        toolBar.add(quoteButton);

        JButton codeButton = new JButton("Code");
        codeButton.addActionListener(e -> setBlockStyle(false, true));
        toolBar.add(codeButton);
    }

    private void toggleBulletPoints() {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        Element paragraph = doc.getParagraphElement(start);
        AttributeSet as = paragraph.getAttributes();
        MutableAttributeSet newAs = new SimpleAttributeSet(as.copyAttributes());
        if (as.getAttribute(StyleConstants.ListAttributeName) == null) {
            StyleConstants.setListAttributes(newAs, true);
        } else {
            newAs.removeAttribute(StyleConstants.ListAttributeName);
        }
        doc.setParagraphAttributes(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset(), newAs, false);
    }

    private void insertChecklist() {
        try {
            textPane.getDocument().insertString(textPane.getCaretPosition(), "[ ] ", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void setBlockStyle(boolean isQuote, boolean isCode) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, isCode ? "Monospaced" : "SansSerif");
        StyleConstants.setForeground(attrs, isCode ? Color.DARK_GRAY : Color.BLACK);
        StyleConstants.setBackground(attrs, isQuote ? new Color(240, 240, 240) : Color.WHITE);
        StyleConstants.setLeftIndent(attrs, isQuote ? 20 : 0);
        StyleConstants.setRightIndent(attrs, isQuote ? 20 : 0);
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        doc.setCharacterAttributes(start, end - start, attrs, false);
    }

    public String getText() {
        return textPane.getText();
    }

    public void setText(String text) {
        textPane.setText(text);
    }
}
