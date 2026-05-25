package edu.cit.lariosa.quickparcel.features.upload;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.shared.entity.File;
import edu.cit.lariosa.quickparcel.features.shared.entity.Parcel;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import edu.cit.lariosa.quickparcel.features.shared.repository.FileRepository;
import edu.cit.lariosa.quickparcel.features.delivery.repository.ParcelRepository;
import edu.cit.lariosa.quickparcel.features.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${profile.upload-dir}")
    private String profileUploadDir;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/parcel/{parcelId}")
    public ResponseEntity<?> uploadParcelImage(@PathVariable Long parcelId,
                                               @RequestParam("file") MultipartFile file,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Parcel parcel = parcelRepository.findById(parcelId)
                    .orElseThrow(() -> new RuntimeException("Parcel not found"));

            // Verify ownership
            if (!parcel.getSender().getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            // Validate file
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File too large (max 10MB)"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files allowed"));
            }

            // Create directory
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Save to database
            File fileEntity = new File();
            fileEntity.setParcel(parcel);
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath("/api/files/" + filename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setMimeType(contentType);
            fileEntity.setProfilePicture(false);
            fileEntity.setUploadedAt(LocalDateTime.now());
            // Removed: fileEntity.setUploadedBy...
            fileRepository.save(fileEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileEntity.getFilePath());
            response.put("fileId", fileEntity.getId());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate file
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File too large (max 5MB)"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files allowed"));
            }

            // Delete old profile picture if exists
            fileRepository.findByUserAndIsProfilePictureTrue(user).ifPresent(oldFile -> {
                try {
                    Path oldPath = Paths.get(profileUploadDir).resolve(Paths.get(oldFile.getFilePath()).getFileName());
                    Files.deleteIfExists(oldPath);
                    fileRepository.delete(oldFile);
                } catch (IOException e) {
                    System.err.println("Failed to delete old profile picture: " + e.getMessage());
                }
            });

            // Create directory
            Path uploadPath = Paths.get(profileUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "profile_" + user.getId() + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Save to database
            File fileEntity = new File();
            fileEntity.setUser(user);
            fileEntity.setParcel(null);
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath("/api/files/profiles/" + filename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setMimeType(contentType);
            fileEntity.setProfilePicture(true);
            fileEntity.setUploadedAt(LocalDateTime.now());
            // Removed: fileEntity.setUploadedBy...
            fileRepository.save(fileEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileEntity.getFilePath());
            response.put("message", "Profile picture updated successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile-picture")
    public ResponseEntity<?> getProfilePicture(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fileRepository.findByUserAndIsProfilePictureTrue(user)
                .map(file -> ResponseEntity.ok(Map.of("hasPicture", true, "url", file.getFilePath())))
                .orElse(ResponseEntity.ok(Map.of("hasPicture", false)));
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<?> deleteProfilePicture(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return fileRepository.findByUserAndIsProfilePictureTrue(user)
                    .map(file -> {
                        try {
                            // Delete file from disk
                            Path filePath = Paths.get(profileUploadDir).resolve(Paths.get(file.getFilePath()).getFileName());
                            Files.deleteIfExists(filePath);

                            // Delete from database
                            fileRepository.delete(file);

                            return ResponseEntity.ok(Map.of("success", true, "message", "Profile picture removed"));
                        } catch (IOException e) {
                            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete file"));
                        }
                    })
                    .orElse(ResponseEntity.ok(Map.of("message", "No profile picture to delete")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}