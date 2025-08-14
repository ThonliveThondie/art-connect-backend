package thonlivethondie.artconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import thonlivethondie.artconnect.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByUserId(Long userId);

    // StoreImage를 함께 조회하는 Fetch Join 쿼리
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.storeImages WHERE s.user.id = :userId")
    Optional<Store> findByUserIdWithImages(@Param("userId") Long userId);

    boolean existsByStoreName(String storeName);
    boolean existsByPhoneNumber(String phoneNumber);
}
