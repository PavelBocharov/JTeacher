package com.mar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question")
public class Question implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String question;

    private List<String> options;

    private String correctAnswer;

    @Lob
    private String detailedAnswer;

    private Long position;

    @ManyToOne
    @JoinColumn(name="type_id", nullable=false)
    private Type type;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Question question1 = (Question) o;
        return Objects.equals(question, question1.question) && Objects.equals(type, question1.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(question);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }
}
