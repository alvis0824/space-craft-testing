package com.cydeo.spacecraft.service.impl;

import com.cydeo.spacecraft.dto.CreateHitDTO;
import com.cydeo.spacecraft.entity.Game;
import com.cydeo.spacecraft.entity.Player;
import com.cydeo.spacecraft.entity.Target;
import com.cydeo.spacecraft.enumtype.AttackType;
import com.cydeo.spacecraft.repository.GameRepository;
import com.cydeo.spacecraft.service.CreateHitService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CreateHitServiceImpl implements CreateHitService {

    private final GameRepository gameRepository;

    public CreateHitServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }


    @Override
    @Transactional
    public Game createHit(CreateHitDTO createHitDTO) {
        final AttackType attackType = createHitDTO.getAttackType();

        final Game game = gameRepository.findById(createHitDTO.getGameId()).orElseThrow(() -> new RuntimeException("Game Not Found"));

        if (game.getIsEnded()){
            throw new RuntimeException("Game is already ended");
        }

        final Player player = game.getPlayer();
        final Set<Target> targets = game.getTargets();
//
//        Set<Target> filteredTargets = targets.stream()
//                .filter(target1 -> target1.getHealth() > 0).collect(Collectors.toSet());

        final Target target = targets.stream().skip(new Random().nextInt(targets.size()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Target Not Found"));

        int playerHealth = player.getHealth();
        switch (attackType) {
            case PLAYER_TO_TARGET:
                final int targetHealth = target.getHealth() + target.getArmor() - player.getShootPower();
                target.setHealth(targetHealth);
                break;
            case TARGET_TO_PLAYER:
                playerHealth = player.getHealth() + player.getArmor() - target.getShootPower();
                player.setHealth(playerHealth);
                break;
        }
        boolean isGameEnded = false;
        boolean isPlayerWin = false;

        if (targets.stream().allMatch(target1 -> target1.getHealth() <= 0)) {
            isGameEnded = true;
            isPlayerWin = true;

        }
        if (playerHealth <= 0) {
            isGameEnded = true;
            isPlayerWin = false;

        }

        game.setIsEnded(isGameEnded);
        game.setIsWin(isPlayerWin);
        return gameRepository.save(game);
    }
}
