package com.vn.go_toeic.util;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage implements Serializable {
    private String message;
    private String type;  // "success", "error", "warning", "info"
    private String style; // "toast", "modal", "sticky"

    // Create toast
    public static AlertMessage toast(String message, String type) {
        return new AlertMessage(message, type, "toast");
    }

    // Create modal
    public static AlertMessage modal(String message, String type) {
        return new AlertMessage(message, type, "modal");
    }

    // Create sticky
    public static AlertMessage sticky(String message, String type) {
        return new AlertMessage(message, type, "sticky");
    }

    // TOAST
    public static AlertMessage successToast(String message) { return toast(message, "success"); }
    public static AlertMessage errorToast(String message) { return toast(message, "error"); }
    public static AlertMessage warningToast(String message) { return toast(message, "warning"); }
    public static AlertMessage infoToast(String message) { return toast(message, "info"); }

    // STICKY
    public static AlertMessage successSticky(String message) { return sticky(message, "success"); }
    public static AlertMessage errorSticky(String message) { return sticky(message, "error"); }
    public static AlertMessage warningSticky(String message) { return sticky(message, "warning"); }
    public static AlertMessage infoSticky(String message) { return sticky(message, "info"); }

    // MODAL
    public static AlertMessage successModal(String message) { return modal(message, "success"); }
    public static AlertMessage errorModal(String message) { return modal(message, "error"); }
}