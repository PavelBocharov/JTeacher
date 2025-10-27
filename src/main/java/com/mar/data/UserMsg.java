package com.mar.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMsg {
    private Long id;
    private String username;
    private Long chatId;
    private String text;
    private Integer msgId;
}
