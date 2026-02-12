package com.vn.go_toeic.controller;

import com.vn.go_toeic.dto.req.ResetPasswordReq;
import com.vn.go_toeic.dto.req.UserRegisterReq;
import com.vn.go_toeic.exception.EmailAlreadyExistsException;
import com.vn.go_toeic.service.AuthService;
import com.vn.go_toeic.util.AlertMessage;
import com.vn.go_toeic.util.meta.LayoutMeta;
import com.vn.go_toeic.util.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${app.auth.verify-token-expire}")
    private int verifyTokenExpire;

    @GetMapping("/dang-nhap")
    public String getLoginPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/";
        }

        model.addAttribute("layoutMeta", new LayoutMeta("Đăng nhập", null, null));
        return "auth/login";
    }

    @GetMapping("/dang-ky")
    public String getRegisterPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/";
        }

        model.addAttribute("userRegisterReq", new UserRegisterReq());
        model.addAttribute("layoutMeta", new LayoutMeta("Đăng ký", null, null));
        return "auth/register";
    }

    @PostMapping("/dang-ky")
    public String handleRegisterUser(
            @Validated(ValidationSequence.class) @ModelAttribute("userRegisterReq") UserRegisterReq userRegisterReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        log.info("Controller: Request đăng ký tài khoản mới với email: {}", userRegisterReq.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Controller: Đăng ký thất bại - Lỗi validation ({} lỗi) cho email: {}",
                    bindingResult.getErrorCount(), userRegisterReq.getEmail());

            model.addAttribute("layoutMeta", new LayoutMeta("Đăng ký", null, null));
            return "auth/register";
        }

        if (!userRegisterReq.getPassword().equals(userRegisterReq.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu xác nhận không khớp.");
            log.warn("Controller: Đăng ký thất bại - Mật khẩu xác nhận không khớp cho email: {}", userRegisterReq.getEmail());

            model.addAttribute("layoutMeta", new LayoutMeta("Đăng ký", null, null));
            return "auth/register";
        }

        try {
            authService.registerAccount(userRegisterReq);

            log.info("Controller: Đăng ký thành công tài khoản với verified = false: {}", userRegisterReq.getEmail());

            redirectAttributes.addFlashAttribute("email", userRegisterReq.getEmail());
            return "redirect:/kiem-tra-email";

        } catch (EmailAlreadyExistsException e) {
            log.warn("Controller: Đăng ký thất bại - Email đã tồn tại và được kích hoạt: {}", userRegisterReq.getEmail());

            bindingResult.rejectValue("email", "Exist.registerReq.email", e.getMessage());
            return "auth/register";

        } catch (Exception e) {
            log.error("Controller: Lỗi hệ thống nghiêm trọng khi đăng ký cho email: {}", userRegisterReq.getEmail(), e);

            model.addAttribute("alertMessage",
                    AlertMessage.errorToast("Đã có lỗi xảy ra. Vui lòng thử lại."));
            model.addAttribute("layoutMeta", new LayoutMeta("Đăng ký", null, null));
            return "auth/register";
        }
    }

    @GetMapping("/kiem-tra-email")
    public String checkEmailPage(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/";
        }

        if (!model.containsAttribute("type")) {
            model.addAttribute("type", "VERIFY");
        }

        model.addAttribute("layoutMeta", new LayoutMeta("Kiểm tra email", null, null));
        model.addAttribute("verifyTokenExpire", verifyTokenExpire);

        return "auth/check-email";
    }

    @GetMapping("/kich-hoat-tai-khoan")
    public String verifyAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        log.info("Controller: Nhận yêu cầu kích hoạt tài khoản với token (ẩn chi tiết): {}...",
                token.substring(0, Math.min(token.length(), 10)));

        try {
            authService.verifyAccount(token);
            log.info("Controller: Kích hoạt thành công, chuyển hướng về trang đăng nhập.");
            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.successSticky("Tài khoản đã được kích hoạt!"));
        } catch (Exception e) {
            log.warn("Controller: Kích hoạt thất bại. Lý do: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.errorSticky("Xác thực thất bại: " + e.getMessage()));
        }

        return "redirect:/dang-nhap";
    }

    @PostMapping("/dang-ky/gui-lai")
    public String resendToken(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        log.info("Controller: Nhận yêu cầu gửi lại email xác thực cho: {}", email);

        try {
            authService.resendVerificationToken(email);

            log.info("Controller: Gửi lại thành công cho email: {}", email);

            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.successToast("Email xác thực mới đã được gửi thành công!"));

        } catch (RuntimeException e) {
            log.warn("Controller: Gửi lại thất bại cho email {}. Lý do: {}", email, e.getMessage());

            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.errorToast(e.getMessage()));

        } catch (Exception e) {
            log.error("Controller: Lỗi hệ thống khi gửi lại email cho {}: ", email, e);

            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.errorToast("Đã có lỗi xảy ra. Vui lòng thử lại sau."));
        }

        redirectAttributes.addFlashAttribute("email", email);

        return "redirect:/kiem-tra-email";
    }

    @GetMapping("/quen-mat-khau")
    public String getForgotPasswordPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/";
        }
        model.addAttribute("layoutMeta", new LayoutMeta("Quên mật khẩu", null, null));
        return "auth/forgot-password";
    }

    @PostMapping("/quen-mat-khau")
    public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        log.info("Controller: Nhận yêu cầu quên mật khẩu cho email: {}", email);

        try {
            authService.processForgotPassword(email);

            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("type", "RESET_PASSWORD");

            return "redirect:/kiem-tra-email";

        } catch (RuntimeException e) {
            log.warn("Controller: Quên mật khẩu thất bại cho email {}. Lý do: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorSticky(e.getMessage()));
            return "redirect:/quen-mat-khau";

        } catch (Exception e) {
            log.error("Controller: Lỗi hệ thống khi quên mật khẩu: ", e);
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorSticky("Đã có lỗi xảy ra. Vui lòng thử lại."));
            return "redirect:/quen-mat-khau";
        }
    }

    @GetMapping("/dat-lai-mat-khau")
    public String getResetPasswordPage(@RequestParam(value = "token", required = false) String token,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorToast("Đường dẫn không hợp lệ."));
            return "redirect:/dang-nhap";
        }

        ResetPasswordReq req = new ResetPasswordReq();
        req.setToken(token);
        model.addAttribute("resetPasswordReq", req);
        model.addAttribute("layoutMeta", new LayoutMeta("Đặt lại mật khẩu", null, null));
        return "auth/reset-password";
    }

    @PostMapping("/dat-lai-mat-khau")
    public String handleResetPassword(
            @Validated(ValidationSequence.class) @ModelAttribute("resetPasswordReq") ResetPasswordReq req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("layoutMeta", new LayoutMeta("Đặt lại mật khẩu", null, null));
            return "auth/reset-password";
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.req", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("layoutMeta", new LayoutMeta("Đặt lại mật khẩu", null, null));
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(req.getToken(), req.getPassword());

            log.info("Controller: Đặt lại mật khẩu thành công.");
            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.successSticky("Đổi mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới."));
            return "redirect:/dang-nhap";

        } catch (RuntimeException e) {
            log.warn("Controller: Đặt lại mật khẩu thất bại: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorSticky(e.getMessage()));
            return "redirect:/dang-nhap";
        } catch (Exception e) {
            log.error("Controller: Lỗi hệ thống khi reset password: ", e);
            model.addAttribute("alertMessage", AlertMessage.errorToast("Lỗi hệ thống."));
            return "auth/reset-password";
        }
    }

    @PostMapping("/quen-mat-khau/gui-lai")
    public String resendForgotPasswordToken(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        log.info("Controller: Nhận yêu cầu GỬI LẠI email quên mật khẩu cho: {}", email);

        try {
            authService.processForgotPassword(email);

            log.info("Controller: Gửi lại email reset password thành công cho: {}", email);

            redirectAttributes.addFlashAttribute("alertMessage",
                    AlertMessage.successToast("Email hướng dẫn đã được gửi lại thành công!"));

        } catch (RuntimeException e) {
            log.warn("Controller: Gửi lại thất bại cho email {}. Lý do: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorToast(e.getMessage()));

        } catch (Exception e) {
            log.error("Controller: Lỗi hệ thống khi gửi lại email reset pass: ", e);
            redirectAttributes.addFlashAttribute("alertMessage", AlertMessage.errorToast("Đã có lỗi xảy ra. Vui lòng thử lại sau."));
        }

        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("type", "RESET_PASSWORD");

        return "redirect:/kiem-tra-email";
    }
}
