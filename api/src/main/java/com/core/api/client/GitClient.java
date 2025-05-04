package com.core.api.client;

import com.core.api.data.dto.FileDto;
import com.core.api.data.dto.response.BranchResponseDto;
import com.core.api.data.dto.response.CompareBranchResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "git", url = "http://localhost:8080")
public interface GitClient {

    @GetMapping("/branch/{owner}/{repo}")
    List<BranchResponseDto> getBranches(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo
    );

    @GetMapping("/branch/{owner}/{repo}/compare/{base}/{head}")
    List<CompareBranchResponseDto> compareBranchHead(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("base") String base,
            @PathVariable("head") String head
    );

    @GetMapping("/branch/{owner}/{repo}/compare/{base}/{head}/file")
    List<FileDto> getChangeFiles(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("base") String base,
            @PathVariable("head") String head
    );


}
