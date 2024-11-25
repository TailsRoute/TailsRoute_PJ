package com.project.tailsroute.service;

import com.project.tailsroute.repository.WalkRepository;
import com.project.tailsroute.vo.Walk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                walk.getRoutePicture(),
                walk.getRoutedistance(),
                walk.getIsLiked()
        );
    }
    public List<Walk> findWalksByMemberId(int memberId) {
        return walkRepository.findByMemberId(memberId);
    }
    public List<Walk> countdate(int year,int memberId) {
        return walkRepository.countdate(year,memberId);
    }
    public String findRoutePicture(String routeName, String purchaseDate, Double routedistance){
        return walkRepository.findRoutePicture(routeName,purchaseDate,routedistance);
    }
    public void updateIsLiked (int isLiked,String routeName, String purchaseDate, double routedistance) {
        walkRepository.updateIsLiked(isLiked,routeName, purchaseDate,routedistance);
    }
    @Transactional
    public void updateWalks(String routeName, String purchaseDate,String routePicture,double routedistance,int id) {
        walkRepository.updateWalks(routeName, purchaseDate,routePicture,routedistance,id);
    }
    public void deleteWalks(int id) {
        walkRepository.deleteWalks(id);
    }

    public List<Walk> getWalksRanking() {
        return walkRepository.getWalksRanking();
    }
    public Walk findWalk(int id) {
        return walkRepository.findById(id);
    }
    public List<Walk> findFavoritesByMemberId(int memberId) {
        return walkRepository.findFavoritesByMemberId(memberId);
    }
}