package com.core.api.client;

import com.core.api.data.dto.response.BranchResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "git", url = "http://localhost:8080")
public interface GitClient {

    @GetMapping("/branch/{owner}/{repo}")
    List<BranchResponseDto> getBranches(@PathVariable("owner") String owner, @PathVariable("repo") String repo);
}
