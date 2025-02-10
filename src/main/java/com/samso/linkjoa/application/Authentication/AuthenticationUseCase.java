package com.samso.linkjoa.application.Authentication;

import com.samso.linkjoa.core.Utility.Encryptor;
import com.samso.linkjoa.core.common.ApplicationInternalException;
import com.samso.linkjoa.domain.Authentication.Authentication;
import com.samso.linkjoa.domain.Authentication.AuthenticationEnum;
import com.samso.linkjoa.domain.mail.MailSender;
import com.samso.linkjoa.infrastructure.redis.RedisOffSetEnum;
import com.samso.linkjoa.infrastructure.redis.RedisRepository;
import com.samso.linkjoa.presentation.Authentication.request.AuthenticationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationUseCase {

    private final Authentication authentication;
    private final RedisRepository redisRepository;
    private final MailSender mailSender;

    public String initAuthentication(String mail) throws Exception {

        //인증번호 생성
        Authentication authenticationInfo = authentication.generateAuthCode(mail);

        //인증정보 redis 저장
        String authKey = UUID.randomUUID().toString();
        Map<String, String> authData = new HashMap<>();
        authData.put("mail", Encryptor.twoWayEncrypt(authenticationInfo.getMail()));
        authData.put("code", Encryptor.twoWayEncrypt(String.valueOf(authenticationInfo.getAuthCode())));
        redisRepository.saveHashData(authKey, authData, RedisOffSetEnum.SIGN_UP.getValue());

        //메일 발송
        String subject = "[인증번호 발송]";
        String body = "인증번호 [" + authenticationInfo.getAuthCode()+ "]를 입력하세요 (유효시간 : 3분)";
        if(!mailSender.sendMail(authenticationInfo.getMail(), subject, body)){
            throw new ApplicationInternalException(AuthenticationEnum.SEND_AUTH_INFO_FAIL.getValue(),"Failed to send authentication number");
        }

        return authKey;
    }

    public String verifyAuthentication(AuthenticationRequest authenticationRequest) throws Exception {

        //Assert.notNull(request.getSession().getAttribute("mailAuth"), AuthenticationEnum.NOT_EXIST_AUTH_INFO.getValue());
        //FIXME 확인
        System.out.println("check 1 : " + authenticationRequest.toString());
        Optional.ofNullable(authenticationRequest.getAuthKey())
                .orElseThrow(() -> new ApplicationInternalException(AuthenticationEnum.NOT_EXIST_AUTH_INFO.getValue(), "no history of authentication attempts"));

        Optional<Map<Object,Object>> storedData = redisRepository.getHashData(authenticationRequest.getAuthKey());
        //FIXME 확인
        System.out.println("check 1 : "  + storedData.toString());

        storedData
                .filter(data -> authenticationRequest.getMail().equals(Encryptor.twoWayDecrypt(data.get("mail").toString()))
                                && authenticationRequest.getAuthCode().equals(Encryptor.twoWayDecrypt(data.get("code").toString())))
                .orElseThrow(() -> new ApplicationInternalException(AuthenticationEnum.AUTH_FAIL.getValue(), "Authentication failed"));

        return AuthenticationEnum.AUTH_SUCCESS.getValue();
    }
}
