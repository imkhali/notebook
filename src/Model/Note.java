package Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Note {

    private final int id;
    private final LocalDate dateCreated;
    private String memo;
    private Set<String> tags;

    /**
     * this is used only when constructing notes read from the database
     */
    public Note(int id, LocalDate dateCreated, String memo, Set<String> tags) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.memo = memo;
        this.tags = tags;
    }

    public Note(int id, String memo, Set<String> tags) {
        this(id, LocalDate.now(), memo, tags);
    }

    /* Getters and Setters */
    public int getID() {
        return id;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /** return true if a given search term is in note's memo or tags
     * @param searchFilter string to match against
     * @param caseSensitive if true match is case sensitive, otherwise case insensitive
     */
    public boolean match(String searchFilter, boolean caseSensitive) {
        if (!caseSensitive)
            return memo.toLowerCase().contains(searchFilter.toLowerCase()) ||
                    tags.stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(searchFilter.toLowerCase());
        return memo.contains(searchFilter) || tags.contains(searchFilter);
    }

    /** return true if a given search term is in note's memo or tags, match is case insensitive
     * @param searchFilter string to match against
     */
    public boolean match(String searchFilter) {
        return match(searchFilter, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", dateCreated=" + DateTimeFormatter.ofPattern("yyyy/LL/dd").format(dateCreated) +
                ", memo='" + memo + '\'' +
                ", tags= [" + tags.stream().map(x -> "'" + x + "'").collect(Collectors.joining(", ")) + "]" +
                '}';
    }
}
