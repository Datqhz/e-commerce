package com.example.my_app.models;

public enum PaymentMethodData {
    CASH_ON_DELIVERY("Thanh toán sau khi nhận hàng"),
    MOMO("Ví MOMO"),
    QR_CODE("QR Code"),
    GPAY("Ví Google Pay");

    private final String displayName;

    PaymentMethodData(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
