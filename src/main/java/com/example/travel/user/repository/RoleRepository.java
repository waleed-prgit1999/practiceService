package com.example.travel.user.repository;

import com.example.travel.user.entity.Role;
import com.example.travel.user.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
