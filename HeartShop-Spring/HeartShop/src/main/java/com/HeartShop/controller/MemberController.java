package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.dto.ChangePasswordRequest;
import com.HeartShop.dto.LoginRequest;
import com.HeartShop.dto.LoginResponse;
import com.HeartShop.dto.MemberDTO;
import com.HeartShop.dto.RegisterRequest;
import com.HeartShop.dto.UpdateProfileRequest;
import com.HeartShop.entity.OrderQa;
import com.HeartShop.entity.ProductQa;
import com.HeartShop.mapper.OrderQaMapper;
import com.HeartShop.mapper.ProductQaMapper;
import com.HeartShop.service.MemberService;
import com.HeartShop.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員控制器
 */
@Slf4j
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final ProductQaMapper productQaMapper;
    private final OrderQaMapper orderQaMapper;

    public MemberController(MemberService memberService, JwtUtil jwtUtil,
                            ProductQaMapper productQaMapper, OrderQaMapper orderQaMapper) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.productQaMapper = productQaMapper;
        this.orderQaMapper = orderQaMapper;
    }

    /**
     * 會員登入
     * POST /api/members/login
     *
     * @param request 登入請求（email + password）
     * @return ApiResponse<LoginResponse> 包含 token 和會員資料
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登入請求：{}", request.getEmail());
        LoginResponse response = memberService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 會員註冊
     * POST /api/members/register
     *
     * @param request 註冊請求（email + name + password）
     * @return ApiResponse<MemberDTO> 新註冊的會員資料
     */
    @PostMapping("/register")
    public ApiResponse<MemberDTO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到註冊請求：{}", request.getEmail());
        MemberDTO member = memberService.register(request);
        return ApiResponse.success("註冊成功", member);
    }

    /**
     * 更新會員資料
     * PUT /api/members/profile
     *
     * @param token   JWT Token (從 Authorization header 取得)
     * @param request 更新請求
     * @return ApiResponse<MemberDTO> 更新後的會員資料
     */
    @PutMapping("/profile")
    public ApiResponse<MemberDTO> updateProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        log.info("收到更新會員資料請求：memberId={}", memberId);
        MemberDTO updatedMember = memberService.updateProfile(memberId, request);
        return ApiResponse.success("更新成功", updatedMember);
    }

    /**
     * 取得當前登入會員資料（含最新 role）
     * GET /api/members/me
     */
    @GetMapping("/me")
    public ApiResponse<MemberDTO> getMe(@RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        MemberDTO dto = memberService.getMemberById(memberId);
        return ApiResponse.success(dto);
    }

    /**
     * 取得當前會員的商品提問紀錄
     * GET /api/members/me/qa
     */
    @GetMapping("/me/qa")
    public ApiResponse<List<ProductQa>> getMyQa(@RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        return ApiResponse.success(productQaMapper.findByMemberId(memberId));
    }

    /**
     * 取得當前會員的訂單提問紀錄
     * GET /api/members/me/order-qa
     */
    @GetMapping("/me/order-qa")
    public ApiResponse<List<OrderQa>> getMyOrderQa(@RequestHeader("Authorization") String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        return ApiResponse.success(orderQaMapper.findByMemberId(memberId));
    }

    /**
     * 修改密碼
     * PUT /api/members/password
     *
     * @param token   JWT Token (從 Authorization header 取得)
     * @param request 修改密碼請求
     * @return ApiResponse<String> 成功訊息
     */
    @PutMapping("/password")
    public ApiResponse<String> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        log.info("收到修改密碼請求：memberId={}", memberId);
        memberService.changePassword(memberId, request);
        return ApiResponse.success("密碼修改成功", null);
    }
}
