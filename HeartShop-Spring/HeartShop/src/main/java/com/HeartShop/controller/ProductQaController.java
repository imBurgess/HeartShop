package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.ProductQa;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.mapper.ProductQaMapper;
import com.HeartShop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductQaController {

    private final ProductQaMapper productQaMapper;
    private final JwtUtil jwtUtil;

    /** 公開查詢：取得商品所有公開問答 */
    @GetMapping("/{productId}/qa")
    public ApiResponse<List<ProductQa>> getQa(@PathVariable Long productId) {
        return ApiResponse.success(productQaMapper.findByProductId(productId));
    }

    /** 需登入：提交問題 */
    @PostMapping("/{productId}/qa")
    public ApiResponse<ProductQa> addQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody Map<String, String> body) {

        Long memberId = jwtUtil.getMemberIdFromToken(token);
        String question = body.get("question");

        if (!StringUtils.hasText(question)) {
            throw new BusinessException("4000", "問題不能為空");
        }

        ProductQa qa = new ProductQa();
        qa.setProductId(productId);
        qa.setMemberId(memberId);
        qa.setQuestion(question.trim());
        qa.setIsPublic(true);
        productQaMapper.insert(qa);

        return ApiResponse.success("提問成功", qa);
    }
}
