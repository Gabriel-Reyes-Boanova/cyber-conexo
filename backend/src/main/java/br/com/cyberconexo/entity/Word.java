package br.com.cyberconexo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_group_id", nullable = false)
    private WordGroup wordGroup;

    @Column(nullable = false, length = 60)
    private String value;

    protected Word() { }

    public Long getId() { return id; }
    public WordGroup getWordGroup() { return wordGroup; }
    public String getValue() { return value; }
}
