package com.book.BookProject.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentInfo {

    @JsonProperty("imp_uid")  // JSON 직렬화 시 필드명 지정
    private String impUid;

    @JsonProperty("merchant_uid")  // JSON 직렬화 시 필드명 지정
    private String merchantUid;

    @JsonProperty("amount")
    private int amount;

    public PaymentInfo(String impUid, String merchantUid, int amount) {
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.amount = amount;
    }

    public String getImpUid() {
        return impUid;
    }

    public String getMerchantUid() {
        return merchantUid;
    }

    public int getAmount() {
        return amount;
    }
}