package com.project.tailsroute.controller;

import com.project.tailsroute.repository.DiaryRepository;
import com.project.tailsroute.service.AlarmService;
import com.project.tailsroute.service.DiaryService;
import com.project.tailsroute.vo.Alarms;
import com.project.tailsroute.vo.Diary;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/usr/diary")
public class UsrDiaryController {

    @Autowired
    private DiaryService diaryService;
    private ResourceLoader resourceLoader;
    private final Rq rq;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private DiaryRepository diaryRepository;


    public UsrDiaryController(Rq rq) {
        this.rq = rq;
    }

    @GetMapping("/write")
    public String showWriteForm(Model model) {
        boolean isLogined = rq.isLogined();

        if (!isLogined) {
            // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/usr/member/login";
        } else {
            // 로그인된 경우
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
            model.addAttribute("isLogined", true);
        }
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
            @RequestParam("information") String information,
            Model model
    ) {
        // DateTimeFormatter 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // String을 LocalDate로 변환
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);


        // 파일 처리 로직
        String imagePath = null;
        String fileName = file.getOriginalFilename();

// 파일이 비어 있지 않은지 확인
        if (file != null && !file.isEmpty()) {
            // 절대 경로를 사용하여 저장
            String uploadDir = "uploads" + File.separator + "diary"; // 저장할 디렉토리
            String filePath = uploadDir + File.separator + fileName; // 저장할 파일 경로

            try {
                // 파일 저장 전에 이미지 크기 조절
                Thumbnails.of(file.getInputStream())
                        .size(800, 800) // 원하는 사이즈로 조정
                        .toFile(new File(filePath));

                imagePath = "/uploads/diary/" + fileName; // 웹에서 접근할 수 있는 경로
            } catch (IOException e) {
                return "redirect:/usr/diary/list";
            }
        } else {
            // 파일이 없을 경우 기본 이미지 경로 설정
            imagePath = "/uploads/photo/default.png"; // 기본 이미지 경로
        }
        // 다이어리 작성 서비스 호출
        diaryService.writeDiary(memberId, title, body, imagePath, startDate, endDate, takingTime, information);

// 알람 설정
// DateTimeFormatter 정의
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

// 날짜와 시간을 함께 LocalDateTime으로 저장
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Alarms alarm = new Alarms();
            alarm.setMemberId(memberId);

            // 날짜와 시간을 함께 LocalDateTime으로 설정
            LocalDateTime alarmDateTime = LocalDateTime.of(date, takingTime);
            alarm.setAlarm_date(alarmDateTime.format(dateTimeFormatter)); // 알람 시간 설정

