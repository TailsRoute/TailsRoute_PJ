package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Walk;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WalkRepository {
    @Insert("INSERT INTO walk (regDate, updateDate, memberId, routeName, purchaseDate, routePicture, routedistance) " +
            "VALUES (NOW(), NOW(), #{memberId}, #{routeName}, #{purchaseDate}, #{routePicture},#{routedistance})")
    public void addWalks(int memberId, String routeName, String purchaseDate, String routePicture,double routedistance);

    @Select("SELECT * FROM walk WHERE memberId = #{memberId}")
    public List<Walk> findByMemberId(int memberId);

    @Update("UPDATE walk SET routeName = #{routeName}, purchaseDate = #{purchaseDate} WHERE id = #{id}")
    public void updateWalks(String routeName, String purchaseDate, int id);

    @Delete("DELETE FROM walk WHERE id = #{id}")
    public void deleteWalks(int id);
}