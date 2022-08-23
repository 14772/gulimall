package com.su.gulimall.product.exception;

import com.su.common.exception.BizCodeEnume;
import com.su.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.su.common.exception.BizCodeEnume.UNKNOWN_EXCEPTION;
import static com.su.common.exception.BizCodeEnume.VALID_EXCEPTION;

@Slf4j
@RestControllerAdvice(basePackages = "com.su.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验异常：{}，异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(VALID_EXCEPTION.getCode(), VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("未知异常：", throwable);
        return R.error(UNKNOWN_EXCEPTION.getCode(), UNKNOWN_EXCEPTION.getMsg());
    }
}
