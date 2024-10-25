package com.project.tailsroute.controller;

import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.service.MissingService;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Missing;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UsrHomeController {
    private final Rq rq;

    public UsrHomeController(Rq rq) {
        this.rq = rq;
    }

    @Autowired
    private MissingService missingService;

    @GetMapping("/usr/home/main")
    public String showMain(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);

        List<Missing> missings = missingService.list(0, 10, "전체");

        model.addAttribute("isLogined", isLogined);
        model.addAttribute("missings", missings);
        return "usr/home/main";
    }
}
