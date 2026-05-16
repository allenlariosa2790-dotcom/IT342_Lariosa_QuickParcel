package edu.cit.lariosa.quickparcel.features.auth.repository;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}