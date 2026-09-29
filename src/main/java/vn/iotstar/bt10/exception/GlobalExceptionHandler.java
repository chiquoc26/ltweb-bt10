package vn.iotstar.bt10.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Email hoặc mật khẩu không chính xác."
        );
        errorDetail.setTitle("Xác thực thất bại");
        errorDetail.setType(URI.create("/errors/bad-credentials"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Yêu cầu xác thực. Vui lòng đính kèm token hợp lệ."
        );
        errorDetail.setTitle("Chưa được xác thực");
        errorDetail.setType(URI.create("/errors/unauthorized"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatusException(AccountStatusException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa."
        );
        errorDetail.setTitle("Tài khoản bị khóa");
        errorDetail.setType(URI.create("/errors/account-status"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập vào tài nguyên này."
        );
        errorDetail.setTitle("Truy cập bị từ chối");
        errorDetail.setType(URI.create("/errors/access-denied"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(SignatureException.class)
    public ProblemDetail handleSignatureException(SignatureException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Chữ ký JWT không hợp lệ hoặc đã bị thay đổi."
        );
        errorDetail.setTitle("Chữ ký Token không hợp lệ");
        errorDetail.setType(URI.create("/errors/invalid-token-signature"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwtException(ExpiredJwtException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Phiên đăng nhập (Token) đã hết hạn. Vui lòng đăng nhập lại."
        );
        errorDetail.setTitle("Token hết hạn");
        errorDetail.setType(URI.create("/errors/token-expired"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ProblemDetail handleMalformedJwtException(MalformedJwtException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Chuỗi định dạng JWT không đúng chuẩn."
        );
        errorDetail.setTitle("Token không hợp lệ");
        errorDetail.setType(URI.create("/errors/malformed-token"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        errorDetail.setTitle("Tham số không hợp lệ");
        errorDetail.setType(URI.create("/errors/bad-request"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu đầu vào không hợp lệ"
        );
        errorDetail.setTitle("Lỗi xác thực dữ liệu đầu vào");
        errorDetail.setType(URI.create("/errors/validation-failed"));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        errorDetail.setProperty("errors", fieldErrors);
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi nội bộ trên máy chủ."
        );
        errorDetail.setTitle("Lỗi máy chủ nội bộ");
        errorDetail.setType(URI.create("/errors/internal-server-error"));
        errorDetail.setProperty("timestamp", Instant.now());
        return errorDetail;
    }
}
