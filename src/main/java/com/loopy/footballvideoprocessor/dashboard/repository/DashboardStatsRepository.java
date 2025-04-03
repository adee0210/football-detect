package com.loopy.footballvideoprocessor.dashboard.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopy.footballvideoprocessor.dashboard.model.DashboardStats;
import com.loopy.footballvideoprocessor.user.model.User;

@Repository
public interface DashboardStatsRepository extends JpaRepository<DashboardStats, UUID> {

    Optional<DashboardStats> findByUser(User user);

    boolean existsByUser(User user);
}