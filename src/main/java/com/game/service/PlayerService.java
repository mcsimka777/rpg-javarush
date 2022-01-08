package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.controller.PlayerPageRequest;
import com.game.controller.Restrictions;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.NotValidValueException;
import com.game.exception.PlayerNotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getAllPlayers(Map<String, String> params) {
        return playerRepository.findAll(createSpecification(params), createPageRequest(params)).toList();
    }

    public Player getPlayer(String id) {
        if (!isValidId(id)) NotValidValue();
        Optional<Player> player = playerRepository.findById(Long.parseLong(id));
        if (!player.isPresent()) PlayerNotFound();
        return player.get();
    }

    public void deletePlayer(String id) {
        Player player = getPlayer(id);
        playerRepository.delete(player);
    }

    public Player createPlayer(Map<String, String> params){
        if (!fieldsIsValid(params,true)) NotValidValue();
        boolean banned = generateStatus(params);
        Player newPlayer = new Player();
        newPlayer.setName(params.get("name"));
        newPlayer.setTitle(params.get("title"));
        newPlayer.setRace(Race.valueOf(params.get("race")));
        newPlayer.setProfession(Profession.valueOf(params.get("profession")));
        newPlayer.setBirthday(new Date(Long.parseLong(params.get("birthday"))));
        newPlayer.setBanned(banned);
        Integer exp = Integer.parseInt(params.get("experience"));
        newPlayer.setExperience(exp);
        newPlayer.setLevel(calculateLevel(newPlayer));
        newPlayer.setUntilNextLevel(calculateUntilNextLevel(newPlayer));
        playerRepository.save(newPlayer);
        return newPlayer;
    }

    public Player modifyPlayer(Map<String, String> params, String id) {
        Player player = getPlayer(id);
        if (!params.isEmpty()) {
            if (!fieldsIsValid(params, false)) NotValidValue();
            for (String key : params.keySet()) {
                switch (key.toLowerCase()) {
                    case "name":
                        player.setName(params.get("name"));
                        break;
                    case "title":
                        player.setTitle(params.get("title"));
                        break;
                    case "race":
                        player.setRace(Race.valueOf(params.get("race")));
                        break;
                    case "profession":
                        player.setProfession(Profession.valueOf(params.get("profession")));
                        break;
                    case "birthday":
                        player.setBirthday(new Date(Long.parseLong(params.get("birthday"))));
                        break;
                    case "banned":
                        player.setBanned("true".equals(params.get(key)));
                        break;
                    case "experience": {
                        Integer exp = Integer.parseInt(params.get("experience"));
                        player.setExperience(exp);
                        player.setLevel(calculateLevel(player));
                        player.setUntilNextLevel(calculateUntilNextLevel(player));
                        break;
                    }
                }
            }
        }
        playerRepository.save(player);
        return player;
    }

    public Long countPlayers(Map<String,String> params) {
        return playerRepository.count(createSpecification(params));
    }

    private static boolean generateStatus(Map<String, String> params) {
        return !params.containsKey("banned") || "true".equals(params.get("banned"));
    }

    private boolean fieldsIsValid(Map<String, String> params, boolean create) {
        if (create && !params.keySet().containsAll(Restrictions.REQUIRED_FIELDS)) return false;
        for (String key : params.keySet()) {
            switch (key.toLowerCase()) {
                case "name":
                    // Проверить наличие null
                    if (params.get("name") == null || params.get("name").length() == 0 || params.get("name").length()>Restrictions.NAME_MAX_LENGTH) return false;
                    break;
                case "title":
                    if (params.get("title") == null || params.get("title").length() == 0 || params.get("title").length()>Restrictions.TITLE_MAX_LENGTH) return false;
                    break;
                case "experience":
                    Long exp = getLongFromString(params.get(key));
                    if (exp < Restrictions.EXP_MIN_VALUE || exp > Restrictions.EXP_MAX_VALUE) return false;
                    break;
                case "birthday":
                    Long birthday = getLongFromString(params.get(key));
                    if (birthday<Restrictions.MIN_DATE || birthday>Restrictions.MAX_DATE) return false;
                    break;
            }
        }
        return true;
    }

    private Long getLongFromString(String s) {
        long value = 0L;
        try {
            value = Long.parseLong(s);
        }
        catch (Exception e) {
            NotValidValue();
        }
        return value;
    }

    private Integer calculateLevel(Player player) {
        return (int)((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
    }

    private Integer calculateUntilNextLevel(Player player) {
        return 50*(player.getLevel()+1)*(player.getLevel()+2)-player.getExperience();
    }

    private void PlayerNotFound() {
        throw new PlayerNotFoundException();
    }

    private void NotValidValue() {
        throw new NotValidValueException();
    }

    private boolean isValidId(String id) {
        try {
            if (Long.parseLong(id) < 1) return false;
        }
        catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    private Pageable createPageRequest(Map<String,String> params) {
        int pageNumber = params.containsKey("pageNumber") ? Integer.parseInt(params.get("pageNumber")) : PlayerPageRequest.DEFAULT_PAGE_NUMBER;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize")) : PlayerPageRequest.DEFAULT_PAGE_SIZE;
        String sort = params.containsKey("order") ? PlayerOrder.valueOf(params.get("order")).getFieldName() : PlayerPageRequest.DEFAULT_SORT_BY;
        return PageRequest.of(pageNumber,pageSize, Sort.by(sort));
    }

    private Specification<Player> createSpecification(Map<String, String> params) {
        return (root, query, criteriaBuilder) -> {
            Predicate mainP = criteriaBuilder.conjunction();
            for (String key: params.keySet()) {
                Predicate p = null;
                switch (key) {
                    case "profession":
                        p = criteriaBuilder.equal(root.get(key.toLowerCase(Locale.ROOT)), Profession.valueOf(params.get(key)));
                        break;
                    case "race":
                        p = criteriaBuilder.equal(root.get(key.toLowerCase(Locale.ROOT)), Race.valueOf(params.get(key)));
                        break;
                    case "minExperience":
                        p = criteriaBuilder.greaterThanOrEqualTo(root.get("experience"),Integer.parseInt(params.get(key)));
                        break;
                    case "maxExperience":
                        p = criteriaBuilder.lessThanOrEqualTo(root.get("experience"),Integer.parseInt(params.get(key)));
                        break;
                    case "minLevel":
                        p = criteriaBuilder.greaterThanOrEqualTo(root.get("level"),Integer.parseInt(params.get(key)));
                        break;
                    case "maxLevel":
                        p = criteriaBuilder.lessThanOrEqualTo(root.get("level"),Integer.parseInt(params.get(key)));
                        break;
                    case "title":
                        p = criteriaBuilder.like(root.get("title"),"%"+params.get(key)+"%");
                        break;
                    case "name":
                        p = criteriaBuilder.like(root.get("name"),"%"+params.get(key)+"%");
                        break;
                    case "after":
                        p = criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(Long.parseLong(params.get(key))));
                        break;
                    case "before":
                        p = criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), new Date(Long.parseLong(params.get(key))));
                        break;
                    case "banned":
                        p = criteriaBuilder.equal(root.get("banned"),"true".equals(params.get(key)));
                        break;
                }
                if (p != null) {
                    mainP = criteriaBuilder.and(mainP,p);
                }
            }
            return mainP;
        };
    }

}
