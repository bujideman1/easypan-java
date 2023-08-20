package com.easypan.component;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.mapper.FileInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private FileInfoMapper fileInfoMapper;
    public SysSettingsDto getSysSettingsDto(){
        SysSettingsDto  sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(null==sysSettingsDto){
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return  sysSettingsDto;
    }

    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto getUserSpaceUse(String userId) {
        UserSpaceDto spaceDto = (UserSpaceDto)redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if(spaceDto==null){
            spaceDto=new UserSpaceDto();
            //todo 计算用户使用空间
            Long useSpace = fileInfoMapper.selectUseSpace(userId);
            spaceDto.setUseSpace(useSpace);
            spaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace()*Constants.MB);
        }
        return spaceDto;
    }
    public void saveFileTempSize(String userId,String fileId,long fileSize){
        Long tempSize = getFileTempSize(userId, fileId);
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId,tempSize+fileSize,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }
    public Long getFileTempSize(String userId,String fileId){
        return getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
    }
    private Long getFileSizeFromRedis(String key){
        Object sizeObj = redisUtils.get(key);
        if(sizeObj==null)return 0L;
        if (sizeObj instanceof Integer){
            return ((Integer) sizeObj).longValue();
        }else if(sizeObj instanceof Long){
            return (Long) sizeObj;
        }
        return 0L;
    }
}
