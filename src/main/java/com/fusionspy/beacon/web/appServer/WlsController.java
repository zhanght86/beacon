package com.fusionspy.beacon.web.appServer;

import com.fusionspy.beacon.report.DateSeries;
import com.fusionspy.beacon.site.MonitorManage;
import com.fusionspy.beacon.site.wls.WlsHisData;
import com.fusionspy.beacon.site.wls.WlsService;
import com.fusionspy.beacon.site.wls.entity.*;
import com.fusionspy.beacon.web.BeaconLocale;
import com.fusionspy.beacon.web.JsonGrid;
import com.sinosoft.one.monitor.utils.MessageUtils;
import com.sinosoft.one.mvc.web.Invocation;
import com.sinosoft.one.mvc.web.annotation.DefValue;
import com.sinosoft.one.mvc.web.annotation.Param;
import com.sinosoft.one.mvc.web.annotation.Path;
import com.sinosoft.one.mvc.web.annotation.rest.Get;
import com.sinosoft.one.mvc.web.annotation.rest.Post;
import com.sinosoft.one.mvc.web.instruction.reply.Reply;
import com.sinosoft.one.mvc.web.instruction.reply.Replys;
import com.sinosoft.one.mvc.web.instruction.reply.transport.Json;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * tuxedo action
 * User: qc
 * Date: 11-8-30
 * Time: 下午8:26
 */
