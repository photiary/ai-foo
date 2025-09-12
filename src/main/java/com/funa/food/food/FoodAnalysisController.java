package com.funa.food.food;

import com.funa.food.food.dto.FoodAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/v1/food", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Food", description = "Food image analysis API")
public class FoodAnalysisController {

    private final FoodAnalysisService foodAnalysisService;

    @Value("${app.upload.dir:data/uploads}")
    private String uploadDir;

    @Operation(
            summary = "Analyze a meal image",
            description = "Uploads a food image with user's status and returns analyzed foods with macros, calories, and positions.",
            requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    )
    @PostMapping(path = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodAnalysisResponse> analyze(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "analysisMode", required = false) String analysisMode,
            @RequestParam(value = "modelName") String modelName
    ) {
        AnalysisMode mode = AnalysisMode.from(analysisMode);
        FoodAnalysisResponse resp = foodAnalysisService.analyze(image, status, mode, modelName);
        return ResponseEntity.ok(resp);
    }
    @Operation(
            summary = "Get analysis detail",
            description = "Get stored food analysis by id"
    )
    @GetMapping(path = "/analysis/{id}")
    public ResponseEntity<FoodAnalysisResponse> getDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(foodAnalysisService.getDetail(id));
    }

    @Operation(
            summary = "List all analyses",
            description = "Get all stored food analyses"
    )
    @GetMapping(path = "/analysis")
    public ResponseEntity<java.util.List<FoodAnalysisResponse>> getList() {
        return ResponseEntity.ok(foodAnalysisService.getList());
    }

    @Operation(
            summary = "Get uploaded food image",
            description = "Serve image stored in 'data/uploads' by fileName query parameter"
    )
    @GetMapping(path = "/analysis/image", produces = MediaType.ALL_VALUE)
    public ResponseEntity<Resource> getImage(@RequestParam("fileName") String fileName) throws Exception {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base) || !Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(target);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        Resource resource = new FileSystemResource(target);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
