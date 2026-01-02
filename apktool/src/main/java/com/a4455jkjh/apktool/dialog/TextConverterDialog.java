package com.a4455jkjh.apktool.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.a4455jkjh.apktool.R;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;

public class TextConverterDialog {

    public static void show(Context context) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_text_converter, null);

        EditText input = view.findViewById(R.id.inputText);
        EditText output = view.findViewById(R.id.outputText);

        Spinner spinnerMode = view.findViewById(R.id.spinnerMode);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);

        Button btnProcess = view.findViewById(R.id.btnProcess);
        Button btnExit = view.findViewById(R.id.btnExit);

        String[] modeItems = {
                "Encode / Encrypt",
                "Decode / Decrypt"
        };

        String[] typeItems = {
                // Encoding
                "Base64",
                "Hex",
                "Unicode",
                "Java Escaped",
                "URL Encode",

                // Smali / Android
                "String ↔ Smali const-string",
                "Package ↔ Smali Path",

                // Crypto (1 arah)
                "MD5 Hash",
                "SHA-1 Hash",
                "SHA-256 Hash",

                // Util
                "Uppercase",
                "Lowercase",
                "Reverse",
                "Remove Whitespace",
                "Line ↔ \\n",
                "Add Quotes",
                "Remove Quotes"
        };

        spinnerMode.setAdapter(new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                modeItems
        ));

        spinnerType.setAdapter(new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                typeItems
        ));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        btnProcess.setOnClickListener(v -> {
            String in = input.getText().toString();
            int mode = spinnerMode.getSelectedItemPosition();
            int type = spinnerType.getSelectedItemPosition();
            output.setText(convert(in, mode, type));
        });

        btnExit.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * mode : 0 = encode, 1 = decode
     */
    private static String convert(String text, int mode, int type) {
        try {
            boolean encode = (mode == 0);

            switch (type) {

                // ================= BASE64 =================
                case 0:
                    return encode ? b64Encode(text) : b64Decode(text);

                // ================= HEX =================
                case 1:
                    return encode ? hexEncode(text) : hexDecode(text);

                // ================= UNICODE =================
                case 2:
                    return encode ? unicodeEncode(text) : unicodeDecode(text);

                // ================= JAVA ESCAPED =================
                case 3:
                    return encode ? javaEscape(text) : javaUnescape(text);

                // ================= URL =================
                case 4:
                    return encode ? urlEncode(text) : urlDecode(text);

                // ================= SMALI CONST-STRING =================
                case 5:
                    return encode
                            ? "const-string v0, \"" + javaEscape(text) + "\""
                            : text.replace("const-string v0, ", "")
                                  .replace("\"", "");

                // ================= PACKAGE ↔ SMALI =================
                case 6:
                    return encode
                            ? "L" + text.replace(".", "/") + ";"
                            : text.replace("L", "")
                                  .replace(";", "")
                                  .replace("/", ".");

                // ================= HASH (1 ARAH) =================
                case 7: return hash(text, "MD5");
                case 8: return hash(text, "SHA-1");
                case 9: return hash(text, "SHA-256");

                // ================= UTIL =================
                case 10: return text.toUpperCase();
                case 11: return text.toLowerCase();
                case 12: return new StringBuilder(text).reverse().toString();
                case 13: return text.replaceAll("\\s+", "");
                case 14:
                    return encode ? text.replace("\n", "\\n")
                                  : text.replace("\\n", "\n");
                case 15:
                    return encode ? "\"" + text + "\"" : text.replace("\"", "");
                case 16:
                    return text.replace("\"", "");

                default:
                    return text;
            }

        } catch (Throwable e) {
            return "Error!";
        }
    }

    // ===================== METHODS =====================

    private static String b64Encode(String s) {
        return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
    }

    private static String b64Decode(String s) {
        return new String(Base64.decode(s, Base64.NO_WRAP));
    }

    private static String hexEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hexDecode(String s) {
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(
                    s.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(data);
    }

    private static String unicodeEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(String.format("\\u%04x", (int) c));
        }
        return sb.toString();
    }

    private static String unicodeDecode(String s) {
        String[] parts = s.split("\\\\u");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.length() >= 4) {
                sb.append((char) Integer.parseInt(p.substring(0, 4), 16));
                sb.append(p.substring(4));
            }
        }
        return sb.toString();
    }

    private static String javaEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
    }

    private static String javaUnescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String urlEncode(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8");
    }

    private static String urlDecode(String s) throws Exception {
        return URLDecoder.decode(s, "UTF-8");
    }

    private static String hash(String s, String algo) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algo);
        byte[] b = md.digest(s.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