@Path("/weblogic")
public class WlsController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Object> message = new HashMap<String, Object>();

    @Resource
    private MonitorManage monitorManage;

    @Resource
    private WlsService wlsService;

    @Get("addUI")
    public String addUI() {
        return "weblogicSaveUI";
    }

    @Get("editUI/{serverName}")
    public String editUI(@Param("serverName")String serverName,Invocation invocation) {
        WlsHisData hisData = monitorManage.getMonitorInf(serverName);
        hisData.getWlsIniData().getWlsSysrec();
        WlsServer wlsServer = wlsService.getSite(serverName);
        invocation.addModel("server", wlsServer);
        return "weblogicSaveUI";
    }

    @Post("save")
    public String save(WlsServer wlsServer) {
        wlsServer.setStatus(1);
        wlsService.save(wlsServer);
        monitorManage.cancel(wlsServer.getServerName());
        return "/appServer/weblogic/start/"+wlsServer.getServerName();
    }

    /**
     * 删除操作(包含删除一个和批量删除操作)
     * @param serverNames
     * @return
     */
    @Get("delete/{serverNames}")
    public Reply delete(@Param("serverNames")List<String> serverNames) {
        message.put("result", true);
        for(String serverName : serverNames) {
            monitorManage.cancel(serverName);
            //wlsService
        }
        return Replys.with(message).as(Json.class);
    }

    @Get("manager")
    public String listUI(){
        return "weblogicList";
    }

    @Post("list")
    public Reply list(Invocation inv) {
        List<WlsServer> wlsServerList = wlsService.list();
        final String viewUrl = inv.getServletContext().getContextPath()+"/appServer/weblogic/view/";
        final String eidUrl = inv.getServletContext().getContextPath()+"/appServer/weblogic/editUI/";
        JsonGrid grid = JsonGrid.buildGrid(wlsServerList, new JsonGrid.JsonRowHandler<WlsServer>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsServer wlsServer) {
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.addCell(MessageUtils.formateMessage(MessageUtils.MESSAGE_FORMAT_A, viewUrl + wlsServer.getServerName(), wlsServer.getServerName()));
                row.addCell(wlsServer.getListenAddress());
                row.addCell(wlsServer.getListenPort()+"");
                row.addCell(wlsServer.getInterval()+"");
                row.addCell(MessageUtils.formateMessage(MessageUtils.HANDLER_FORMAT, eidUrl+wlsServer.getServerName()));
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    @Get("view/{serverName}")
    public String view(@Param("serverName")String serverName,Invocation invocation) {
        WlsHisData hisData = monitorManage.getMonitorInf(serverName);
        WlsIniData iniData = hisData.getWlsIniData();
        WlsInTimeData inTimeData = hisData.getWlsInTimeData();
        invocation.addModel("serverName",serverName);
        invocation.addModel("serverType","weblogic");
        invocation.addModel("wlsVersion", iniData.getWlsSysrec().getDomainVersion());
        invocation.addModel("rectime", DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime(iniData.getWlsSysrec().getRecTime())));
        invocation.addModel("osVersion", iniData.getWlsSysrec().getOsVersion());
        invocation.addModel("serverNum", iniData.getWlsSysrec().getServerNum());
        invocation.addModel("domainName", iniData.getWlsSysrec().getName());
        invocation.addModel("adminServerName", iniData.getWlsSysrec().getAdminServerName());
        invocation.addModel("cpuIdle", inTimeData.getResource().getCpuIdle());
        invocation.addModel("memFree", inTimeData.getResource().getMemFree());
        invocation.addModel("agentVer", iniData.getWlsSysrec().getAgentVersion());
        invocation.addModel("systemboot", iniData.getWlsSysrec().getSystemBoot());
        invocation.addModel("count", hisData.getMonitorCount());
        invocation.addModel("ip", inTimeData.getServerRuntimes().get(0).getListenAddress());
        invocation.addModel("port", inTimeData.getServerRuntimes().get(0).getListenPort());
        return "weblogicInfo";
    }

    @Get("serverInfo/{serverName}")
    public Reply serverInfo(@Param("serverName")String serverName) {
        WlsHisData hisData = monitorManage.getMonitorInf(serverName);
        List<WlsSvr> serverRuntimes = hisData.getWlsInTimeData().getServerRuntimes();
        JsonGrid grid = JsonGrid.buildGrid(serverRuntimes, new JsonGrid.JsonRowHandler<WlsSvr>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsSvr wlsSvr) {
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(wlsSvr.getId()+"");
                row.addCell(wlsSvr.getServerName());
                row.addCell(wlsSvr.getListenAddress());
                row.addCell(wlsSvr.getListenPort()+"");
                //HEALTH_OK，HEALTH_WARN，HEALTH_CRITICAL，HEALTH_FAILED
                String health = wlsSvr.getHealth();
                String cssClass = "";
                if(health.indexOf("HEALTH_OK")!=-1) {
                    cssClass = "fine";
                } else if(health.indexOf("HEALTH_WARN")!=-1) {
                    cssClass = "y_poor";
                } else if(health.indexOf("HEALTH_CRITICAL")!=-1) {
                    cssClass = "poor";
                } else if(health.indexOf("HEALTH_FAILED")!=-1) {
                    cssClass = "poor";
                }
                row.addCell(MessageUtils.formateMessage(MessageUtils.MESSAGE_FORMAT_DIV, cssClass));
                row.addCell(wlsSvr.getState());
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    @Post("start/{serverName}")
    public Reply startMonitor(@Param("serverName")String serverName, @DefValue("zh_CN")Locale locale){
        BeaconLocale.setLocale(locale);
        monitorManage.monitor(serverName);
        logger.debug("start server name is {} ", serverName);
        message.put("type", "success");
        return Replys.with(message).as(Json.class);
    }

    @Get("/chart/{type}/{serverName}/{operation}")
    public Reply chartFree(@Param("type")String type, @Param("serverName")String serverName, @Param("operation")String operation) {
        WlsHisData hisData = monitorManage.getMonitorInf(serverName);
        WlsInTimeData inTimeData = hisData.getWlsInTimeData();
        Iterator<WlsInTimeData> iterator = hisData.getIntimeDatasQue(serverName);
        Object retVal = null; //返回char数据对象，用于转化为JSON
        if(type.equals("cpu")||type.equals("memory")){
            List<Object> list = new ArrayList<Object>(); //cpu和memory时，返回对象为数组形式
            if(operation.equals("latest")){
                list.add(inTimeData.getResource().getRecTime().getTime());
                if("cpu".equals(type)) {
                    //cpu使用率=100-cpu空闲率
                    list.add(100 - inTimeData.getResource().getCpuIdle());
                } else {
                    //TODO 内存使用率取值 此时获取的是内存空闲
                    list.add(inTimeData.getResource().getMemFree());
                }
            }else{
                while(iterator.hasNext()) {
                    inTimeData = iterator.next();
                    Map<String, Object> point = new HashMap<String, Object>();
                    point.put("x", inTimeData.getResource().getRecTime().getTime());
                    //TODO 非latest
                    if("cpu".equals(type)) {
                        //cpu使用率=100-cpu空闲率
                        point.put("y", 100 - inTimeData.getResource().getCpuIdle());
                    } else {
                        //TODO 内存使用率取值 此时获取的是内存空闲
                        point.put("y", inTimeData.getResource().getMemFree());
                    }
                    list.add(point);

                }
            }
            retVal = list;
        } else if("server_ram".equals(type)){
            if(operation.equals("latest")){
                List<Object> list = new ArrayList<Object>();
                for(WlsJvm jvm : inTimeData.getJvmRuntimes()) {
                    List<Object> point = new ArrayList<Object>();
                    //lineSerie.addData(jvm.getRecTime().getTime());
                    int freePercent = Integer.parseInt(jvm.getFreePercent());
                    point.add(jvm.getRecTime().getTime());
                    point.add(100-freePercent);
                    list.add(point);
                }
                retVal = list;
            } else {
                Map<String, Map<String, Object>> series = new LinkedHashMap<String, Map<String, Object>>();
                while (iterator.hasNext()) {
                    inTimeData = iterator.next();
                    for(WlsJvm jvm : inTimeData.getJvmRuntimes()) {
                        Map<String, Object> serie = series.get(jvm.getServerName());
                        if(serie == null) {
                            serie = new HashMap<String, Object>();
                            series.put(jvm.getServerName(), serie);
                            serie.put("name", jvm.getServerName());
                            List<Object> data = new ArrayList<Object>();
                            serie.put("data", data);
                        }
                        List<Object> data = (List<Object>) serie.get("data");
                        Map<String, Object> point = new HashMap<String, Object>();
                        int freePercent = Integer.parseInt(jvm.getFreePercent());
                        point.put("x", jvm.getRecTime().getTime());
                        point.put("y", 100 - freePercent);
                        data.add(point);
                    }
                }
                retVal = series.values();
            }
        } else if("server_throughput".equals(type)) {
            if(operation.equals("latest")){
                List<Object> list = new ArrayList<Object>();
                for(WlsThread thread : inTimeData.getThreadPoolRuntimes()) {
                    List<Object> point = new ArrayList<Object>();
                    point.add(thread.getRecTime().getTime());
                    point.add(thread.getThoughput());
                    list.add(point);
                }
                retVal = list;
            } else {
                Map<String, Map<String, Object>> series = new LinkedHashMap<String, Map<String, Object>>();
                while (iterator.hasNext()) {
                    inTimeData = iterator.next();
                    for(WlsThread thread : inTimeData.getThreadPoolRuntimes()) {
                        Map<String, Object> serie = series.get(thread.getServerName());
                        if(serie == null) {
                            serie = new HashMap<String, Object>();
                            series.put(thread.getServerName(), serie);
                            serie.put("name", thread.getServerName());
                            List<Object> data = new ArrayList<Object>();
                            serie.put("data", data);
                        }
                        List<Object> data = (List<Object>) serie.get("data");
                        Map<String, Object> point = new HashMap<String, Object>();
                        point.put("x", thread.getRecTime().getTime());
                        point.put("y", thread.getThoughput());
                        data.add(point);
                    }
                }
                retVal = series.values();
            }
        }  else if("thdusage".equals(type)) {
            if(operation.equals("latest")){
                List<Object> list = new ArrayList<Object>();
                for(WlsThread thread : inTimeData.getThreadPoolRuntimes()) {
                    List<Object> point = new ArrayList<Object>();
                    point.add(thread.getRecTime().getTime());
                    point.add(thread.getThdusage());
                    list.add(point);
                }
                retVal = list;
            } else {
                Map<String, Map<String, Object>> series = new LinkedHashMap<String, Map<String, Object>>();
                while (iterator.hasNext()) {
                    inTimeData = iterator.next();
                    for(WlsThread thread : inTimeData.getThreadPoolRuntimes()) {
                        Map<String, Object> serie = series.get(thread.getServerName());
                        if(serie == null) {
                            serie = new HashMap<String, Object>();
                            series.put(thread.getServerName(), serie);
                            serie.put("name", thread.getServerName());
                            List<Object> data = new ArrayList<Object>();
                            serie.put("data", data);
                        }
                        List<Object> data = (List<Object>) serie.get("data");
                        Map<String, Object> point = new HashMap<String, Object>();
                        point.put("x", thread.getRecTime().getTime());
                        point.put("y", thread.getThdusage());
                        data.add(point);
                    }
                }
                retVal = series.values();
            }
        }  else if("server_session".equals(type)) {
            if(operation.equals("latest")){
                List<Object> list = new ArrayList<Object>();
                for(WlsWebapp webapp : inTimeData.getComponentRuntimes()) {
                    List<Object> point = new ArrayList<Object>();
                    point.add(webapp.getRecTime().getTime());
                    point.add(webapp.getOpenSessionsCurrentCount());
                    list.add(point);
                }
                retVal = list;
            } else {
                Map<String, Map<String, Object>> series = new LinkedHashMap<String, Map<String, Object>>();
                while (iterator.hasNext()) {
                    inTimeData = iterator.next();
                    for(WlsWebapp webapp : inTimeData.getComponentRuntimes()) {
                        Map<String, Object> serie = series.get(webapp.getServerName());
                        if(serie == null) {
                            serie = new HashMap<String, Object>();
                            series.put(webapp.getServerName(), serie);
                            serie.put("name", webapp.getServerName());
                            List<Object> data = new ArrayList<Object>();
                            serie.put("data", data);
                        }
                        List<Object> data = (List<Object>) serie.get("data");
                        Map<String, Object> point = new HashMap<String, Object>();
                        point.put("x", webapp.getRecTime().getTime());
                        point.put("y", webapp.getOpenSessionsCurrentCount());
                        data.add(point);
                    }
                }
                retVal = series.values();
            }
        }
        return Replys.with(retVal).as(Json.class);
    }

    @Get("data/{type}/{serverName}")
    public Reply getInTimeData(@Param("type")String type,@Param("serverName")String serverName){
        WlsHisData hisData = monitorManage.getMonitorInf(serverName);
        WlsInTimeData wlsInTimeData = hisData.getWlsInTimeData();
        if(type.equals("WlsServer")){
            return getServerDate(wlsInTimeData.getServerRuntimes());
        } else if(type.equals("JVM")){
           return getJVMDate(wlsInTimeData.getJvmRuntimes());
        } else if(type.equals("ThreadPool")){
            return getThreadDate(wlsInTimeData.getThreadPoolRuntimes());
        } else if(type.equals("JDBC")){
            return getJDBCDate(wlsInTimeData.getJdbcDataSourceRuntimes());
        } else if(type.equals("Component")){
            return getComponentDate(wlsInTimeData.getComponentRuntimes());
        } else if(type.equals("JMS")){
            return getJMSDate(wlsInTimeData.getJmsServers());
        } else if(type.equals("EjbPool")){
            return getEjbPoolDate(wlsInTimeData.getPoolRuntimes());
        } else if(type.equals("EjbCache")){
            return getEjbCacheDate(wlsInTimeData.getCacheRuntime());
        }
        return null;
    }

    private Reply getServerDate(List<WlsSvr> serverRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(serverRuntimes, new JsonGrid.JsonRowHandler<WlsSvr>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsSvr wlsSvr) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(wlsSvr.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(wlsSvr.getServerName());
                row.addCell(wlsSvr.getListenAddress());
                row.addCell(wlsSvr.getListenPort());
                row.addCell(wlsSvr.getHealth());
                row.addCell(wlsSvr.getState());
                row.addCell(wlsSvr.getOpenSocketNum()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getJVMDate(List<WlsJvm> jvmRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(jvmRuntimes, new JsonGrid.JsonRowHandler<WlsJvm>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsJvm jvm) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(jvm.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(jvm.getServerName());
                row.addCell(jvm.getFreeHeap());
                row.addCell(jvm.getCurrentHeap());
                row.addCell(jvm.getFreePercent());
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getThreadDate(List<WlsThread> threadPoolRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(threadPoolRuntimes, new JsonGrid.JsonRowHandler<WlsThread>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsThread thread) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(thread.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(thread.getServerName());
                row.addCell(thread.getIdleCount()+"");
                row.addCell(thread.getStandbyCount()+"");
                row.addCell(thread.getTotalCount()+"");
                row.addCell(thread.getThoughput()+"");
                row.addCell(thread.getQueueLength()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getJDBCDate(List<WlsJdbc> jdbcDataSourceRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(jdbcDataSourceRuntimes, new JsonGrid.JsonRowHandler<WlsJdbc>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsJdbc jdbc) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(jdbc.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(jdbc.getServerName());
                row.addCell(jdbc.getName()+"");
                row.addCell(jdbc.getActiveCount()+"");
                row.addCell(jdbc.getActiveHigh()+"");
                row.addCell(jdbc.getCurrCapacity()+"");
                row.addCell(jdbc.getLeakCount()+"");
                row.addCell(jdbc.getState()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getComponentDate(List<WlsWebapp> componentRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(componentRuntimes, new JsonGrid.JsonRowHandler<WlsWebapp>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsWebapp webapp) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(webapp.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(webapp.getServerName());
                row.addCell(webapp.getName()+"");
                row.addCell(webapp.getName()+"");
                row.addCell(webapp.getDeploymentState()+"");
                row.addCell(webapp.getStatus()+"");
                row.addCell(webapp.getComponentName()+"");
                row.addCell(webapp.getOpenSessionsHighCount()+"");
                row.addCell(webapp.getOpenSessionsCurrentCount()+"");
                row.addCell(webapp.getSessionsOpenedTotalCount()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getJMSDate(List<WlsJms> jmsServers) {
        JsonGrid grid = JsonGrid.buildGrid(jmsServers, new JsonGrid.JsonRowHandler<WlsJms>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsJms jms) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(jms.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(jms.getServerName());
                row.addCell(jms.getName()+"");
                row.addCell(jms.getName()+"");
                row.addCell(jms.getBytesCurrentCount()+"");
                row.addCell(jms.getBytesHighCount()+"");
                row.addCell(jms.getBytesPendingCount()+"");
                row.addCell(jms.getBytesReceivedCount()+"");
                row.addCell(jms.getMessagesCurrentCount()+"");
                row.addCell(jms.getMessagesHighCount()+"");
                row.addCell(jms.getMessagesPendingCount()+"");
                row.addCell(jms.getMessagesReceivedCount()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getEjbPoolDate(List<WlsEjbpool> poolRuntimes) {
        JsonGrid grid = JsonGrid.buildGrid(poolRuntimes, new JsonGrid.JsonRowHandler<WlsEjbpool>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsEjbpool ejbPool) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(ejbPool.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(ejbPool.getServerName());
                row.addCell(ejbPool.getName()+"");
                row.addCell(ejbPool.getName()+"");
                row.addCell(ejbPool.getBeansInUseCount()+"");
                row.addCell(ejbPool.getBeansInUserCurrentCount()+"");
                row.addCell(ejbPool.getAccessTotalCount()+"");
                row.addCell(ejbPool.getDestroyedTotalCount()+"");
                row.addCell(ejbPool.getIdleBeansCount()+"");
                row.addCell(ejbPool.getMissTotalCount()+"");
                row.addCell(ejbPool.getPooledBeansCurrentCount()+"");
                row.addCell(ejbPool.getTimeoutTotalCount()+"");
                row.addCell(ejbPool.getWaiterCurrentCount()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

    private Reply getEjbCacheDate(List<WlsEjbcache> cacheRuntime) {
        JsonGrid grid = JsonGrid.buildGrid(cacheRuntime, new JsonGrid.JsonRowHandler<WlsEjbcache>() {
            @Override
            public JsonGrid.JsonRow buildRow(WlsEjbcache ejbCache) {
                int index = 0;
                JsonGrid.JsonRow row = new JsonGrid.JsonRow();
                row.setId(ejbCache.getId()+"");
                index = index +1;
                row.addCell(index+"");
                row.addCell(ejbCache.getServerName());
                row.addCell(ejbCache.getName()+"");
                row.addCell(ejbCache.getName()+"");
                row.addCell(ejbCache.getCacheAccessCount()+"");
                row.addCell(ejbCache.getActivationCount()+"");
                row.addCell(ejbCache.getCacheBeansCurrentCount()+"");
                row.addCell(ejbCache.getCacheHitCount()+"");
                row.addCell(ejbCache.getCacheMissCount()+"");
                row.addCell(ejbCache.getPassivationCount()+"");
                return row;
            }
        });
        return Replys.with(grid).as(Json.class);
    }

}
