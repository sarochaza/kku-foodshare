package com.kku.foodshare.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.kku.foodshare.exception.Problem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;

@Service
public class QrScanService {
  private static final int MAX_BYTES = 2 * 1024 * 1024;
  private static final int MAX_SIDE = 4096;
  private static final long MAX_PIXELS = 10_000_000L;

  public String decode(byte[] bytes) {
    if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES)
      throw new Problem(400, "ภาพสำหรับสแกนต้องมีขนาดไม่เกิน 2 MB");

    try (ImageInputStream stream =
        ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (stream == null) throw invalidImage();
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw invalidImage();
      ImageReader reader = readers.next();
      try {
        reader.setInput(stream, true, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width < 1
            || height < 1
            || width > MAX_SIDE
            || height > MAX_SIDE
            || (long) width * height > MAX_PIXELS) throw invalidImage();

        var image = reader.read(0);
        var bitmap =
            new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new MultiFormatReader()
            .decode(
                bitmap,
                Map.of(
                    DecodeHintType.POSSIBLE_FORMATS,
                    List.of(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER,
                    Boolean.TRUE))
            .getText();
      } catch (NotFoundException exception) {
        throw new Problem(422, "ยังไม่พบ QR ในภาพ กรุณาเล็งกล้องให้นิ่งแล้วลองอีกครั้ง");
      } finally {
        reader.dispose();
      }
    } catch (IOException exception) {
      throw invalidImage();
    }
  }

  private Problem invalidImage() {
    return new Problem(400, "ไฟล์จากกล้องไม่ใช่ภาพ JPG หรือ PNG ที่อ่านได้");
  }
}
