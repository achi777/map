package ge.devspace.gismap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @GetMapping("/")
    public String index() {
        return "redirect:/map";
    }

    @GetMapping("/map")
    public String map(Model model) {
        model.addAttribute("title", "Interactive GIS Map");
        return "map";
    }
}