package com.joe_goldberge.web;

import com.joe_goldberge.entities.UserContent;
import com.joe_goldberge.entities.UserDTO;
import com.joe_goldberge.entities.Users;

import com.joe_goldberge.repository.UserContentRepository;
import com.joe_goldberge.repository.UsersRepository;
import com.joe_goldberge.service.RecordFetcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"https://joe-goldberg.vercel.app", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserContentRepository userContentRepo;

    private final UsersRepository usersRepo;

    public UserController(UserContentRepository userContentRepo, UsersRepository usersRepo) {
        this.userContentRepo = userContentRepo;
        this.usersRepo = usersRepo;

    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {

        CompletableFuture.runAsync(() -> {
            try {
                RecordFetcher fetcher = new RecordFetcher();
                fetcher.loadNewUsers();
            } catch (Exception e) {
                // Log the exception or handle it as needed
                e.printStackTrace();
            }
        });


        List<UserContent> userContents = userContentRepo.findAll();
        List<Users> usersList = usersRepo.findAll();
        // Corrected: Map userId to Users
        Map<String, Users> userMap = usersList.stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        List<UserDTO> users = userContents.stream()
                .map(p -> new UserDTO(userMap.get(p.getUserId()), p))
                .collect(Collectors.toList());

        return users;
    }

}
