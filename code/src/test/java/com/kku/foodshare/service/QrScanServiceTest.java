package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kku.foodshare.service.impl.QrScanServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QrScanServiceTest {
  @Test
  void decodesFoodSharePassFromCameraImage() throws Exception {
    var matrix = new QRCodeWriter().encode("FS1:42:001234", BarcodeFormat.QR_CODE, 320, 320);
    var output = new ByteArrayOutputStream();
    ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "png", output);

    assertEquals("FS1:42:001234", new QrScanServiceImpl().decode(output.toByteArray()));
  }
}
