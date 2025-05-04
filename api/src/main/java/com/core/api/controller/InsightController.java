package com.core.api.controller;

import com.core.api.data.dto.TemplateDto;
import com.core.api.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insight")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/{owner}/{repo}/{baseHead}")
    public ResponseEntity<TemplateDto> makePullRequestTemplate(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String base,
            @PathVariable String head
    ) {
        TemplateDto template = insightService.generatePullRequestTemplate(owner, repo, base, head);
        return ResponseEntity.ok(template);
    }
}
