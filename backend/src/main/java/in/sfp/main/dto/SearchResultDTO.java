package in.sfp.main.dto;

public class SearchResultDTO {
    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String link;

    public SearchResultDTO() {}

    public SearchResultDTO(String type, Long id, String title, String subtitle, String link) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.link = link;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
