package com.pd.framework.security.oauth2;

import com.pd.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2TokenExchangeController {

    @GetMapping("/exchange")
    public AjaxResult exchangeTokens(@RequestParam String code) {
        OAuth2TokenStore.TokenPair pair = OAuth2TokenStore.exchange(code);
        if (pair == null) {
            return AjaxResult.error("Invalid or expired exchange code");
        }
        AjaxResult result = AjaxResult.success();
        result.put("token", pair.token());
        result.put("refreshToken", pair.refreshToken());
        return result;
    }
}
