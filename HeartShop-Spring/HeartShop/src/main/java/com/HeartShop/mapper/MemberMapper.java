package com.HeartShop.mapper;

import com.HeartShop.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 會員 Mapper 介面
 */
@Mapper
public interface MemberMapper {
    /**
     * 根據 Email 查詢會員（登入用）
     */
    Optional<Member> selectByEmail(@Param("email") String email);

    /**
     * 根據 name 查詢會員（支援 username 登入）
     */
    Optional<Member> selectByName(@Param("name") String name);

    /**
     * 新增會員（註冊用）
     */
    int insertMember(Member member);

    /**
     * 根據 ID 查詢會員
     */
    Optional<Member> selectById(@Param("memberId") Long memberId);

    /**
     * 更新會員資料（不包含 email 和 password）
     */
    int updateProfile(
        @Param("memberId") Long memberId,
        @Param("name") String name,
        @Param("phone") String phone,
        @Param("birthday") LocalDate birthday,
        @Param("address") String address
    );

    /**
     * 更新會員密碼
     */
    int updatePassword(
        @Param("memberId") Long memberId,
        @Param("password") String password
    );

    // ── Admin ──────────────────────────────────────────

    List<Member> adminFindAll(@Param("keyword") String keyword,
                              @Param("status") String status,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    long adminCountAll(@Param("keyword") String keyword,
                       @Param("status") String status);

    int adminUpdateStatus(@Param("memberId") Long memberId,
                          @Param("status") String status);

    int adminUpdateRole(@Param("memberId") Long memberId,
                        @Param("role") String role);

    int adminDeleteMember(@Param("memberId") Long memberId);
}
