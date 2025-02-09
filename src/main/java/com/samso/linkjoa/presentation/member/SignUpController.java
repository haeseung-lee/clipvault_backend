package com.samso.linkjoa.presentation.member;

import com.samso.linkjoa.application.member.SignUpUseCase;
import com.samso.linkjoa.domain.member.MemberEnum;
import com.samso.linkjoa.presentation.member.request.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("signUp")
public class SignUpController {

    private final SignUpUseCase signUpUseCase;

    @PostMapping("/signUp.do")
    public @ResponseBody String signUp(HttpServletRequest request, @RequestBody SignUpRequest signUpRequest) throws Exception{

        return signUpUseCase.signUp(request, signUpRequest);
    }
}
