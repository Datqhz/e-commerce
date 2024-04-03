package com.example.my_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.android.gms.auth.api.phone.SmsRetriever;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String messageBody = smsMessage.getMessageBody();
                        if (messageBody != null && messageBody.contains("Your Firebase App verification code is")) { // Change "Your OTP" to your SMS content
                            String otp = extractOtpFromMessage(messageBody);
                            if (otp != null) {
                                // Pass the OTP to your activity or fragment
                                Intent otpIntent = new Intent("otp_received");
                                otpIntent.putExtra("otp", otp);
                                context.sendBroadcast(otpIntent);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private String extractOtpFromMessage(String message) {
        // Define the regular expression pattern to match the OTP
        Pattern pattern = Pattern.compile("(\\d{6})"); // Matches a sequence of 6 digits
        Matcher matcher = pattern.matcher(message);

        // Check if the pattern is found in the message
        if (matcher.find()) {
            return matcher.group(0); // Returns the first match found
        } else {
            return null; // No match found
        }
    }
}
