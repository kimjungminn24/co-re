package com.core.api.data.repository;

import com.core.api.data.entity.RepoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoInfoRepository extends JpaRepository<RepoInfo,Long> {
}
