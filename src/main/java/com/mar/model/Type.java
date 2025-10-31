package com.mar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "type")
public class Type implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String title;

    private String description;

    private Long version;

    @OneToMany(mappedBy = "type")
    private Set<Question> questions;

    @Override
    public String toString() {
        return "Type{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", version=" + version +
                ", questions.size=" + (questions == null ? 0 : questions.size()) +
                '}';
    }
}
