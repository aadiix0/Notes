package burp;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RichTextEditor extends JPanel {
    private JTextPane textPane;
    private NoteEntry currentNoteEntry;

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

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveContent();
            }
        });

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
        try {
            StyledDocument doc = textPane.getStyledDocument();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            Element startPara = doc.getParagraphElement(start);
            Element endPara = doc.getParagraphElement(end);
            for (int i = startPara.getStartOffset(); i <= endPara.getEndOffset(); i = doc.getParagraphElement(i).getEndOffset() + 1) {
                Element para = doc.getParagraphElement(i);
                if (para.getEndOffset() > doc.getLength()) {
                    break;
                }
                String text = doc.getText(para.getStartOffset(), para.getEndOffset() - para.getStartOffset());
                if (text.startsWith("• ")) {
                    doc.remove(para.getStartOffset(), 2);
                } else {
                    doc.insertString(para.getStartOffset(), "• ", null);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    public void setNoteEntry(NoteEntry noteEntry) {
        this.currentNoteEntry = noteEntry;
    }

    private void saveContent() {
        if (currentNoteEntry != null) {
            currentNoteEntry.setContent(textPane.getText());
        }
    }
}
