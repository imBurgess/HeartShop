package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.OrderQa;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.mapper.OrderQaMapper;
import com.HeartShop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderQaController {

    private final OrderQaMapper orderQaMapper;
    private final JwtUtil jwtUtil;

    /** 取得訂單的公開問答 */
    @GetMapping("/{orderNo}/qa")
    public ApiResponse<List<OrderQa>> getQa(@PathVariable String orderNo) {
        return ApiResponse.success(orderQaMapper.findByOrderNo(orderNo));
    }

    /** 登入會員提交訂單問題 */
    @PostMapping("/{orderNo}/qa")
    public ApiResponse<OrderQa> addQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo,
            @RequestBody Map<String, String> body) {

        Long memberId = jwtUtil.getMemberIdFromToken(token);
        String question = body.get("question");

        if (!StringUtils.hasText(question)) {
            throw new BusinessException("4000", "問題不能為空");
        }

        OrderQa qa = new OrderQa();
        qa.setOrderNo(orderNo);
        qa.setMemberId(memberId);
        qa.setQuestion(question.trim());
        qa.setIsPublic(true);
        orderQaMapper.insert(qa);

        return ApiResponse.success("提問成功", qa);
    }
}
