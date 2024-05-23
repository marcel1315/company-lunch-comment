package com.marceldev.companylunchcomment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "직장인 점심 코멘트 서비스",
        version = "1.0",
        description = "점심 코멘트를 기록하고, 사내에 공유하는 서비스입니다"
    )
)
public class SwaggerConfiguration {

}
