package com.joe_goldberge.web;

import com.joe_goldberge.entities.UserContent;
import com.joe_goldberge.entities.UserDTO;
import com.joe_goldberge.entities.Users;

import com.joe_goldberge.repository.UserContentRepository;
import com.joe_goldberge.repository.UsersRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "https://joe-goldberg.vercel.app")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserContentRepository userContentRepo;

    private final UsersRepository usersRepo;

    public UserController(UserContentRepository userContentRepo, UsersRepository usersRepo) {
        this.userContentRepo = userContentRepo;
        this.usersRepo = usersRepo;

    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        List<UserContent> userContents = userContentRepo.findAll();
        List<Users> usersList = usersRepo.findAll();
        System.out.println(userContents);
        System.out.println(usersList);

        // Corrected: Map userId to Users
        Map<String, Users> userMap = usersList.stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        List<UserDTO> users = userContents.stream()
                .map(p -> new UserDTO(userMap.get(p.getUserId()), p))
                .collect(Collectors.toList());

        return users;
    }
}
