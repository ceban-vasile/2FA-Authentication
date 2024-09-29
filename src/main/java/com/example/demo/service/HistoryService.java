package com.example.demo.service;

import com.example.demo.repository.HistoryRepository;

import com.example.demo.model.History;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;


    public Optional<History> history(Long id) throws Exception {
        return historyRepository.findById(id);
    }
}
