package com.example.ai_droid;

import android.content.Intent;
import android.net.Uri;

public class JoinNow {

    public Intent join_now() {
        String subject = Uri.encode("Interested for joining club");
        String body = Uri.encode("**** Please Fill the following details: ****\nName: <name>\nBranch: CSM <section>\nRoll No: <roll>\nPhone no. : <phone_no>");
        Uri uri = Uri.parse("mailto:gurupreetambodapati@gmail.com?subject="+subject+"&body="+body);
        Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
        return intent;
    }
}
