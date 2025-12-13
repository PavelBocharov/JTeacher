package com.mar.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionInfo implements Serializable {

    private String id;
    private Long position;
    private String question;
    @JsonProperty("question_img_path")
    private String questionImgPath;
    @JsonProperty("answer_img_path")
    private String answerImgPath;
    private List<String> options;
    @JsonProperty("correct_answer")
    private String correctAnswer;
    @JsonProperty("detailed_answer")
    private String detailedAnswer;

    @Override
    public String toString() {
        return "QuestionInfo{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", question='" + question + '\'' +
                ", questionImgPath='" + questionImgPath + '\'' +
                ", answerImgPath='" + answerImgPath + '\'' +
                ", options=" + options +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", detailedAnswer.length='" + detailedAnswer.length() + '\'' +
                '}';
    }
}
