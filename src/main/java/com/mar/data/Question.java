package com.mar.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Question implements Serializable {

    private String id;
    private Long position;
    private String question;
    private List<String> options;
    @JsonProperty("correct_answer")
    private String correctAnswer;
    @JsonProperty("detailed_answer")
    private String detailedAnswer;

}
