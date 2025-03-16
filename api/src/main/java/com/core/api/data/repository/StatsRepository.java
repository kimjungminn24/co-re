package com.core.api.data.repository;

import com.core.api.data.entity.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<Stats,Long> {
}
