package com.hala.authentification.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageDTO {
    private Integer id;
    private Date date_pub;
    private String name;
    private Integer userId;
    private List<MessageDTO> messages;


}