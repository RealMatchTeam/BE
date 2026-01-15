package com.example.RealMatch.global.presentation;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;
import com.example.RealMatch.global.presentation.code.BaseSuccessCode;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class CustomResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    @JsonProperty("code")
    private final String code;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("result")
    private T result;

    // 200 OK
    public static <T> CustomResponse<T> ok(T result) {
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }

    public static <T> CustomResponse<T> onSuccess(BaseSuccessCode code, T result) {
        return new CustomResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static <T> CustomResponse<T> onFailure(BaseErrorCode code, T result) {
        return new CustomResponse<>(false, code.getCode(), code.getMessage(), result);
    }
}
