package cn.cnic.trackrecord.service;

import cn.cnic.trackrecord.common.date.LongDate;
import cn.cnic.trackrecord.data.entity.Track;
import cn.cnic.trackrecord.data.entity.TrackStat;
import cn.cnic.trackrecord.data.vo.TrackSearchParams;

import java.util.List;

public interface TrackService {
    int addAndGetId(Track track);

    boolean existByMd5AndFileSize(String md5, int fileSize);

    @Deprecated
    List<Track> getByTrackSearchParams(TrackSearchParams params);

    Track get(int id);
    TrackStat countUserByDay(int userId, LongDate beginTime, LongDate endTime);

    List<Track> getAll();

    List<Track> getByUserId(int userId);
}
