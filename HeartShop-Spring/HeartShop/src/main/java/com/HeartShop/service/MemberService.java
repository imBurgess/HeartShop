package com.HeartShop.service;

import com.HeartShop.dto.ChangePasswordRequest;
import com.HeartShop.dto.LoginRequest;
import com.HeartShop.dto.LoginResponse;
import com.HeartShop.dto.MemberDTO;
import com.HeartShop.dto.RegisterRequest;
import com.HeartShop.dto.UpdateProfileRequest;
import com.HeartShop.entity.Member;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.mapper.MemberMapper;
import com.HeartShop.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員服務類
 */
@Slf4j
@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper, JwtUtil jwtUtil) {
        this.memberMapper = memberMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 會員登入
     *
     * @param request 登入請求（email + password）
     * @return LoginResponse（token + member info）
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 查詢會員（先以 email 查，若找不到則以 name 查，支援 username 登入）
        Member member = memberMapper.selectByEmail(request.getEmail())
                .or(() -> memberMapper.selectByName(request.getEmail()))
                .orElseThrow(() -> new BusinessException("4001", "帳號或密碼錯誤"));

        // 2. 檢查會員狀態
        if ("INACTIVE".equals(member.getStatus())) {
            log.warn("會員 {} 已停用，無法登入", request.getEmail());
            throw new BusinessException("4003", "此帳號已停用，請聯繫客服");
        }

        // 3. 驗證密碼（BCrypt）
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            log.warn("會員 {} 登入失敗：密碼錯誤", request.getEmail());
            throw new BusinessException("4001", "帳號或密碼錯誤");
        }

        // 4. 生成 JWT Token（包含角色資訊）
        String token = jwtUtil.generateToken(
                member.getMemberId(),
                member.getEmail(),
                member.getRole()
        );

        // 5. 組裝回傳資料（account 和 email 都設為 email）
        MemberDTO memberDTO = new MemberDTO(
                member.getMemberId(),
                member.getEmail(),  // account 欄位
                member.getEmail(),  // email 欄位
                member.getName(),
                member.getRole(),
                member.getCreatedAt() != null ? member.getCreatedAt().toString() : ""
        );

        log.info("會員 {} ({}) 登入成功", member.getEmail(), member.getRole());
        return new LoginResponse(token, memberDTO);
    }

    /**
     * 會員註冊
     *
     * @param request 註冊請求（email + name + password）
     * @return MemberDTO 新註冊的會員資料
     */
    @Transactional
    public MemberDTO register(RegisterRequest request) {
        // 1. 檢查 Email 是否已存在
        if (memberMapper.selectByEmail(request.getEmail()).isPresent()) {
            log.warn("註冊失敗：Email {} 已被使用", request.getEmail());
            throw new BusinessException("4009", "此 Email 已被註冊");
        }

        // 2. 建立會員實體
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setName(request.getName());
        member.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt 加密
        member.setRole("CUSTOMER");      // 一般用戶註冊時自動設為 CUSTOMER
        member.setStatus("ACTIVE");      // 新會員預設啟用
        member.setSubscribeEdm(false);   // 預設不訂閱電子報

        // 3. 儲存到資料庫
        int result = memberMapper.insertMember(member);
        if (result != 1) {
            log.error("註冊失敗：資料庫插入錯誤");
            throw new BusinessException("5001", "註冊失敗，請稍後再試");
        }

        log.info("新會員註冊成功：{} (ID: {})", member.getEmail(), member.getMemberId());

        // 4. 回傳會員資料（不含密碼）
        return new MemberDTO(
                member.getMemberId(),
                member.getEmail(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getCreatedAt() != null ? member.getCreatedAt().toString() : ""
        );
    }

    /**
     * 更新會員資料
     *
     * @param memberId 會員 ID
     * @param request  更新請求
     * @return 更新後的會員資料
     */
    @Transactional
    public MemberDTO updateProfile(Long memberId, UpdateProfileRequest request) {
        // 1. 查詢會員是否存在（不存在則拋出例外）
        memberMapper.selectById(memberId)
                .orElseThrow(() -> new BusinessException("4004", "會員不存在"));

        // 2. 更新會員資料
        int result = memberMapper.updateProfile(
                memberId,
                request.getName(),
                request.getPhone(),
                request.getBirthday(),
                request.getAddress()
        );

        if (result != 1) {
            log.error("更新會員資料失敗：memberId={}", memberId);
            throw new BusinessException("5002", "更新失敗，請稍後再試");
        }

        log.info("會員資料更新成功：memberId={}", memberId);

        // 3. 重新查詢並回傳更新後的資料
        Member updatedMember = memberMapper.selectById(memberId)
                .orElseThrow(() -> new BusinessException("4004", "會員不存在"));

        return toDTO(updatedMember);
    }

    /**
     * 根據 ID 查詢會員（供 /me 端點使用）
     */
    public MemberDTO getMemberById(Long memberId) {
        Member member = memberMapper.selectById(memberId)
                .orElseThrow(() -> new BusinessException("4004", "會員不存在"));
        return toDTO(member);
    }

    private MemberDTO toDTO(Member m) {
        return new MemberDTO(
                m.getMemberId(),
                m.getEmail(),
                m.getEmail(),
                m.getName(),
                m.getRole(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""
        );
    }

    /**
     * 修改密碼
     *
     * @param memberId 會員 ID
     * @param request  修改密碼請求
     */
    @Transactional
    public void changePassword(Long memberId, ChangePasswordRequest request) {
        // 1. 查詢會員
        Member member = memberMapper.selectById(memberId)
                .orElseThrow(() -> new BusinessException("4004", "會員不存在"));

        // 2. 驗證舊密碼
        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            log.warn("修改密碼失敗：舊密碼不正確, memberId={}", memberId);
            throw new BusinessException("4005", "舊密碼不正確");
        }

        // 3. 加密新密碼
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        // 4. 更新密碼
        int result = memberMapper.updatePassword(memberId, encodedPassword);

        if (result != 1) {
            log.error("修改密碼失敗：memberId={}", memberId);
            throw new BusinessException("5003", "修改密碼失敗，請稍後再試");
        }

        log.info("會員密碼修改成功：memberId={}", memberId);
    }
}
