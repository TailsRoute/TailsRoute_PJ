package com.project.tailsroute.service;

import com.project.tailsroute.repository.WalkRepository;
import com.project.tailsroute.vo.Walk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalkService {
    private final WalkRepository walkRepository;

    @Autowired
    public WalkService(WalkRepository walkRepository) {
        this.walkRepository = walkRepository;
    }

    public void addWalk(Walk walk) {
        walkRepository.addWalks(
                walk.getMemberId(),
                walk.getRouteName(),
                walk.getPurchaseDate(),
                walk.getRoutePicture(),
                walk.getRoutedistance()
        );
    }
    public List<Walk> findWalksByMemberId(int memberId) {
        return walkRepository.findByMemberId(memberId);
    }
    public void updateWalks(String routeName, String purchaseDate, int id) {
        walkRepository.updateWalks(routeName, purchaseDate,id);
    }
    public void deleteWalks(int id) {
        walkRepository.deleteWalks(id);
    }
}