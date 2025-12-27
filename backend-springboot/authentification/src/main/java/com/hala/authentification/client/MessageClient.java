package com.hala.authentification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "messaging", url = "http://localhost:8050/api/v1/messages")
public interface MessageClient {
    @GetMapping("/allMessages")
    List<MessageDTO> findAllMessageByUser(@PathVariable("user-id") Integer userId);
}