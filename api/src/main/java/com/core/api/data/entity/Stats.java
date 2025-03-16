package com.core.api.data.entity;

import com.core.api.data.dto.StatsDto;
import com.core.api.data.dto.github.PullRequestServerDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;
    private String repo;

    private int totalCommit;
    private int currentWeekCommit;
    private int lastWeekCommit;
    private double commitGrowthRate;

    private int totalPullRequest;
    private int currentWeekPullRequest;
    private int lastWeekPullRequest;
    private double pullRequestGrowthRate;

    private int totalReview;
    private int currentWeekReview;
    private int lastWeekReview;
    private double reviewGrowthRate;

    private int totalHotfix;
    private int currentWeekHotfix;
    private int lastWeekHotfix;
    private double hotfixGrowthRate;

    public static Stats createStats(StatsDto dto) {
        Stats stats = new Stats();

        stats.totalCommit = dto.getTotalCommit();
        stats.currentWeekCommit = dto.getCurrentWeekCommit();
        stats.lastWeekCommit = dto.getLastWeekCommit();
        stats.commitGrowthRate = dto.getCommitGrowthRate();

        stats.totalPullRequest = dto.getTotalPullRequest();
        stats.currentWeekPullRequest = dto.getCurrentWeekPullRequest();
        stats.lastWeekPullRequest = dto.getLastWeekPullRequest();
        stats.pullRequestGrowthRate = dto.getPullRequestGrowthRate();

        stats.totalReview = dto.getTotalReview();
        stats.currentWeekReview = dto.getCurrentWeekReview();
        stats.lastWeekReview = dto.getLastWeekReview();
        stats.reviewGrowthRate = dto.getReviewGrowthRate();

        stats.totalHotfix = dto.getTotalHotfix();
        stats.currentWeekHotfix = dto.getCurrentWeekHotfix();
        stats.lastWeekHotfix = dto.getLastWeekHotfix();
        stats.hotfixGrowthRate = dto.getHotfixGrowthRate();

        return stats;
    }


}
