package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.request.RegisterRequest;
//อันนี้คือ interface ของ Service ก่อน

public interface UserService {
//ความหมายคือ เรากำหนดไว้ว่า UserService ต้องมีความสามารถในการสมัครสมาชิกผ่าน
    User register(RegisterRequest request);
}
/*เหตุผลที่เราไม่เขียน logic ไว้ใน interface เลย เพราะเราจะแยก implementation ออกมาอีกไฟล์หนึ่ง 
ตามแนวทาง DIP และ constructor injection ที่โจทย์ต้องการด้วย */