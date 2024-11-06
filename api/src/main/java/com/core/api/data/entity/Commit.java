package com.core.api.data.entity;


import com.core.api.data.dto.github.CommitServerDto;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Commit extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commit_id", nullable = false)
    private Long id;

    @Column(name = "commit_sha", nullable = false)
    private String sha;

    @Column(name = "commit_message", length = 2000)
    private String message;

    @ManyToOne
    @JoinColumn(name = "pr_id", nullable = false)
    private PullRequest pullRequest;

    @Column(name = "commit_parent")
    private String parent;

    @Column(name = "commit_second_parent")
    private String secondParent;

    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public static Commit from(CommitServerDto commitServerDto, PullRequest pullRequest) {
        Commit commit = new Commit();
        commit.sha = commitServerDto.sha();
        commit.message = commitServerDto.message();
        commit.pullRequest = pullRequest;
        commit.parent = commitServerDto.parent();
        commit.secondParent = commitServerDto.secondParent();
        return commit;
    }

}
