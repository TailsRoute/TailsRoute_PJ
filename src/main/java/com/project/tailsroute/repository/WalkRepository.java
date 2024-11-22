package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Walk;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WalkRepository {
    @Insert("INSERT INTO walk (updateDate, memberId, routeName, purchaseDate, routePicture, routedistance,isLiked) " +
            "VALUES (NOW(), #{memberId}, #{routeName}, NOW(), #{routePicture},#{routedistance},#{isLiked})")
    public void addWalks(int memberId, String routeName, String routePicture,double routedistance,int isLiked);

    @Select("SELECT * FROM walk WHERE memberId = #{memberId}")
    public List<Walk> findByMemberId(int memberId);

    @Select("SELECT routePicture FROM walk WHERE routeName= #{routeName} AND purchaseDate=#{purchaseDate} AND routedistance=#{routedistance}")
    public String findRoutePicture(String routeName, String purchaseDate, double routedistance);

    @Select("SELECT DATE(purchaseDate) AS date, COUNT(*) AS extra__count FROM walk WHERE YEAR(purchaseDate) = #{year} AND memberId= #{memberId} GROUP BY DATE(purchaseDate) ORDER BY DATE(purchaseDate)")
    public List<Walk> countdate(int year,int memberId);

    @Update("UPDATE walk SET isLiked= #{isLiked} WHERE routeName= #{routeName} AND purchaseDate=#{purchaseDate} AND routedistance=#{routedistance}")
    public void updateIsLiked(int isLiked,String routeName, String purchaseDate, double routedistance);

    @Update("UPDATE walk SET routeName = #{routeName}, purchaseDate = #{purchaseDate} WHERE id = #{id}")
    public void updateWalks(String routeName, String purchaseDate, int id);

    @Delete("DELETE FROM walk WHERE id = #{id}")
    public void deleteWalks(int id);

    @Select("""
    SELECT M.nickname extra__walker, COUNT(W.purchaseDate) extra__count, ROUND(SUM(W.routedistance), 1) extra__distance\s
    FROM walk W
    LEFT JOIN `member` M
    ON W.memberId = M.id
    WHERE MONTH(W.purchaseDate) = MONTH(CURRENT_DATE())
    AND YEAR(W.purchaseDate) = YEAR(CURRENT_DATE())
    GROUP BY W.memberId
    ORDER BY extra__Count DESC, extra__distance DESC
    LIMIT 0, 10;
    """)
    List<Walk> getWalksRanking();
}