package com.core.api.service;


import com.core.api.client.GitClient;
import com.core.api.data.dto.FileDto;
import com.core.api.data.dto.response.BranchResponseDto;
import com.core.api.data.dto.response.CompareBranchResponseDto;
import com.core.api.utils.DecodingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {


    private final GitClient gitClient;

    public List<BranchResponseDto> getBranches(String owner, String repo) {
        return gitClient.getBranches(owner, repo);
    }

    public List<CompareBranchResponseDto> compareBranchHead(
            String owner,
            String repo,
            String base,
            String head
    ) {
        return gitClient.compareBranchHead(owner, repo, base, head);
    }

    public List<FileDto> getChangeFiles(String owner, String repo, String base, String head) {
        List<FileDto> files = gitClient.getChangeFiles(owner, repo, base, head);
        return files.stream()
                .peek(file -> file.setContent(DecodingUtils.decodeBase64(file.getContent())))
                .collect(Collectors.toList());
    }


}
