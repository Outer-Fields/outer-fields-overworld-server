package io.mindspice.outerfieldsserver.util;

import io.mindspice.outerfieldsserver.core.Settings;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AbuseFilters {
    private volatile int wsMsgCount = 0;
    private volatile long wsMsgEpoch = 0;
    private volatile long lastSmallReq = 0;
    private volatile long lastLargeReq = 0;
    private volatile int restMsgCount = 0;
    private volatile long restMsgEpoch = 0;
    private volatile long queueCoolDownTime = 0;
    private volatile long lastMsgTime = Instant.now().getEpochSecond();
    private final Set<String> ips = Collections.synchronizedSet(new HashSet<>(2));

    public boolean wsTimeout() {
        long now = Instant.now().getEpochSecond();
        if ((now - wsMsgEpoch) >= Settings.GET().wsMsgWindow) {
            wsMsgEpoch = now;
            wsMsgCount = 0;
        }
        wsMsgCount++;
        return wsMsgCount > Settings.GET().wsMsgLimit;
    }

    public boolean restTimeout(long now) {
        System.out.println("here");
        if ((now - restMsgEpoch) >= Settings.GET().restMsgWindow) {
            restMsgEpoch = now;
            restMsgCount = 0;
        }
        restMsgCount++;
        return restMsgCount > Settings.GET().restMsgLimit;
    }

    public boolean setIp(String ip) {
        ips.add(ip);
        if (ips.size() > 1) {
           return false;
        }
        return true;
    }

    public boolean restTimeout() {
        long now = Instant.now().getEpochSecond();
        if ((now - restMsgEpoch) >= Settings.GET().restMsgWindow) {
            restMsgEpoch = now;
            restMsgCount = 0;
        }
        restMsgCount++;
        return restMsgCount > Settings.GET().restMsgLimit;
    }

    public long lastMsgTime() { return lastMsgTime; }

    public List<String> ips() { return List.copyOf(ips); }

    public int wsMsgCount() {
        return wsMsgCount;
    }

    public void setWsMsgCount(int wsMsgCount) {
        this.wsMsgCount = wsMsgCount;
    }

    public long wsMsgEpoch() {
        return wsMsgEpoch;
    }

    public void setWsMsgEpoch(long wsMsgEpoch) {
        this.wsMsgEpoch = wsMsgEpoch;
    }

    public long lastSmallReq() {
        return lastSmallReq;
    }

    public void setLastSmallReq(long lastSmallReq) {
        this.lastSmallReq = lastSmallReq;
    }

    public long lastLargeReq() {
        return lastLargeReq;
    }

    public void setLastLargeReq(long lastLargeReq) {
        this.lastLargeReq = lastLargeReq;
    }

    public int restMsgCount() {
        return restMsgCount;
    }

    public void setRestMsgCount(int restMsgCount) {
        this.restMsgCount = restMsgCount;
    }

    public long restMsgEpoch() {
        return restMsgEpoch;
    }

    public void setRestMsgEpoch(long restMsgEpoch) {
        this.restMsgEpoch = restMsgEpoch;
    }

    public long queueCoolDownTime() {
        return queueCoolDownTime;
    }

    public void setQueueCoolDownTime(long queueCoolDownTime) {
        this.queueCoolDownTime = queueCoolDownTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }


}
