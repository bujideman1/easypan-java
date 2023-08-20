package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.ResponseResult;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.CreateImageCode;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AccountController extends ABaseController {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private EmailCodeService emailCodeService;
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/list")
    public ResponseResult list() {
        return ResponseResult.okResult(userInfoService.list());
    }

    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/sendEmailCode")
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                    @VerifyParam(required = true) String checkCode,
                                    @VerifyParam(required = true) Integer type) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码不正确");
            }
            emailCodeService.sendEmailCode(email, type);
            return ResponseVO.okResult();
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }

    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/register")
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true) String nickName,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode
    ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }

    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/login")
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode
    ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            SessionWebUserDto webUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY, webUserDto);
            return getSuccessResponseVO(webUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }

    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/resetPwd")
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode
    ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.resetPwd(email, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId
    ) {

        String avatarFolderName = appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        File folder = new File(avatarFolderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String avatar_path =avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File file = new File(avatar_path);
        if (!file.exists()) {
            //查看默认头像
            if (!new File(avatarFolderName + Constants.AVATAR_DEFAULT).exists()) {
                printNoDefaultImage(response);
            }
            avatar_path = avatarFolderName + Constants.AVATAR_DEFAULT;
        }

        response.setContentType("image/jpg");
        readFile(response, avatar_path);
    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(HttpSession session){
        return getSuccessResponseVO(getUserInfoFromSession(session));

    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/getUseSpace")
    public ResponseVO getUseSpace(HttpSession session){
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        UserSpaceDto userSpace=redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId());
        return getSuccessResponseVO(userSpace);

    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session){
        session.invalidate();
        return getSuccessResponseVO(null);

    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/updateUserAvatar")
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String baseFolder=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
        File targetFileFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
        File targetFile=new File(targetFileFolder.getPath()+"/"+webUserDto.getUserId()+Constants.AVATAR_SUFFIX);
        if(!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        try {
            avatar.transferTo(targetFile);
        }
        catch (Exception e){
            logger.error("上传头像失败",e);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(webUserDto.getUserId());
        userInfo.setQqAvatar("");//上传头像优先级大于qq头像
        userInfoService.updateById(userInfo);
        webUserDto.setAvatar(null);
        session.setAttribute(Constants.SESSION_KEY,webUserDto);
        return getSuccessResponseVO(null);
    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/updatePassword")
    public ResponseVO updatePassword(HttpSession session, @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD) String password){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(webUserDto.getUserId());
        userInfo.setPassword(StringTools.encodeByMd5(password));
        userInfoService.updateById(userInfo);
        return getSuccessResponseVO(null);
    }
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录放置默认头像default_avatar.jpg");
        } catch (Exception e) {
            logger.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/qqlogin")
    public ResponseVO qqlogin(HttpSession session, String callbackUrl) throws UnsupportedEncodingException {
        String state = StringTools.getRandomNumber(Constants.LENGTH_30);
        if(!StringTools.isEmpty(callbackUrl)){
            session.setAttribute(state,callbackUrl);
        }
        String url=String.format(appConfig.getAuthorizationUrl(),appConfig.getAppId(), URLEncoder.encode(appConfig.getRedirectUrl(),"utf-8"),state);
        return getSuccessResponseVO(url);
    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/qqlogincallback")
    public ResponseVO qqLoginCallback(HttpSession session, @VerifyParam(required = true) String code,@VerifyParam(required = true)String state)   {
        SessionWebUserDto webUserDto=userInfoService.qqLogin(code);
        session.setAttribute(Constants.SESSION_KEY,webUserDto);
        Map<String,Object>result=new HashMap<>();
        result.put("callbackUrl",session.getAttribute(state));
        result.put("userInfo",webUserDto);
        return getSuccessResponseVO(result);
    }
}
