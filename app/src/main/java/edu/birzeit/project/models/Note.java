package edu.birzeit.project.models;

import java.util.Objects;

public class Note {
    private long id;
    private final String ownerEmail;
    private String title;
    private String content;
    private long createdAt;
    private long updatedAt;
    private boolean favorite;
    private String tag;

    public Note(long id, String ownerEmail, String title, String content, long createdAt,
                long updatedAt, boolean favorite, String tag) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.favorite = favorite;
        this.tag = tag;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Note)) return false;
        Note note = (Note) object;
        return id == note.id && createdAt == note.createdAt && updatedAt == note.updatedAt
                && favorite == note.favorite && Objects.equals(ownerEmail, note.ownerEmail)
                && Objects.equals(title, note.title) && Objects.equals(content, note.content)
                && Objects.equals(tag, note.tag);
    }

    @Override
    public int hashCode() { return Objects.hash(id, ownerEmail, title, content, createdAt, updatedAt, favorite, tag); }
}
