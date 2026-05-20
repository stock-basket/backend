package com.hanyahunya.stockbasket.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiResponseTest {

    @Test
    void ok_data만_성공_응답() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void ok_메시지와_data_성공_응답() {
        ApiResponse<Integer> response = ApiResponse.ok("생성 완료", 42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("생성 완료");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    void fail_실패_응답() {
        ApiResponse<Void> response = ApiResponse.fail("오류 발생");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("오류 발생");
        assertThat(response.getData()).isNull();
    }

    @Test
    void ok_null_data_허용() {
        ApiResponse<String> response = ApiResponse.ok((String) null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }
}
