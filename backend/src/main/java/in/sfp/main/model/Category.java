package in.sfp.main.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Lob
    @Column(name = "category_image", columnDefinition = "LONGBLOB")
    private byte[] categoryImage;

    private boolean hasImage;

    @com.fasterxml.jackson.annotation.JsonProperty("hasImage")
    public boolean isHasImage() {
        return categoryImage != null && categoryImage.length > 0;
    }

    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Asset> assets;

    public Category() {
    }

    public Category(Long id, String name, String description, byte[] categoryImage, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryImage = categoryImage;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(byte[] categoryImage) {
        this.categoryImage = categoryImage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }
}
