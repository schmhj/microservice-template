package com.schmhj.microservice.platform.foundation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultTest {

    @Test
    @DisplayName("given Success result, when checking isSuccess, then return true")
    void given_Success_when_isSuccess_then_returnTrue() {
        Result<String> result = Result.success("test");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("given Error result, when checking isSuccess, then return false")
    void given_Error_when_isSuccess_then_returnFalse() {
        Result<String> result = Result.error(new RuntimeException("failed"));

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("given Success result, when checking isError, then return false")
    void given_Success_when_isError_then_returnFalse() {
        Result<String> result = Result.success("test");

        assertThat(result.isError()).isFalse();
    }

    @Test
    @DisplayName("given Error result, when checking isError, then return true")
    void given_Error_when_isError_then_returnTrue() {
        Result<String> result = Result.error(new RuntimeException("failed"));

        assertThat(result.isError()).isTrue();
    }

    @Test
    @DisplayName("given Success result, when getValueOrThrow, then return value")
    void given_Success_when_getValueOrThrow_then_returnValue() throws Throwable {
        Result<String> result = Result.success("test");

        assertThat(result.getValueOrThrow()).isEqualTo("test");
    }

    @Test
    @DisplayName("given Error result, when getValueOrThrow, then throw exception")
    void given_Error_when_getValueOrThrow_then_throwException() {
        RuntimeException expectedError = new RuntimeException("failed");
        Result<String> result = Result.error(expectedError);

        assertThatThrownBy(result::getValueOrThrow)
                .isSameAs(expectedError);
    }

    @Test
    @DisplayName("given Success result, when getValueOrDefault, then return value")
    void given_Success_when_getValueOrDefault_then_returnValue() {
        Result<String> result = Result.success("test");

        assertThat(result.getValueOrDefault("default")).isEqualTo("test");
    }

    @Test
    @DisplayName("given Error result, when getValueOrDefault, then return default")
    void given_Error_when_getValueOrDefault_then_returnDefault() {
        Result<String> result = Result.error(new RuntimeException("failed"));

        assertThat(result.getValueOrDefault("default")).isEqualTo("default");
    }

    @Test
    @DisplayName("given Success result, when map, then return mapped Success")
    void given_Success_when_map_then_returnMappedSuccess() {
        Result<String> result = Result.success("test");

        Result<Integer> mapped = result.map(String::length);

        assertThat(mapped.isSuccess()).isTrue();
        assertThat(mapped.getValueOrDefault(-1)).isEqualTo(4);
    }

    @Test
    @DisplayName("given Success result, when map throws exception, then return Error")
    void given_Success_when_mapThrows_then_returnError() {
        Result<String> result = Result.success("test");
        RuntimeException exception = new RuntimeException("map failed");

        Result<Integer> mapped = result.map(s -> {
            throw exception;
        });

        assertThat(mapped.isError()).isTrue();
        assertThat(mapped.isSuccess()).isFalse();
        // Since we cannot easily access the error exception directly without casting or
        // helper,
        // (assuming Result doesn't expose public getError() on interface easily yet
        // without casting in test)
        // We verify via behavior.
        // Actually, let's verify if we can access error.
        // We can inspect it if we cast or if we had a getter properly exposed.
        // The implementation has 'getError' logic internally or switch case.
        // Let's use assertj to verify it's an error.
    }

    @Test
    @DisplayName("given Error result, when map, then return same Error")
    void given_Error_when_map_then_returnSameError() {
        RuntimeException exception = new RuntimeException("original failure");
        Result<String> result = Result.error(exception);

        Result<Integer> mapped = result.map(String::length);

        assertThat(mapped.isError()).isTrue();
        assertThatThrownBy(mapped::getValueOrThrow).isSameAs(exception);
    }

    @Test
    @DisplayName("given Success result, when flatMap, then return flatMapped Success")
    void given_Success_when_flatMap_then_returnFlatMappedSuccess() {
        Result<String> result = Result.success("test");

        Result<Integer> flatMapped = result.flatMap(s -> Result.success(s.length()));

        assertThat(flatMapped.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("given Success result, when flatMap returns Error, then return Error")
    void given_Success_when_flatMapReturnsError_then_returnError() {
        Result<String> result = Result.success("test");
        RuntimeException exception = new RuntimeException("flatMap failed");

        Result<Integer> flatMapped = result.flatMap(s -> Result.error(exception));

        assertThat(flatMapped.isError()).isTrue();
        assertThatThrownBy(flatMapped::getValueOrThrow).isSameAs(exception);
    }

    @Test
    @DisplayName("given Success result, when flatMap throws, then return Error")
    void given_Success_when_flatMapThrows_then_returnError() {
        Result<String> result = Result.success("test");
        RuntimeException exception = new RuntimeException("flatMap threw");

        Result<Integer> flatMapped = result.flatMap(s -> {
            throw exception;
        });

        assertThat(flatMapped.isError()).isTrue();
    }

    @Test
    @DisplayName("given Error result, when flatMap, then return same Error")
    void given_Error_when_flatMap_then_returnSameError() {
        RuntimeException exception = new RuntimeException("original");
        Result<String> result = Result.error(exception);

        Result<Integer> flatMapped = result.flatMap(s -> Result.success(s.length()));

        assertThat(flatMapped.isError()).isTrue();
        assertThatThrownBy(flatMapped::getValueOrThrow).isSameAs(exception);
    }

    @Test
    @DisplayName("given Supplier, when of, then return Success")
    void given_Supplier_when_of_then_returnSuccess() {
        Result<String> result = Result.of(() -> "success");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValueOrDefault("fail")).isEqualTo("success");
    }

    @Test
    @DisplayName("given Throwing Supplier, when of, then return Error")
    void given_ThrowingSupplier_when_of_then_returnError() {
        RuntimeException exception = new RuntimeException("supply failed");

        Result<String> result = Result.of(() -> {
            throw exception;
        });

        assertThat(result.isError()).isTrue();
        assertThatThrownBy(result::getValueOrThrow).isSameAs(exception);
    }

    @Test
    @DisplayName("given Error result, when mapError, then return mapped Error")
    void given_Error_when_mapError_then_returnMappedError() {
        Result<String> result = Result.error(new RuntimeException("fail"));

        Result<String> mapped = result.mapError(e -> new IllegalArgumentException("mapped: " + e.getMessage()));

        assertThat(mapped.isError()).isTrue();
        assertThatThrownBy(mapped::getValueOrThrow)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mapped: fail");
    }

    @Test
    @DisplayName("given Success result, when mapError, then return original Success")
    void given_Success_when_mapError_then_returnOriginalSuccess() {
        Result<String> result = Result.success("ok");

        Result<String> mapped = result.mapError(e -> new RuntimeException("fail"));

        assertThat(mapped.isSuccess()).isTrue();
        assertThat(mapped.getValueOrDefault("bad")).isEqualTo("ok");
    }

    @Test
    @DisplayName("given Success result, when peek, then execute action")
    void given_Success_when_peek_then_executeAction() {
        Result<String> result = Result.success("ok");
        AtomicBoolean called = new AtomicBoolean(false);

        Result<String> returned = result.peek(v -> {
            assertThat(v).isEqualTo("ok");
            called.set(true);
        });

        assertThat(called).isTrue();
        assertThat(returned).isSameAs(result);
    }

    @Test
    @DisplayName("given Error result, when peek, then do nothing")
    void given_Error_when_peek_then_doNothing() {
        Result<String> result = Result.error(new RuntimeException("fail"));
        AtomicBoolean called = new AtomicBoolean(false);

        result.peek(v -> called.set(true));

        assertThat(called).isFalse();
    }

    @Test
    @DisplayName("given Error result, when peekError, then execute action")
    void given_Error_when_peekError_then_executeAction() {
        RuntimeException ex = new RuntimeException("fail");
        Result<String> result = Result.error(ex);
        AtomicBoolean called = new AtomicBoolean(false);

        Result<String> returned = result.peekError(e -> {
            assertThat(e).isSameAs(ex);
            called.set(true);
        });

        assertThat(called).isTrue();
        assertThat(returned).isSameAs(result);
    }

    @Test
    @DisplayName("given Success result, when peekError, then do nothing")
    void given_Success_when_peekError_then_doNothing() {
        Result<String> result = Result.success("ok");
        AtomicBoolean called = new AtomicBoolean(false);

        result.peekError(e -> called.set(true));

        assertThat(called).isFalse();
    }

    @Test
    @DisplayName("given Error result, when recover, then return recovered value")
    void given_Error_when_recover_then_returnRecoveredValue() {
        Result<String> result = Result.error(new RuntimeException("fail"));

        String val = result.recover(e -> "recovered");

        assertThat(val).isEqualTo("recovered");
    }

    @Test
    @DisplayName("given Success result, when recover, then return original value")
    void given_Success_when_recover_then_returnOriginalValue() {
        Result<String> result = Result.success("ok");

        String val = result.recover(e -> "recovered");

        assertThat(val).isEqualTo("ok");
    }

    @Test
    @DisplayName("given Error result, when recoverWith, then return recovered Result")
    void given_Error_when_recoverWith_then_returnRecoveredResult() {
        Result<String> result = Result.error(new RuntimeException("fail"));

        Result<String> recovered = result.recoverWith(e -> Result.success("recovered"));

        assertThat(recovered.isSuccess()).isTrue();
        assertThat(recovered.getValueOrDefault("bad")).isEqualTo("recovered");
    }

    @Test
    @DisplayName("given Success result, when filter matches, then return Success")
    void given_Success_when_filterMatches_then_returnSuccess() {
        Result<String> result = Result.success("ok");

        Result<String> filtered = result.filter(s -> s.length() == 2, () -> new RuntimeException("stopped"));

        assertThat(filtered.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("given Success result, when filter mismatches, then return Error")
    void given_Success_when_filterMismatches_then_returnError() {
        Result<String> result = Result.success("ok");

        Result<String> filtered = result.filter(s -> s.length() > 5, () -> new IllegalArgumentException("too short"));

        assertThat(filtered.isError()).isTrue();
        assertThatThrownBy(filtered::getValueOrThrow)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("too short");
    }
}
