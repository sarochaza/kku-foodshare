package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.SavedPost;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.dto.response.PostView;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.mapper.PostViewMapper;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.SavedPostRepository;
import com.kku.foodshare.service.MemberService;
import com.kku.foodshare.service.SavedPostService;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SavedPostServiceImpl implements SavedPostService {
  private final SavedPostRepository saved;
  private final FoodPostRepository posts;
  private final MemberService members;
  private final PostViewMapper mapper;
  private final Clock clock;

  public SavedPostServiceImpl(SavedPostRepository saved, FoodPostRepository posts, MemberService members, PostViewMapper mapper, Clock clock) {
    this.saved = saved; this.posts = posts; this.members = members; this.mapper = mapper; this.clock = clock;
  }

  public void save(String email, long postId) {
    var user = members.require(email);
    if (saved.existsByUserIdAndPostId(user.getId(), postId)) return;
    var post = posts.findById(postId).orElseThrow(Problem::missing);
    var entry = new SavedPost(); entry.setUser(user); entry.setPost(post); entry.setCreatedAt(LocalDateTime.now(clock));
    saved.save(entry);
  }

  public void remove(String email, long postId) {
    var user = members.require(email);
    saved.findByUserIdAndPostId(user.getId(), postId).ifPresent(saved::delete);
  }

  @Transactional(readOnly = true)
  public PageView<PostView> mine(String email, int page) {
    if (page < 0 || page > 10000) throw new Problem(400, "หน้าข้อมูลไม่ถูกต้อง");
    var user = members.require(email);
    return PageView.of(saved.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, 12))
        .map(entry -> mapper.map(entry.getPost(), email, null, null)));
  }
}
