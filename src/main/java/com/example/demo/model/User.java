package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String secret;

    public User() {
    }

    public Long getId(){;
        return id;
    }

    public String getEmail(){;
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(){;
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecret(){;
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
}