package com.HeartShop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量至少為1")
    private Integer quantity;
}
