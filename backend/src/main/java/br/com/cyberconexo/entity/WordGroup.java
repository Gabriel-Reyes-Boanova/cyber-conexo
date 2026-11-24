package br.com.cyberconexo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Um grupo de 4 palavras com categoria e explicação (o "gabarito"). */
@Entity
@Table(name = "word_group")
public class WordGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    private Puzzle puzzle;

    @Column(nullable = false, length = 120)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    /** 1 (mais fácil) a 4 (mais difícil). */
    @Column(name = "level", nullable = false)
    private int level = 1;

    @OneToMany(mappedBy = "wordGroup", fetch = FetchType.LAZY)
    private List<Word> words = new ArrayList<>();

    protected WordGroup() { }

    public Long getId() { return id; }
    public Puzzle getPuzzle() { return puzzle; }
    public String getCategory() { return category; }
    public String getExplanation() { return explanation; }
    public int getLevel() { return level; }
    public List<Word> getWords() { return words; }
}
