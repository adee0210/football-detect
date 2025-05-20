package com.loopy.footballvideoprocessor.dashboard.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.common.exception.ResourceNotFoundException;
import com.loopy.footballvideoprocessor.dashboard.dto.DashboardSummary;
import com.loopy.footballvideoprocessor.dashboard.dto.UserStats;
import com.loopy.footballvideoprocessor.dashboard.dto.VideoStats;
import com.loopy.footballvideoprocessor.dashboard.repository.DashboardStatsRepository;
import com.loopy.footballvideoprocessor.user.dto.UserDTO;
import com.loopy.footballvideoprocessor.user.dto.UserUpdateDTO;
import com.loopy.footballvideoprocessor.user.model.Role;
import com.loopy.footballvideoprocessor.user.model.User;
import com.loopy.footballvideoprocessor.user.repository.RoleRepository;
import com.loopy.footballvideoprocessor.user.repository.UserRepository;
import com.loopy.footballvideoprocessor.user.service.UserService;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.mapper.VideoMapper;
import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.repository.VideoRepository;
import com.loopy.footballvideoprocessor.video.service.VideoService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final VideoRepository videoRepository;
        private final DashboardStatsRepository dashboardStatsRepository;
        private final UserService userService;
        private final VideoService videoService;
        private final VideoMapper videoMapper;
        private final EntityManager entityManager;

        @Override
        @Transactional(readOnly = true)
        public DashboardSummary getDashboardSummary() {
                log.debug("Lấy thông tin tổng quan cho Admin Dashboard");

                // Lấy tổng số người dùng
                long totalUsers = userRepository.count();

                // Lấy số người dùng mới trong 30 ngày qua
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                long newUsersLast30Days = userRepository.findAll().stream()
                                .filter(user -> user.getCreatedAt().isAfter(thirtyDaysAgo))
                                .count();

                // Lấy tổng số video
                long totalVideos = videoRepository.count();

                // Lấy số video được xử lý trong 30 ngày qua
                long processedVideosLast30Days = videoRepository.findAll().stream()
                                .filter(video -> video.getStatus() == VideoStatus.COMPLETED)
                                .filter(video -> video.getUpdatedAt().isAfter(thirtyDaysAgo))
                                .count();

                // Lấy tổng dung lượng lưu trữ đã sử dụng
                long totalStorageUsed = videoRepository.findAll().stream()
                                .filter(video -> video.getFileSize() != null)
                                .mapToLong(Video::getFileSize)
                                .sum();

                // Lấy thống kê người dùng
                UserStats userStats = getUserStats();

                // Lấy thống kê video
                VideoStats videoStats = getVideoStats();

                // Lấy danh sách người dùng mới nhất
                List<UserStats.UserSummary> recentUsers = userRepository
                                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                                .stream()
                                .map(this::mapToUserSummary)
                                .collect(Collectors.toList());

                // Lấy danh sách video mới nhất
                List<VideoStats.VideoSummary> recentVideos = videoRepository
                                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                                .stream()
                                .map(this::mapToVideoSummary)
                                .collect(Collectors.toList());

                // Lấy phân phối trạng thái video
                Map<String, Integer> videoStatusDistribution = new HashMap<>();
                for (VideoStatus status : VideoStatus.values()) {
                        videoStatusDistribution.put(status.name(), videoRepository.findAllByStatus(status).size());
                }

                return DashboardSummary.builder()
                                .totalUsers(totalUsers)
                                .newUsersLast30Days(newUsersLast30Days)
                                .totalVideos(totalVideos)
                                .processedVideosLast30Days(processedVideosLast30Days)
                                .totalStorageUsed(totalStorageUsed)
                                .userStats(userStats)
                                .videoStats(videoStats)
                                .recentUsers(recentUsers)
                                .recentVideos(recentVideos)
                                .videoStatusDistribution(videoStatusDistribution)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public UserStats getUserStats() {
                log.debug("Lấy thống kê người dùng");

                // Tổng số người dùng
                long totalUsers = userRepository.count();

                // Số người dùng đang hoạt động và chưa kích hoạt
                long activeUsers = userRepository.findAll().stream().filter(User::getEnabled).count();
                long inactiveUsers = totalUsers - activeUsers;

                // Phân phối người dùng theo vai trò
                List<UserStats.RoleDistribution> roleDistribution = new ArrayList<>();

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Tuple> query = cb.createTupleQuery();
                Root<Role> role = query.from(Role.class);

                query.multiselect(
                                role.get("name").alias("roleName"),
                                cb.count(role).alias("count"));
                query.groupBy(role.get("name"));

                List<Tuple> results = entityManager.createQuery(query).getResultList();

                for (Tuple result : results) {
                        String roleName = result.get("roleName", String.class);
                        Long count = result.get("count", Long.class);

                        roleDistribution.add(UserStats.RoleDistribution.builder()
                                        .roleName(roleName)
                                        .count(count)
                                        .build());
                }

                return UserStats.builder()
                                .totalUsers(totalUsers)
                                .activeUsers(activeUsers)
                                .inactiveUsers(inactiveUsers)
                                .roleDistribution(roleDistribution)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public VideoStats getVideoStats() {
                log.debug("Lấy thống kê video");

                // Tổng số video
                long totalVideos = videoRepository.count();

                // Số video theo loại
                long uploadedVideos = videoRepository.findAll().stream()
                                .filter(video -> video.getVideoType() == VideoType.UPLOADED)
                                .count();
                long youtubeVideos = totalVideos - uploadedVideos;

                // Phân phối video theo trạng thái
                List<VideoStats.StatusDistribution> statusDistribution = new ArrayList<>();
                for (VideoStatus status : VideoStatus.values()) {
                        long count = videoRepository.findAllByStatus(status).size();
                        statusDistribution.add(VideoStats.StatusDistribution.builder()
                                        .status(status.name())
                                        .count(count)
                                        .build());
                }

                return VideoStats.builder()
                                .totalVideos(totalVideos)
                                .uploadedVideos(uploadedVideos)
                                .youtubeVideos(youtubeVideos)
                                .statusDistribution(statusDistribution)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PagedResponse<UserDTO> getUsers(int page, int size) {
                log.debug("Lấy danh sách người dùng phân trang, trang: {}, kích thước: {}", page, size);
                return userService.getAllUsers(page, size);
        }

        @Override
        @Transactional(readOnly = true)
        public UserDTO getUserById(UUID id) {
                log.debug("Lấy thông tin người dùng với id: {}", id);
                return userService.getUserById(id);
        }

        @Override
        @Transactional
        public UserDTO disableUser(UUID id) {
                log.debug("Vô hiệu hóa người dùng với id: {}", id);
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

                UserUpdateDTO updateDTO = new UserUpdateDTO();
                updateDTO.setName(user.getName());
                updateDTO.setEmail(user.getEmail());
                updateDTO.setEnabled(false);

                return userService.updateUser(id, updateDTO);
        }

        @Override
        @Transactional
        public UserDTO enableUser(UUID id) {
                log.debug("Kích hoạt người dùng với id: {}", id);
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

                UserUpdateDTO updateDTO = new UserUpdateDTO();
                updateDTO.setName(user.getName());
                updateDTO.setEmail(user.getEmail());
                updateDTO.setEnabled(true);

                return userService.updateUser(id, updateDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public PagedResponse<VideoDto> getVideos(int page, int size) {
                log.debug("Lấy danh sách video phân trang cho Admin Dashboard, trang: {}, kích thước: {}", page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Video> videos = videoRepository.findAll(pageable);

                List<VideoDto> videoDtos = videos.getContent().stream()
                                .map(videoMapper::toDto)
                                .collect(Collectors.toList());

                return new PagedResponse<>(
                                videoDtos,
                                videos.getNumber(),
                                videos.getSize(),
                                videos.getTotalElements(),
                                videos.getTotalPages(),
                                videos.isLast());
        }

        @Override
        @Transactional(readOnly = true)
        public VideoDto getVideoById(UUID id) {
                log.debug("Lấy thông tin video với id: {} cho Admin Dashboard", id);

                Video video = videoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id.toString()));

                return videoMapper.toDto(video);
        }

        @Override
        @Transactional
        public void deleteVideo(UUID id) {
                log.debug("Xóa video với id: {}", id);
                videoService.deleteVideo(id);
        }

        /**
         * Map User entity sang UserSummary DTO
         */
        private UserStats.UserSummary mapToUserSummary(User user) {
                // Lấy số lượng video của người dùng
                long videoCount = videoRepository.countByUser(user);

                // Lấy dung lượng lưu trữ đã sử dụng
                Long storageUsed = videoRepository.sumFileSizeByUser(user);
                if (storageUsed == null) {
                        storageUsed = 0L;
                }

                // Lấy danh sách vai trò
                List<String> roles = user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toList());

                return UserStats.UserSummary.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .name(user.getName())
                                .enabled(user.getEnabled())
                                .roles(roles)
                                .createdAt(user.getCreatedAt())
                                .videoCount(videoCount)
                                .storageUsed(storageUsed)
                                .build();
        }

        /**
         * Map Video entity sang VideoSummary DTO
         */
        private VideoStats.VideoSummary mapToVideoSummary(Video video) {
                return VideoStats.VideoSummary.builder()
                                .id(video.getId())
                                .userId(video.getUser().getId())
                                .username(video.getUser().getUsername())
                                .title(video.getTitle())
                                .description(video.getDescription())
                                .videoType(video.getVideoType().name())
                                .status(video.getStatus().name())
                                .fileSize(video.getFileSize())
                                .duration(video.getDuration())
                                .createdAt(video.getCreatedAt())
                                .build();
        }
}