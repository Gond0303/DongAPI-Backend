package com.dong.project.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.project.model.entity.DailyCheckIn;
import com.dong.project.service.DailyCheckInService;
import com.dong.project.utils.RedissonLockUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户签到定时任务
 */
@Component
public class DailyCheckInJob {
    @Resource
    private DailyCheckInService dailyCheckInService;
    @Resource
    private RedissonLockUtil redissonLockUtil;

    /**
     * 每天晚上12点批量清理签到表的所有数据
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearCheckInList(){
        redissonLockUtil.redissonDistributedLocks("clearCheckInList",() -> {
            //每批删除的数据量
            int batchSize = 1000;
            //是否还有数据需要删除
            boolean hasMoreData = true;
            while (hasMoreData){
                //分批查询数据,每次查1000条
                QueryWrapper<DailyCheckIn> dailyCheckInQueryWrapper = new QueryWrapper<>();
                dailyCheckInQueryWrapper.last("LIMIT "+batchSize);
                List<DailyCheckIn> dataList = dailyCheckInService.list(dailyCheckInQueryWrapper);

                if (dataList.isEmpty()){
                    //没有数据了,退出循环
                    hasMoreData = false;
                } else {
                    //批量删除数据,删除后继续查1000条，直到没有数据退出循环
                    dailyCheckInService.removeByIds(dataList.stream().map(DailyCheckIn::getId).collect(Collectors.toList()));
                }
            }
        });
    }
}
