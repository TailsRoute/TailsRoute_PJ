package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Carts;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartRepository {
    @Insert("INSERT INTO cart (regDate, updateDate, memberId, itemName, itemprice) " +
            "VALUES (NOW(), NOW(), #{memberId}, #{itemName}, #{itemprice})")
    public void addCarts(int memberId, String itemName, Integer itemprice);

    @Select("SELECT * FROM cart WHERE memberId = #{memberId}")
    public List<Carts> findByMemberId(int memberId);

    @Delete("DELETE FROM cart WHERE id = #{id}")
    public void deleteCart(int id);
}
