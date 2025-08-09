package thonlivethondie.artconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import thonlivethondie.artconnect.entity.StoreImage;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    List<StoreImage> findByStoreIdOrderByImageOrderAsc(Long storeId);

    Optional<StoreImage> findByStoreIdAndIsMainTrue(Long storeId);

    long countByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}
