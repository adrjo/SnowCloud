package com.github.adrjo.snowcloud;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions") // prefix for the endpoints
public class SnowCloud {

    @PostMapping("/yo")
    public String yo() {
        return "hey buddy";
    }

    @GetMapping("/yo")
    public String yo2() {
        return "try post :D";
    }

//    @PostMapping("/register")
//    public boolean register(@RequestBody User user) {
//        return user.name != null && user.pass != null;
//    }

    //returnvalue ResponseEntity to return different status codes

//    @AllArgsConstructor
//    public static class User {
//        private final String name;
//        private final String pass;
//    }
}
