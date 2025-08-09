package thonlivethondie.artconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import thonlivethondie.artconnect.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByUserId(Long userId);

    boolean existsByStoreName(String storeName);
    boolean existsByPhoneNumber(String phoneNumber);
}
