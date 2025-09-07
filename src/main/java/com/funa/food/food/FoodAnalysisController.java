package com.funa.food.food;

import com.funa.food.food.dto.FoodAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/v1/food", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Food", description = "Food image analysis API")
public class FoodAnalysisController {

    private final FoodAnalysisService foodAnalysisService;

    @Operation(
            summary = "Analyze a meal image",
            description = "Uploads a food image with user's status and returns analyzed foods with macros, calories, and positions.",
            requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    )
    @PostMapping(path = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodAnalysisResponse> analyze(
            @RequestParam("image") MultipartFile image,
            @RequestParam("status") String status
    ) {
        FoodAnalysisResponse resp = foodAnalysisService.analyze(image, status);
        return ResponseEntity.ok(resp);
    }
}
