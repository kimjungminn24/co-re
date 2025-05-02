package com.core.api.data.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CompareBranchResponseDto(
        String message,
        String writerName,
        String date,
        String direction
) {

}
