package com.core.api.scheduler;

import com.core.api.data.dto.StatsDto;
import com.core.api.data.entity.PullRequest;
import com.core.api.data.entity.RepoInfo;
import com.core.api.data.entity.Stats;
import com.core.api.data.repository.PullRequestRepository;
import com.core.api.data.repository.RepoInfoRepository;
import com.core.api.data.repository.StatsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsScheduledTask {


    private final PullRequestRepository pullRequestRepository;
    private final StatsRepository statsRepository;
    private final RepoInfoRepository repoInfoRepository;


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    @SchedulerLock(name = "scheduler_stats_task")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000)
    )
    public void dailyStatsForAllRepositories() {
        log.info("[통계 집계 시작]");
        List<RepoInfo> repositories = repoInfoRepository.findAll();
            for (RepoInfo repository : repositories) {
                dailyStats(repository.getOwner(), repository.getName());
            }

        log.info("[통계 집계 끝]");
    }

    @Recover
    public void recover(Exception e) {
        log.error("[통계 집계 실패] 최대 재시도(2회) 후에도 실패 : {}",  e.getMessage());
    }

    public void  dailyStats(String owner, String repo) {
        List<PullRequest> pullRequests = pullRequestRepository.findAllByOwnerAndRepo(owner, repo)
                .orElse(List.of());

        int totalPullRequest = pullRequests.size();

        LocalDateTime startOfThisWeek = getStartOfThisWeek();
        LocalDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);

        int currentWeekPullRequest = countItemsWithinPeriod(pullRequests, PullRequest::getDeadline, startOfThisWeek, startOfThisWeek.plusWeeks(1));
        int lastWeekPullRequest = countItemsWithinPeriod(pullRequests, PullRequest::getDeadline, startOfLastWeek, startOfThisWeek);
        double pullRequestGrowthRate = calculateGrowthRate(currentWeekPullRequest, lastWeekPullRequest);

        int totalCommit = pullRequests.stream()
                .mapToInt(pr -> pr.getCommits()
                        .size())
                .sum();
        int totalReview = pullRequests.stream()
                .flatMap(pr -> pr.getReviewers()
                        .stream())
                .mapToInt(reviewer -> reviewer.getReviews()
                        .size())
                .sum();
        int totalHotfix = (int) pullRequests.stream()
                .filter(PullRequest::getAfterReview)
                .count();

        int currentWeekCommit = countCommitsWithinPeriod(pullRequests, startOfThisWeek, startOfThisWeek.plusWeeks(1));
        int lastWeekCommit = countCommitsWithinPeriod(pullRequests, startOfLastWeek, startOfThisWeek);
        double commitGrowthRate = calculateGrowthRate(currentWeekCommit, lastWeekCommit);

        int currentWeekReview = countReviewsWithinPeriod(pullRequests, startOfThisWeek, startOfThisWeek.plusWeeks(1));
        int lastWeekReview = countReviewsWithinPeriod(pullRequests, startOfLastWeek, startOfThisWeek);
        double reviewGrowthRate = calculateGrowthRate(currentWeekReview, lastWeekReview);

        int currentWeekHotfix = countItemsWithinPeriod(
                pullRequests.parallelStream()
                        .filter(PullRequest::getAfterReview)
                        .toList(),
                PullRequest::getDeadline, startOfThisWeek, startOfThisWeek.plusWeeks(1));
        int lastWeekHotfix = countItemsWithinPeriod(
                pullRequests.parallelStream()
                        .filter(PullRequest::getAfterReview)
                        .toList(),
                PullRequest::getDeadline, startOfLastWeek, startOfThisWeek);
        double hotfixGrowthRate = calculateGrowthRate(currentWeekHotfix, lastWeekHotfix);

        Stats newStats = Stats.createStats(
                StatsDto.builder()
                .totalCommit(totalCommit)
                .currentWeekCommit(currentWeekCommit)
                .lastWeekCommit(lastWeekCommit)
                .commitGrowthRate(commitGrowthRate)
                .totalPullRequest(totalPullRequest)
                .currentWeekPullRequest(currentWeekPullRequest)
                .lastWeekPullRequest(lastWeekPullRequest)
                .pullRequestGrowthRate(pullRequestGrowthRate)
                .totalReview(totalReview)
                .currentWeekReview(currentWeekReview)
                .lastWeekReview(lastWeekReview)
                .reviewGrowthRate(reviewGrowthRate)
                .totalHotfix(totalHotfix)
                .currentWeekHotfix(currentWeekHotfix)
                .lastWeekHotfix(lastWeekHotfix)
                .hotfixGrowthRate(hotfixGrowthRate)
                .build());
        statsRepository.save(newStats);
    }


    private LocalDateTime getStartOfThisWeek() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault())
                        .getFirstDayOfWeek()))
                .toLocalDate()
                .atStartOfDay();
    }

    private <T> int countItemsWithinPeriod(List<T> items, Function<T, LocalDateTime> dateExtractor, LocalDateTime start, LocalDateTime end) {
        return (int) items.parallelStream()
                .filter(item -> isWithinPeriod(dateExtractor.apply(item), start, end))
                .count();
    }

    private int countCommitsWithinPeriod(List<PullRequest> pullRequests, LocalDateTime start, LocalDateTime end) {
        return pullRequests.parallelStream()
                .flatMap(pr -> pr.getCommits()
                        .stream())
                .filter(commit -> isWithinPeriod(commit.getCreatedDate(), start, end))
                .toList()
                .size();
    }

    private int countReviewsWithinPeriod(List<PullRequest> pullRequests, LocalDateTime start, LocalDateTime end) {
        return pullRequests.parallelStream()
                .flatMap(pr -> pr.getReviewers()
                        .parallelStream())
                .flatMap(reviewer -> reviewer.getReviews()
                        .parallelStream())
                .filter(review -> isWithinPeriod(review.getCreatedDate(), start, end))
                .toList()
                .size();
    }

    private boolean isWithinPeriod(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date != null && (date.isEqual(start) || date.isAfter(start)) && date.isBefore(end);
    }

    private double calculateGrowthRate(int current, int last) {
        if (last == 0) return current > 0 ? 100.0 : 0;
        return ((double) (current - last) / last) * 100;
    }




}
