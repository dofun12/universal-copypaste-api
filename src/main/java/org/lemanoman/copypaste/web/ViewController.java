package org.lemanoman.copypaste.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Locale;

/**
 * Serves the Thymeleaf pages. All actual data access happens client-side via
 * the REST/WebSocket API, so these controllers only render shells with the
 * relevant model attributes (e.g. the chat code from the URL).
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/chat/{code}")
    public String chatRoom(@PathVariable String code, Model model) {
        model.addAttribute("code", code.trim().toUpperCase(Locale.ROOT));
        return "chat";
    }
}
