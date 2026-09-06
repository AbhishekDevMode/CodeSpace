package com.email.backend.repository;

import com.email.backend.model.Project;
import com.email.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByUser(User user);
    List<Project> findByUserId(Integer userId);
}
