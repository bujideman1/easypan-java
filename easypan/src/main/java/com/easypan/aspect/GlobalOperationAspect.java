package com.easypan.aspect;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.utils.StringTools;
import com.easypan.utils.VerifyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    private static final String TYPE_STRING="java.lang.String";
    private static final String TYPE_INTEGER="java.lang.Integer";
    private static final String TYPE_LONG="java.lang.Long";
    private final Logger logger= LoggerFactory.getLogger(GlobalOperationAspect.class);
//    定义切点
    @Pointcut("@annotation(com.easypan.annotation.GlobalInterceptor)")
    private void requestInterceptor(){}
    //定义切面
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point){
        
        try {
            Object target = point.getTarget();
            Object[] args = point.getArgs();
            String name = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(name, parameterTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if(null==interceptor){
                return;
            }
            //校验登录
            if(interceptor.checkLogin()||interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }
            //校验参数
            if(interceptor.checkParams()){
                validateParams(method,args);
            }
        }catch (BusinessException e){
            logger.error("全局拦截器异常",e);
            throw e;
        }
        catch (Throwable e){
            logger.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkLogin(boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto webUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if(null==webUserDto){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if(checkAdmin&&!webUserDto.getAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }

    /**
     * 校验输入的参数
     * @param method:需要校验的参数，通过aop获取
     * @param args:该方法的参数列表--值
     */
    private void validateParams(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if(verifyParam==null){
                continue;
            }
            //数据类型是基本类型
            if(TYPE_STRING.equals(parameter.getParameterizedType().getTypeName())||TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())||TYPE_LONG.equals(parameter.getParameterizedType().getTypeName())){
                checkValue(arg,verifyParam);//检查注解项
            }else{
                //对象类型，传递参数项，进入对象里查看对象字段注解
                checkObject(arg,parameter);
            }
        }
    }

    /**
     * 校验对象参数，通过反射获取对象字段值，然后校验字段上的注解
     * @param obj:要校验的对象
     * @param parameter:aop获取的方法里的参数
     */
    private void checkObject(Object obj, Parameter parameter) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class aClass = Class.forName(typeName);
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam verifyParam = field.getAnnotation(VerifyParam.class);
                if (verifyParam==null){
                    continue;
                }
                field.setAccessible(true);
                Object o = field.get(obj);
                checkValue(o,verifyParam);
            }

        }catch (BusinessException e){
            logger.error("参数校验失败",e);
            throw  e;
        }
        catch (Exception e){
            logger.error("校验参数失败");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 校验基本类型
     * @param arg:输入的参数具体值
     * @param verifyParam:要校验的参数注解
     */
    private void checkValue(Object arg, VerifyParam verifyParam) {
       boolean isEmpty= arg==null|| StringTools.isEmpty(arg.toString());
       int length=arg==null?0:arg.toString().length();
       //校验空
        if(isEmpty&&verifyParam.required()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //校验长度
        if(!isEmpty&&(verifyParam.max()!=-1&&verifyParam.max()<length||verifyParam.min()!=-1&&verifyParam.min()>length)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //校验正则
        if(!isEmpty&&!StringTools.isEmpty(verifyParam.regex().getRegex())&& !VerifyUtils.verify(verifyParam.regex(),String.valueOf(arg))){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        
    }
}
