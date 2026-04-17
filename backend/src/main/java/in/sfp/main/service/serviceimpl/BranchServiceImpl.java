package in.sfp.main.service.serviceimpl;

import in.sfp.main.model.Branch;
import in.sfp.main.repo.BranchRepo;
import in.sfp.main.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private in.sfp.main.repo.AssetsRepo assetsRepo;

    @Autowired
    private in.sfp.main.repo.EmployeeRepo employeeRepo;

    @jakarta.annotation.PostConstruct
    public void initBranches() {
        try {
            // Get unique branch names from Assets (location field)
            List<String> assetBranches = assetsRepo.findDistinctLocationsFromRaw();
            // Get unique branch names from Employees (branchName field)
            List<String> employeeBranches = employeeRepo.findDistinctBranchNames();

            java.util.Set<String> allBranchNames = new java.util.HashSet<>();
            if (assetBranches != null) allBranchNames.addAll(assetBranches);
            if (employeeBranches != null) allBranchNames.addAll(employeeBranches);

            for (String name : allBranchNames) {
                if (name != null && !name.trim().isEmpty() && !branchRepo.existsByName(name.trim())) {
                    branchRepo.save(new Branch(name.trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize branches: " + e.getMessage());
        }
    }

    @Override
    public List<Branch> getAllBranches() {
        return branchRepo.findAll();
    }

    @Override
    public Branch getBranchById(Long id) {
        return branchRepo.findById(id).orElse(null);
    }

    @Override
    public Branch saveBranch(Branch branch) {
        if (branch.getId() == null && branchRepo.existsByName(branch.getName())) {
             throw new RuntimeException("Branch with name '" + branch.getName() + "' already exists.");
        }
        return branchRepo.save(branch);
    }

    @Override
    public void deleteBranch(Long id) {
        branchRepo.deleteById(id);
    }

    @Override
    public Branch findByName(String name) {
        return branchRepo.findByName(name).orElse(null);
    }
}
