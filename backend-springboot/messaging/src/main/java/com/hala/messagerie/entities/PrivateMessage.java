package com.hala.messagerie.entities;

import com.hala.messagerie.client.UserDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "PrivateMessage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String contenu;
    private Date dateMsg;

    private Integer senderId;
    private Integer recipientId;

    @Column(nullable = false)
    private boolean consulter;
}
