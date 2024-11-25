package com.project.tailsroute.repository;

import com.project.tailsroute.vo.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface MemberRepository {

    @Select("""			
            SELECT M.*, D.photo extra__dogPoto
            FROM `member` M
            LEFT JOIN dog D
            ON D.memberId = M.id
            WHERE M.id = #{id}
            LIMIT 1
            """)
    public Member getMemberById(int id);

    @Select("""
			SELECT M.*, D.photo extra__dogPoto
            FROM `member` M
            LEFT JOIN dog D
            ON D.memberId = M.id
            WHERE loginId = #{loginId}
            LIMIT 1
			""")
    public Member getMemberByLoginId(String loginId);

    @Insert("INSERT INTO `member` SET regDate = NOW(), updateDate = NOW(), loginId = #{loginId}, loginPw = #{loginPw}, `name` = #{name}, nickname = #{nickname}, cellphoneNum = #{cellphoneNum}, email = #{email}, socialLoginStatus = #{socialLoginStatus}")
    public void doJoin(String loginId, String loginPw, String name, String nickname, String cellphoneNum,String email, int socialLoginStatus);

    @Select("SELECT LAST_INSERT_ID();")
    public int getLastInsertId();

    @Update("""
            UPDATE member
            SET updateDate = NOW(),            
            loginPw = #{loginPw},
            name = #{name},
            nickname = #{nickname},
            cellphoneNum = #{cellphoneNum}
            WHERE id = #{loginedMemberId}
            """)
    void memberModify(int loginedMemberId, String name, String nickname, String cellphoneNum, String loginPw);

    @Update("""
            UPDATE member
            SET updateDate = NOW(),
            delDate = NOW(),
            delStatus = 1 
            WHERE id = #{loginedMemberId}
            """)
    void memberDelStatus(int loginedMemberId);

    @Update("""
            UPDATE member
            SET updateDate = NOW(),
            delStatus = 0 
            WHERE id = #{loginedMemberId}
            """)
    void memberReStatus(int loginedMemberId);

    @Select("""
			SELECT M.*, D.photo extra__dogPoto
            FROM `member` M
            LEFT JOIN dog D
            ON D.memberId = M.id
            WHERE email = #{email}
            LIMIT 1
			""")
    Member getMemberByEmail(String email);

    @Update("""
            UPDATE member
            SET updateDate = NOW(),
            loginPw = #{loginPW}
            WHERE id = #{id}
            """)
    void setTemporaryPassword(int id, String loginPW);



    @Select("""
			SELECT *
			FROM `member`
			WHERE name = #{name}
            AND cellphoneNum = #{cellphoneNum}
			""")
    Member getMemberByNameAndcellphoneNum(String name, String cellphoneNum);
}