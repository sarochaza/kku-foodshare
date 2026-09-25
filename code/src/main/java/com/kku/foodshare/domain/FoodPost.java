package com.kku.foodshare.domain;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_post")
public class FoodPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @Column(name = "estimated_remaining", nullable = false)
    private Integer estimatedRemaining;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoodPostStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //เชื่อม FoodPost กับ PostImage
    @OneToMany(mappedBy = "foodPost",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PostImage> images = new ArrayList<>();

    public FoodPost() {}

    //before insert
    @PrePersist
    protected void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = FoodPostStatus.AVAILABLE;
        }
    }

    //before update
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

    //get & set
    public Long getPostId(){
        return postId;
    }
    public void setPostId(Long postId){
        this.postId = postId;
    }

    public Long getUserId(){
        return userId;
    }
    public void setUserId(Long userId){
        this.userId = userId;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public Integer getInitialQuantity(){
        return initialQuantity;
    }
    public void setInitialQuantity(Integer initialQuantity){
        this.initialQuantity = initialQuantity;
    }

    public Integer getEstimatedRemaining(){
        return estimatedRemaining;
    }
    public void setEstimatedRemaining(Integer estimatedRemaining){
        this.estimatedRemaining = estimatedRemaining;
    }

    public String getLocationName(){
        return locationName;
    }
    public void setLocationName(String locationName){
        this.locationName = locationName;
    }

    public BigDecimal getLatitude(){
        return latitude;
    }
    public void setLatitude(BigDecimal latitude){
        this.latitude = latitude;
    }

    public BigDecimal getLongitude(){
        return longitude;
    }
    public void setLongitude(BigDecimal longitude){
        this.longitude = longitude;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime){
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime(){
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime){
        this.endTime = endTime;
    }

    public FoodPostStatus getStatus(){
        return status;
    }
    public void setStatus(FoodPostStatus status){
        this.status = status;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    //get-set PostImage
    public List<PostImage> getImages(){
        return images;
    }
    public void setImages(List<PostImage> images){
        this.images = images;
    }
}
