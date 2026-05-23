package com.HeartShop.controller;

import com.HeartShop.common.ApiResponse;
import com.HeartShop.entity.OrderQa;
import com.HeartShop.entity.ProductQa;
import com.HeartShop.exception.BusinessException;
import com.HeartShop.mapper.OrderQaMapper;
import com.HeartShop.mapper.ProductQaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/qa")
@RequiredArgsConstructor
public class AdminQaController {

    private final ProductQaMapper productQaMapper;
    private final OrderQaMapper orderQaMapper;

    // ── 商品問答 ────────────────────────────────────────────

    @GetMapping("/products")
    public ApiResponse<Map<String, Object>> listProductQa(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Boolean answered) {

        int offset = (page - 1) * pageSize;
        List<ProductQa> items = productQaMapper.adminFindAll(answered, offset, pageSize);
        long total = productQaMapper.adminCountAll(answered);

        return ApiResponse.success(Map.of(
                "items", items,
                "total", total,
                "page", page,
                "pageSize", pageSize
        ));
    }

    @PutMapping("/products/{qaId}/answer")
    public ApiResponse<Void> answerProductQa(
            @PathVariable Long qaId,
            @RequestBody Map<String, String> body) {

        String answer = body.get("answer");
        if (!StringUtils.hasText(answer)) {
            throw new BusinessException("4000", "回覆內容不能為空");
        }
        int updated = productQaMapper.updateAnswer(qaId, answer.trim());
        if (updated == 0) {
            throw new BusinessException("4004", "問題不存在");
        }
        return ApiResponse.success("回覆成功", null);
    }

    // ── 訂單問答 ────────────────────────────────────────────

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> listOrderQa(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Boolean answered) {

        int offset = (page - 1) * pageSize;
        List<OrderQa> items = orderQaMapper.adminFindAll(answered, offset, pageSize);
        long total = orderQaMapper.adminCountAll(answered);

        return ApiResponse.success(Map.of(
                "items", items,
                "total", total,
                "page", page,
                "pageSize", pageSize
        ));
    }

    @PutMapping("/orders/{qaId}/answer")
    public ApiResponse<Void> answerOrderQa(
            @PathVariable Long qaId,
            @RequestBody Map<String, String> body) {

        String answer = body.get("answer");
        if (!StringUtils.hasText(answer)) {
            throw new BusinessException("4000", "回覆內容不能為空");
        }
        int updated = orderQaMapper.updateAnswer(qaId, answer.trim());
        if (updated == 0) {
            throw new BusinessException("4004", "問題不存在");
        }
        return ApiResponse.success("回覆成功", null);
    }
}
