
// 5. New Controller
package com.example.demo.controller;

import com.example.demo.model.History;
import com.example.demo.model.User;
import com.example.demo.service.HistoryService;
import com.example.demo.service.JwtManager;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    private JwtManager jwtManager;
    private UserService userService;

    @PostMapping("/history")
    public Optional<History> addHistory(@RequestBody Map<String, String> request) throws Exception {
        String token = request.get("token");
        String email = request.get("email");
        String content = request.get("content");
        long historyId = Long.parseLong(request.get("history_id"));

        User user = userService.getUserByEmail(email);
        History history = historyService.getByUserIdAndHistoryId(user.getId(), historyId)
                .orElseGet(() -> new History(null, "", HistoryService.generateChatName(content), user));

        String prevHistory = history.getChatHistory();
        history.setChatHistory(prevHistory + content);

        jwtManager.validateAccessToken(token);
        return historyService.save(history);
    }

    @GetMapping("/history")
    public ResponseEntity<History> getHistory(@RequestBody Map<String, String> request) throws Exception {
        String token = request.get("token");
        String email = request.get("email");
        long historyId = Long.parseLong(request.get("history_id"));

        User user = userService.getUserByEmail(email);

        jwtManager.validateAccessToken(token);
        return historyService.getByUserIdAndHistoryId(user.getId(), historyId)
                .map(ResponseEntity::ok) // If the result is present, return it with an OK status
                .orElseGet(() -> ResponseEntity.notFound().build()); // If not, return a 404 response
    }

    @GetMapping("/history/all")
    public ResponseEntity<List<History>> getAllHistory(@RequestBody Map<String, String> request) throws Exception {
        String token = request.get("token");
        String email = request.get("email");

        User user = userService.getUserByEmail(email);
        jwtManager.validateAccessToken(token);
        return historyService.getAllByUserId(user.getId())
                .filter(histories -> !histories.isEmpty()) // Ensure that the list is not empty
                .map(ResponseEntity::ok) // Return OK with the list if present and not empty
                .orElseGet(() -> ResponseEntity.notFound().build()); // Return 404 if no history found
    }
}