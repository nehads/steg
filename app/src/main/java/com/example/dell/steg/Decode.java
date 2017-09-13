package com.example.dell.steg;

/**
 * Created by DELL on 01-May-17.
 */
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.*;
public class Decode {
    private String strMessage;

    public String getString() {
        return strMessage;
    }

    // https://github.com/subc/steganography/blob/master/steganography/steganography.py
    public Decode(Bitmap b) {
        int x = b.getWidth();
        int y = b.getHeight();
        int counter = 0;
        List<Integer> result = new ArrayList<Integer>() {};

        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) {
                int pixel = b.getPixel(i, j);
                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);

                if (redValue % 8 == 1 &&
                        blueValue % 8 == 1 &&
                        greenValue % 8 == 1) {
                    result.add(counter);
                }

                counter++;
                counter = counter % 16;
            }
        }

        List<String> hex = new ArrayList<String>() {};
        for (int k = 0; k < result.size(); k++) {
            // radix hex
            String hexify = Integer.toString(result.get(k), 16);
            hex.add(hexify);
        }

        String hexAll = TextUtils.join("", hex);
        byte[] bytes = new byte[0];

        try {
            bytes = Hex.decodeHex(hexAll.toCharArray());
            strMessage = new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            strMessage = "There was an exception: " + e.getMessage();
        }
    }
}
