package com.mar.service;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Question implements Serializable {

    private Long id;
    private List<QuestionValue> values;
    private QuestionLevel level;
    private String imgPath;
    private String fullAnswer;

}
