package com.vn.go_toeic.controller.admin;

import com.vn.go_toeic.model.User;
import com.vn.go_toeic.util.meta.LayoutMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {
    @GetMapping("/admin/blank")
    public String getBlankPage(Model model,  @ModelAttribute(value = "currentUser", binding = false) User currentUser) {
        model.addAttribute("layoutMeta", new LayoutMeta(null, null, null));
        return "admin/page/blank";
    }

    @GetMapping("/admin")
    public String getDashboardPage(Model model,  @ModelAttribute(value = "currentUser", binding = false) User currentUser) {
        model.addAttribute("layoutMeta", new LayoutMeta("Bảng điều khiển", "dashboard", null));
        return "admin/page/dashboard";
    }
}
