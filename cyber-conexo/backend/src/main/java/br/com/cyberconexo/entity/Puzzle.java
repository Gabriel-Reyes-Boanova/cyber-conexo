package br.com.cyberconexo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Um tabuleiro: 16 palavras divididas em 4 grupos. */
@Entity
@Table(name = "puzzle")
public class Puzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "puzzle", fetch = FetchType.LAZY)
    private List<WordGroup> groups = new ArrayList<>();

    protected Puzzle() { }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Topic getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<WordGroup> getGroups() { return groups; }
}
