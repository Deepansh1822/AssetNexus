package in.sfp.main.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import in.sfp.main.model.Category;
import in.sfp.main.repo.CategoryRepo;
import in.sfp.main.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepo.findByActiveTrue();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id).orElse(null);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Autowired
    private in.sfp.main.repo.AssetsRepo assetsRepo;

    @Override
    public void deactivateCategory(Long id) {
        Category category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            // Check if any assets in this category are IN_USE or UNDER_MAINTENANCE
            long activeAssetsCount = assetsRepo.countByCategoryIdAndStatusIn(id,
                    java.util.Arrays.asList(in.sfp.main.enums.Status.IN_USE,
                            in.sfp.main.enums.Status.UNDER_MAINTENANCE));

            if (activeAssetsCount > 0) {
                throw new IllegalStateException("Cannot deactivate category: " + activeAssetsCount +
                        " assets are currently 'In Use' or 'Under Maintenance'. Please reassign or dispose of them first.");
            }

            category.setActive(false);
            categoryRepo.save(category);
        }
    }

    @Override
    public void reactivateCategory(Long id) {
        Category category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            category.setActive(true);
            categoryRepo.save(category);
        }
    }
}
