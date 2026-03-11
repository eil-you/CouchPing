package com.couchping.model;

import com.couchping.entity.Room;
import java.util.List;

public record RoomRequest(
        String title,
        String description,
        String imageUrl,
        String location,
        Integer price,
        Integer capacity,
        String initialQuestion,
        RoomType roomType,
        PreferredGender preferredGender,
        Boolean isInstantBook,
        List<AmenityType> amenities) {
    public Room toEntity(Long hostId) {
        Room room = Room.builder()
                .hostId(hostId)
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .location(location)
                .price(price)
                .capacity(capacity)
                .initialQuestion(initialQuestion)
                .roomType(roomType)
                .preferredGender(preferredGender)
                .build();

        if (isInstantBook != null) {
            room.updateInstantBook(isInstantBook);
        }

        if (amenities != null) {
            amenities.forEach(room::addAmenity);
        }

        return room;
    }
}
