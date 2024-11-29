package com.project.tailsroute.service;

import com.project.tailsroute.repository.DogRepository;
import com.project.tailsroute.repository.MissingRepository;
import com.project.tailsroute.vo.Missing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissingService {
    @Autowired
    private MissingRepository missingRepository;

    public int lastNumber() {
        Integer number = missingRepository.lastNumber();
        return (number != null) ? number : 0; // null일 경우 0을 반환
    }

    public void write(int loginedMemberId, String name, String reportDate, String missingLocation, String breed, String color, String gender, String age, String rfid, String photoPath, String trait) {
        missingRepository.write(loginedMemberId, name, reportDate, missingLocation, breed, color, gender, age, rfid, photoPath, trait);
    }

    public int totalCnt(String str) {
        String[] strings = getRegionCode(str);
        return missingRepository.totalCnt(strings[0], strings[1]);
    }

    public List<Missing> list(int limitFrom, int itemsInAPage, String str) {
        String[] strings = getRegionCode(str);
        return missingRepository.list(limitFrom, itemsInAPage, strings[0], strings[1]);
    }

    public Missing missingArticle(int missingId) {
        return missingRepository.missingArticle(missingId);
    }

    public void missingDelete(int missingId) {
        missingRepository.missingDelete(missingId);
    }

    public void modify(int id, String name, String reportDate, String missingLocation, String breed, String color, String gender, String age, String rfid, String photoPath, String trait) {
        missingRepository.modify(id, name, reportDate, missingLocation, breed, color, gender, age, rfid, photoPath, trait);
    }

    public String[] getRegionCode(String missingLocation) {
        if (missingLocation.contains("전체")) return new String[]{"전체", "전체"};
        if (missingLocation.contains("인천") || missingLocation.contains("인천광역시")) return new String[]{"인천", "인천광역시"};
        if (missingLocation.contains("서울") || missingLocation.contains("서울특별시")) return new String[]{"서울", "서울특별시"};
        if (missingLocation.contains("경기") || missingLocation.contains("경기도")) return new String[]{"경기", "경기도"};
        if (missingLocation.contains("강원") || missingLocation.contains("강원도")) return new String[]{"강원", "강원도"};
        if (missingLocation.contains("충남") || missingLocation.contains("충청남도")) return new String[]{"충남", "충청남도"};
        if (missingLocation.contains("세종") || missingLocation.contains("세종특별자치시")) return new String[]{"세종", "세종특별자치시"};
        if (missingLocation.contains("대전") || missingLocation.contains("대전광역시")) return new String[]{"대전", "대전광역시"};
        if (missingLocation.contains("충북") || missingLocation.contains("충청북도")) return new String[]{"충북", "충청북도"};
        if (missingLocation.contains("전북") || missingLocation.contains("전라북도")) return new String[]{"전북", "전라북도"};
        if (missingLocation.contains("대구") || missingLocation.contains("대구광역시")) return new String[]{"대구", "대구광역시"};
        if (missingLocation.contains("울산") || missingLocation.contains("울산광역시")) return new String[]{"울산", "울산광역시"};
        if (missingLocation.contains("경북") || missingLocation.contains("경상북도")) return new String[]{"경북", "경상북도"};
        if (missingLocation.contains("전남") || missingLocation.contains("전라남도")) return new String[]{"전남", "전라남도"};
        if (missingLocation.contains("광주") || missingLocation.contains("광주광역시")) return new String[]{"광주", "광주광역시"};
        if (missingLocation.contains("경남") || missingLocation.contains("경상남도")) return new String[]{"경남", "경상남도"};
        if (missingLocation.contains("부산") || missingLocation.contains("부산광역시")) return new String[]{"부산", "부산광역시"};
        if (missingLocation.contains("제주") || missingLocation.contains("제주특별자치도")) return new String[]{"제주", "제주특별자치도"};
        return null;
    }

    public int findMissingId() {
        return missingRepository.findMissingId();
    }
}
