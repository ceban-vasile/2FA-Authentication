package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    public enum UserType {
        HOME, BUSINESS
    }

    public enum CustomerCategory {
        STANDARD, PREMIUM, VIP
    }

    public enum SubscriptionType {
        BASIC, STANDARD, PREMIUM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false, unique = true)
    private String idnp;  // National Identification Number

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;  // Enum to define Home/Business

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "communication_language", nullable = false)
    private String communicationLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_category", nullable = false)
    private CustomerCategory customerCategory;  // Enum for Customer Category

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String connection;  // Physical connection address

    @Column(name = "region_branch", nullable = false)
    private String regionBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;  // Enum for Subscription Type

    @Column(name = "connected_equipment_type", nullable = false)
    private String connectedEquipmentType;

    @Column(length = 1024)
    private String chat_history;
}
