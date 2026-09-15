package com.example.RealMatch.global.common;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

public final class QueryLimits {
    private QueryLimits() {
    }

    public static void page(int page, int size) {
        if (page < 0 || page > 10000 || size < 1 || size > 100) {
            throw new CustomException(GeneralErrorCode.BAD_REQUEST);
        }
    }

    public static void text(String value, int max) {
        if (value != null && value.length() > max) {
            throw new CustomException(GeneralErrorCode.BAD_REQUEST);
        }
    }
}
