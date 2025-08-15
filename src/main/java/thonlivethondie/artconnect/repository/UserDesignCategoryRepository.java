package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.UserDesignCategory;

public interface UserDesignCategoryRepository extends JpaRepository<UserDesignCategory, Long> {

  @Modifying
  @Query("DELETE FROM UserDesignCategory udc WHERE udc.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
