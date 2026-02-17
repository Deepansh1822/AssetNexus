package in.sfp.main.service;

import java.util.List;
import in.sfp.main.model.Asset;

public interface AssetsService {
    List<Asset> getAllAssets();

    Asset getAssetById(Long id);

    Asset saveAsset(Asset asset);

    List<Asset> saveAllAssets(List<Asset> assets);

    void deleteAsset(Long id);

    Asset assignAsset(Long assetId, Long employeeId, String notes);

    Asset returnAsset(Long assetId, String notes);

    Asset disposeAsset(Long assetId, String reason);
}
