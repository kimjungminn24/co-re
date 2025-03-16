package com.core.api.data.entity;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RepoInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_name")
    private String name;

    @Column(name = "repo_owner")
    private String owner;

}
