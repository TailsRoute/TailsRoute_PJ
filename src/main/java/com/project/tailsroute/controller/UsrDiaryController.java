package com.project.tailsroute.controller;

import com.project.tailsroute.service.DiaryService;
import com.project.tailsroute.vo.Diary;
import com.project.tailsroute.vo.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.project.tailsroute.vo.Rq;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/usr/diary")
public class UsrDiaryController {

    @Autowired
    private DiaryService diaryService;
    private ResourceLoader resourceLoader;
    private final Rq rq;
    private Date regDate;

    public UsrDiaryController(Rq rq) {
        this.rq = rq;
    }

    @GetMapping("/write")
    public String showWriteForm(Model model) {
          boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);


     /*   if (!isLogined) {
            // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/usr/member/login";
        } else {
            // 로그인된 경우
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
            model.addAttribute("isLogined", true);
        }*/
        return "usr/diary/write"; // 다이어리 작성 페이지로 이동
    }


    @PostMapping("/write")
    public String submitDiary(
            @RequestParam("memberId") int memberId,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file,
            @RequestParam("startDate") String startDateStr, // String으로 받아오기
            @RequestParam("endDate") String endDateStr, // String으로 받아오기
            @RequestParam("takingTime") LocalTime takingTime,
            @RequestParam("information") String information
    ) {
        boolean isLogined = rq.isLogined();

        // DateTimeFormatter 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // String을 LocalDate로 변환
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        String imagePath;

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            // 저장할 디렉토리 경로 설정
            String directoryPath = "src/main/resources/static/resource/DiaryImages";
            File directory = new File(directoryPath);

            // 디렉토리가 존재하지 않으면 생성
            if (!directory.exists()) {
                directory.mkdirs(); // 디렉토리 생성
            }

            String savePath = new File(directory, fileName).getAbsolutePath();

            System.out.println("File will be saved to: " + savePath);

            try {
                // 파일을 지정된 경로에 저장
                file.transferTo(new File(savePath));
                imagePath = "/resource/DiaryImages/" + fileName; // 웹에서 접근할 수 있는 URL 경로
            } catch (IOException e) {
                e.printStackTrace();
                imagePath = "/resource/DiaryImages/default.png"; // 기본 이미지 URL로 설정
            }
        } else {
            imagePath = "/resource/DiaryImages/default.png"; // 기본 이미지 URL
        }

        // 다이어리 작성 서비스 호출
        diaryService.writeDiary(memberId, title, body, imagePath, startDate, endDate, takingTime, information);
        return "redirect:/usr/diary/list";
    }
    @GetMapping("/list")
    public String showDiaryList(Model model, @RequestParam(defaultValue = "oldest") String sort,@RequestParam(defaultValue = "1") int page) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);

        int size = 7; // 페이지당 아이템 수
        List<Diary> diaries = diaryService.getDiaryList(sort, page, size);
        int totalDiaries = diaryService.countDiaries();
        int totalPages = (int) Math.ceil((double) totalDiaries / size);

        model.addAttribute("diaries", diaries);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sort", sort);

        // pageNumbers 리스트 추가
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);

        return "usr/diary/list";
    }




}