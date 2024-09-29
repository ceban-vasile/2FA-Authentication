package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_history")
    private String chatHistory;

    @Column
    private String chatName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}