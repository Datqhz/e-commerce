package com.example.my_app.utils;

import android.content.Context;

import com.example.my_app.constants.Constants;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentsUtil {

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    public static PaymentsClient createPaymentsClient(Context context) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT).build();
        return Wallet.getPaymentsClient(context, walletOptions);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put("parameters", new JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleGatewayMerchantId")
                );
    }

    private static JSONObject getDirectTokenizationSpecification()
            throws JSONException, RuntimeException {
        return new JSONObject()
                .put("type", "DIRECT")
                .put("parameters", new JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS));
    }

    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray(Constants.SUPPORTED_NETWORKS);
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray(Constants.SUPPORTED_METHODS);
    }

    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        return new JSONObject()
                .put("type", "CARD")
                .put("parameters", new JSONObject()
                        .put("allowedAuthMethods", getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", getAllowedCardNetworks())
                        .put("billingAddressRequired", true)
                        .put("billingAddressParameters", new JSONObject()
                                .put("format", "FULL")
                        )
                );
    }

    private static JSONObject getCardPaymentMethod() throws JSONException {
        return getBaseCardPaymentMethod()
                .put("tokenizationSpecification", getGatewayTokenizationSpecification());
    }

    public static JSONArray getAllowedPaymentMethods() throws JSONException {
        return new JSONArray().put(getCardPaymentMethod());
    }

    public static JSONObject getIsReadyToPayRequest() {
        try {
            return getBaseRequest()
                    .put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject getTransactionInfo(String price) throws JSONException {
        return new JSONObject()
                .put("totalPrice", price)
                .put("totalPriceStatus", "FINAL")
                .put("countryCode", Constants.COUNTRY_CODE)
                .put("currencyCode", Constants.CURRENCY_CODE)
                .put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE");
    }

    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", "Từ Khánh");
    }

    public static JSONObject getPaymentDataRequest(String price) {
        try {
            return PaymentsUtil.getBaseRequest()
                    .put("allowedPaymentMethods", getAllowedPaymentMethods())
                    .put("transactionInfo", getTransactionInfo(price))
                    .put("merchantInfo", getMerchantInfo())
                    .put("shippingAddressRequired", true)
                    .put("shippingAddressParameters", new JSONObject()
                            .put("phoneNumberRequired", false)
                            .put("allowedCountryCodes", new JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES))
                    );

        } catch (JSONException e) {
            return null;
        }
    }
}
