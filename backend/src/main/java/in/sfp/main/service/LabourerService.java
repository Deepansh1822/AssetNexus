package in.sfp.main.service;

import in.sfp.main.model.Labourer;
import in.sfp.main.repo.LabourerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LabourerService {

    @Autowired
    private LabourerRepository repository;

    public List<Labourer> getAllLabourers() {
        return repository.findAll();
    }

    public Labourer registerLabourer(Labourer labourer) {
        // Auto-generate ID if empty
        if (labourer.getPersonnelId() == null || labourer.getPersonnelId().isEmpty()) {
            labourer.setPersonnelId("LAB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        
        // Ensure starting status is AVAILABLE
        labourer.setStatus("AVAILABLE");
        labourer.setCurrentSite(null);
        
        return repository.save(labourer);
    }

    public Optional<Labourer> findById(Long id) {
        return repository.findById(id);
    }
    
    public List<Labourer> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    public Labourer updateAssignment(Long labourerId, String siteName, String status) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        labourer.setCurrentSite(siteName);
        labourer.setStatus(status);
        
        return repository.save(labourer);
    }

    public Labourer unassign(Long labourerId) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        labourer.setCurrentSite(null);
        labourer.setStatus("AVAILABLE");
        
        return repository.save(labourer);
    }

    public Labourer dispose(Long labourerId) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        labourer.setCurrentSite(null);
        labourer.setStatus("DISPOSED");
        
        return repository.save(labourer);
    }
}
