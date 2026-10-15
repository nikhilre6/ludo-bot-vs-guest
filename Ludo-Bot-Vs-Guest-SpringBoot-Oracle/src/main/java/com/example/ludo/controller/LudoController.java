package com.example.ludo.controller;

import com.example.ludo.model.Game;
import com.example.ludo.model.MoveRequest;
import com.example.ludo.service.LudoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ludo")
@CrossOrigin
public class LudoController {
    private final LudoService service;
    public LudoController(LudoService service){this.service=service;}

    @PostMapping("/games")
    public Game newGame(){return service.newGame();}

    @GetMapping("/games/{id}")
    public Game get(@PathVariable String id){return service.getGame(id);}

    @PostMapping("/games/{id}/roll")
    public Game roll(@PathVariable String id){return service.roll(id);}

    @PostMapping("/games/{id}/move")
    public Game move(@PathVariable String id,@RequestBody MoveRequest request){
        return service.moveGuest(id,request.getTokenIndex());
    }

    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class})
    public ResponseEntity<String> error(RuntimeException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
