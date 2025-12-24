package com.schmhj.microservice.platform.foundation;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A monadic container type representing a value of type {@code T} (Success) or
 * a failure with a {@link Throwable} (Error).
 * <p>
 * This class provides functional methods to transform, obtain, and recover from
 * values or errors.
 *
 * @param <T> the type of the value
 */
public sealed interface Result<T> permits Result.Success, Result.Error {

    /**
     * Represents a successful result containing a value.
     */
    record Success<T>(T value) implements Result<T> {
    }

    /**
     * Represents a failed result containing an error.
     */
    record Error<T>(Throwable error) implements Result<T> {
    }

    /**
     * Creates a success result.
     *
     * @param value the value
     * @param <T>   the type of the value
     * @return a Success result
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates an error result.
     *
     * @param error the error
     * @param <T>   the type of the value
     * @return an Error result
     */
    static <T> Result<T> error(Throwable error) {
        return new Error<>(error);
    }

    /**
     * Checks if this result is a success.
     *
     * @return true if success, false otherwise
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Checks if this result is an error.
     *
     * @return true if error, false otherwise
     */
    default boolean isError() {
        return this instanceof Error;
    }

    /**
     * Creates a result from a supplier execution. Captures any thrown exception as
     * an Error.
     *
     * @param value the supplier
     * @param <T>   the type of the value
     * @return a Success containing the supplied value, or an Error if supplier
     *         throws
     */
    static <T> Result<T> of(Supplier<T> value) {
        try {
            return success(value.get());
        } catch (Throwable e) {
            return error(e);
        }
    }

    /**
     * Returns the value if success, or throws the underlying error if failure.
     *
     * @return the value
     * @throws Throwable the error if this is a failure
     */
    default T getValueOrThrow() throws Throwable {
        return switch (this) {
            case Success<T> success -> success.value;
            case Error<T> error -> throw error.error;
        };
    }

    /**
     * Returns the value if success, or the default value if failure.
     *
     * @param defaultValue the value to return if this is an error
     * @return the value or default
     */
    default T getValueOrDefault(T defaultValue) {
        return switch (this) {
            case Success<T> success -> success.value;
            case Error<T> _ -> defaultValue;
            default -> defaultValue;
        };
    }

    /**
     * Maps the value if this is a success. If the mapper throws, returns an Error.
     *
     * @param mapper function to apply to the value
     * @param <U>    type of the new value
     * @return mapped result
     */
    default <U> Result<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T> success -> {
                try {
                    yield success(mapper.apply(success.value));
                } catch (Throwable e) {
                    yield error(e);
                }
            }
            case Error<T> error -> error(error.error);
        };
    }

    /**
     * FlatMaps the value if this is a success. If the mapper throws, returns an
     * Error.
     *
     * @param mapper function returning a result to apply to the value
     * @param <U>    type of the new value
     * @return mapped result
     */
    default <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        return switch (this) {
            case Success<T> success -> {
                try {
                    yield Objects.requireNonNull(mapper.apply(success.value));
                } catch (Throwable e) {
                    yield error(e);
                }
            }
            case Error<T> error -> error(error.error);
        };
    }

    /**
     * Maps the error if this is a failure.
     *
     * @param mapper function to apply to the error
     * @return result with mapped error (type T remains same)
     */
    default Result<T> mapError(Function<Throwable, Throwable> mapper) {
        if (this instanceof Error<T> error) {
            return error(mapper.apply(error.error));
        }
        return this;
    }

    /**
     * Performs the given action with the value if this is a success.
     *
     * @param action consumer of the value
     * @return this result
     */
    default Result<T> peek(Consumer<T> action) {
        if (this instanceof Success<T> success) {
            action.accept(success.value);
        }
        return this;
    }

    /**
     * Alias for {@link #peek(Consumer)}.
     */
    default Result<T> onSuccess(Consumer<T> action) {
        return peek(action);
    }

    /**
     * Performs the given action with the error if this is a failure.
     *
     * @param action consumer of the error
     * @return this result
     */
    default Result<T> peekError(Consumer<Throwable> action) {
        if (this instanceof Error<T> error) {
            action.accept(error.error);
        }
        return this;
    }

    /**
     * Alias for {@link #peekError(Consumer)}.
     */
    default Result<T> onFailure(Consumer<Throwable> action) {
        return peekError(action);
    }

    /**
     * Recovers from an error by returning a value generated from the error.
     *
     * @param recovery function to produce a value from the error
     * @return the value (either original success value or recovered value)
     */
    default T recover(Function<Throwable, T> recovery) {
        return switch (this) {
            case Success<T> success -> success.value;
            case Error<T> error -> recovery.apply(error.error);
        };
    }

    /**
     * Recovers from an error by returning a new Result generated from the error.
     *
     * @param recovery function to produce a new Result from the error
     * @return the result (either original success or recovered result)
     */
    default Result<T> recoverWith(Function<Throwable, Result<T>> recovery) {
        return switch (this) {
            case Success<T> _ -> this;
            case Error<T> error -> recovery.apply(error.error);
        };
    }

    /**
     * Filters a success value. If the predicate fails, returns an Error generated
     * by the supplier.
     *
     * @param predicate     predicate to test the value
     * @param errorSupplier supplier for the error if predicate fails
     * @return Success if predicate matches, otherwise Error
     */
    default Result<T> filter(Predicate<T> predicate, Supplier<Throwable> errorSupplier) {
        if (this instanceof Success<T> success) {
            if (predicate.test(success.value)) {
                return this;
            }
            return error(errorSupplier.get());
        }
        return this;
    }
}