            // 현재 시간이 설정된 알람 시간보다 이전이고, 정확히 takingTime과 일치하는 경우에만 저장
            if (alarmDateTime.isAfter(LocalDateTime.now()) && alarmDateTime.getHour() == takingTime.getHour() && alarmDateTime.getMinute() == takingTime.getMinute()) {
                // 현재 시간 이후의 알람 중 정확히 takingTime에 일치하는 시간만 저장
                System.out.println("Setting alarm for: " + alarmDateTime.format(dateTimeFormatter));
                alarm.setMessage(" 복용시간 " + takingTime + "입니다."); // 메시지 설정
                alarm.setSite("/usr/diary/list"); // 알람 링크 설정

                // 알람 저장
                alarmService.saveAlarm(alarm); // 알람 저장
            }
        }
        return "redirect:/usr/diary/list";
    }

    @GetMapping("/list")
    public String showDiaryList(Model model, @RequestParam(defaultValue = "oldest") String sort, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "") String keyword) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {

            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);

            int memeberId = member.getId();

            int size = 8; // 페이지당 아이템 수
            List<Diary> diaries = diaryService.getDiaryList(memeberId, sort, page, size, keyword);
            int totalDiaries = diaryService.countDiaries(memeberId, keyword);
            int totalPages = (int) Math.ceil((double) totalDiaries / size);

            model.addAttribute("diaries", diaries);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("sort", sort);
            model.addAttribute("keyword", keyword);


            // pageNumbers 리스트 추가
            List<Integer> pageNumbers = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                pageNumbers.add(i);
            }
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("isLogined", isLogined);

        return "usr/diary/list";
    }

    @GetMapping("/calendar")
    @ResponseBody // 이 메서드는 JSON으로 반환됨
    public List<Map<String, Object>> getDiaryEvents(Model model, @RequestParam(defaultValue = "") String keyword) {
        List<Map<String, Object>> events = new ArrayList<>();
        boolean isLogined = rq.isLogined(); // 로그인 여부 확인

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);

            int memberId = member.getId(); // memberId 수정

            List<Diary> diaries = diaryService.findAllDiary(memberId, keyword); // 로그인한 사용자 다이어리 항목 가져오기

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            for (Diary diary : diaries) {

                // 약 복용 이벤트 추가
                Map<String, Object> medicineEvent = new HashMap<>();
                medicineEvent.put("id", diary.getId());
                medicineEvent.put("title", diary.getTitle());

                LocalDateTime startDateTime = LocalDateTime.of(diary.getStartDate(), diary.getTakingTime());
                medicineEvent.put("start", startDateTime.format(dateTimeFormatter)); // 복용 시작일
                LocalDateTime endDateTime = LocalDateTime.of(diary.getEndDate(), diary.getTakingTime());
                medicineEvent.put("end", endDateTime.format(dateTimeFormatter)); // 종료 시간 설정
                medicineEvent.put("className", "medicineEvent");
                events.add(medicineEvent);
            }
        } else {
            // 로그인하지 않은 경우 에러 메시지를 추가할 수 있습니다.
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("error", "로그인 후 이용해주세요.");
            events.add(errorEvent);
        }
        return events; // JSON 형식으로 반환
    }

    @GetMapping("/detail")
    public String showDiaryDetail(@RequestParam("id") int id, Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);

        Diary diary = diaryService.getForPrintDiary(id);
        if (diary == null) {
            // 다이어리가 존재하지 않을 경우, 목록으로 리다이렉트
            return "redirect:/usr/diary/list";
        }

        model.addAttribute("diary", diary);

        return "usr/diary/detail"; // 올바른 뷰 이름
    }

    @PostMapping("/delete")
    public String deleteDiary(@RequestParam("id") int id) {
        diaryService.deleteDiary(id);
        return "redirect:/usr/diary/list";
    }

    @GetMapping("/modify")
    public String showModifyForm(@RequestParam("id") int id, Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);

        Diary diary = diaryService.getDiaryById(id);
        model.addAttribute("diary", diary);
        return "usr/diary/modify";
    }

    @PostMapping("/modify")
    public String modifyDiaryEntry(
            @RequestParam("id") int id,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam(value = "file", required = false) MultipartFile file, // file 파라미터를 선택적으로 받도록 설정
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam("takingTime") LocalTime takingTime,
            @RequestParam("information") String information
    ) {
        // 기존 이미지 경로 가져오기
        String imagePath = diaryService.getDiaryById(id).getImagePath(); // 기존 경로 가져오기

        if (file != null && !file.isEmpty()) { // 파일이 있는 경우에만 처리
            String fileName = file.getOriginalFilename();
            String uploadDir = "uploads" + File.separator + "diary";
            String filePath = uploadDir + File.separator + fileName;

            try {
                Thumbnails.of(file.getInputStream())
                        .size(800, 800)
                        .toFile(new File(filePath));

                imagePath = "/uploads/diary/" + fileName; // 새 이미지 경로 설정
            } catch (IOException e) {
                return "redirect:/usr/diary/detail?id=" + id;
            }
        }

        diaryService.modifyDiary(id, title, body, imagePath, startDate, endDate, takingTime, information);
        return "redirect:/usr/diary/detail?id=" + id;
    }

    @GetMapping("/recommend")
    public String showRecommendForm(@RequestParam("id") int id, Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);

        Diary diary = diaryService.getDiaryById(id);
        model.addAttribute("diary", diary);
        return "usr/diary/recommend";
    }


    @PostMapping("/recommend")
    public String recommendDiary(
            @RequestParam("id") int id,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam("takingTime") LocalTime takingTime,
            @RequestParam("information") String information
    ) {
        // 파일 처리 로직
        String imagePath = null;
        String fileName = file.getOriginalFilename();

        // 절대 경로를 사용하여 저장
        String uploadDir = "uploads" + File.separator + "diary"; // 저장할 디렉토리
        String filePath = uploadDir + File.separator + fileName; // 저장할 파일 경로

        try {
            // 파일 저장 전에 이미지 크기 조절
            Thumbnails.of(file.getInputStream())
                    .size(800, 800) // 원하는 사이즈로 조정
                    .toFile(new File(filePath));

            imagePath = "/uploads/diary/" + fileName; // 웹에서 접근할 수 있는 경로
        } catch (IOException e) {
            return "redirect:/usr/diary/detail?id=" + id; // 상대 경로로 수정
        }
        diaryService.modifyDiary(id, title, body, imagePath, startDate, endDate, takingTime, information);
        return "redirect:/usr/diary/detail?id=" + id; // 상대 경로로 수정
    }
}