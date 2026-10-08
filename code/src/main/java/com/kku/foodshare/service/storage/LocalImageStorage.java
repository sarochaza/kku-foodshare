package com.kku.foodshare.service.storage;

import com.kku.foodshare.exception.Problem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageStorage implements ImageStorage {
  private final Path base;

  public LocalImageStorage(@Value("${app.upload-dir:./uploads}") String path) {
    base = Path.of(path).toAbsolutePath().normalize();
  }

  public String store(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new Problem(400, "กรุณาเลือกรูป JPG หรือ PNG ขนาดไม่เกิน 5 MB");
    try (InputStream in = file.getInputStream();
        ImageInputStream stream = ImageIO.createImageInputStream(in)) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new Problem(400, "ไฟล์นี้ไม่ใช่รูปภาพที่รองรับ");
      ImageReader reader = readers.next();
      BufferedImage source;
      try {
        reader.setInput(stream);
        String type = reader.getFormatName();
        int w = reader.getWidth(0), h = reader.getHeight(0);
        if (!(type.equalsIgnoreCase("PNG") || type.equalsIgnoreCase("JPEG"))
            || w < 1
            || h < 1
            || w > 6000
            || h > 6000
            || (long) w * h > 20000000)
          throw new Problem(400, "รองรับ JPG/PNG ไม่เกิน 20 ล้านพิกเซล");
        source = reader.read(0);
      } finally {
        reader.dispose();
      }
      double scale = Math.min(1, 1400.0 / Math.max(source.getWidth(), source.getHeight()));
      BufferedImage out =
          new BufferedImage(
              Math.max(1, (int) (source.getWidth() * scale)),
              Math.max(1, (int) (source.getHeight() * scale)),
              BufferedImage.TYPE_INT_RGB);
      Graphics2D g = out.createGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, out.getWidth(), out.getHeight());
      g.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(source, 0, 0, out.getWidth(), out.getHeight(), null);
      g.dispose();
      Files.createDirectories(base);
      String name = UUID.randomUUID() + ".jpg";
      Path dest = base.resolve(name);
      try (OutputStream output = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW)) {
        if (!ImageIO.write(out, "jpg", output)) throw new IOException("JPEG encoder unavailable");
      }
      return name;
    } catch (IOException e) {
      throw new Problem(400, "อ่านหรือบันทึกรูปไม่สำเร็จ กรุณาลองใหม่");
    }
  }

  public Resource load(String name) {
    if (!name.matches("[0-9a-f-]{36}\\.jpg")) throw Problem.missing();
    Path p = base.resolve(name);
    if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) throw Problem.missing();
    return new FileSystemResource(p);
  }

  public void remove(String name) {
    if (name != null && name.matches("[0-9a-f-]{36}\\.jpg"))
      try {
        Files.deleteIfExists(base.resolve(name));
      } catch (IOException e) {
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("Unable to remove image {}", name);
      }
  }
}
