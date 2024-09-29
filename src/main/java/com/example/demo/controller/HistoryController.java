
// 5. New Controller
package com.example.demo.controller;

import com.example.demo.dto.HistoryDTO;
import com.example.demo.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/thef/{id}")
    public ResponseEntity<HistoryDTO> getHistory(@PathVariable Long id) throws Exception {
        return historyService.history(id)
                .map(history -> ResponseEntity.ok(new HistoryDTO(history)))
                .orElse(ResponseEntity.notFound().build());
    }
}