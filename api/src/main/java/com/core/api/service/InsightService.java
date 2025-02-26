package com.core.api.service;

import com.core.api.client.BackendClient;
import com.core.api.client.ChatGptClient;
import com.core.api.client.GitHubClient;
import com.core.api.data.dto.TemplateDto;
import com.core.api.data.dto.chatgpt.ChatGptDto;
import com.core.api.data.dto.response.CompareBranchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final ChatGptClient chatGptClient;
    private final BackendClient backendClient;
    private final GitHubClient gitHubClient;

    public TemplateDto generatePullRequestTemplate(String owner, String repo, String baseHead) {
        return extractTemplateContent(
                chatGptClient.makePullRequestTemplate(
                        ChatGptDto.from(
                                backendClient.getProjectInfo(owner, repo)
                                        .template(),
                                fetchBranchComparison(owner, repo, baseHead)
                        )
                )
        );
    }

    private List<CompareBranchResponseDto> fetchBranchComparison(String owner, String repo, String baseHead) {
        return CompareBranchResponseDto.from(
                gitHubClient.compareBranchHead(owner, repo, baseHead)
        );
    }

    private TemplateDto extractTemplateContent(Map<String, Object> response) {
        return ((List<Map<String, Object>>) response.get("choices"))
                .stream()
                .findFirst()
                .map(choice -> (Map<String, Object>) choice.get("message"))
                .map(message -> message.get("content")
                        .toString())
                .map(TemplateDto::new)
                .orElseGet(() -> new TemplateDto(""));
    }
}
