package com.housekeeping.service;

import com.housekeeping.DTO.ClothDTO;
import com.housekeeping.entity.Cloth;
import com.housekeeping.entity.User;
import com.housekeeping.repository.ClothRepository;
import com.housekeeping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClothService {

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 주입

    @Autowired
    private UserService userService; // UserRepository 주입

    public List<ClothDTO> getClothes(String name, String category, String details) {
        // Cloth 엔티티 목록을 조회
        List<Cloth> clothes = clothRepository.findAll();
        // Cloth 엔티티 목록을 ClothDTO 목록으로 변환하여 반환
        return clothes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

//    public ClothDTO saveCloth(ClothDTO clothDTO) {
//        Long userId = clothDTO.getUserId();
//        System.out.println("받고있는 userId: " + userId); // 디버깅을 위한 로그 추가
//        User user = userService.getUserById(userId);
//
//        // 유저가 없는 경우에 대한 예외 처리
//        if (user == null) {
//            throw new IllegalArgumentException("User with ID " + userId + " not found in the system.");
//        }
public ClothDTO saveCloth(ClothDTO clothDTO) {
    try {
        // User 엔티티를 조회
        User user = userRepository.findById(clothDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid userId: " + clothDTO.getUserId()));

        // ClothDTO를 Cloth 엔티티로 변환
        Cloth cloth = toEntity(clothDTO, user);
        // Cloth 엔티티 저장
        Cloth savedCloth = clothRepository.save(cloth);
        // 저장된 Cloth 엔티티를 ClothDTO로 변환하여 반환
        return toDTO(savedCloth);
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Failed to save Cloth. Error: " + e.getMessage(), e);
    } catch (Exception e) {
        throw new RuntimeException("Unexpected error occurred while saving Cloth: " + e.getMessage(), e);
    }
}


    private ClothDTO toDTO(Cloth cloth) {
        // Cloth 엔티티를 ClothDTO로 변환
        ClothDTO dto = new ClothDTO();
        dto.setClothId(cloth.getClothId());
        dto.setUserId(cloth.getUser().getUserId());
        dto.setClothName(cloth.getClothName());
        dto.setClothType(cloth.getClothType());
        dto.setClothColor(cloth.getClothColor());
        dto.setClothSeason(cloth.getClothSeason());
        dto.setClothCustomTag(cloth.getClothCustomTag());
        dto.setImageUrl(cloth.getImageUrl()); // 추가된 부분
        return dto;
    }

    private Cloth toEntity(ClothDTO clothDTO, User user) {
        // ClothDTO를 Cloth 엔티티로 변환
        Cloth cloth = new Cloth();
        cloth.setUser(user);
        cloth.setClothName(clothDTO.getClothName());
        cloth.setClothType(clothDTO.getClothType());
        cloth.setClothColor(clothDTO.getClothColor());
        cloth.setClothSeason(clothDTO.getClothSeason());
        cloth.setClothCustomTag(clothDTO.getClothCustomTag());
        cloth.setImageUrl(clothDTO.getImageUrl()); // 추가된 부분
        return cloth;
    }
}
