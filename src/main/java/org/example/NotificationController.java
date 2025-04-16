package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final LiquibaseService liquibaseService;

    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notifications"; // без .html
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("wrapper", new NotificationListWrapper());
        return "notifications";
    }

    @PostMapping("/generate")
    public String generateSql(@ModelAttribute NotificationListWrapper wrapper) {
        liquibaseService.processNotifications(wrapper.getNotifications());
        return "redirect:/";
    }
}
