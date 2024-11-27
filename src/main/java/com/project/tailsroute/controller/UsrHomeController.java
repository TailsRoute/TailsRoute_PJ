package com.project.tailsroute.controller;

import com.project.tailsroute.service.GpsChackService;
import com.project.tailsroute.service.MissingService;
import com.project.tailsroute.vo.GpsChack;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Missing;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${GOOGLE_MAP_API_KEY}")
    private String API_KEY;

    @Autowired
    private MissingService missingService;

    @Autowired
    private GpsChackService gpsChackService;

    @GetMapping("/usr/home/main")
    public String showMain(Model model) {
        boolean isLogined = rq.isLogined();
        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
            GpsChack gpsChack = gpsChackService.chack(member.getId());
            model.addAttribute("gpsChack", gpsChack);
        }
        model.addAttribute("isLogined", isLogined);

        model.addAttribute("API_KEY", API_KEY);

        List<Missing> missings = missingService.list(0, 10, "전체");

        model.addAttribute("missings", missings);
        return "usr/home/main";
    }

    @GetMapping("/usr/home/main2")
    public String showMain_ver2(Model model) {
        boolean isLogined = rq.isLogined();
        Member member = rq.getLoginedMember();

        if(member == null) {
            model.addAttribute("isLogined", false);
            model.addAttribute("nickname","");
        }else{
            model.addAttribute("isLogined", true);
            model.addAttribute("nickname", member.getNickname());
        }

        if (isLogined) {
            GpsChack gpsChack = gpsChackService.chack(member.getId());
            model.addAttribute("gpsChack", gpsChack);
        }
        model.addAttribute("isLogined", isLogined);
        model.addAttribute("API_KEY", API_KEY);

        List<Missing> missings = missingService.list(0, 10, "전체");

        model.addAttribute("missings", missings);

        return "usr/home/main2";
    }
}
