package burp;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class NoteEntry implements Serializable {
    public enum NoteStatus { TODO, DONE, NONE }
    private static final long serialVersionUID = 1L;
    private String title;
    private String content;
    private boolean isFolder;
    private List<String> tags;
    private NoteStatus status;


    public NoteEntry(String title, String content, boolean isFolder) {
        this.title = title;
        this.content = content;
        this.isFolder = isFolder;
        this.tags = new ArrayList<>();
        this.status = isFolder ? NoteStatus.NONE : NoteStatus.TODO;
    }

    public NoteStatus getStatus() {
        return status;
    }

    public void setStatus(NoteStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return title;
    }
}
