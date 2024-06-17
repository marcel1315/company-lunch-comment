package com.marceldev.companylunchcomment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info = @Info(
        title = "우리 회사 점심 - 직장인 점심 기록 서비스 백엔드",
        version = "1.0",
        description = "점심 코멘트를 기록하고, 사내에 공유하는 서비스입니다"
    ),
    security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(name = "BearerAuth", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SwaggerConfiguration {

}
