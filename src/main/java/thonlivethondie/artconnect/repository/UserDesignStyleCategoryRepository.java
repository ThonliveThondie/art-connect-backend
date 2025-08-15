package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.UserDesignStyleCategory;

public interface UserDesignStyleCategoryRepository extends JpaRepository<UserDesignStyleCategory, Long> {

  @Modifying
  @Query("DELETE FROM UserDesignStyleCategory udsc WHERE udsc.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
