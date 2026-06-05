package com.fluxo.user.repository;

import com.fluxo.user.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Integer> {

    Optional<StudentProfile> findByStudentUserId(Integer userId);

    Optional<StudentProfile> findByStudentUserIdAndTeamProjectId(
            Integer userId,
            Integer projectId
    );

    List<StudentProfile> findByTeamProjectId(Integer projectId);

    List<StudentProfile> findByStudentUserIdIn(Collection<Integer> userIds);
}