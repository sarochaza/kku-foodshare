package com.kku.foodshare.controller.api;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.service.QrScanService;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pickup")
public class PickupScanController {
  private final QrScanService scanner;

  public PickupScanController(QrScanService scanner) {
    this.scanner = scanner;
  }

  @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, String> scan(@RequestParam("frame") MultipartFile frame) {
    try {
      return Map.of("value", scanner.decode(frame.getBytes()));
    } catch (IOException exception) {
      throw new Problem(400, "อ่านภาพจากกล้องไม่สำเร็จ กรุณาลองอีกครั้ง");
    }
  }
}
