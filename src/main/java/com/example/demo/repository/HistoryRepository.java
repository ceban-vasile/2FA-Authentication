package com.example.demo.repository;

import com.example.demo.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.User;

import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
}