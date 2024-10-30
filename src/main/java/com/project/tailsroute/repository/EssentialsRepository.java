package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Essentials;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EssentialsRepository {

    @Insert("INSERT INTO essentials (regDate, updateDate, memberId, itemType, purchaseDate, usageCycle, timing, productPicture, productPrice, productLink) " +
            "VALUES (NOW(), NOW(), #{memberId}, #{itemType}, #{purchaseDate}, #{usageCycle}, #{timing}, #{productPicture}, #{productPrice}, #{productLink})")
    public void addEssentials(int memberId, String itemType, String purchaseDate, Integer usageCycle, Integer timing, String productPicture, String productPrice, String productLink);

    @Select("SELECT * FROM essentials WHERE memberId = #{memberId}")
    public List<Essentials> findByMemberId(int memberId);

    @Update("UPDATE essentials SET itemType = #{itemType}, usageCycle = #{usageCycle}, timing = #{timing} ,purchaseDate = #{purchaseDate} WHERE id = #{id}")
    public void updateEssentials(String itemType, int usageCycle, int timing,String purchaseDate , int id);

    @Delete("DELETE FROM essentials WHERE id = #{id}")
    public void deleteEssentials(int id);
}