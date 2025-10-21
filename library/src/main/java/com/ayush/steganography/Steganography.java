/*
 * Copyright (C) 2017  , All Rights Reserved.
 *
 * All information contained herein is, and remains the property of
 * and its suppliers, if any. The intellectual and technical concepts
 * contained herein are proprietary to and its suppliers and may be covered
 * by U.S. and Foreign Patents, patents in process, and are protected by
al trade secret or copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from .
 *
 *
 *
 *
 */

package com.ayush.steganography;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ayush on 20/11/17.
 */

public class Steganography {
    private static int BITS_PER_CHANNEL = 1;
    private static int width;
    private static int height;

    private static int[] getPixelData(Bitmap img) {
        width = img.getWidth();
        height = img.getHeight();

        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);

        return pixels;
    }


    /**
     * @param img
     * @param data
     * @return
     * @throws SteganographyException
     */
    public static Bitmap encode(Bitmap img, byte[] data) throws SteganographyException {
        // Todo check if data fits in image

        if (data.length <= 0) {
            throw new SteganographyException("No data to encode");
        }

        int[] pixels = getPixelData(img);

        // Convert data to list of booleans
        List<Boolean> dataBools = bytesToBoolList(data);

        // Add stopping bit
        for (int i = 0; i < 8; i++) {
            dataBools.add(false);
        }


        // Encode data into image
        pixels = encodeData(pixels, dataBools);

        return Bitmap.createBitmap(pixels, width, height, img.getConfig());
    }


    /**
     * @param img
     * @return
     * @throws SteganographyException
     */
    public static byte[] decode(Bitmap img) throws SteganographyException {
        // TODO check header if compressed / encrypted

        int[] pixels = getPixelData(img);

        List<Boolean> dataBools = decodeData(pixels);

        return boolListToBytes(dataBools);
    }

    private static int[] encodeData(int[] pixels, List<Boolean> data) {
        // TODO Check data fits into image

        // Data pointer
        int dataPtr = 0;

        // For each pixel
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int a = Color.alpha(p);
            int r = Color.red(p);
            int g = Color.green(p);
            int b = Color.blue(p);

            // Red Channel
            if (dataPtr < data.size()) {
                r = setLsb(r, data.get(dataPtr++));
            }
            // Green Channel
            if (dataPtr < data.size()) {
                g = setLsb(g, data.get(dataPtr++));
            }
            // Blue Channel
            if (dataPtr < data.size()) {
                b = setLsb(b, data.get(dataPtr++));
            }

            pixels[i] = Color.argb(a, r, g, b);

            if (dataPtr >= data.size()) {
                // Return if all data is encoded
                return pixels;
            }
        }
        return pixels;
    }


    private static List<Boolean> decodeData(int[] pixels) {
        List<Boolean> data = new ArrayList<>();
        // For each pixel
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int r = Color.red(p);
            int g = Color.green(p);
            int b = Color.blue(p);

            // Red Channel
            data.add(getLsb(r));
            // Green Channel
            data.add(getLsb(g));
            // Blue Channel
            data.add(getLsb(b));
        }

        return data;

    }


    /**
     * @param b
     * @param bit
     * @return
     */
    private static int setLsb(int b, boolean bit) {
        if (bit) {
            // Set if not set
            return b | 1;
        } else {
            // Unset if set
            return b & ~1;
        }
    }


    private static boolean getLsb(int b) {
        return (b & 1) == 1;
    }


    /**
     * @param bytes
     * @return
     */
    private static List<Boolean> bytesToBoolList(byte[] bytes) {
        ArrayList<Boolean> bools = new ArrayList<>();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                bools.add((b & (1 << i)) != 0);
            }
        }
        return bools;
    }


    /**
     * @param bits
     * @return
     * @throws SteganographyException
     */
    private static byte[] boolListToBytes(List<Boolean> bits) throws SteganographyException {
        // TODO check for encrypted / compressed header
        // For now, scan until we see a stop byte (8 false bits)

        List<Byte> byteList = new ArrayList<>();
        int falseCounter = 0;

        for (int i = 0; i < bits.size() - 8; i = i + 8) {
            byte b = 0;
            falseCounter = 0;

            for (int j = 0; j < 8; j++) {
                if (bits.get(i + j)) {
                    b |= 1 << j;
                    falseCounter = 0;
                } else {
                    falseCounter++;
                }

                if (falseCounter >= 8) {
                    // Stop byte found
                    byte[] bytes = new byte[byteList.size()];
                    for (int k = 0; k < byteList.size(); k++) {
                        bytes[k] = byteList.get(k);
                    }
                    return bytes;
                }
            }

            byteList.add(b);
        }
        throw new SteganographyException("No stop byte found");
    }

    /**
     * @param bmp
     * @param file
     * @throws IOException
     */
    public static void bitmapToFile(Bitmap bmp, File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
