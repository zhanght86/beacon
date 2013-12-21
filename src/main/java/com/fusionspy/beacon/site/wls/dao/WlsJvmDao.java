package com.fusionspy.beacon.site.wls.dao;
// Generated 2013-9-20 23:36:28 by One Data Tools 1.0.0

import com.fusionspy.beacon.report.Statistics;
import com.sinosoft.one.data.jade.annotation.SQL;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.fusionspy.beacon.site.wls.entity.WlsJvm;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface WlsJvmDao extends PagingAndSortingRepository<WlsJvm, Integer> {

    @SQL("select server_name name, MAX(HeapFreeCurrent) max,MIN(HeapFreeCurrent) min,AVG(HeapFreeCurrent) avg from ge_monitor_wls_jvm u " +
            "where u.site_name = ?1 and u.rec_time >= ?2 and u.rec_time < ?3 group by server_name")
    List<Statistics> statisticFreeHeap(String resourceId, Timestamp startDate, Timestamp endDate);
}

