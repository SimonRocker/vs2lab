package de.hska.lkit.demo.redis.controller;

import de.hska.lkit.demo.redis.model.Greeting;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class HomeController {

    @RequestMapping(value = "/home")
    public String greetingSubmit(@ModelAttribute Greeting greeting, Model model) {
        model.addAttribute("home", greeting != null ? greeting : new Greeting());
        return "home";
    }



}
