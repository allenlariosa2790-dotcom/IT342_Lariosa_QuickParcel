package edu.cit.lariosa.quickparcel.features.shared.repository;

import edu.cit.lariosa.quickparcel.features.shared.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}