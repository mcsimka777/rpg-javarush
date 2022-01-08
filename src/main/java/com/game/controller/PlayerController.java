package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(path="/rest/players")
    public @ResponseBody List<Player> getALLPlayers(@RequestParam Map<String,String> params) {
        return playerService.getAllPlayers(params);
    }

    @GetMapping(path="/rest/players/count")
    public @ResponseBody Long countPlayers(@RequestParam Map<String,String> params) {
        return playerService.countPlayers(params);
    }

    @GetMapping(path="/rest/players/{id}")
    public @ResponseBody Player getPlayer(@PathVariable String id) {
        return playerService.getPlayer(id);
    }

    @DeleteMapping(path="/rest/players/{id}")
    public @ResponseBody void deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(id);
    }

    @PostMapping(path="/rest/players")
    public @ResponseBody Player createPlayer(@RequestBody Map<String, String> params){
        return playerService.createPlayer(params);
    }

    @PostMapping(path="/rest/players/{id}")
    public @ResponseBody Player modifyPlayer(@RequestBody Map<String, String> params, @PathVariable String id) {
        return playerService.modifyPlayer(params,id);
    }

}
