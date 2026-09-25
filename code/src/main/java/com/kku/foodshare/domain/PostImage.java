package com.kku.foodshare.domain;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "post_image")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private FoodPost foodPost;

    @Column(name = "img_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    public PostImage(){}

    @PrePersist
    protected void onCreate(){
        createAt = LocalDateTime.now();
        if(displayOrder == null){
            displayOrder = 0;
        }
    }

    //get-set
    public Long getImageId(){
        return imageId;
    }
    public void setImageId(Long imageId){
        this.imageId = imageId;
    }

    public FoodPost getFoodPost(){
        return foodPost;
    }
    public void setFoodPost(FoodPost foodPost){
        this.foodPost = foodPost;
    }

    public String getImageUrl(){
        return imageUrl;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder(){
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder){
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt(){
        return createAt;
    }
    public void setCreatedAt(LocalDateTime createAt){
        this.createAt = createAt;
    }
}
