package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.Member;
import com.HeartShop.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping
    public ApiResponse<Void> createMember(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String name     = body.get("name");
        String password = body.get("password");
        String role     = body.getOrDefault("role", "CUSTOMER");

        if (email == null || email.isBlank())    return ApiResponse.error("400", "email 不可為空");
        if (name == null || name.isBlank())      return ApiResponse.error("400", "姓名不可為空");
        if (password == null || password.length() < 6) return ApiResponse.error("400", "密碼至少 6 位");
        if (memberMapper.selectByEmail(email).isPresent()) return ApiResponse.error("409", "此 Email 已被使用");

        Member m = new Member();
        m.setEmail(email);
        m.setName(name);
        m.setPassword(passwordEncoder.encode(password));
        m.setRole(role);
        m.setStatus("ACTIVE");
        m.setSubscribeEdm(false);
        memberMapper.insertMember(m);
        return ApiResponse.success("會員已建立", null);
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> listMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        int offset = (page - 1) * pageSize;
        List<Member> items = memberMapper.adminFindAll(keyword, status, offset, pageSize);
        long total = memberMapper.adminCountAll(keyword, status);

        // 隱藏密碼欄位
        items.forEach(m -> m.setPassword(null));

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return ApiResponse.success(result);
    }

    @GetMapping("/{memberId}")
    public ApiResponse<Member> getMember(@PathVariable Long memberId) {
        Member member = memberMapper.selectById(memberId).orElse(null);
        if (member == null) {
            return ApiResponse.error("404", "找不到會員");
        }
        member.setPassword(null);
        return ApiResponse.success(member);
    }

    @PutMapping("/{memberId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long memberId,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ApiResponse.error("400", "status 不可為空");
        }
        memberMapper.adminUpdateStatus(memberId, newStatus);
        return ApiResponse.success("狀態已更新", null);
    }

    @PutMapping("/{memberId}/role")
    public ApiResponse<Void> updateRole(
            @PathVariable Long memberId,
            @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ApiResponse.error("400", "role 不可為空");
        }
        if (!newRole.equals("CUSTOMER") && !newRole.equals("VIP") && !newRole.equals("ADMIN")) {
            return ApiResponse.error("400", "無效的權限值");
        }
        memberMapper.adminUpdateRole(memberId, newRole);
        return ApiResponse.success("權限已更新", null);
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> deleteMember(@PathVariable Long memberId) {
        Member member = memberMapper.selectById(memberId).orElse(null);
        if (member == null) {
            return ApiResponse.error("404", "找不到會員");
        }
        memberMapper.adminDeleteMember(memberId);
        return ApiResponse.success("會員已刪除", null);
    }
}
