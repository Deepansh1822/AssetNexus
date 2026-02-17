package in.sfp.main.service;

import java.util.List;
import in.sfp.main.model.Category;

public interface CategoryService {
    List<Category> getAllCategories();

    List<Category> getActiveCategories();

    Category getCategoryById(Long id);

    Category saveCategory(Category category);

    void deactivateCategory(Long id);

    void reactivateCategory(Long id);
}
