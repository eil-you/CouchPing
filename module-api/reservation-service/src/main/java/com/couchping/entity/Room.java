package com.couchping.entity;

import com.couchping.model.AmenityType;
import com.couchping.model.PreferredGender;
import com.couchping.model.RoomType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hostId; // 호스트 ID

    @Column(nullable = false)
    private String title; // 숙소 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 숙소 상세 설명

    @Column
    private String imageUrl; // 숙소 대표 사진 경로

    @Column(nullable = false)
    private String location; // 대략적인 위치

    @Column(nullable = false)
    private Integer price; // 1박당 가격

    @Column(nullable = false)
    private Integer capacity; // 최대 수용 인원

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType; // COUCH, PRIVATE_ROOM, SHARED_ROOM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferredGender preferredGender; // ANY, MALE, FEMALE

    @ElementCollection(targetClass = AmenityType.class)
    @CollectionTable(name = "room_amenity", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity_type")
    private List<AmenityType> amenities = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String initialQuestion; // 호스트가 게스트에게 묻는 첫 질문

    @Column(nullable = false)
    private boolean isInstantBook = false; // 즉시 예약 허용 여부

    @Column(nullable = false)
    private boolean isActive = true; // 숙소 활성화 상태

    @Column(nullable = false)
    private boolean isDeleted = false; // 삭제 여부 (Soft Delete)

    @Column(nullable = false)
    private Double hostRating = 0.0; // 호스트의 최신 평점 (캐싱)

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> images = new ArrayList<>();

    @Builder
    public Room(Long hostId, String title, String description, String imageUrl, String location,
            Integer price, Integer capacity, String initialQuestion, RoomType roomType,
            PreferredGender preferredGender) {
        this.hostId = hostId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.price = price;
        this.capacity = capacity;
        this.initialQuestion = initialQuestion;
        this.roomType = roomType;
        this.preferredGender = preferredGender;
        this.initialQuestion = initialQuestion;
        this.isInstantBook = false; // 기본값
        this.isActive = true;
        this.isDeleted = false;
    }

    // 편의 메서드: 이미지 추가
    public void addImage(String imageUrl, int sortOrder) {
        RoomImage roomImage = RoomImage.builder()
                .room(this)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
        this.images.add(roomImage);
    }

    // 편의 메서드: 편의시설 추가
    public void addAmenity(AmenityType amenityType) {
        if (!this.amenities.contains(amenityType)) {
            this.amenities.add(amenityType);
        }
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public void updateInstantBook(boolean isInstantBook) {
        this.isInstantBook = isInstantBook;
    }

    public void delete() {
        this.isDeleted = true;
        this.isActive = false;
    }

    public void updateHostRating(Double hostRating) {
        this.hostRating = hostRating;
    }

    public void update(String title, String description, String imageUrl, String location,
            Integer price, Integer capacity, String initialQuestion,
            RoomType roomType, PreferredGender preferredGender,
            List<AmenityType> amenities) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.price = price;
        this.capacity = capacity;
        this.initialQuestion = initialQuestion;
        this.roomType = roomType;
        this.preferredGender = preferredGender;
        this.amenities.clear();
        if (amenities != null) {
            this.amenities.addAll(amenities);
        }
    }
}
