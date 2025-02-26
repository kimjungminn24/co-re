package com.core.api.service;

import com.core.api.data.dto.DayDto;
import com.core.api.data.dto.StatsDto;
import com.core.api.data.dto.StatsVersionDto;
import com.core.api.data.entity.Commit;
import com.core.api.data.entity.PullRequest;
import com.core.api.data.entity.Review;
import com.core.api.data.entity.Version;
import com.core.api.data.repository.PullRequestRepository;
import com.core.api.data.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PullRequestRepository pullRequestRepository;
    private final VersionRepository versionRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd");

    public StatsDto getStats(String owner, String repo) {
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
                pullRequests.stream()
                        .filter(PullRequest::getAfterReview)
                        .toList(),
                PullRequest::getDeadline, startOfThisWeek, startOfThisWeek.plusWeeks(1));
        int lastWeekHotfix = countItemsWithinPeriod(
                pullRequests.stream()
                        .filter(PullRequest::getAfterReview)
                        .toList(),
                PullRequest::getDeadline, startOfLastWeek, startOfThisWeek);
        double hotfixGrowthRate = calculateGrowthRate(currentWeekHotfix, lastWeekHotfix);

        return StatsDto.builder()
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
                .build();
    }

    public StatsVersionDto getVersionStats(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
        List<PullRequest> pullRequests = pullRequestRepository.findAllByVersion(version)
                .orElse(List.of());

        List<DayDto> prCounts = getCountsByDate(pullRequests, PullRequest::getCreatedDate);
        List<DayDto> reviewCounts = getCountsByDate(
                pullRequests.stream()
                        .flatMap(pr -> pr.getReviewers()
                                .stream())
                        .flatMap(reviewer -> reviewer.getReviews()
                                .stream())
                        .toList(),
                Review::getCreatedDate
        );
        List<DayDto> commitCounts = getCountsByDate(
                pullRequests.stream()
                        .flatMap(pr -> pr.getCommits()
                                .stream())
                        .toList(),
                Commit::getCreatedDate
        );

        return new StatsVersionDto(commitCounts, prCounts, reviewCounts);
    }

    private LocalDateTime getStartOfThisWeek() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault())
                        .getFirstDayOfWeek()))
                .toLocalDate()
                .atStartOfDay();
    }

    private <T> int countItemsWithinPeriod(List<T> items, Function<T, LocalDateTime> dateExtractor, LocalDateTime start, LocalDateTime end) {
        return (int) items.stream()
                .filter(item -> isWithinPeriod(dateExtractor.apply(item), start, end))
                .count();
    }

    private int countCommitsWithinPeriod(List<PullRequest> pullRequests, LocalDateTime start, LocalDateTime end) {
        return pullRequests.stream()
                .flatMap(pr -> pr.getCommits()
                        .stream())
                .filter(commit -> isWithinPeriod(commit.getCreatedDate(), start, end))
                .toList()
                .size();
    }

    private int countReviewsWithinPeriod(List<PullRequest> pullRequests, LocalDateTime start, LocalDateTime end) {
        return pullRequests.stream()
                .flatMap(pr -> pr.getReviewers()
                        .stream())
                .flatMap(reviewer -> reviewer.getReviews()
                        .stream())
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

    private <T> List<DayDto> getCountsByDate(List<T> items, Function<T, LocalDateTime> dateExtractor) {
        Map<String, Integer> countMap = items.stream()
                .collect(Collectors.groupingBy(
                        item -> dateExtractor.apply(item)
                                .format(DATE_FORMATTER),
                        Collectors.summingInt(i -> 1)
                ));

        return countMap.entrySet()
                .stream()
                .map(entry -> new DayDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}

