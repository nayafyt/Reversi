package com.aueb.reversi.controller;

import com.aueb.reversi.dto.GameState;
import com.aueb.reversi.dto.MoveRequest;
import com.aueb.reversi.dto.NewGameRequest;
import com.aueb.reversi.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    public GameState newGame(@RequestBody NewGameRequest request) {
        return gameService.newGame(request.getPlayerColor(), request.getLevel());
    }

    @PostMapping("/move")
    public GameState makeMove(@RequestBody MoveRequest request) {
        return gameService.playerMove(request.getRow(), request.getCol());
    }

    @GetMapping("/state")
    public GameState getState() {
        return gameService.getState();
    }
}
