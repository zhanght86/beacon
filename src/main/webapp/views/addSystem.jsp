<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="msg" uri="http://mvc.one.sinosoft.com/validation/msg" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:c="http://www.springframework.org/schema/beans">
<head>
    <title>新建监视器</title>
    <%@include file="/WEB-INF/layouts/base.jsp"%>
    <script type="text/javascript">

        /*校验数据*/
        function isValid(form) {
            String.prototype.trim = function(){
                return this.replace(/(^\s*|\s*$)/g,'');
            }
            var appName="^[A-Za-z]+$";
            if (!form.applicationName.value.match(appName)) {
                msgAlert("系统消息", "显示名称必须是英文！");
                return false;
            }
            if (form.cnName.value==null||form.cnName.value=="") {
                msgAlert("系统消息","中文名称不能为空！");
                return false;
            }
            var appIp="^[0-9.]+$";
            if(!form.applicationIp.value.match(appIp)){
                msgAlert("系统消息","主机IP地址必须是数字和\".\"的组合！");
                return false;
            }
            var appPort="^[0-9]+$";
            if(!form.applicationPort.value.match(appPort)||form.applicationPort.value.length>5){
                msgAlert("系统消息","端口必须是5位以内的数字！");
                return false;
            }            
            if(form.interval.value.trim()==""){
                msgAlert("系统消息","轮询间隔不能为空或者空格！");
                return false;
            }
            var appInterval="^[0-9]+$";
            if(!form.interval.value.match(appInterval)||form.interval.value.length>10){
                msgAlert("系统消息","轮询间隔必须是10位以内的数字！");
                return false;
            }
            return true;
        }
    </script>
</head>

<body>
<%@include file="/WEB-INF/layouts/menu.jsp"%>
<div id="layout_center">
    <div class="main">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td>
                    <div class="add_monitor">
                        <%--<h2 class="title2"><b>新建监视器类型　</b>
                            <select name="" class="diySelect" onchange="top.location=this.value;">
                                <optgroup label="应用服务器">
                                    <option selected="selected" value="addSystem.html">应用系统　　</option>
                                </optgroup>
                                <optgroup label="数据库">
                                    <option value="addOracle.html">Oracle</option>
                                </optgroup>
                                <optgroup label="操作系统">
                                    <option value="addLinux.html">Linux</option>
                                </optgroup>
                            </select>
                        </h2>--%>
                        <%@include file="/WEB-INF/layouts/selectMonitorType.jsp"%>
                        <form:form id="addSystem" action="${ctx}/addmonitor/addapp" method="post"
                                   class="form-horizontal" onsubmit="return isValid(this);">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0"
                                   class="add_monitor_box add_form">
                                <tr>
                                    <td colspan="2" class="group_name">基本信息</td>
                                </tr>
                                <tr>
                                    <td width="25%">显示名称<span class="mandatory">*</span></td>
                                    <td><input id="applicationName" name="applicationName"
                                               value="${application.applicationName}" type="text" class="formtext required"/>
                                        <msg:errorMsg property="applicationName" type="message"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="25%">中文名称<span class="mandatory">*</span></td>
                                    <td><input id="cnName" name="cnName" value="${application.cnName}" type="text"
                                               class="formtext"/>
                                        <msg:errorMsg property="cnName" type="message"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>主机IP地址<span class="mandatory">*</span></td>
                                    <td><input id="applicationIp" name="applicationIp"
                                               value="${application.applicationIp}" type="text" class="formtext"
                                               size="30"/>
                                        <msg:errorMsg property="applicationIp" type="message"/>
                                    </td>
                                </tr>
                                <!--<tr>
                                  <td>子网掩码<span class="mandatory">*</span></td>
                                  <td><input name="input3" type="text" class="formtext" value="255.255.255.0" size="30" /></td>
                                </tr>-->
                                <tr>
                                    <td>端口<span class="mandatory">*</span></td>
                                    <td><input id="applicationPort" name="applicationPort"
                                               value="${application.applicationPort}" type="text" class="formtext"
                                               size="8"/>
                                        <msg:errorMsg property="applicationPort" type="message"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>轮询间隔（分钟）<span class="mandatory">*</span></td>
                                    <td><input id="interval" name="interval" value="${application.interval}" type="text"
                                               class="formtext" size="10"/>
                                        <msg:errorMsg property="interval" type="message"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="group_name">&nbsp;</td>
                                    <td class="group_name">
                                        <!--<input type="button" class="buttons" value="确定添加" onclick="save()" />　-->
                                        <input id="submit" type="submit" class="buttons" value="确定添加"/>　
                                        <input type="reset" class="buttons" value="重 置"/>　
                                        <input type="button" class="buttons" value="取 消"
                                               onclick="window.history.back()"/>
                                    </td>
                                </tr>
                            </table>
                        </form:form>
                    </div>
                </td>
                <td width="15">&nbsp;</td>
                <td width="33%" style="vertical-align:top">
                    <div class="conf_box help">
                        <div class="conf_title">
                            <div class="conf_title_r"></div>
                            <div class="conf_title_l"></div>
                            <span>帮助信息</span>
                        </div>
                        <div class="conf_cont_box">
                            <div class="conf_cont">
                                <ul>
                                    <li class="first the_set"><b>新建应用系统监视器</b><br/>创建系统监视器后可根据业务需求配置业务场景。</li>
                                    <li><b>管理业务场景</b><br/>点击“管理业务场景”,可以增加新业务场景或管理已有业务场景。</li>
                                    <li><b>管理URL</b><br/>点击“管理URL”,对应业务场景将URL按流程顺序进行添加与监控配置。</li>
                                    <li><b>管理方法</b><br/>点击“管理方法”,对URL中所执行的方法按顺序进行添加与监控配置</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>
<%@include file="/WEB-INF/layouts/foot.jsp"%>
</body>
</html>
