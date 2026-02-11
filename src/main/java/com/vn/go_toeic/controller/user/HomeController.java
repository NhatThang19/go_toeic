package com.vn.go_toeic.controller.user;

import com.vn.go_toeic.util.meta.LayoutMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String getHomePage(Model model) {
        model.addAttribute("layoutMeta", new LayoutMeta("Trang chủ", "home", null));

        return "user/page/index";
    }


}
