package in.sfp.main.service;

import in.sfp.main.model.Branch;
import java.util.List;

public interface BranchService {
    List<Branch> getAllBranches();
    Branch getBranchById(Long id);
    Branch saveBranch(Branch branch);
    void deleteBranch(Long id);
    Branch findByName(String name);
}
