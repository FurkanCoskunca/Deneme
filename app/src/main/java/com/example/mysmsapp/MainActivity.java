package com.example.mysmsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private ArrayList<String> clickableItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML'den TextView nesnesini al
        myTextView = findViewById(R.id.textView);
        // Tıklanabilir bağlantıları etkinleştir
        myTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // SMS okuma izni talep et
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
    }

    // SMS'leri okumak için çağrılan metot
    public void Read_SMS(View view) {
        // Tıklanabilir olmayan ve tıklanabilir olan öğeleri saklamak için StringBuilders oluştur
        StringBuilder allNonClickableItemsBuilder = new StringBuilder();
        StringBuilder allClickableItemsBuilder = new StringBuilder();
        readAllSMS(allNonClickableItemsBuilder, allClickableItemsBuilder);

        // Tıklanabilir bağlantıları önce göster
        for (String clickableItem : clickableItemsList) {
            allClickableItemsBuilder.append(clickableItem).append("\n");
        }

        // Tıklanabilir olmayan öğeleri eklemek
        allClickableItemsBuilder.append(allNonClickableItemsBuilder);

        // TextView'a sonuçları göster
        myTextView.setText(allClickableItemsBuilder.toString());
    }

    // Tüm SMS'leri oku ve içerikleri ayır
    private void readAllSMS(StringBuilder allNonClickableItemsBuilder, StringBuilder allClickableItemsBuilder) {
        // SMS içeriklerini almak için Uri oluştur
        Uri uri = Uri.parse("content://sms");
        // Cursor ile veritabanını sorgula
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int bodyIndex = cursor.getColumnIndex("body");
            int addressIndex = cursor.getColumnIndex("address");

            do {
                // SMS göndereni ve içeriğini al
                String senderAddress = cursor.getString(addressIndex);
                String messageBody = cursor.getString(bodyIndex);
                // İçerikteki öğeleri ayır ve uygun StringBuilders'a ekle
                findAndAppendItems(messageBody, senderAddress, allNonClickableItemsBuilder, allClickableItemsBuilder);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    // SMS içeriğindeki öğeleri ayır ve ilgili StringBuilders'a ekle
    private void findAndAppendItems(String text, String sender, StringBuilder allNonClickableItemsBuilder, StringBuilder allClickableItemsBuilder) {
        // Tıklanabilir bağlantıları tespit etmek için regex deseni
        String clickablePattern = "\\b(?i)((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)" +
                "(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+" +
                "(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))|([+0-9()\\-\\s]{7,}|\\d{5,})|" +
                "(?:(?<=\\s|\\.|\\()([a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,})(?=\\s|[.,?!]|\\)|$))\\b";

        // Deseni derle
        Pattern clickableRegex = Pattern.compile(clickablePattern);

        // İçerikteki deseni bul ve eşleşen öğeleri tespit etmek için Matcher kullanma
        Matcher clickableMatcher = clickableRegex.matcher(text);

        while (clickableMatcher.find()) {
            // Bulunan tıklanabilir öğeyi sakla
            String clickableItem = clickableMatcher.group();
            clickableItemsList.add("From: " + sender + "  Clickable: " + clickableItem);
        }

        // Tıklanabilir olmayan öğeleri oluşturulan StringBuilders'a ekle
        allNonClickableItemsBuilder.append("From: ").append(sender).append("  Non-Clickable: ").append(text).append("\n");
    }

    // İzin talep sonucunu işle
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildiyse SMS'leri oku ve göster
                Read_SMS(myTextView);
                // Gerekli işlemleri burada gerçekleştir
            }
            else {
                // SMS izni reddedildiyse, kullanıcıya uygun bir geri bildirimde bulun
                myTextView.setText("SMS izni reddedildi. Uygulamayı kullanabilmek için SMS izni gereklidir.");
            }
        }
    }
}
