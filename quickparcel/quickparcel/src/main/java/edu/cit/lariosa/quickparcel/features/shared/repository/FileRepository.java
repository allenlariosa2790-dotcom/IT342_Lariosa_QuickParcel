package edu.cit.lariosa.quickparcel.features.shared.repository;

import edu.cit.lariosa.quickparcel.features.shared.entity.File;
import edu.cit.lariosa.quickparcel.features.shared.entity.Parcel;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByParcel(Parcel parcel);
    Optional<File> findByUserAndIsProfilePictureTrue(User user);
    List<File> findByUser(User user);
}